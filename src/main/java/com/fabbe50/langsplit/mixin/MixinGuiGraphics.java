package com.fabbe50.langsplit.mixin;

import com.fabbe50.langsplit.ModConfig;
import com.fabbe50.langsplit.TextRenderHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if <1.21.6 {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

/**
 * Hooks the two Component-based text entry points of the GUI renderer.
 * Both delegate to the FormattedCharSequence overloads, which we use for drawing (no recursion).
 * <ul>
 *     <li>&lt;= 1.21.5: {@code GuiGraphics#drawString} returns int, pose is a {@code PoseStack}</li>
 *     <li>1.21.6 - 1.21.11: {@code GuiGraphics#drawString} returns void, pose is a {@code Matrix3x2fStack}</li>
 *     <li>26.1+: class renamed to {@code GuiGraphicsExtractor}, methods to {@code text}/{@code centeredText}</li>
 * </ul>
 */
//? if >=26.1 {
@Mixin(GuiGraphicsExtractor.class)
//?} else {
/*@Mixin(GuiGraphics.class)
*///?}
public abstract class MixinGuiGraphics {
    //? if >=26.1 {
    @Unique
    private GuiGraphicsExtractor langsplit$self() {
        return (GuiGraphicsExtractor) (Object) this;
    }
    //?} else {
    /*@Unique
    private GuiGraphics langsplit$self() {
        return (GuiGraphics) (Object) this;
    }
    *///?}

    /** Draws one (vertically squashed) line with its top-left corner at x/y. */
    @Unique
    private void langsplit$drawScaled(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow) {
        var pose = langsplit$self().pose();
        //? if >=1.21.6 {
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(1f, TextRenderHelper.TWO_LINE_SCALE);
        //?} else {
        /*pose.pushPose();
        pose.translate(x, y, 0f);
        pose.scale(1f, TextRenderHelper.TWO_LINE_SCALE, 1f);
        *///?}
        langsplit$draw(font, text, 0, 0, color, shadow);
        //? if >=1.21.6 {
        pose.popMatrix();
        //?} else {
        /*pose.popPose();
        *///?}
    }

    @Unique
    private void langsplit$draw(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        //? if >=26.1 {
        langsplit$self().text(font, text, x, y, color, shadow);
        //?} else {
        /*langsplit$self().drawString(font, text, x, y, color, shadow);
        *///?}
    }

    /** @return true if the text was drawn by us */
    @Unique
    private boolean langsplit$render(Font font, Component component, int x, int y, int color, boolean shadow, boolean centered) {
        TextRenderHelper.Split split = TextRenderHelper.split(component, color);
        if (split == null) return false;

        if (ModConfig.inline) {
            FormattedCharSequence text = split.inline().getVisualOrderText();
            langsplit$draw(font, text, centered ? x - font.width(text) / 2 : x, y, color, shadow);
        } else {
            FormattedCharSequence original = split.original().getVisualOrderText();
            FormattedCharSequence translation = split.translation().getVisualOrderText();
            float originalX = centered ? x - font.width(original) / 2f : x;
            float translationX = centered ? x - font.width(translation) / 2f : x;
            langsplit$drawScaled(font, original, originalX, TextRenderHelper.lineTop(y, 0), color, shadow);
            langsplit$drawScaled(font, translation, translationX, TextRenderHelper.lineTop(y, 1), color, shadow);
        }
        return true;
    }

    //? if >=26.1 {
    @Inject(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void langsplit$drawString(Font font, Component component, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (langsplit$render(font, component, x, y, color, shadow, false)) ci.cancel();
    }
    //?} elif >=1.21.6 {
    /*@Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void langsplit$drawString(Font font, Component component, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (langsplit$render(font, component, x, y, color, shadow, false)) ci.cancel();
    }
    *///?} else {
    /*@Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void langsplit$drawString(Font font, Component component, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (langsplit$render(font, component, x, y, color, shadow, false)) {
            cir.setReturnValue(x + font.width(component) + (shadow ? 1 : 0));
        }
    }
    *///?}

    //? if >=26.1 {
    @Inject(method = "centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at = @At("HEAD"), cancellable = true)
    //?} else {
    /*@Inject(method = "drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at = @At("HEAD"), cancellable = true)
    *///?}
    private void langsplit$drawCenteredString(Font font, Component component, int x, int y, int color, CallbackInfo ci) {
        if (langsplit$render(font, component, x, y, color, true, true)) ci.cancel();
    }
}
