package me.johron.item;

import me.johron.Estrellas;
import me.johron.block.ModBlocks;
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
                    .icon(() -> new ItemStack(ModBlocks.CHARCOAL_BLOCK)).entries((displayContext, entries) -> {
                        entries.add(ModBlocks.CHARCOAL_BLOCK);
                        entries.add(ModItems.LARGE_BUCKET);
            }).build());

    public static void registerItemGroups() {
        Estrellas.LOGGER.info("Registering ModItemGroups for " + Estrellas.MOD_ID);
    }
}
