package dev.beffaxone.beauth.listeners;

import dev.beffaxone.beauth.BeAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryListener implements Listener {

    private final BeAuth plugin;

    public InventoryListener(BeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getMessagesManager().sendMessage(player, "security.inventory-blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        String title = plugin.getMessagesManager().getRawMessage("gui.account-settings.title");
        if (event.getView().getTitle().equalsIgnoreCase(title)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            int slot = event.getRawSlot();
            if (slot == 13) {
                player.closeInventory();
                plugin.getPremiumManager().togglePremium(player);
            }
        }
    }
}
