package se.dao;

import java.sql.*;
import se.entity.User;

public class AdminDAO {

    /**
     * Get information of Admin from Database
     *
     * @param usernameInput username of admin
     * @return user object if exists in Database else returns null
     */
    public static User getAdmin(String usernameInput) {

        // Initialize ConnectionPool Manager
        User result = null;
        Connection conn = null;
        PreparedStatement smt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionPoolManager.getInstance().getConnection();
            // SQL Statement to call from Database
            smt = conn.prepareStatement("select username, password from admin where username = ?");
            smt.setString(1, usernameInput);
            rs = smt.executeQuery();
            
            // Set results executed from SQL as username and password
            if (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                result = new User(username, password);
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
}
