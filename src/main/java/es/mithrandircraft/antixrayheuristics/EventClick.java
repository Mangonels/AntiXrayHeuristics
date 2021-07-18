//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

class EventClick implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public EventClick(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @EventHandler
    public void clickEvent(InventoryClickEvent e)
    {
        //GUI CLICK EVENT:

        //Check if click occured with xrayer vault gui view open:
        if(e.getView().getTitle().contains(mainClassAccess.vault.GetGUITitle()))
        {
            //A non-null, non AIR, uper window slot was clicked
            if(e.getCurrentItem() != null && e.getRawSlot() < e.getView().getTopInventory().getSize() && e.getCurrentItem().getType() != Material.AIR)
            {
                //An item was clicked
                final String playerWhoClicked = e.getWhoClicked().getName();
                if(mainClassAccess.vault.GetInspectedXrayer(playerWhoClicked) == null) //We're on the overall xrayer vault which shows xrayer entries
                {
                    switch(e.getSlot())
                    {
                        case 48:
                        {
                            //Purge all xrayers:
                            if(e.getWhoClicked().hasPermission("AXH.Vault.Purge")) {
                                mainClassAccess.vault.PurgeAllXrayersAndRefreshVault();
                            }
                            else e.getView().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));

                            break;
                        }
                        case 50:
                        {
                            //Refresh vault:
                            mainClassAccess.vault.UpdateXrayerInfoLists((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked));

                            break;
                        }
                        case 53:
                        {
                            //Show next vault row:
                            if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.nextButton.getItemMeta().getDisplayName()))
                                mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked) + 1);

                            break;
                        }
                        case 45:
                        {
                            //Show previous vault row:
                            if(e.getCurrentItem().getItemMeta().getDisplayName().equals(mainClassAccess.vault.prevButton.getItemMeta().getDisplayName()))
                                mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked) - 1);

                            break;
                        }
                        default:
                        {
                            if(e.getSlot() > -1 && e.getSlot() < 45)
                            {
                                //Open xrayer's confiscated inventory: The slot the item we clicked is on + the page we're on multiplied by the entry slots range (45 player heads) is equal to the xrayer's UUID position in the vault's XrayerUUID's array:
                                mainClassAccess.vault.OpenXrayerConfiscatedInventory((Player) e.getWhoClicked(), e.getRawSlot() + mainClassAccess.vault.GetPage(playerWhoClicked) * 45);
                            }

                            break;
                        }
                    }
                }
                else //We're inspecting an xrayer's information and possible confiscated items
                {
                    switch(e.getSlot())
                    {
                        case 45:
                        {
                            //Go back to previous page:
                            mainClassAccess.vault.OpenVault((Player) e.getView().getPlayer(), mainClassAccess.vault.GetPage(playerWhoClicked));

                            break;
                        }
                        case 53:
                        {
                            //Absolve player:
                            if(e.getWhoClicked().hasPermission("AXH.Commands.AbsolvePlayer")) {
                                final String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(playerWhoClicked);
                                //Return inventory to player, and do the rest if player was online:
                                Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(xrayerUUID, new CallbackGetXrayerBelongings()
                                {
                                    @Override
                                    public void onQueryDone(ItemStack[] belongings)
                                    {
                                        if (XrayerHandler.PlayerAbsolver(xrayerUUID, belongings, mainClassAccess)) {
                                            mainClassAccess.vault.XrayerDataRemover(playerWhoClicked, true);
                                        }
                                        else e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerNotOnlineOnAbsolution")));
                                    }
                                }));
                            }
                            else e.getView().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));

                            break;
                        }
                        case 51:
                        {
                            //Purge player:
                            if(e.getWhoClicked().hasPermission("AXH.Commands.PurgePlayer")) {
                                mainClassAccess.vault.XrayerDataRemover(playerWhoClicked, true);
                            }
                            else e.getView().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));

                            break;
                        }
                        case 49:
                        {
                            //Teleport to player detection (HandleLocation) coordinates:
                            String xrayerUUID = mainClassAccess.vault.GetInspectedXrayer(playerWhoClicked);
                            mainClassAccess.vault.TeleportToDetectionCoordinates((Player) e.getWhoClicked(), xrayerUUID);

                            break;
                        }
                    }
                }
            }

            e.setCancelled(true);
        }
    }
}