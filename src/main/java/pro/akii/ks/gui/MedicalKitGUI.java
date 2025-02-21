package pro.akii.ks.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pro.akii.ks.managers.DoctorManager;
import pro.akii.ks.utils.ConfigUtil;

/**
 * GUI for the Doctor's Medical Kit, displaying healing items.
 */
public class MedicalKitGUI {

    private final Player player;
    private final DoctorManager doctorManager;
    private final Inventory inventory;

    public MedicalKitGUI(Player player, DoctorManager doctorManager) {
        this.player = player;
        this.doctorManager = doctorManager;
        this.inventory = Bukkit.createInventory(null, 9, "Medical Kit");
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(0, ConfigUtil.getStethoscopeItem());
        inventory.setItem(1, ConfigUtil.getBandageItem());
        inventory.setItem(2, ConfigUtil.getAntidoteItem());
        inventory.setItem(3, ConfigUtil.getSutureKitItem());
        inventory.setItem(4, ConfigUtil.getSplintItem());
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("Medical Kit")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Player target = player.getServer().getPlayer(player.getNearbyEntities(5, 5, 5).stream()
            .filter(e -> e instanceof Player && e != player)
            .map(e -> (Player) e)
            .findFirst().orElse(null));

        DoctorManager doctorManager = ((pro.akii.ks.DoctorPlugin) player.getServer().getPluginManager().getPlugin("Doctor")).getDoctorManager();

        if (target == null) {
            player.sendMessage("§cNo nearby player to heal!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            return;
        }

        if (clicked.isSimilar(ConfigUtil.getStethoscopeItem())) {
            doctorManager.diagnosePlayer(player, target);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        } else if (player.getInventory().containsAtLeast(clicked, 1)) {
            doctorManager.healPlayer(player, target, clicked);
            if (clicked.isSimilar(ConfigUtil.getBandageItem())) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);
                player.sendMessage("§aApplied Bandage to " + target.getName() + "!");
            } else if (clicked.isSimilar(ConfigUtil.getAntidoteItem())) {
                player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1.0F, 1.0F);
                player.sendMessage("§aAdministered Antidote to " + target.getName() + "!");
            } else if (clicked.isSimilar(ConfigUtil.getSutureKitItem())) {
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                player.sendMessage("§aUsed Suture Kit on " + target.getName() + "!");
            } else if (clicked.isSimilar(ConfigUtil.getSplintItem())) {
                player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1.0F, 1.0F);
                player.sendMessage("§aApplied Splint to " + target.getName() + "!");
            }
        } else {
            player.sendMessage("§cYou don’t have this item in your inventory!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
        }
    }
}