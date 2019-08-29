package es.mithrandircraft.antixrayheuristics.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AXH implements CommandExecutor {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public AXH(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) //Non parametrized command:
        {
            if (sender instanceof Player) //Is player
            {
                Player player = (Player) sender;
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("AXH.commands.Reload")) {
                        //Do reload
                        mainClassAccess.reloadConfig();
                        player.sendMessage("AntiXrayHeuristics has reloaded.");
                    } else player.sendMessage("You do not have permission to execute this command.");
                } else player.sendMessage("Invalid command argument.");
            } else { //Is console
                if (args[0].equalsIgnoreCase("reload")) {
                    //Do reload
                    mainClassAccess.reloadConfig();
                    System.out.println("AntiXrayHeuristics has reloaded.");
                } else System.out.println("Invalid command argument.");
            }
        }
        else if(args.length == 0)
        {
            System.out.println("Command instructions will be here soon.");
        }
        else
        {
            System.out.println("Invalid command argument. May also show command instructions here soon.");
        }
        return false;
    }
}
