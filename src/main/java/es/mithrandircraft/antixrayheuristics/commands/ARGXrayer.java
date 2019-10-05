package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGXrayer{
    public static void X(CommandSender sender, String arg) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Xrayer")) {
                XrayerHandler.HandleXrayer(arg);
            } else player.sendMessage("You do not have permission to execute this command.");
        } else { //Is console
            XrayerHandler.HandleXrayer(arg);
        }
    }
}
