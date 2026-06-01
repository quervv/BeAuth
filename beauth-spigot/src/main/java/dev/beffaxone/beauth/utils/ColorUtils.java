package dev.beffaxone.beauth.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HASH_HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    private ColorUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        message = parseMiniMessageSimulated(message);
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(sb, getSpigotHexCode(color));
        }
        matcher.appendTail(sb);
        message = sb.toString();

        matcher = HASH_HEX_PATTERN.matcher(message);
        sb = new StringBuilder();
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(sb, getSpigotHexCode(color));
        }
        matcher.appendTail(sb);
        message = sb.toString();

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static String getSpigotHexCode(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            builder.append('§').append(c);
        }
        return builder.toString();
    }

    private static String parseMiniMessageSimulated(String msg) {
        msg = msg.replace("<red>", "&c")
                 .replace("<green>", "&a")
                 .replace("<yellow>", "&e")
                 .replace("<gold>", "&6")
                 .replace("<aqua>", "&b")
                 .replace("<blue>", "&9")
                 .replace("<white>", "&f")
                 .replace("<gray>", "&7")
                 .replace("<dark_green>", "&2")
                 .replace("<dark_aqua>", "&3")
                 .replace("<dark_red>", "&4")
                 .replace("<dark_purple>", "&5")
                 .replace("<light_purple>", "&d")
                 .replace("<black>", "&0")
                 .replace("<bold>", "&l")
                 .replace("<italic>", "&o")
                 .replace("<underlined>", "&n")
                 .replace("<strikethrough>", "&m")
                 .replace("<obfuscated>", "&k")
                 .replace("<reset>", "&r")
                 .replace("</red>", "&r")
                 .replace("</green>", "&r")
                 .replace("</yellow>", "&r")
                 .replace("</gold>", "&r")
                 .replace("</aqua>", "&r")
                 .replace("</blue>", "&r")
                 .replace("</white>", "&r")
                 .replace("</gray>", "&r");
        Pattern p = Pattern.compile("<color:(#[A-Fa-f0-9]{6})>");
        Matcher m = p.matcher(msg);
        while (m.find()) {
            msg = msg.replace(m.group(0), "&" + m.group(1));
        }
        p = Pattern.compile("<(#[A-Fa-f0-9]{6})>");
        m = p.matcher(msg);
        while (m.find()) {
            msg = msg.replace(m.group(0), "&" + m.group(1));
        }
        msg = msg.replaceAll("</[a-zA-Z_]+>", "");
        return msg;
    }
}
