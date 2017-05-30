package se.ctr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import se.dao.AppDAO;
import se.dao.AppLookupDAO;
import se.dao.DemographicDAO;

public class BootstrapController {

    /**
     * Method bootstrap uploaded data and returns HashMap of errors in csv files
     *
     * @param brDemo BufferReader contains inputStream of demographics.csv
     * @param brAppLookup BufferReader contains inputStream of app-Lookup.csv
     * @param brApp BufferReader contains inputStream of app.csv
     * @param function String Object that shows either bootstrap or upload
     * additional files
     * @return HashMap that returns error messages of csv files
     * @throws IOException exception during reading of files
     */
    public HashMap<String, TreeMap<Integer, ArrayList<String>>> bootstrap(BufferedReader brDemo, BufferedReader brAppLookup, BufferedReader brApp, String function) throws IOException {
        // Checks for presence of all three csv files
        if (brDemo != null && brAppLookup != null && brApp != null) {
            DemographicDAO.clearData();
            DemographicDAO.readDemographic(brDemo);
            
            AppLookupDAO.clearData();
            AppLookupDAO.readAppLookup(brAppLookup);
            
            AppDAO.clearData();
            AppDAO.readApp(brApp, function);
            
            // Initialize TreeMap to contain error messages for respective files in sequence
            TreeMap<Integer, ArrayList<String>> errorListDemo = DemographicDAO.getErrorListDemographics();
            TreeMap<Integer, ArrayList<String>> errorListAppLookup = AppLookupDAO.getErrorListAppLookup();
            TreeMap<Integer, ArrayList<String>> errorListApp = AppDAO.getErrorListApp();
            
            // Initialize HashMap to return that contains TreeMap of errors for all files
            HashMap<String, TreeMap<Integer, ArrayList<String>>> errorMap = new HashMap<String, TreeMap<Integer, ArrayList<String>>>();
            errorMap.put("errorListDemo", errorListDemo);
            errorMap.put("errorListAppLookup", errorListAppLookup);
            errorMap.put("errorListApp", errorListApp);
            return errorMap;
        }
        return null;
    }
    
    /**
     * Method uploads additional files and returns HashMap of errors in csv files
     *
     * @param brDemo BufferReader contains inputStream of demographics.csv
     * @param brApp BufferReader contains inputStream of app.csv
     * @param function String Object that shows either bootstrap or upload
     * additional files
     * @return HashMap that returns error messages of csv files
     * @throws IOException exception during reading of files
     */
    public HashMap<String, TreeMap<Integer, ArrayList<String>>> addFiles(BufferedReader brDemo, BufferedReader brApp, String function) throws IOException {
        // Initialize TreeMap to store errors in respective files in sequence
        TreeMap<Integer, ArrayList<String>> errorListDemo;
        TreeMap<Integer, ArrayList<String>> errorListApp;
        
        // Initiaize HashMap to contain TreeMaps of errors for all files
        HashMap<String, TreeMap<Integer, ArrayList<String>>> errorMap = new HashMap<String, TreeMap<Integer, ArrayList<String>>>();

        // Check for the different combination of demo and app.csv
        if (brDemo != null && brApp != null) { 
            DemographicDAO.readDemographic(brDemo);
            AppDAO.readApp(brApp, function);
            errorListDemo = DemographicDAO.getErrorListDemographics();
            errorListApp = AppDAO.getErrorListApp();
            errorMap.put("errorListDemo", errorListDemo);
            errorMap.put("errorListApp", errorListApp);
            return errorMap;
            
        } else if (brDemo != null) {
            DemographicDAO.readDemographic(brDemo);
            errorListDemo = DemographicDAO.getErrorListDemographics();
            errorMap.put("errorListDemo", errorListDemo);
            return errorMap;
            
        } else if (brApp != null) {
            AppDAO.readApp(brApp, function);
            errorListApp = AppDAO.getErrorListApp();
            errorMap.put("errorListApp", errorListApp);
            return errorMap;
        }

        return null;
    }
}
