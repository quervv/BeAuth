package dev.beffaxone.beauth.gui;

import dev.beffaxone.beauth.BeAuth;
import dev.beffaxone.beauth.database.PlayerData;
import dev.beffaxone.beauth.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountSettingsGUI {

    private final BeAuth plugin;
    private final Player player;
    private Inventory inventory;

    public AccountSettingsGUI(BeAuth plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        String title = plugin.getMessagesManager().getRawMessage("gui.account-settings.title");
        this.inventory = Bukkit.createInventory(null, 27, title);
        fillBorders();
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (data == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getMessagesManager().sendMessage(player, "login.not-registered");
                });
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                setupItems(data);
                player.openInventory(inventory);
            });
        });
    }

    private void fillBorders() {
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, grayGlass);
            }
        }
    }

    private void setupItems(PlayerData data) {
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ColorUtils.colorize("&#5B2D8F♦ Account Info"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Player: &f" + data.getUsername()));
            lore.add(ColorUtils.colorize("&7Status: &a" + plugin.getMessagesManager().getRawMessage("gui.login-info.status-logged")));
            lore.add(ColorUtils.colorize("&7Last IP: &f" + data.getLastIp()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            lore.add(ColorUtils.colorize("&7Last Login: &f" + sdf.format(new Date(data.getLastLogin()))));
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(11, infoItem);

        ItemStack premiumItem = new ItemStack(data.isPremium() ? Material.GOLD_INGOT : Material.IRON_INGOT);
        ItemMeta premiumMeta = premiumItem.getItemMeta();
        if (premiumMeta != null) {
            premiumMeta.setDisplayName(ColorUtils.colorize("&#FFD700★ Premium Status"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (data.isPremium()) {
                lore.add(plugin.getMessagesManager().getRawMessage("gui.premium-status.premium"));
                lore.add(ColorUtils.colorize("&7Your account auto-logins."));
                lore.add("");
                lore.add(ColorUtils.colorize("&eClick to switch to cracked mode."));
            } else {
                lore.add(plugin.getMessagesManager().getRawMessage("gui.premium-status.cracked"));
                lore.add(ColorUtils.colorize("&7You must enter your password to login."));
                lore.add("");
                lore.add(ColorUtils.colorize("&eClick to enable premium login."));
            }
            premiumMeta.setLore(lore);
            premiumItem.setItemMeta(premiumMeta);
        }
        inventory.setItem(13, premiumItem);

        ItemStack pwItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta pwMeta = pwItem.getItemMeta();
        if (pwMeta != null) {
            pwMeta.setDisplayName(ColorUtils.colorize("&#FF6B6B⚙ Account Security"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Change your password in game."));
            lore.add("");
            lore.add(ColorUtils.colorize("&eUse in chat:"));
            lore.add(ColorUtils.colorize("&b/changepassword <old> <new>"));
            pwMeta.setLore(lore);
            pwItem.setItemMeta(pwMeta);
        }
        inventory.setItem(15, pwItem);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
