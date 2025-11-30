package com.sjs395.yourvillagesnames.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.sjs395.yourvillagesnames.client.VillageOverlayRenderer;

import io.netty.buffer.ByteBuf;

public class PacketUpdateOverlay {
	public record NameDisplayData(boolean entering, String name, int fadeIn, int stay, int fadeOut)
			implements CustomPacketPayload {

		public static final CustomPacketPayload.Type<NameDisplayData> TYPE = new CustomPacketPayload.Type<>(
				ResourceLocation.fromNamespaceAndPath("mymod", "my_data"));

		public static final StreamCodec<ByteBuf, NameDisplayData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, NameDisplayData::entering, ByteBufCodecs.STRING_UTF8, NameDisplayData::name,
				ByteBufCodecs.INT, NameDisplayData::fadeIn, ByteBufCodecs.INT, NameDisplayData::stay, ByteBufCodecs.INT,
				NameDisplayData::fadeOut, NameDisplayData::new);

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public class ClientPayloadHandler {
		public static void handleDataOnMain(final NameDisplayData data, final IPayloadContext context) {
			VillageOverlayRenderer.showOverlay(data.entering ? "Entrando a" : "Saliendo de", data.name, data.fadeIn, data.stay, data.fadeOut);
		}
	}

	public class ServerPayloadHandler {
		public static void handleDataOnMain(final NameDisplayData data, final IPayloadContext context) {
		}
	}

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playBidirectional(NameDisplayData.TYPE, NameDisplayData.STREAM_CODEC, new DirectionalPayloadHandler<>(
				ClientPayloadHandler::handleDataOnMain, ServerPayloadHandler::handleDataOnMain));
	}

}
