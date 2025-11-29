package com.sjs395.yourvillagesnames.config;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class ModConfigHolder {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Integer> VILLAGE_SEARCH_RADIUS;
    public static final ConfigValue<List<? extends Object>> VILLAGE_IDS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");

        VILLAGE_SEARCH_RADIUS =
                BUILDER.define("detectionDistance", 80);

        List<String> defaultVillageIds = List.of(
                "minecraft:village_desert",
                "minecraft:village_plains",
                "minecraft:village_savanna",
                "minecraft:village_snowy",
                "minecraft:village_taiga"
        );

        VILLAGE_IDS = BUILDER
                .comment("Village IDs")
                .defineList(
                        "villageIds",
                        defaultVillageIds,
                        () -> List.of(),
                        o -> o instanceof String
                );

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
