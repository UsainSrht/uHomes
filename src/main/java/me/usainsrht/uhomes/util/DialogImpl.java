package me.usainsrht.uhomes.util;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.function.Consumer;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

public class DialogImpl {

    private static Component mm(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static void sendHomeForm(Player player, Home home, Location location, Consumer<String> onSuccess) {
        if (!DialogUtil.isSupported()) return;

        try {
            String title = MainConfig.getDialogTitle();
            String text = MainConfig.getDialogText();
            String initial = home != null && home.getName() != null ? home.getName() : "";

            DialogBase base = DialogBase.builder(mm(title))
                    .body(Collections.singletonList(DialogBody.plainMessage(mm(text))))
                    .inputs(Collections.singletonList(DialogInput.text("home_name", mm("Home Name"))
                            .initial(initial)
                            .build()))
                    .build();

            ActionButton submitBtn = ActionButton.builder(mm("<green>✔ Confirm ✔"))
                    .action(DialogAction.customClick((view, audience) -> {
                        String name = view.getText("home_name");
                        if (name == null) return;
                        
                        SchedulerUtil.runNextTick(UHomes.getInstance(), () -> {
                            onSuccess.accept(name);
                        });
                    }, ClickCallback.Options.builder().build()))
                    .build();

            Dialog dialog = Dialog.create(factory -> factory
                    .empty()
                    .base(base)
                    .type(DialogType.multiAction(Collections.singletonList(submitBtn)).build())
            );

            DialogUtil.getShowDialogMethod().invoke(player, dialog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
