package es.mithrandircraft.antixrayheuristics.events;

import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClickEv implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public ClickEv(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @EventHandler
    public void clickEvent(InventoryClickEvent e)
    {
        //GUI CLICK EVENT:
        //Check if xrayer vault gui item was clicked:
        if(e.getView().getTitle().equalsIgnoreCase("Xrayer Vault"))
        {
            if(e.getRawSlot() < e.getView().getTopInventory().getSize()) //The slot clicked was from the upper window
            {
                e.setCancelled(true);
                //Check if item was clicked:
                if(e.getCurrentItem() == null) //Nothing clicked
                {
                    return;
                }
                else if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) //Clicked on player head
                {
                    //Open xrayer's confiscated inventory: The slot the item we clicked is on + the page we're on multiplied by the entry slots range (45 player heads) is equal to the xrayer's UUID position in the vault's XrayerUUID's array:
                    mainClassAccess.vault.OpenXrayerConfiscatedInventory((Player) e.getView().getPlayer(), e.getRawSlot() + mainClassAccess.vault.GetPage(e.getWhoClicked().getName()) * 45);
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetNextButtonFormat())) //Clicked next button
                {
                    //Show next row:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()) + 1);
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetPrevButtonFormat())) //Clicked prev button
                {
                    //Show previous row:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()) - 1);
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetPurgeButtonFormat())) //Clicked purge vault button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Vault.Purge")) {
                        //Dump registered xrayers:
                        mainClassAccess.mm.DeleteRegisteredXrayers();
                        //refresh:
                        mainClassAccess.vault.UpdateUUIDList();
                        mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                    }
                    else e.getView().getPlayer().sendMessage("You do not have permission to execute this command.");
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetRefreshButtonFormat())) //Clicked refresh vault button
                {
                    //refresh:
                    mainClassAccess.vault.UpdateUUIDList();
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetBackButtonFormat())) //Clicked back button
                {
                    //go back to previous page:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetPurgePlayerButtonFormat())) //Clicked purge player button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Commands.PurgePlayer")) {
                        //purge xrayer from database:
                        mainClassAccess.mm.DeleteXrayer(mainClassAccess.vault.GetInspectedXrayer(e.getWhoClicked().getName()));
                        //remove purged uuid from being listed in vault:
                        mainClassAccess.vault.RemoveXrayerUUIDFromList(mainClassAccess.vault.GetInspectedXrayer(e.getWhoClicked().getName()));
                        //go back to previous page with removed uuid:
                        mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                    }
                    else e.getView().getPlayer().sendMessage("You do not have permission to execute this command.");
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetAbsolvePlayerButtonFormat()))
                {
                    if(e.getWhoClicked().hasPermission("AXH.Commands.AbsolvePlayer")) {
                        //return inventory to player, and do the rest if player was online (function returns true if it worked):
                        if (XrayerHandler.PlayerAbsolver(mainClassAccess.vault.GetInspectedXrayer(e.getWhoClicked().getName()), mainClassAccess.mm.GetXrayerBelongings(mainClassAccess.vault.GetInspectedXrayer(e.getWhoClicked().getName())))) {
                            //purge player from database:
                            mainClassAccess.mm.DeleteXrayer(mainClassAccess.vault.GetInspectedXrayer(e.getWhoClicked().getName()));
                            //remove absolved uuid from being listed in vault:
                            mainClassAccess.vault.RemoveXrayerUUIDFromList(mainClassAccess.vault.GetInspectedXrayer(e.getWhoClicked().getName()));
                            //go back to previous page with removed uuid:
                            mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                        }
                        else e.getWhoClicked().sendMessage("[AntiXrayHeuristics] Player wasn't online. You can only absolve online players in order to return their items.");
                    }
                    else e.getView().getPlayer().sendMessage("You do not have permission to execute this command.");
                }
            }
            else if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) //This check stops item stack movement with shift between inventories
            {
                e.setCancelled(true);
            }
        }
    }
}
