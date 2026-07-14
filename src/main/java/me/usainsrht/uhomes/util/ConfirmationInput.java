package me.usainsrht.uhomes.util;

import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.InputGuiConfig;
import me.usainsrht.uhomes.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public final class ConfirmationInput {

    public enum Type {
        REMOVE,
        RELOCATE
    }

    private ConfirmationInput() {
    }

    public static void open(Player player, Type type, Home home, int index, Runnable onConfirm) {
        InputGuiConfig config = type == Type.REMOVE
                ? MainConfig.getRemoveInput()
                : MainConfig.getRelocateInput();

        if (config.isDialogEnabled() && DialogUtil.isSupported()) {
            DialogImpl.sendConfirmationForm(player, config, home, index, onConfirm);
            return;
        }

        if (config.isAnvilEnabled()) {
            openAnvil(player, config, home, index, onConfirm);
            return;
        }

        onConfirm.run();
    }

    private static void openAnvil(Player player, InputGuiConfig config, Home home, int index, Runnable onConfirm) {
        String homeName = home.getName() == null ? "" : home.getName();

        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .plugin(UHomes.getInstance())
                .jsonTitle(MMUtil.mmStringToJson(config.getAnvilTitle(),
                        Placeholder.unparsed("home_name", homeName),
                        Placeholder.unparsed("confirmation_text", config.getConfirmationText())));

        config.getAnvilSlots().forEach((slot, item) -> {
            if (slot == 0) {
                if (index >= 0) {
                    builder.itemLeft(HomesGUI.getAnvilLeftItem(home, index, config.getAnvilText(),
                            Placeholder.unparsed("home_name", homeName),
                            Placeholder.unparsed("confirmation_text", config.getConfirmationText())));
                } else {
                    item.set("name", config.getAnvilText());
                    builder.itemLeft(ItemUtil.getItemFromYaml(item));
                }
            } else if (slot == 1) {
                builder.itemRight(ItemUtil.getItemFromYaml(item));
            } else if (slot == 2) {
                builder.itemOutput(ItemUtil.getItemFromYaml(item));
            }
        });

        builder.onClick((slot, stateSnapshot) -> {
            if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                return Collections.singletonList(AnvilGUI.ResponseAction.close());
            }
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return Collections.emptyList();
            }

            String input = stateSnapshot.getText().trim();
            if (!matches(input, config.getConfirmationText())) {
                return Arrays.asList(
                        AnvilGUI.ResponseAction.updateJsonTitle(MMUtil.mmStringToJson(config.getAnvilNotConfirmed(),
                                Placeholder.unparsed("home_name", homeName),
                                Placeholder.unparsed("confirmation_text", config.getConfirmationText()),
                                Placeholder.unparsed("input", input)
                        ), true),
                        AnvilGUI.ResponseAction.run(() -> SoundUtil.play(player, MainConfig.getSound("confirmation_not_matched")))
                );
            }

            return Arrays.asList(
                    AnvilGUI.ResponseAction.close(),
                    AnvilGUI.ResponseAction.run(onConfirm)
            );
        });

        builder.open(player);
    }

    private static boolean matches(String input, String expected) {
        return input.equalsIgnoreCase(expected.trim());
    }
}
