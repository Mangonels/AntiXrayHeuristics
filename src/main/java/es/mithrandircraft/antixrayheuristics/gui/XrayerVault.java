package es.mithrandircraft.antixrayheuristics.gui;

import es.mithrandircraft.antixrayheuristics.math.MathFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class XrayerVault {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    private ArrayList<String> xrayerUUIDs;
    private int pages; //How many pages the vault should have

    private HashMap<String, PlayerViewInfo> viewers = new HashMap<String, PlayerViewInfo>(); //Stores who's viewing the GUI and info on what's being looked at.

    private ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private ItemStack nextButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    private ItemStack prevButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    private ItemStack purgeButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private ItemStack refreshButton = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
    private ItemStack backButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    private ItemStack purgePlayerButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private ItemStack absolvePlayerButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);

    public XrayerVault(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;

        ItemMeta separator_meta = separator.getItemMeta();
        separator_meta.setDisplayName(" ");
        separator.setItemMeta(separator_meta);

        ItemMeta next_meta = nextButton.getItemMeta();
        next_meta.setDisplayName(ChatColor.GREEN + "Next");
        nextButton.setItemMeta(next_meta);

        ItemMeta prev_meta = prevButton.getItemMeta();
        prev_meta.setDisplayName(ChatColor.GREEN + "Back");
        prevButton.setItemMeta(prev_meta);

        ItemMeta purge_meta = purgeButton.getItemMeta();
        purge_meta.setDisplayName(ChatColor.RED + "Purge vault");
        purge_meta.setLore(Arrays.asList("This will delete all xrayer", "entries within the vault,", "Entries are irrecoverable", "after they have been purged."));
        purgeButton.setItemMeta(purge_meta);

        ItemMeta refresh_meta = refreshButton.getItemMeta();
        refresh_meta.setDisplayName(ChatColor.BLUE + "Refresh vault");
        refresh_meta.setLore(Arrays.asList("Refreshes xrayer entries", "showing new, modified, or", "removed logged xrayers."));
        refreshButton.setItemMeta(refresh_meta);

        ItemMeta back_meta = backButton.getItemMeta();
        back_meta.setDisplayName(ChatColor.GREEN + "Go back");
        backButton.setItemMeta(back_meta);

        ItemMeta purgeplayer_meta = purgePlayerButton.getItemMeta();
        purgeplayer_meta.setDisplayName(ChatColor.RED + "Purge player");
        purgeplayer_meta.setLore(Arrays.asList("This will delete this specific", " xrayer entry and inventory.", "Entries are irrecoverable", "after they have been purged."));
        purgePlayerButton.setItemMeta(purgeplayer_meta);

        ItemMeta absolveplayer_meta = absolvePlayerButton.getItemMeta();
        absolveplayer_meta.setDisplayName(ChatColor.YELLOW + "Absolve player");
        absolveplayer_meta.setLore(Arrays.asList("Absolve this player,", "returning all confiscated", "items. If they don't fit in", "player's inventory, they", "will be dropped nearby.", "This also purges the player's", "entry in this vault."));
        absolvePlayerButton.setItemMeta(absolveplayer_meta);
    }

    public void UpdateUUIDList() //Updates the Xrayer UUID ArrayList
    {
        xrayerUUIDs = mainClassAccess.mm.GetXrayerUUIDs();
        pages = MathFunctions.Cut(45, xrayerUUIDs.size()); //Calculate pages considering the amount of registered xrayer uuid's, and that there can only be 27 results per page
    }

    //Button format getter methods:
    public ItemStack GetNextButtonFormat(){ return nextButton; }
    public ItemStack GetPrevButtonFormat(){ return prevButton; }
    public ItemStack GetPurgeButtonFormat(){ return purgeButton; }
    public ItemStack GetRefreshButtonFormat(){ return refreshButton; }

    public ItemStack GetBackButtonFormat(){ return backButton; }
    public ItemStack GetPurgePlayerButtonFormat(){ return purgePlayerButton; }
    public ItemStack GetAbsolvePlayerButtonFormat(){ return absolvePlayerButton; }

    public void OpenVault(Player player, int page) //Opens xrayer vault in specified page (This shows player heads with info about the xrayer)
    {
        Inventory gui = Bukkit.createInventory(null, 54, "Xrayer Vault"); //Vault contents to display
        viewers.put(player.getName(), new PlayerViewInfo(page)); //Register player as gui viewer on a certain page (used as reference)

        //Fill up vault:
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        UUID currentUUID;
        int iteration = 0;
        ListIterator<String> iter = xrayerUUIDs.listIterator(page * 45);
        while (iter.hasNext() && !(iteration >= 45)) //Fills up the vault page with skulls containing xrayer data
        {
            currentUUID = UUID.fromString(iter.next());
            meta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(currentUUID)); //Assigns player to head owner
            meta.setDisplayName(Bukkit.getServer().getOfflinePlayer(currentUUID).getName()); //Head name editing
            //meta.setLore(); //Head lore editing
            skull.setItemMeta(meta);
            gui.setItem(iteration, skull);
            iteration++;
        }

        //Lower section separators:
        gui.setItem(46, separator);
        gui.setItem(47, separator);
        gui.setItem(49, separator);
        gui.setItem(51, separator);
        gui.setItem(52, separator);

        //Lower section vault/gui stuff:
        if(page + 1 < pages) gui.setItem(53, nextButton); else gui.setItem(53, separator);
        if(page - 1 > -1) gui.setItem(45, prevButton); else gui.setItem(45, separator);
        gui.setItem(48, purgeButton);
        gui.setItem(50, refreshButton);

        player.openInventory(gui);
    }

    public void OpenXrayerConfiscatedInventory(Player player, int xrayerUUIDIndex) //Opens an xrayer's confiscated inventory by xrayer name
    {
        viewers.get(player.getName()).xrayerInvUUID = xrayerUUIDs.get(xrayerUUIDIndex); //Update uuid of xrayer we're watching

        Inventory inv = Bukkit.createInventory(null, 54, "Xrayer Vault"); //Vault contents to display

        ItemStack[] confiscatedItems = mainClassAccess.mm.GetXrayerBelongings(xrayerUUIDs.get(xrayerUUIDIndex));

        //Fill up vault:
        for (int i = 0; i < 41; i++) //Fills up the vault page with confiscated xrayer's inventory
        {
            inv.setItem(i, confiscatedItems[i]);
        }

        //Separation bar for mere decoration:
        for(int i = 45; i < 54; i++) { inv.setItem(i, separator); }
        inv.setItem(46, separator);
        inv.setItem(47, separator);
        inv.setItem(48, separator);
        inv.setItem(49, separator);
        inv.setItem(50, separator);
        inv.setItem(52, separator);

        //Lower section vault/gui stuff:
        inv.setItem(45, backButton);
        inv.setItem(51, purgePlayerButton);
        inv.setItem(53, absolvePlayerButton);

        player.openInventory(inv);
    }

    public int GetPage(String player) { return viewers.get(player).page; } //Returns page player is on
    public String GetInspectedXrayer(String player) { return viewers.get(player).xrayerInvUUID; } //Returns the uuid of the inventory player is inspecting (if any)

    public void RemoveXrayerUUIDFromList(String uuid){ xrayerUUIDs.remove(uuid); }
    public void RemovePlayerAsViewer(String name) { viewers.remove(name); }

}
