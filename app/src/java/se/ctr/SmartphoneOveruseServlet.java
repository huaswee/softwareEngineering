package se.ctr;

import se.dao.AppDAO;
import se.entity.App;
import se.entity.User;
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

@WebServlet(name = "SmartphoneOveruseServlet", urlPatterns = {"/SmartphoneOveruseServlet"})
public class SmartphoneOveruseServlet extends HttpServlet {

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

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        ArrayList<String> error = new ArrayList<>();

        Date start = null;
        Date end = null;
        // validation for dates field
        if (startDate == null || endDate == null) {
            error.add("Incorrect Date Field: Date Cannot be empty field");
        } else if (startDate.trim().equals("") || endDate.trim().equals("")) {
            error.add("Incorrect Date Field: Date Cannot be blank");
        } else {
            try {
                // add the time to start date
                startDate += " 00:00:00";
                // add the time to the end date
                endDate += " 23:59:59";
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                start = format.parse(startDate);
                end = format.parse(endDate);

                String[] startDateYear = startDate.split("-");
                String[] endDateYear = endDate.split("-");

                Integer startYear = Integer.valueOf(startDateYear[0]);
                Integer endYear = Integer.valueOf(endDateYear[0]);
                //Check if dates are within range (2011(inclusive) - 2015(inclusive))     
                if (!format.format(start).equals(startDate) && !format.format(end).equals(endDate)) {
                    error.add("Incorrect Date Field: Incorrect Date Format");
                } else if (startYear < 2011
                        || startYear > 2015 || endYear < 2011
                        || endYear > 2015) {
                    error.add("Incorrect Date Field: Date out of range, choose date between 2011(inclusive) -2015 (inclusive)");
                } else if (start.compareTo(end) > 0) {
                    error.add("Incorrect Date Field: Start Date should be before End Date");
                }
            } catch (ParseException ex) {
                error.add("Incorrect Date Field: Please Enter the dates in the correct format: yyyy-mm-dd");
            }
        }

        if (error.size() > 0) {
            request.setAttribute("err", error);
            RequestDispatcher dispatch = request.getRequestDispatcher("smartphoneOveruse.jsp");
            dispatch.forward(request, response);
            return;
        }
        
        // if everything is correct, get the logged in user data
        LinkedHashMap<App, String> appData = AppDAO.getLoggedInUserAppData(startDate, endDate, user.getHashedMAC());
        String searchInput = "";
        String[][] results = null;

        SmartphoneOveruseController soCtr = new SmartphoneOveruseController();
        results = soCtr.smartphoneOveruseReport(start, end, appData);

        //this is to let the user know their input values
        searchInput = "<b>Start Date: </b>" + startDate.substring(0, startDate.indexOf(" "))
                + ", <b>End Date: </b>" + endDate.substring(0, endDate.indexOf(" "));
        session.setAttribute("results", results);

        session.setAttribute("searchInput", searchInput);
        response.sendRedirect("smartphoneOveruse.jsp");
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
