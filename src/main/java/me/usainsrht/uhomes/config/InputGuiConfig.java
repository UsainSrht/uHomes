package me.usainsrht.uhomes.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class InputGuiConfig {

    private final String confirmationText;
    private final boolean dialogEnabled;
    private final String dialogTitle;
    private final String dialogText;
    private final String dialogInputLabel;
    private final String dialogConfirmButton;
    private final String dialogCancelButton;
    private final String dialogNotMatched;
    private final boolean anvilEnabled;
    private final String anvilTitle;
    private final String anvilText;
    private final String anvilNotConfirmed;
    private final HashMap<Integer, ConfigurationSection> anvilSlots;

    public InputGuiConfig(String confirmationText,
                          boolean dialogEnabled, String dialogTitle, String dialogText,
                          String dialogInputLabel, String dialogConfirmButton,
                          String dialogCancelButton, String dialogNotMatched,
                          boolean anvilEnabled, String anvilTitle, String anvilText,
                          String anvilNotConfirmed, HashMap<Integer, ConfigurationSection> anvilSlots) {
        this.confirmationText = confirmationText;
        this.dialogEnabled = dialogEnabled;
        this.dialogTitle = dialogTitle;
        this.dialogText = dialogText;
        this.dialogInputLabel = dialogInputLabel;
        this.dialogConfirmButton = dialogConfirmButton;
        this.dialogCancelButton = dialogCancelButton;
        this.dialogNotMatched = dialogNotMatched;
        this.anvilEnabled = anvilEnabled;
        this.anvilTitle = anvilTitle;
        this.anvilText = anvilText;
        this.anvilNotConfirmed = anvilNotConfirmed;
        this.anvilSlots = anvilSlots;
    }

    public static InputGuiConfig fromConfig(ConfigurationSection section, String defaultConfirmationText) {
        if (section == null) {
            return new InputGuiConfig(defaultConfirmationText,
                    true, "", "", "Confirmation", "<green>✔ Confirm ✔",
                    "<gray>✖ Cancel", "<red>Type <confirmation_text> to confirm. You entered <input>.",
                    true, "", defaultConfirmationText, "",
                    new HashMap<>());
        }

        String confirmationText = section.getString("confirmation_text", defaultConfirmationText);

        ConfigurationSection dialog = section.getConfigurationSection("dialog");
        boolean dialogEnabled = dialog == null || dialog.getBoolean("enabled", true);
        String dialogTitle = dialog != null ? dialog.getString("title", "") : "";
        String dialogText = dialog != null ? dialog.getString("text", "") : "";
        String dialogInputLabel = dialog != null ? dialog.getString("input_label", "Confirmation") : "Confirmation";
        String dialogConfirmButton = dialog != null ? dialog.getString("confirm_button", "<green>✔ Confirm ✔") : "<green>✔ Confirm ✔";
        String dialogCancelButton = dialog != null ? dialog.getString("cancel_button", "<gray>✖ Cancel") : "<gray>✖ Cancel";
        String dialogNotMatched = dialog != null ? dialog.getString("not_matched", "<red>Type <confirmation_text> to confirm. You entered <input>.") : "<red>Type <confirmation_text> to confirm. You entered <input>.";

        ConfigurationSection anvil = section.getConfigurationSection("anvil_gui");
        boolean anvilEnabled = anvil == null || anvil.getBoolean("enabled", true);
        String anvilTitle = anvil != null ? anvil.getString("title", "") : "";
        String anvilText = anvil != null ? anvil.getString("text", defaultConfirmationText) : defaultConfirmationText;
        String anvilNotConfirmed = anvil != null ? anvil.getString("not_confirmed", "") : "";

        HashMap<Integer, ConfigurationSection> anvilSlots = new HashMap<>();
        if (anvil != null && anvil.contains("slots")) {
            anvil.getConfigurationSection("slots").getKeys(false).forEach(keyString -> {
                anvilSlots.put(Integer.parseInt(keyString), anvil.getConfigurationSection("slots." + keyString));
            });
        }

        return new InputGuiConfig(confirmationText,
                dialogEnabled, dialogTitle, dialogText, dialogInputLabel, dialogConfirmButton,
                dialogCancelButton, dialogNotMatched,
                anvilEnabled, anvilTitle, anvilText, anvilNotConfirmed, anvilSlots);
    }

    public String getConfirmationText() {
        return confirmationText;
    }

    public boolean isDialogEnabled() {
        return dialogEnabled;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public String getDialogText() {
        return dialogText;
    }

    public String getDialogInputLabel() {
        return dialogInputLabel;
    }

    public String getDialogConfirmButton() {
        return dialogConfirmButton;
    }

    public String getDialogCancelButton() {
        return dialogCancelButton;
    }

    public String getDialogNotMatched() {
        return dialogNotMatched;
    }

    public boolean isAnvilEnabled() {
        return anvilEnabled;
    }

    public String getAnvilTitle() {
        return anvilTitle;
    }

    public String getAnvilText() {
        return anvilText;
    }

    public String getAnvilNotConfirmed() {
        return anvilNotConfirmed;
    }

    public HashMap<Integer, ConfigurationSection> getAnvilSlots() {
        return anvilSlots;
    }
}
