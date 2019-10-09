package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ARGVault {
    static void V(CommandSender sender, AntiXrayHeuristics mainClass)
    {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Vault")) {
                mainClass.vault.UpdateXrayerInfoLists(); //Update all of the xrayer's uuid's in vault's arraylist
                mainClass.vault.OpenVault(player, 0);

            } else player.sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
        } else { //Is console
            System.out.println(LocaleManager.get().getString("PlayerOnlyCommand"));
        }
    }
}
