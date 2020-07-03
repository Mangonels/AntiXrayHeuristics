//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import es.mithrandircraft.antixrayheuristics.commands.AXH;

import es.mithrandircraft.antixrayheuristics.commands.AXHAutoCompleter;
import es.mithrandircraft.antixrayheuristics.events.*;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import es.mithrandircraft.antixrayheuristics.gui.XrayerVault;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public final class AntiXrayHeuristics extends JavaPlugin implements Listener {

    //Captured spigot version:
    SpigotVersion spigotVersion;

    //Mining sessions HashMap <Name, MiningSession>:
    public HashMap<String, MiningSession> sessions = new HashMap<String, MiningSession>();

    //Persistent memory storage manager:
    public MemoryManager mm = new MemoryManager(this);

    //Hardcoded heuristics:

    private final float suspicionLevelThreshold = 100f; //Suspicion Threshold value above which we consider a player as Xraying.

    private final int MainRunnableFrequency = 200; //(ticks)15s - Time in ticks at which suspicion decrease runnable is executed.

    private final float suspicionDecreaseAmount = -4f; //Suspicion decrease from all sessions every time suspicionDecreaseFrequency is reached.

    private final int suspicionStreakZeroThreshold = 20; //Ammount of consecutive times after which a player is considered as no longer mining.

    //Precalculated heuristics:

    private int nonOreStreakDecreaseAmount; //Mined blocks streak decrease from all sessions every time suspicionDecreaseFrequency is reached.

    private int usualEncounterThreshold; //Threshold of mined non-ore blocks after which we consider the player is definetly mining legit

    private float extraDiamondWeight; //A higher weight value applied to MiningSessions on diamond encounter if suspicion is higher than usual
    private float extraEmeraldWeight; //A higher weight value applied to MiningSessions on emerald encounter if suspicion is higher than usual
    private float extraAncientDebrisWeight; //A higher weight value applied to MiningSessions on ancient debris encounter if suspicion is higher than usual

    //GUI:
    public XrayerVault vault;

    @Override
    public void onEnable() {

        //Spigot version capture:
        spigotVersion = new SpigotVersion();

        //Config load:
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        //Locale load:
        LocaleManager.setup(getName());
        LocaleManager.get().options().copyDefaults(true);
        LocaleManager.save();

        //Vault GUI object initialize:
        vault = new XrayerVault(this);

        //Commands:
        getCommand("AXH").setExecutor(new AXH(this));
        //Tab completer:
        getCommand("AXH").setTabCompleter(new AXHAutoCompleter());

        //Sql connection?:
        if(getConfig().getString("StorageType").equals("MYSQL"))
        {
            mm.InitializeDataSource();
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                mm.SQLCreateTableIfNotExists();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        //Create json file if not exists?:
        else if(getConfig().getString("StorageType").equals("JSON"))
        {
            if(mm.JSONFileCreateIfNotExists()) System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("JSONFileCreated")));
        }

        //Event registring:
        getServer().getPluginManager().registerEvents(new BlockBreakEv(this), this);
        getServer().getPluginManager().registerEvents(new ClickEv(this), this);
        getServer().getPluginManager().registerEvents(new ItemDragEv(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseEv(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldEv(this), this);

        //Runnables:
        MainRunnable();

        //Precalculations:
        nonOreStreakDecreaseAmount = -((int)Math.ceil((float)getConfig().getInt("MinimumBlocksMinedToNextVein") / 4f)); //Calculates bock streak reduction ammount on Runnable

        usualEncounterThreshold = getConfig().getInt("MinimumBlocksMinedToNextVein") * 4; //Calculates how many blocks till we should find diamond and/or emerald average

        extraDiamondWeight = getConfig().getLong("DiamondWeight") + (getConfig().getLong("DiamondWeight") / 2);
        extraEmeraldWeight = getConfig().getLong("EmeraldWeight") + (getConfig().getLong("EmeraldWeight") / 2);
        extraAncientDebrisWeight = getConfig().getLong("AncientDebrisWeight") + (getConfig().getLong("AncientDebrisWeight") / 2);
    }

    @Override
    public void onDisable()
    {
        if(getConfig().getString("StorageType").equals("MYSQL")) mm.CloseDataSource();
    }

    //Performs plugin updates at scheduled time
    private void MainRunnable()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //Task: sessions HashMap update, Player suspicion decrease:
                Set sessionsKeySet = sessions.keySet();
                Iterator sessionsIterator = sessionsKeySet.iterator();
                while (sessionsIterator.hasNext()) {
                    String key = (String) sessionsIterator.next();
                    //Reductions:
                    sessions.get(key).AddSuspicionLevel(suspicionDecreaseAmount); //Less suspicion
                    sessions.get(key).minedNonOreBlocksStreak += nonOreStreakDecreaseAmount; //Less streak

                    //Clamps:
                    if(sessions.get(key).GetSuspicionLevel() < 0)
                    {
                        sessions.get(key).SetSuspicionLevel(0); //Suspicion min 0
                        sessions.get(key).foundAtZeroSuspicionStreak++;
                        if(sessions.get(key).foundAtZeroSuspicionStreak >= suspicionStreakZeroThreshold) sessions.remove(sessions.get(key)); //Remove MiningSession for inactivity
                    }
                    else sessions.get(key).foundAtZeroSuspicionStreak = 0; //Reset streak
                    if(sessions.get(key).minedNonOreBlocksStreak < 0) sessions.get(key).minedNonOreBlocksStreak = 0; //Non ore mined blocks streak min 0
                }
            }
        }.runTaskTimer(this, MainRunnableFrequency, MainRunnableFrequency);
    }

    //Trail algorithm updater
    private void UpdateTrail(BlockBreakEvent ev, MiningSession s)
    {
        if(s.GetLastBlockCoordsStoreCounter() == 3) //Every 4 mined blocks
        {
            s.SetMinedBlocksTrailArrayPos(s.GetNextCoordsStorePos(), ev.getBlock().getLocation()); //Store player block destruction coordinates in MiningSession IntVector3 Array
        }

        s.CycleBlockCoordsStoreCounter();
        s.CycleNextCoordsStorePos();
    }
    //Trail algorithm analysis
    private float GetWeightFromAnalyzingTrail(BlockBreakEvent ev, MiningSession s, float mineralWeight)
    {
        int unalignedMinedBlocksTimesDetected = 0; //Keeps track of how many times a block was detected as outside relative mined ore block height and or X || Z tunnel axises.
        int iteratedBlockCoordSlots = 0; //Keeps track of how many stored blocks we've iterated that weren't null. This is useful for pondering weights according to distance.

        for (int i = 0; i < 10; i++)
        {
            if(s.GetMinedBlocksTrailArrayPos(i) != null) //Check for a possible empty traced block slot, if so skip, else analise:
            {
                //Z, X, Y check: Check if the block coordinates we're iterating are outside "3x3 horizontal Z and X axis tunnels" from mined ore. (You can imagine this as a cross with mined ore in center)
                //Relative altitude check:
                if (s.GetMinedBlocksTrailArrayPos(i).GetY() < ev.getBlock().getLocation().getY() - 1 || s.GetMinedBlocksTrailArrayPos(i).GetY() > ev.getBlock().getLocation().getY() + 1)
                {
                    //Mined block is outside Y axis width
                    unalignedMinedBlocksTimesDetected++; //If trailed block wasn't in an axis, we'll add an unalignment point.
                }
                //Relative X axis separation check:
                if (s.GetMinedBlocksTrailArrayPos(i).GetZ() < ev.getBlock().getLocation().getZ() - 1 || s.GetMinedBlocksTrailArrayPos(i).GetZ() > ev.getBlock().getLocation().getZ() + 1)
                {
                    //Relative Z axis separation check:
                    if(s.GetMinedBlocksTrailArrayPos(i).GetX() < ev.getBlock().getLocation().getX() - 1 || s.GetMinedBlocksTrailArrayPos(i).GetX() > ev.getBlock().getLocation().getX() + 1)
                    {
                        //Mined block is ALSO outside X axis width
                        unalignedMinedBlocksTimesDetected++; //If trailed block wasn't in an axis, we'll add an unalignment point.
                    }
                }

                iteratedBlockCoordSlots++; //Slot had IntVector3 content, and we did two separate axis checks on it. Iteration complete.
            }
        }

        //Check how many unalignedMinedBlocksTimesDetected we encountered. Apply extra weight for mined ore vein.
        float fractionReducerValue = iteratedBlockCoordSlots - unalignedMinedBlocksTimesDetected / 2; //This value will reduce the additional OreWeight applied

        //If enough unaligned coordinates are detected (more than half the axises checked), assign smaller reduction value.
        if ( unalignedMinedBlocksTimesDetected / 2 > iteratedBlockCoordSlots / 2 ) fractionReducerValue = fractionReducerValue / 3;

        if (fractionReducerValue < 1) fractionReducerValue = 1; //Min clamp to 1.

        //Reset all array positions to null:
        s.ResetBlocksTrailArray();

        return mineralWeight + (mineralWeight / fractionReducerValue); //Return final weight based on analysis
    }

    private boolean CheckGoldBiome(BlockBreakEvent ev) //Returns true if biome has incremented chances of gold
    {
        if(ev.getPlayer().getLocation().getBlock().getBiome() == Biome.BADLANDS
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.BADLANDS_PLATEAU
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.ERODED_BADLANDS
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.MODIFIED_BADLANDS_PLATEAU
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.MODIFIED_WOODED_BADLANDS_PLATEAU
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.WOODED_BADLANDS_PLATEAU)
        {
            return true;
        } else return false;
    }
    private boolean CheckEmeraldBiome(BlockBreakEvent ev) //Returns true if biome has incremented chances of emerald
    {
        if(ev.getPlayer().getLocation().getBlock().getBiome() == Biome.GRAVELLY_MOUNTAINS
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.MODIFIED_GRAVELLY_MOUNTAINS
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.MOUNTAINS
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.MOUNTAIN_EDGE
                || ev.getPlayer().getLocation().getBlock().getBiome() == Biome.WOODED_MOUNTAINS)
        {
            return true;
        } else return false;
    }

    //Attempts at updating the mining session for a player who broke a block, with just a few arguments. If this fails, the function returns false, else returns true
    private boolean UpdateMiningSession(BlockBreakEvent ev, Material m)
    {
        MiningSession s = sessions.get(ev.getPlayer().getName());
        if (s == null) return false; //Return update unsuccessful
        else {
            //MiningSession PROPERTY UPDATES:

            //Relevant non-ores mining triggers:
            if (m == Material.STONE) //This one's right on top of the state machine because it's very common
            {
                s.minedNonOreBlocksStreak++;
                UpdateTrail(ev, s); //We mined a non-ore, so we update our trail
            }
            //Relevant ores mining triggers:
            else if (m == Material.COAL_ORE) {
                //Check that it's not the same block ore material as the last mined block's. If it is, it will execute "||" statement which will verify the distance from last same mined block material to new mined block is not less than configured vein size:
                if(s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    //Check if enough non-ore blocks have been previously mined in order to account for this ore (exposed ores fp prevention):
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        //We got to an ore over threshold, so we analyze our non-ores mined trail and get weight based on that:
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("CoalWeight")));
                        s.minedNonOreBlocksStreak = 0; //Resets previously mined blocks counter
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.REDSTONE_ORE) {
                if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("RedstoneWeight")));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.IRON_ORE) {
                if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("IronWeight")));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.GOLD_ORE) {
                if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        //Weight according to biome frequency:
                        if(CheckGoldBiome(ev)) s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("GoldWeight")) / getConfig().getLong("FinalGoldWeightDivisionReducer") );
                        else s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("GoldWeight")));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.LAPIS_ORE) {
                if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("LapisWeight")));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.DIAMOND_ORE) {
                if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        if (s.minedNonOreBlocksStreak > usualEncounterThreshold)
                            s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("DiamondWeight"))); //Updates suspicion level normally.
                        else s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, extraDiamondWeight)); //Updates suspicion level with extra suspicion since the ore was quite close to last mined ore.

                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.EMERALD_ORE) {
                if(s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        if(s.minedNonOreBlocksStreak > usualEncounterThreshold) {
                            //Weight according to biome frequency:
                            if (CheckEmeraldBiome(ev)) s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("EmeraldWeight")) / getConfig().getLong("FinalEmeraldWeightDivisionReducer"));
                            else s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("EmeraldWeight")));
                        }
                        else {
                            //Weight according to biome frequency:
                            if (CheckEmeraldBiome(ev)) s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, extraEmeraldWeight) / getConfig().getLong("FinalEmeraldWeightDivisionReducer"));
                            else s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, extraEmeraldWeight));
                        }

                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.NETHER_QUARTZ_ORE) {
                if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("QuartzWeight")));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());

            } else if(spigotVersion.version.GetValue() >= 116) { //Spigot for MC 1.16+

                if (m == Material.NETHER_GOLD_ORE) {
                    if(s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                        if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                            s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("NetherGoldWeight")));
                            s.minedNonOreBlocksStreak = 0;
                        }
                    s.SetLastMinedOreData(m, ev.getBlock().getLocation());
                } else if (m == Material.ANCIENT_DEBRIS) {
                    if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                        if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                            if (s.minedNonOreBlocksStreak > usualEncounterThreshold)
                                s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, getConfig().getLong("AncientDebrisWeight"))); //Updates suspicion level normally.
                            else s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, extraAncientDebrisWeight)); //Updates suspicion level with extra suspicion since the ore was quite close to last mined ore.

                            s.minedNonOreBlocksStreak = 0;
                        }
                    s.SetLastMinedOreData(m, ev.getBlock().getLocation());
                } else {
                    //Any other block during mining session
                    s.minedNonOreBlocksStreak++;
                    UpdateTrail(ev, s); //We mined a non-ore, so we update our trail
                }
            } else {
                //Any other block during mining session
                s.minedNonOreBlocksStreak++;
                UpdateTrail(ev, s); //We mined a non-ore, so we update our trail
            }
        }

            //Property clamping:
            if (s.GetSuspicionLevel() < 0f) s.SetSuspicionLevel(0f);

            //Behaviour analysis and handling:
            if(s.GetSuspicionLevel() > suspicionLevelThreshold)
            {
                XrayerHandler.HandleXrayer(ev.getPlayer().getName());
            }

        return true; //Return update successful
    }

    //Returns block if relevant, returns Material.AIR if irrelevant
    private Material RelevantBlockCheck(BlockBreakEvent e)
    {
        if (e.getBlock().getType() == Material.STONE)
            return Material.STONE;
        else if (e.getBlock().getType() == Material.NETHERRACK && getConfig().getLong("QuartzWeight") != 0f)
            return Material.NETHERRACK;
        else if (e.getBlock().getType() == Material.COAL_ORE && getConfig().getLong("CoalWeight") != 0f)
            return Material.COAL_ORE;
        else if (e.getBlock().getType() == Material.REDSTONE_ORE && getConfig().getLong("RedstoneWeight") != 0f)
            return Material.REDSTONE_ORE;
        else if (e.getBlock().getType() == Material.IRON_ORE && getConfig().getLong("IronWeight") != 0f)
            return Material.IRON_ORE;
        else if (e.getBlock().getType() == Material.GOLD_ORE && getConfig().getLong("GoldWeight") != 0f)
            return Material.GOLD_ORE;
        else if (e.getBlock().getType() == Material.LAPIS_ORE && getConfig().getLong("LapisWeight") != 0f)
            return Material.LAPIS_ORE;
        else if (e.getBlock().getType() == Material.DIAMOND_ORE && getConfig().getLong("DiamondWeight") != 0f)
            return Material.DIAMOND_ORE;
        else if (e.getBlock().getType() == Material.EMERALD_ORE && getConfig().getLong("EmeraldWeight") != 0f)
            return Material.EMERALD_ORE;
        else if (e.getBlock().getType() == Material.NETHER_QUARTZ_ORE && getConfig().getLong("QuartzWeight") != 0f)
            return Material.NETHER_QUARTZ_ORE;

        else if(spigotVersion.version.GetValue() >= 116) //Spigot for MC 1.16+
        {
            if (e.getBlock().getType() == Material.NETHER_GOLD_ORE && getConfig().getLong("NetherGoldWeight") != 0f)
                return Material.NETHER_GOLD_ORE;
            else if (e.getBlock().getType() == Material.ANCIENT_DEBRIS && getConfig().getLong("AncientDebrisWeight") != 0f)
                return Material.ANCIENT_DEBRIS;

            else return Material.AIR;
        }
        else return Material.AIR;
    }

    //Inspects the blockbreak event further for actions
    public void BBEventAnalyzer(BlockBreakEvent ev)
    {
        if (!ev.getPlayer().hasPermission("AXH.Ignore")) {
            //Check if the block is relevant:
            Material m = RelevantBlockCheck(ev);
            if (m != Material.AIR) { //Attempt at updating player mining session:
                if (!UpdateMiningSession(ev, m)) { //Let's asume the player doesn't have a MiningSession entry. Then is the block consequently a first stone or first netherrack?
                    if (m == Material.STONE) {
                        sessions.put(ev.getPlayer().getName(), new MiningSession()); //Adds new entry to sessions HashMap for player
                    }
                    else if (m == Material.NETHERRACK) {
                        sessions.put(ev.getPlayer().getName(), new MiningSession()); //Adds new entry to sessions HashMap for player
                    }
                }
            }
        }
    }
}
