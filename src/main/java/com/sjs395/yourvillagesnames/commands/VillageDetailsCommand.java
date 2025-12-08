package com.sjs395.yourvillagesnames.commands;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sjs395.yourvillagesnames.chat.ChatManager;
import com.sjs395.yourvillagesnames.config.ModConfigHolder;
import com.sjs395.yourvillagesnames.data.FileManager;
import com.sjs395.yourvillagesnames.world.VillageDetector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class VillageDetailsCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

		dispatcher.register(Commands.literal("villagedetails").requires(src -> src.hasPermission(0))

				.then(Commands.literal("you_in").executes(ctx -> youIn(ctx)))

				.then(Commands.literal("near").executes(ctx -> near(ctx)))

				.then(Commands.literal("all").executes(ctx -> all(ctx)))

				.then(Commands.literal("id").then(Commands.argument("villageID", StringArgumentType.string())
						.suggests((ctx, builder) -> suggestNearestVillage(ctx, builder)).executes(ctx -> id(ctx)))));
	}

	private static int youIn(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		ServerLevel level = ctx.getSource().getServer().overworld();

		BlockPos village = VillageDetector.findNearestVillage(level, player.blockPosition(),
				ModConfigHolder.VILLAGE_SEARCH_RADIUS.get(), ModConfigHolder.VILLAGE_SEARCH_Y_H.get(),
				ModConfigHolder.VILLAGE_SEARCH_Y_L.get());

		if (village == null) {
			ChatManager.writeError(Component.translatable("error.yourvillagesnames.you_not_in_village"), ctx);
			return 0;
		}

		String id = village.getX() + "_" + village.getZ();
		String name = FileManager.searchVillageName(level, id);

		ChatManager.writeStringCopy(Component.translatable("gui.yourvillagesnames.id"), id, ctx);
		ChatManager.writeString(Component.translatable("gui.yourvillagesnames.name"), name, ctx);

		return Command.SINGLE_SUCCESS;
	}

	private static int near(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		ServerLevel level = ctx.getSource().getServer().overworld();

		String id = FileManager.getNearestVillageId(level, player.blockPosition());

		if (id == null) {
			ChatManager.writeError(Component.translatable("error.yourvillagesnames.no_villages_found"), ctx);
			return 0;
		}

		String name = FileManager.searchVillageName(level, id);

		ChatManager.writeStringCopy(Component.translatable("gui.yourvillagesnames.id"), id, ctx);
		ChatManager.writeString(Component.translatable("gui.yourvillagesnames.name"), name, ctx);

		return Command.SINGLE_SUCCESS;
	}

	private static int all(CommandContext<CommandSourceStack> ctx) {
		ServerLevel level = ctx.getSource().getServer().overworld();
		Map<String, String> villages = FileManager.loadAllVillages(level);

		if (villages.isEmpty()) {
			ChatManager.writeError(Component.translatable("error.yourvillagesnames.no_villages_saved"), ctx);
			return 0;
		}

		for (var entry : villages.entrySet()) {
			ChatManager.writeLine(ctx);
			ChatManager.writeStringCopy(Component.translatable("gui.yourvillagesnames.id"), entry.getKey(), ctx);
			ChatManager.writeString(Component.translatable("gui.yourvillagesnames.name"), entry.getValue(), ctx);
		}

		ChatManager.writeLine(ctx);
		return Command.SINGLE_SUCCESS;
	}

	private static int id(CommandContext<CommandSourceStack> ctx) {
		ServerLevel level = ctx.getSource().getServer().overworld();
		String id = StringArgumentType.getString(ctx, "villageID");

		String name = FileManager.searchVillageName(level, id);

		if (name == null) {
			ChatManager.writeError(Component.translatable("error.yourvillagesnames.village_id_not_exist"), ctx);
			return 0;
		}

		ChatManager.writeStringCopy(Component.translatable("gui.yourvillagesnames.id"), id, ctx);
		ChatManager.writeString(Component.translatable("gui.yourvillagesnames.name"), name, ctx);

		return Command.SINGLE_SUCCESS;
	}

	private static CompletableFuture<Suggestions> suggestNearestVillage(CommandContext<CommandSourceStack> ctx,
			SuggestionsBuilder builder) {

		try {
			ServerPlayer player = ctx.getSource().getPlayer();
			ServerLevel level = ctx.getSource().getServer().overworld();

			String nearestId = FileManager.getNearestVillageId(level, player.blockPosition());

			if (nearestId != null) {
				String typed = builder.getRemaining().toLowerCase();

				if (nearestId.toLowerCase().startsWith(typed)) {
					builder.suggest(nearestId);
				}
			}

		} catch (Exception e) {
		}

		return builder.buildFuture();
	}

}
