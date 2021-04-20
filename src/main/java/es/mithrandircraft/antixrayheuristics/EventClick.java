//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

class EventClick implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public EventClick(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @EventHandler
    public void clickEvent(InventoryClickEvent e)
    {
        //GUI CLICK EVENT:

        //Check if xrayer vault gui item was clicked:
        if(e.getView().getTitle().contains(mainClassAccess.vault.GetGUITitle()))
        {
            if(e.getRawSlot() < e.getView().getTopInventory().getSize()) //The slot clicked was from the upper window
            {
                Material refMaterial;
                if(mainClassAccess.spigotVersion.version.GetValue() > 109) refMaterial = Material.LEGACY_SKULL_ITEM;
                else refMaterial = Material.PLAYER_HEAD;

                //Check if a relevant GUI item was clicked:
                if(e.getCurrentItem() == null) //Nothing clicked
                {
                    e.setCancelled(true);
                }
                else if(e.getCurrentItem().getData().getItemType().equals(refMaterial) && e.getSlot() == 49) //Clicked on player head, and it WAS located at slot 49 (which shows up in xrayer confiscated belongings inspector)
                {
                    //Teleport to player detection (HandleLocation) coordinates
                    String viewerName = e.getWhoClicked().getName();
                    String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(viewerName);
                    mainClassAccess.vault.TeleportToDetectionCoordinates((Player) e.getWhoClicked(), xrayerUUID);
                }
                else if(e.getCurrentItem().getData().getItemType().equals(refMaterial)) //Clicked on handled xrayer entry
                {
                    //Open xrayer's confiscated inventory: The slot the item we clicked is on + the page we're on multiplied by the entry slots range (45 player heads) is equal to the xrayer's UUID position in the vault's XrayerUUID's array:
                    mainClassAccess.vault.OpenXrayerConfiscatedInventory((Player) e.getWhoClicked(), e.getRawSlot() + mainClassAccess.vault.GetPage(e.getWhoClicked().getName()) * 45);
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.nextButton.getItemMeta().getDisplayName())) //Clicked next button
                {
                    //Show next row:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()) + 1);
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.prevButton.getItemMeta().getDisplayName())) //Clicked prev button
                {
                    //Show previous row:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()) - 1);
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.purgeButton.getItemMeta().getDisplayName())) //Clicked purge vault button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Vault.Purge")) {
                        mainClassAccess.vault.PurgeAllXrayersAndRefreshVault();
                    }
                    else e.getView().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.refreshButton.getItemMeta().getDisplayName())) //Clicked refresh vault button
                {
                    //refresh:
                    mainClassAccess.vault.UpdateXrayerInfoLists((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.backButton.getItemMeta().getDisplayName())) //Clicked back button
                {
                    //go back to previous page:
                    mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(e.getWhoClicked().getName()));
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.purgePlayerButton.getItemMeta().getDisplayName())) //Clicked purge player button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Commands.PurgePlayer")) {
                        mainClassAccess.vault.XrayerDataRemover(e.getWhoClicked().getName(), true);
                    }
                    else e.getView().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
                }
                else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.absolvePlayerButton.getItemMeta().getDisplayName())) //Clicked absolve player button
                {
                    if(e.getWhoClicked().hasPermission("AXH.Commands.AbsolvePlayer")) {
                        final String viewerName = e.getWhoClicked().getName();
                        final String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(viewerName);
                        //return inventory to player, and do the rest if player was online (function returns true if it worked):
                        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(xrayerUUID, new CallbackGetXrayerBelongings()
                        {
                            @Override
                            public void onQueryDone(ItemStack[] belongings)
                            {
                                if (XrayerHandler.PlayerAbsolver(xrayerUUID, belongings, mainClassAccess)) {
                                    mainClassAccess.vault.XrayerDataRemover(viewerName, true);
                                }
                                else e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                            }
                        }));
                    }
                    else e.getView().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
                }
                e.setCancelled(true);
            }
            else if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) //This check stops item stack movement with shift between inventories
            {
                e.setCancelled(true);
            }
        }
    }
}
