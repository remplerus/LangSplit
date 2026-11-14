package com.fabbe50.langsplit;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Loader-independent core of the mod. */
public final class Langsplit {
    public static final String MOD_ID = "langsplit";
    public static final String divider = "  -  ";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Secondary language we last tried to load (also set when loading failed, so we do not retry every frame). */
    private static String loadedLanguage = null;
    private static ClientLanguage clientLanguage = null;

    private Langsplit() {}

    /** Called once from the loader-specific client entrypoint. */
    public static void init() {
        ModConfig.register();
    }

    private static void setupLanguage(LanguageManager manager, String secondary) {
        loadedLanguage = secondary;
        clientLanguage = null;
        LanguageInfo info = manager.getLanguage(secondary);
        if (info == null) {
            LOGGER.warn("Unknown secondary language: {}", secondary);
            return;
        }
        clientLanguage = ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(), Lists.newArrayList(secondary), info.bidirectional());
        LOGGER.info("Secondary language initialized: {} ({})", secondary, clientLanguage.getOrDefault("language.name", secondary));
    }

    /** @return true if a secondary language (different from the game language) is configured and loaded. */
    public static boolean isLanguageLoaded() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || ModConfig.language == null) return false;
        LanguageManager manager = minecraft.getLanguageManager();
        if (manager == null) return false;

        String secondary = ModConfig.language;
        if (secondary.equals(manager.getSelected())) return false;
        if (!secondary.equals(loadedLanguage)) setupLanguage(manager, secondary);
        return clientLanguage != null;
    }

    /** Forces the secondary language to be reloaded on next use (config change, resource reload). */
    public static void invalidate() {
        loadedLanguage = null;
        clientLanguage = null;
    }

    public static ClientLanguage getClientLanguage() {
        return clientLanguage;
    }

    /** Adds translations to an item tooltip. The list is modified in place. */
    public static void modifyTooltip(List<Component> lines) {
        if (!isLanguageLoaded()) return;

        List<Component> newTooltip = new ArrayList<>(lines.size() * 2);
        for (Component component : lines) {
            newTooltip.add(component);
            Component[] pair;
            try {
                pair = LangUtils.translate(component);
            } catch (RuntimeException e) {
                continue;
            }
            if (pair.length < 2 || pair[1] == null) continue;

            Component translation = ModConfig.translationBrackets ? LangUtils.encase(pair[1]) : pair[1];
            String text = translation.getString();
            if (text.isEmpty() || text.equals("[]") || text.equals(component.getString())) continue;

            Style style = component.getStyle();
            TextColor color = style.getColor();
            int base = color != null ? color.getValue() : 0xFFFFFF;
            int rgb = ModConfig.blendColor ? ModConfig.getTextColor(base) : base;
            Component styled = translation.plainCopy().withStyle(style.withColor(TextColor.fromRgb(rgb)));

            if (ModConfig.inline) {
                newTooltip.set(newTooltip.size() - 1, LangUtils.combine(component, styled, LangUtils.CombineType.DIVIDER));
            } else {
                newTooltip.add(styled);
            }
        }

        lines.clear();
        lines.addAll(newTooltip);
    }
}
