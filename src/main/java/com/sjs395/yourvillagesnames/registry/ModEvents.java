package com.sjs395.yourvillagesnames.registry;

import com.sjs395.yourvillagesnames.network.PacketUpdateOverlay.ClientPayloadHandler;
import com.sjs395.yourvillagesnames.network.PacketUpdateOverlay.NameDisplayData;
import com.sjs395.yourvillagesnames.network.PacketUpdateOverlay.ServerPayloadHandler;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModEvents {

	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event) {
		// Forzar carga de almacenamiento en el overworld
		try {
			event.getServer().overworld();
		} catch (Exception ignored) {
		}
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playBidirectional(NameDisplayData.TYPE, NameDisplayData.STREAM_CODEC, new DirectionalPayloadHandler<>(
				ClientPayloadHandler::handleDataOnMain, ServerPayloadHandler::handleDataOnMain));
	}
}
