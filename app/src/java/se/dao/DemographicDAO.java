package se.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import se.entity.User;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class DemographicDAO {

    private static HashMap<String, User> users;
    private static TreeMap<Integer, ArrayList<String>> errorListDemo;

    /**
     * read the demographics.csv uploaded and insert into database
     * 
     * @param brDemo the BufferedReader containing inputStream to
     * demographics.csv
     * @throws IOException If an input or output exception occurs
     */
    public static void readDemographic(BufferedReader brDemo) throws IOException {
        CSVReader reader = null;
        ArrayList<String> errors = null;

        try {
            // CSVWriter.DEFAULT_SEPARATOR is a comma, '\"' allows the escape of double quotes in the csv files
            reader = new CSVReader(brDemo, CSVWriter.DEFAULT_SEPARATOR, '\"', 0);
            String[] nextLine;
            
            // reads in the header row
            String[] header = reader.readNext();

            // holds all rows without any errors
            users = new HashMap<String, User>();
            
            // holds line number and the errors in that line
            errorListDemo = new TreeMap<Integer, ArrayList<String>>();
            
            // start from 1 to skip header row
            int lineCount = 1;

            while ((nextLine = reader.readNext()) != null) {
                lineCount++;
                
                // stores all errors within a particular row
                errors = new ArrayList<String>();
                boolean emptyField = false;
                for (int i = 0; i < nextLine.length; i++) {
                    String value = nextLine[i].trim();
                    if (value.equals("")) {
                        errors.add("blank " + header[i]);
                        
                        // if any field is empty, set this to true
                        emptyField = true;
                        errorListDemo.put(lineCount, errors);
                    }
                }
                
                // if any field is empty, can skip the file specific validation below
                if (emptyField == false) {
                    boolean errorField = false;
                    String hashedMAC = null;
                    String name = null;
                    String password = null;
                    String email = null;
                    String gender = null;
                    String cca = "";

                    for (int i = 0; i < nextLine.length; i++) {
                        if (header[i].equalsIgnoreCase("mac-address")) {
                            hashedMAC = nextLine[i].trim();
                            // check if macAddress is 40 characters long, 0-9, a-f and case-insensitive
                            if (!hashedMAC.matches("(?i)([a-f]|[0-9]){40}")) {
                                errorField = true;
                                errors.add("invalid mac address");
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("name")) {
                            name = nextLine[i].trim();
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("password")) {
                            password = nextLine[i].trim();
                            
                            // check if there is a space within the password or the length is less than 8
                            if (password.contains(" ") || password.length() < 8) {
                                errorField = true;
                                errors.add("invalid password");
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("gender")) {
                            gender = nextLine[i].trim();
                            
                            // check if gender is m or f, case-insensitive
                            if (!gender.matches("(?i)(M|F)")) {
                                errorField = true;
                                errors.add("invalid gender");
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("email")) {
                            email = nextLine[i].trim();
                            
                            // check if email follows the following format
                            if (!email.matches("[\\w|.]+.201[1-5]@(business|accountancy|sis|economics|law|socsc).smu.edu.sg")) {
                                errorField = true;
                                errors.add("invalid email");
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("cca")) {
                            cca = nextLine[i].trim();
                            
                            // check if number of characters if more than 63
                            if (cca.length() > 63) {
                                errorField = true;
                                errors.add("CCA record too long");
                            }
                            continue;
                        }

                    }
                    
                    if (errorField == false) {
                        User student = new User(hashedMAC, name, password, email, gender, cca);
                        users.put(hashedMAC, student);
                    } else {
                        errorListDemo.put(lineCount, errors);
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        insertUserData();
    }

    private static void insertUserData() {
        if (users.isEmpty()) {
            System.out.println("empty list");
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = null;
        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            int columnCount = 6;
            
            // StringBuilder is mutable. When you call append(..) 
            // it alters the internal char array, rather than creating a new string object. More efficient
            StringBuilder builder = new StringBuilder("INSERT INTO demographics VALUES "); 
            
            // this loop decides how many set of values 
            for (int i = 0; i < users.size(); i++) {
                
                // if its not the first set of value, append a , between each set
                if (i != 0) { 
                    builder.append(",");
                }
                builder.append("(");

                for (int k = 0; k < columnCount; k++) {
                    
                    // if its not the first value in a set, append a , between each value
                    if (k != 0) { 
                        builder.append(",");
                    }
                    
                    // placeholder for the preparedStatement
                    builder.append("?"); 
                }
                builder.append(")");
            }

            sql = builder.toString();
            stmt = conn.prepareStatement(sql);

            int placeHolderIndex = 1;
            Iterator<String> iter = users.keySet().iterator();
            while (iter.hasNext()) {
                String demoKey = iter.next();
                User u = users.get(demoKey);

                stmt.setString(placeHolderIndex, u.getHashedMAC());
                stmt.setString((placeHolderIndex + 1), u.getUsername());
                stmt.setString((placeHolderIndex + 2), u.getPassword());
                stmt.setString((placeHolderIndex + 3), u.getEmail());
                stmt.setString((placeHolderIndex + 4), u.getGender());
                stmt.setString((placeHolderIndex + 5), u.getCca());

                placeHolderIndex += columnCount;
            }

            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt);
        }
    }

    /**
     * Retrieve a User object given its email.
     *
     * @param email the email of the User to be retrieved.
     * @return User object. If there is no Student with the given email, return
     * null.
     */
    public static User getUser(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        User result = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();

            sql = "SELECT * FROM demographics WHERE email LIKE '" + email + "%'";
            stmt = conn.prepareStatement(sql);
            //stmt.setString(1, email);

            rs = stmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                String hashedMAC = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String gender = rs.getString("gender");
                String realEmail = rs.getString("email");
                String cca = rs.getString("cca");

                result = new User(hashedMAC, name, password, realEmail, gender, cca);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt, rs);
        }
        return result;
    }

    /**
     * Retrieve a User object given its macAddress.
     * 
     * @param mac macAddress of user
     * @return user Object if exist in database, else null
     */
    public static User getUserByMac(String mac) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        User result = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();

            sql = "SELECT * FROM demographics WHERE mac_address LIKE '" + mac + "%'";
            stmt = conn.prepareStatement(sql);
            //stmt.setString(1, email);

            rs = stmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                String hashedMAC = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String gender = rs.getString("gender");
                String realEmail = rs.getString("email");
                String cca = rs.getString("cca");

                result = new User(hashedMAC, name, password, realEmail, gender, cca);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt, rs);
        }
        return result;
    }

    /**
     * Clear the data in database
     *
     */
    public static void clearData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();

            sql = "DELETE FROM demographics";
            stmt = conn.prepareStatement(sql);

            stmt.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt);
        }
    }

    /**
     * Retrieve the errors in demographic.csv.
     *
     * @return TreeMap of ArrayList of errors and the corresponding line count
     * as the key. If there is no error, return null.
     */
    public static TreeMap<Integer, ArrayList<String>> getErrorListDemographics() {
        return errorListDemo;
    }

    /**
     * Retrieve all Users
     *
     * @return HashMap of User object with the mac-address as key. If there is
     * no User, return null.
     */
    public static HashMap<String, User> getAllUsers() {

        return users;
    }

    /**
     * Retrieve all Users from database
     *
     * @return HashMap of User object with the mac-address as key.
     */
    public static HashMap<String, User> getAllUsersFromDatabase() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        User user = null;
        HashMap<String, User> users = new HashMap<String, User>();

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();

            sql = "SELECT * FROM demographics";
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                String hashedMAC = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String gender = rs.getString("gender");
                String cca = rs.getString("cca");

                user = new User(hashedMAC, name, password, email, gender, cca);
                users.put(hashedMAC, user);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt, rs);
        }

        return users;
    }

    /**
     * Retrieves all unique users within the date range
     * 
     * @param startDate start date
     * @param endDate end date
     * @return a HashMap users with macAddress as key, else null if the query returns 0
     * records
     */
    public static HashMap<String, User> getAllUniqueUsers(String startDate, String endDate) {
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        HashMap<String, User> results = new HashMap<String, User>();

        try {

            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement("SELECT distinct dm.email 'email', dm.mac_address 'mac_address', dm.name 'name', dm.password 'password', dm.gender 'gender', dm.cca 'cca' "
                    + "FROM demographics as dm, app WHERE "
                    + "dm.mac_address = app.mac_address");
            rs = smt.executeQuery();

            while (rs.next()) {
                String email = rs.getString("email");
                String mac = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String gender = rs.getString("gender");
                String cca = rs.getString("cca");

                results.put(mac, new User(mac, name, password, email, gender, cca));

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, smt, rs);
            }
        }

        return results;
    }

    /**
     * Clear the data in memory
     *
     */
    public static void clearMemory() {
        users = null;
        errorListDemo = null;
    }

    /**
     * Retrieves all unique CCAs
     *
     * @param isJson whether the method call is from json or web servlet
     * @return all unique CCAs
     */
    public static String[] getAllCCAs(boolean isJson) {
        String[] results = null;
        ArrayList<String> record = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String sql = "SELECT distinct cca from demographics ";
        if (isJson) {
            sql += "ORDER BY cca desc";
        } else {
            sql += "ORDER BY cca asc";
        }

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sql);
            rs = smt.executeQuery();
            while (rs.next()) {
                record.add(rs.getString("cca"));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, smt, rs);
            }
        }

        results = new String[record.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = record.get(i);
        }

        return results;
    }

}
