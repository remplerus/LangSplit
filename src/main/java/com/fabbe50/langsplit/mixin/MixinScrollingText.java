package com.fabbe50.langsplit.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.11 {
import com.fabbe50.langsplit.ModConfig;
import com.fabbe50.langsplit.TextRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?} else {
/*import net.minecraft.client.gui.components.AbstractWidget;
*///?}

/**
 * Scrolling labels of widgets (buttons, icon buttons, sliders, string widgets).
 * <ul>
 *     <li>&lt;= 1.21.8: these labels go through {@code GuiGraphics#drawString/drawCenteredString}, which
 *     {@link MixinGuiGraphics} already handles - this mixin is intentionally empty there.</li>
 *     <li>1.21.11+: they go through {@code ActiveTextCollector#acceptScrolling}, implemented by
 *     {@code GuiGraphics(Extractor)$RenderingTextCollector}.</li>
 * </ul>
 */
//? if >=26.1 {
@Mixin(targets = "net.minecraft.client.gui.GuiGraphicsExtractor$RenderingTextCollector")
//?} elif >=1.21.11 {
/*@Mixin(targets = "net.minecraft.client.gui.GuiGraphics$RenderingTextCollector")
*///?} else {
/*@Mixin(AbstractWidget.class)
*///?}
public abstract class MixinScrollingText {
    //? if >=1.21.11 {
    @Inject(method = "acceptScrolling(Lnet/minecraft/network/chat/Component;IIIIILnet/minecraft/client/gui/ActiveTextCollector$Parameters;)V",
            at = @At("HEAD"), cancellable = true)
    private void langsplit$acceptScrolling(Component message, int centerX, int minX, int maxX, int minY, int maxY,
                                           ActiveTextCollector.Parameters parameters, CallbackInfo ci) {
        TextRenderHelper.Split split = TextRenderHelper.split(message, 0xFFFFFF);
        if (split == null) return;

        ActiveTextCollector self = (ActiveTextCollector) (Object) this;
        if (ModConfig.inline) {
            // the combined component is not translatable, so this does not recurse into our handler
            self.acceptScrolling(split.inline(), centerX, minX, maxX, minY, maxY, parameters);
        } else {
            Font font = Minecraft.getInstance().font;
            float textY = (minY + maxY - font.lineHeight) / 2f + 1;
            int available = Math.max(1, maxX - minX);
            langsplit$line(self, parameters, font, split.original(), centerX, TextRenderHelper.lineTop(textY, 0), available);
            langsplit$line(self, parameters, font, split.translation(), centerX, TextRenderHelper.lineTop(textY, 1), available);
        }
        ci.cancel();
    }

    /** Draws one squashed, centred line; shrinks it horizontally if it does not fit. */
    @Unique
    private static void langsplit$line(ActiveTextCollector collector, ActiveTextCollector.Parameters parameters, Font font,
                                       Component text, float centerX, float top, int available) {
        FormattedCharSequence sequence = text.getVisualOrderText();
        int width = font.width(sequence);
        float scaleX = width > available ? (float) available / width : 1f;
        Matrix3x2f pose = new Matrix3x2f(parameters.pose()).translate(centerX, top).scale(scaleX, TextRenderHelper.TWO_LINE_SCALE);
        collector.accept(TextAlignment.CENTER, 0, 0, parameters.withPose(pose), sequence);
    }
    //?}
}
