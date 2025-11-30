package com.sjs395.yourvillagesnames.data;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.file.Path;
import java.nio.file.Files;

import net.neoforged.fml.loading.FMLPaths;

public class YourVillagesNamesGenerator {
	private static final List<String> names = new ArrayList<>();
	private static final Random rand = new Random();

	public static void loadNames() {
		try {
			Path configDir = FMLPaths.CONFIGDIR.get();

			Path modFolder = configDir.resolve("yourvillagesnames");
			Files.createDirectories(modFolder);

			Path file = modFolder.resolve("village_names.txt");

			if (Files.exists(file)) {
				try (BufferedReader br = Files.newBufferedReader(file)) {
					String line;
					while ((line = br.readLine()) != null) {
						line = line.trim();
						if (!line.isEmpty())
							names.add(line);
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (names.isEmpty()) {
			names.add("Valle Verde");
			names.add("Aldea Rocablanca");
			names.add("Puerta del Bosque");
			names.add("Villa Alba");
			names.add("Colina Dorada");
			names.add("Río Viejo");
			names.add("Paso del Viento");
			names.add("Mirador del Sol");
		}
	}

	public static String getRandomName() {
		if (names.isEmpty())
			return "Aldea";
		return names.get(rand.nextInt(names.size()));
	}

	public static List<String> getAllNames() {
		return names;
	}
}
