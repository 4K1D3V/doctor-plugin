package pro.akii.ks.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pro.akii.ks.managers.DoctorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GUI for team management, displaying members, XP pool, and chat toggle.
 */
public class TeamGUI {

    private final Player player;
    private final DoctorManager doctorManager;
    private final String teamName;
    private final Inventory inventory;

    public TeamGUI(Player player, DoctorManager doctorManager, String teamName) {
        this.player = player;
        this.doctorManager = doctorManager;
        this.teamName = teamName;
        this.inventory = Bukkit.createInventory(null, 27, "Team: " + teamName);
        initializeItems();
    }

    private void initializeItems() {
        UUID leaderUuid = doctorManager.getTeamLeader(teamName);
        Player leader = Bukkit.getPlayer(leaderUuid);
        ItemStack leaderItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta leaderMeta = leaderItem.getItemMeta();
        leaderMeta.setDisplayName("§6Leader: " + (leader != null ? leader.getName() : "Offline"));
        leaderItem.setItemMeta(leaderMeta);
        inventory.setItem(4, leaderItem);

        ItemStack xpPoolItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpPoolMeta = xpPoolItem.getItemMeta();
        xpPoolMeta.setDisplayName("§eTeam XP Pool");
        List<String> poolLore = new ArrayList<>();
        poolLore.add("§7Total: " + doctorManager.getTeamXpPool(teamName) + " XP");
        if (doctorManager.isTeamLeader(player)) {
            poolLore.add("§eClick to distribute");
        }
        xpPoolMeta.setLore(poolLore);
        xpPoolItem.setItemMeta(xpPoolMeta);
        inventory.setItem(0, xpPoolItem);

        int slot = 9;
        for (UUID memberUuid : doctorManager.getTeamMembers(teamName)) {
            Player member = Bukkit.getPlayer(memberUuid);
            ItemStack memberItem = new ItemStack(Material.SKELETON_SKULL);
            ItemMeta memberMeta = memberItem.getItemMeta();
            memberMeta.setDisplayName("§a" + (member != null ? member.getName() : "Offline"));
            List<String> lore = new ArrayList<>();
            if (doctorManager.getDoctorData(member) != null) {
                lore.add("§7Level: " + doctorManager.getDoctorData(member).getLevel());
                lore.add("§7XP: " + doctorManager.getDoctorData(member).getXp() + "/" + (doctorManager.getDoctorData(member).getLevel() * 100));
            } else {
                lore.add("§7Level: N/A");
                lore.add("§7XP: N/A");
            }
            lore.add(doctorManager.isTeamLeader(member) ? "§6[Leader]" : "§7[Member]");
            if (doctorManager.isTeamLeader(player) && !memberUuid.equals(player.getUniqueId())) {
                lore.add("§eClick to promote");
                lore.add("§cShift-click to kick");
            }
            memberMeta.setLore(lore);
            memberItem.setItemMeta(memberMeta);
            inventory.setItem(slot++, memberItem);
            if (slot >= 18) break;
        }

        ItemStack chatItem = new ItemStack(doctorManager.isTeamChatEnabled(player) ? Material.GREEN_DYE : Material.RED_DYE);
        ItemMeta chatMeta = chatItem.getItemMeta();
        chatMeta.setDisplayName(doctorManager.isTeamChatEnabled(player) ? "§aTeam Chat: ON" : "§cTeam Chat: OFF");
        chatMeta.setLore(List.of("§7Click to toggle"));
        chatItem.setItemMeta(chatMeta);
        inventory.setItem(25, chatItem);

        ItemStack leaveItem = new ItemStack(Material.REDSTONE);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§cLeave Team");
        leaveItem.setItemMeta(leaveMeta);
        inventory.setItem(26, leaveItem);
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith("Team: ")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getItemMeta() == null) return;

        DoctorManager doctorManager = ((pro.akii.ks.DoctorPlugin) player.getServer().getPluginManager().getPlugin("Doctor")).getDoctorManager();
        String teamName = event.getView().getTitle().substring(6);

        if (clicked.getType() == Material.REDSTONE && clicked.getItemMeta().getDisplayName().equals("§cLeave Team")) {
            doctorManager.leaveTeam(player);
            player.closeInventory();
        } else if (clicked.getType() == Material.SKELETON_SKULL && doctorManager.isTeamLeader(player)) {
            String memberName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Player target = Bukkit.getPlayer(memberName);
            if (target != null && !target.equals(player)) {
                if (event.isShiftClick()) {
                    doctorManager.kickFromTeam(player, target);
                } else {
                    doctorManager.promoteToLeader(player, target);
                }
                player.closeInventory();
            }
        } else if ((clicked.getType() == Material.GREEN_DYE || clicked.getType() == Material.RED_DYE) && 
                   (clicked.getItemMeta().getDisplayName().startsWith("§aTeam Chat:") || 
                    clicked.getItemMeta().getDisplayName().startsWith("§cTeam Chat:"))) {
            doctorManager.toggleTeamChat(player);
            new TeamGUI(player, doctorManager, teamName).open();
        } else if (clicked.getType() == Material.EXPERIENCE_BOTTLE && 
                   clicked.getItemMeta().getDisplayName().equals("§eTeam XP Pool") && 
                   doctorManager.isTeamLeader(player)) {
            doctorManager.distributeTeamXp(player);
            player.closeInventory();
        }
    }
}