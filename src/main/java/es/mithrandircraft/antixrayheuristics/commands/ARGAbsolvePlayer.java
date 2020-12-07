//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import es.mithrandircraft.antixrayheuristics.callbacks.GetXrayerBelongingsCallback;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ARGAbsolvePlayer {
    public static void A(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.AbsolvePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    //return inventory to player
                    final String targetUUID = target.getUniqueId().toString();
                    Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.GetXrayerBelongings(targetUUID, new GetXrayerBelongingsCallback()
                    {
                        @Override
                        public void onQueryDone(ItemStack[] belongings)
                        {
                            if (XrayerHandler.PlayerAbsolver(targetUUID, belongings, mainClass)) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerAbsolved")));
                                //purge player from database:
                                mainClass.vault.XrayerDataRemover(arg, false);
                            } else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                        }
                    }));
                } else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
            }
            else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        }
        else { //Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) { //Player online
                //return inventory to player
                final String targetUUID = target.getUniqueId().toString();
                Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.GetXrayerBelongings(targetUUID, new GetXrayerBelongingsCallback()
                {
                    @Override
                    public void onQueryDone(ItemStack[] belongings)
                    {
                        if (XrayerHandler.PlayerAbsolver(targetUUID, belongings, mainClass)) {
                            mainClass.vault.XrayerDataRemover(arg, false);
                        } else
                            System.out.print(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                    }
                }));
            } else System.out.print(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
        }
    }
}
