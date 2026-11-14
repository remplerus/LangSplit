package com.fabbe50.langsplit;

import com.fabbe50.langsplit.platform.Platform;
import net.minecraft.network.chat.TextColor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;

public final class ModConfig {
    public static Path configFile;

    public static String language = "en_us";
    public static boolean inline = false;
    public static boolean translationBrackets = false;

    public static boolean blendColor = true;
    public static float blendingRatio = 0.35f;
    public static int textColor = 0x77ff77;

    private ModConfig() {}

    public static void register() {
        configFile = Platform.configDir().resolve("langsplit.properties");
        load();
    }

    public static void load() {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configFile)) {
            properties.load(in);
        } catch (NoSuchFileException ignored) {
            // first start: defaults are written below
        } catch (IOException e) {
            Langsplit.LOGGER.error("Could not read {}", configFile, e);
        }

        language = properties.getProperty("language", "en_us");
        inline = Boolean.parseBoolean(properties.getProperty("inline", "false"));
        translationBrackets = Boolean.parseBoolean(properties.getProperty("translationBrackets", "false"));
        blendColor = Boolean.parseBoolean(properties.getProperty("blendTextColor", "true"));
        try {
            blendingRatio = Math.clamp(Float.parseFloat(properties.getProperty("blendingRatio", "0.35")), 0f, 1f);
        } catch (NumberFormatException e) {
            blendingRatio = 0.35f;
        }
        int r = parseChannel(properties.getProperty("textColorRed"), 119);
        int g = parseChannel(properties.getProperty("textColorGreen"), 255);
        int b = parseChannel(properties.getProperty("textColorBlue"), 119);
        textColor = (r << 16) | (g << 8) | b;

        save();
        Langsplit.invalidate();
    }

    private static int parseChannel(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Math.clamp(Integer.parseInt(value.trim()), 0, 255);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void save() {
        try {
            Files.createDirectories(configFile.getParent());
            try (Writer out = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
                out.write("language=" + language + "\n");
                out.write("inline=" + inline + "\n");
                out.write("translationBrackets=" + translationBrackets + "\n");
                out.write("blendTextColor=" + blendColor + "\n");
                out.write("blendingRatio=" + blendingRatio + "\n");
                out.write("textColorRed=" + ((textColor >> 16) & 255) + "\n");
                out.write("textColorGreen=" + ((textColor >> 8) & 255) + "\n");
                out.write("textColorBlue=" + (textColor & 255) + "\n");
            }
        } catch (IOException e) {
            Langsplit.LOGGER.error("Could not write {}", configFile, e);
        }
    }

    public static int getTextColor(TextColor original) {
        return getTextColor(original != null ? original.getValue() : 0xffffff);
    }

    /** Blends the configured translation colour with the original colour (RGB, no alpha). */
    public static int getTextColor(int originalColor) {
        originalColor &= 0xffffff;
        if (blendColor && originalColor != 0xffffff) {
            float ratio = Math.clamp(blendingRatio, 0f, 1f);
            float inverse = 1.0f - ratio;

            int oR = (originalColor >> 16) & 0xff;
            int oG = (originalColor >> 8) & 0xff;
            int oB = originalColor & 0xff;

            int tR = (textColor >> 16) & 0xff;
            int tG = (textColor >> 8) & 0xff;
            int tB = textColor & 0xff;

            int newR = (int) (oR * inverse + tR * ratio);
            int newG = (int) (oG * inverse + tG * ratio);
            int newB = (int) (oB * inverse + tB * ratio);
            return newR << 16 | newG << 8 | newB;
        }
        return textColor;
    }
}
