package me.usainsrht.uhomes.gui;

import me.usainsrht.uhomes.config.MainConfig;
import org.bukkit.event.inventory.ClickType;

public enum HomeButtonAction {

    TELEPORT,
    RENAME,
    RELOCATE,
    DELETE,

    HomeButtonAction();

    public static HomeButtonAction getFromClick(ClickType clickType) {
        if (clickType == ClickType.LEFT) return MainConfig.getHomeButtonLeftClick();
        else if (clickType == ClickType.RIGHT) return MainConfig.getHomeButtonRightClick();
        else if (clickType == ClickType.SHIFT_LEFT) return MainConfig.getHomeButtonLeftClickWithShift();
        else if (clickType == ClickType.SHIFT_RIGHT) return MainConfig.getHomeButtonRightClickWithShift();
        return null;
    }

}
