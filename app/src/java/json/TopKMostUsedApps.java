package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.ctr.TopKController;

@WebServlet(name = "TopKMostUsedApps", urlPatterns = {"/json/top-k-most-used-apps"})
public class TopKMostUsedApps extends HttpServlet {

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
            ArrayList<String> errorArray = new ArrayList<String>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String token = request.getParameter("token");
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            String school = request.getParameter("school").toLowerCase();
            String k = request.getParameter("k");

            Date start = null;
            Date end = null;

            if (token == null) {
                errorArray.add("missing token");
            } else if (token.length() == 0) {
                errorArray.add("blank token");
            } else {
                try {
                    if (JWTUtility.verify(token, "2046001611999925") == null) {
                        errorArray.add("invalid token");
                    }
                } catch (JWTException e) {
                    errorArray.add("invalid token");
                }
            }

            if (endDate == null) {
                errorArray.add("missing enddate");
            } else if (endDate.length() == 0) {
                errorArray.add("blank enddate");
            } else {
                try {
                    endDate += " 23:59:59";
                    end = format.parse(endDate);
                    String endDateTest = format.format(end);

                    String[] endDateYear = endDate.split("-");
                    Integer endYear = Integer.valueOf(endDateYear[0]);
                    // check if it's a proper date, e.g. converting 30 feb into date and back to String
                    //compare the converted 30 feb with the original end date
                    if (!endDate.equals(endDateTest)) {
                        errorArray.add("invalid enddate");
                    } else if (endYear < 2011
                            || endYear > 2015) {
                        errorArray.add("invalid enddate");
                    }
                } catch (ParseException e) {
                    results.addProperty("status", "error");
                    errorArray.add("invalid enddate");
                }
            }

            if (startDate == null) {
                errorArray.add("missing startdate");
            } else if (startDate.length() == 0) {
                errorArray.add("blank startdate");
            } else {
                try {
                    startDate += " 00:00:00";
                    start = format.parse(startDate);
                    String startDateTest = format.format(start);

                    String[] startDateYear = startDate.split("-");
                    Integer startYear = Integer.valueOf(startDateYear[0]);

                    // check if it's a proper date, e.g. converting 30 feb into date and back to String
                    //compare the converted 30 feb with the original start date
                    if (!startDate.equals(startDateTest)) {
                        errorArray.add("invalid startdate");
                    } else if (startYear < 2011
                            || startYear > 2015) {
                        errorArray.add("invalid startdate");
                    } else {
                        if (end != null) {
                            if (start.compareTo(end) > 0) {
                                errorArray.add("invalid startdate");
                            }
                        }
                    }
                } catch (ParseException e) {
                    results.addProperty("status", "error");
                    errorArray.add("invalid startdate");
                }
            }

            if (school == null) {
                errorArray.add("missing school");
            } else {
                if (school.length() == 0) {
                    errorArray.add("blank school");
                } else if (!school.equals("sis")
                        && !school.equals("law")
                        && !school.equals("socsc")
                        && !school.equals("business")
                        && !school.equals("accountancy")
                        && !school.equals("economics")) {
                    errorArray.add("invalid school");
                }
            }

            int kValue = 0;
            if (k == null) {
                kValue = 3;
            } else if (k.length() == 0) {
                errorArray.add("blank k");
            } else {
                try {
                    kValue = Integer.parseInt(k);
                    if (kValue > 10 || kValue < 1) {
                        errorArray.add("invalid k");
                    }
                } catch (NumberFormatException e) {
                    errorArray.add("invalid k");
                }
            }

            if (errorArray.size() == 0) {
                // there is no error
                results.addProperty("status", "success");
                JsonArray success = new JsonArray();
                
                /*Calculates a list of apps and their usageTime based on users criteria
                 and then sorts them based on rank and k value */
                ArrayList<String[]> tkResult = TopKController.topKApps(startDate, endDate, school, kValue);
                for (int i = 0; i < tkResult.size(); i++) {
                    
                    String[] result = tkResult.get(i); // store rank, name, duration 
                    String[] apps = result[1].split(","); 
                    for (int j = 0; j < apps.length; j++) {
                        Arrays.sort(apps);
                        JsonObject app = new JsonObject();
                        app.addProperty("rank", Integer.parseInt(result[0]));
                        app.addProperty("app-name", apps[j]);
                        app.addProperty("duration", Integer.parseInt(result[2]));
                        success.add(app);
                    }
                }
                results.add("results", success);
                out.println(gson.toJson(results));

            } else {
                // sort errorArray in ascending order
                Collections.sort(errorArray);
                JsonArray errorJsonArray = (JsonArray) new Gson().toJsonTree(errorArray,
                        new TypeToken<ArrayList<String>>() {
                        }.getType());
                results.addProperty("status", "error");
                results.add("messages", errorJsonArray);
                out.println(gson.toJson(results));
            }
            out.close();

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
