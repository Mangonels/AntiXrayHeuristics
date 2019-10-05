package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGPurgePlayer {
    public static void P(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.PurgePlayer")) {
                //purge xrayer from database:
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    mainClass.mm.DeleteXrayer(target.getUniqueId().toString());
                    sender.sendMessage("[AntiXrayHeuristics] The Xrayer's data was purged from the xrayer vault.");
                }
                else player.sendMessage("[AntiXrayHeuristics] Purging players through command is currently impossible for players that are not currently connected to the server. You CAN however purge individual players that are offline through the ingame XrayerVault GUI: /axh v");
            } else player.sendMessage("You do not have permission to execute this command.");
        } else { //Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) {
                mainClass.mm.DeleteXrayer(target.getUniqueId().toString());
            }
            else System.out.print("[AntiXrayHeuristics] Purging players through command is currently impossible for players that are not currently connected to the server. You CAN however purge individual players that are offline through the ingame XrayerVault GUI: /axh v");
        }
    }
}