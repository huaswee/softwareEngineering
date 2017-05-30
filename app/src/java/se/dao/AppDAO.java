package se.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import se.entity.App;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.text.ParseException;
import se.entity.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class AppDAO {

    private static int appsInserted;
    private static HashMap<String, App> apps;
    private static TreeMap<Integer, ArrayList<String>> errorListApp;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  

    /**
     * Retrieve number of apps inserted into database
     *
     * @return int of number of apps
     */
    public static int getAppsInserted() {
        return appsInserted;
    }

    /**
     * Read the app.csv uploaded and insert into database
     *
     * @param brApp the BufferedReader containing inputStream to app.csv
     * @param function bootstrap or upload additional file
     *
     * @throws IOException If an input or output exception occurs
     *
     *
     */
    public static void readApp(BufferedReader brApp, String function) throws IOException {
        CSVReader reader = null;
        ArrayList<String> errors = null;
        HashMap<String, User> students = null;
        HashMap<Integer, AppLookup> appLookup = null;
        HashMap<String, App> appsFromDatabase = null;
        HashMap<String, Integer> successApps = null;

        try {
            // CSVWriter.DEFAULT_SEPARATOR is a comma, '\"' allows the escape of double quotes in the csv files
            reader = new CSVReader(brApp, CSVWriter.DEFAULT_SEPARATOR, '\"', 0);
            String[] nextLine;
            // reads in the header row
            String[] header = reader.readNext();

            // get all data from demographics table
            students = DemographicDAO.getAllUsersFromDatabase();

            // get all data from appLookup table
            appLookup = AppLookupDAO.getAllAppLookupFromDatabase();
            appsFromDatabase = new HashMap<String, App>();

            // contains the line number of rows without any error
            successApps = new HashMap<String, Integer>();
            if (function.equals("addFile")) {
                // if admin is adding additional file then we retrieve all data from app table to check for duplication
                // with database
                appsFromDatabase = getAllAppsFromDatabase();
            }

            // holds line number and the errors in that line
            errorListApp = new TreeMap<Integer, ArrayList<String>>();
            // holds all rows without any errors
            apps = new HashMap<String, App>();

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
                        errorListApp.put(lineCount, errors);
                    }
                }

                // if any field is empty, can skip the file specific validation below
                if (emptyField == false) {
                    boolean errorField = false;
                    Date timeStamp = null;
                    String hashedMAC = null;
                    int app_ID = 0;

                    for (int i = 0; i < nextLine.length; i++) {
                        if (header[i].equalsIgnoreCase("timestamp")) {

                            // check for correct timestamp format
                            try {
                                timeStamp = simpleDateFormat.parse(nextLine[i].trim());
                            } catch (ParseException e) {
                                errorField = true;
                                errors.add("invalid timestamp");
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("mac-address")) {
                            hashedMAC = nextLine[i].trim();

                            // check if macAddress is 40 characters long, 0-9, a-f and case-insensitive
                            if (!hashedMAC.matches("(?i)([a-f]|[0-9]){40}")) {
                                errorField = true;
                                errors.add("invalid mac address");
                            } else {
                                // if macAddress is correct format, check if it exists in demographics table
                                if (students.get(hashedMAC) == null) {
                                    errorField = true;
                                    errors.add("no matching mac address");
                                }
                            }
                            continue;
                        }
                        if (header[i].equalsIgnoreCase("app-id")) {
                            app_ID = Integer.parseInt(nextLine[i].trim());

                            // check if appId exists in appLookup table
                            if (appLookup.get(app_ID) == null) {
                                errorField = true;
                                errors.add("invalid app");
                            }
                            continue;
                        }
                    }

                    // none of the fields has error, check for duplicates
                    if (errorField == false) {
                        String key = hashedMAC + simpleDateFormat.format(timeStamp);

                        // for adding additional files: if there are 
                        // duplicates in database, discard those in files
                        if (appsFromDatabase.get(key) != null) {
                            errorField = true;
                            errors.add("duplicate row");
                            errorListApp.put(lineCount, errors);
                        } else {
                            // if database no duplicate or for bootstrap function,
                            // check against records in CSV files.
                            if (apps.get(key) != null) {
                                errorField = true;

                                // to use the latest copy if there are duplicates
                                // if duplicate means mac-address and timeStamp same,
                                // so only need to change appId
                                apps.get(key).setApp_id(app_ID);
                                errors.add("duplicate row");
                                errorListApp.put(successApps.get(key), errors);

                                // to update the lineCount of the latest successful app inserted
                                successApps.put(key, lineCount);
                            } else {
                                App app = new App(timeStamp, hashedMAC, app_ID);
                                apps.put(key, app);

                                // put the lineCount of successfully inserted so that if there are duplicated rows later,
                                // can retrieve the row to know which row is being duplicated
                                successApps.put(key, lineCount);
                                appsInserted++;
                            }
                        }

                    } else {
                        errorListApp.put(lineCount, errors);
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        // insert the correct rows into database
        insertAppData();
    }

    private static void insertAppData() {
        if (apps.isEmpty()) {
            System.out.println("empty list");
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "";

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            int appIndex = 0;
            int columnCount = 3;

            // number of rows to be inserted at once
            int batchSize = 20000;
            int batchCount = 0;
            int placeHolderIndex = 1;

            Iterator<String> iter = apps.keySet().iterator();

            // StringBuilder is mutable. When you call append(..) 
            // it alters the internal char array, rather than creating a new string object. More efficient
            StringBuilder builder = new StringBuilder("INSERT INTO app VALUES ");

            // this loop decides how many set of values eg: INSERT INTO app VALUES (timestamp, mac, appId), (timestamp2, mac2, appId2) etc
            for (int i = 0; i < apps.size(); i++) {

                // if its not the first set of value, append a , between each set
                if (appIndex != 0) {
                    builder.append(",");
                }
                builder.append("(");
                appIndex++;

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
                batchCount++;

                // check if the number of sets of values hit the batchSize or the last app in the list
                if (batchCount == batchSize || (i + 1) == apps.size()) {
                    // reset all the values for the next insert statement to be created
                    appIndex = 0;
                    batchCount = 0;
                    placeHolderIndex = 1;
                    sql = builder.toString();
                    stmt = conn.prepareStatement(sql);
                    builder = new StringBuilder("INSERT INTO app VALUES ");

                    while (iter.hasNext()) {
                        String appKey = iter.next();
                        App a = apps.get(appKey);

                        // convert java.util.date to java.sql.Timestamp to store in database
                        Timestamp dateTime = new Timestamp(a.getTimeStamp().getTime());

                        stmt.setTimestamp(placeHolderIndex, dateTime);
                        stmt.setString((placeHolderIndex + 1), a.getHashedMAC());
                        stmt.setInt((placeHolderIndex + 2), a.getApp_id());

                        placeHolderIndex += columnCount;
                        batchCount++;

                        // check if the number of sets of values hit the batchSize or the last app in the list
                        if (batchCount == batchSize || !iter.hasNext()) {
                            batchCount = 0;
                            stmt.executeUpdate();
                            break;
                        }
                    }

                }
            }

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
        String sql = "";

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            sql = "DELETE FROM app";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt);
        }
    }

    /**
     * Retrieve the errors in app.csv.
     *
     * @return TreeMap of ArrayList of errors and the corresponding line count
     * as the key. If there is no error, return null.
     */
    public static TreeMap<Integer, ArrayList<String>> getErrorListApp() {
        return errorListApp;
    }

    /**
     * Retrieve all Apps
     *
     * @return HashMap of App object with the concatenated String of mac-address
     * and timestamp as key. If there is no App, return null.
     */
    public static HashMap<String, App> getAllApps() {
        return apps;
    }

    /**
     * Retrieve all Apps from database
     *
     * @return HashMap of App object with the concatenated String of mac-address
     * and timestamp as key.
     */
    public static HashMap<String, App> getAllAppsFromDatabase() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        App app = null;
        HashMap<String, App> apps = new HashMap<String, App>();

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            sql = "SELECT * FROM app";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                //Retrieve by column name
                int app_ID = rs.getInt("app_id");
                String hashedMAC = rs.getString("mac_address");
                Date dateTime = new Date(rs.getTimestamp("timestamp").getTime());
                String key = hashedMAC + simpleDateFormat.format(dateTime);

                app = new App(dateTime, hashedMAC, app_ID);
                apps.put(key, app);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionPoolManager.close(conn, stmt, rs);
        }

        return apps;
    }

    /**
     * Clear the data in memory
     *
     */
    public static void clearMemory() {
        apps = null;
        appsInserted = 0; //tracking how many rows inserted into database
        errorListApp = null;
    }

    /**
     * Retrieve all Apps of the logged-in user from database
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param macAddress the macAddress of the logged-in user
     *
     * @return LinkedHashMap of App object with the app category as key.
     */
    public static LinkedHashMap<App, String> getLoggedInUserAppData(String startDate, String endDate, String macAddress) {
        LinkedHashMap<App, String> result = new LinkedHashMap<App, String>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            stmt = conn.prepareStatement("SELECT app.timestamp 'timestamp', app.app_id 'appid', app.mac_address 'macAddress', al.app_category 'category' "
                    + "FROM demographics as dm, app, applookup as al "
                    + "WHERE dm.mac_address = app.mac_address "
                    + "AND al.app_id = app.app_id "
                    + "AND app.timestamp >= ? AND app.timestamp <= ? "
                    + "AND dm.mac_address LIKE ? "
                    + "ORDER BY app.timestamp asc");

            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setString(3, macAddress);

            rs = stmt.executeQuery();

            while (rs.next()) {

                int appId = rs.getInt("appid");
                Date dateTime = new Date(rs.getTimestamp("timestamp").getTime());
                App app = new App(dateTime, macAddress, appId);
                result.put(app, rs.getString("category"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, stmt, rs);
            }
        }

        return result;
    }

    /**
     *
     * calls the getTotalUsers method with the school, gender, year, and cca set
     * to "null" string
     *
     * @param startDate start date
     * @param endDate end date
     * @return int of unique users that fall within the criteria stated, or 0 if
     * there is no unique users
     */
    public static int getTotalUsers(String startDate, String endDate) {
        int results = getTotalUsers(startDate, endDate, "null", "null", "null", "null");
        return results;
    }

    /**
     *
     * counts the number of unique users that fall in the criteria stated from
     * database
     *
     * @param startDate start date
     * @param endDate end date
     * @param school school sort
     * @param gender gender sort
     * @param year year sort
     * @param cca cca sort
     * @return an int of number of unique users
     */
    public static int getTotalUsers(String startDate, String endDate, String school, String gender, String year, String cca) {
        String sqlCount = "SELECT count(distinct mac_address) 'count' "
                + "FROM app "
                + "WHERE app.timestamp >= ? AND app.timestamp <= ? ";

        if (!year.equalsIgnoreCase("null")) {
            sqlCount += " AND dm.email LIKE '%" + year + "@%' ";
        }

        if (!gender.equalsIgnoreCase("null")) {
            sqlCount += " AND dm.gender LIKE '%" + gender + "%' ";
        }

        if (!school.equalsIgnoreCase("null")) {
            sqlCount += " AND dm.email LIKE '%@" + school + "%' ";
        }

        if (!cca.equalsIgnoreCase("null")) {
            sqlCount += " AND dm.cca = '" + cca + "' ";
        }

        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        int result = 0;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sqlCount);
            smt.setString(1, startDate);
            smt.setString(2, endDate);

            rs = smt.executeQuery();

            while (rs.next()) {
                result = rs.getInt("count");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, smt, rs);
            }
        }
        return result;
    }

    /**
     *
     * This method counts the number of unique users that fits the time given as
     * well as other parameters based on the users characteristics.
     *
     * @param startDate start date
     * @param endDate end date
     * @param firstSortType the first sort type
     * @param secondSortType the second sort type
     * @param thirdSortType the third sort type
     * @param fourthSortType the fourth sort type
     * @param firstSortValue the value of first sort
     * @param secondSortValue the value of second sort 
     * @param thirdSortValue the value of third sort
     * @param fourthSortValue the value of fourth sort
     * @return int of unique users that fit the criteria from database
     */
    public static int getSortTypeData(String startDate, String endDate, String firstSortType, String secondSortType, String thirdSortType, String fourthSortType,
            String firstSortValue, String secondSortValue, String thirdSortValue, String fourthSortValue) {
        int result = 0;
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;

        String sql = "SELECT count(distinct dm.email) 'count' "
                + "FROM demographics as dm, app "
                + "WHERE dm.mac_address = app.mac_address " + " AND app.timestamp >= ? AND app.timestamp <= ? ";

        String[][] sortData = new String[][]{{firstSortType, firstSortValue}, {secondSortType, secondSortValue}, {thirdSortType, thirdSortValue}, {fourthSortType, fourthSortValue}};

        for (String[] strArr : sortData) {
            String sortType = strArr[0];
            String sortValue = strArr[1];

            if (sortType.equalsIgnoreCase("year")) {
                sql += " AND dm.email LIKE '%" + sortValue + "%' ";
            }

            if (sortType.equalsIgnoreCase("gender")) {
                sql += " AND dm.gender LIKE '%" + sortValue + "%' ";
            }

            if (sortType.equalsIgnoreCase("school")) {
                sql += " AND dm.email LIKE '%" + sortValue + "%' ";
            }

            if (sortType.equalsIgnoreCase("cca")) {
                sql += " AND dm.cca = '" + sortValue + "' ";
            }

        }

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sql);
            smt.setString(1, startDate);
            smt.setString(2, endDate);

            rs = smt.executeQuery();

            while (rs.next()) {
                result = rs.getInt("count");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, smt, rs);
            }
        }

        return result;
    }

    /**
     *
     * this method returns a list of records that fit the criteria entered by
     * the user from the database.
     *
     * @param startDate startDate entered by the user
     * @param endDate endDate entered by the user
     * @param school school entered by the user
     * @param gender gender entered by the user
     * @param year year entered by the user
     * @param cca cca entered by the user
     * @return an ArrayList of String Arrays that contains the email and
     * timeStamp of every record from database, or an empty arrayList if there
     * is no record
     */
    public static ArrayList getUsageTime(String startDate, String endDate, String school, String gender, String year, String cca) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String sql = "SELECT dm.email 'email', app.timestamp 'timestamp' "
                + "FROM demographics as dm, app "
                + "WHERE dm.mac_address = app.mac_address "
                + "AND app.timestamp >= ? AND app.timestamp <= ? ";

        if (!school.equals("null")) {
            sql += " AND dm.email LIKE '%" + school + "%' ";
        }

        if (!year.equals("null")) {
            sql += " AND dm.email LIKE '%" + year + "%' ";
        }

        if (!gender.equals("null")) {
            sql += " AND dm.gender = '" + gender + "' ";
        }

        if (!cca.equals("null")) {
            sql += " AND dm.cca = '" + cca + "' ";
        }

        sql += " ORDER BY dm.email, app.timestamp asc";

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sql);
            smt.setString(1, startDate);
            smt.setString(2, endDate);
            rs = smt.executeQuery();

            if (!rs.isBeforeFirst()) {
                return result;

            } else {
                //Retrieve first resultSet
                while (rs.next()) {
                    result.add(new String[]{rs.getString("email"), rs.getString("timestamp")});
                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, smt, rs);
            }
        }
        return result;
    }

    /**
     *
     * this method gets a list of records from the database that fits the
     * criteria entered by the user
     *
     * @param startDate startDate entered by the user
     * @param endDate endDate entered by the user
     * @param appCat app Cateogry entered by the user
     * @return An ArrayList of String Arrays of mac Address, student name,
     * timestamp, and app category for every record from the database, or an
     * emptying ArrayList if there is no record
     */
    public static ArrayList getUsageTimeTopKStudents(String startDate, String endDate, String appCat) {
        ArrayList<String[]> results = new ArrayList<String[]>();

        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String sql = "SELECT dm.mac_address 'macAdd', dm.name 'name', app.timestamp 'timestamp', applookup.app_category 'appCat'"
                + "FROM demographics as dm, app, applookup "
                + "WHERE dm.mac_address = app.mac_address "
                + "AND app.app_id = applookup.app_id "
                + "AND app.timestamp >= ? AND app.timestamp <= ? "
                + "ORDER BY dm.email, app.timestamp asc";

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sql);
            smt.setString(1, startDate);
            smt.setString(2, endDate);
            rs = smt.executeQuery();

            while (rs.next()) {
                results.add(new String[]{rs.getString("macAdd"), rs.getString("name"), rs.getString("timestamp"), rs.getString("appCat")});
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
     *
     * this method gets a list of records from the database that fits the
     * criteria entered by the users
     *
     * @param startDate startDate entered by the user
     * @param endDate endDate entered by the user
     * @param school school entered by the user
     * @return an ArrayList of String Arrays with email, time stamp, and app
     * category for every record that fits the criteria, or an empty ArrayList
     * if there is no record
     */
    public static ArrayList getUsageTimeTopKSchool(String startDate, String endDate, String school) {
        ArrayList<String[]> results = new ArrayList<String[]>();
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String sql = "SELECT dm.email 'email', app.timestamp 'timestamp', applookup.app_category 'appCat' "
                + "FROM demographics as dm, app, applookup "
                + "WHERE dm.mac_address = app.mac_address "
                + "AND app.app_id = applookup.app_id "
                + "AND app.timestamp >= ? AND app.timestamp <= ? "
                + "AND dm.email LIKE '%" + school + "%' "
                + "ORDER BY dm.email, app.timestamp asc";

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sql);
            smt.setString(1, startDate);
            smt.setString(2, endDate);
            rs = smt.executeQuery();

            while (rs.next()) {
                results.add(new String[]{rs.getString("email"), rs.getString("timestamp"), rs.getString("appCat")});
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
     *
     * this method gets a list of records that fit the criteris entered by the
     * user from the database
     *
     * @param startDate startDate entered by the user
     * @param endDate endDate entered by the user
     * @param school school entered by the user
     * @return an ArrayList of String Arrays of email, timestmap, app id, and
     * app names from every record, else an empty arrayList if there is no
     * record
     */
    public static ArrayList getUsageTimeTopKApp(String startDate, String endDate, String school) {
        ArrayList<String[]> results = new ArrayList<String[]>();
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String sql = "SELECT dm.email 'email', app.timestamp 'timestamp', app.app_id 'appID', applookup.app_name 'appName' "
                + "FROM demographics as dm, app, applookup "
                + "WHERE dm.mac_address = app.mac_address "
                + "AND app.app_id = applookup.app_id "
                + "AND app.timestamp >= ? AND app.timestamp <= ? "
                + "AND dm.email LIKE '%" + school + "%' "
                + "ORDER BY dm.email, app.timestamp asc";

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sql);
            smt.setString(1, startDate);
            smt.setString(2, endDate);
            rs = smt.executeQuery();
            while (rs.next()) {
                results.add(new String[]{rs.getString("email"), rs.getString("timestamp"), rs.getString("appID"), rs.getString("appName")});
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
     *
     * this method gets a particular student name from a mac address
     *
     * @param mac mac address of the user
     * @return a string that is the user's name if the mac address exists in the
     * database, else an empty String
     */
    public static String getUserNameFromEmail(String mac) {

        String userName = "";

        String sqlCount = "SELECT name "
                + "FROM demographics "
                + "WHERE mac_address = ?";

        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            smt = conn.prepareStatement(sqlCount);
            smt.setString(1, mac);

            rs = smt.executeQuery();

            while (rs.next()) {
                userName = rs.getString("name");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionPoolManager.close(conn, smt, rs);
            }
        }
        return userName;

    }

}
