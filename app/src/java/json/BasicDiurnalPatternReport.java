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
import se.ctr.BasicAppController;

@WebServlet(name = "BasicDiurnalPatternReport", urlPatterns = {"/json/basic-diurnalpattern-report"})
public class BasicDiurnalPatternReport extends HttpServlet {

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
            JsonArray successArray = new JsonArray();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String token = request.getParameter("token");
            String date = request.getParameter("date");
            String yearFilter = request.getParameter("yearfilter").toLowerCase();
            String genderFilter = request.getParameter("genderfilter").toLowerCase();
            String schoolFilter = request.getParameter("schoolfilter").toLowerCase();
            String endDate = null;
            Date d = null;

            if (token == null) {
                errorArray.add("missing token");
            } else if (token.length() == 0) {
                errorArray.add("blank token");
            } else {
                try {
                    if (JWTUtility.verify(token.trim(), "2046001611999925") == null) {
                        errorArray.add("invalid token");
                    }
                } catch (JWTException e) {
                    errorArray.add("invalid token");
                }
            }

            if (date == null) {
                errorArray.add("missing date");
            } else if (date.length() == 0) {
                errorArray.add("blank date");
            } else {
                try {
                    //creation of an endDate that is the same date as the startDate BUT with different timestamp 
                    endDate = date + " 23:59:59";
                    date += " 00:00:00";

                    d = format.parse(date);
                    String dateTest = format.format(d);

                    String[] dateYear = date.split("-");
                    Integer year = Integer.valueOf(dateYear[0]);

                    /* check if the endDate is a valid date, 
                     for e.g. 30 feb converted to date obj and back to string  */
                    if (!date.equals(dateTest)) {
                        errorArray.add("invalid date");
                    } else if (year < 2011
                            || year > 2015) {
                        errorArray.add("invalid date");
                    }
                } catch (ParseException e) {
                    results.addProperty("status", "error");
                    errorArray.add("invalid date");
                }
            }

            if (yearFilter == null) {
                errorArray.add("missing year filter");
            } else {
                if (yearFilter.length() == 0) {
                    errorArray.add("blank year filter");
                } else if (yearFilter.equalsIgnoreCase("na")) {
                    yearFilter = "null";
                } else {
                    try {
                        int yearFilterInInt = Integer.parseInt(yearFilter);
                        if (yearFilterInInt > 2015 || yearFilterInInt < 2011) {
                            errorArray.add("invalid year filter");
                        }
                    } catch (NumberFormatException e) {
                        errorArray.add("invalid year filter");
                    }

                }
            }

            if (genderFilter == null) {
                errorArray.add("missing gender filter");
            } else {
                if (genderFilter.length() == 0) {
                    errorArray.add("blank gender filter");
                } else if (genderFilter.equalsIgnoreCase("na")) {
                    genderFilter = "null";
                } else if (!genderFilter.equals("m") && !genderFilter.equals("f")) {
                    errorArray.add("invalid gender filter");
                }
            }

            if (schoolFilter == null) {
                errorArray.add("missing school filter");
            } else {
                if (schoolFilter.length() == 0) {
                    errorArray.add("blank school filter");
                } else if (schoolFilter.equalsIgnoreCase("na")) {
                    schoolFilter = "null";
                } else if (!schoolFilter.equals("accountancy") && !schoolFilter.equals("business") && !schoolFilter.equals("sis") && !schoolFilter.equals("socsc") && !schoolFilter.equals("economics") && !schoolFilter.equals("law")) {
                    errorArray.add("invalid school filter");
                }
            }

            if (errorArray.size() == 0) {
                // no errors
                BasicAppController baCtr = new BasicAppController();
                LinkedHashMap<String, Integer> diurnalResult = baCtr.diurnalPattern(date, endDate, schoolFilter, genderFilter, yearFilter, "null");

                for (int i = 0; i < 24; i++) {
                    // i stands for the hours in a day
                    // loop through all the hours in a day to get their individual time usage from diurnalResult
                    JsonObject success = new JsonObject();
                    int time = 0;
                    String period = "";
                    if (i < 10) {
                        if (i != 9) {
                            period = "0" + i + ":00-0" + (i + 1) + ":00";

                        } else {
                            period = "0" + i + ":00-" + (i + 1) + ":00";
                        }
                        time = diurnalResult.get(period.substring(0, 5) + ":00 - " + period.substring(6, 11) + ":00");
                    } else if (i < 23) {
                        period = i + ":00-" + (i + 1) + ":00";
                        time = diurnalResult.get(period.substring(0, 5) + ":00 - " + period.substring(6, 11) + ":00");
                    } else {
                        period = "23:00-00:00";
                        time = diurnalResult.get("23:00:00 - 00:00:00");
                    }
                    success.addProperty("period", period);
                    success.addProperty("duration", time);
                    successArray.add(success);
                }

                results.addProperty("status", "success");
                results.add("breakdown", successArray);
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
