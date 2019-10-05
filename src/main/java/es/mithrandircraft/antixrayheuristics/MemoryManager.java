package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.*;
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
                    SQLPlayerDataStore(playername);
                } catch (SQLException e) {
                    System.err.print(e);
                }
                break;
            case "FLATFILE":
                FlatFilePlayerDataStore(playername);
                break;
            case "SQLITE":
            default:
                SQLitePlayerDataStore(playername);
                break;
        }
    }

    public ArrayList<String> GetBaseXrayerData(ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes) //Returns array list containing all registered xrayer UUID's
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    return SQLGetBaseXrayerData(handledAmounts, firstHandledTimes);
                } catch (SQLException e) {
                    System.err.print(e);
                }
                break;
            case "FLATFILE":
                //FlatFileGetBaseXrayerData();
                break;
            case "SQLITE":
            default:
                //SQLiteGetBaseXrayerData();
                break;
        }
        return null;
    }

    public ItemStack[] GetXrayerBelongings(String xrayerUUID) //Returns ItemStack array containing all confiscated ItemStacks from the specified player by UUID
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    return SQLGetXrayerBelongings(xrayerUUID);
                } catch (SQLException e) {
                    System.err.print(e);
                }
                break;
            case "FLATFILE":
                //FlatFileGetXrayerBelongings();
                break;
            case "SQLITE":
            default:
                //SQLiteGetXrayerBelongings();
                break;
        }
        return null;
    }

    public void DeleteXrayer(String xrayerUUID) //Deletes xrayer with specified UUID from memory
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLDeleteXrayer(xrayerUUID);
                } catch (SQLException e) {
                    System.err.print(e);
                }
                break;
            case "FLATFILE":
                //FlatFileDeleteXrayer();
                break;
            case "SQLITE":
            default:
                //SQLiteDeleteXrayer();
                break;
        }
    }

    public void DeleteRegisteredXrayers() //Deletes all registered xrayers (basically leaves memory empty)
    {
        switch (mainClassAccess.getConfig().getString("StorageType")) {
            case "MYSQL":
                try {
                    SQLDeleteRegistry();
                } catch (SQLException e) {
                    System.err.print(e);
                }
                break;
            case "FLATFILE":
                //FlatFileDeleteRegistry();
                break;
            case "SQLITE":
            default:
                //SQLiteDeleteRegistry();
                break;
        }
    }

    //SQL related operations:

    void SQLConnect() throws SQLException //Establishes sql connection
    {
        String url = "jdbc:mysql://" + mainClassAccess.getConfig().getString("SQLHost") + "/" + mainClassAccess.getConfig().getString("SQLDatabaseName");
        SQLcon = DriverManager.getConnection(url, mainClassAccess.getConfig().getString("SQLUsername"), mainClassAccess.getConfig().getString("SQLPassword"));
        System.out.println("[AntiXrayHeuristics]: Successfully connected to SQL database.");
    }

    void SQLDisconnect() throws SQLException //Disconnects from sql
    {
        if(SQLcon != null)
        {
            SQLcon.close();
            System.out.println("[AntiXrayHeuristics]: Disconnected from SQL database.");
        }
    }

    void SQLCreateTableIfNotExists() throws SQLException //Creates the Xrayers table
    {
        PreparedStatement create = SQLcon.prepareStatement("CREATE TABLE IF NOT EXISTS Xrayers(UUID VARCHAR(36) NOT NULL, Handled INT NOT NULL, FirstHandleTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, Belongings TEXT NULL, PRIMARY KEY(UUID))");
        create.executeUpdate();

        System.out.println("[AntiXrayHeuristics]: SQL Xrayers table was either found or created from scratch. All seems to be in order.");
    }

    private boolean SQLFindUUID(String n) throws SQLException //Returns true if UUID was found in the database
    {
        PreparedStatement query = SQLcon.prepareStatement("SELECT COUNT(1) FROM Xrayers WHERE UUID = ?");
        query.setString(1, n);
        ResultSet result = query.executeQuery();
        result.next();
        return result.getInt(1) == 1;
    }

    private void SQLPlayerDataStore(String n) throws SQLException //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored.
    {
        Player p = Bukkit.getServer().getPlayer(n);
        assert p != null;
        if(!SQLFindUUID(p.getUniqueId().toString())){ //Primary key (player UUID) doesn't already exist
            if(mainClassAccess.getConfig().getBoolean("StoreCopy")) //Full store
            {
                PreparedStatement entry = SQLcon.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, Belongings) VALUES(?,?,?,?)");
                entry.setString(1, p.getUniqueId().toString());
                entry.setInt(2, 1);
                entry.setString(3, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(p.getInventory(), p.getEquipment())));
                entry.executeUpdate();
            }
            else //Partial store
            {
                PreparedStatement entry = SQLcon.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime) VALUES(?,?,?)");
                entry.setString(1, p.getUniqueId().toString());
                entry.setInt(2, 1);
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

    private ArrayList<String> SQLGetBaseXrayerData(ArrayList<Integer> handledAmounts, ArrayList<String> firstHandledTimes) throws SQLException //Returns all of the basic xrayer information (pretty much everything except for the inventory)
    {
        PreparedStatement entry = SQLcon.prepareStatement("SELECT UUID, Handled, FirstHandleTime FROM Xrayers");

        ResultSet result = entry.executeQuery();

        ArrayList<String> arr = new ArrayList<String>();
        while(result.next())
        {
            arr.add(result.getString("UUID"));
            handledAmounts.add(result.getInt("Handled"));
            firstHandledTimes.add(result.getString("FirstHandleTime"));
        }
        return arr;
    }

//    private ArrayList<String> SQLGetXrayerUUIDs() throws SQLException //Returns UUID column from database as array list.
//    {
//        PreparedStatement entry = SQLcon.prepareStatement("SELECT UUID FROM Xrayers;");
//
//        ResultSet result = entry.executeQuery();
//
//        ArrayList<String> arr =  new ArrayList<String>();
//        while(result.next())
//        {
//            arr.add(result.getString("UUID"));
//        }
//        return arr;
//    }

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

        System.out.println("[AntiXrayHeuristics]: Player with UUID: " + xrayerUUID + " stored in SQL Xrayers table was cleared by a user with permissions.");
    }

    private void SQLDeleteRegistry() throws SQLException //Truncates the whole Xrayers table, basically emptying all registered xrayers
    {
        PreparedStatement purge = SQLcon.prepareStatement("TRUNCATE TABLE Xrayers");
        purge.executeUpdate();

        System.out.println("[AntiXrayHeuristics]: SQL Xrayers table was cleared by a user with permissions.");
    }

    public static String MySQLGetPlayerData(Player p) //Gets an xrayer player's data
    {
        //TO-DO
        return p.getName();
    }
    public static String MySQLGetPlayerData(Player p, Inventory inventory) //Gets an xrayer player's data and belongings
    {
        //TO-DO 2
        return p.getName();
    }

    //SQLite Operations
    private void SQLitePlayerDataStore(String n)
    {

    }

    //FlatFile Operations
    private void FlatFilePlayerDataStore(String n)
    {

    }
}
