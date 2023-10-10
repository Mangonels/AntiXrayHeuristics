package es.mithrandircraft.antixrayheuristics;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class AntiXrayHeuristics extends JavaPlugin implements Listener {
    private static AntiXrayHeuristics plugin;

    private APIAntiXrayHeuristics api;

    protected SpigotVersion spigotVersion;

    protected HashMap<String, MiningSession> sessions = new HashMap<>();

    protected MemoryManager mm = new MemoryManager(this);

    private final float suspicionLevelThreshold = 100.0F;

    private final int mainRunnableFrequency = 200;

    protected final float maxSuspicionDecreaseProportion = -10.0F;

    protected final float minSuspicionDecreaseProportion = -0.1F;

    protected final float absoluteMinimumSuspicionDecrease = -3.0F;

    protected final int maxAccountableMillisecondDeltaForThirtyMinedBlocks = 20000;

    protected final int minAccountableMillisecondDeltaForThirtyMinedBlocks = 0;

    private final int suspicionStreakZeroThreshold = 20;

    private int nonOreStreakDecreaseAmount;

    private int usualEncounterThreshold;

    private float extraDiamondWeight;

    private float extraEmeraldWeight;

    private float extraAncientDebrisWeight;

    protected XrayerVault vault;

    public static AntiXrayHeuristics GetPlugin() {
        return plugin;
    }

    public APIAntiXrayHeuristics GetAPI() {
        return this.api;
    }

    public void onEnable() {
        plugin = this;
        this.api = new APIAntiXrayHeuristicsImpl(this);
        this.spigotVersion = new SpigotVersion();
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', "&5[&bAntiXrayHeuristics&5] &aHas enabled successfully"));
        ConfigurationSerialization.registerClass(BlockWeightInfo.class);
        LocaleManager.setup(getName());
        LocaleManager.get().options().copyDefaults(true);
        LocaleManager.save();
        WeightsCard.setup(getName());
        WeightsCard.get().options().copyDefaults(true);
        WeightsCard.save();
        this.vault = new XrayerVault(this);
        getCommand("AXH").setExecutor(new CommandAXH(this));
        getCommand("AXH").setTabCompleter(new CommandAXHAutoCompleter());
        if (getConfig().getString("StorageType").equals("MYSQL")) {
            this.mm.InitializeDataSource();
            Bukkit.getScheduler().runTaskAsynchronously((Plugin)this, () -> {
                try {
                    this.mm.SQLCreateTableIfNotExists();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else if (getConfig().getString("StorageType").equals("JSON")) {
            this.mm.JSONFileCreateIfNotExists();
        }
        getServer().getPluginManager().registerEvents(new EventBlockBreak(this), (Plugin)this);
        getServer().getPluginManager().registerEvents(new EventBlockPlace(this), (Plugin)this);
        getServer().getPluginManager().registerEvents(new EventClick(this), (Plugin)this);
        getServer().getPluginManager().registerEvents(new EventItemDrag(), (Plugin)this);
        getServer().getPluginManager().registerEvents(new EventInventoryClose(this), (Plugin)this);
        getServer().getPluginManager().registerEvents(new EventPlayerChangedWorld(this), (Plugin)this);
        MainRunnable();
        this.nonOreStreakDecreaseAmount = -((int)Math.ceil((getConfig().getInt("MinimumBlocksMinedToNextVein") / 4.0F)));
        this.usualEncounterThreshold = getConfig().getInt("MinimumBlocksMinedToNextVein") * 4;
        this.extraDiamondWeight = (float)getConfig().getLong("DiamondWeight") * 1.5F;
        this.extraEmeraldWeight = (float)getConfig().getLong("EmeraldWeight") * 1.5F;
        this.extraAncientDebrisWeight = (float)getConfig().getLong("AncientDebrisWeight") * 1.5F;
    }

    public void onDisable() {
        if (getConfig().getString("StorageType").equals("MYSQL"))
            this.mm.CloseDataSource();
    }

    private void MainRunnable() {
        (new BukkitRunnable() {
            public void run() {
                Set<String> sessionsKeySet = AntiXrayHeuristics.this.sessions.keySet();
                Iterator<String> sessionsIterator = sessionsKeySet.iterator();
                while (sessionsIterator.hasNext()) {
                    String key = sessionsIterator.next();
                    ((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).SelfSuspicionReducer();
                    ((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).minedNonOreBlocksStreak += AntiXrayHeuristics.this.nonOreStreakDecreaseAmount;
                    if (((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).GetSuspicionLevel() < 0.0F) {
                        ((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).SetSuspicionLevel(0.0F);
                        ((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).foundAtZeroSuspicionStreak++;
                        if (((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).foundAtZeroSuspicionStreak >= 20)
                            AntiXrayHeuristics.this.sessions.remove(AntiXrayHeuristics.this.sessions.get(key));
                    } else {
                        ((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).foundAtZeroSuspicionStreak = 0;
                    }
                    if (((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).minedNonOreBlocksStreak < 0)
                        ((MiningSession)AntiXrayHeuristics.this.sessions.get(key)).minedNonOreBlocksStreak = 0;
                }
            }
        }).runTaskTimer((Plugin)this, 200L, 200L);
    }

    private void UpdateTrail(BlockBreakEvent ev, MiningSession s) {
        if (s.GetLastBlockCoordsStoreCounter() == 3)
            s.SetMinedBlocksTrailArrayPos(s.GetNextCoordsStorePos(), ev.getBlock().getLocation());
        s.CycleBlockCoordsStoreCounter();
        s.CycleNextCoordsStorePos();
    }

    private float GetWeightFromAnalyzingTrail(BlockBreakEvent ev, MiningSession s, float mineralWeight) {
        int unalignedMinedBlocksTimesDetected = 0;
        int iteratedBlockCoordSlots = 0;
        for (int i = 0; i < 10; i++) {
            if (s.GetMinedBlocksTrailArrayPos(i) != null) {
                if (s.GetMinedBlocksTrailArrayPos(i).GetY() < ev.getBlock().getLocation().getY() - 2.0D || s.GetMinedBlocksTrailArrayPos(i).GetY() > ev.getBlock().getLocation().getY() + 2.0D)
                    unalignedMinedBlocksTimesDetected++;
                if (s.GetMinedBlocksTrailArrayPos(i).GetZ() < ev.getBlock().getLocation().getZ() - 2.0D || s.GetMinedBlocksTrailArrayPos(i).GetZ() > ev.getBlock().getLocation().getZ() + 2.0D)
                    if (s.GetMinedBlocksTrailArrayPos(i).GetX() < ev.getBlock().getLocation().getX() - 2.0D || s.GetMinedBlocksTrailArrayPos(i).GetX() > ev.getBlock().getLocation().getX() + 2.0D)
                        unalignedMinedBlocksTimesDetected++;
                iteratedBlockCoordSlots++;
            }
        }
        float fractionReducerValue = (iteratedBlockCoordSlots - unalignedMinedBlocksTimesDetected / 2);
        if (unalignedMinedBlocksTimesDetected / 2 > iteratedBlockCoordSlots / 2)
            fractionReducerValue /= 3.0F;
        if (fractionReducerValue < 1.0F)
            fractionReducerValue = 1.0F;
        s.ResetBlocksTrailArray();
        return mineralWeight + mineralWeight / fractionReducerValue;
    }

    private boolean CheckGoldBiome(BlockBreakEvent ev) {
        if (ev.getPlayer().getLocation().getBlock().getBiome() == Biome.BADLANDS || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.BADLANDS_PLATEAU || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.ERODED_BADLANDS || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.MODIFIED_BADLANDS_PLATEAU || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.MODIFIED_WOODED_BADLANDS_PLATEAU || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.WOODED_BADLANDS_PLATEAU)
            return true;
        return false;
    }

    private boolean CheckEmeraldBiome(BlockBreakEvent ev) {
        if (ev.getPlayer().getLocation().getBlock().getBiome() == Biome.GRAVELLY_MOUNTAINS || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.MODIFIED_GRAVELLY_MOUNTAINS || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.MOUNTAINS || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.MOUNTAIN_EDGE || ev
                .getPlayer().getLocation().getBlock().getBiome() == Biome.WOODED_MOUNTAINS)
            return true;
        return false;
    }

    private boolean UpdateMiningSession(BlockBreakEvent ev, Material m) {
        MiningSession s = this.sessions.get(ev.getPlayer().getName());
        if (s == null)
            return false;
        System.out.print(m);
        if (m == Material.STONE || m == Material.NETHERRACK || (this.spigotVersion.version
                .GetValue() >= 118 && m == Material.DEEPSLATE) || m == Material.TUFF || (this.spigotVersion.version
                .GetValue() >= 116 && m == Material.BASALT)) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            s.minedNonOreBlocksStreak++;
            UpdateTrail(ev, s);
        } else if (m == Material.COAL_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if (s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance"))
                if (s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("CoalWeight")));
                    s.minedNonOreBlocksStreak = 0;
                }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.REDSTONE_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("RedstoneWeight")));
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.IRON_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("IronWeight")));
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.GOLD_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                if (CheckGoldBiome(ev)) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("GoldWeight")) / (float)getConfig().getLong("FinalGoldWeightDivisionReducer"));
                } else {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("GoldWeight")));
                }
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.LAPIS_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("LapisWeight")));
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.DIAMOND_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                if (s.minedNonOreBlocksStreak > this.usualEncounterThreshold) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("DiamondWeight")));
                } else {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, this.extraDiamondWeight));
                }
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.EMERALD_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                if (s.minedNonOreBlocksStreak > this.usualEncounterThreshold) {
                    if (CheckEmeraldBiome(ev)) {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("EmeraldWeight")) / (float)getConfig().getLong("FinalEmeraldWeightDivisionReducer"));
                    } else {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("EmeraldWeight")));
                    }
                } else if (CheckEmeraldBiome(ev)) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, this.extraEmeraldWeight) / (float)getConfig().getLong("FinalEmeraldWeightDivisionReducer"));
                } else {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, this.extraEmeraldWeight));
                }
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.NETHER_QUARTZ_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("QuartzWeight")));
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.ANCIENT_DEBRIS) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                if (s.minedNonOreBlocksStreak > this.usualEncounterThreshold) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("AncientDebrisWeight")));
                } else {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, this.extraAncientDebrisWeight));
                }
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (m == Material.NETHER_GOLD_ORE) {
            s.UpdateTimeAccountingProperties(ev.getPlayer());
            if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                    s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("NetherGoldWeight")));
                s.minedNonOreBlocksStreak = 0;
            }
            s.SetLastMinedOreData(m, ev.getBlock().getLocation());
        } else if (this.spigotVersion.version.GetValue() >= 118) {
            if (m == Material.DEEPSLATE_DIAMOND_ORE) {
                s.UpdateTimeAccountingProperties(ev.getPlayer());
                if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                        s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                    if (s.minedNonOreBlocksStreak > this.usualEncounterThreshold) {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("DeepslateDiamond")));
                    } else {
                        s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, this.extraDiamondWeight));
                    }
                    s.minedNonOreBlocksStreak = 0;
                }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.DEEPSLATE_GOLD_ORE) {
                s.UpdateTimeAccountingProperties(ev.getPlayer());
                if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                        s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("DeepslateGold")));
                    s.minedNonOreBlocksStreak = 0;
                }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.DEEPSLATE_IRON_ORE) {
                s.UpdateTimeAccountingProperties(ev.getPlayer());
                if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                        s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("DeepslateIron")));
                    s.minedNonOreBlocksStreak = 0;
                }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.DEEPSLATE_LAPIS_ORE) {
                s.UpdateTimeAccountingProperties(ev.getPlayer());
                if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                        s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("DeepslateLapis")));
                    s.minedNonOreBlocksStreak = 0;
                }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            } else if (m == Material.DEEPSLATE_REDSTONE_ORE) {
                s.UpdateTimeAccountingProperties(ev.getPlayer());
                if ((s.GetLastMinedOre() != m || s.GetLastMinedOreLocation().distance(ev.getBlock().getLocation()) > getConfig().getInt("ConsiderAdjacentWithinDistance")) &&
                        s.minedNonOreBlocksStreak > getConfig().getInt("MinimumBlocksMinedToNextVein")) {
                    s.AddSuspicionLevel(GetWeightFromAnalyzingTrail(ev, s, (float)getConfig().getLong("DeepslateRedstone")));
                    s.minedNonOreBlocksStreak = 0;
                }
                s.SetLastMinedOreData(m, ev.getBlock().getLocation());
            }
        } else {
            s.minedNonOreBlocksStreak++;
            UpdateTrail(ev, s);
        }
        if (s.GetSuspicionLevel() < 0.0F)
            s.SetSuspicionLevel(0.0F);
        if (s.GetSuspicionLevel() > 100.0F)
            XrayerHandler.HandleXrayer(ev.getPlayer().getName());
        return true;
    }

    private Material RelevantBlockCheck(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.STONE)
            return Material.STONE;
        if (e.getBlock().getType() == Material.NETHERRACK)
            return Material.NETHERRACK;
        if (e.getBlock().getType() == Material.DEEPSLATE)
            return Material.DEEPSLATE;
        if (e.getBlock().getType() == Material.TUFF)
            return Material.TUFF;
        if (e.getBlock().getType() == Material.COAL_ORE && (float)getConfig().getLong("CoalWeight") != 0.0F)
            return Material.COAL_ORE;
        if (e.getBlock().getType() == Material.REDSTONE_ORE && (float)getConfig().getLong("RedstoneWeight") != 0.0F)
            return Material.REDSTONE_ORE;
        if (e.getBlock().getType() == Material.IRON_ORE && (float)getConfig().getLong("IronWeight") != 0.0F)
            return Material.IRON_ORE;
        if (e.getBlock().getType() == Material.GOLD_ORE && (float)getConfig().getLong("GoldWeight") != 0.0F)
            return Material.GOLD_ORE;
        if (e.getBlock().getType() == Material.LAPIS_ORE && (float)getConfig().getLong("LapisWeight") != 0.0F)
            return Material.LAPIS_ORE;
        if (e.getBlock().getType() == Material.DIAMOND_ORE && (float)getConfig().getLong("DiamondWeight") != 0.0F)
            return Material.DIAMOND_ORE;
        if (e.getBlock().getType() == Material.EMERALD_ORE && (float)getConfig().getLong("EmeraldWeight") != 0.0F)
            return Material.EMERALD_ORE;
        if (e.getBlock().getType() == Material.NETHER_QUARTZ_ORE && (float)getConfig().getLong("QuartzWeight") != 0.0F)
            return Material.NETHER_QUARTZ_ORE;
        if (e.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE && (float)getConfig().getLong("DeepslateDiamond") != 0.0F)
            return Material.DEEPSLATE_DIAMOND_ORE;
        if (e.getBlock().getType() == Material.DEEPSLATE_IRON_ORE && (float)getConfig().getLong("DeepslateIron") != 0.0F)
            return Material.DEEPSLATE_IRON_ORE;
        if (e.getBlock().getType() == Material.DEEPSLATE_GOLD_ORE && (float)getConfig().getLong("DeepslateGold") != 0.0F)
            return Material.DEEPSLATE_GOLD_ORE;
        if (e.getBlock().getType() == Material.DEEPSLATE_REDSTONE_ORE && (float)getConfig().getLong("DeepslateRedstone") != 0.0F)
            return Material.DEEPSLATE_REDSTONE_ORE;
        if (e.getBlock().getType() == Material.DEEPSLATE_LAPIS_ORE && (float)getConfig().getLong("DeepslateLapis") != 0.0F)
            return Material.DEEPSLATE_LAPIS_ORE;
        if (e.getBlock().getType() == Material.NETHER_GOLD_ORE && (float)getConfig().getLong("NetherGoldWeight") != 0.0F)
            return Material.NETHER_GOLD_ORE;
        if (e.getBlock().getType() == Material.ANCIENT_DEBRIS && (float)getConfig().getLong("AncientDebrisWeight") != 0.0F)
            return Material.ANCIENT_DEBRIS;
        if (this.spigotVersion.version.GetValue() >= 116) {
            if (e.getBlock().getType() == Material.BASALT)
                return Material.BASALT;
            return Material.AIR;
        }
        return Material.AIR;
    }

    protected void BBEventAnalyzer(BlockBreakEvent ev) {
        if (!ev.getPlayer().hasPermission("AXH.Ignore")) {
            Material m = RelevantBlockCheck(ev);
            if (m != Material.AIR &&
                    !UpdateMiningSession(ev, m))
                if (m == Material.STONE || m == Material.NETHERRACK) {
                    this.sessions.put(ev.getPlayer().getName(), new MiningSession(this));
                } else if (this.spigotVersion.version.GetValue() >= 118) {
                    if (m == Material.DEEPSLATE || m == Material.TUFF)
                        this.sessions.put(ev.getPlayer().getName(), new MiningSession(this));
                } else if (this.spigotVersion.version.GetValue() >= 116 &&
                        m == Material.BASALT) {
                    this.sessions.put(ev.getPlayer().getName(), new MiningSession(this));
                }
        }
    }
}
