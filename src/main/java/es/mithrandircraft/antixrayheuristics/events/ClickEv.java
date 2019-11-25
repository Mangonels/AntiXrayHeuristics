//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.events;

import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import es.mithrandircraft.antixrayheuristics.callbacks.GetXrayerBelongingsCallback;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
                else if(e.getCurrentItem().getType() == Material.PLAYER_HEAD && e.getSlot() != 49) //Clicked on player head, and it wasn't located at slot 49 (which shows up in xrayer confiscated belongings inspector)
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
                        mainClassAccess.vault.PurgeAllXrayersAndRefreshVault();
                    }
                    else e.getView().getPlayer().sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetRefreshButtonFormat())) //Clicked refresh vault button
                {
                    //refresh:
                    mainClassAccess.vault.UpdateXrayerInfoLists((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetBackButtonFormat())) //Clicked back button
                {
                    //go back to previous page:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetPurgePlayerButtonFormat())) //Clicked purge player button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Commands.PurgePlayer")) {
                        mainClassAccess.vault.XrayerDataRemover(e.getWhoClicked().getName(), true);
                    }
                    else e.getView().getPlayer().sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
                }
                else if(e.getCurrentItem().equals(mainClassAccess.vault.GetAbsolvePlayerButtonFormat())) //Clicked absolve player button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Commands.AbsolvePlayer")) {
                        final String viewerName = e.getWhoClicked().getName();
                        final String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(viewerName);
                        //return inventory to player, and do the rest if player was online (function returns true if it worked):
                        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(xrayerUUID, new GetXrayerBelongingsCallback()
                        {
                            @Override
                            public void onQueryDone(ItemStack[] belongings)
                            {
                                if (XrayerHandler.PlayerAbsolver(xrayerUUID, belongings, mainClassAccess)) {
                                    mainClassAccess.vault.XrayerDataRemover(viewerName, true);
                                }
                                else e.getWhoClicked().sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerNotOnlineOnAbsolution"));
                            }
                        }));
                    }
                    else e.getView().getPlayer().sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
                }
            }
            else if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) //This check stops item stack movement with shift between inventories
            {
                e.setCancelled(true);
            }
        }
    }
}
