package com.fabbe50.langsplit;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public final class ClothScreen {
    private static final String FALLBACK = "English (US) American English";

    private ClothScreen() {}

    public static Screen getConfigScreen(Screen parent) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("text.langsplit.title"));

        Map<String, String> languages = getLocalizedLanguagesFromGame();

        var entryBuilder = builder.entryBuilder();
        var general = builder.getOrCreateCategory(Component.translatable("text.langsplit.category.general"));
        general.addEntry(entryBuilder.startDropdownMenu(Component.translatable("text.langsplit.option.language"),
                DropdownMenuBuilder.TopCellElementBuilder.of(ModConfig.language,
                        s -> languages.getOrDefault(s, "en_us"),
                        t -> {
                            if (languages.containsKey(t)) {
                                return Component.literal(t);
                            } else if (languages.containsValue(t)) {
                                LanguageInfo info = Minecraft.getInstance().getLanguageManager().getLanguage(t);
                                if (info != null) {
                                    return info.toComponent().copy().append(" ").append(Component.translatable("text.langsplit.language." + t));
                                }
                            }
                            return Component.literal(FALLBACK);
                        }),
                DropdownMenuBuilder.CellCreatorBuilder.of(t -> Component.literal(languages.containsKey(t) ? t : FALLBACK)))
                .setDefaultValue("en_us")
                .setSelections(Lists.newArrayList(languages.keySet()))
                .setSaveConsumer(s -> ModConfig.language = s)
                .setSuggestionMode(false)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("text.langsplit.option.inline"), ModConfig.inline)
                .setDefaultValue(false)
                .setSaveConsumer(s -> ModConfig.inline = s)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("text.langsplit.option.translationbrackets"), ModConfig.translationBrackets)
                .setDefaultValue(false)
                .setSaveConsumer(s -> ModConfig.translationBrackets = s)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("text.langsplit.option.blendcolor"), ModConfig.blendColor)
                .setDefaultValue(true)
                .setSaveConsumer(s -> ModConfig.blendColor = s)
                .setTooltip(
                        Component.translatable("text.langsplit.option.blendcolor.desc"),
                        Component.translatable("text.langsplit.option.blendcolor.desc2"),
                        Component.translatable("text.langsplit.option.blendcolor.desc3"))
                .build());
        general.addEntry(entryBuilder.startIntSlider(Component.translatable("text.langsplit.option.blendingratio"), Math.round(ModConfig.blendingRatio * 100), 0, 100)
                .setDefaultValue(35)
                .setSaveConsumer(s -> ModConfig.blendingRatio = s / 100f)
                .setTextGetter(v -> Component.translatable("text.langsplit.option.blendingratio.value", v))
                .setTooltip(
                        Component.translatable("text.langsplit.option.blendingratio.desc"),
                        Component.translatable("text.langsplit.option.blendingratio.desc2"))
                .build());
        general.addEntry(entryBuilder.startColorField(Component.translatable("text.langsplit.option.color"), ModConfig.textColor)
                .setDefaultValue(0x77ff77)
                .setSaveConsumer(s -> ModConfig.textColor = s)
                .build());

        return builder.setSavingRunnable(() -> {
            ModConfig.save();
            ModConfig.load();
        }).build();
    }

    /** @return display name ("Deutsch (Deutschland) German") -> language code ("de_de") */
    private static Map<String, String> getLocalizedLanguagesFromGame() {
        LanguageManager manager = Minecraft.getInstance().getLanguageManager();
        Map<String, String> formattedNames = new HashMap<>();
        for (Map.Entry<String, LanguageInfo> entry : manager.getLanguages().entrySet()) {
            String code = entry.getKey();
            String english = Language.getInstance().getOrDefault("text.langsplit.language." + code, code);
            formattedNames.put(entry.getValue().toComponent().getString() + " " + english, code);
        }
        return formattedNames;
    }
}
