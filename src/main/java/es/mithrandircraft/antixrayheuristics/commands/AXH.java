package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AXH implements CommandExecutor {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public AXH(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { //Check for what command it is in following list:
        if(args.length == 1)
        {
            if (args[0].equalsIgnoreCase("vault") || args[0].equalsIgnoreCase("v")) ARGVault.V(sender, mainClassAccess);
            else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) ARGReload.R(sender, mainClassAccess);
            else if (args[0].equalsIgnoreCase("resetsuspicion") || args[0].equalsIgnoreCase("rs")) ARGResetSuspicion.RS(sender);
            //else if (args[0].equalsIgnoreCase("suspicion") || args[0].equalsIgnoreCase("s")) ARGSuspicion.S(sender);
            else sender.sendMessage(LocaleManager.get().getString("InvalidCMDArg"));
        }
        else if(args.length == 2)
        {
            if (args[0].equalsIgnoreCase("xrayer") || args[0].equalsIgnoreCase("x")) ARGXrayer.X(sender, args[1]);
            else if (args[0].equalsIgnoreCase("absolve") || args[0].equalsIgnoreCase("a")) ARGAbsolvePlayer.A(sender, args[1], mainClassAccess);
            else if (args[0].equalsIgnoreCase("purge") || args[0].equalsIgnoreCase("p")) ARGPurgePlayer.P(sender, args[1], mainClassAccess);
            else if (args[0].equalsIgnoreCase("resetsuspicion") || args[0].equalsIgnoreCase("rs")) ARGResetSuspicion.RS(sender, args[1]);
            //else if (args[0].equalsIgnoreCase("suspicion") || args[0].equalsIgnoreCase("s")) ARGSuspicion.S(sender, args[1]);
            else sender.sendMessage(LocaleManager.get().getString("InvalidCMDArg"));
        }
        else //Show help
        {
            sender.sendMessage(LocaleManager.get().getString("InvalidCMDArg"));
        }
        return false;
    }
}
