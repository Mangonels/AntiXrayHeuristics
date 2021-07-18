//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

class RandomItemStackGenerator {
    public static ItemStack GetRandomItemStack()
    {
        Random random = new Random();
        int material = random.nextInt(Material.values().length); //Random material
        int amount = Material.values()[material].getMaxStackSize(); //Max stack size

        return new ItemStack(Material.values()[material], amount);
    }
}