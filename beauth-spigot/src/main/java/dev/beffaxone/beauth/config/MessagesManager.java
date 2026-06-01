package dev.beffaxone.beauth.config;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.utils.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessagesManager {

    private final BeAuth plugin;
    private YamlConfiguration messagesConfig;
    private File messagesFile;
    private String prefix;

    public MessagesManager(BeAuth plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        this.prefix = messagesConfig.getString("prefix", "&8[&bBeAuth&8] &r");
    }

    public String getRawMessage(String path) {
        String msg = messagesConfig.getString(path);
        if (msg == null) {
            return ColorUtils.colorize("&c[Missing message: " + path + "]");
        }
        return ColorUtils.colorize(msg);
    }

    public String getMessage(String path) {
        String msg = messagesConfig.getString(path);
        if (msg == null) {
            return ColorUtils.colorize(prefix + "&c[Missing message: " + path + "]");
        }
        return ColorUtils.colorize(prefix + msg);
    }

    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendMessage(CommandSender sender, String path, String... replacements) {
        String msg = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(msg);
    }

    public String getMessage(String path, String... replacements) {
        String msg = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        return msg;
    }

    public String getRawMessage(String path, String... replacements) {
        String msg = getRawMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        return msg;
    }

    public String getPrefix() {
        return ColorUtils.colorize(prefix);
    }
}
