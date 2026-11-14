package com.fabbe50.langsplit;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Version-independent part of the GUI text rendering.
 * The mixins only do the actual (version-specific) drawing.
 */
public final class TextRenderHelper {
    /** Vertical scale applied to both lines in two-line mode. */
    public static final float TWO_LINE_SCALE = 0.75f;
    private static final int LINE_HEIGHT = 9;

    private TextRenderHelper() {}

    /** A component split into the original text and its translation (already styled/coloured). */
    public record Split(Component original, Component translation) {
        public Component inline() {
            return Component.empty().append(original).append(Langsplit.divider).append(translation);
        }
    }

    /**
     * @param color the colour the caller would draw the text with (ARGB or RGB)
     * @return the split text, or {@code null} if the component should be drawn unchanged
     */
    public static Split split(Component component, int color) {
        if (!(component.getContents() instanceof TranslatableContents) || !Langsplit.isLanguageLoaded()) return null;

        Component[] pair;
        try {
            pair = LangUtils.translate(component);
        } catch (RuntimeException e) {
            return null;
        }
        if (pair.length < 2 || pair[0] == null || pair[1] == null) return null;

        String original = pair[0].getString();
        String translated = pair[1].getString();
        if (translated.isEmpty() || original.equals(translated)) return null;

        Style style = component.getStyle();
        TextColor styleColor = style.getColor();
        int base = styleColor != null ? styleColor.getValue() : (color & 0xFFFFFF);
        int rgb = ModConfig.blendColor ? ModConfig.getTextColor(base) : base;

        Component translation = ModConfig.translationBrackets ? LangUtils.encase(pair[1]) : pair[1];
        return new Split(
                pair[0].copy().setStyle(style),
                translation.copy().setStyle(style.withColor(TextColor.fromRgb(rgb))));
    }

    /**
     * Top Y of a line in two-line mode, centred around the original single line at {@code y}.
     * The returned position is in unscaled coordinates; the line itself is drawn with {@link #TWO_LINE_SCALE}.
     */
    public static float lineTop(float y, int line) {
        float middle = y + LINE_HEIGHT / 2f;
        return line == 0 ? middle - LINE_HEIGHT * TWO_LINE_SCALE : middle;
    }
}
