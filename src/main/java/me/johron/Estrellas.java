package me.johron;

import me.johron.block.ModBlocks;
import me.johron.item.ModItemGroups;
import me.johron.item.ModItems;
import me.johron.util.FluidStorageImpl;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Estrellas implements ModInitializer {
	public static final String MOD_ID = "estrellas";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		FluidStorage.ITEM.registerForItems((stack, context) -> new FluidStorageImpl(stack), ModItems.FLUID_BARREL);

		FuelRegistry.INSTANCE.add(ModBlocks.CHARCOAL_BLOCK, 16000);
	}
}