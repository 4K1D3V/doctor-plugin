package pro.akii.ks.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pro.akii.ks.DoctorPlugin;
import pro.akii.ks.data.DoctorData;
import pro.akii.ks.gui.LeaderboardGUI;
import pro.akii.ks.gui.MedicalKitGUI;
import pro.akii.ks.gui.TeamGUI;
import pro.akii.ks.utils.ConfigUtil;
import pro.akii.ks.utils.NMSUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages Doctor roles, teams, XP progression, and persistence.
 */
public class DoctorManager {

    private final DoctorPlugin plugin;
    private final Map<UUID, DoctorData> doctors;
    private final Map<String, TeamData> teams;
    private final Set<UUID> teamChatEnabled;
    private final File dataFile;

    public DoctorManager(DoctorPlugin plugin) {
        this.plugin = plugin;
        this.doctors = new HashMap<>();
        this.teams = new HashMap<>();
        this.teamChatEnabled = new HashSet<>();
        this.dataFile = new File(plugin.getDataFolder(), "doctors.yml");
        loadData();
    }

    public boolean isDoctor(Player player) {
        return doctors.containsKey(player.getUniqueId());
    }

    public DoctorData getDoctorData(Player player) {
        return doctors.get(player.getUniqueId());
    }

    public void registerDoctor(Player player) {
        UUID uuid = player.getUniqueId();
        if (!doctors.containsKey(uuid)) {
            doctors.put(uuid, new DoctorData(uuid));
            player.sendMessage("§aYou are now a Doctor! Use /doctor kit to start.");
            saveData();
        } else {
            player.sendMessage("§cYou are already a Doctor!");
        }
    }

    public void healPlayer(Player doctor, Player target, ItemStack item) {
        DoctorData data = getDoctorData(doctor);
        if (data == null) {
            doctor.sendMessage("§cYou must be a Doctor to heal!");
            return;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        String teamName = getPlayerTeam(doctor);
        double buffMultiplier = teamName != null ? 1.0 - (getTeamXpPool(teamName) * ConfigUtil.getTeamXpBuffMultiplier()) : 1.0;
        double cooldown = ConfigUtil.getHealCooldown() * (teamName != null ? ConfigUtil.getTeamHealCooldownBuff() : 1.0) * Math.max(0.5, buffMultiplier);
        if (currentTime - data.getLastHealTime() < cooldown) {
            doctor.sendMessage("§cHealing on cooldown! Wait " + 
                ((int) (cooldown - (currentTime - data.getLastHealTime()))) + "s.");
            return;
        }

        if (target == null || !target.isOnline() || doctor.getLocation().distanceSquared(target.getLocation()) > 25) {
            doctor.sendMessage("§cTarget is invalid or too far!");
            return;
        }

        boolean consumed = false;
        if (item.isSimilar(ConfigUtil.getBandageItem()) && !NMSUtil.hasAnyInjury(target)) {
            NMSUtil.applyCustomHeal(target, data.getLevel());
            doctor.sendMessage("§aHealed " + target.getName() + " for " + (2 + data.getLevel()) + " HP!");
            target.sendMessage("§aYou were healed by Dr. " + doctor.getName() + "!");
            consumed = true;
        } else if (item.isSimilar(ConfigUtil.getAntidoteItem()) && NMSUtil.hasInfection(target)) {
            NMSUtil.cureInfection(target);
            plugin.getInjuryManager().cureInjury(target, "infection");
            doctor.sendMessage("§aCured " + target.getName() + "'s Infection!");
            target.sendMessage("§aYour Infection was cured by Dr. " + doctor.getName() + "!");
            consumed = true;
        } else if (item.isSimilar(ConfigUtil.getSutureKitItem()) && NMSUtil.hasBleeding(target)) {
            NMSUtil.cureBleeding(target);
            plugin.getInjuryManager().cureInjury(target, "bleeding");
            doctor.sendMessage("§aStopped " + target.getName() + "'s Bleeding!");
            target.sendMessage("§aYour Bleeding was stopped by Dr. " + doctor.getName() + "!");
            consumed = true;
        } else if (item.isSimilar(ConfigUtil.getSplintItem()) && NMSUtil.hasBrokenLeg(target)) {
            NMSUtil.cureBrokenLeg(target);
            plugin.getInjuryManager().cureInjury(target, "brokenleg");
            doctor.sendMessage("§aFixed " + target.getName() + "'s Broken Leg!");
            target.sendMessage("§aYour Broken Leg was fixed by Dr. " + doctor.getName() + "!");
            consumed = true;
        } else {
            doctor.sendMessage("§cWrong item or no curable condition!");
            return;
        }

        if (consumed) {
            doctor.getInventory().removeItem(item);
            int xp = ConfigUtil.getXpPerHeal();
            data.addXp(xp);
            data.setLastHealTime(currentTime);
            shareTeamXp(doctor, xp);
            saveData();
            notifyTeam(doctor, "Dr. " + doctor.getName() + " healed " + target.getName() + "!");
        }
    }

    public void diagnosePlayer(Player doctor, Player target) {
        DoctorData data = getDoctorData(doctor);
        if (data == null) {
            doctor.sendMessage("§cYou must be a Doctor to diagnose!");
            return;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - data.getLastDiagnoseTime() < ConfigUtil.getDiagnoseCooldown()) {
            doctor.sendMessage("§cDiagnosis on cooldown! Wait " + 
                (ConfigUtil.getDiagnoseCooldown() - (currentTime - data.getLastDiagnoseTime())) + "s.");
            return;
        }

        if (!doctor.getInventory().containsAtLeast(ConfigUtil.getStethoscopeItem(), 1)) {
            doctor.sendMessage("§cYou need a Stethoscope to diagnose!");
            return;
        }

        if (target == null || !target.isOnline()) {
            doctor.sendMessage("§cTarget player not found!");
            return;
        }

        data.setLastDiagnoseTime(currentTime);
        saveData();

        float health = target.getHealth();
        String status = health < 5 ? "§cCritical" : health < 10 ? "§eInjured" : "§aHealthy";
        List<String> injuries = new ArrayList<>();
        if (NMSUtil.hasBrokenLeg(target)) injuries.add("§cBroken Leg");
        if (NMSUtil.hasInfection(target)) injuries.add("§cInfection");
        if (NMSUtil.hasBleeding(target)) injuries.add("§cBleeding");

        doctor.sendMessage("§6Diagnosis for " + target.getName() + ":");
        doctor.sendMessage("§7- Health: " + String.format("%.1f", health) + "/20");
        doctor.sendMessage("§7- Status: " + status);
        doctor.sendMessage("§7- Injuries: " + (injuries.isEmpty() ? "§aNone" : String.join(", ", injuries)));
    }

    public void openMedicalKit(Player player) {
        if (!isDoctor(player)) {
            player.sendMessage("§cYou must be a Doctor to use the Medical Kit!");
            return;
        }
        new MedicalKitGUI(player, this).open();
    }

    public void openTeamGUI(Player player) {
        if (!isDoctor(player)) {
            player.sendMessage("§cYou must be a Doctor to view team info!");
            return;
        }
        String teamName = getPlayerTeam(player);
        if (teamName == null) {
            player.sendMessage("§cYou’re not in a team! Join one with /doctor team join <name>");
            return;
        }
        new TeamGUI(player, this, teamName).open();
    }

    public void openLeaderboardGUI(Player player, int page) {
        new LeaderboardGUI(player, this, page).open();
    }

    public void applyRandomInjury(Player player) {
        if (Math.random() < ConfigUtil.getInjuryChance()) {
            int injuryType = new Random().nextInt(3);
            switch (injuryType) {
                case 0:
                    plugin.getInjuryManager().applyBrokenLeg(player);
                    player.sendMessage("§cYou’ve suffered a Broken Leg! Movement slowed.");
                    break;
                case 1:
                    plugin.getInjuryManager().applyInfection(player);
                    player.sendMessage("§cYou’ve contracted an Infection! Health will drain slowly.");
                    break;
                case 2:
                    plugin.getInjuryManager().applyBleeding(player);
                    player.sendMessage("§cYou’re Bleeding! Losing health over time.");
                    break;
            }
        }
    }

    public void joinTeam(Player player, String teamName) {
        if (!isDoctor(player)) {
            player.sendMessage("§cYou must be a Doctor to join a team!");
            return;
        }
        String currentTeam = getPlayerTeam(player);
        if (currentTeam != null) {
            player.sendMessage("§cLeave your current team first!");
            return;
        }
        TeamData team = teams.computeIfAbsent(teamName, k -> new TeamData());
        team.members.add(player.getUniqueId());
        if (team.leader == null) team.leader = player.getUniqueId();
        player.sendMessage("§aJoined team " + teamName + "!" + (team.leader.equals(player.getUniqueId()) ? " You are the leader!" : ""));
        notifyTeam(player, "Dr. " + player.getName() + " joined the team!");
        saveData();
    }

    public void leaveTeam(Player player) {
        String teamName = getPlayerTeam(player);
        if (teamName == null) {
            player.sendMessage("§cYou’re not in a team!");
            return;
        }
        TeamData team = teams.get(teamName);
        team.members.remove(player.getUniqueId());
        if (team.leader.equals(player.getUniqueId()) && !team.members.isEmpty()) {
            team.leader = team.members.iterator().next();
            Player newLeader = plugin.getServer().getPlayer(team.leader);
            if (newLeader != null) newLeader.sendMessage("§aYou are now the leader of " + teamName + "!");
        }
        if (team.members.isEmpty()) teams.remove(teamName);
        player.sendMessage("§aLeft team " + teamName + "!");
        notifyTeam(player, "Dr. " + player.getName() + " left the team!");
        saveData();
    }

    public void kickFromTeam(Player leader, Player target) {
        if (!isTeamLeader(leader)) {
            leader.sendMessage("§cYou must be a team leader to kick members!");
            return;
        }
        String teamName = getPlayerTeam(leader);
        if (!teams.get(teamName).members.contains(target.getUniqueId())) {
            leader.sendMessage("§c" + target.getName() + " is not in your team!");
            return;
        }
        teams.get(teamName).members.remove(target.getUniqueId());
        target.sendMessage("§cYou were kicked from team " + teamName + " by Dr. " + leader.getName() + "!");
        leader.sendMessage("§aKicked " + target.getName() + " from the team!");
        notifyTeam(leader, "Dr. " + leader.getName() + " kicked " + target.getName() + " from the team!");
        saveData();
    }

    public void promoteToLeader(Player leader, Player target) {
        if (!isTeamLeader(leader)) {
            leader.sendMessage("§cYou must be a team leader to promote someone!");
            return;
        }
        String teamName = getPlayerTeam(leader);
        if (!teams.get(teamName).members.contains(target.getUniqueId())) {
            leader.sendMessage("§c" + target.getName() + " is not in your team!");
            return;
        }
        teams.get(teamName).leader = target.getUniqueId();
        leader.sendMessage("§aPromoted " + target.getName() + " to team leader!");
        target.sendMessage("§aYou’ve been promoted to leader of team " + teamName + "!");
        notifyTeam(leader, "Dr. " + leader.getName() + " promoted Dr. " + target.getName() + " to leader!");
        saveData();
    }

    public void toggleTeamChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (teamChatEnabled.contains(uuid)) {
            teamChatEnabled.remove(uuid);
            player.sendMessage("§aTeam chat disabled. Messages will go to global chat.");
        } else {
            if (getPlayerTeam(player) == null) {
                player.sendMessage("§cYou must be in a team to enable team chat!");
                return;
            }
            teamChatEnabled.add(uuid);
            player.sendMessage("§aTeam chat enabled. Messages will go to your team.");
        }
        saveData();
    }

    public void distributeTeamXp(Player leader) {
        if (!isTeamLeader(leader)) {
            leader.sendMessage("§cYou must be a team leader to distribute XP!");
            return;
        }
        String teamName = getPlayerTeam(leader);
        TeamData team = teams.get(teamName);
        if (team.xpPool <= 0) {
            leader.sendMessage("§cNo XP in the team pool to distribute!");
            return;
        }
        int xpPerMember = team.xpPool / team.members.size();
        for (UUID uuid : team.members) {
            DoctorData memberData = doctors.get(uuid);
            if (memberData != null) {
                memberData.addXp(xpPerMember);
                Player member = plugin.getServer().getPlayer(uuid);
                if (member != null) {
                    member.sendMessage("§aReceived " + xpPerMember + " XP from team pool distribution!");
                }
            }
        }
        team.xpPool = 0;
        leader.sendMessage("§aDistributed " + xpPerMember + " XP to each team member!");
        notifyTeam(leader, "Leader " + leader.getName() + " distributed " + xpPerMember + " XP from the pool!");
        saveData();
    }

    public List<String> getTeamLeaderboard() {
        List<String> leaderboard = new ArrayList<>();
        teams.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().xpPool, a.getValue().xpPool))
            .forEach(entry -> {
                String teamName = entry.getKey();
                int xpPool = entry.getValue().xpPool;
                int totalMemberXp = entry.getValue().members.stream()
                    .mapToInt(uuid -> doctors.get(uuid) != null ? doctors.get(uuid).getXp() : 0)
                    .sum();
                leaderboard.add("§6" + teamName + ": §ePool: " + xpPool + " XP, §7Members: " + totalMemberXp + " XP");
            });
        return leaderboard;
    }

    public List<Map.Entry<String, TeamData>> getSortedTeams() {
        return teams.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().xpPool, a.getValue().xpPool))
            .collect(Collectors.toList());
    }

    public int getTotalTeams() {
        return teams.size();
    }

    public boolean isTeamChatEnabled(Player player) {
        return teamChatEnabled.contains(player.getUniqueId());
    }

    public void sendTeamMessage(Player sender, String message) {
        String teamName = getPlayerTeam(sender);
        if (teamName == null) {
            sender.sendMessage("§cYou’re not in a team!");
            return;
        }
        for (UUID uuid : teams.get(teamName).members) {
            Player teammate = plugin.getServer().getPlayer(uuid);
            if (teammate != null) {
                teammate.sendMessage("§6[Team " + teamName + "] §7" + sender.getName() + ": " + message);
            }
        }
    }

    public String getPlayerTeam(Player player) {
        return teams.entrySet().stream()
            .filter(entry -> entry.getValue().members.contains(player.getUniqueId()))
            .map(Map.Entry::getKey)
            .findFirst().orElse(null);
    }

    public boolean isTeamLeader(Player player) {
        String teamName = getPlayerTeam(player);
        return teamName != null && teams.get(teamName).leader.equals(player.getUniqueId());
    }

    public Set<UUID> getTeamMembers(String teamName) {
        return teams.containsKey(teamName) ? new HashSet<>(teams.get(teamName).members) : new HashSet<>();
    }

    public UUID getTeamLeader(String teamName) {
        return teams.containsKey(teamName) ? teams.get(teamName).leader : null;
    }

    public int getTeamXpPool(String teamName) {
        return teams.containsKey(teamName) ? teams.get(teamName).xpPool : 0;
    }

    public void addToTeamXpPool(String teamName, int xp) {
        if (teams.containsKey(teamName)) {
            teams.get(teamName).xpPool += xp;
            saveData();
        }
    }

    private void notifyTeam(Player player, String message) {
        String teamName = getPlayerTeam(player);
        if (teamName != null) {
            for (UUID uuid : teams.get(teamName).members) {
                Player teammate = plugin.getServer().getPlayer(uuid);
                if (teammate != null && teammate != player) {
                    teammate.sendMessage("§6[Team " + teamName + "] §7" + message);
                }
            }
        }
    }

    private void shareTeamXp(Player healer, int xp) {
        String teamName = getPlayerTeam(healer);
        if (teamName != null) {
            int sharedXp = xp / 2;
            int poolXp = (int) (xp * ConfigUtil.getTeamXpPoolShare());
            addToTeamXpPool(teamName, poolXp);
            for (UUID uuid : teams.get(teamName).members) {
                if (!uuid.equals(healer.getUniqueId())) {
                    DoctorData teammateData = doctors.get(uuid);
                    if (teammateData != null) {
                        teammateData.addXp(sharedXp);
                        Player teammate = plugin.getServer().getPlayer(uuid);
                        if (teammate != null) {
                            teammate.sendMessage("§aGained " + sharedXp + " XP from team healing!");
                        }
                    }
                }
            }
        }
    }

    public void saveData() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (DoctorData data : doctors.values()) {
            String path = "doctors." + data.getUuid().toString();
            yaml.set(path + ".level", data.getLevel());
            yaml.set(path + ".xp", data.getXp());
            yaml.set(path + ".lastHealTime", data.getLastHealTime());
            yaml.set(path + ".lastDiagnoseTime", data.getLastDiagnoseTime());
            yaml.set(path + ".teamChatEnabled", teamChatEnabled.contains(data.getUuid()));
        }
        for (Map.Entry<String, TeamData> entry : teams.entrySet()) {
            String path = "teams." + entry.getKey();
            yaml.set(path + ".leader", entry.getValue().leader.toString());
            yaml.set(path + ".members", entry.getValue().members.stream().map(UUID::toString).toList());
            yaml.set(path + ".xpPool", entry.getValue().xpPool);
        }
        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save doctor data: " + e.getMessage());
        }
    }

    public void loadData() {
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

        if (yaml.contains("doctors")) {
            for (String uuidStr : yaml.getConfigurationSection("doctors").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                String path = "doctors." + uuidStr;
                int level = yaml.getInt(path + ".level", 1);
                int xp = yaml.getInt(path + ".xp", 0);
                long lastHealTime = yaml.getLong(path + ".lastHealTime", 0);
                long lastDiagnoseTime = yaml.getLong(path + ".lastDiagnoseTime", 0);
                doctors.put(uuid, new DoctorData(uuid, level, xp, lastHealTime, lastDiagnoseTime));
                if (yaml.getBoolean(path + ".teamChatEnabled", false)) {
                    teamChatEnabled.add(uuid);
                }
            }
        }

        if (yaml.contains("teams")) {
            for (String teamName : yaml.getConfigurationSection("teams").getKeys(false)) {
                String path = "teams." + teamName;
                UUID leader = UUID.fromString(yaml.getString(path + ".leader"));
                List<String> memberStrings = yaml.getStringList(path + ".members");
                Set<UUID> members = new HashSet<>();
                for (String member : memberStrings) {
                    members.add(UUID.fromString(member));
                }
                int xpPool = yaml.getInt(path + ".xpPool", 0);
                teams.put(teamName, new TeamData(leader, members, xpPool));
            }
        }
    }

    private static class TeamData {
        UUID leader;
        Set<UUID> members;
        int xpPool;

        TeamData() {
            this.members = new HashSet<>();
            this.xpPool = 0;
        }

        TeamData(UUID leader, Set<UUID> members) {
            this.leader = leader;
            this.members = members;
            this.xpPool = 0;
        }

        TeamData(UUID leader, Set<UUID> members, int xpPool) {
            this.leader = leader;
            this.members = members;
            this.xpPool = xpPool;
        }
    }
}