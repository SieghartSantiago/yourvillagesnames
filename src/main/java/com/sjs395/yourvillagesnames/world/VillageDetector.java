package com.sjs395.yourvillagesnames.world;

import com.mojang.datafixers.util.Pair;
import com.sjs395.yourvillagesnames.config.ModConfigHolder;
import com.sjs395.yourvillagesnames.data.FileManager;
import com.sjs395.yourvillagesnames.data.YourVillagesNamesGenerator;
import com.sjs395.yourvillagesnames.network.PacketUpdateOverlay.NameDisplayData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VillageDetector {
	private static final Map<String, String> villageNames = new HashMap<>();
	private static boolean loaded = false;

	private static final Map<UUID, Boolean> playerInVillage = new HashMap<>();
	private static final Map<UUID, String> lastVillageId = new HashMap<>();

	private static double getDistance2D(BlockPos p1, BlockPos p2) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getZ() - p1.getZ();
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static void setLoaded(boolean newLoaded) {
		loaded = newLoaded;
	}

	private static void sendTitle(ServerPlayer player, boolean entering, String nameVillage, int fadeInTicks,
			int stayTicks, int fadeOutTicks) {

		PacketDistributor.sendToPlayer(player,
				new NameDisplayData(entering, nameVillage, fadeInTicks, stayTicks, fadeOutTicks));

	}

	private static void load(ServerLevel level) {
		if (loaded)
			return;
		loaded = true;

		try {
			Map<String, String> map = FileManager.loadAllVillages(level);

			if (map != null) {
				villageNames.clear();
				villageNames.putAll(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BlockPos findNearestVillage(ServerLevel level, BlockPos origin, int radius) {
		var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

		double bestVillageDistance = -1;
		BlockPos bestVillagePos = null;
		
		String[] VILLAGES_ID = ModConfigHolder.VILLAGE_IDS.get().toArray(String[]::new);

		for (int i = 0; i < VILLAGES_ID.length; i++) {
			var villageKey = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(VILLAGES_ID[i]));

			var optVillageHolder = structureRegistry.getHolder(villageKey);
			if (optVillageHolder.isEmpty()) {
				return null;
			}

			HolderSet<Structure> villageSet = HolderSet.direct(optVillageHolder.get());

			Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
					.findNearestMapStructure(level, villageSet, origin, radius, false);

			if (result == null)
				continue;

			level.getChunkSource().getChunkNow(result.getFirst().getX() >> 4, result.getFirst().getZ() >> 4);

			BlockPos villagePos = result.getFirst().atY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
					result.getFirst().getX(), result.getFirst().getZ()));

			double distanceVillage = getDistance2D(villagePos, origin);

			if (distanceVillage <= radius && (distanceVillage <= bestVillageDistance || bestVillageDistance == -1)) {
				bestVillageDistance = distanceVillage;
				bestVillagePos = villagePos;
			}
		}

		return bestVillagePos;

	}
	
	public static List<String> splitText(String text, int maxLen) {
	    List<String> lines = new ArrayList<>();
	    StringBuilder current = new StringBuilder();

	    for (String word : text.split(" ")) {
	        if (current.length() + word.length() + 1 > maxLen) {
	            lines.add(current.toString().trim());
	            current = new StringBuilder();
	        }
	        current.append(word).append(" ");
	    }

	    if (!current.isEmpty()) {
	        lines.add(current.toString().trim());
	    }

	    return lines;
	}

	public static void placeVillageSign(ServerLevel level, BlockPos centerPos, String villageName) {
		BlockPos signPos = centerPos.above();
		BlockPos supportPos = centerPos;

		level.setBlock(supportPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
		level.setBlock(signPos, Blocks.OAK_SIGN.defaultBlockState(), 3);

		BlockEntity be = level.getBlockEntity(signPos);
		if (be instanceof SignBlockEntity sign) {

			SignText front = sign.getFrontText();

			List<String> result = splitText(villageName, 15);

			front = front.setMessage(0, Component.literal("Welcome to"));

			for (int i = 0; i < 3; i++) {
			    String line = (i < result.size()) ? result.get(i) : "";
			    front = front.setMessage(i + 1, Component.literal(line));
			}

			sign.setText(front, false);

			sign.setChanged();
			level.sendBlockUpdated(signPos, sign.getBlockState(), sign.getBlockState(), 3);
		}
	}

	@SubscribeEvent
	public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {

		var server = event.getServer();
		if (server == null)
			return;

		ServerLevel level = server.overworld();
		if (level == null)
			return;

		load(level);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {

			BlockPos playerPos = player.blockPosition();
			UUID uuid = player.getUUID();

			BlockPos villagePos = findNearestVillage(level, playerPos, ModConfigHolder.VILLAGE_SEARCH_RADIUS.get());
			boolean isInVillage = villagePos != null;
			boolean wasInVillage = playerInVillage.getOrDefault(uuid, false);

			if (isInVillage) {
				String id = villagePos.getX() + "_" + villagePos.getZ();

				System.out.println("En la aldea: " + id);
				if (!villageNames.containsKey(id)) {
					String newName = YourVillagesNamesGenerator.getRandomName();
					villageNames.put(id, newName);
					FileManager.save(level, villageNames);
					placeVillageSign(level, villagePos, newName);
				}

				if (!wasInVillage || !id.equals(lastVillageId.get(uuid))) {
					sendTitle(player, true, villageNames.get(id), 500, 2000, 500);
				}

				lastVillageId.put(uuid, id);

			} else {
				if (wasInVillage) {
					String id = lastVillageId.get(uuid);
					if (id != null && villageNames.containsKey(id)) {
						sendTitle(player, false, villageNames.get(id), 500, 2000, 500);
					} else {
						sendTitle(player, false, "la aldea", 500, 2000, 500);
					}
				}
			}
			playerInVillage.put(uuid, isInVillage);
		}

	}

}
