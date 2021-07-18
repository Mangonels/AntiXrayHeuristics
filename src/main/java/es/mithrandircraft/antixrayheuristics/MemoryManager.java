//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.dbcp.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class MemoryManager {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    MemoryManager(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    //SQL Data:
    private BasicDataSource dataSource; //Stores a pool of sql connections

    //JSON Data:
    private List<Xrayer> storedXrayersFromJSON = new ArrayList<Xrayer>(); //Used for loading xrayer data from JSON

    //The following methods manage persistent memory resources (SQL or JSON is managed depending on plugin config.yml)
    //The methods are designed to be called asynchronously through Bukkit's scheduler, and return data through a callback function:

    //Stores player as xrayer with data
    void StorePlayerData(Player player, final StorePlayerDataCallback callback)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null)
                    {
                        SQLPlayerDataStore(cn, player, callback);
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
                JSONPlayerDataStore(player, callback);
                break;
            default:
                break;
        }
    }

    //Stores fake player as xrayer with fake data
    void StoreDummyPlayerData(final StorePlayerDataCallback callback)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null)
                    {
                        SQLPlayerDataStore(cn, null, callback);
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
                JSONPlayerDataStore(null, callback);
                break;
            default:
                break;
        }
    }

    //Returns various array lists through callback function containing all registered xrayer UUID's, handled times amount, and firstHandled time.
    public void GetAllBaseXrayerData(final CallbackGetAllBaseXrayerData callback)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        SQLGetAllBaseXrayerData(cn, callback);
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
                JSONGetAllBaseXrayerData(callback);
                break;
            default:
                break;
        }
    }

    //Returns ItemStack array through callback function containing all confiscated ItemStacks from the specified player by UUID
    public void GetXrayerBelongings(String xrayerUUID, final CallbackGetXrayerBelongings callback)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        SQLGetXrayerBelongings(cn, xrayerUUID, callback);
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
                JSONGetXrayerBelongings(xrayerUUID, callback);
            default:
                break;
        }
    }

    //Returns HandleLocation Location through callback function by UUID
    public void GetXrayerHandleLocation(String xrayerUUID, final CallbackGetXrayerHandleLocation callback)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                java.sql.Connection cn = null;
                try {
                    cn = dataSource.getConnection();
                    if(cn != null) {
                        SQLGetXrayerHandleLocation(cn, xrayerUUID, callback);
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
                JSONGetXrayerHandleLocation(xrayerUUID, callback);
            default:
                break;
        }
    }

    //Deletes xrayer with specified UUID from memory
    public void DeleteXrayer(String xrayerUUID)
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

    //Deletes all registered xrayers (basically leaves memory empty)
    public void DeleteRegisteredXrayers()
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

        if(!mainClassAccess.getConfig().getString("SQLDriverClassName").equals(""))
            basicDataSource.setDriverClassName(mainClassAccess.getConfig().getString("SQLDriverClassName"));
        basicDataSource.setUsername(mainClassAccess.getConfig().getString("SQLUsername"));
        basicDataSource.setPassword(mainClassAccess.getConfig().getString("SQLPassword"));
        basicDataSource.setUrl("jdbc:mysql://" + mainClassAccess.getConfig().getString("SQLHost") + ":" + mainClassAccess.getConfig().getString("SQLPort") + "/" + mainClassAccess.getConfig().getString("SQLDatabaseName") + "?useSSL=false");
        basicDataSource.setMaxActive(mainClassAccess.getConfig().getInt("SQLMaxActiveConnections"));
        basicDataSource.setTestOnBorrow(true);
        basicDataSource.setValidationQuery("SELECT 1");

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

    //Creates the Xrayers table
    void SQLCreateTableIfNotExists() throws SQLException
    {
        java.sql.Connection cn = null;
        try {
            cn = dataSource.getConnection();
            if(cn != null) {
                PreparedStatement create = cn.prepareStatement("CREATE TABLE IF NOT EXISTS Xrayers(UUID VARCHAR(36) NOT NULL, Handled INT NOT NULL, FirstHandleTime VARCHAR(32) NOT NULL, HandleLocation VARCHAR(128) NOT NULL, Belongings TEXT NULL, PRIMARY KEY(UUID))");

                create.executeUpdate();
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

    //Returns true if UUID was found in the database
    private boolean SQLFindUUID(java.sql.Connection connection, String n) throws SQLException
    {
        PreparedStatement query = connection.prepareStatement("SELECT COUNT(1) FROM Xrayers WHERE UUID = ?");
        query.setString(1, n);

        ResultSet result = query.executeQuery();

        result.next();
        return result.getInt(1) == 1;
    }
    //Stores player name as xrayer and some other info + player belongings if configured (or dummy data if player is null), ONLY IF there isn't UUID related information already stored.
    private void SQLPlayerDataStore(java.sql.Connection connection, Player player, final StorePlayerDataCallback callback) throws SQLException
    {
        String playerUUID;
        //Assign true UUID?
        if(player != null) playerUUID = player.getUniqueId().toString();
        //Assign a random "fake" UUID?
        else playerUUID = UUID.randomUUID().toString();

        if(!SQLFindUUID(connection, playerUUID)) //Primary key (player UUID) doesn't already exist
        {
            String serializedPlayerLocation;
            //Assign true "serialized" location
            if(player != null) serializedPlayerLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getPitch() + "," + player.getLocation().getYaw();
            //Assign default "fake" "serialized" location
            else if (!mainClassAccess.getConfig().getStringList("TrackWorlds").isEmpty()) serializedPlayerLocation = mainClassAccess.getConfig().getStringList("TrackWorlds").get(0) + ",0.0,0.0,0.0,0.0,0.0";
            else serializedPlayerLocation = "world,0.0,0.0,0.0,0.0,0.0";

            if(mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, HandleLocation, Belongings) VALUES(?,?,?,?,?)");
                entry.setString(1, playerUUID);
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, serializedPlayerLocation);
                //Assign true player inventory and equipment
                if(player != null) entry.setString(5, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(player.getInventory(), player.getEquipment())));
                //Assign "fake" player inventory and equipment
                else
                {
                    Inventory madeUpInventory = new MadeUpInventory();
                    EntityEquipment madeUpEquipment = new MadeUpEquipment();
                    entry.setString(5, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(madeUpInventory, madeUpEquipment)));
                }

                entry.executeUpdate();
            }
            else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, HandleLocation) VALUES(?,?,?,?)");
                entry.setString(1, playerUUID);
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, serializedPlayerLocation);

                entry.executeUpdate();
            }

            if(callback != null)
            {
                Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                    @Override
                    public void run() {
                        callback.onInsertDone(1);
                    }
                });
            }
        }
        else //Primary key (player UUID) already exists
        {
            //Add +1 to Handled column
            PreparedStatement update = connection.prepareStatement("UPDATE Xrayers SET Handled = Handled + 1 WHERE UUID = ?");
            update.setString(1, playerUUID);

            update.executeUpdate();

            //Get Handled column value
            PreparedStatement query = connection.prepareStatement("SELECT Handled FROM Xrayers WHERE UUID = ?");
            query.setString(1, playerUUID);

            ResultSet result = query.executeQuery();

            result.next();

            if(callback != null)
            {
                final int timesHandledFinal = result.getInt("Handled");
                Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                    @Override
                    public void run() {
                        callback.onInsertDone(timesHandledFinal);
                    }
                });
            }
        }
    }
    private void SQLGetAllBaseXrayerData(java.sql.Connection connection, final CallbackGetAllBaseXrayerData callback) throws SQLException //Returns all of the basic xrayer information (pretty much everything except for the inventory and handlecoordinates)
    {
        PreparedStatement entry = connection.prepareStatement("SELECT UUID, Handled, FirstHandleTime FROM Xrayers");

        ResultSet result = entry.executeQuery();

        ArrayList<String> UUIDs = new ArrayList<String>();
        ArrayList<Integer> handledAmounts = new ArrayList<Integer>();
        ArrayList<String> firstHandledTimes = new ArrayList<String>();

        while(result.next())
        {
            UUIDs.add(result.getString("UUID"));
            handledAmounts.add(result.getInt("Handled"));
            firstHandledTimes.add(result.getString("FirstHandleTime"));
        }

        //We send the extracted data to the vault array from here:
        mainClassAccess.vault.SubstituteXrayerInfoLists(UUIDs, handledAmounts, firstHandledTimes);
        Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread
            @Override
            public void run() {
                callback.onQueryDone();
            }
        });
    }
    private void SQLGetXrayerBelongings(java.sql.Connection connection, String xrayerUUID, CallbackGetXrayerBelongings callback) throws SQLException //Gets an xrayer player's (by UUID) confiscated belongings
    {
        PreparedStatement query = connection.prepareStatement("SELECT Belongings FROM Xrayers WHERE UUID = ?");
        query.setString(1, xrayerUUID);

        ResultSet result = query.executeQuery();

        result.next();

        try {
            final ItemStack[] belongings = BukkitSerializer.itemStackArrayFromBase64(result.getString("Belongings"));
            Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                @Override
                public void run() {
                    callback.onQueryDone(belongings);
                }
            });
        } catch (IOException e){
            System.err.print(e);
        }
    }
    private void SQLGetXrayerHandleLocation(java.sql.Connection connection, String xrayerUUID, CallbackGetXrayerHandleLocation callback) throws SQLException //Gets an xrayer player's (by UUID) handle location
    {
        PreparedStatement query = connection.prepareStatement("SELECT HandleLocation FROM Xrayers WHERE UUID = ?");
        query.setString(1, xrayerUUID);

        ResultSet result = query.executeQuery();

        result.next();

        //Deserialize obtained location string:
        String[] serializedPlayerLocation = result.getString("HandleLocation").split(",");
        final Location deserializedHandleLocation = new Location(Bukkit.getWorld(serializedPlayerLocation[0]), Double.parseDouble(serializedPlayerLocation[1]), Double.parseDouble(serializedPlayerLocation[2]), Double.parseDouble(serializedPlayerLocation[3]), Float.parseFloat(serializedPlayerLocation[4]), Float.parseFloat(serializedPlayerLocation[5]));
        Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
            @Override
            public void run() {
                callback.onQueryDone(deserializedHandleLocation);
            }
        });
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

    public void JSONFlushLoadedXrayerData() //Removes the loaded xrayer data from memory. Used for when nothing is actually using it (no one's reading it, definitely).
    {
        storedXrayersFromJSON.clear();
    }

    private void JSONPlayerDataStore(Player player, final StorePlayerDataCallback callback) //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored. Also notifies through callback on finish
    {
        String playerUUID;
        //Assign true UUID?
        if(player != null) playerUUID = player.getUniqueId().toString();
        //Assign a random "fake" UUID?
        else playerUUID = UUID.randomUUID().toString();

        //Handled times for returning:
        int timesHandled = 0;

        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Check if xrayer is already stored?:
        boolean exists = false;
        for(Xrayer xrayer : storedXrayersFromJSON){
            if(xrayer.UUID.equals(playerUUID))
            {
                exists = true;
                //Also add +1 to handled:
                xrayer.Handled += 1;
                timesHandled = xrayer.Handled;
                //Store List back to file:
                String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                JSONStoreInFile(serial);
                break;
            }
        }
        if (!exists) { //player UUID doesn't already exist in file

            String serializedPlayerLocation;
            //Assign true "serialized" location
            if(player != null) serializedPlayerLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getPitch() + "," + player.getLocation().getYaw();
            //Assign default "fake" "serialized" location
            else if (!mainClassAccess.getConfig().getStringList("TrackWorlds").isEmpty()) serializedPlayerLocation = mainClassAccess.getConfig().getStringList("TrackWorlds").get(0) + ",0.0,0.0,0.0,0.0,0.0";
            else serializedPlayerLocation = "world,0.0,0.0,0.0,0.0,0.0";

            if (mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                //Add xrayer to List:
                //Existing inventory and equipment
                if(player != null) storedXrayersFromJSON.add(new Xrayer(playerUUID, 1, dtf.format(now), serializedPlayerLocation, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(player.getInventory(), player.getEquipment()))));
                //Made up inventory and equipment
                else {
                    Inventory madeUpInventory = new MadeUpInventory();
                    EntityEquipment madeUpEquipment = new MadeUpEquipment();
                    storedXrayersFromJSON.add(new Xrayer(playerUUID, 1, dtf.format(now), serializedPlayerLocation, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(madeUpInventory, madeUpEquipment))));
                }
                //Store List back to file:
                String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                JSONStoreInFile(serial);
            } else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                //Add xrayer to List:
                storedXrayersFromJSON.add(new Xrayer(playerUUID, 1, dtf.format(now), serializedPlayerLocation, null));
                //Store List back to file:
                JSONStoreInFile(JSONSerializeXrayersData(storedXrayersFromJSON));
            }

            if(callback != null) {
                Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                    @Override
                    public void run() {
                        callback.onInsertDone(1);
                    }
                });
            }
        } else if(callback != null) {
            final int timesHandledFinal = timesHandled;
            Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                @Override
                public void run() {
                    callback.onInsertDone(timesHandledFinal);
                }
            });
        }
    }
    private void JSONGetAllBaseXrayerData(CallbackGetAllBaseXrayerData callback) //Returns all of the basic xrayer information (pretty much everything except for the inventory and handlecoordinates)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Extract information from storedXrayersFromJSON:

        ArrayList<String> UUIDs = new ArrayList<String>();
        ArrayList<Integer> handledAmounts = new ArrayList<Integer>();
        ArrayList<String> firstHandledTimes = new ArrayList<String>();

        for(Xrayer xrayer : storedXrayersFromJSON)
        {
            UUIDs.add(xrayer.UUID);
            handledAmounts.add(xrayer.Handled);
            firstHandledTimes.add(xrayer.FirstHandleTime);
        }

        //We send the extracted data to the vault array from here:
        mainClassAccess.vault.SubstituteXrayerInfoLists(UUIDs, handledAmounts, firstHandledTimes);
        Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread
            @Override
            public void run() {
                callback.onQueryDone();
            }
        });
    }
    private void JSONGetXrayerBelongings(String xrayerUUID, CallbackGetXrayerBelongings callback)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Find uuid, and return it's belongings:
        for(Xrayer xrayer : storedXrayersFromJSON)
        {
            if(xrayer.UUID.equals(xrayerUUID)) {
                try {
                    final ItemStack[] belongings = BukkitSerializer.itemStackArrayFromBase64(xrayer.Belongings);
                    Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                        @Override
                        public void run() {
                            callback.onQueryDone(belongings);
                        }
                    });
                } catch (IOException e){
                    System.err.print(e);
                }
            }
        }
    }
    private void JSONGetXrayerHandleLocation(String xrayerUUID, CallbackGetXrayerHandleLocation callback)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Find uuid, and return it's handle location:
        for(Xrayer xrayer : storedXrayersFromJSON)
        {
            if(xrayer.UUID.equals(xrayerUUID)) {

                //Deserialize obtained location string:
                String[] serializedPlayerLocation = xrayer.HandleLocation.split(",");
                final Location deserializedHandleLocation = new Location(Bukkit.getWorld(serializedPlayerLocation[0]), Double.parseDouble(serializedPlayerLocation[1]), Double.parseDouble(serializedPlayerLocation[2]), Double.parseDouble(serializedPlayerLocation[3]), Float.parseFloat(serializedPlayerLocation[4]), Float.parseFloat(serializedPlayerLocation[5]));
                Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                    @Override
                    public void run() {
                        callback.onQueryDone(deserializedHandleLocation);
                    }
                });
            }
        }
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