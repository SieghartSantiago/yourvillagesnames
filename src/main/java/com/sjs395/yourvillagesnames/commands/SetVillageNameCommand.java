package com.sjs395.yourvillagesnames.commands;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SetVillageNameCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("setvillagename").requires(src -> src.hasPermission(0))

				.then(Commands.literal("you_in")
						.then(Commands.argument("villageName", StringArgumentType.greedyString())
								.executes(ctx -> youIn(ctx))))

				.then(Commands.literal("near").then(
						Commands.argument("villageName", StringArgumentType.greedyString()).executes(ctx -> near(ctx))))

				.then(Commands.literal("id")
						.then(Commands.argument("villageID", StringArgumentType.string())
								.suggests((ctx, builder) -> suggestNearestVillage(ctx, builder))
								.then(Commands.argument("villageName", StringArgumentType.greedyString())
										.executes(ctx -> id(ctx))))));
	}

	private static int youIn(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		ServerLevel level = ctx.getSource().getServer().overworld();

		String newName = StringArgumentType.getString(ctx, "villageName");

		BlockPos village = VillageDetector.findNearestVillage(level, player.blockPosition(),
				ModConfigHolder.VILLAGE_SEARCH_RADIUS.get());

		if (village == null) {
			ChatManager.writeError("ERROR: You are not in a village", ctx);
			return 0;
		}

		String id = village.getX() + "_" + village.getZ();

		changeName(level, id, newName, ctx);

		return Command.SINGLE_SUCCESS;
	}

	private static int near(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		ServerLevel level = ctx.getSource().getServer().overworld();

		String id = FileManager.getNearestVillageId(level, player.blockPosition());

		String newName = StringArgumentType.getString(ctx, "villageName");

		if (id == null) {
			ChatManager.writeError("ERROR: No villages found", ctx);
			return 0;
		}

		changeName(level, id, newName, ctx);

		return Command.SINGLE_SUCCESS;
	}

	private static int id(CommandContext<CommandSourceStack> ctx) {
		ServerLevel level = ctx.getSource().getServer().overworld();
		String id = StringArgumentType.getString(ctx, "villageID");

		String name = FileManager.searchVillageName(level, id);

		if (name == null) {
			ChatManager.writeError("ERROR: Village ID does not exist", ctx);
			return 0;
		}

		String newName = StringArgumentType.getString(ctx, "villageName");

		changeName(level, id, newName, ctx);

		return Command.SINGLE_SUCCESS;
	}

	private static void changeName(ServerLevel level, String id, String newName,
			CommandContext<CommandSourceStack> ctx) {
		switch (FileManager.renameVillage(level, id, newName)) {
		case -1:
			ChatManager.writeError("ERROR: -1", ctx);
			break;
		case 0:
			ChatManager.writeError("ERROR: No file found", ctx);
			break;
		case 1:
			ChatManager.writeError("ERROR: No ID found", ctx);
			break;
		case 2:
			ChatManager.writeStringCopy("Village selected: ", id, ctx);
			ChatManager.writeString("New name: ", newName, ctx);
			break;
		}

		VillageDetector.setLoaded(false);
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