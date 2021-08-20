//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

class MadeUpEquipment implements EntityEquipment {
    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {

    }

    @Override
    public void setItem(EquipmentSlot equipmentSlot, ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public ItemStack getItemInMainHand() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setItemInMainHand(ItemStack item) {

    }

    @Override
    public void setItemInMainHand(ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack getItemInOffHand() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setItemInOffHand(ItemStack item) {

    }

    @Override
    public void setItemInOffHand(ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack getItemInHand() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setItemInHand(ItemStack stack) {

    }

    @Override
    public ItemStack getHelmet() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setHelmet(ItemStack helmet) {

    }

    @Override
    public void setHelmet(ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack getChestplate() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setChestplate(ItemStack chestplate) {

    }

    @Override
    public void setChestplate(ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack getLeggings() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setLeggings(ItemStack leggings) {

    }

    @Override
    public void setLeggings(ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack getBoots() {
        return RandomItemStackGenerator.GetRandomItemStack();
    }

    @Override
    public void setBoots(ItemStack boots) {

    }

    @Override
    public void setBoots(ItemStack itemStack, boolean b) {

    }

    @Override
    public ItemStack[] getArmorContents() {
        return new ItemStack[0];
    }

    @Override
    public void setArmorContents(ItemStack[] items) {

    }

    @Override
    public void clear() {

    }

    @Override
    public float getItemInHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInHandDropChance(float chance) {

    }

    @Override
    public float getItemInMainHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInMainHandDropChance(float chance) {

    }

    @Override
    public float getItemInOffHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInOffHandDropChance(float chance) {

    }

    @Override
    public float getHelmetDropChance() {
        return 0;
    }

    @Override
    public void setHelmetDropChance(float chance) {

    }

    @Override
    public float getChestplateDropChance() {
        return 0;
    }

    @Override
    public void setChestplateDropChance(float chance) {

    }

    @Override
    public float getLeggingsDropChance() {
        return 0;
    }

    @Override
    public void setLeggingsDropChance(float chance) {

    }

    @Override
    public float getBootsDropChance() {
        return 0;
    }

    @Override
    public void setBootsDropChance(float chance) {

    }

    @Override
    public Entity getHolder() {
        return null;
    }
}
