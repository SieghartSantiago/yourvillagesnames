package com.sjs395.yourvillagesnames.commands;

import com.sjs395.yourvillagesnames.chat.ChatManager;
import com.sjs395.yourvillagesnames.config.ModConfigHolder;
import com.sjs395.yourvillagesnames.data.FileManager;
import com.sjs395.yourvillagesnames.world.VillageDetector;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CheckVillageCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("villagehere").requires(src -> src.hasPermission(0)).executes(ctx -> {

			ServerPlayer player = ctx.getSource().getPlayer();
			if (player == null)
				return 0;

			var server = ctx.getSource().getServer();
			ServerLevel level = server.overworld();

			BlockPos playerPos = player.blockPosition();

			BlockPos villagePos = VillageDetector.findNearestVillage(level, playerPos, ModConfigHolder.VILLAGE_SEARCH_RADIUS.get());
			boolean isInVillage = villagePos != null;

			if (isInVillage) {
				String id = villagePos.getX() + "_" + villagePos.getZ();

				ChatManager.writeString("You are in the village ", FileManager.searchVillageName(level, id), ctx);

			} else {
				ChatManager.writeString("You are not in a village", "", ctx);
			}

			return Command.SINGLE_SUCCESS;
		}));
	}
}
