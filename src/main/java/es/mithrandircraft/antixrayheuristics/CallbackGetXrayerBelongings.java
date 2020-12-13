package es.mithrandircraft.antixrayheuristics;

import org.bukkit.inventory.ItemStack;

interface CallbackGetXrayerBelongings {
    void onQueryDone(ItemStack[] belongings);
}