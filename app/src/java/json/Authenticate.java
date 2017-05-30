package json;

import com.google.gson.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.dao.DemographicDAO;
import se.entity.User;
import is203.*;
import se.dao.AdminDAO;

@WebServlet(name = "authenticate", urlPatterns = {"/json/authenticate"})
public class Authenticate extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {

// Gson object to modify default compact display to prettyprintng display
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // JsonObject to store number of records inserted and errors
            JsonObject results = new JsonObject();
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username != null && password != null) {
                if (!username.isEmpty() && !password.isEmpty()) {
                    //checking for admin
                    User user = AdminDAO.getAdmin(username);
                    String token = null;
                    if (user != null) {
                        // if the username is admin
                        if (user.getPassword().equalsIgnoreCase(password)) {
                            String sharedSecret = "2046001611999925";
                            // creating a token
                            token = JWTUtility.sign(sharedSecret, username);

                            results.addProperty("status", "success");
                            results.addProperty("token", token);
                            out.println(gson.toJson(results));
                            return;
                        }
                    } else {
                        // if the username is NOT admin, go and get the username from normal user
                        user = DemographicDAO.getUser(username);
                        if (user != null) {
                            //check if it is a normal user
                            if (user.getPassword().equalsIgnoreCase(password)) {
                                String sharedSecret = "2046001611999925";
                                
                                // creating a token
                                token = JWTUtility.sign(sharedSecret, username);

                                results.addProperty("status", "success");
                                results.addProperty("token", token);
                                out.println(gson.toJson(results));
                                return;
                            }
                        }
                    }
                }
            }

            String error = "invalid username/password";
            JsonArray errorMsgs = new JsonArray();
            errorMsgs.add(new JsonPrimitive(error));
            results.addProperty("status", "error");
          
            results.add("messages", errorMsgs);
            out.println(gson.toJson(results));

        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
