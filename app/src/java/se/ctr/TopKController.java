package se.ctr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import se.dao.AppDAO;
import se.dao.DemographicDAO;
import se.entity.App;
import se.entity.User;

public class TopKController {

    static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final String[] years = new String[]{"2011", "2012", "2013", "2014", "2015"};
    static final String[] genders = new String[]{"M", "F"};
    static final String[] schools = new String[]{"socsc", "sis", "law", "economics", "business", "accountancy"};

    /**
     *
     * This method analyzes the data from database and calculates the total
     * usageTime for all users from every school before returning the ranks up
     * till the k value.
     *
     * @param startDate startDate entered by user
     * @param endDate endDate entered by user
     * @param appCat app Category selected by user
     * @param k rankings to show
     * @return ArrayList of String Arrays with each array containing a school
     * and its total usageTime, else an empty arrayList if there is no records
     */
    public static ArrayList<String[]> topKSchools(String startDate, String endDate, String appCat, int k) {
        ArrayList<String[]> results = new ArrayList<String[]>();
        ArrayList<String[]> unsortedResults = new ArrayList<String[]>();

        Date d1 = null;
        Date d2 = null;

        for (String school : schools) {

            try {
                d1 = format.parse(startDate);
                d2 = format.parse(endDate);
                //grabs a list of all records from database of users belonging to a particular school
                ArrayList<String[]> records = AppDAO.getUsageTimeTopKSchool(startDate, endDate, school);
                if (records == null || records.isEmpty()) {
                    continue;
                }
                // adding one second to the endDate's timestamp to get the next day with the timestamp 00:00:00
                records.add(new String[]{"exit", format.format(d2.getTime() + 1000), "exit"});
                Iterator<String[]> iter = records.iterator();
                int usageTime = 0;

                //grabs the first list in the record
                String[] compareRecord = iter.next();
                String compareEmail = compareRecord[0];
                Date compareTimeStamp = format.parse(compareRecord[1]);
                String compareStartDayStr = format.format(compareTimeStamp);

                //add the timestamp 00:00:00 to compareStartDAY to get the time right from the start of the day
                Date compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");
                String compareAppCategory = compareRecord[2];

                while (iter.hasNext()) {
                    //if the record does not belong to the app category, move on to the next app
                    if (!compareAppCategory.equalsIgnoreCase(appCat)) {
                        compareRecord = iter.next();
                        compareEmail = compareRecord[0];
                        compareTimeStamp = format.parse(compareRecord[1]);
                        compareStartDayStr = format.format(compareTimeStamp);
                        //add the timestamp 00:00:00 to compareStartDAY to get the time right from the start of the day
                        compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");
                        compareAppCategory = compareRecord[2];
                        continue;
                    }

                    //grabs next record in the list
                    String[] record = iter.next();
                    String email = record[0];
                    Date timeStamp = format.parse(record[1]);
                    String startDayStr = format.format(timeStamp);
                    Date startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");
                    String appCategory = record[2];
                    long a = 0;
                    long b = 0;

                    if (email.equals(compareEmail)) {
                        //check if the two apps are used by the same email 
                        if (compareStartDay.getTime() - startDay.getTime() == 0) {
                            //check if the two apps are from the same day
                            a = timeStamp.getTime() / 1000;
                            b = compareTimeStamp.getTime() / 1000;

                        } else {
                            // the two apps are not used in the same day
                            a = compareStartDay.getTime() / 1000 + 86400;
                            b = compareTimeStamp.getTime() / 1000;

                        }
                    } else {
                        /*
                         the two apps are used by different users
                         in order to calculate the last app that is used by one user, 
                         we added one day worth of time (86400) to the compareStartDay
                         */
                        a = compareStartDay.getTime() / 1000 + 86400;
                        b = compareTimeStamp.getTime() / 1000;
                    }

                    //calcualtes difference based on parameters
                    long diff = a - b;

                    if (diff <= 120) {
                        usageTime += diff;

                    } else {
                        // if more than 120 secs, add 10 seconds only
                        usageTime += 10;

                    }
                    //sets new record to compare with
                    compareEmail = email;
                    compareTimeStamp = timeStamp;
                    compareStartDayStr = startDayStr;
                    compareStartDay = startDay;
                    compareAppCategory = appCategory;

                }

                //adds them to result
                unsortedResults.add(new String[]{school, String.valueOf(usageTime)});

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

        if (unsortedResults == null || unsortedResults.isEmpty()) {
            return unsortedResults;
        }

        //sorts the results in order
        unsortedResults = topKSort(unsortedResults, "");
        unsortedResults.add(new String[]{"exit", "0", "testMac"});
        Iterator<String[]> iter = unsortedResults.iterator();
        int rankCounter = 1;

        //grabs the first record from result
        String[] compareArr = iter.next();

        int compareUsageTime = Integer.parseInt(compareArr[1]);

        while (iter.hasNext()) {
            String[] arr = iter.next();
            int usageTime = Integer.parseInt(arr[1]);
            String[] strArray = compareArr[0].split(",");
            // sort those in same rank by accending order
            Arrays.sort(strArray);

            compareArr[0] = "";
            for (int i = 0; i < strArray.length; i++) {
                if (compareArr[0].equals("")) {
                    compareArr[0] += strArray[i].trim();
                } else {
                    compareArr[0] += "," + strArray[i].trim();
                }
            }

            //if the next record has lesser usageTime than previous record and ranking not at k value
            if (usageTime < compareUsageTime && rankCounter <= k) {
                //add record to final return result
                results.add(new String[]{String.valueOf(rankCounter), compareArr[0], compareArr[1]});
                rankCounter += strArray.length;
                compareUsageTime = usageTime;
                compareArr = arr;
            }

        }

        return results;
    }

    /**
     *
     * This method calculates a list of students that fits the criteria given
     * the user and sorts it by rank and k value.
     *
     * @param startDate start date
     * @param endDate end date
     * @param appCat app category
     * @param k number of top results to show
     * @return ArrayList of String Array with each array containing the student
     * email, student mac address, and its usageTime, else an empty arrayList
     */
    public static ArrayList<String[]> topKStudents(String startDate, String endDate, String appCat, int k) {
        ArrayList<String[]> results = new ArrayList<String[]>();
        ArrayList<String[]> sortedResults = new ArrayList<String[]>();
        ArrayList<String[]> sortedResultsWithTopK = new ArrayList<String[]>();

        //gets list of all unqiue users in the timeLine
        HashMap<String, User> uniqMacAddList = DemographicDAO.getAllUniqueUsers(startDate, endDate);
        for (String mac : uniqMacAddList.keySet()) {
            int usageTime = 0;
            //Retrieve email for every user

            LinkedHashMap<App, String> appData = AppDAO.getLoggedInUserAppData(startDate, endDate, mac);
            if (appData == null || appData.isEmpty()) {
                continue;
            }

            //Created temporary app object as stopper
            try {
                appData.put(new App(new Date(format.parse(endDate).getTime() + 1000), "000000", 0), "temp");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Iterating through every keySet
            Iterator<App> iter = appData.keySet().iterator();

            App compareApp = iter.next();
            Date compareTimeStamp = compareApp.getTimeStamp();
            String compareCat = appData.get(compareApp);

            String compareStartDayStr = format.format(compareApp.getTimeStamp());
            Date compareStartDay = null;
            try {
                compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (iter.hasNext()) {
                App app = iter.next();
                Date timeStamp = app.getTimeStamp();
                String cat = appData.get(app);
                String startDayStr = format.format(app.getTimeStamp());
                Date startDay = null;
                try {
                    startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long a = 0;
                long b = 0;

                if (compareStartDay.getTime() - startDay.getTime() == 0) {
                    a = timeStamp.getTime() / 1000;
                    b = compareTimeStamp.getTime() / 1000;

                } else {
                    a = compareStartDay.getTime() / 1000 + 86400;
                    b = compareTimeStamp.getTime() / 1000;

                }

                long diff = a - b;
                if (compareCat.equalsIgnoreCase(appCat)) {

                    if (diff <= 120) {
                        usageTime += diff;

                    } else {
                        // if more than 120 secs then add 10 secs only
                        usageTime += 10;
                    }
                }

                compareApp = app;
                compareTimeStamp = timeStamp;
                compareCat = cat;
                compareStartDay = startDay;

            }

            User u = uniqMacAddList.get(mac);

            String userName = u.getUsername();
            String[] userData = new String[3];

            userData[0] = userName;
            userData[2] = mac;
            userData[1] = String.valueOf(usageTime);

            if (usageTime > 0) {
                results.add(userData);
            }

        }

        if (results == null || results.isEmpty()) {
            return results;
        }

        //sorts into ranks
        sortedResults = topKSort(results, "topKStudents");
        sortedResults.add(new String[]{"exit", "0", "testMac"});

        Iterator<String[]> iter = sortedResults.iterator();
        int rankCounter = 1;
        String[] compareArr = iter.next();

        int compareUsageTime = Integer.parseInt(compareArr[1]);

        while (iter.hasNext()) {
            String[] arr = iter.next();
            int usageTime = Integer.parseInt(arr[1]);
            String[] strArray = compareArr[0].split(",");
            String[] strMacArr = compareArr[2].split(",");
            String[] finalArr = new String[strArray.length];
            for (int i = 0; i < finalArr.length; i++) {
                // concatenate the name and macAddress together so that after sorting it will still correspond 
                finalArr[i] = strArray[i] + ":" + strMacArr[i];
            }
            // sort those in same rank by accending order
            Arrays.sort(finalArr);
            compareArr[0] = "";
            compareArr[2] = "";
            for (int i = 0; i < finalArr.length; i++) {
                String[] split = finalArr[i].split(":");
                if (compareArr[0].equals("")) {
                    compareArr[0] += split[0].trim();
                    compareArr[2] += split[1].trim();
                } else {
                    compareArr[0] += "," + split[0].trim();
                    compareArr[2] += "," + split[1].trim();
                }
            }

            if (usageTime < compareUsageTime && rankCounter <= k) {
                sortedResultsWithTopK.add(new String[]{String.valueOf(rankCounter), compareArr[0], compareArr[2], compareArr[1]});
                rankCounter += strArray.length;
                compareUsageTime = usageTime;
                compareArr = arr;
            }

        }
        return sortedResultsWithTopK;
    }

    /**
     * This Method calculates a list of apps and their usageTime based on users
     * criteria and then sorts them based on rank and k value
     *
     * @param startDate Start Date entered by the user
     * @param endDate End Date entered by the user
     * @param school the school the user wants the data to be sorted by
     * @param k how many rows the user wants to see
     * @return an ArrayList of Array of results
     */
    public static ArrayList topKApps(String startDate, String endDate, String school, int k) {
        ArrayList<String[]> results = new ArrayList<String[]>();

        ArrayList<String[]> sortedResults = new ArrayList<String[]>();
        ArrayList<String[]> sortedResultsWithTopK = new ArrayList<String[]>();
        HashMap<String, Long> appUsage = new HashMap<String, Long>();

        //grabs all records within that timeLine and in the school
        ArrayList<String[]> uniqueSchookAppRecords = AppDAO.getUsageTimeTopKApp(startDate, endDate, school);
        try {
            uniqueSchookAppRecords.add(new String[]{"", format.format(new Date(format.parse(endDate).getTime() + 1000)), "0", "exit"});
        } catch (Exception e) {
            e.printStackTrace();
        }

        //gets first record
        Iterator<String[]> iter = uniqueSchookAppRecords.iterator();
        String[] compareAppRecord = iter.next();
        while (iter.hasNext()) {
            String[] appRecord = iter.next();

            Date compareTimeStamp = null;
            Date timeStamp = null;
            Date compareStartDay = null;
            Date startDay = null;

            try {
                //gets second record for comparison
                compareTimeStamp = format.parse(compareAppRecord[1]);
                timeStamp = format.parse(appRecord[1]);
                compareStartDay = format.parse(compareAppRecord[1].substring(0, compareAppRecord[1].indexOf(" ")) + " 00:00:00");
                startDay = format.parse(appRecord[1].substring(0, appRecord[1].indexOf(" ")) + " 00:00:00");
            } catch (Exception e) {
                e.printStackTrace();
            }

            long a = 0;
            long b = 0;

            if (!compareAppRecord[0].equals(appRecord[0])) { //checking the email to see if timestamp belongs to same user
                a = compareStartDay.getTime() / 1000 + 86400;
                b = compareTimeStamp.getTime() / 1000;
            } else {
                if (compareStartDay.getTime() - startDay.getTime() == 0) {

                    a = timeStamp.getTime() / 1000;
                    b = compareTimeStamp.getTime() / 1000;

                } else {

                    a = compareStartDay.getTime() / 1000 + 86400;
                    b = compareTimeStamp.getTime() / 1000;

                }
            }

            //calculates the difference
            long diff = a - b;

            if (diff <= 120) {

                Long appUsageTime = appUsage.get(compareAppRecord[3]);
                if (appUsageTime != null) {
                    // add on the diff to the app category
                    appUsage.put(compareAppRecord[3], (diff + appUsageTime));
                } else {
                    // if this is the first record for the particular category, just add the diff
                    appUsage.put(compareAppRecord[3], diff);
                }

            } else {
                // more than 120 secs then add 10 secs only
                Long appUsageTime = appUsage.get(compareAppRecord[3]);
                if (appUsageTime != null) {
                    // add on the diff to the app category
                    appUsage.put(compareAppRecord[3], (10 + appUsageTime));
                } else {
                    // if this is the first record for the particular category, just add the diff
                    appUsage.put(compareAppRecord[3], 10L);
                }

            }

            compareAppRecord = appRecord;
        }
        for (String name : appUsage.keySet()) {
            results.add(new String[]{name, String.valueOf(appUsage.get(name))});
        }

        if (results == null || results.isEmpty()) {
            return results;
        }

        //sorts the results
        sortedResults = topKSort(results, "");
        sortedResults.add(new String[]{"exit", "0"});

        //gets first record
        Iterator<String[]> iterResults = sortedResults.iterator();
        int rankCounter = 1;
        String[] compareArr = iterResults.next();

        int compareUsageTime = Integer.parseInt(compareArr[1]);

        while (iterResults.hasNext()) {

            String[] arr = iterResults.next();
            int usageTime = Integer.parseInt(arr[1]);
            String[] strArray = compareArr[0].split(",");
            
            // sort those in same rank by accending order
            Arrays.sort(strArray);
            compareArr[0] = "";
            for (int i = 0; i < strArray.length; i++) {
                if (compareArr[0].equals("")) {
                    compareArr[0] += strArray[i].trim();
                } else {
                    compareArr[0] += "," + strArray[i].trim();
                }
            }

            //if usageTime is smaller than previous record and the ranking is not higher than k value
            if (usageTime < compareUsageTime && rankCounter <= k) {
                sortedResultsWithTopK.add(new String[]{String.valueOf(rankCounter), compareArr[0], compareArr[1]});
                rankCounter += strArray.length;
                compareUsageTime = usageTime;
                compareArr = arr;
            }

        }

        return sortedResultsWithTopK;
    }

    /**
     * This Method sorts the ArrayList by usageTime into proper ranks
     *
     * @param unsort ArrayList to be sorted
     * @param sortType Which function is calling the sort
     * @return an ArrayList<String []> which is the sorted copy of the input
     * ArrayList
     */
    private static ArrayList topKSort(ArrayList<String[]> unsort, String sortType) {

        for (int i = 1; i < unsort.size(); i++) {
            String[] tempArr = unsort.get(i);
            int temp = Integer.parseInt(tempArr[1]);
            int j;

            for (j = i - 1; j >= 0 && temp >= Integer.parseInt(unsort.get(j)[1]); j--) {
                if (temp > Integer.parseInt(unsort.get(j)[1])) {
                    String[] test = unsort.get(j);

                    unsort.remove(j);
                    unsort.add(j + 1, test);
                } else if (temp == Integer.parseInt(unsort.get(j)[1])) {
                    String[] test = unsort.get(j);

                    test[0] = test[0] + "," + tempArr[0];
                    if (sortType.equals("topKStudents")) {
                        test[2] = test[2] + "," + tempArr[2];
                    }

                    unsort.remove(j);

                    unsort.add(j, test);

                }
            }

        }

        return unsort;

    }
}
