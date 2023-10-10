//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

class CommandAXH implements CommandExecutor {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public CommandAXH(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { //Check for what command it is in following list:
        if(args.length == 1)
        {
            if (args[0].equalsIgnoreCase("xrayer") || args[0].equalsIgnoreCase("x")) CommandARGXrayer.X(sender, mainClassAccess);
            else if (args[0].equalsIgnoreCase("vault") || args[0].equalsIgnoreCase("v")) CommandARGVault.V(sender, mainClassAccess);
            else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) CommandARGReload.R(sender, mainClassAccess);
            else if (args[0].equalsIgnoreCase("resetsuspicion") || args[0].equalsIgnoreCase("rs")) CommandARGResetSuspicion.RS(sender, mainClassAccess);
            else if (args[0].equalsIgnoreCase("suspicion") || args[0].equalsIgnoreCase("s")) CommandARGSuspicion.S(sender, mainClassAccess);
            else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("InvalidCMDArg")));
        }
        else if(args.length == 2)
        {
            if (args[0].equalsIgnoreCase("xrayer") || args[0].equalsIgnoreCase("x")) CommandARGXrayer.X(sender, args[1]);
            else if (args[0].equalsIgnoreCase("absolve") || args[0].equalsIgnoreCase("a")) CommandARGAbsolvePlayer.A(sender, args[1], mainClassAccess);
            else if (args[0].equalsIgnoreCase("purge") || args[0].equalsIgnoreCase("p")) CommandARGPurgePlayer.P(sender, args[1], mainClassAccess);
            else if (args[0].equalsIgnoreCase("resetsuspicion") || args[0].equalsIgnoreCase("rs")) CommandARGResetSuspicion.RS(sender, args[1], mainClassAccess);
            else if (args[0].equalsIgnoreCase("suspicion") || args[0].equalsIgnoreCase("s")) CommandARGSuspicion.S(sender, args[1], mainClassAccess);
            else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("InvalidCMDArg")));
        }
        else //Show help
        {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("InvalidCMDArg")));
        }
        return false;
    }
}
