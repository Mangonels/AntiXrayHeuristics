package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGReload {
    public static void R(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Reload")) {
                //Do reload
                mainClass.reloadConfig(); //Reload main config
                LocaleManager.reload(); //Reload locale config
                player.sendMessage(LocaleManager.get().getString("Reloaded"));
            } else player.sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
        } else { //Is console
                //Do reload
                mainClass.reloadConfig();
                LocaleManager.reload();
                System.out.println(LocaleManager.get().getString("Reloaded"));
        }
    }
}
