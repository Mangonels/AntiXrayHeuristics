package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.MiningSession;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetXraySuspicion implements CommandExecutor {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public ResetXraySuspicion(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(args.length == 0) //Non parametrized command:
         {
             if(sender instanceof Player) //Is player
             {
                 Player player = (Player) sender;
                 if(player.hasPermission("AXH.commands.ResetSuspicion")) {
                     MiningSession tempMS = AntiXrayHeuristics.sessions.get(player.getName());
                     if (tempMS != null){
                         AntiXrayHeuristics.sessions.remove(player.getName());
                         player.sendMessage("Your suspicion has been nullified.");
                     }
                     else player.sendMessage("You are not suspicious of Xray usage. No suspicion reset applied.");
                 }
                 else player.sendMessage("You do not have permission to execute this command.");
             }
             else System.out.println("You need to be a player in order to execute this command without arguments."); //Is console
         }
         else //Parametrized command:
         {
             if (sender instanceof Player) { //Is player
                 Player player = (Player) sender;
                 if(player.hasPermission("AXH.commands.ResetSuspicion")) {
                     MiningSession tempMS = AntiXrayHeuristics.sessions.get(args[0]);
                     if (tempMS != null) {
                         AntiXrayHeuristics.sessions.remove(args[0]);
                         player.sendMessage(args[0] + "'s suspicion has been nullified.");
                     } else
                         player.sendMessage(args[0] + " Is not suspicious of Xray usage. No suspicion reset was applied.");
                 }
                 else player.sendMessage("You do not have permission to execute this command.");
             }
             else {
                 MiningSession tempMS = AntiXrayHeuristics.sessions.get(args[0]);
                 if (tempMS != null) {
                     AntiXrayHeuristics.sessions.remove(args[0]);
                     System.out.println(args[0] + "'s suspicion has been nullified.");
                 } else
                     System.out.println(args[0] + " Is not suspicious of Xray usage. No suspicion reset was applied."); //Is console
             }
         }
     return false;
    }
}
