package com.sjs395.yourvillagesnames;

import com.sjs395.yourvillagesnames.client.VillageOverlayRenderer;

import net.neoforged.neoforge.common.NeoForge;

public class YourVillagesNamesClient {
    // Si luego querés mostrar títulos fancier o HUD, lo implementamos aquí (lado cliente).
	public YourVillagesNamesClient() {
		NeoForge.EVENT_BUS.register(VillageOverlayRenderer.class);
	}
}
