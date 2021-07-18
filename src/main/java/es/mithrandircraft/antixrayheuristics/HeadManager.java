package es.mithrandircraft.antixrayheuristics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class HeadManager {

    /**
     *Gets the head of a player for, will only work on 1.13 or higher
     *This method should be called asynchronously
     *
     * @param playerUUID The player's UUID for the head we want
     * @param callback Optionally add a callback for post-async task
     * @return ItemStack with the player's skull as material, null if version is
     */
    static ItemStack GetPlayerHead(UUID playerUUID, CallbackAddXrayerHeadToCache callback)
    {
        String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();

        String value;
        value = HeadManager.GetHeadValue(playerName);
        if (value == null) value = "";

        ItemStack playerHead = HeadManager.GetHead(value);

        if(callback != null) callback.onFetchUpdateDone(playerHead);

        return playerHead;
    }

    /**Gets a head by UUID*/
    static ItemStack GetHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }

    /**Gets a head's internal code (long string with alphanumbers ending in =, which represents the head), as a string*/
    static String GetHeadValue(String name){
        try
        {
            String result = GetURLContent("https://api.mojang.com/users/profiles/minecraft/" + name);
            Gson g = new Gson();
            JsonObject obj = g.fromJson(result, JsonObject.class);
            String uid = obj.get("id").toString().replace("\"","");
            String signature = GetURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uid);
            obj = g.fromJson(signature, JsonObject.class);
            String value = obj.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
            String decoded = new String(Base64.getDecoder().decode(value));
            obj = g.fromJson(decoded,JsonObject.class);
            String skinURL = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            byte[] skinByte = ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}").getBytes();
            return new String(Base64.getEncoder().encode(skinByte));
        }
        catch (Exception ignored) { }

        return null;
    }

    /**Gets the contents of a web URL as a string*/
    private static String GetURLContent(String urlStr) {
        URL url;
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try
        {
            url = new URL(urlStr);
            in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8) );
            String str;
            while((str = in.readLine()) != null) {
                sb.append( str );
            }
        }
        catch (Exception ignored) { }
        finally
        {
            try
            {
                if(in!=null) in.close();
            }
            catch(IOException ignored) { }
        }
        return sb.toString();
    }
}
