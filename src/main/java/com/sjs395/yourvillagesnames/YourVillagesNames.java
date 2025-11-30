package com.sjs395.yourvillagesnames;

import com.sjs395.yourvillagesnames.registry.ModEvents;
import com.sjs395.yourvillagesnames.world.VillageDetector;
import com.sjs395.yourvillagesnames.commands.CheckVillageCommand;
import com.sjs395.yourvillagesnames.commands.SetVillageNameCommand;
import com.sjs395.yourvillagesnames.commands.VillageDetailsCommand;
import com.sjs395.yourvillagesnames.config.ModConfigHolder;
import com.sjs395.yourvillagesnames.data.YourVillagesNamesGenerator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.neoforged.fml.config.ModConfig;

@Mod(YourVillagesNames.MODID)
public class YourVillagesNames {

	public static final String MODID = "yourvillagesnames";

	public YourVillagesNames(ModContainer container) {

		IEventBus modEventBus = container.getEventBus();

		modEventBus.addListener(this::onCommonSetup);
		modEventBus.addListener(ModEvents::register);

		NeoForge.EVENT_BUS.register(ModEvents.class);
		NeoForge.EVENT_BUS.register(VillageDetector.class);
		NeoForge.EVENT_BUS.register(this);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			new YourVillagesNamesClient();
		}

		container.registerConfig(ModConfig.Type.COMMON, ModConfigHolder.SPEC);
	}

	private void onCommonSetup(final FMLCommonSetupEvent event) {
		YourVillagesNamesGenerator.loadNames();
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		CheckVillageCommand.register(event.getDispatcher());
		VillageDetailsCommand.register(event.getDispatcher());
		SetVillageNameCommand.register(event.getDispatcher());
	}
}
