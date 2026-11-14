//? if neoforge {
/*package com.fabbe50.langsplit.platform.neoforge;

import com.fabbe50.langsplit.ClothScreen;
import com.fabbe50.langsplit.Langsplit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = Langsplit.MOD_ID, dist = Dist.CLIENT)
public class LangsplitNeoForge {
    public LangsplitNeoForge(IEventBus modBus, ModContainer container) {
        Langsplit.init();
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> ClothScreen.getConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, event -> Langsplit.modifyTooltip(event.getToolTip()));
    }
}
*///?}
