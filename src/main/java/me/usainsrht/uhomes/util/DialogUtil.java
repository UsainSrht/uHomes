package me.usainsrht.uhomes.util;

import org.bukkit.entity.Player;
import java.lang.reflect.Method;

public class DialogUtil {

    private static boolean supported = false;
    private static Method showDialogMethod = null;

    static {
        try {
            Class<?> dialogClass = Class.forName("io.papermc.paper.dialog.Dialog");
            for (Method method : Player.class.getMethods()) {
                if (method.getName().equals("showDialog") && method.getParameterCount() == 1) {
                    showDialogMethod = method;
                    break;
                }
            }
            supported = showDialogMethod != null;
        } catch (Exception e) {
            supported = false;
        }
    }

    public static boolean isSupported() {
        return supported;
    }

    public static Method getShowDialogMethod() {
        return showDialogMethod;
    }
}
