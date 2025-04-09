package me.johron.item;

import me.johron.Estrellas;
import me.johron.item.custom.LargeBucketItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item LARGE_BUCKET = registerItem("large_bucket",
            new LargeBucketItem(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Estrellas.MOD_ID, name), item);
    }
}
