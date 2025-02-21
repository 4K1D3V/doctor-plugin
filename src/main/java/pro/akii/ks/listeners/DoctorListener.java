package pro.akii.ks.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pro.akii.ks.DoctorPlugin;
import pro.akii.ks.gui.MedicalKitGUI;
import pro.akii.ks.gui.TeamGUI;
import pro.akii.ks.gui.LeaderboardGUI;

/**
 * Event listener for player interactions and injuries.
 */
public class DoctorListener implements Listener {

    private final DoctorPlugin plugin;

    public DoctorListener(DoctorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDoctorManager().isDoctor(player)) {
            player.sendMessage("§aWelcome back, Dr. " + player.getName() + "!");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.getDoctorManager().applyRandomInjury(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        MedicalKitGUI.handleClick(event);
        TeamGUI.handleClick(event);
        LeaderboardGUI.handleClick(player, event, plugin.getDoctorManager());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDoctorManager().isTeamChatEnabled(player)) {
            event.setCancelled(true);
            plugin.getDoctorManager().sendTeamMessage(player, event.getMessage());
        }
    }
}