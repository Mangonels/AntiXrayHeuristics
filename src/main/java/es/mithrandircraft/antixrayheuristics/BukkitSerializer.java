package es.mithrandircraft.antixrayheuristics;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class BukkitSerializer {

    public static ItemStack[] InventoryAndEquipmentToSingleItemStackArray(Inventory inv, EntityEquipment equip) //Returns inventory and equipment item stacks put into an array
    {
        ItemStack[] confiscatedItems = new ItemStack[41]; //41 = max number of items a player can have in inventory and equipment combined.
        for(int i = 0; i < 36; i++) confiscatedItems[i] = inv.getItem(i);
        confiscatedItems[36] = equip.getItemInOffHand();
        confiscatedItems[37] = equip.getHelmet();
        confiscatedItems[38] = equip.getChestplate();
        confiscatedItems[39] = equip.getLeggings();
        confiscatedItems[40] = equip.getBoots();

        return confiscatedItems;
    }

    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            //Write the size of the inventory
            dataOutput.writeInt(items.length);

            //Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            //Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        if(data == null) //Null string case returns null
        {
            return null;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            //Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}