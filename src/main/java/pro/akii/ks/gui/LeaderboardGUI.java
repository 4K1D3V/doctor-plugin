package pro.akii.ks.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pro.akii.ks.managers.DoctorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Paginated GUI for the team leaderboard with clickable team details.
 */
public class LeaderboardGUI {

    private final Player player;
    private final DoctorManager doctorManager;
    private final int page;
    private final Inventory inventory;
    private static final int ITEMS_PER_PAGE = 18;

    public LeaderboardGUI(Player player, DoctorManager doctorManager, int page) {
        this.player = player;
        this.doctorManager = doctorManager;
        this.page = page;
        this.inventory = Bukkit.createInventory(null, 27, "Team XP Leaderboard - Page " + (page + 1));
        initializeItems();
    }

    private void initializeItems() {
        List<Map.Entry<String, DoctorManager.TeamData>> sortedTeams = doctorManager.getSortedTeams();
        int totalPages = (int) Math.ceil((double) sortedTeams.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sortedTeams.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, DoctorManager.TeamData> entry = sortedTeams.get(i);
            String teamName = entry.getKey();
            int xpPool = entry.getValue().xpPool;
            int totalMemberXp = entry.getValue().members.stream()
                .mapToInt(uuid -> doctorManager.getDoctorData(Bukkit.getPlayer(uuid)) != null ?
                    doctorManager.getDoctorData(Bukkit.getPlayer(uuid)).getXp() : 0)
                .sum();

            ItemStack teamItem = new ItemStack(Material.BEACON);
            ItemMeta teamMeta = teamItem.getItemMeta();
            teamMeta.setDisplayName("§6" + teamName);
            List<String> lore = new ArrayList<>();
            lore.add("§ePool XP: " + xpPool);
            lore.add("§7Member XP: " + totalMemberXp);
            lore.add("§7Members: " + entry.getValue().members.size());
            lore.add("§eClick for details");
            teamMeta.setLore(lore);
            teamItem.setItemMeta(teamMeta);
            inventory.setItem(slot++, teamItem);
        }

        if (page > 0) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            prevMeta.setDisplayName("§aPrevious Page");
            prevItem.setItemMeta(prevMeta);
            inventory.setItem(18, prevItem);
        }

        if (endIndex < sortedTeams.size()) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            nextMeta.setDisplayName("§aNext Page");
            nextItem.setItemMeta(nextMeta);
            inventory.setItem(26, nextItem);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static void handleClick(Player player, InventoryClickEvent event, DoctorManager doctorManager) {
        if (!event.getView().getTitle().startsWith("Team XP Leaderboard")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getItemMeta() == null) return;

        String title = event.getView().getTitle();
        int currentPage = Integer.parseInt(title.split("Page ")[1]) - 1;

        if (clicked.getType() == Material.ARROW) {
            if (clicked.getItemMeta().getDisplayName().equals("§aPrevious Page")) {
                doctorManager.openLeaderboardGUI(player, currentPage - 1);
            } else if (clicked.getItemMeta().getDisplayName().equals("§aNext Page")) {
                doctorManager.openLeaderboardGUI(player, currentPage + 1);
            }
        } else if (clicked.getType() == Material.BEACON) {
            String teamName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            showTeamDetails(player, doctorManager, teamName);
        }
    }

    private static void showTeamDetails(Player player, DoctorManager doctorManager, String teamName) {
        Inventory detailsInv = Bukkit.createInventory(null, 27, "Team Details: " + teamName);
        DoctorManager.TeamData teamData = doctorManager.getSortedTeams().stream()
            .filter(entry -> entry.getKey().equals(teamName))
            .findFirst().map(Map.Entry::getValue).orElse(null);
        if (teamData == null) return;

        int xpPool = teamData.xpPool;
        int totalMemberXp = teamData.members.stream()
            .mapToInt(uuid -> doctorManager.getDoctorData(Bukkit.getPlayer(uuid)) != null ?
                doctorManager.getDoctorData(Bukkit.getPlayer(uuid)).getXp() : 0)
            .sum();

        ItemStack summaryItem = new ItemStack(Material.BEACON);
        ItemMeta summaryMeta = summaryItem.getItemMeta();
        summaryMeta.setDisplayName("§6" + teamName);
        List<String> summaryLore = new ArrayList<>();
        summaryLore.add("§ePool XP: " + xpPool);
        summaryLore.add("§7Member XP: " + totalMemberXp);
        summaryLore.add("§7Members: " + teamData.members.size());
        summaryMeta.setLore(summaryLore);
        summaryItem.setItemMeta(summaryMeta);
        detailsInv.setItem(4, summaryItem);

        Player leader = Bukkit.getPlayer(teamData.leader);
        ItemStack leaderItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta leaderMeta = leaderItem.getItemMeta();
        leaderMeta.setDisplayName("§6Leader: " + (leader != null ? leader.getName() : "Offline"));
        detailsInv.setItem(13, leaderItem);

        int slot = 18;
        for (UUID memberUuid : teamData.members) {
            Player member = Bukkit.getPlayer(memberUuid);
            ItemStack memberItem = new ItemStack(Material.SKELETON_SKULL);
            ItemMeta memberMeta = memberItem.getItemMeta();
            memberMeta.setDisplayName("§a" + (member != null ? member.getName() : "Offline"));
            List<String> lore = new ArrayList<>();
            DoctorData memberData = doctorManager.getDoctorData(member);
            if (memberData != null) {
                lore.add("§7Level: " + memberData.getLevel());
                lore.add("§7XP: " + memberData.getXp());
            }
            memberMeta.setLore(lore);
            memberItem.setItemMeta(memberMeta);
            detailsInv.setItem(slot++, memberItem);
            if (slot >= 27) break;
        }

        player.openInventory(detailsInv);
    }
}