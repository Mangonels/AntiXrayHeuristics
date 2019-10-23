//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import es.mithrandircraft.antixrayheuristics.files.Xrayer;
import org.apache.commons.dbcp.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MemoryManager {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    MemoryManager(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    //SQL Data:
    public BasicDataSource dataSource; //Stores a pool of sql connections

    //JSON Data:
    private List<Xrayer> storedXrayersFromJSON = new ArrayList<Xrayer>(); //Used for loading xrayer data from JSON

    //The following functions manage persistent memory resources depending on plugin configuration:

    void StorePlayerData(String playername)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null)
                    {
                        SQLPlayerDataStore(cn, playername);
                    }
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                        try {
                            cn.close();
                        } catch (SQLException e) {
                            System.err.print(e);
                        }
                }
                break;
            case "JSON":
                JSONFilePlayerDataStore(playername);
                break;
            default:
                break;
        }
    }

    public void GetAllBaseXrayerData(ArrayList<String> UUIDs, ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes) //Returns array list containing all registered xrayer UUID's
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        SQLGetAllBaseXrayerData(cn, UUIDs, handledAmounts, firstHandledTimes);
                    }
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        cn.close();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
            case "JSON":
                JSONGetAllBaseXrayerData(UUIDs ,handledAmounts, firstHandledTimes);
                break;
            default:
                break;
        }
    }

    public ItemStack[] GetXrayerBelongings(String xrayerUUID) //Returns ItemStack array containing all confiscated ItemStacks from the specified player by UUID
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        return SQLGetXrayerBelongings(cn, xrayerUUID);
                    }
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        cn.close();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
            case "JSON":
                return JSONGetXrayerBelongings(xrayerUUID);
            default:
                break;
        }
        return null;
    }

    public void DeleteXrayer(String xrayerUUID) //Deletes xrayer with specified UUID from memory
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        SQLDeleteXrayer(cn, xrayerUUID);
                    }
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        cn.close();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
            case "JSON":
                JSONDeleteXrayer(xrayerUUID);
                break;
            default:
                break;
        }
    }

    public void DeleteRegisteredXrayers() //Deletes all registered xrayers (basically leaves memory empty)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        SQLDeleteRegistry(cn);
                    }
                } catch (SQLException e) {
                    System.err.print(e);
                } finally {
                    try {
                        cn.close();
                    } catch (SQLException e) {
                        System.err.print(e);
                    }
                }
                break;
            case "JSON":
                JSONStoreInFile("[]");
                break;
            default:
                break;
        }
    }

    //------------------ SQL RELATED OPERATIONS ------------------:

    void InitializeDataSource()
    {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("org.gjt.mm.mysql.Driver");
        basicDataSource.setUsername(mainClassAccess.getConfig().getString("SQLUsername"));
        basicDataSource.setPassword(mainClassAccess.getConfig().getString("SQLPassword"));
        basicDataSource.setUrl("jdbc:mysql://" + mainClassAccess.getConfig().getString("SQLHost") + ":" + mainClassAccess.getConfig().getString("SQLPort") + "/" + mainClassAccess.getConfig().getString("SQLDatabaseName") + "?useSSL=false");
        basicDataSource.setMaxActive(mainClassAccess.getConfig().getInt("SQLMaxActiveConnections"));

        dataSource = basicDataSource;
    }

    void CloseDataSource()
    {
        try {
            dataSource.close();
        } catch (SQLException e) {
            System.err.print(e);
        }
    }

    void SQLCreateTableIfNotExists() throws SQLException //Creates the Xrayers table
    {
        java.sql.Connection cn = null;
        try {
            cn = dataSource.getConnection();
            if(cn != null) {
                PreparedStatement create = cn.prepareStatement("CREATE TABLE IF NOT EXISTS Xrayers(UUID VARCHAR(36) NOT NULL, Handled INT NOT NULL, FirstHandleTime VARCHAR(36) NOT NULL, Belongings TEXT NULL, PRIMARY KEY(UUID))");

                create.executeUpdate();

                System.out.println(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("SQLTableFoundOrCreated"));
            }
        } catch (SQLException e) {
            System.err.print(e);
        } finally {
            try {
                cn.close();
            } catch (SQLException e) {
                System.err.print(e);
            }
        }
    }

    private boolean SQLFindUUID(java.sql.Connection connection, String n) throws SQLException //Returns true if UUID was found in the database
    {
        PreparedStatement query = connection.prepareStatement("SELECT COUNT(1) FROM Xrayers WHERE UUID = ?");
        query.setString(1, n);

        ResultSet result = query.executeQuery();

        result.next();
        return result.getInt(1) == 1;
    }

    private void SQLPlayerDataStore(java.sql.Connection connection, String playername) throws SQLException //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored.
    {
        Player p = Bukkit.getServer().getPlayer(playername);
        assert p != null;
        if(!SQLFindUUID(connection, p.getUniqueId().toString())){ //Primary key (player UUID) doesn't already exist
            if(mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, Belongings) VALUES(?,?,?,?)");
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

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime) VALUES(?,?,?)");
                entry.setString(1, p.getUniqueId().toString());
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));

                entry.executeUpdate();
            }
        }
        else{ //Primary key (player UUID) already exists
            //Just add +1 to Handled column
            PreparedStatement update = connection.prepareStatement("UPDATE Xrayers SET Handled = Handled + 1 WHERE UUID = ?");
            update.setString(1, p.getUniqueId().toString());

            update.executeUpdate();
        }
    }

    private void SQLGetAllBaseXrayerData(java.sql.Connection connection, ArrayList<String> UUIDs, ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes) throws SQLException //Returns all of the basic xrayer information (pretty much everything except for the inventory)
    {
        PreparedStatement entry = connection.prepareStatement("SELECT UUID, Handled, FirstHandleTime FROM Xrayers");

        ResultSet result = entry.executeQuery();

        while(result.next())
        {
            UUIDs.add(result.getString("UUID"));
            handledAmounts.add(result.getInt("Handled"));
            firstHandledTimes.add(result.getString("FirstHandleTime"));
        }
    }

    private ItemStack[] SQLGetXrayerBelongings(java.sql.Connection connection, String xrayerUUID) throws SQLException //Gets an xrayer player's (by UUID) confiscated belongings
    {
        PreparedStatement query = connection.prepareStatement("SELECT Belongings FROM Xrayers WHERE UUID = ?");
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

    private void SQLDeleteXrayer(java.sql.Connection connection, String xrayerUUID) throws SQLException //Removes player (by UUID) from xrayers database
    {
        PreparedStatement purge = connection.prepareStatement("DELETE FROM Xrayers WHERE UUID = ?");
        purge.setString(1, xrayerUUID);

        purge.executeUpdate();
    }

    private void SQLDeleteRegistry(java.sql.Connection connection) throws SQLException //Truncates the whole Xrayers table, basically emptying all registered xrayers
    {
        PreparedStatement purge = connection.prepareStatement("TRUNCATE TABLE Xrayers");

        purge.executeUpdate();
    }


    //------------------ JSON RELATED OPERATIONS ------------------

    //Java File I.O. functions

    public boolean JSONFileCreateIfNotExists() //Returns true if file was created
    {
        try {
            //Create file (will do nothing if it already exists):
            if(new File(mainClassAccess.getDataFolder().getAbsolutePath() + "/data.json").createNewFile()) { JSONStoreInFile("[]"); } //Store empty JSON array in file
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private void JSONStoreInFile(String toStore) //Writes json content as string to file
    {
        try {
            FileWriter writer = new FileWriter(mainClassAccess.getDataFolder().getAbsolutePath() + "/data.json");
            writer.write(toStore);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private BufferedReader JSONGetFromFile() //Gets json content from file in BufferedReader format
    {
        try {
            return new BufferedReader(new FileReader(mainClassAccess.getDataFolder().getAbsolutePath() + "/data.json")); //Return buffered file contents
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //GSON Serialization functions

    private String JSONSerializeXrayersData(List<Xrayer> xrayers) //Serializes class ArrayList to json String
    {
        return new Gson().toJson(xrayers);
    }

    private List<Xrayer> JSONDeserializeXrayersData(BufferedReader xrayers) //Serializes from BufferedReader to ArrayList.
    {
        return new Gson().fromJson(xrayers, new TypeToken<ArrayList<Xrayer>>(){}.getType()); //TypeToken gets the type of an arraylist of xrayers
    }

    //File data manipulation and querying using GSON

    private void JSONRefreshLoadedXrayerData() //Loads json data from file and converts it to List, assigning it to storedXrayersFromJSON, consequently refreshing it in RAM Stack
    {
        storedXrayersFromJSON = JSONDeserializeXrayersData(JSONGetFromFile());
    }

    public void JSONFlushLoadedXrayerData() //Removes the loaded xrayer data from memory. Used for when nothing is actually using it.
    {
        storedXrayersFromJSON.clear();
    }

    private void JSONFilePlayerDataStore(String playername) //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored.
    {
        Player p = Bukkit.getServer().getPlayer(playername);
        if (p != null) //Just a check to avoid null player errors
        {
            //Refresh loaded xrayers in RAM Stack:
            JSONRefreshLoadedXrayerData();
            //Check if xrayer is already stored?:
            boolean exists = false;
            for(Xrayer xrayer : storedXrayersFromJSON){
                if(xrayer.UUID.equals(Bukkit.getServer().getPlayer(playername).getUniqueId().toString()))
                {
                    exists = true;
                    //Also add +1 to handled:
                    xrayer.Handled += 1;
                    //Store List back to file:
                    String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                    JSONStoreInFile(serial);
                    break;
                }
            }
            if (!exists) { //player UUID doesn't already exist in file
                if (mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
                {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    //Add xrayer to List:
                    storedXrayersFromJSON.add(new Xrayer(p.getUniqueId().toString(), 1, dtf.format(now), BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(p.getInventory(), p.getEquipment()))));
                    //Store List back to file:
                    String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                    JSONStoreInFile(serial);
                } else //Partial store
                {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    //Add xrayer to List:
                    storedXrayersFromJSON.add(new Xrayer(p.getUniqueId().toString(), 1, dtf.format(now), null));
                    //Store List back to file:
                    JSONStoreInFile(JSONSerializeXrayersData(storedXrayersFromJSON));
                }
            }
        }
    }
    private void JSONGetAllBaseXrayerData(ArrayList<String> UUIDs, ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Extract information from storedXrayersFromJSON:
        for(Xrayer xrayer : storedXrayersFromJSON)
        {
            UUIDs.add(xrayer.UUID);
            handledAmounts.add(xrayer.Handled);
            firstHandledTimes.add(xrayer.FirstHandleTime);
        }
    }
    private ItemStack[] JSONGetXrayerBelongings(String xrayerUUID)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Find uuid, and return it's belongings:
        for(Xrayer xrayer : storedXrayersFromJSON)
        {
            if(xrayer.UUID.equals(xrayerUUID)) {
                try{
                return BukkitSerializer.itemStackArrayFromBase64(xrayer.Belongings);
                } catch (IOException e){
                    System.err.print(e);
                    return null;
                }
            }
        }
        return null;
    }
    private void JSONDeleteXrayer(String xrayerUUID)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        for(Xrayer xrayer : storedXrayersFromJSON)
        {
            if(xrayer.UUID.equals(xrayerUUID)) {
                storedXrayersFromJSON.remove(xrayer);
                //Store List back to file:
                JSONStoreInFile(JSONSerializeXrayersData(storedXrayersFromJSON));
                break;
            }
        }
    }
}
