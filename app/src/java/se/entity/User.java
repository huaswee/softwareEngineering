package se.entity;

public class User {

    private String hashedMAC;
    private String name;
    private String password;
    private String email;
    private String gender;
    private String cca;
    private boolean admin;

    /**
     * User Constructor
     * @param name name of user
     * @param password password of user
     */
    public User(String name, String password) {
	this.name = name;
	this.password = password;
	admin = true;
    }

    /**
     * User Constructor
     * @param hashedMAC macAddress of user
     * @param name name of user
     * @param password password of user
     * @param email email of user
     * @param gender gender of user
     * @param cca cca of user
     */
    public User(String hashedMAC, String name, String password, String email, String gender, String cca) {
	this(hashedMAC, name, password, email, gender, cca, false);
    }

    /**
     * User Constructor
     * @param hashedMAC macAddress of user
     * @param name name of user
     * @param password password of user
     * @param email email of user
     * @param gender gender of user
     * @param cca cca of user
     * @param admin check if user is admin
     */
    public User(String hashedMAC, String name, String password, String email, String gender, String cca, boolean admin) {
	this.hashedMAC = hashedMAC;
	this.name = name;
	this.password = password;
	this.email = email;
	this.gender = gender;
	this.cca = cca;
	this.admin = admin;
    }

    /**
     *
     * @return macAddress of user
     */
    public String getHashedMAC() {
	return hashedMAC;
    }

    /**
     *
     * @param hashedMAC set macAddress of user
     */
    public void setHashedMAC(String hashedMAC) {
	this.hashedMAC = hashedMAC;
    }

    /**
     *
     * @return username
     */
    public String getUsername() {
	return name;
    }

    /**
     *
     * @param name set Name of user
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     *
     * @return password of user
     */
    public String getPassword() {
	return password;
    }

    /**
     *
     * @param password set password for user
     */
    public void setPassword(String password) {
	this.password = password;
    }

    /**
     *
     * @return email of user
     */
    public String getEmail() {
	return email;
    }

    /**
     *
     * @param email set email for user
     */
    public void setEmail(String email) {
	this.email = email;
    }

    /**
     *
     * @return gender of user
     */
    public String getGender() {
	return gender;
    }

    /**
     *
     * @param gender set gender of user
     */
    public void setGender(String gender) {
	this.gender = gender;
    }

    /**
     *
     * @return cca of user
     */
    public String getCca() {
	return cca;
    }

    /**
     *
     * @param cca set cca of user
     */
    public void setCca(String cca) {
	this.cca = cca;
    }

    /**
     *
     * @return if user is admin
     */
    public boolean isAdmin() {
	return admin;
    }
}
