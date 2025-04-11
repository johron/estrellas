package me.johron.estrellas.item;

import me.johron.estrellas.Estrellas;
import me.johron.estrellas.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup ESTRELLAS_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Estrellas.MOD_ID, "estrellas"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.estrellas"))
                    .icon(() -> new ItemStack(ModItems.FLUID_BARREL)).entries((displayContext, entries) -> {
                        entries.add(ModBlocks.CHARCOAL_BLOCK);
                        entries.add(ModItems.FLUID_BARREL);
            }).build());

    public static void registerItemGroups() {
    }
}
