package com.sjs395.yourvillagesnames.chat;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class ChatManager {

	static int SIZE_LINE = 15;
	static String CHAR_LINE = "-";

	public static void writeLine(CommandContext<CommandSourceStack> ctx) {
		String line = CHAR_LINE.repeat(SIZE_LINE);

		ctx.getSource().sendSuccess(() -> Component.literal(line), false);
	}

	public static void writeStringCopy(String txtWhite, String txtGreen, CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal(txtWhite)
				.append(Component.literal(txtGreen).withStyle(s -> s.withColor(0x00FF00)
						.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, txtGreen)).withHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy"))))),
				false);
	}

	public static void writeString(String txtWhite, String txtOrange, CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal(txtWhite)
				.append(Component.literal(txtOrange).withStyle(s -> s.withColor(0xFFA500))), false);
	}
	
	public static void writeError(String msg, CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendFailure(Component.literal(msg).withStyle(ChatFormatting.RED));
	}
}