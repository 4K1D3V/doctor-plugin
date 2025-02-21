package pro.akii.ks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pro.akii.ks.DoctorPlugin;
import pro.akii.ks.data.DoctorData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler with tab completion for all Doctor plugin commands.
 */
public class DoctorCommand implements CommandExecutor, TabCompleter {

    private final DoctorPlugin plugin;

    public DoctorCommand(DoctorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is for players only!");
            return true;
        }

        if (!player.hasPermission("doctor.use")) {
            player.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /doctor [register|diagnose|heal|stats|kit|team|leaderboard] [args]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "register":
                plugin.getDoctorManager().registerDoctor(player);
                break;

            case "diagnose":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /doctor diagnose <player>");
                    return true;
                }
                Player targetDiagnose = plugin.getServer().getPlayer(args[1]);
                plugin.getDoctorManager().diagnosePlayer(player, targetDiagnose);
                break;

            case "heal":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /doctor heal <player>");
                    return true;
                }
                Player targetHeal = plugin.getServer().getPlayer(args[1]);
                plugin.getDoctorManager().healPlayer(player, targetHeal, player.getInventory().getItemInMainHand());
                break;

            case "stats":
                DoctorData data = plugin.getDoctorManager().getDoctorData(player);
                if (data == null) {
                    player.sendMessage("§cYou are not a Doctor!");
                } else {
                    player.sendMessage("§6Doctor Stats:");
                    player.sendMessage("§7- Level: " + data.getLevel());
                    player.sendMessage("§7- XP: " + data.getXp() + "/" + (data.getLevel() * 100));
                    String team = plugin.getDoctorManager().getPlayerTeam(player);
                    player.sendMessage("§7- Team: " + (team != null ? team : "None"));
                }
                break;

            case "kit":
                plugin.getDoctorManager().openMedicalKit(player);
                break;

            case "team":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /doctor team [join <name>|leave|gui|kick <player>|distribute]");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "join":
                        if (args.length < 3) {
                            player.sendMessage("§cUsage: /doctor team join <name>");
                            return true;
                        }
                        plugin.getDoctorManager().joinTeam(player, args[2]);
                        break;
                    case "leave":
                        plugin.getDoctorManager().leaveTeam(player);
                        break;
                    case "gui":
                        plugin.getDoctorManager().openTeamGUI(player);
                        break;
                    case "kick":
                        if (args.length < 3) {
                            player.sendMessage("§cUsage: /doctor team kick <player>");
                            return true;
                        }
                        Player targetKick = plugin.getServer().getPlayer(args[2]);
                        if (targetKick == null) {
                            player.sendMessage("§cPlayer not found!");
                            return true;
                        }
                        plugin.getDoctorManager().kickFromTeam(player, targetKick);
                        break;
                    case "distribute":
                        plugin.getDoctorManager().distributeTeamXp(player);
                        break;
                    default:
                        player.sendMessage("§cUsage: /doctor team [join <name>|leave|gui|kick <player>|distribute]");
                }
                break;

            case "leaderboard":
                if (args.length > 1 && args[1].equalsIgnoreCase("gui")) {
                    int page = args.length > 2 ? Math.max(0, Integer.parseInt(args[2]) - 1) : 0;
                    plugin.getDoctorManager().openLeaderboardGUI(player, page);
                } else {
                    player.sendMessage("§6Team XP Leaderboard:");
                    plugin.getDoctorManager().getTeamLeaderboard().forEach(player::sendMessage);
                }
                break;

            default:
                player.sendMessage("§cUnknown subcommand! Use: register, diagnose, heal, stats, kit, team, leaderboard.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission("doctor.use")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("register", "diagnose", "heal", "stats", "kit", "team", "leaderboard"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "diagnose":
                case "heal":
                    completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
                    break;
                case "team":
                    completions.addAll(Arrays.asList("join", "leave", "gui", "kick", "distribute"));
                    break;
                case "leaderboard":
                    completions.add("gui");
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "team":
                    if (args[1].equalsIgnoreCase("kick")) {
                        String teamName = plugin.getDoctorManager().getPlayerTeam(player);
                        if (teamName != null && plugin.getDoctorManager().isTeamLeader(player)) {
                            completions.addAll(plugin.getDoctorManager().getTeamMembers(teamName).stream()
                                .map(uuid -> plugin.getServer().getPlayer(uuid))
                                .filter(Objects::nonNull)
                                .map(Player::getName)
                                .collect(Collectors.toList()));
                        }
                    } else if (args[1].equalsIgnoreCase("join")) {
                        completions.add("<team_name>");
                    }
                    break;
                case "leaderboard":
                    if (args[1].equalsIgnoreCase("gui")) {
                        int totalPages = (int) Math.ceil((double) plugin.getDoctorManager().getTotalTeams() / 18.0);
                        for (int i = 1; i <= totalPages; i++) {
                            completions.add(String.valueOf(i));
                        }
                    }
                    break;
            }
        }
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}