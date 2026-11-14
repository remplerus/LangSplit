package com.fabbe50.langsplit.platform;

import java.nio.file.Path;

/** Thin loader abstraction (replaces Architectury's Platform). */
public final class Platform {
    private Platform() {}

    public static Path configDir() {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //?} else {
        /*return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?}
    }
}
