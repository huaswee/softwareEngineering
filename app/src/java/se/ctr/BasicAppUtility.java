package se.ctr;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import se.entity.User;

@WebServlet(name = "BasicAppUtility", urlPatterns = {"/BasicAppUtility"})
public class BasicAppUtility extends HttpServlet {

    int dateNum = 1;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final String[] schools = new String[]{"accountancy", "business", "socsc", "economics", "law", "sis"};
    BasicAppController baCtr = new BasicAppController();

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
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String reportType = request.getParameter("reportType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        ArrayList<String> err = new ArrayList<String>();

        String firstSort = request.getParameter("firstSort");
        String secondSort = request.getParameter("secondSort");
        String thirdSort = request.getParameter("thirdSort");
        String fourthSort = request.getParameter("fourthSort");

        String school = request.getParameter("school");
        String gender = request.getParameter("gender");
        String year = request.getParameter("year");
        //Search input to display
        String searchInput = "";

        if (firstSort == null) {
            firstSort = "null";
        }

        if (secondSort == null) {
            secondSort = "null";
        }

        if (thirdSort == null) {
            thirdSort = "null";
        }

        if (fourthSort == null) {
            fourthSort = "null";
        }

        if (school == null) {
            school = "null";
        }

        if (gender == null) {
            gender = "null";
        }

        if (year == null) {
            year = "null";
        }

        //Check for null in dates
        if (user != null && reportType == null) {
            response.sendRedirect("main.jsp");
            return;
        } else if (user == null && reportType == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        if (reportType.equals("diurnalPattern")) {
            if (startDate == null || startDate.length() == 0) {
                err.add("Incorrect Date Field: Date Cannot be empty field");

            } else {
                //Date Validation
                Date start = null;
                //Check if dates are within range (2011(inclusive) - 2015(inclusive))

                try {
                    endDate = startDate + " 23:59:59";
                    startDate += " 00:00:00";
                    //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    start = format.parse(startDate);

                    String[] startDateYear = startDate.split("-");
                    Integer startYear = Integer.valueOf(startDateYear[0]);

                    if (startYear < 2011
                            || startYear > 2015) {
                        err.add("Incorrect Date Field: Date out of range, choose date between 2011(inclusive) -2015 (inclusive)");
                    }

                } catch (ParseException ex) {
                    err.add("Incorrect Date Field: Please Enter the dates in the correct format: yyyy-mm-dd");
                }

            }

        } else {
            if (startDate == null || endDate == null || startDate.length() <= 0 || endDate.equals("")) {
                err.add("Incorrect Date Field: Date Cannot be empty field");

            } else {

                Date start = null;
                Date end = null;

                //Date Validation
                try {
                    startDate += " 00:00:00";
                    endDate += " 23:59:59";
                    start = format.parse(startDate);
                    end = format.parse(endDate);
                    String startDateTest = format.format(start);
                    String endDateTest = format.format(end);
                    dateNum = Integer.parseInt(String.valueOf((end.getTime() + 1000 - start.getTime()) / 1000 / 60 / 60 / 24));
                    //Check if dates are within range (2011(inclusive) - 2015(inclusive))
                    String[] startDateYear = startDate.split("-");
                    String[] endDateYear = endDate.split("-");
                    Integer startYear = Integer.valueOf(startDateYear[0]);
                    Integer endYear = Integer.valueOf(endDateYear[0]);

                    if (!startDateTest.equals(startDate) || !endDateTest.equals(endDate)) {
                        err.add("Incorrect Date Field: Please Enter the dates in the correct format: yyyy/mm/dd");
                    } else if (startYear < 2011
                            || startYear > 2015
                            || endYear < 2011
                            || endYear > 2015) {
                        err.add("Incorrect Date Field: Date out of range, choose date between 2011(inclusive) -2015 (inclusive)");
                    } else if (start.compareTo(end) > 0) {
                        err.add("Incorrect Date Field: Start Date should be before End Date");

                    }
                } catch (ParseException ex) {
                    err.add("Incorrect Date Field: Please Enter the dates in the correct format: yyyy/mm/dd");
                }
            }
        }

        if ((firstSort.equals(secondSort) && !firstSort.equals("null"))
                || (firstSort.equals(thirdSort) && !firstSort.equals("null"))
                || (firstSort.equals(fourthSort) && !firstSort.equals("null"))
                || (secondSort.equals(thirdSort) && !secondSort.equals("null"))
                || (secondSort.equals(fourthSort) && !secondSort.equals("null"))
                || (thirdSort.equals(fourthSort) && !thirdSort.equals("null"))) {
            err.add("Incorrect Filter field: Filter inputs cannot be the same.");
        }

        if (err.size() > 0) {
            request.setAttribute("err", err);
            RequestDispatcher view = request.getRequestDispatcher(reportType + ".jsp");
            view.forward(request, response);
            return;
        }

        //Respond to appropriate report type
        switch (reportType) {
            case "usageTimeCat":
                String[] results = baCtr.usageTimeCategory(startDate, endDate);
                session.setAttribute("results", results);
                searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" ")) + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "));
                break;

            case "usageTimeCatDemo":

                ArrayList<String[]> demoResult = baCtr.usageTimeCategoryDemo(startDate, endDate, firstSort, secondSort, thirdSort, fourthSort, false);
                if (demoResult == null) {
                    err.add("Incorrect Filter field: Filter inputs cannot be the same");
                    request.setAttribute("err", err);
                    RequestDispatcher rd = request.getRequestDispatcher("usageTimeCatDemo.jsp");
                    rd.forward(request, response);
                    return;
                }
                if (firstSort.equals("null") && secondSort.equals("null") && thirdSort.equals("null") && fourthSort.equals("null")) {
                    // if no filters specified, the controller will redirect to basicAppUsage, so the first result
                    // in demoResult will be the mild, normal and intense users.
                    String[] result = demoResult.get(0);
                    session.setAttribute("results", result);
                    response.sendRedirect("usageTimeCat.jsp");
                    return;
                }
                
                session.setAttribute("results", demoResult);
                searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" ")) + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "));
                if (!firstSort.equals("null")) {
                    searchInput += "<br/> <b>First Filter: </b>" + firstSort;
                }
                if (!secondSort.equals("null")) {
                    searchInput += "<br/> <b>Second Filter: </b>" + secondSort;
                }
                if (!thirdSort.equals("null")) {
                    searchInput += "<br/> <b>Third Filter: </b>" + thirdSort;
                }
                if (!fourthSort.equals("null")) {
                    searchInput += "<br/> <b>Fourth Filter: </b>" + fourthSort;
                }
                break;

            case "appCategory":
                LinkedHashMap<String, Double> appCatResults = baCtr.appCategory(startDate, endDate);
                session.setAttribute("results", appCatResults);
                searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" ")) + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "));
                break;

            case "diurnalPattern":

                LinkedHashMap<String, Integer> diurnalResult = baCtr.diurnalPattern(startDate, endDate, school, gender, year, "null");
                searchInput = "<b>Date: </b>" + startDate.substring(0, startDate.indexOf(" "));
                if (!school.equals("null")) {
                    searchInput += "<br/> <b>School Filter: </b>" + school;
                }
                if (!gender.equals("null")) {
                    searchInput += "<br/> <b>Gender Filter: </b>" + gender;
                }
                if (!year.equals("null")) {
                    searchInput += "<br/> <b>Year Filter: </b>" + year;
                }

                session.setAttribute("results", diurnalResult);

                break;

        }
        session.setAttribute("searchInput", searchInput);
        response.sendRedirect(reportType + ".jsp");
        return;

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
