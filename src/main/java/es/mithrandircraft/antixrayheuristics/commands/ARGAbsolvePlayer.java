package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGAbsolvePlayer {
    public static void A(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.AbsolvePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    //return inventory to player
                    XrayerHandler.PlayerAbsolver(target.getUniqueId().toString(), mainClass.mm.GetXrayerBelongings(target.getUniqueId().toString()));
                    sender.sendMessage("[AntiXrayHeuristics] Absolving player, sent confiscated items back to player.");
                    //purge player from database:
                    mainClass.mm.DeleteXrayer(target.getUniqueId().toString());
                } else sender.sendMessage("[AntiXrayHeuristics] Player wasn't online. You can only absolve online players in order to return their items.");
            }
            else sender.sendMessage("You do not have permission to execute this command.");
        }
        else { //Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) { //Player online
                if (XrayerHandler.PlayerAbsolver(target.getUniqueId().toString(), mainClass.mm.GetXrayerBelongings(target.getUniqueId().toString()))) {
                    mainClass.mm.DeleteXrayer(target.getUniqueId().toString());
                } else
                    System.out.print("[AntiXrayHeuristics] Player wasn't online. You can only absolve online players in order to return their items.");
            } else System.out.print("[AntiXrayHeuristics] Player wasn't online. You can only absolve online players in order to return their items.");
        }
    }
}
