//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import es.mithrandircraft.antixrayheuristics.callbacks.StorePlayerDataCallback;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class XrayerHandler {

    private static void XrayerWarn(String xrayername) //Sends a warning message to an xrayer by name
    {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission("AXH.XrayerWarning")) player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix") + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("AutoHandledPlayer"), xrayername)));
        }
    }

    public static void HandleXrayer(String xrayername) //Executes what must be done to an inputted Xrayer by name.
    {
        AntiXrayHeuristics mainClass = JavaPlugin.getPlugin(AntiXrayHeuristics.class);
        
        Player player = Bukkit.getPlayer(xrayername); //Reference to player
        if(player != null)
        {
            //Send message to xrayer if configured:
            if (mainClass.getConfig().getBoolean("SendMessageToPlayer")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerMessageOnXray")));
            }

            //Store xrayer's data (and inventory only if configured):
            Bukkit.getScheduler().runTaskAsynchronously(mainClass, () -> mainClass.mm.StorePlayerData(player, new StorePlayerDataCallback(){
                @Override
                public void onInsertDone(int handleTimes) {
                    //The following are better occurring AFTER xrayer data storing is done asynchronously, that's why they're in this callback:

                    //Remove all of the xrayer's belongings if configured:
                    if (mainClass.getConfig().getBoolean("ClensePlayerItems")) {
                        try{player.getInventory().clear();
                            player.getEquipment().clear(); } catch(Exception e){ if(mainClass.getConfig().getBoolean("Debug")) System.out.println("Failed to remove player " + xrayername + "'s equipment while attempting to handle as Xrayer."); }
                    }

                    //Compare the handled time, and see if there's an action to be executed from config:
                    ConfigurationSection section = mainClass.getConfig().getConfigurationSection("CommandsExecutedOnXrayerDetected"); //Gets the whole hierarchical config section with commands to execute according to times detected
                    if(section != null && section.contains(String.valueOf(handleTimes))) {
                        ConfigurationSection subSection = mainClass.getConfig().getConfigurationSection("CommandsExecutedOnXrayerDetected." + String.valueOf(handleTimes)); //Gets the specific commands to execute if coinciding with amount of times detected
                        if(subSection != null) {
                            Map<String, Object> commandsToExecute = subSection.getValues(false); //Will contain all the configured commands to execute this time
                            for (Map.Entry<String, Object> pair : commandsToExecute.entrySet()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderManager.SubstitutePlayerNameAndHandleTimesPlaceholders((String) pair.getValue(), xrayername, Integer.toString(handleTimes))); //Executes the command and also replaces any placeholders within it
                            }
                        }
                    }
                }
            }));

            //Delete xrayer mining session if configured:
            if (mainClass.getConfig().getBoolean("NullifySuspicionAferPunish")) {
                mainClass.sessions.remove(player.getName());
            }

            //Console message:
            System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("AutoHandledPlayer") , xrayername));

            //Warn players with permission:
            if(mainClass.getConfig().getBoolean("TellPlayersWithPermission")) XrayerWarn(xrayername);
        }
        else{ System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("PlayerNotOnlineOnHandle") , xrayername)); }
    }

    private static void DropItemAtPlayerLocation(ItemStack item, Player p) //Drops items at player location
    {
        if(item != null && item.getType() != Material.AIR) //Error prevention
        {
            Item dropedItem = p.getWorld().dropItem(p.getLocation(), item); //Drop item at player location
            dropedItem.setVelocity(new Vector(0, 0, 0)); //Remove velocity from dropped item
        }
    }

    public static boolean PlayerAbsolver(String uuid, ItemStack[] possessions, AntiXrayHeuristics mainClassAccess) //Currently returns confiscated items to an absolved player. If the player isn't online, function returns false.
    {
        Player target = Bukkit.getPlayer(UUID.fromString(uuid));
        if (target != null) { //Player online
            //Return inventory
            for(int i = 0; i < 36; i++)
            {
                //Check if there's a free slot and place confiscated possession if so (With a bunch of error preventions):
                if (possessions[i] != null && possessions[i].getType() != Material.AIR && target.getInventory().firstEmpty() != -1) target.getInventory().addItem(possessions[i]);
                else if(possessions[i] != null && possessions[i].getType() != Material.AIR) DropItemAtPlayerLocation(possessions[i], target); //No space, drop on floor.
            }
            //Return equipment
            if(target.getEquipment().getItemInOffHand().getType().equals(Material.AIR)) target.getEquipment().setItemInOffHand(possessions[36]); //Check if nothing in slot
            else DropItemAtPlayerLocation(possessions[36], target); //No space, drop on floor.
            if(target.getEquipment().getBoots() == null) target.getEquipment().setBoots(possessions[40]); //Same with rest...
            else DropItemAtPlayerLocation(possessions[40], target);
            if(target.getEquipment().getLeggings() == null) target.getEquipment().setLeggings(possessions[39]);
            else DropItemAtPlayerLocation(possessions[39], target);
            if(target.getEquipment().getChestplate() == null) target.getEquipment().setChestplate(possessions[38]);
            else DropItemAtPlayerLocation(possessions[38], target);
            if(target.getEquipment().getHelmet() == null) target.getEquipment().setHelmet(possessions[37]);
            else DropItemAtPlayerLocation(possessions[37], target);

            System.out.print(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + target.getName() + LocaleManager.get().getString("AbsolvedPlayer"));

            //Execute configured commands:
            for (int i = 0; i < mainClassAccess.getConfig().getStringList("CommandsExecutedOnPlayerAbsolved").size(); i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(mainClassAccess.getConfig().getStringList("CommandsExecutedOnPlayerAbsolved").get(i), target.getName()));
            }

            return true;
        }
        else return false; //Player offline
    }
}