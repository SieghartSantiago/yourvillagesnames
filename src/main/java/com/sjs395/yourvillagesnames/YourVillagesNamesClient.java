package com.sjs395.yourvillagesnames;

import com.sjs395.yourvillagesnames.client.VillageOverlayRenderer;
import net.neoforged.neoforge.common.NeoForge;

public class YourVillagesNamesClient {
	public YourVillagesNamesClient() {
		NeoForge.EVENT_BUS.register(VillageOverlayRenderer.class);
	}
}
