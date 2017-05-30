package se.ctr;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import se.entity.App;

public class SmartphoneOveruseController {

    /**
     * Calculates the average daily usage time, average daily gaming duration
     * and the smartphone access frequency of the logged in user
     *
     * @param start the start date
     * @param end the end date
     * @param appData all the appData of the logged in user within the specific
     * dates
     * @return results String 2D Array
     */
    public String[][] smartphoneOveruseReport(Date start, Date end, LinkedHashMap<App, String> appData) {
        Date exitTimestamp = new Date(end.getTime() + (1 * 1000));
        App exitApp = new App(exitTimestamp, "000000", 0);
        appData.put(exitApp, "exit");

        int numOfDays = (int) (end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000) + 1;
        int numOfDaysInHours = numOfDays * 24;

        Iterator<App> iter = appData.keySet().iterator();
        //first app in the linkedHashMap
        App compareApp = iter.next();
        Date compareTimeStamp = compareApp.getTimeStamp();
        String category1 = appData.get(compareApp);
        double usageTime = 0.0;
        double gamingUsageTime = 0.0;
        double frequency = 0.0;

        while (iter.hasNext()) {
            //second app onwards
            App app = iter.next();
            Date timeStamp = app.getTimeStamp();
            String category2 = appData.get(app);

            long diffInHours = (timeStamp.getTime() / (1000 * 60 * 60) - compareTimeStamp.getTime() / (1000 * 60 * 60));

            long diffInDays = (timeStamp.getTime() / (1000 * 60 * 60 * 24) - compareTimeStamp.getTime() / (1000 * 60 * 60 * 24));
            // check if the two apps are used in the same day
            if (diffInDays != 0) {
                // two apps not in the same day
                Date timeStampofNextDay = new Date(((compareTimeStamp.getTime() / (1000 * 60 * 60 * 24)) + 1) * 1000 * 60 * 60 * 24);
                // use the time for the next day to calculate the difference of the last time stamp of that day 
                long diff = (timeStampofNextDay.getTime() / 1000 - compareTimeStamp.getTime() / 1000); //in sec
                // if the difference in time is less than 2 mins, then use 00:00:00 of the next day to calculate the difference in time
                if (diff <= 120) {
                    if (category1.equals("Games")) {
                        gamingUsageTime += diff; // in sec
                    }
                    // if the timestamps falls on two different hours, add frequency as it is considered to be different phone usage session
                    if (diffInHours != 0) {
                        frequency++;
                    }
                    usageTime += diff;
                    compareTimeStamp = timeStamp;
                    category1 = category2;
                } else {

                    // if the time difference is larger than 2 mins, add 10 secs as stated in wiki
                    if (category1.equals("Games")) {
                        gamingUsageTime += 10;

                    }
                    frequency++;
                    usageTime += 10;
                    compareTimeStamp = timeStamp;
                    category1 = category2;
                }
            } else {
                //the two time stamps falls on the same day!
                long diff = (timeStamp.getTime() / 1000 - compareTimeStamp.getTime() / 1000); //in sec

                if (diff <= 120) {

                    if (category1.equals("Games")) {
                        gamingUsageTime += diff; // in sec
                    }

                    usageTime += diff;
                    compareTimeStamp = timeStamp;
                    category1 = category2;

                    if (diffInHours != 0) {
                        frequency++;
                    }
                } else {
                    if (category1.equals("Games")) {
                        gamingUsageTime += 10;

                    }
                    frequency++;
                    usageTime += 10;
                    compareTimeStamp = timeStamp;
                    category1 = category2;
                }
            }
        }
        //rounding to the nearest integer
        long dailyAvgInSec = Math.round(usageTime / numOfDays);
        long dailyAvgInHours = dailyAvgInSec / (60 * 60);

        long dailyGamingAvgInSec = Math.round(gamingUsageTime / numOfDays);
        long dailyGamingAvgInHours = dailyGamingAvgInSec / (60 * 60);

        // rounding to two decimal places
        DecimalFormat df = new DecimalFormat("0.00");
        double frequencyPerHour = Math.round(frequency / numOfDaysInHours * 100) / 100.0;
        String frequencyString = df.format(frequencyPerHour);

        // first array store category, second array store numerical value 
        String[][] results = new String[4][2];

        if (dailyAvgInHours >= 5) {
            results[0][0] = "Severe";
            results[0][1] = String.valueOf(dailyAvgInSec);
        } else if (dailyAvgInHours >= 3) {
            results[0][0] = "Moderate";
            results[0][1] = String.valueOf(dailyAvgInSec);
        } else {
            results[0][0] = "Light";
            results[0][1] = String.valueOf(dailyAvgInSec);
        }

        if (dailyGamingAvgInHours >= 2) {
            results[1][0] = "Severe";
            results[1][1] = String.valueOf(dailyGamingAvgInSec);
        } else if (dailyGamingAvgInHours >= 1) {
            results[1][0] = "Moderate";
            results[1][1] = String.valueOf(dailyGamingAvgInSec);
        } else {
            results[1][0] = "Light";
            results[1][1] = String.valueOf(dailyGamingAvgInSec);
        }

        if (frequencyPerHour >= 5) {
            results[2][0] = "Severe";
            results[2][1] = frequencyString;
        } else if (frequencyPerHour >= 3) {
            results[2][0] = "Moderate";
            results[2][1] = frequencyString;
        } else {
            results[2][0] = "Light";
            results[2][1] = frequencyString;
        }

        results[3][0] = "Normal";

        for (int i = 0; i < 3; i++) {
            if (results[i][0].equals("Severe")) {
                results[3][0] = "Overusing";
                break;
            }
            if (!results[i][0].equals("Light")) {
                results[3][0] = "ToBeCautious";
                break;
            }
        }
        return results;
    }
}
