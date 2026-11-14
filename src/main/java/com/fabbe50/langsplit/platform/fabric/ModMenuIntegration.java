//? if fabric {
package com.fabbe50.langsplit.platform.fabric;

import com.fabbe50.langsplit.ClothScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClothScreen::getConfigScreen;
    }
}
//?}
