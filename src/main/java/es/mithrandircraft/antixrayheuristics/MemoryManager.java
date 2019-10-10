package es.mithrandircraft.antixrayheuristics;

import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import es.mithrandircraft.antixrayheuristics.files.XrayerDataParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MemoryManager {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    MemoryManager(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    private Connection SQLcon = null;
    Connection GetSQLcon() //Returns sql connection reference
    {
        return SQLcon;
    }

    //The following functions manage persistent memory resources depending on plugin configuration:

    void StorePlayerData(String playername)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLConnect();
                    SQLPlayerDataStore(playername);
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        SQLDisconnect();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
            case "JSON":
            default:
                JSONFilePlayerDataStore(playername);
                break;
        }
    }

    public void GetBaseXrayerData(ArrayList<String> UUIDs, ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes) //Returns array list containing all registered xrayer UUID's
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLConnect();
                    SQLGetBaseXrayerData(UUIDs ,handledAmounts, firstHandledTimes);
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        SQLDisconnect();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
//            case "JSON":
//            default:
//                JSONGetBaseXrayerData(UUIDs ,handledAmounts, firstHandledTimes);
//                break;
        }
    }

    public ItemStack[] GetXrayerBelongings(String xrayerUUID) //Returns ItemStack array containing all confiscated ItemStacks from the specified player by UUID
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLConnect();
                    return SQLGetXrayerBelongings(xrayerUUID);
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        SQLDisconnect();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
//            case "JSON":
//            default:
//                return JSONGetXrayerBelongings(xrayerUUID);
//                break;
        }
        return null;
    }

    public void DeleteXrayer(String xrayerUUID) //Deletes xrayer with specified UUID from memory
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLConnect();
                    SQLDeleteXrayer(xrayerUUID);
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        SQLDisconnect();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
//            case "JSON":
//            default:
//                JSONDeleteXrayer(xrayerUUID);
//                break;
        }
    }

    public void DeleteRegisteredXrayers() //Deletes all registered xrayers (basically leaves memory empty)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLConnect();
                    SQLDeleteRegistry();
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        SQLDisconnect();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
//            case "JSON":
//            default:
//                JSONDeleteRegistry();
//                break;
        }
    }

    //SQL related operations:

    void SQLConnect() throws SQLException //Establishes sql connection
    {
        String url = "jdbc:mysql://" + mainClassAccess.getConfig().getString("SQLHost") + "/" + mainClassAccess.getConfig().getString("SQLDatabaseName") + "?useSSL=false";
        SQLcon = DriverManager.getConnection(url, mainClassAccess.getConfig().getString("SQLUsername"), mainClassAccess.getConfig().getString("SQLPassword"));
    }

    void SQLDisconnect() throws SQLException //Disconnects from sql
    {
        if(SQLcon != null)
        {
            SQLcon.close();
        }
    }

    void SQLCreateTableIfNotExists() throws SQLException //Creates the Xrayers table
    {
        PreparedStatement create = SQLcon.prepareStatement("CREATE TABLE IF NOT EXISTS Xrayers(UUID VARCHAR(36) NOT NULL, Handled INT NOT NULL, FirstHandleTime VARCHAR(36) NOT NULL, Belongings TEXT NULL, PRIMARY KEY(UUID))");

        create.executeUpdate();

        System.out.println(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("SQLTableFoundOrCreated"));
    }

    private boolean SQLFindUUID(String n) throws SQLException //Returns true if UUID was found in the database
    {
        PreparedStatement query = SQLcon.prepareStatement("SELECT COUNT(1) FROM Xrayers WHERE UUID = ?");
        query.setString(1, n);

        ResultSet result = query.executeQuery();

        result.next();
        return result.getInt(1) == 1;
    }

    private void SQLPlayerDataStore(String playername) throws SQLException //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored.
    {
        Player p = Bukkit.getServer().getPlayer(playername);
        assert p != null;
        if(!SQLFindUUID(p.getUniqueId().toString())){ //Primary key (player UUID) doesn't already exist
            if(mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = SQLcon.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, Belongings) VALUES(?,?,?,?)");
                entry.setString(1, p.getUniqueId().toString());
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(p.getInventory(), p.getEquipment())));

                entry.executeUpdate();
            }
            else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = SQLcon.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime) VALUES(?,?,?)");
                entry.setString(1, p.getUniqueId().toString());
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));

                entry.executeUpdate();
            }
        }
        else{ //Primary key (player UUID) already exists
            //Just add +1 to Handled column
            PreparedStatement update = SQLcon.prepareStatement("UPDATE Xrayers SET Handled = Handled + 1 WHERE UUID = ?");
            update.setString(1, p.getUniqueId().toString());

            update.executeUpdate();
        }
    }

    private void SQLGetBaseXrayerData(ArrayList<String> UUIDs, ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes) throws SQLException //Returns all of the basic xrayer information (pretty much everything except for the inventory)
    {
        PreparedStatement entry = SQLcon.prepareStatement("SELECT UUID, Handled, FirstHandleTime FROM Xrayers");

        ResultSet result = entry.executeQuery();

        while(result.next())
        {
            UUIDs.add(result.getString("UUID"));
            handledAmounts.add(result.getInt("Handled"));
            firstHandledTimes.add(result.getString("FirstHandleTime"));
        }
    }

    private ItemStack[] SQLGetXrayerBelongings(String xrayerUUID) throws SQLException //Gets an xrayer player's (by UUID) confiscated belongings
    {
        PreparedStatement query = SQLcon.prepareStatement("SELECT Belongings FROM Xrayers WHERE UUID = ?");
        query.setString(1, xrayerUUID);

        ResultSet result = query.executeQuery();

        result.next();

        try {
            return BukkitSerializer.itemStackArrayFromBase64(result.getString("Belongings"));
        } catch (IOException e){
            System.err.print(e);
            return null;
        }
    }

    private void SQLDeleteXrayer(String xrayerUUID) throws SQLException //Removes player (by UUID) from xrayers database
    {
        PreparedStatement purge = SQLcon.prepareStatement("DELETE FROM Xrayers WHERE UUID = ?");
        purge.setString(1, xrayerUUID);

        purge.executeUpdate();
    }

    private void SQLDeleteRegistry() throws SQLException //Truncates the whole Xrayers table, basically emptying all registered xrayers
    {
        PreparedStatement purge = SQLcon.prepareStatement("TRUNCATE TABLE Xrayers");

        purge.executeUpdate();
    }


    //JSON related operations:

    private boolean JSONFindUUID(String uuid) //Returns true if UUID was found in the JSON file
    {
        return true;
    }

    private void JSONFilePlayerDataStore(String playername) //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored.
    {
        Player p = Bukkit.getServer().getPlayer(playername);
        assert p != null;
        if(!JSONFindUUID(p.getUniqueId().toString())){ //player UUID doesn't already exist in file
            if(mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                //String inputJson ="{  \"email\":\"abc@123.com\",  \"did_you_mean\":\"abc@me.com\",  \"user\":\"abc\",  \"domain\":\"123.com\",  \"format_valid\":true,  \"mx_found\":false,  \"smtp_check\":false,  \"catch_all\":null,  \"role\":false,  \"disposable\":false,  \"free\":true,  \"score\":0.16}";
                String inputJson = "{\"xrayer\":{\"uuid\":" + p.getUniqueId().toString() + ", \"Handled\":\"1\", \"FirstHandleTime\":" + dtf.format(now) + ", \"Belongings\":" + BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(p.getInventory(), p.getEquipment())) + "}";

                ObjectMapper mapper = new ObjectMapper();
                try {
                    XrayerDataParser xdp = mapper.readValue(inputJson, XrayerDataParser.class);
                    System.out.println(xdp.getXrayerData().getUUID());
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                String inputJson = "{\"xrayer\":{\"uuid\":" + p.getUniqueId().toString() + ", \"Handled\":\"1\", \"FirstHandleTime\":" + dtf.format(now) + "}";

                ObjectMapper mapper = new ObjectMapper();
                try {
                    XrayerDataParser xdp = mapper.readValue(inputJson, XrayerDataParser.class);
                    System.out.println(xdp.getXrayerData().getUUID());
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{ //player UUID already exists in file
            //Just add +1 to Handled

        }
    }
}
