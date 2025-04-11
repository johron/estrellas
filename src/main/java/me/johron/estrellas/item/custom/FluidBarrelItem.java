package me.johron.estrellas.item.custom;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class FluidBarrelItem extends Item {

    // Store capacity in millibuckets (mB)
    public static final long CAPACITY_MB = FluidConstants.BUCKET * 6 * 1000 / FluidConstants.BUCKET; // 6000 mB

    public static final long ONE_BUCKET_MB = 1000;

    public FluidBarrelItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        FluidVariant fluid = getFluid(stack);
        long amountMB = getAmount(stack);
        if (!fluid.isBlank()) {
            tooltip.add(Text.literal(String.format("%s - %d/%d \uD83E\uDEA3",
                    Registries.FLUID.getId(
                            fluid.getFluid()).getPath().substring(0, 1).toUpperCase() +
                            Registries.FLUID.getId(fluid.getFluid()).getPath().substring(1),
                    amountMB / 1000, CAPACITY_MB / 1000)).formatted(Formatting.GOLD));
        } else {
            tooltip.add(Text.translatable("tooltip.estrellas.fluid_empty").formatted(Formatting.GRAY));
        }
    }

    public static FluidVariant getFluid(ItemStack stack) {
        return FluidVariant.fromNbt(stack.getOrCreateNbt().getCompound("Fluid"));
    }

    public static long getAmount(ItemStack stack) {
        return stack.getOrCreateNbt().getLong("Amount");
    }

    public static void setFluid(ItemStack stack, FluidVariant fluid, long amountMB) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put("Fluid", fluid.toNbt());
        nbt.putLong("Amount", amountMB);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);

        if (transferToBucket(stack, player)) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 0.5F, 1.0F);
            world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return TypedActionResult.success(stack);
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            FluidState fluidState = world.getFluidState(pos);
            Fluid fluid = fluidState.getFluid();
            FluidVariant currentFluid = getFluid(stack);

            if (!fluidState.isEmpty() && (currentFluid.isBlank() || currentFluid.getFluid() == fluid)
                    && getAmount(stack) + ONE_BUCKET_MB <= CAPACITY_MB) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                world.playSound(player, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 0.7F);

                FluidVariant newFluid = FluidVariant.of(fluid);
                setFluid(stack, newFluid, getAmount(stack) + ONE_BUCKET_MB);

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction direction = context.getSide();
        ItemStack stack = context.getStack();
        FluidVariant fluid = getFluid(stack);
        long amountMB = getAmount(stack);

        if (!fluid.isBlank() && amountMB >= ONE_BUCKET_MB) {
            BlockPos placePos = pos.offset(direction);
            BlockState state = world.getBlockState(placePos);

            if (state.isAir() || state.isReplaceable()) {
                Fluid fluidInstance = fluid.getFluid();
                FluidState fluidState = fluidInstance.getDefaultState();

                world.setBlockState(placePos, fluidState.getBlockState());
                world.playSound(context.getPlayer(), placePos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 0.7F);

                long remainingAmount = amountMB - ONE_BUCKET_MB;
                setFluid(stack, remainingAmount > 0 ? fluid : FluidVariant.blank(), remainingAmount);

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    public static boolean transferToBucket(ItemStack largeBucketStack, PlayerEntity player) {
        FluidVariant fluid = getFluid(largeBucketStack);
        long amountMB = getAmount(largeBucketStack);

        // Check if the fluid barrel has at least 1000 mB
        if (!fluid.isBlank() && amountMB >= ONE_BUCKET_MB) {
            // Check the main hand and offhand for an empty bucket
            for (Hand hand : Hand.values()) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() == Items.BUCKET) {
                    // Add the filled bucket to the inventory
                    player.getInventory().insertStack(new ItemStack(fluid.getFluid().getBucketItem(), 1));

                    // Keep the remaining empty buckets in the hand
                    if (stack.getCount() > 1) {
                        stack.decrement(1);
                    } else {
                        player.setStackInHand(hand, ItemStack.EMPTY);
                    }

                    // Deduct 1000 mB from the fluid barrel
                    setFluid(largeBucketStack, amountMB > ONE_BUCKET_MB ? fluid : FluidVariant.blank(), amountMB - ONE_BUCKET_MB);

                    return true; // Transfer successful
                }
            }
        }

        return false; // Transfer failed
    }
}
