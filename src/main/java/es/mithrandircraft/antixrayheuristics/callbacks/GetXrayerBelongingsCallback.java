package es.mithrandircraft.antixrayheuristics.callbacks;

import org.bukkit.inventory.ItemStack;

public interface GetXrayerBelongingsCallback {
    void onQueryDone(ItemStack[] belongings);
}