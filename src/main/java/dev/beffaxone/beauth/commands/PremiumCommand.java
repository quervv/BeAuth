package dev.beffaxone.beauth.commands;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.gui.AccountSettingsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PremiumCommand implements CommandExecutor {

    private final BeAuth plugin;

    public PremiumCommand(BeAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessagesManager().sendMessage(sender, "general.player-only");
            return true;
        }
        if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
            plugin.getMessagesManager().sendMessage(player, "change-password.not-logged-in");
            return true;
        }
        if (plugin.getConfigManager().isGuiEnabled() && args.length == 0) {
            new AccountSettingsGUI(plugin, player).open();
            return true;
        }
        plugin.getPremiumManager().togglePremium(player);
        return true;
    }
}
