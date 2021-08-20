//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.IOException;
import java.util.*;

class BlockWeightInfo implements ConfigurationSerializable {
    public Material blockMaterial;
    public int blockHeight;
    public float blockWeight;

    /*Standard constructor*/
    public BlockWeightInfo(Material material, int height, float weight)
    {
        blockMaterial = material;
        blockHeight = height;
        blockWeight = weight;
    }

    /*Constructor used when deserializing*/
    public BlockWeightInfo(Map<String, Object> deserializedProperties)
    {
        blockMaterial = (Material)deserializedProperties.get("Material");
        blockHeight = (int)deserializedProperties.get("Height");
        blockWeight = (float)deserializedProperties.get("Weight");
    }

    @Override
    /*Method used when serializing*/
    public final Map<String, Object> serialize()
    {
        Map<String, Object> serializedProperties = new HashMap<>();
        serializedProperties.put("Material", blockMaterial.ordinal());
        serializedProperties.put("Height", blockHeight);
        serializedProperties.put("Weight", blockWeight);

        return serializedProperties;
    }
}

class WeightsCard {

    private static File weightsFile;
    private static FileConfiguration weightsConfiguration;

    private static void SetDefaultFileEntries() //Sets the default language entries in english
    {
        List<BlockWeightInfo> worldWeights = new ArrayList<>();
        worldWeights.add(new BlockWeightInfo(Material.COAL_ORE,5, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.COAL_ORE,65, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.REDSTONE_ORE, 5, 9.0f));
        worldWeights.add(new BlockWeightInfo(Material.REDSTONE_ORE, 15, 9.0f));
        worldWeights.add(new BlockWeightInfo(Material.IRON_ORE, 5, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.IRON_ORE, 60, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.GOLD_ORE, 5, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.GOLD_ORE, 30, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.COPPER_ORE, 0, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.COPPER_ORE, 45, 2.5f));
        worldWeights.add(new BlockWeightInfo(Material.COPPER_ORE, 70, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.DIAMOND_ORE, 5, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.DIAMOND_ORE, 5, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.EMERALD_ORE, 5, 22.0f));
        worldWeights.add(new BlockWeightInfo(Material.EMERALD_ORE, 30, 22.0f));
        worldWeights.add(new BlockWeightInfo(Material.LAPIS_ORE, 0, 8.0f));
        worldWeights.add(new BlockWeightInfo(Material.LAPIS_ORE, 15, 2.5f));
        worldWeights.add(new BlockWeightInfo(Material.LAPIS_ORE, 30, 8.0f));

        weightsConfiguration.addDefault("world", worldWeights);

        List<BlockWeightInfo> netherWeights = new ArrayList<>();
        netherWeights.add(new BlockWeightInfo(Material.NETHER_QUARTZ_ORE, 5, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.NETHER_QUARTZ_ORE, 60, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.NETHER_GOLD_ORE,0, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.NETHER_GOLD_ORE,255, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.ANCIENT_DEBRIS, 0, 22.0f));
        netherWeights.add(new BlockWeightInfo(Material.ANCIENT_DEBRIS, 255, 22.0f));

        weightsConfiguration.addDefault("world_nether", netherWeights);
    }

    public static void setup(String pluginName) //Finds or generates custom config file
    {
        weightsFile = new File(Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDataFolder(), "weights.yml");

        if(!weightsFile.exists())
        {
            try {
                weightsFile.createNewFile(); //Creates the file
            } catch(IOException e)
            {
                System.out.print("[AntiXrayHeuristics] Could not create weights file.");
            }
        }
        weightsConfiguration = YamlConfiguration.loadConfiguration(weightsFile);
        SetDefaultFileEntries(); //Sets default entries
    }

    public static FileConfiguration get()
    {
        return weightsConfiguration;
    }

    public static void save()
    {
        try {
            weightsConfiguration.save(weightsFile);
        } catch(IOException e)
        {
            System.out.print("[AntiXrayHeuristics] Could not save weights file.");
        }
    }

    public static void reload() //Used from ARGReload AXH command argument in order to reload the weights file
    {
        weightsConfiguration = YamlConfiguration.loadConfiguration(weightsFile);
    }
}






























