package se.ctr;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import se.entity.User;

@WebServlet(name = "TopKUtility", urlPatterns = {"/TopKUtility"})
public class TopKUtility extends HttpServlet {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        /*try (PrintWriter out = response.getWriter()) {
         /* TODO output your page here. You may use following sample code. 

         }*/

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String topKType = request.getParameter("topKType");
        String school = request.getParameter("school");
        String appCat = request.getParameter("appCat");
        String kStr = request.getParameter("K");
        ArrayList<String> err = new ArrayList<String>();
        //Return user's search variables
        String searchInput = "";

        if (user == null && topKType == null) {
            response.sendRedirect("login.jsp");
            return;
        } else if (user != null && topKType == null) {
            response.sendRedirect("main.jsp");
            return;
        }

        Date start = null;
        Date end = null;

        if (startDate == null || startDate.trim().length() == 0 || endDate == null || endDate.trim().length() == 0) {
            err.add("Incorrect Date Field: Date Cannot be empty field");
        } else {
            //Date Validation
            try {
                startDate += " 00:00:00";
                endDate += " 23:59:59";
                //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                start = format.parse(startDate);
                end = format.parse(endDate);
                String startDateTest = format.format(start);
                String endDateTest = format.format(end);

                String[] startDateYear = startDate.split("-");
                String[] endDateYear = endDate.split("-");
                int startYear = Integer.parseInt(startDateYear[0]);
                int endYear = Integer.parseInt(endDateYear[0]);

                if (!startDateTest.equals(startDate) || !endDateTest.equals(endDate)) {
                    err.add("Incorrect Date Field: Please Enter the dates in the correct format: yyyy/mm/dd");
                } else if (startYear < 2011 || startYear > 2015 || endYear < 2011 || endYear > 2015) {
                    err.add("Incorrect Date Field: Date out of range, choose date between 2011(inclusive) & 2015(inclusive)");
                } else if (start.compareTo(end) > 0) {
                    err.add("Incorrect Date Field: Start Date should be before End Date");
                }
            } catch (ParseException ex) {
                err.add("Incorrect Date Field: Please Enter the dates in the correct format: yyyy/mm/dd");
            }
        }

        int k = 0;

        try {
            k = Integer.parseInt(kStr);
        } catch (NumberFormatException e) {
            err.add("Invalid K Value. Please Enter a Whole Number.");
        }

        if (k <= 0 || k > 10) {
            err.add("Invalid K Value. Please enter a value between 1 to 10.");
        }

        if ((appCat == null || appCat.length() <= 0) && (topKType.equals("topKSchools") || topKType.equals("topKStudents"))) {
            err.add("Invalid App Cat Value. Please enter a valid App Category.");
        }

        if ((school == null || school.length() <= 0) && topKType.equals("topKApps")) {
            err.add("invalid School value. Please enter a valid School.");
        }

        if (err.size() > 0) {
            request.setAttribute("err", err);
            RequestDispatcher view = request.getRequestDispatcher(topKType + ".jsp");
            view.forward(request, response);
            return;
        }

        ArrayList<String[]> results = new ArrayList<String[]>();

        switch (topKType) {
            case "topKApps":
                results = TopKController.topKApps(startDate, endDate, school, k);
                searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" ")) + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "))
                        + "<br/> <b>School: </b>" + school + ",<br/> <b>Input K: </b>" + k;

                break;

            case "topKStudents":
                results = TopKController.topKStudents(startDate, endDate, appCat, k);
                searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" ")) + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "))
                        + "<br/> <b>App Category: </b>" + appCat + ",<br/> <b>Input K: </b>" + k;
                break;

            case "topKSchools":
                results = TopKController.topKSchools(startDate, endDate, appCat, k);
                searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" ")) + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "))
                        + "<br/> <b>App Category: </b>" + appCat + ",<br/> <b>Input K: </b>" + k;

                break;

        }
        if (results == null || results.isEmpty()) {
            err.add("There is no App usage for the given criteria");
            request.setAttribute("err", err);
            RequestDispatcher dispatch = request.getRequestDispatcher(topKType + ".jsp");
            dispatch.forward(request, response);
            return;
        }

        session.setAttribute("searchInput", searchInput);
        session.setAttribute("results", results);
        response.sendRedirect(topKType + ".jsp");
     
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
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        processRequest(request, response);
//    }

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
