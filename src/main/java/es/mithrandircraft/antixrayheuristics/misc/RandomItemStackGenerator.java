//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.misc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RandomItemStackGenerator {
    public static ItemStack GetRandomItemStack()
    {
        Random random = new Random();
        int material = random.nextInt(Material.values().length); //Random material
        int amount = Material.values()[material].getMaxStackSize(); //Max stack size

        return new ItemStack(Material.values()[material], amount);
    }
}