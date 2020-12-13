//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

class MadeUpInventory implements Inventory {
    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setMaxStackSize(int size) {

    }

    @Override
    public ItemStack getItem(int index) {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setItem(int index, ItemStack item) {

    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @Override
    public ItemStack[] getContents() {
        return new ItemStack[0];
    }

    @Override
    public void setContents(ItemStack[] items) throws IllegalArgumentException {

    }

    @Override
    public ItemStack[] getStorageContents() {
        return new ItemStack[0];
    }

    @Override
    public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {

    }

    @Override
    public boolean contains(Material material) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack item) {
        return false;
    }

    @Override
    public boolean contains(Material material, int amount) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        return false;
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
        return null;
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
        return null;
    }

    @Override
    public int first(Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int first(ItemStack item) {
        return 0;
    }

    @Override
    public int firstEmpty() {
        return 0;
    }

    @Override
    public void remove(Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(ItemStack item) {

    }

    @Override
    public void clear(int index) {

    }

    @Override
    public void clear() {

    }

    @Override
    public List<HumanEntity> getViewers() {
        return null;
    }

    @Override
    public InventoryType getType() {
        return null;
    }

    @Override
    public InventoryHolder getHolder() {
        return null;
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        return null;
    }

    @Override
    public ListIterator<ItemStack> iterator(int index) {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }
}
