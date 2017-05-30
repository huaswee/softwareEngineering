package se.ctr;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import se.dao.AdminDAO;
import se.dao.DemographicDAO;
import se.entity.User;

@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {

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

	ArrayList<String> errMsg = new ArrayList<String>();
	String email = request.getParameter("username");
	String password = request.getParameter("password");

	if (email.contains("@")) {
	    errMsg.add("Invalid username/password");
	    request.setAttribute("errMsg", errMsg);
	    RequestDispatcher view = request.getRequestDispatcher("login.jsp");
	    view.forward(request, response);

	    return;

	}

	User user = DemographicDAO.getUser(email);

	if (user == null) {
	    user = AdminDAO.getAdmin(email);
	}

	if (user == null) {
	    errMsg.add("Invalid username/password");
	    request.setAttribute("errMsg", errMsg);
	    RequestDispatcher view = request.getRequestDispatcher("login.jsp");
	    view.forward(request, response);
	    return;
	}

	if (user.getPassword().equals(password)) {
	    if (user.isAdmin()) {
		HttpSession session = request.getSession();
		session.setAttribute("user", user);
		response.sendRedirect("adminMain.jsp");

	    } else {
		HttpSession session = request.getSession();
		session.setAttribute("user", user);
		response.sendRedirect("main.jsp");

	    }

	} else {
	    errMsg.add("Invalid username/password");
	    request.setAttribute("errMsg", errMsg);
	    RequestDispatcher view = request.getRequestDispatcher("login.jsp");
	    view.forward(request, response);
	    return;

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
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//	    throws ServletException, IOException {
//	processRequest(request, response);
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
