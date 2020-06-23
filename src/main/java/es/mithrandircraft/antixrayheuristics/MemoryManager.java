//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import es.mithrandircraft.antixrayheuristics.callbacks.*;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import es.mithrandircraft.antixrayheuristics.files.Xrayer;
import org.apache.commons.dbcp.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    //The following functions manage persistent memory resources depending on plugin configuration
    //They are designed to be called asynchronously through Bukkit's scheduler, and return data through a callback function:

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

    public void GetAllBaseXrayerData(final GetAllBaseXrayerDataCallback callback) //Returns various array lists through callback function containing all registered xrayer UUID's, handled times amount, and firstHandled time.
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

    public void GetXrayerBelongings(String xrayerUUID, final GetXrayerBelongingsCallback callback) //Returns ItemStack array through callback function containing all confiscated ItemStacks from the specified player by UUID
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

    public void GetXrayerHandleLocation(String xrayerUUID, final GetXrayerHandleLocationCallback callback) //Returns HandleLocation Location through callback function by UUID
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

    void SQLCreateTableIfNotExists() throws SQLException //Creates the Xrayers table
    {
        java.sql.Connection cn = null;
        try {
            cn = dataSource.getConnection();
            if(cn != null) {
                PreparedStatement create = cn.prepareStatement("CREATE TABLE IF NOT EXISTS Xrayers(UUID VARCHAR(36) NOT NULL, Handled INT NOT NULL, FirstHandleTime VARCHAR(36) NOT NULL, HandleLocation VARCHAR(36) NOT NULL, Belongings TEXT NULL, PRIMARY KEY(UUID))");

                create.executeUpdate();

                System.out.println(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("SQLTableFoundOrCreated")));
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
    private void SQLPlayerDataStore(java.sql.Connection connection, Player player, final StorePlayerDataCallback callback) throws SQLException //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored.
    {
        assert player != null;
        if(!SQLFindUUID(connection, player.getUniqueId().toString())){ //Primary key (player UUID) doesn't already exist

            String serializedPlayerLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getPitch() + "," + player.getLocation().getYaw();

            if(mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, HandleLocation, Belongings) VALUES(?,?,?,?,?)");
                entry.setString(1, player.getUniqueId().toString());
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, serializedPlayerLocation);
                entry.setString(5, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(player.getInventory(), player.getEquipment())));

                entry.executeUpdate();
            }
            else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, HandleLocation) VALUES(?,?,?,?)");
                entry.setString(1, player.getUniqueId().toString());
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, serializedPlayerLocation);

                entry.executeUpdate();
            }

            Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                @Override
                public void run() {
                    callback.onInsertDone(1);
                }
            });
        }
        else{ //Primary key (player UUID) already exists
            //Add +1 to Handled column
            PreparedStatement update = connection.prepareStatement("UPDATE Xrayers SET Handled = Handled + 1 WHERE UUID = ?");
            update.setString(1, player.getUniqueId().toString());

            update.executeUpdate();

            //Get Handled column value
            PreparedStatement query = connection.prepareStatement("SELECT Handled FROM Xrayers WHERE UUID = ?");
            query.setString(1, player.getUniqueId().toString());

            ResultSet result = query.executeQuery();

            result.next();

            final int timesHandledFinal = result.getInt("Handled");
            Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                @Override
                public void run() {
                    callback.onInsertDone(timesHandledFinal);
                }
            });
        }
    }
    private void SQLGetAllBaseXrayerData(java.sql.Connection connection, final GetAllBaseXrayerDataCallback callback) throws SQLException //Returns all of the basic xrayer information (pretty much everything except for the inventory and handlecoordinates)
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

        Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
            @Override
            public void run() {
                callback.onQueryDone(UUIDs, handledAmounts, firstHandledTimes);
            }
        });
    }
    private void SQLGetXrayerBelongings(java.sql.Connection connection, String xrayerUUID, GetXrayerBelongingsCallback callback) throws SQLException //Gets an xrayer player's (by UUID) confiscated belongings
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
    private void SQLGetXrayerHandleLocation(java.sql.Connection connection, String xrayerUUID, GetXrayerHandleLocationCallback callback) throws SQLException //Gets an xrayer player's (by UUID) handle location
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
        //Handled times for returning:
        int timesHandled = 0;
        if (player != null) //Just a check to avoid null player errors
        {
            //Refresh loaded xrayers in RAM Stack:
            JSONRefreshLoadedXrayerData();
            //Check if xrayer is already stored?:
            boolean exists = false;
            for(Xrayer xrayer : storedXrayersFromJSON){
                if(xrayer.UUID.equals(Bukkit.getServer().getPlayer(player.getName()).getUniqueId().toString()))
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

                String serializedPlayerLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getPitch() + "," + player.getLocation().getYaw();

                if (mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
                {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    //Add xrayer to List:
                    storedXrayersFromJSON.add(new Xrayer(player.getUniqueId().toString(), 1, dtf.format(now), serializedPlayerLocation, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(player.getInventory(), player.getEquipment()))));
                    //Store List back to file:
                    String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                    JSONStoreInFile(serial);
                } else //Partial store
                {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    //Add xrayer to List:
                    storedXrayersFromJSON.add(new Xrayer(player.getUniqueId().toString(), 1, dtf.format(now), serializedPlayerLocation, null));
                    //Store List back to file:
                    JSONStoreInFile(JSONSerializeXrayersData(storedXrayersFromJSON));
                }

                Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                    @Override
                    public void run() {
                        callback.onInsertDone(1);
                    }
                });
            } else {
                final int timesHandledFinal = timesHandled;
                Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
                    @Override
                    public void run() {
                        callback.onInsertDone(timesHandledFinal);
                    }
                });
            }
        }
    }
    private void JSONGetAllBaseXrayerData(GetAllBaseXrayerDataCallback callback) //Returns all of the basic xrayer information (pretty much everything except for the inventory and handlecoordinates)
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

        Bukkit.getScheduler().runTask(mainClassAccess, new Runnable() { //Callback to main thread returns extracted data
            @Override
            public void run() {
                callback.onQueryDone(UUIDs, handledAmounts, firstHandledTimes);
            }
        });
    }
    private void JSONGetXrayerBelongings(String xrayerUUID, GetXrayerBelongingsCallback callback)
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
    private void JSONGetXrayerHandleLocation(String xrayerUUID, GetXrayerHandleLocationCallback callback)
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
