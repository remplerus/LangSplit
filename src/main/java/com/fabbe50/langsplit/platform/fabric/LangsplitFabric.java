//? if fabric {
package com.fabbe50.langsplit.platform.fabric;

import com.fabbe50.langsplit.Langsplit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class LangsplitFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Langsplit.init();
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> Langsplit.modifyTooltip(lines));
    }
}
//?}
