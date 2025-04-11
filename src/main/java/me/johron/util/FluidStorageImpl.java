package me.johron.util;

import me.johron.item.custom.FluidBarrelItem;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.item.ItemStack;

public class FluidStorageImpl extends SingleVariantStorage<FluidVariant> {
    private final ItemStack stack;

    public FluidStorageImpl(ItemStack stack) {
        this.stack = stack;

        this.variant = FluidBarrelItem.getFluid(stack);
        this.amount = FluidBarrelItem.getAmount(stack);
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return FluidBarrelItem.CAPACITY_MB;
    }

    @Override
    protected void onFinalCommit() {
        FluidBarrelItem.setFluid(stack, this.variant, this.amount);
    }
}
