package dev.beffaxone.beauth.commands;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BeAuthAdminCommand implements CommandExecutor, TabCompleter {

    private final BeAuth plugin;

    public BeAuthAdminCommand(BeAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("beauth.admin")) {
            plugin.getMessagesManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload" -> {
                if (!sender.hasPermission("beauth.admin.reload")) {
                    plugin.getMessagesManager().sendMessage(sender, "general.no-permission");
                    return true;
                }
                try {
                    plugin.reload();
                    plugin.getMessagesManager().sendMessage(sender, "admin.reload-success");
                } catch (Exception e) {
                    plugin.getMessagesManager().sendMessage(sender, "general.reload-failed");
                }
            }
            case "forcechangepassword" -> {
                if (!sender.hasPermission("beauth.admin.forcechangepassword")) {
                    plugin.getMessagesManager().sendMessage(sender, "general.no-permission");
                    return true;
                }
                if (args.length < 3) {
                    plugin.getMessagesManager().sendMessage(sender, "admin.force-change-usage");
                    return true;
                }
                String targetName = args[1];
                String newPassword = args[2];
                plugin.getDatabaseManager().getPlayerDataByName(targetName).thenAccept(data -> {
                    if (data == null) {
                        plugin.getMessagesManager().sendMessage(sender, "admin.player-not-registered", "{player}", targetName);
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        String hash = new dev.beffaxone.beauth.auth.PasswordManager(plugin).hashPassword(newPassword);
                        data.setPasswordHash(hash);
                        plugin.getDatabaseManager().savePlayerData(data).thenRun(() -> {
                            plugin.getMessagesManager().sendMessage(sender, "admin.force-change-success", "{player}", targetName);
                            Player onlineTarget = Bukkit.getPlayerExact(targetName);
                            if (onlineTarget != null && onlineTarget.isOnline()) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    onlineTarget.kickPlayer(ColorUtils.colorize("&cYour password was changed by an admin."));
                                });
                            }
                        });
                    });
                });
            }
            case "premium" -> {
                if (!sender.hasPermission("beauth.admin.premium")) {
                    plugin.getMessagesManager().sendMessage(sender, "general.no-permission");
                    return true;
                }
                if (args.length < 3) {
                    plugin.getMessagesManager().sendMessage(sender, "admin.set-premium-usage");
                    return true;
                }
                String targetName = args[1];
                boolean isPremium = Boolean.parseBoolean(args[2]);
                plugin.getDatabaseManager().getPlayerDataByName(targetName).thenAccept(data -> {
                    UUID targetUuid;
                    if (data != null) {
                        targetUuid = data.getUuid();
                    } else {
                        Player onlineTarget = Bukkit.getPlayerExact(targetName);
                        if (onlineTarget != null) {
                            targetUuid = onlineTarget.getUniqueId();
                        } else {
                            targetUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + targetName).getBytes());
                        }
                    }
                    plugin.getPremiumManager().setPremiumStatus(targetUuid, targetName, isPremium).thenRun(() -> {
                        if (isPremium) {
                            plugin.getMessagesManager().sendMessage(sender, "admin.set-premium-success", "{player}", targetName);
                        } else {
                            plugin.getMessagesManager().sendMessage(sender, "admin.set-cracked-success", "{player}", targetName);
                        }
                        Player onlineTarget = Bukkit.getPlayerExact(targetName);
                        if (onlineTarget != null && onlineTarget.isOnline()) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                onlineTarget.kickPlayer(ColorUtils.colorize("&eYour account status has been changed by an admin."));
                            });
                        }
                    });
                });
            }
            case "unregister" -> {
                if (!sender.hasPermission("beauth.admin.unregister")) {
                    plugin.getMessagesManager().sendMessage(sender, "general.no-permission");
                    return true;
                }
                if (args.length < 2) {
                    plugin.getMessagesManager().sendMessage(sender, "admin.unregister-usage");
                    return true;
                }
                String targetName = args[1];
                plugin.getDatabaseManager().getPlayerDataByName(targetName).thenAccept(data -> {
                    if (data == null) {
                        plugin.getMessagesManager().sendMessage(sender, "admin.player-not-registered", "{player}", targetName);
                        return;
                    }
                    plugin.getDatabaseManager().deletePlayerData(data.getUuid()).thenRun(() -> {
                        plugin.getMessagesManager().sendMessage(sender, "admin.unregister-success", "{player}", targetName);
                        plugin.getAuthManager().getSessionManager().removeSession(data.getUuid());
                        Player onlineTarget = Bukkit.getPlayerExact(targetName);
                        if (onlineTarget != null && onlineTarget.isOnline()) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                onlineTarget.kickPlayer(ColorUtils.colorize("&cYour account was unregistered by an admin."));
                            });
                        }
                    });
                });
            }
            case "info" -> {
                if (args.length < 2) {
                    plugin.getMessagesManager().sendMessage(sender, "admin.unregister-usage");
                    return true;
                }
                String targetName = args[1];
                plugin.getDatabaseManager().getPlayerDataByName(targetName).thenAccept(data -> {
                    if (data == null) {
                        plugin.getMessagesManager().sendMessage(sender, "admin.player-not-registered", "{player}", targetName);
                        return;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String date = data.getLastLogin() > 0 ? sdf.format(new Date(data.getLastLogin())) : "Never";
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-header", "{version}", plugin.getDescription().getVersion()));
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-player", "{player}", data.getUsername()));
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-uuid", "{uuid}", data.getUuid().toString()));
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-registered", "{registered}", "Yes"));
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-premium", "{premium}", data.isPremium() ? "Yes (Premium)" : "No (Cracked)"));
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-last-ip", "{ip}", data.getLastIp()));
                    sender.sendMessage(plugin.getMessagesManager().getRawMessage("admin.info-last-login", "{date}", date));
                });
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtils.colorize("&b&lBeAuth &7- Admin Commands"));
        sender.sendMessage(ColorUtils.colorize("&f/beauth reload &7- Reload config"));
        sender.sendMessage(ColorUtils.colorize("&f/beauth info <player> &7- Get account details"));
        sender.sendMessage(ColorUtils.colorize("&f/beauth forcechangepassword <player> <new> &7- Force password change"));
        sender.sendMessage(ColorUtils.colorize("&f/beauth premium <player> <true|false> &7- Force premium status"));
        sender.sendMessage(ColorUtils.colorize("&f/beauth unregister <player> &7- Delete player registration"));
        sender.sendMessage(ColorUtils.colorize("&8&m----------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (!sender.hasPermission("beauth.admin")) {
            return list;
        }
        if (args.length == 1) {
            list.add("reload");
            list.add("info");
            list.add("forcechangepassword");
            list.add("premium");
            list.add("unregister");
            return filter(list, args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("forcechangepassword") ||
                    args[0].equalsIgnoreCase("premium") ||
                    args[0].equalsIgnoreCase("unregister") ||
                    args[0].equalsIgnoreCase("info")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
                return filter(list, args[1]);
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("premium")) {
            list.add("true");
            list.add("false");
            return filter(list, args[2]);
        }
        return list;
    }

    private List<String> filter(List<String> raw, String search) {
        List<String> result = new ArrayList<>();
        for (String s : raw) {
            if (s.toLowerCase().startsWith(search.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
