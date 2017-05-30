package se.ctr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import se.dao.AppDAO;
import se.dao.DemographicDAO;
import se.entity.App;
import se.entity.User;

public class BasicAppController {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    int dateNum = 1;

    /**
     * This Method takes in the start Date and end Date entered by the user and
     * calculates and assigns every user to a type of user depending on its
     * usageTime
     *
     * @param startDate Start Date entered by the user
     * @param endDate End Date entered by the user
     * @return a String Array, else null if there is no record
     * @throws IOException when there is input or output errors
     */
    public String[] usageTimeCategory(String startDate, String endDate)
	    throws IOException {

	ArrayList<String[]> records = AppDAO.getUsageTime(startDate, endDate, "null", "null", "null", "null");

	String[] results = null;
	try {

	    Date d1 = null;
	    Date d2 = null;

	    d1 = format.parse(startDate);
	    d2 = format.parse(endDate);

	    dateNum += (d2.getTime() - d1.getTime()) / 86400000;

	    records.add(new String[]{"exit", format.format(new Date(d2.getTime() + 1000))});

	    Iterator<String[]> iter = records.iterator();
	    String[] rec = iter.next();
	    String compareEmail = rec[0];
	    results = new String[3];

	    Date compareTimeStamp = format.parse(rec[1]);
	    String compareStartDayStr = format.format(compareTimeStamp);

	    Date compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");

	    int mildUser = 0;
	    int normalUser = 0;
	    int intenseUser = 0;

	    long usageTime = 0;

	    while (iter.hasNext()) {
		String[] record = iter.next();
		String email = record[0];
		Date timeStamp = format.parse(record[1]);
		String startDayStr = format.format(timeStamp);
		Date startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");
		long a = 0;
		long b = 0;

		if (email.equals(compareEmail)) {
		    if (compareStartDay.getTime() - startDay.getTime() == 0) {
			a = timeStamp.getTime() / 1000;
			b = compareTimeStamp.getTime() / 1000;

		    } else {
			a = compareStartDay.getTime() / 1000 + 86400;
			b = compareTimeStamp.getTime() / 1000;

		    }
		    //Last resultSet (timestamp) of a particular User
		} else {
		    a = compareStartDay.getTime() / 1000 + 86400;
		    b = compareTimeStamp.getTime() / 1000;

		}

		long diff = a - b;
		if (diff <= 120) {
		    usageTime += diff;

		} else {
                    // if diff is more than 120 secs, just add 10 secs
		    usageTime += 10;

		}

                // timestamp of a different user
		if (!email.equals(compareEmail)) {
		   
                    // categorise the users in the mild, normal or intense
		    if (Math.round(usageTime / dateNum) <= 3600) {
			mildUser++;

		    } else if (Math.round(usageTime / dateNum) >= 18000) {
			intenseUser++;
			
		    } else {
			normalUser++;
			
		    }
		    usageTime = 0;	   

		}

		compareTimeStamp = timeStamp;
		compareEmail = email;
		compareStartDay = startDay;

	    }

	    int totalUsers = AppDAO.getTotalUsers(startDate, endDate);

	    results[0] = String.valueOf(mildUser) + " (" + Math.round(mildUser * 100.0 / totalUsers) + "%)";
	    results[1] = String.valueOf(normalUser) + " (" + Math.round(normalUser * 100.0 / totalUsers) + "%)";
	    results[2] = String.valueOf(intenseUser) + " (" + Math.round(intenseUser * 100.0 / totalUsers) + "%)";
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}
	return results;
    }

    /**
     * This method calculates and assigns the usageTime relevant to each
     * appCategory inside a LinkedHashMap, which is then returned.
     *
     * @param startDate start date
     * @param endDate end date
     * @return a LinkedHashMap of usage time with app category as the key
     * @throws ServletException defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException when there are input or output errors
     */
    public LinkedHashMap<String, Double> appCategory(String startDate, String endDate)
	    throws ServletException, IOException {

	try {
	    Date d1 = null;
	    Date d2 = null;

	    d1 = format.parse(startDate);
	    d2 = format.parse(endDate);

	    dateNum += (d2.getTime() - d1.getTime()) / 86400000;
	} catch (Exception e) {
	    e.printStackTrace();
	}

	//allUsageTime counter takes into consideration all UsageTime for every category
	double allUsageTime = 0;

	
	//LinkedHashMap stores category and total Usage Time for each specific category
	LinkedHashMap<String, Double> catResult = new LinkedHashMap<String, Double>();
	

	catResult.put("Books", 0.0);
	catResult.put("Social", 0.0);
	catResult.put("Education", 0.0);
	catResult.put("Entertainment", 0.0);
	catResult.put("Information", 0.0);
	catResult.put("Library", 0.0);
	catResult.put("Local", 0.0);
	catResult.put("Tools", 0.0);
	catResult.put("Fitness", 0.0);
	catResult.put("Games", 0.0);
	catResult.put("Others", 0.0);

	HashMap<String, User> uniqMacAddList = DemographicDAO.getAllUniqueUsers(startDate, endDate);
	for (String mac : uniqMacAddList.keySet()) {

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
		    System.out.println(e.getMessage());
		}
		long a = 0;
		long b = 0;
		
                // check if the timestamps are in the same day
		if (compareStartDay.getTime() - startDay.getTime() == 0) {
		   
		    a = timeStamp.getTime() / 1000;
		    b = compareTimeStamp.getTime() / 1000;

		} else {
		  
		    a = compareStartDay.getTime() / 1000 + 86400;
		    b = compareTimeStamp.getTime() / 1000;
		    compareStartDay = startDay;
		}

		long diff = a - b;

		
		if (diff <= 120) {

		    double catUsageTime = catResult.get(compareCat);
		    catUsageTime += diff;
		    allUsageTime += diff;
		    catResult.put(compareCat, catUsageTime);

		} else {

                    // if diff is more than 120 secs, add 10 secs only
		    double catUsageTime = catResult.get(compareCat);
		    catUsageTime += 10;
		    allUsageTime += 10;
		    catResult.put(compareCat, catUsageTime);

		}
		compareApp = app;
		compareTimeStamp = timeStamp;
		compareCat = cat;

	    }

	}

        for (String appCat : catResult.keySet()) {
	    double catUsageTime = catResult.get(appCat) * 1.0 / dateNum;
	    catResult.put(appCat, catUsageTime);
	}
	// putting in total average duration across all categories
	catResult.put("all", allUsageTime / dateNum);
	return catResult;
    }

    /**
     * This method calculates and assigns the usageTime for every hour in the
     * particular day entered by the user, then returns it as a LinkedHashMap,
     * where the key is the start hour to the end hour as a String, and the
     * Object as the usageTime occurred by all users within that period.
     *
     * @param startDate start date
     * @param endDate end date
     * @param school school
     * @param cca cca
     * @param gender gender
     * @param year year
     * @return a LinkedHashMap of usage time with time period as the key
     * @throws ServletException defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException when there are input or output errors
     */
    public LinkedHashMap<String, Integer> diurnalPattern(String startDate, String endDate, String school, String gender, String year, String cca)
	    throws ServletException, IOException {

	LinkedHashMap<String, Integer> results = new LinkedHashMap<String, Integer>();
	String sd = "";
	String ed = "";

	int totalUser = AppDAO.getTotalUsers(startDate, endDate, school, gender, year, cca);
	if (totalUser <= 0) {
	    totalUser = 1;
	}

	for (int i = 0; i < 24; i++) {
	    if (i < 10) {
		sd = startDate.substring(0, startDate.indexOf(" ")) + " 0" + i + ":00:00";
		ed = endDate.substring(0, endDate.indexOf(" ")) + " 0" + i + ":59:59";
	    } else if (i < 24) {
		sd = startDate.substring(0, startDate.indexOf(" ")) + " " + i + ":00:00";
		ed = endDate.substring(0, endDate.indexOf(" ")) + " " + i + ":59:59";
	    }
	  
	    ArrayList<String[]> totalUsageData = AppDAO.getUsageTime(sd, ed, school, gender, year, cca);
           
	    Date d1 = null;
	    Date d2 = null;
	    int usageTime = 0;

	    try {

		d1 = format.parse(sd);
		d2 = format.parse(ed);
		
		totalUsageData.add(new String[]{"exit", format.format(new Date((d2.getTime() + 1000)))});
		

		Iterator<String[]> iter = totalUsageData.iterator();
		String[] compareRecord = iter.next();
		String compareEmail = compareRecord[0];
		Date compareTimeStamp = format.parse(compareRecord[1]);
		

		while (iter.hasNext()) {
		    String[] record = iter.next();
		    String email = record[0];
		    Date timeStamp = format.parse(record[1]);
		  
		    long a = 0;
		    long b = 0;

                    // check if timestamp belong to a different user
		    if (email.equals(compareEmail)) {
			a = timeStamp.getTime() / 1000;
			b = compareTimeStamp.getTime() / 1000;
			
			long diff = a - b;
			if (diff <= 120) {
			    usageTime += diff;
			    compareTimeStamp = timeStamp;
			    compareEmail = email;
			} else {
			    usageTime += 10;
			    compareTimeStamp = timeStamp;
			    compareEmail = email;
			}

		    } else {
			
			a = (d2.getTime() + 1000) / 1000;
			b = compareTimeStamp.getTime() / 1000;

			long diff = a - b;
			if (diff <= 10) {
			    usageTime += diff;
			    compareTimeStamp = timeStamp;
			    compareEmail = email;
			} else {
			    usageTime += 10;
			    compareTimeStamp = timeStamp;
			    compareEmail = email;
			}

		    }

		}
	    } catch (Exception e) {
		System.out.println(e.getMessage());
	    }

	    Date eDate = null;

	    try {
		eDate = new Date(d2.getTime() + 1000);
		
		results.put(sd.substring(11) + " - " + format.format(eDate).substring(11), (int) Math.round((usageTime * 1.0 / totalUser)));

		usageTime = 0;

	    } catch (Exception ex) {
		Logger.getLogger(BasicAppUtility.class
			.getName()).log(Level.SEVERE, null, ex);
	    }

	}
	
	return results;
    }

    /**
     *
     * This Method takes in the start date and end date as entered by the user,
     * as well as 4 possible sort filters, whether or not they are entered by
     * the user. the default value for this 4 filters is the string value
     * "null".
     *
     * @param isJson when the method call is from json or web servlet
     * @return an ArrayList of String Arrays of Data to be displayed, or an
     * empty ArrayList if there is no records from the database, or null if
     * there is an Exception.
     *
     * @param startDate Start Date entered by the user
     * @param endDate End Date entered by the user
     * @param firstSort 1st filter selected by the user, "null" string if none
     * @param secondSort 2nd filter selected by the user, "null" string if none
     * @param thirdSort 3rd filter selected by the user, "null" string if none
     * @param fourthSort 4th filter selected by the user, "null" string if none
     *
     * @throws ServletException defines a general exception a servlet can throw when it encounters difficulty.
     * @throws IOException when there are input or output errors
     */
    public ArrayList<String[]> usageTimeCategoryDemo(String startDate, String endDate, String firstSort, String secondSort, String thirdSort, String fourthSort, boolean isJson)
	    throws ServletException, IOException {

	try {
	    Date d1 = null;
	    Date d2 = null;

	    d1 = format.parse(startDate);
	    d2 = format.parse(endDate);

	    dateNum += (d2.getTime() - d1.getTime()) / 86400000;
	} catch (Exception e) {
	    e.printStackTrace();
	}

	String[] years = null;// 
	String[] genders = null;// 
	String[] schools = null;//
	

	if (isJson) {
	    years = new String[]{"2015", "2014", "2013", "2012", "2011"};
	    genders = new String[]{"M", "F"};
	    schools = new String[]{"sis", "socsc", "law", "economics", "business", "accountancy"};
	} else {
	    years = new String[]{"2011", "2012", "2013", "2014", "2015"};
	    genders = new String[]{"F", "M"};
	    schools = new String[]{"accountancy", "business", "economics", "law", "socsc", "sis"};
	}

	// check to see which filters are null
	ArrayList<String[]> results = new ArrayList<String[]>();

	
	HashMap<String, String[]> sortingMap = new HashMap<String, String[]>();
	sortingMap.put("year", years.clone());
	sortingMap.put("gender", genders.clone());
	sortingMap.put("school", schools.clone());
	sortingMap.put("cca", DemographicDAO.getAllCCAs(isJson));

	String[] sortArr = new String[]{firstSort, secondSort, thirdSort, fourthSort};
	ArrayList<String> sortedList = new ArrayList<String>();

	for (String str : sortArr) {
	    if (str != null && !str.equalsIgnoreCase("null")) {
		sortedList.add(str);
	    }
	}

	int totalUser = AppDAO.getTotalUsers(startDate, endDate);
	
        // depending on the sort types, construct different queries in AppDAO to get the relevant results
	switch (sortedList.size()) {
	    case 0:
		//Case where by no attribute is selected

		String[] toReturn = usageTimeCategory(startDate, endDate);
		ArrayList<String[]> toReturnList = new ArrayList<String[]>();
		toReturnList.add(toReturn);
		return toReturnList;
	    case 1:
		//Case of one sort type
		String sortType = sortedList.get(0);
		String[] SortArr = sortingMap.get(sortType);

		for (String sortStr : SortArr) {
		    
		    int sortedUser = AppDAO.getSortTypeData(startDate, endDate, sortType, "", "", "", sortStr, "", "", "");
		    String percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
		    String displayData = sortStr + " : " + sortedUser + "(" + percentage + "%)";
		    results.add(new String[]{displayData});

		    ArrayList<String[]> records = null;

		    String schoolStr = "null";
		    String genderStr = "null";
		    String yearStr = "null";
		    String ccaStr = "null";

		    if (sortType.equalsIgnoreCase("gender")) {
			genderStr = sortStr;
		
		    } else if (sortType.equalsIgnoreCase("cca")) {
			ccaStr = sortStr;
			
		    } else if (sortType.equalsIgnoreCase("school")) {
			schoolStr = sortStr;
			
		    } else {
			yearStr = sortStr;
		    }

		    records = AppDAO.getUsageTime(startDate, endDate, schoolStr, genderStr, yearStr, ccaStr);

		    try {

			records.add(new String[]{"exit", format.format(new Date(format.parse(endDate).getTime() + 1000))});

			Iterator<String[]> iter = records.iterator();
			String[] rec = iter.next();
			String compareEmail = rec[0];
			String[] result = new String[4];
			Date compareTimeStamp = format.parse(rec[1]);

			String compareStartDayStr = format.format(compareTimeStamp);
			Date compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");

			int mildUser = 0;
			int normalUser = 0;
			int intenseUser = 0;

			long usageTime = 0;

			while (iter.hasNext()) {
			    String[] record = iter.next();
			    String email = record[0];
			    Date timeStamp = format.parse(record[1]);
			    String startDayStr = format.format(timeStamp);
			    Date startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");

			    long a = 0;
			    long b = 0;

			    if (email.equals(compareEmail)) {
				if (compareStartDay.getTime() - startDay.getTime() == 0) {
				    a = timeStamp.getTime() / 1000;
				    b = compareTimeStamp.getTime() / 1000;
				} else {
				    a = compareStartDay.getTime() / 1000 + 86400;
				    b = compareTimeStamp.getTime() / 1000;
				}
			    } else {
				//Check last timestamp of unique email
				a = compareStartDay.getTime() / 1000 + 86400;
				b = compareTimeStamp.getTime() / 1000;
			    }

			    long diff = a - b;

			    if (diff <= 120) {
				usageTime += diff;
			    } else {
				usageTime += 10;
			    }

			    if (!email.equals(compareEmail)) {
				if (Math.round(usageTime / dateNum) <= 3600) {
				    mildUser++;
				} else if (Math.round(usageTime / dateNum) >= 18000) {
				    intenseUser++;
				} else {
				    normalUser++;
				}
				usageTime = 0;

			    }

			    compareTimeStamp = timeStamp;
			    compareEmail = email;
			    compareStartDay = startDay;

			}

			result[0] = "";
			result[1] = String.valueOf(mildUser + "(" + Math.round(mildUser * 100.0 / totalUser) + "%)");
			result[2] = String.valueOf(normalUser + "(" + Math.round(normalUser * 100.0 / totalUser) + "%)");
			result[3] = String.valueOf(intenseUser + "(" + Math.round(intenseUser * 100.0 / totalUser) + "%)");
			results.add(new String[]{"", "Mild Users", "Normal Users", "Intense Users"});
			results.add(result);

		    } catch (Exception e) {
			System.out.println(e.getMessage());
		    }

		}

		break;
	    case 2:
		//case of two sort types
		String firstSortType = sortedList.get(0);
		String secondSortType = sortedList.get(1);
		String[] firstSortArr = sortingMap.get(firstSortType);
		String[] secondSortArr = sortingMap.get(secondSortType);

		for (String firstSortStr : firstSortArr) {

		    int sortedUser = AppDAO.getSortTypeData(startDate, endDate, firstSortType, "", "", "", firstSortStr, "", "", "");
		    String percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
		    String displayData = firstSortStr + " : " + sortedUser + "(" + percentage + "%)";
		    results.add(new String[]{displayData});

		    for (String secondSortStr : secondSortArr) {
			sortedUser = AppDAO.getSortTypeData(startDate, endDate, firstSortType, secondSortType, "", "", firstSortStr, secondSortStr, "", "");
			percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
			displayData = secondSortStr + " : " + sortedUser + "(" + percentage + "%)";
			results.add(new String[]{"", displayData});

			ArrayList<String[]> records = null;

			String schoolStr = "null";
			String genderStr = "null";
			String yearStr = "null";
			String ccaStr = "null";

			if (firstSortType.equalsIgnoreCase("gender")) {
			    genderStr = firstSortStr;
			} else if (firstSortType.equalsIgnoreCase("year")) {
			    yearStr = firstSortStr;
			} else if (firstSortType.equalsIgnoreCase("school")) {
			    schoolStr = firstSortStr;
			} else {
			    ccaStr = firstSortStr;
			}

			if (secondSortType.equalsIgnoreCase("gender")) {
			    genderStr = secondSortStr;
			} else if (secondSortType.equalsIgnoreCase("year")) {
			    yearStr = secondSortStr;
			} else if (secondSortType.equalsIgnoreCase("school")) {
			    schoolStr = secondSortStr;
			} else {
			    ccaStr = secondSortStr;
			}

			records = AppDAO.getUsageTime(startDate, endDate, schoolStr, genderStr, yearStr, ccaStr);

			try {
			    records.add(new String[]{"exit", format.format(new Date(format.parse(endDate).getTime() + 1000))});

			    Iterator<String[]> iter = records.iterator();
			    String[] rec = iter.next();
			    String compareEmail = rec[0];
			    String[] result = new String[5];
			    Date compareTimeStamp = format.parse(rec[1]);

			    String compareStartDayStr = format.format(compareTimeStamp);
			    Date compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");

			    int mildUser = 0;
			    int normalUser = 0;
			    int intenseUser = 0;

			    long usageTime = 0;

			    while (iter.hasNext()) {
				String[] record = iter.next();
				String email = record[0];
				Date timeStamp = format.parse(record[1]);
				String startDayStr = format.format(timeStamp);
				Date startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");

				long a = 0;
				long b = 0;

				if (email.equals(compareEmail)) {
				    if (compareStartDay.getTime() - startDay.getTime() == 0) {
					a = timeStamp.getTime() / 1000;
					b = compareTimeStamp.getTime() / 1000;
				    } else {
					a = compareStartDay.getTime() / 1000 + 86400;
					b = compareTimeStamp.getTime() / 1000;
				    }
				} else {
				    //Check last timestamp of unique email
				    a = compareStartDay.getTime() / 1000 + 86400;
				    b = compareTimeStamp.getTime() / 1000;
				}

				long diff = a - b;

				if (diff <= 120) {
				    usageTime += diff;
				} else {
				    usageTime += 10;
				}

				if (!email.equals(compareEmail)) {
				    if (usageTime / dateNum <= 3600) {
					mildUser++;
				    } else if (usageTime / dateNum >= 18000) {
					intenseUser++;
				    } else {
					normalUser++;
				    }
				    usageTime = 0;

				}

				compareTimeStamp = timeStamp;
				compareEmail = email;
				compareStartDay = startDay;

			    }

			    result[0] = "";
			    result[1] = "";
			    result[2] = String.valueOf(mildUser + "(" + Math.round(mildUser * 100.0 / totalUser) + "%)");
			    result[3] = String.valueOf(normalUser + "(" + Math.round(normalUser * 100.0 / totalUser) + "%)");
			    result[4] = String.valueOf(intenseUser + "(" + Math.round(intenseUser * 100.0 / totalUser) + "%)");
			    results.add(new String[]{"", "", "Mild Users", "Normal Users", "Intense Users"});
			    results.add(result);

			} catch (Exception e) {
			    System.out.println(e.getMessage());
			}
		    }

		}

		break;

	    case 3:
		//case of 3 sort types
		String sortType1 = sortedList.get(0);
		String sortType2 = sortedList.get(1);
		String sortType3 = sortedList.get(2);
		String[] sortArr1 = sortingMap.get(sortType1);
		String[] sortArr2 = sortingMap.get(sortType2);
		String[] sortArr3 = sortingMap.get(sortType3);

		for (String firstSortStr : sortArr1) {

		    int sortedUser = AppDAO.getSortTypeData(startDate, endDate, sortType1, "", "", "", firstSortStr, "", "", "");
		    String percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
		    String displayData = firstSortStr + " : " + sortedUser + "(" + percentage + "%)";
		    results.add(new String[]{displayData});

		    for (String secondSortStr : sortArr2) {

			sortedUser = AppDAO.getSortTypeData(startDate, endDate, sortType1, sortType2, "", "", firstSortStr, secondSortStr, "", "");
			percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
			displayData = secondSortStr + " : " + sortedUser + "(" + percentage + "%)";
			results.add(new String[]{"", displayData});

			for (String thirdSortStr : sortArr3) {

			    sortedUser = AppDAO.getSortTypeData(startDate, endDate, sortType1, sortType2, sortType3, "", firstSortStr, secondSortStr, thirdSortStr, "");
			    percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
			    displayData = thirdSortStr + " : " + sortedUser + "(" + percentage + "%)";
			    results.add(new String[]{"", "", displayData});

			    ArrayList<String[]> records = null;

			    String schoolStr = "null";
			    String genderStr = "null";
			    String yearStr = "null";
			    String ccaStr = "null";

			    if (sortType1.equalsIgnoreCase("gender")) {
				genderStr = firstSortStr;
			    } else if (sortType1.equalsIgnoreCase("year")) {
				yearStr = firstSortStr;
			    } else if (sortType1.equalsIgnoreCase("school")) {
				schoolStr = firstSortStr;
			    } else {
				ccaStr = firstSortStr;
			    }

			    if (sortType2.equalsIgnoreCase("gender")) {
				genderStr = secondSortStr;
			    } else if (sortType2.equalsIgnoreCase("year")) {
				yearStr = secondSortStr;
			    } else if (sortType2.equalsIgnoreCase("school")) {
				schoolStr = secondSortStr;
			    } else {
				ccaStr = secondSortStr;
			    }

			    if (sortType3.equalsIgnoreCase("gender")) {
				genderStr = thirdSortStr;
			    } else if (sortType3.equalsIgnoreCase("year")) {
				yearStr = thirdSortStr;
			    } else if (sortType3.equalsIgnoreCase("school")) {
				schoolStr = thirdSortStr;
			    } else {
				ccaStr = thirdSortStr;
			    }

			    records = AppDAO.getUsageTime(startDate, endDate, schoolStr, genderStr, yearStr, ccaStr);

			    try {
				records.add(new String[]{"exit", format.format(new Date(format.parse(endDate).getTime() + 1000))});

				Iterator<String[]> iter = records.iterator();
				String[] rec = iter.next();
				String compareEmail = rec[0];
				String[] result = new String[6];
				Date compareTimeStamp = format.parse(rec[1]);

				String compareStartDayStr = format.format(compareTimeStamp);
				Date compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");

				int mildUser = 0;
				int normalUser = 0;
				int intenseUser = 0;

				long usageTime = 0;

				while (iter.hasNext()) {
				    String[] record = iter.next();
				    String email = record[0];
				    Date timeStamp = format.parse(record[1]);
				    String startDayStr = format.format(timeStamp);
				    Date startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");

				    long a = 0;
				    long b = 0;

				    if (email.equals(compareEmail)) {
					if (compareStartDay.getTime() - startDay.getTime() == 0) {
					    a = timeStamp.getTime() / 1000;
					    b = compareTimeStamp.getTime() / 1000;
					} else {
					    a = compareStartDay.getTime() / 1000 + 86400;
					    b = compareTimeStamp.getTime() / 1000;
					}
				    } else {
					//Check last timestamp of unique email
					a = compareStartDay.getTime() / 1000 + 86400;
					b = compareTimeStamp.getTime() / 1000;
				    }

				    long diff = a - b;

				    if (diff <= 120) {
					usageTime += diff;
				    } else {
					usageTime += 10;
				    }

				    if (!email.equals(compareEmail)) {
					if (usageTime / dateNum <= 3600) {
					    mildUser++;
					} else if (usageTime / dateNum >= 18000) {
					    intenseUser++;
					} else {
					    normalUser++;
					}
					usageTime = 0;

				    }

				    compareTimeStamp = timeStamp;
				    compareEmail = email;
				    compareStartDay = startDay;

				}

				result[0] = "";
				result[1] = "";
				result[2] = "";
				result[3] = String.valueOf(mildUser + "(" + Math.round(mildUser * 100.0 / totalUser) + "%)");
				result[4] = String.valueOf(normalUser + "(" + Math.round(normalUser * 100.0 / totalUser) + "%)");
				result[5] = String.valueOf(intenseUser + "(" + Math.round(intenseUser * 100.0 / totalUser) + "%)");
				results.add(new String[]{"", "", "", "Mild Users", "Normal Users", "Intense Users"});
				results.add(result);

			    } catch (Exception e) {
				System.out.println(e.getMessage());
			    }

			  
			}
		    }
		}

		break;

	    case 4:
		//in case of 4 sort types
		String sort1 = sortedList.get(0);
		String sort2 = sortedList.get(1);
		String sort3 = sortedList.get(2);
		String sort4 = sortedList.get(3);
		String[] arr1 = sortingMap.get(sort1);
		String[] arr2 = sortingMap.get(sort2);
		String[] arr3 = sortingMap.get(sort3);
		String[] arr4 = sortingMap.get(sort4);

		for (String firstSortStr : arr1) {
		    int sortedUser = AppDAO.getSortTypeData(startDate, endDate, sort1, "", "", "", firstSortStr, "", "", "");
		    String percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
		    String displayData = firstSortStr + " : " + sortedUser + "(" + percentage + "%)";
		    results.add(new String[]{displayData});
		    for (String secondSortStr : arr2) {
			sortedUser = AppDAO.getSortTypeData(startDate, endDate, sort1, sort2, "", "", firstSortStr, secondSortStr, "", "");
			percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
			displayData = secondSortStr + " : " + sortedUser + "(" + percentage + "%)";
			results.add(new String[]{"", displayData});
			for (String thirdSortStr : arr3) {
			    sortedUser = AppDAO.getSortTypeData(startDate, endDate, sort1, sort2, sort3, "", firstSortStr, secondSortStr, thirdSortStr, "");
			    percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
			    displayData = thirdSortStr + " : " + sortedUser + "(" + percentage + "%)";
			    results.add(new String[]{"", "", displayData});
			    for (String fourthSortStr : arr4) {
				sortedUser = AppDAO.getSortTypeData(startDate, endDate, sort1, sort2, sort3, sort4, firstSortStr, secondSortStr, thirdSortStr, fourthSortStr);
				percentage = String.valueOf(Math.round(sortedUser * 100.0 / totalUser));
				displayData = fourthSortStr + " : " + sortedUser + "(" + percentage + "%)";
				results.add(new String[]{"", "", "", displayData});

				ArrayList<String[]> records = null;
				
				String schoolStr = "null";
				String genderStr = "null";
				String yearStr = "null";
				String ccaStr = "null";

				if (sort1.equalsIgnoreCase("gender")) {
				    genderStr = firstSortStr;
				} else if (sort1.equalsIgnoreCase("year")) {
				    yearStr = firstSortStr;
				} else if (sort1.equalsIgnoreCase("school")) {
				    schoolStr = firstSortStr;
				} else {
				    ccaStr = firstSortStr;
				}

				if (sort2.equalsIgnoreCase("gender")) {
				    genderStr = secondSortStr;
				} else if (sort2.equalsIgnoreCase("year")) {
				    yearStr = secondSortStr;
				} else if (sort2.equalsIgnoreCase("school")) {
				    schoolStr = secondSortStr;
				} else {
				    ccaStr = secondSortStr;
				}

				if (sort3.equalsIgnoreCase("gender")) {
				    genderStr = thirdSortStr;
				} else if (sort3.equalsIgnoreCase("year")) {
				    yearStr = thirdSortStr;
				} else if (sort3.equalsIgnoreCase("school")) {
				    schoolStr = thirdSortStr;
				} else {
				    ccaStr = thirdSortStr;
				}

				if (sort4.equalsIgnoreCase("gender")) {
				    genderStr = fourthSortStr;
				} else if (sort4.equalsIgnoreCase("year")) {
				    yearStr = fourthSortStr;
				} else if (sort4.equalsIgnoreCase("school")) {
				    schoolStr = fourthSortStr;
				} else {
				    ccaStr = fourthSortStr;
				}

				records = AppDAO.getUsageTime(startDate, endDate, schoolStr, genderStr, yearStr, ccaStr);

				try {
				    records.add(new String[]{"exit", format.format(new Date(format.parse(endDate).getTime() + 1000))});

				    Iterator<String[]> iter = records.iterator();
				    String[] rec = iter.next();
				    String compareEmail = rec[0];
				    String[] result = new String[7];
				    Date compareTimeStamp = format.parse(rec[1]);

				    String compareStartDayStr = format.format(compareTimeStamp);
				    Date compareStartDay = format.parse(compareStartDayStr.substring(0, compareStartDayStr.indexOf(" ")) + " 00:00:00");

				    int mildUser = 0;
				    int normalUser = 0;
				    int intenseUser = 0;

				    long usageTime = 0;

				    while (iter.hasNext()) {
					String[] record = iter.next();
					String email = record[0];
					Date timeStamp = format.parse(record[1]);
					String startDayStr = format.format(timeStamp);
					Date startDay = format.parse(startDayStr.substring(0, startDayStr.indexOf(" ")) + " 00:00:00");

					long a = 0;
					long b = 0;

					if (email.equals(compareEmail)) {
					    if (compareStartDay.getTime() - startDay.getTime() == 0) {
						a = timeStamp.getTime() / 1000;
						b = compareTimeStamp.getTime() / 1000;
					    } else {
						a = compareStartDay.getTime() / 1000 + 86400;
						b = compareTimeStamp.getTime() / 1000;
					    }
					} else {
					    //Check last timestamp of unique email
					    a = compareStartDay.getTime() / 1000 + 86400;
					    b = compareTimeStamp.getTime() / 1000;
					}

					long diff = a - b;

					if (diff <= 120) {
					    usageTime += diff;
					} else {
					    usageTime += 10;
					}

					if (!email.equals(compareEmail)) {
					    if (usageTime / dateNum <= 3600) {
						mildUser++;
					    } else if (usageTime / dateNum >= 18000) {
						intenseUser++;
					    } else {
						normalUser++;
					    }
					    usageTime = 0;

					}

					compareTimeStamp = timeStamp;
					compareEmail = email;
					compareStartDay = startDay;

				    }

				    result[0] = "";
				    result[1] = "";
				    result[2] = "";
				    result[3] = "";
				    result[4] = String.valueOf(mildUser + "(" + Math.round(mildUser * 100.0 / totalUser) + "%)");
				    result[5] = String.valueOf(normalUser + "(" + Math.round(normalUser * 100.0 / totalUser) + "%)");
				    result[6] = String.valueOf(intenseUser + "(" + Math.round(intenseUser * 100.0 / totalUser) + "%)");
				    results.add(new String[]{"", "", "", "", "Mild Users", "Normal Users", "Intense Users"});
				    results.add(result);

				} catch (Exception e) {
				    System.out.println(e.getMessage());
				}

			    }
			}
		    }
		}

		break;

	}
	return results;
    }

}
