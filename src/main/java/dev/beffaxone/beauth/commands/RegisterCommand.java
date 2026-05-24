package dev.beffaxone.beauth.commands;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.utils.RateLimiter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final BeAuth plugin;
    private final RateLimiter rateLimiter;

    public RegisterCommand(BeAuth plugin) {
        this.plugin = plugin;
        this.rateLimiter = new RateLimiter(
                plugin.getConfigManager().getRateLimitMaxAttempts(),
                plugin.getConfigManager().getRateLimitWindowSeconds()
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessagesManager().sendMessage(sender, "general.player-only");
            return true;
        }
        if (args.length < 2) {
            plugin.getMessagesManager().sendMessage(player, "register.usage");
            return true;
        }
        if (plugin.getConfigManager().isRateLimitEnabled()) {
            String ip = player.getAddress().getAddress().getHostAddress();
            if (rateLimiter.isRateLimited(ip)) {
                long remainingSeconds = rateLimiter.getRemainingTimeSeconds(ip);
                plugin.getMessagesManager().sendMessage(player, "login.too-many-attempts", "{seconds}", String.valueOf(remainingSeconds));
                return true;
            }
        }
        String password = args[0];
        String confirmPassword = args[1];
        plugin.getAuthManager().register(player, password, confirmPassword);
        return true;
    }
}
