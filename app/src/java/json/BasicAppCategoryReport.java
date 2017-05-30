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
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.ctr.BasicAppController;

@WebServlet(name = "BasicAppCategoryReport", urlPatterns = {"/json/basic-appcategory-report"})
public class BasicAppCategoryReport extends HttpServlet {

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
                    results.addProperty("status", "error");
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

            if (errorArray.size() == 0) {

                BasicAppController baCtr = new BasicAppController();
                
                //we use TreeMap here because TreeMap helps us to sort
                TreeMap<String, Double> catResult = new TreeMap(baCtr.appCategory(startDate, endDate));
                JsonArray success = new JsonArray();
                
                // getting total average duration across all categories   
                int totalCount = catResult.get("all").intValue();

                for (String cat : catResult.keySet()) {
                    double usageTime = catResult.get(cat);
                    int percentage = 0;
                    if (totalCount != 0) {
                        percentage = (int)Math.round(usageTime * 100.0 / totalCount);
                    }

                    if (!cat.equals("all")) {
                        JsonObject category = new JsonObject();

                        category.addProperty("category-name", cat.toLowerCase());
                        category.addProperty("category-duration", Math.round(usageTime));
                        category.addProperty("category-percent", percentage);

                        success.add(category);
                    }
                }

                results.addProperty("status", "success");

                results.add("breakdown", success);
                out.println(gson.toJson(results));

            } else {
                // have errors
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
