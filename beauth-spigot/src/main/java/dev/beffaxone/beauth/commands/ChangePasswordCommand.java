package dev.beffaxone.beauth.commands;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {

    private final BeAuth plugin;

    public ChangePasswordCommand(BeAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessagesManager().sendMessage(sender, "general.player-only");
            return true;
        }
        if (args.length < 2) {
            plugin.getMessagesManager().sendMessage(player, "change-password.usage");
            return true;
        }
        String oldPassword = args[0];
        String newPassword = args[1];
        plugin.getAuthManager().changePassword(player, oldPassword, newPassword);
        return true;
    }
}
