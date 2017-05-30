package se.entity;

import java.util.*;

public class App {
    private Date timeStamp;
    private String hashedMAC;
    private int app_id;

    /**
     * App Contructor
     * @param timeStamp timeStamp of App
     * @param hashedMAC macAddress corresponding to demographics.csv
     * @param app_id app_id corresponding to app-Lookup.csv
     */
    public App(Date timeStamp, String hashedMAC, int app_id) {
        this.timeStamp = timeStamp;
        this.hashedMAC = hashedMAC;
        this.app_id = app_id;
    }

    /**
     *
     * @return timeStamp of app
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     *
     * @param timeStamp set timeStamp of app
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     *
     * @return macAddress of app
     */
    public String getHashedMAC() {
        return hashedMAC;
    }

    /**
     *
     * @param hashedMAC set macAddress of app
     */
    public void setHashedMAC(String hashedMAC) {
        this.hashedMAC = hashedMAC;
    }

    /**
     *
     * @return appId 
     */
    public int getApp_id() {
        return app_id;
    }

    /**
     *
     * @param app_id set appId
     */
    public void setApp_id(int app_id) {
        this.app_id = app_id;
    }
    
}
