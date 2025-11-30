package com.sjs395.yourvillagesnames.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class VillageOverlayRenderer {

	private static String title = "";
	private static String subtitle = "";

	private static long fadeIn = 0;
	private static long stay = 0;
	private static long fadeOut = 0;

	private static final float POS_INICIAL = -40f;
	private static final float POS_FINAL_PERCENT = 4f;

	private static long startTime = 0;
	private static boolean active = false;

	public static void showOverlay(String t, String s, long fadeInMs, long stayMs, long fadeOutMs) {
		title = t;
		subtitle = s;
		fadeIn = fadeInMs;
		stay = stayMs;
		fadeOut = fadeOutMs;
		startTime = System.currentTimeMillis();
		active = true;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiEvent.Post event) {
		if (!active)
			return;

		long elapsed = System.currentTimeMillis() - startTime;
		float total = fadeIn + stay + fadeOut;

		if (elapsed > total) {
			active = false;
			return;
		}

		float progress = computeProgress(elapsed);
		renderText(event.getGuiGraphics(), progress);
	}

	private static float computeProgress(long elapsed) {

		if (elapsed < fadeIn) {
			float t = (float) elapsed / fadeIn;
			return easeOutCubic(t);

		} else if (elapsed < fadeIn + stay) {
			return 1f;

		} else {
			float t = (float) (elapsed - fadeIn - stay) / fadeOut;
			return 1f - easeInCubic(t);
		}
	}

	private static float easeOutCubic(float x) {
		return 1f - (float) Math.pow(1f - x, 3);
	}

	private static float easeInCubic(float x) {
		return (float) Math.pow(x, 3);
	}

	private static void renderText(GuiGraphics gui, float progress) {
		Minecraft mc = Minecraft.getInstance();
		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();

		float posFinalPx = height * (POS_FINAL_PERCENT / 100f);

		float y = lerp(POS_INICIAL, posFinalPx, progress);

		int yTitle = (int) y;
		int ySub = yTitle + 15;

		gui.drawCenteredString(mc.font, title, width / 2, yTitle, 0xFFFFFF);
		gui.drawCenteredString(mc.font, subtitle, width / 2, ySub, 0xFFA500);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
