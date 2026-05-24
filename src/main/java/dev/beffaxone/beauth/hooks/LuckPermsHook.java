package dev.beffaxone.beauth.hooks;

import dev.beffaxone.beauth.BeAuth;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class LuckPermsHook {

    private static BeAuth plugin;
    private static LuckPerms api;

    private LuckPermsHook() {}

    public static void initialize(BeAuth pluginInstance) {
        plugin = pluginInstance;
        try {
            api = LuckPermsProvider.get();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting LuckPerms API", e);
        }
    }

    public static void setUnauthenticated(Player player) {
        if (api == null) return;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        String unauthGroup = plugin.getConfigManager().getLuckPermsUnauthGroup();
        if (unauthGroup == null || unauthGroup.isEmpty()) return;
        InheritanceNode node = InheritanceNode.builder(unauthGroup).build();
        user.transientData().add(node);
        api.getUserManager().saveUser(user);
    }

    public static void setAuthenticated(Player player) {
        if (api == null) return;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        String unauthGroup = plugin.getConfigManager().getLuckPermsUnauthGroup();
        if (unauthGroup == null || unauthGroup.isEmpty()) return;
        InheritanceNode node = InheritanceNode.builder(unauthGroup).build();
        user.transientData().remove(node);
        api.getUserManager().saveUser(user);
    }

    public static void shutdown() {
        plugin = null;
        api = null;
    }
}
