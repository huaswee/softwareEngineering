package se.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import se.entity.AppLookup;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class AppLookupDAO {

    private static HashMap<Integer, AppLookup> appLookups;
    private static TreeMap<Integer, ArrayList<String>> errorListAppLookup;

    /**
     * read the appLookup.csv uploaded and insert into database
     *
     * @param brAppLookup the BufferedReader containing inputStream to
     * appLookup.csv
     * @throws IOException If an input or output exception occurs
     */
    public static void readAppLookup(BufferedReader brAppLookup) throws IOException {
        CSVReader reader = null;
        ArrayList<String> errors = null;

        try {
            // CSVWriter.DEFAULT_SEPARATOR is a comma, '\"' allows the escape of double quotes in the csv files
            reader = new CSVReader(brAppLookup, CSVWriter.DEFAULT_SEPARATOR, '\"', 0);
            String[] nextLine;

            // reads in the header row
            String[] header = reader.readNext();

            // holds line number and the errors in that line
            errorListAppLookup = new TreeMap<Integer, ArrayList<String>>();

            // holds all rows without any errors
            appLookups = new HashMap<Integer, AppLookup>();

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
                        errorListAppLookup.put(lineCount, errors);
                    }
                }

                // if any field is empty, can skip the file specific validation below
                if (emptyField == false) {
                    boolean errorField = false;
                    int app_ID = 0;
                    String app_Name = null;
                    String app_Category = null;

                    for (int i = 0; i < nextLine.length; i++) {
                        if (header[i].equalsIgnoreCase("app-id")) {
                            app_ID = Integer.parseInt(nextLine[i].trim());

                            // check if appId is greater than 0
                            if (app_ID <= 0) {
                                errorField = true;
                                errors.add("invalid app id");
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("app-name")) {
                            app_Name = nextLine[i].trim();
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("app-category")) {
                            app_Category = nextLine[i].trim();

                            // check is app category belongs to any of the given categories, case-insensitive
                            if (!app_Category.matches("(?i)Books|Social|Education|Entertainment|Information|Library|Local|Tools|Fitness|Games|Others")) {
                                errorField = true;
                                errors.add("invalid app category");
                            }
                            continue;
                        }
                    }

                    // if there are no errors in that particular row
                    if (errorField == false) {
                        AppLookup appLookup = new AppLookup(app_ID, app_Name, app_Category);
                        appLookups.put(app_ID, appLookup);
                    } else {
                        errorListAppLookup.put(lineCount, errors);
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        // insert the correct rows into database
        insertAppLookupData();
    }

    private static void insertAppLookupData() {
        if (appLookups.isEmpty()) {
            System.out.println("empty list");
            return;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            int columnCount = 3;

            // StringBuilder is mutable. When you call append(..) 
            // it alters the internal char array, rather than creating a new string object. More efficient
            StringBuilder builder = new StringBuilder("INSERT INTO applookup VALUES ");

            // this loop decides how many set of values eg: INSERT INTO applookup VALUES (appId, appName, appCategory), (appId2, addName2, appCategory2) etc
            for (int i = 0; i < appLookups.size(); i++) {
                
                // if its not the first set of value, append a , between each set
                if (i != 0) {
                    builder.append(",");
                }
                builder.append("(");

                // this loop decides how many values in a set depending on the number of columns
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
            Iterator<Integer> iter = appLookups.keySet().iterator();
            while (iter.hasNext()) {
                int appLookupKey = iter.next();
                AppLookup a = appLookups.get(appLookupKey);

                stmt.setInt(placeHolderIndex, a.getApp_id());
                stmt.setNString((placeHolderIndex + 1), a.getApp_name());
                stmt.setString((placeHolderIndex + 2), a.getApp_category());

                placeHolderIndex += columnCount;
            }

            stmt.executeUpdate();
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt);
        }
    }

    /**
     * Clears data in database
     *
     */
    public static void clearData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            sql = "DELETE FROM applookup";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt);
        }
    }

    /**
     * Retrieve the errors in app-lookup.csv.
     *
     * @return TreeMap of ArrayList of errors and the corresponding line count
     * as the key. If there is no error, return null.
     */
    public static TreeMap<Integer, ArrayList<String>> getErrorListAppLookup() {
        return errorListAppLookup;
    }

    /**
     * Retrieve all AppLookups
     *
     * @return HashMap of AppLookup object with the app-id as key. If there is
     * no Applookup, return null.
     */
    public static HashMap<Integer, AppLookup> getAllAppLookup() {
        return appLookups;
    }

    /**
     * Retrieve all AppLookups from database
     *
     * @return HashMap of AppLookup object with the app-id as key.
     */
    public static HashMap<Integer, AppLookup> getAllAppLookupFromDatabase() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        AppLookup appLookup = null;
        appLookups = new HashMap<Integer, AppLookup>();

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();

            sql = "SELECT * FROM applookup";
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                int app_ID = rs.getInt("app_id");
                String app_Category = rs.getString("app_category");
                String app_Name = rs.getString("app_name");

                appLookup = new AppLookup(app_ID, app_Name, app_Category);
                appLookups.put(app_ID, appLookup);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt, rs);
        }

        return appLookups;

    }

    /**
     * Clear the data in memory
     *
     */
    public static void clearMemory() {
        appLookups = null;
        errorListAppLookup = null;
    }

    /**
     * Retrieves all apps belonging to users belonging to the particular school
     * 
     * @param startDate start date of the apps
     * @param endDate end date of the apps
     * @param school the school entered by user
     * @return ArrayList of apps
     */
    public static ArrayList getUniqueAppsFromSchool(String startDate, String endDate, String school) {
        ArrayList<String> results = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement("SELECT distinct applookup.app_name 'appName' "
                    + "FROM demographics as dm, app, applookup "
                    + "WHERE dm.mac_address = app.mac_address "
                    + "AND app.app_id = applookup.app_id "
                    + "AND app.timestamp >= ? AND app.timestamp <= ? "
                    + "AND dm.email LIKE '%" + school + "%' "
                    + "ORDER BY applookup.app_name asc");

            smt.setString(1, startDate);
            smt.setString(2, endDate);
            rs = smt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString("appName"));
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
}
