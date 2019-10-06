package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.UUID;

public class XrayerHandler {

    private static void XrayerWarn(String xrayername) //Sends a warning message to an xrayer by name
    {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission("AXH.XrayerWarning")) player.sendMessage(PlaceholderManager.SubstitutePlaceholders("[AntiXrayHeuristics] {PlayerName} was automatically registered and handled for xraying.", xrayername));
        }
    }

    public static void HandleXrayer(String xrayername) //Executes what must be done to an inputted Xrayer by name.
    {
        AntiXrayHeuristics mainClass = JavaPlugin.getPlugin(AntiXrayHeuristics.class);

        mainClass.getConfig();
        Player player = Bukkit.getServer().getPlayer(xrayername); //Reference to player
        if(player != null)
        {
            //Send message to xrayer if configured:
            if (mainClass.getConfig().getBoolean("SendMessageToPlayer")) {
                String m = mainClass.getConfig().getString("MessageToSend");
                if (m != null) player.sendMessage(m);
            }

            //Store xrayer's data (and inventory only if configured):
            mainClass.mm.StorePlayerData(xrayername);

            //Remove all of the xrayer's belongings if configured:
            if (mainClass.getConfig().getBoolean("ClensePlayerItems")) {
                try{player.getInventory().clear();
                    player.getEquipment().clear(); } catch(Exception e){ if(mainClass.getConfig().getBoolean("Debug")) System.out.println("Failed to remove player " + xrayername + "'s equipment while attempting to handle as Xrayer."); }
            }

            //Delete xrayer mining session if configured:
            if (mainClass.getConfig().getBoolean("NullifySuspicionAferPunish")) {
                AntiXrayHeuristics.sessions.remove(player.getName());
            }

            //Execute configured commands:
            for (int i = 0; i < mainClass.getConfig().getStringList("CommandsExecuted").size(); i++) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), PlaceholderManager.SubstitutePlaceholders(mainClass.getConfig().getStringList("CommandsExecuted").get(i), xrayername));
            }

            //Console message:
            System.out.println(PlaceholderManager.SubstitutePlaceholders("[AntiXrayHeuristics] {PlayerName} was automatically registered and handled for xraying.", xrayername));

            //Warn players with permission:
            if(mainClass.getConfig().getBoolean("TellPlayersWithPermission")) XrayerWarn(xrayername);
        }
        else{ System.out.println("Player named " + xrayername + " was not found while attempting to handle as Xrayer. Player must be online."); }
    }

    private static void DropItemAtPlayerLocation(ItemStack item, Player p) //Drops items at player location
    {
        if(item != null && item.getType() != Material.AIR) //Error prevention
        {
            Item dropedItem = p.getWorld().dropItem(p.getLocation(), item); //Drop item at player location
            dropedItem.setVelocity(new Vector(0, 0, 0)); //Remove velocity from dropped item
        }
    }

    public static boolean PlayerAbsolver(String uuid, ItemStack[] possessions) //Currently returns confiscated items to an absolved player. If the player isn't online, function returns false.
    {
        Player target = Bukkit.getServer().getPlayer(UUID.fromString(uuid));
        if (target != null) { //Player online
            //Return inventory
            for(int i = 0; i < 36; i++)
            {
                //Check if there's a free slot and place confiscated possession if so (With a bunch of error preventions):
                if (possessions[i] != null && possessions[i].getType() != Material.AIR && target.getInventory().firstEmpty() != -1) target.getInventory().addItem(possessions[i]);
                else if(possessions[i] != null && possessions[i].getType() != Material.AIR) DropItemAtPlayerLocation(possessions[i], target); //No space, drop on floor.
            }
            //Return equipment
            if(target.getEquipment().getItemInOffHand() == null) target.getEquipment().setItemInOffHand(possessions[36]); //Check if nothing in slot
            else DropItemAtPlayerLocation(possessions[36], target); //No space, drop on floor.
            if(target.getEquipment().getBoots() == null) target.getEquipment().setBoots(possessions[37]); //Same with rest...
            else DropItemAtPlayerLocation(possessions[37], target);
            if(target.getEquipment().getLeggings() == null) target.getEquipment().setLeggings(possessions[38]);
            else DropItemAtPlayerLocation(possessions[38], target);
            if(target.getEquipment().getChestplate() == null) target.getEquipment().setChestplate(possessions[39]);
            else DropItemAtPlayerLocation(possessions[39], target);
            if(target.getEquipment().getHelmet() == null) target.getEquipment().setHelmet(possessions[40]);
            else DropItemAtPlayerLocation(possessions[40], target);

            System.out.print("[AntiXrayHeuristics] Player " + target.getName() + "has been absolved from the Xrayer Vault by a player with permission, and his items have been returned.");
            return true;
        }
        else return false; //Player offline
    }
}