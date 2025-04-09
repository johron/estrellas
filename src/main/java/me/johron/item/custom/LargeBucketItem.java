package me.johron.item.custom;

import me.johron.util.FluidStorageImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class LargeBucketItem extends Item {
    public static final long CAPACITY = FluidConstants.BUCKET * 8;

    public LargeBucketItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        FluidVariant fluid = getFluid(stack);
        long amount = getAmount(stack);
        if (!fluid.isBlank()) {
            tooltip.add(Text.literal(String.format("%s: %.2f buckets",
                    Registries.FLUID.getId(fluid.getFluid()),
                    (double) amount / FluidConstants.BUCKET)));
        } else {
            tooltip.add(Text.literal("Empty"));
        }
    }

    public static FluidVariant getFluid(ItemStack stack) {
        return FluidVariant.fromNbt(stack.getOrCreateNbt().getCompound("Fluid"));
    }

    public static long getAmount(ItemStack stack) {
        return stack.getOrCreateNbt().getLong("Amount");
    }

    public static void setFluid(ItemStack stack, FluidVariant fluid, long amount) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put("Fluid", fluid.toNbt());
        nbt.putLong("Amount", amount);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
            ItemStack stack = player.getStackInHand(hand);
            BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                FluidState fluidState = world.getFluidState(pos);

                if (!fluidState.isEmpty()) {
                    Fluid fluid = fluidState.getFluid();
                    long currentAmount = getAmount(stack);
                    if (currentAmount < CAPACITY) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        world.playSound(player, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        setFluid(stack, FluidVariant.of(fluid), currentAmount + FluidConstants.BUCKET);
                        return TypedActionResult.success(stack, world.isClient());
                    }
                }
            }

            return TypedActionResult.pass(stack);
        }

        @Override
        public ActionResult useOnBlock(ItemUsageContext context) {
            World world = context.getWorld();
            BlockPos pos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockPos targetPos = pos.offset(direction);
            ItemStack stack = context.getStack();
            PlayerEntity player = context.getPlayer();

            if (player != null && !world.isClient()) {
                long currentAmount = getAmount(stack);
                FluidVariant fluid = getFluid(stack);
                if (currentAmount >= FluidConstants.BUCKET && !fluid.isBlank()) {
                    BlockState targetState = world.getBlockState(targetPos);
                    if (targetState.isAir()) {
                        world.setBlockState(targetPos, fluid.getFluid().getDefaultState().getBlockState());
                        world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        setFluid(stack, fluid, currentAmount - FluidConstants.BUCKET);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        }
}
