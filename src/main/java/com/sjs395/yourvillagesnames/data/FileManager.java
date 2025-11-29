package com.sjs395.yourvillagesnames.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;

public class FileManager {
	private static final Gson GSON = new Gson();

	public static File getSaveFile(ServerLevel level) {

		// Carpeta del mundo
		File worldDir = level.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("yourvillagenames")
				.toFile();

		worldDir.mkdirs();

		return new File(worldDir, "villages.json");
	}

	public static int renameVillage(ServerLevel level, String id, String newName) {

		File villagesFile = getSaveFile(level);

		if (!villagesFile.exists())
			return 0;

		try {
			Gson gson = new Gson();
			Map<String, String> villages;

			try (Reader reader = Files.newReader(villagesFile, StandardCharsets.UTF_8)) {
				Type type = new TypeToken<Map<String, String>>() {
					private static final long serialVersionUID = 1L;
				}.getType();
				villages = gson.fromJson(reader, type);
			}

			if (villages == null) {
				return 1;
			}

			// Verificar si existe el ID
			if (!villages.containsKey(id)) {
				return 1; // ID no existe
			}

			// Cambiar el nombre
			villages.put(id, newName);

			// Guardar el JSON actualizado
			try (Writer writer = Files.newWriter(villagesFile, StandardCharsets.UTF_8)) {
				gson.toJson(villages, writer);
			}

			return 2; // Exito

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static void save(ServerLevel level, Map<String, String> villageNames) {
		try {
			File f = FileManager.getSaveFile(level);
			FileWriter writer = new FileWriter(f);
			GSON.toJson(villageNames, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String searchVillageName(ServerLevel level, String id) {
		try {
			File f = getSaveFile(level);

			if (!f.exists()) {
				return null;
			}

			FileReader reader = new FileReader(f);
			java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
				private static final long serialVersionUID = 1L;
			}.getType();

			Map<String, String> map = GSON.fromJson(reader, type);
			reader.close();

			if (map != null) {

				String villageName = map.get(id);

				if (villageName == null || villageName.isBlank()) {
					return null;
				}

				return villageName;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getNearestVillageId(ServerLevel level, BlockPos playerPos) {
		try {
			File f = getSaveFile(level);

			FileReader reader = new FileReader(f);
			java.lang.reflect.Type type = new com.google.common.reflect.TypeToken<Map<String, String>>() {
				private static final long serialVersionUID = 1L;
			}.getType();
			Map<String, String> map = new Gson().fromJson(reader, type);
			reader.close();

			if (map == null || map.isEmpty()) {
				return null;
			}

			String bestId = null;
			double bestDist = Double.MAX_VALUE;

			for (String id : map.keySet()) {

				String[] parts = id.split("_");
				if (parts.length != 2)
					continue;

				int x = Integer.parseInt(parts[0]);
				int z = Integer.parseInt(parts[1]);

				BlockPos villagePos = new BlockPos(x,level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 2, z);

				double dist = villagePos.distSqr(playerPos);

				if (dist < bestDist) {
					bestDist = dist;
					bestId = id;
				}
			}

			return bestId;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Map<String, String> loadAllVillages(ServerLevel level) {
		try {
			File f = getSaveFile(level);

			if (!f.exists()) {
				return Map.of();
			}

			// Leer JSON → Map<String, String>
			FileReader reader = new FileReader(f);
			java.lang.reflect.Type type = new com.google.common.reflect.TypeToken<Map<String, String>>() {
				private static final long serialVersionUID = 1L;
			}.getType();

			Map<String, String> map = new Gson().fromJson(reader, type);
			reader.close();

			if (map == null) {
				return Map.of(); // En caso de JSON vacío
			}

			return map;

		} catch (Exception e) {
			e.printStackTrace();
			return Map.of(); // Fallo → mapa vacío
		}
	}

}