package se.entity;

public class AppLookup {
    private int app_id;
    private String app_name;
    private String app_category;

    /**
     * AppLookUp Contructor
     * @param app_id appId of app
     * @param app_name name of app
     * @param app_category category of app
     */
    public AppLookup(int app_id, String app_name, String app_category) {
        this.app_id = app_id;
        this.app_name = app_name;
        this.app_category = app_category;
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

    /**
     *
     * @return app name
     */
    public String getApp_name() {
        return app_name;
    }

    /**
     *
     * @param app_name set app name
     */
    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    /**
     *
     * @return app category
     */
    public String getApp_category() {
        return app_category;
    }

    /**
     *
     * @param app_category set app category
     */
    public void setApp_category(String app_category) {
        this.app_category = app_category;
    }
    
}
