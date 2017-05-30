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
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.ctr.SmartphoneOveruseController;
import se.dao.AppDAO;
import se.dao.DemographicDAO;
import se.entity.App;
import se.entity.User;

@WebServlet(name = "OveruseReport", urlPatterns = {"/json/overuse-report"})
public class OveruseReport extends HttpServlet {

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
            String macAddress = request.getParameter("macaddress");

            Date start = null;
            Date end = null;
            String username = null;

            if (token == null) {
                errorArray.add("missing token");
            } else if (token.length() == 0) {
                errorArray.add("blank token");
            } else {
                try {
                    if ((username = JWTUtility.verify(token, "2046001611999925")) == null) {
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

            if (macAddress == null) {
                errorArray.add("missing macaddress");
            } else if (macAddress.length() == 0) {
                errorArray.add("blank macaddress");
            } else if (macAddress.length() != 40) {
                errorArray.add("invalid macaddress");
            } else {
                User user = DemographicDAO.getUserByMac(macAddress);
                if (user == null) {
                    errorArray.add("invalid macaddress");
                }
            }

            if (errorArray.size() == 0) {
                LinkedHashMap<App, String> appData = AppDAO.getLoggedInUserAppData(startDate, endDate, macAddress);
                SmartphoneOveruseController soCtr = new SmartphoneOveruseController();
                /*get average aily usage time, average daily gaming duration 
                and the smartphone access frequency of the logged in user */
                String[][] reports = soCtr.smartphoneOveruseReport(start, end, appData);

                results.addProperty("status", "success");
                JsonObject temp = new JsonObject();

                temp.addProperty("overuse-index", reports[3][0]);
                JsonArray array = new JsonArray();

                JsonObject temp1 = new JsonObject();
                temp1.addProperty("usage-category", reports[0][0]);
                temp1.addProperty("usage-duration", Integer.parseInt(reports[0][1]));

                JsonObject temp2 = new JsonObject();
                temp2.addProperty("gaming-category", reports[1][0]);
                temp2.addProperty("gaming-duration", Integer.parseInt(reports[1][1]));

                JsonObject temp3 = new JsonObject();
                temp3.addProperty("accessfrequency-category", reports[2][0]);
                temp3.addProperty("accessfrequency", Double.parseDouble(reports[2][1]));

                array.add(temp1);
                array.add(temp2);
                array.add(temp3);

                temp.add("metrics", array);
                results.add("results", temp);
                out.println(gson.toJson(results));

            } else {
                //sorting errorArray in ascending order
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
