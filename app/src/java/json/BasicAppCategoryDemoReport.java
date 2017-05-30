package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.ctr.BasicAppController;

@WebServlet(name = "BasicAppCategoryDemoReport", urlPatterns = {"/json/basic-usetime-demographics-report"})
public class BasicAppCategoryDemoReport extends HttpServlet {

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
            String order = request.getParameter("order");

            Date start = null;
            Date end = null;
            // validation
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
                    // check if it's a proper startdate, e.g. converting 30 feb to string and back to date
                    
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

            String[] orders = null; // to store the correct final orders
            int numOfFilters = 0;
            if (order == null) {
                errorArray.add("missing order");
            } else if (order.length() == 0) {
                errorArray.add("blank order");
            } else {
                String[] test = order.split(",");
                for (int i = 0; i < test.length; i++) {
                    test[i] = test[i].toLowerCase();
                }
                //check if the order given is valid
                for (String str : test) {
                    if (!str.equals("year") && !str.equals("school") && !str.equals("gender") && !str.equals("cca")) {
                        errorArray.add("invalid order");
                    }
                }
                if (test.length == 1) {
                    orders = test;
                    numOfFilters = 1;
                }
                if (test.length == 2) {
                    // ensure that there is no repetition of filters
                    if (test[0].equals(test[1])) {
                        errorArray.add("invalid order");
                    } else {
                        orders = test;
                        numOfFilters = 2;
                    }
                }
                if (test.length == 3) {
                    // ensure that there is no repetition of filters
                    if (test[0].equals(test[1]) || test[0].equals(test[2]) || test[1].equals(test[2])) {
                        errorArray.add("invalid order");
                    } else {
                        orders = test;
                        numOfFilters = 3;
                    }
                }
                if (test.length == 4) {
                    // ensure that there is no repetition of filters
                    if (test[0].equals(test[1]) || test[0].equals(test[2]) || test[0].equals(test[3]) || test[1].equals(test[2]) || test[1].equals(test[3]) || test[2].equals(test[3])) {
                        errorArray.add("invalid order");
                    } else {
                        orders = test;
                        numOfFilters = 4;
                    }
                }
            }

            if (errorArray.size() == 0) {
                results.addProperty("status", "success");

                BasicAppController baCtr = new BasicAppController();
                ArrayList<String[]> demoResult = null;
                switch (orders.length) {
                    case 1:
                        demoResult = baCtr.usageTimeCategoryDemo(startDate, endDate, orders[0], "null", "null", "null", true);
                        break;
                    case 2:
                        demoResult = baCtr.usageTimeCategoryDemo(startDate, endDate, orders[0], orders[1], "null", "null", true);
                        break;
                    case 3:
                        demoResult = baCtr.usageTimeCategoryDemo(startDate, endDate, orders[0], orders[1], orders[2], "null", true);
                        break;
                    case 4:
                        demoResult = baCtr.usageTimeCategoryDemo(startDate, endDate, orders[0], orders[1], orders[2], orders[3], true);
                        break;
                }

                JsonArray firstFilter = new JsonArray();
                JsonArray secFilter = new JsonArray();
                JsonArray thirdFilter = new JsonArray();
                JsonArray fourthFilter = new JsonArray();
                String[] first = null;
                String[] sec = null;
                String[] third = null;
                String[] fourth = null;

                JsonArray breakdownArr = null;
                /* reading the demoResult from bottom up 
                because we need to nest json arrays into json arrays */
                for (int i = demoResult.size() - 1; i >= 0; i--) {
                    //demoResult is an ArrayList of String array
                    String[] result = demoResult.get(i);
                    if (result.length == 1) {
                        first = result;
                        JsonObject temp = new JsonObject();
                        String name = first[0].substring(0, first[0].indexOf(":") - 1);
                        int count = Integer.parseInt(first[0].substring(first[0].lastIndexOf(" ") + 1, first[0].indexOf("(")));
                        int percent = Integer.parseInt(first[0].substring(first[0].indexOf("(") + 1, first[0].indexOf(")") - 1));

                        if (orders[0].equals("year")) {
                            temp.addProperty(orders[0], Integer.parseInt(name));
                        } else {
                            temp.addProperty(orders[0], name);
                        }
                        temp.addProperty("count", count);
                        temp.addProperty("percent", percent);

                        // check the previous filter if there are any results, if there is then add into current filter
                        if (secFilter.size() != 0) {
                            temp.add("breakdown", secFilter);
                            
                            //restart the current filter
                            secFilter = new JsonArray();
                        } else {
                            temp.add("breakdown", breakdownArr);
                        }
                        firstFilter.add(temp);
                    } else if (result.length == 2) {
                        sec = result;
                        JsonObject temp = new JsonObject();
                        String name = sec[1].substring(0, sec[1].indexOf(":") - 1);
                        int count = Integer.parseInt(sec[1].substring(sec[1].lastIndexOf(" ") + 1, sec[1].indexOf("(")));
                        int percent = Integer.parseInt(sec[1].substring(sec[1].indexOf("(") + 1, sec[1].indexOf(")") - 1));

                        if (orders[1].equals("year")) {
                            temp.addProperty(orders[1], Integer.parseInt(name));
                        } else {
                            temp.addProperty(orders[1], name);
                        }
                        temp.addProperty("count", count);
                        temp.addProperty("percent", percent);

                        // check the previous filter if there are any results, if there is then add into current filter
                        if (thirdFilter.size() != 0) {
                            temp.add("breakdown", thirdFilter);
                            
                            //restart the current filter
                            thirdFilter = new JsonArray();
                        } else {
                            temp.add("breakdown", breakdownArr);
                        }
                        secFilter.add(temp);
                    } else if (result.length == 3) {
                        third = result;
                        JsonObject temp = new JsonObject();

                        String name = third[2].substring(0, third[2].indexOf(":") - 1);
                        int count = Integer.parseInt(third[2].substring(third[2].lastIndexOf(" ") + 1, third[2].indexOf("(")));
                        int percent = Integer.parseInt(third[2].substring(third[2].indexOf("(") + 1, third[2].indexOf(")") - 1));

                        if (orders[2].equals("year")) {
                            temp.addProperty(orders[2], Integer.parseInt(name));
                        } else {
                            temp.addProperty(orders[2], name);
                        }
                        temp.addProperty("count", count);
                        temp.addProperty("percent", percent);

                        // check the previous filter if there are any results, if there is then add into current filter
                        if (fourthFilter.size() != 0) {
                            temp.add("breakdown", fourthFilter);
                            
                            //restart the current filter
                            fourthFilter = new JsonArray();
                        } else {
                            temp.add("breakdown", breakdownArr);
                        }
                        thirdFilter.add(temp);

                    } else if (result.length == 4 && result.length == numOfFilters) {
                        // check that the result.length is the same as numOfFilter so that the values added will be correct
                        // e.g. if only has 1 order, then the result.length will not be equal to the numOfFilters
                        fourth = result;
                        JsonObject temp = new JsonObject();

                        String name = fourth[3].substring(0, fourth[3].indexOf(":") - 1);
                        int count = Integer.parseInt(fourth[3].substring(fourth[3].lastIndexOf(" ") + 1, fourth[3].indexOf("(")));
                        int percent = Integer.parseInt(fourth[3].substring(fourth[3].indexOf("(") + 1, fourth[3].indexOf(")") - 1));

                        if (orders[3].equals("year")) {
                            temp.addProperty(orders[3], Integer.parseInt(name));
                        } else {
                            temp.addProperty(orders[3], name);
                        }
                        temp.addProperty("count", count);
                        temp.addProperty("percent", percent);
                        temp.add("breakdown", breakdownArr);
                        fourthFilter.add(temp);
                    } else {
                        // for result.length > 4. Adding in values for intense, normal, mild
                        result = demoResult.get(i--);
                        JsonObject intense = new JsonObject();
                        String intenseValues = result[result.length - 1];
                        intense.addProperty("intense-count", Integer.parseInt(intenseValues.substring(0, intenseValues.indexOf("("))));
                        intense.addProperty("intense-percent", Integer.parseInt(intenseValues.substring(intenseValues.indexOf("(") + 1, intenseValues.indexOf("%"))));
                        
                        JsonObject normal = new JsonObject();
                        String normalValues = result[result.length - 2];
                        normal.addProperty("normal-count", Integer.parseInt(normalValues.substring(0, normalValues.indexOf("("))));
                        normal.addProperty("normal-percent", Integer.parseInt(normalValues.substring(normalValues.indexOf("(") + 1, normalValues.indexOf("%"))));
                        
                        JsonObject mild = new JsonObject();
                        String mildValues = result[result.length - 3];
                        mild.addProperty("mild-count", Integer.parseInt(mildValues.substring(0, mildValues.indexOf("("))));
                        mild.addProperty("mild-percent", Integer.parseInt(mildValues.substring(mildValues.indexOf("(") + 1, mildValues.indexOf("%"))));

                        breakdownArr = new JsonArray();
                        breakdownArr.add((JsonElement) intense);
                        breakdownArr.add((JsonElement) normal);
                        breakdownArr.add((JsonElement) mild);

                    }

                }
                results.add("breakdown", firstFilter);
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
