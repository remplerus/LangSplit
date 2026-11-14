package com.fabbe50.langsplit.mixin;

import com.fabbe50.langsplit.ClothScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds a "LangSplit Options" button to the top right of the vanilla language screen. */
@Mixin(LanguageSelectScreen.class)
public abstract class MixinLanguageSelectScreen extends Screen {
    protected MixinLanguageSelectScreen(Component title) {
        super(title);
    }

    @Inject(method = "addFooter", at = @At("TAIL"))
    private void langsplit$addOptionsButton(CallbackInfo ci) {
        Screen self = this;
        this.addRenderableWidget(Button.builder(Component.translatable("text.langsplit.title"),
                        //? if >=26.2 {
                        button -> this.minecraft.gui.setScreen(ClothScreen.getConfigScreen(self)))
                        //?} else {
                        /*button -> this.minecraft.setScreen(ClothScreen.getConfigScreen(self)))
                        *///?}
                .bounds(this.width - 160, 8, 150, 20)
                .build());
    }
}
