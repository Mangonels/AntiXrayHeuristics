package es.mithrandircraft.antixrayheuristics;

import es.mithrandircraft.antixrayheuristics.commands.AXH;
import es.mithrandircraft.antixrayheuristics.commands.ResetXraySuspicion;
import es.mithrandircraft.antixrayheuristics.commands.XraySuspicion;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public final class AntiXrayHeuristics extends JavaPlugin implements Listener {

    //Mining sessions HashMap:
    public static HashMap<String, MiningSession> sessions = new HashMap<String, MiningSession>();

    //Hardcoded heuristics:

    private final float suspicionLevelThreshold = 100f; //Suspicion Threshold value above which we consider a player as Xraying.

    private final int suspicionDecreaseFrequency = 200; //(ticks)15s - Time in ticks at which suspicion decrease runnable is executed.

    private final float suspicionDecreaseAmount = -4f; //Suspicion decrease from all sessions every time suspicionDecreaseFrequency is reached.

    private final int suspicionStreakZeroThreshold = 20; //Ammount of consecutive times after which a player is considered as no longer mining.

    //Precalculated heuristics:

    private int nonOreStreakDecreaseAmount; //Mined blocks streak decrease from all sessions every time suspicionDecreaseFrequency is reached.

    private int usualEncounterThreshold; //Threshold of mined non-ore blocks after which we consider the player is definetly mining legit

    private float extraDiamondWeight; //A higher weight value applied to MiningSessions on diamond encounter if suspicion is higher than usual
    private float extraEmeraldWeight; //A higher weight value applied to MiningSessions on emerald encounter if suspicion is higher than usual

//    private final int specificOreFocusThreshold = 20; //Threshold which, if exceeded, determines if a player is going only for a certain kind of ore
//
//    private final int descentMiningStreakThreshold = 5; //Threshold that determines when it should be considered suspicious that a player mined straight down
//
//    private final int diagonalDescentThreshold = 5; //Threshold that determines when it should be considered suspicious that a player mined diagonally
//
//    private final int straightMiningThreshold = 5; //Threshold that determines when we can determine that the player is mining straight

    @Override
    public void onEnable() {

        //Config load:
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        //Commands:
        getCommand("AXH").setExecutor(new AXH(this));
        getCommand("ResetXraySuspicion").setExecutor(new ResetXraySuspicion());
        getCommand("XraySuspicion").setExecutor(new XraySuspicion());

        //Events:
        getServer().getPluginManager().registerEvents(this, this);

        //Runnables:
        SessionsRunnable();

        //Precalculations:
        nonOreStreakDecreaseAmount = -((int)Math.ceil((float)getConfig().getInt("MinimumBlocksMinedToNextVein") / 4f)); //Calculates bock streak reduction ammount on Runnable

        usualEncounterThreshold = getConfig().getInt("MinimumBlocksMinedToNextVein") * 4; //Calculates how many blocks till we should find diamond and/or emerald average

        extraDiamondWeight = getConfig().getLong("DiamondWeight") + (getConfig().getLong("DiamondWeight") / 2);
        extraEmeraldWeight = getConfig().getLong("EmeraldWeight") + (getConfig().getLong("EmeraldWeight") / 2);
    }

    @Override
    public void onDisable()
    {

    }

    public void SessionsRunnable() //Performs sessions HashMap updates at scheduled time
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {

                for (HashMap.Entry<String, MiningSession> entry : sessions.entrySet()) {
                    //Reductions:
                    entry.getValue().AddSuspicionLevel(suspicionDecreaseAmount); //Less suspicion
                    entry.getValue().minedNonOreBlocksStreak += nonOreStreakDecreaseAmount; //Less streak
                    //Clamps:
                    if(entry.getValue().GetSuspicionLevel() < 0)
                    {
                        entry.getValue().SetSuspicionLevel(0); //Suspicion min 0
                        entry.getValue().foundAtZeroSuspicionStreak++;
                        if(entry.getValue().foundAtZeroSuspicionStreak >= suspicionStreakZeroThreshold) sessions.remove(entry.getKey()); //Remove MiningSession for inactivity
                    }
                    else entry.getValue().foundAtZeroSuspicionStreak = 0; //Reset streak
                    if(entry.getValue().minedNonOreBlocksStreak < 0) entry.getValue().minedNonOreBlocksStreak = 0; //Non ore mined blocks streak min 0
                }
            }
        }.runTaskTimer(this, suspicionDecreaseFrequency, suspicionDecreaseFrequency);
    }

    private void HandleXrayer(String name) //If called, executes what must be done to an inputted Xrayer by name.
    {
        Player player = getServer().getPlayer(name); //Reference to player
        if(player != null)
        {
            for (int i = 0; i < getConfig().getStringList("CommandsExecuted").size(); i++) { //Executes commands if configured:
                getServer().dispatchCommand(getServer().getConsoleSender(), PlaceholderManager.SubstitutePlaceholders(getConfig().getStringList("CommandsExecuted").get(i), name));
            }

            if (getConfig().getBoolean("SendMessageToPlayer")) { //Sends message to player if configured:
                String m = getConfig().getString("MessageToSend");
                if (m != null) player.sendMessage(m);
            }

            if (getConfig().getBoolean("ClensePlayerItems")) { //Removes all of the player's belongings if configured:
                try{player.getInventory().clear();
                    player.getEquipment().clear(); } catch(Exception e){ if(getConfig().getBoolean("Debug")) System.out.println("Failed to remove player " + name + "'s equipment while attempting to handle as Xrayer."); }
            }

            if (getConfig().getBoolean("NullifySuspicionAferPunish")) {
                sessions.remove(player.getName());
            }
        }
        else{ System.out.println("Player named " + name + " was not found while attempting to handle as Xrayer."); }
    }

    private boolean UpdateMiningSession(BlockBreakEvent ev, Material m) //Attempts at updating the mining session for a player who broke a block, with just a few arguments. If this fails, the function returns false, else returns true
    {
        MiningSession s = sessions.get(ev.getPlayer().getName());
        if (s == null) return false; //Return update unsuccessful
        else {
            //MiningSession PROPERTY UPDATES:

            //Relevant non-ores mining triggers:
            if (m == Material.STONE)
            {
                s.minedNonOreBlocksStreak++;
            }
            //Relevant ores mining triggers:
            else if (m == Material.COAL_ORE) {
                //Check that it's not the same block ore material as the last mined block's. If it is, it will execute "||" statement which will verify the distance from last same mined block material to new mined block is not less than configured vein size:
                if(s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    //Check if enough non-ore blocks have been previously mined in order to account for this ore (exposed ores fp prevention):
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(getConfig().getLong("CoalWeight"));
                        s.minedNonOreBlocksStreak = 0; //Resets previously mined blocks counter
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.REDSTONE_ORE) {
                if (s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(getConfig().getLong("RedstoneWeight"));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.IRON_ORE) {
                if (s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(getConfig().getLong("IronWeight"));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.GOLD_ORE) {
                if (s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(getConfig().getLong("GoldWeight"));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.NETHER_QUARTZ_ORE) {
                if (s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        s.AddSuspicionLevel(getConfig().getLong("QuartzWeight"));
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.LAPIS_ORE) {
                    if (s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                        if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                            s.AddSuspicionLevel(getConfig().getLong("LapisWeight"));
                            s.minedNonOreBlocksStreak = 0;
                        }
                    s.lastMinedOre = m;
                    s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.DIAMOND_ORE) {
                if (s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        if (s.minedNonOreBlocksStreak > usualEncounterThreshold)
                            s.AddSuspicionLevel(getConfig().getLong("DiamondWeight"));  //Updates suspicion level normally.
                        else
                            s.AddSuspicionLevel(extraDiamondWeight); //Updates suspicion level with extra suspicion since the ore was quite close to last mined ore.
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();
            } else if (m == Material.EMERALD_ORE) {
                if(s.lastMinedOre != m || s.lastMinedOreLocation.distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                    if(s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                        if(s.minedNonOreBlocksStreak > usualEncounterThreshold)
                            s.AddSuspicionLevel(getConfig().getLong("EmeraldWeight"));
                        else s.AddSuspicionLevel(extraEmeraldWeight);
                        s.minedNonOreBlocksStreak = 0;
                    }
                s.lastMinedOre = m;
                s.lastMinedOreLocation = ev.getBlock().getLocation();

            } else {
                s.minedNonOreBlocksStreak++;
            }
        }

            //Property clamping:
            if (s.GetSuspicionLevel() < 0f) s.SetSuspicionLevel(0f);

            //Behaviour analysis and handling:
            if(s.GetSuspicionLevel() > suspicionLevelThreshold)
            {
                HandleXrayer(ev.getPlayer().getName());
            }

        return true; //Return update successful
    }

    private Material RelevantBlockCheck(BlockBreakEvent e) //Returns block if relevant, returns Material.AIR if irrelevant
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
        else if (e.getBlock().getType() == Material.NETHER_QUARTZ_ORE && getConfig().getLong("QuartzWeight") != 0f)
            return Material.NETHER_QUARTZ_ORE;
        else if (e.getBlock().getType() == Material.LAPIS_ORE && getConfig().getLong("LapisWeight") != 0f)
            return Material.LAPIS_ORE;
        else if (e.getBlock().getType() == Material.DIAMOND_ORE && getConfig().getLong("DiamondWeight") != 0f)
            return Material.DIAMOND_ORE;
        else if (e.getBlock().getType() == Material.EMERALD_ORE && getConfig().getLong("EmeraldWeight") != 0f)
            return Material.EMERALD_ORE;

        else return Material.AIR;
    }

    private void BBEventAnalyzer(BlockBreakEvent ev) //Inspects the blockbreak event further for actions
    {
        if (!ev.getPlayer().hasPermission("AXH.ignore")) {
            //Check if the block is relevant:
            Material m = RelevantBlockCheck(ev);
            if (m != Material.AIR) { //Attempt at updating player mining session:
                if (!UpdateMiningSession(ev, m)) { //Then is the block consequently a first stone or first netherrack (while netherquartz isn't disabled in config)?
                    if (m == Material.STONE) {
                        sessions.put(ev.getPlayer().getName(), new MiningSession()); //Adds new entry to sessions HashMap for player
                    }
                    else if (m == Material.NETHERRACK && getConfig().getInt("QuartzWeight") != 0f) {
                        sessions.put(ev.getPlayer().getName(), new MiningSession()); //Adds new entry to sessions HashMap for player
                    }
                }
            }
        }
    }

    @EventHandler
    public void OnPlayerMineBlock(BlockBreakEvent ev) {
        //Check if the event occurred in one of the configured worlds:
        for (int i = 0; i < getConfig().getStringList("TrackWorlds").size(); i++) {
            if (getConfig().getStringList("TrackWorlds").get(i).equals(ev.getBlock().getWorld().getName()) && ev.getBlock().getLocation().getY() < getConfig().getInt("IgnoreHigherThanAltitude")) //It's one of the whitelisted "TrackWorlds", and altitude is lower than configured.
            {
                BBEventAnalyzer(ev);
                break;
            }
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent ev) {

    }
}
