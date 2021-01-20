/**************************************************
 * Ian Webb
 * Gabe Prudencio
 * Adrien Clay
 * CSCI 2251 Final Project
 * 12/8/2020
 * Rent Management System
 **************************************************/
package Server;


import com.sun.rowset.CachedRowSetImpl;
import javax.sql.rowset.CachedRowSet;
import java.io.*;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.DecimalFormat;

public class Server {


    private static String dbURL = "jdbc:sqlite:FinalRental.db";
    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static Connection c;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(dbURL);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }



    private static String tenantQuery = "SELECT name AS 'Name', leaseLength AS 'Lease Length (Monthly)', propertyID AS 'Property ID'," +
            "CASE WHEN rentPaid = 0 THEN 'Not Paid' " +
            "ELSE 'Paid' END AS RentPaid, email AS 'Email', phone AS 'Phone'\n" +
            "FROM tenants ORDER BY propertyID;";
    private static String availableQuery = "SELECT type AS 'Rental Type'," +
            "numBedrooms AS 'Bedroom Count', propertyID AS 'Property ID', rentPrice AS 'Cost (Monthly/Nightly)' FROM allProperties WHERE propertyID NOT IN (SELECT propertyID FROM tenants) ORDER BY propertyID;";
    private static String occupiedQuery = "SELECT t.name as 'Tenant Name', t.propertyID as 'Property ID', a.rentPrice as 'Cost (Monthly/Nightly)'\n" +
            "FROM tenants as t\n" +
            "INNER JOIN allProperties as a\n" +
            "ON t.propertyID = a.propertyID;";

    private static String allQuery = "SELECT type AS 'Rental Type', numBedrooms AS 'Bedroom Count', propertyID AS 'Property ID', rentPrice AS 'Cost (Monthly/Nightly)'," +
            "CASE WHEN availability = 1 THEN 'Available' ELSE 'Not Available' END AS 'Availability' FROM allProperties ORDER BY propertyID;";
    private static String updateAvailable = "UPDATE allProperties SET availability = NOT availability WHERE propertyID = ?;";


    private static String command;
    private static CachedRowSet returned;


    public static void main(String[] args) throws IOException {

        try {
            ServerSocket server = new ServerSocket(5000);
            System.out.println("Waiting For Client..");
            Socket conn = server.accept();
            System.out.println("Connected");
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
            out.flush();
            ObjectInputStream objectInputStream = new ObjectInputStream(conn.getInputStream());
            DataInputStream input = new DataInputStream(new BufferedInputStream(conn.getInputStream()));


            // SWITCH STATEMENT PARSE COMMANDS AND TELL SERVER WHAT TO DO
            while (true) {
                command = input.readUTF();
                out.flush();
                switch (command) {
                    case "1": // BUILD TENANT TABLE
                        CachedRowSet initialTenant = buildData(tenantQuery); // RETURN CACHED ROW SET TO UNPACK ON CLIENT SIDE
                        out.writeObject(initialTenant);
                        out.flush();
                        break;
                    case "2": // BUILD AVAILABILITY TABLE
                        CachedRowSet initialAvail = buildData(availableQuery);
                        out.writeObject(initialAvail);
                        out.flush();
                        break;
                    case "3": // BUILD OCCUPIED TABLE
                        CachedRowSet initalOccupied = buildData(occupiedQuery);
                        out.writeObject(initalOccupied);
                        out.flush();
                        break;
                    case "4": // BUILD ALL PROPERTIES TABLE
                        returned = buildData(allQuery);
                        out.writeObject(returned);
                        out.flush();
                        break;
                    case "5": // POPULATE DROPDOWN
                        CachedRowSet returnedDropdown = populateDropdown();
                        out.writeObject(returnedDropdown);
                        out.flush();
                        break;
                    case "6": // ADD TENANT
                        String name = input.readUTF();
                        double leaseLength = input.readDouble();
                        boolean rentPaid = input.readBoolean();
                        String propID = input.readUTF();
                        String email = input.readUTF();
                        String phone = input.readUTF();
                        CachedRowSet tenantAdd = addTenant(name, leaseLength, rentPaid, propID, email, phone);
                        out.writeObject(tenantAdd);
                        out.flush();
                        break;
                    case "7": // DELETE TENANT
                        String tenantName = input.readUTF();
                        String updateAvailableID = input.readUTF();
                        CachedRowSet tenantDelete = deleteTenant(tenantName, updateAvailableID);
                        out.writeObject(tenantDelete);
                        out.flush();
                        break;
                    case "8": // UPDATE DATA
                        String query = input.readUTF();
                        if (query.equals("availTable")){
                            returned = updateData(availableQuery);
                        } else if (query.equals("tenant")){
                            returned = updateData(tenantQuery);
                        } else if (query.equals("allQuery")){
                            returned = updateData(allQuery);
                        }else {
                            returned = updateData(occupiedQuery);
                        }
                        out.writeObject(returned);
                        out.flush();
                        break;
                    case "9": //UPDATE TENANT
                        String oldID = input.readUTF();
                        String nameUpdate = input.readUTF();
                        double leaseLengthUpdate = input.readDouble();
                        boolean rentPaidUpdate = input.readBoolean();
                        String propIDUpdate = input.readUTF();
                        String newEmail = input.readUTF();
                        String newPhone = input.readUTF();
                        CachedRowSet returnedTenantUpdate = updateTenant(oldID, nameUpdate, leaseLengthUpdate, rentPaidUpdate, propIDUpdate, newEmail, newPhone);
                        out.writeObject(returnedTenantUpdate);
                        out.flush();
                        break;
                    case "10": // ADD PROPERTY
                        String type = input.readUTF();
                        int numBedrooms = input.readInt();
                        String propertyID = input.readUTF();
                        double rentPrice = input.readDouble();

                        CachedRowSet addProperty = addProperty(type, numBedrooms, propertyID, rentPrice);
                        out.writeObject(addProperty);
                        out.flush();
                        break;
                    case "11": //DELETE PROPERTY
                        String delPropId = input.readUTF();
                        CachedRowSet deleteProperty = deleteProperty(delPropId);
                        out.writeObject(deleteProperty);
                        out.flush();
                        break;
                    case "12": // EDIT PROPERTY
                        String oldPropId = input.readUTF();
                        String newType = input.readUTF();
                        int newBedrooms = input.readInt();
                        String newID = input.readUTF();
                        double newPrice = input.readDouble();
                        CachedRowSetImpl editProp = updateProperty(oldPropId, newType, newBedrooms, newID, newPrice);
                        out.writeObject(editProp);
                        out.flush();
                        break;
                    case "13": // APPLY LATE FEES

                        String lateFeeID = input.readUTF();
                        double oldRent = input.readDouble();
                        CachedRowSet lateFees = applyLateFees(lateFeeID, oldRent);
                        out.writeObject(lateFees);
                        out.flush();
                        break;
                    case "14": // ADJUST COST ON OCCUPIED TABLE
                        String adjustCostID = input.readUTF();
                        double newRentPrice = input.readDouble();
                        CachedRowSet adjustedCost = adjustCostMethod(adjustCostID, newRentPrice);
                        out.writeObject(adjustedCost);
                        out.flush();
                        break;
                    case "15": // EXCEL EXPORT DATA
                        CachedRowSet tenantExport = buildData(tenantQuery);
                        out.writeObject(tenantExport);
                        out.flush();
                        CachedRowSet allExport = buildData(allQuery);
                        out.writeObject(allExport);
                        out.flush();
                        CachedRowSet availExport = buildData(availableQuery);
                        out.writeObject(availExport);
                        out.flush();
                        CachedRowSet occupiedExport = buildData(occupiedQuery);
                        out.writeObject(occupiedExport);
                        out.flush();
                        break;



                    default:

                        break;

                }
            }
        } catch (Exception ignored) {

        }


    }


    /*
    BEGIN METHODS TO PERFORM QUERIES
    This is where methods will perform all queries and actually update to the database
    Each method then also queries the database for the main info from the database and returns it to the client
    after making the change. This is so that each display can update everytime a change is made, rather than having
    to manually call the info again from the GUI.
     */
    private static CachedRowSet adjustCostMethod(String adjustCostID, double newRentPrice) throws SQLException {
        try {
            String query = "UPDATE allProperties SET rentPrice=? WHERE propertyID=?";
            PreparedStatement statement = c.prepareStatement(query);

            statement.setDouble(1, newRentPrice);
            statement.setString(2, adjustCostID);
            statement.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery(occupiedQuery);

            CachedRowSetImpl adjustedCost = new CachedRowSetImpl();

            adjustedCost.populate(rs);
            return adjustedCost;
        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;



    }

    private static CachedRowSet applyLateFees(String lateFeeID, double oldRent) {

        try {
            String query = "UPDATE allProperties SET rentPrice=? WHERE propertyID=?";
            double lateFee = oldRent * 1.05;
            decimalFormat.setRoundingMode(RoundingMode.DOWN);


            PreparedStatement statement = c.prepareStatement(query);
            statement.setDouble(1, Double.parseDouble(decimalFormat.format(lateFee)));
            statement.setString(2, lateFeeID);
            statement.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery("SELECT t.name as 'Tenant Name', t.propertyID as 'Property ID', a.rentPrice as 'Cost (Monthly/Nightly)'\n" +
                    "FROM tenants as t\n" +
                    "INNER JOIN allProperties as a\n" +
                    "ON t.propertyID = a.propertyID;");

            CachedRowSetImpl lateFeeReturn = new CachedRowSetImpl();
            lateFeeReturn.populate(rs);
            return lateFeeReturn;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

        private static CachedRowSetImpl updateProperty(String oldPropId, String newType, int newBedrooms, String newID, double newPrice) {

        try {
            String query = "UPDATE allProperties SET type=?, numBedrooms=?, propertyID=?, rentPrice=? WHERE propertyID=?";
            PreparedStatement statement = c.prepareStatement(query);

            statement.setString(1, newType);
            statement.setInt(2, newBedrooms);
            statement.setString(3, newID);
            statement.setDouble(4, newPrice);
            statement.setString(5, oldPropId);
            statement.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery("SELECT type AS Rental_Type," +
                    "numBedrooms AS Bedroom_Count, propertyID AS Property_ID, rentPrice AS 'Cost (Monthly)' FROM allProperties WHERE propertyID NOT IN (SELECT propertyID FROM tenants) ORDER BY propertyID;");

            CachedRowSetImpl updateProp = new CachedRowSetImpl();
            updateProp.populate(rs);
            return updateProp;
        } catch(Exception e){
            e.printStackTrace();
        }










        return null;
    }

    private static CachedRowSet deleteProperty(String delPropId) {


        try{
            String query = "DELETE FROM allProperties WHERE propertyID = ?";

            PreparedStatement statement =c.prepareStatement(query);
            statement.setString(1, delPropId);
            statement.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery("SELECT * FROM allProperties WHERE propertyID NOT IN (SELECT propertyID FROM tenants) ORDER BY propertyID;");

            CachedRowSetImpl delProp = new CachedRowSetImpl();
            delProp.populate(rs);
            return delProp;



        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    private static CachedRowSet addProperty(String type, int numBedrooms, String propertyID, double rentPrice) {
        try{
            String query = "INSERT INTO allProperties VALUES(?, ?, ?, ?, ?);";
            PreparedStatement statement = c.prepareStatement(query);
            boolean available = true;

            statement.setString(1, type);
            statement.setInt(2, numBedrooms);
            statement.setString(3, propertyID);
            statement.setDouble(4, rentPrice);
            statement.setBoolean(5, available);
            statement.executeUpdate();


            ResultSet rs = c.createStatement().executeQuery("SELECT * FROM availableProperties");

            CachedRowSetImpl addProp = new CachedRowSetImpl();

            addProp.populate(rs);
            return addProp;


        } catch (SQLException ignored) {

        }

        return null;


    }

    private static CachedRowSet updateData(String query) {

        try {

            ResultSet rs = c.createStatement().executeQuery(query);
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(rs);
            return crs;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }


    public static CachedRowSet buildData(String query) {
        try {

            ResultSet rs = c.createStatement().executeQuery(query);
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(rs);
            return crs;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
        return null;

    }


    public static CachedRowSet addTenant(String name, double leaseLength, boolean rentPaid, String propID, String email, String phone) {
        try {

            String query = "INSERT INTO tenants VALUES (?,?,?,?,?,?)";

            PreparedStatement statement = c.prepareStatement(query);

            statement.setString(1, name);
            statement.setDouble(2, leaseLength);
            statement.setBoolean(3, rentPaid);
            statement.setString(4, propID);
            statement.setString(5, email);
            statement.setString(6, phone);

            statement.executeUpdate();

            PreparedStatement statement2 = c.prepareStatement(updateAvailable);
            statement2.setString(1, propID);
            statement2.executeUpdate();

            ResultSet rs1 = c.createStatement().executeQuery(tenantQuery);

            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(rs1);
            return crs;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;



    }
    public static CachedRowSet deleteTenant(String name, String propID){
        try{
            String query = "DELETE FROM tenants WHERE name = ?";
            //ResultSet
            PreparedStatement statement = c.prepareStatement(query);

            statement.setString(1, name);
            statement.executeUpdate();

            PreparedStatement statement2 = c.prepareStatement(updateAvailable);
            statement2.setString(1, propID);
            statement2.executeUpdate();

            ResultSet rs2 = c.createStatement().executeQuery(tenantQuery);
            CachedRowSetImpl crsDeleteTenant = new CachedRowSetImpl();
            crsDeleteTenant.populate(rs2);
            return crsDeleteTenant;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static CachedRowSet updateTenant(String oldID, String name, double leaseLength, boolean rentPaid, String propID, String newEmail, String newPhone) {
        try {
            String query = "UPDATE tenants set name=?, leaseLength=?, rentPaid=?, propertyID=?, email=?, phone=? WHERE propertyID=?";
            //ResultSet
            PreparedStatement statement = c.prepareStatement(query);

            statement.setString(1, name);
            statement.setDouble(2, leaseLength);
            statement.setBoolean(3, rentPaid);
            statement.setString(4, propID);
            statement.setString(5, newEmail);
            statement.setString(6, newPhone);
            statement.setString(7, oldID);
            statement.executeUpdate();

            PreparedStatement statement2 = c.prepareStatement(updateAvailable);
            statement2.setString(1, propID);
            statement2.executeUpdate();
            PreparedStatement statement3 = c.prepareStatement(updateAvailable);
            statement3.setString(1, oldID);
            statement3.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery("SELECT name AS 'Name', leaseLength AS 'Lease Length', propertyID AS 'Propery ID'," +
                    "CASE WHEN rentPaid = 0 THEN 'Not Paid' " +
                    "ELSE 'Paid' END AS RentPaid, email AS 'Email', phone AS 'Phone'\n" +
                    "FROM tenants ORDER BY propertyID;");





            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(rs);
            return crs;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


        public static CachedRowSet populateDropdown(){

        try {

            ResultSet rs = c.createStatement().executeQuery("SELECT propertyID FROM allProperties WHERE propertyID NOT IN (SELECT propertyID FROM tenants);");
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(rs);
            return crs;

    } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}


