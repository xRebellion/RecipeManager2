package haveric.recipeManager;

import haveric.recipeManager.messages.MessageSender;
import haveric.recipeManager.recipes.BaseRecipeEvents;
import haveric.recipeManager.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * RecipeManager handled events
 */
public class Events extends BaseRecipeEvents {
    public Events() { }

    public static void reload() {
        HandlerList.unregisterAll(RecipeManager.getEvents());
        Bukkit.getPluginManager().registerEvents(RecipeManager.getEvents(), RecipeManager.getPlugin());
    }

    // TODO: Apparently we are blocking enchanting based on canCraft permissions. Replace with a new permission for blocking enchanting
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            Material enchantingTableMaterial;
            if (Version.has1_13Support()) {
                enchantingTableMaterial = Material.ENCHANTING_TABLE;
            } else {
                enchantingTableMaterial = Material.getMaterial("ENCHANTMENT_TABLE");
            }

            if (block.getType() == enchantingTableMaterial) {
                if (!RecipeManager.getPlugin().canCraft(event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();

        if (Settings.getInstance().getFixModResults()) {
            for (ItemStack item : human.getInventory().getContents()) {
                itemProcess(item);
            }
        }
    }

    /*
     * Marked item monitor events
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (Settings.getInstance().getUpdateBooks()) {
            RecipeBooks.getInstance().updateBook(player, item);
        }

        if (Settings.getInstance().getFixModResults()) {
            itemProcess(item);
        }
    }

    private void itemProcess(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        List<String> lore = meta.getLore();

        if (lore == null || lore.isEmpty()) {
            return;
        }

        for (int i = 0; i < lore.size(); i++) {
            String s = lore.get(i);

            if (s != null && s.startsWith(Recipes.RECIPE_ID_STRING)) {
                lore.remove(i);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
        }
    }

    /*
     * Update check notifier
     */

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Players.addJoined(player);

        if (Settings.getInstance().getUpdateCheckEnabled() && player.hasPermission("recipemanager.command.rmupdate")) {
            String latestVersion = Updater.getLatestVersion();
            String currentVersion = Updater.getCurrentVersion();

            if (latestVersion != null) {
                int compare = Updater.compareVersions();

                if (compare == -1) {
                    MessageSender.getInstance().send(player, "[RecipeManager] New version: <green>" + latestVersion + "<reset>! You're using <yellow>" + currentVersion + "<reset>, grab it at: <light_purple>" + Updater.getLatestLink());
                } else if (compare == 2) {
                    MessageSender.getInstance().send(player, "[RecipeManager] New alpha/beta version: <green>" + latestVersion + " " + Updater.getLatestBetaStatus() + "<reset>! You're using <yellow>" + currentVersion + "<reset>, grab it at: <light_purple>" + Updater.getLatestLink());
                } else if (compare == 3) {
                    MessageSender.getInstance().send(player, "[RecipeManager] BukkitDev has a different alpha/beta version: <green>" + latestVersion + " " + Updater.getLatestBetaStatus() + "<reset>! You're using <yellow>" + currentVersion + " " + Updater.getCurrentBetaStatus() + "<reset>, grab it at: <light_purple>" + Updater.getLatestLink());
                }
            }
        }
    }
}
