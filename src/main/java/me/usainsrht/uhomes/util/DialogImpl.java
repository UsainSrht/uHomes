package me.usainsrht.uhomes.util;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.config.InputGuiConfig;
import me.usainsrht.uhomes.config.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.function.Consumer;

public class DialogImpl {

    private static Component mm(Player player, String text) {
        return MMUtil.deserialize(player, text);
    }

    public static void sendHomeForm(Player player, Home home, Location location, int index, Consumer<String> onSuccess) {
        sendHomeForm(player, home, location, index, onSuccess, null, null);
    }

    private static void sendHomeForm(Player player, Home home, Location location, int index, Consumer<String> onSuccess,
                                     @Nullable String wrongInput, @Nullable Component error) {
        if (!DialogUtil.isSupported()) return;

        try {
            String title = MainConfig.getDialogTitle();
            String text = MainConfig.getDialogText();
            String initial = wrongInput != null
                    ? wrongInput
                    : home != null && home.getName() != null ? home.getName() : "";

            DialogBase base = DialogBase.builder(mm(player, title))
                    .body(HomesGUI.getDialogBodies(home, index, mm(player, text), error))
                    .inputs(Collections.singletonList(DialogInput.text("home_name", mm(player, MainConfig.getDialogInputLabel()))
                            .initial(initial)
                            .build()))
                    .build();

            ActionButton submitBtn = ActionButton.builder(mm(player, MainConfig.getDialogConfirmButton()))
                    .action(DialogAction.customClick((view, audience) -> {
                        String name = view.getText("home_name");
                        if (name == null) return;

                        SchedulerUtil.runNextTick(UHomes.getInstance(), () -> {
                            String trimmed = name.trim();
                            if (!MainConfig.getHomeNameCharLimit().isInBetween(trimmed.length())) {
                                SoundUtil.play(player, MainConfig.getSound("home_name_limit"));
                                sendHomeForm(player, home, location, index, onSuccess, trimmed,
                                        mm(player, MainConfig.getDialogHomeNameLimit(),
                                                Formatter.number("min", MainConfig.getHomeNameCharLimit().getMin()),
                                                Formatter.number("max", MainConfig.getHomeNameCharLimit().getMax()),
                                                Formatter.number("home_name_char_size", trimmed.length()),
                                                Placeholder.unparsed("home_name", trimmed)));
                                return;
                            }
                            if (!trimmed.matches(MainConfig.getHomeNameValidChars())) {
                                SoundUtil.play(player, MainConfig.getSound("home_name_not_valid"));
                                sendHomeForm(player, home, location, index, onSuccess, trimmed,
                                        mm(player, MainConfig.getDialogHomeNameNotValid(),
                                                Placeholder.unparsed("home_name", trimmed),
                                                Placeholder.unparsed("invalid_characters",
                                                        String.join(" ", trimmed.split(MainConfig.getHomeNameValidChars())))));
                                return;
                            }
                            onSuccess.accept(trimmed);
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

    public static void sendConfirmationForm(Player player, InputGuiConfig config, Home home, int index, Runnable onConfirm) {
        sendConfirmationForm(player, config, home, index, onConfirm, null);
    }

    private static void sendConfirmationForm(Player player, InputGuiConfig config, Home home, int index,
                                             Runnable onConfirm, @Nullable String wrongInput) {
        if (!DialogUtil.isSupported()) return;

        try {
            String homeName = home.getName() == null ? "" : home.getName();
            TagResolver homeNamePlaceholder = Placeholder.unparsed("home_name", homeName);
            TagResolver confirmationTextPlaceholder = Placeholder.unparsed("confirmation_text", config.getConfirmationText());
            TagResolver[] resolvers = new TagResolver[]{homeNamePlaceholder, confirmationTextPlaceholder};

            Component error = wrongInput != null
                    ? mm(player, config.getDialogNotMatched(), concat(resolvers, Placeholder.unparsed("input", wrongInput)))
                    : null;

            DialogBase base = DialogBase.builder(mm(player, config.getDialogTitle(), resolvers))
                    .body(HomesGUI.getDialogBodies(home, index,
                            mm(player, config.getDialogText(), resolvers), error))
                    .inputs(Collections.singletonList(DialogInput.text("confirmation", mm(player, config.getDialogInputLabel()))
                            .initial(wrongInput != null ? wrongInput : "")
                            .build()))
                    .build();

            ActionButton submitBtn = ActionButton.builder(mm(player, config.getDialogConfirmButton()))
                    .action(DialogAction.customClick((view, audience) -> {
                        String input = view.getText("confirmation");
                        if (input == null) return;

                        SchedulerUtil.runNextTick(UHomes.getInstance(), () -> {
                            if (input.trim().equalsIgnoreCase(config.getConfirmationText().trim())) {
                                onConfirm.run();
                            } else {
                                SoundUtil.play(player, MainConfig.getSound("confirmation_not_matched"));
                                sendConfirmationForm(player, config, home, index, onConfirm, input.trim());
                            }
                        });
                    }, ClickCallback.Options.builder().build()))
                    .build();

            ActionButton cancelBtn = ActionButton.builder(mm(player, config.getDialogCancelButton())).build();

            Dialog dialog = Dialog.create(factory -> factory
                    .empty()
                    .base(base)
                    .type(DialogType.multiAction(Collections.singletonList(submitBtn))
                            .exitAction(cancelBtn)
                            .build())
            );

            DialogUtil.getShowDialogMethod().invoke(player, dialog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static TagResolver[] concat(TagResolver[] resolvers, TagResolver extra) {
        TagResolver[] combined = new TagResolver[resolvers.length + 1];
        System.arraycopy(resolvers, 0, combined, 0, resolvers.length);
        combined[resolvers.length] = extra;
        return combined;
    }

    private static Component mm(Player player, String text, TagResolver... placeholders) {
        return MMUtil.deserialize(player, text, placeholders);
    }
}
