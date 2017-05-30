package se.ctr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import se.entity.User;

/*
 javax.servlet.annotation.MultipartConfig, is used to indicate that the servlet on which it is 
 declared expects requests to made using the multipart/form-data MIME type.

 fileSizeThreshold: The file size in bytes after which the file will be temporarily stored on disk. The default size is 0 bytes.

 MaxFileSize: The maximum size allowed for uploaded files, in bytes. If the size of any uploaded file is 
 greater than this size, the web container will throw an exception (IllegalStateException). The default size is unlimited.

 maxRequestSize: The maximum size allowed for a multipart/form-data request, in bytes. The web container 
 will throw an exception if the overall size of all uploaded files exceeds this threshold. The default size is unlimited.

 */

/**
 *
 * @author USER
 */

@WebServlet("/BootstrapServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 200,
        maxFileSize = 1024 * 1024 * 400,
        maxRequestSize = 1024 * 1024 * 800)
public class BootstrapServlet extends HttpServlet {

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
        // Validation for login user
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Check if user submitted correct file by checking multipart type
        if (request.getContentType() == null || !request.getContentType().toLowerCase().contains("multipart/form-data")) {
            String error = "Please select a File";
            request.setAttribute("error", error);
            RequestDispatcher view = request.getRequestDispatcher("bootstrapMain.jsp");
            view.forward(request, response);
            return;
        }

        // Retrieves <input type="file" name="file">
        Part filePart = request.getPart("zipFile");
        String clientFileName = filePart.getName();

        // Validation for file, blank files
        if (clientFileName == null) {
            String error = "Please select a File";
            request.setAttribute("error", error);
            RequestDispatcher view = request.getRequestDispatcher("bootstrapMain.jsp");
            view.forward(request, response);
            return;
        }

        // Validation for file, blank files
        if (clientFileName.trim().equals("")) {
            String error = "Please select a File";
            request.setAttribute("error", error);
            RequestDispatcher view = request.getRequestDispatcher("bootstrapMain.jsp");
            view.forward(request, response);
            return;
        }

        InputStream formFile = filePart.getInputStream();
        // Puts InputStream into ZipInputStream to extract files in Zip File
        ZipInputStream zipIn = new ZipInputStream(formFile);
        ZipEntry entry = null;

        // Initialize Bytes Arrays to store content for each CSV file
        byte[] demo;
        byte[] appLookup;
        byte[] app;

        // Initialize BufferedReader to read Bytes 
        BufferedReader brDemo = null;
        BufferedReader brAppLookup = null;
        BufferedReader brApp = null;

        // Loop through each file entry in zip file
        while ((entry = zipIn.getNextEntry()) != null) {
            String fileName = entry.getName();

            if (fileName.equals("demographics.csv")) {
                // IOUtils library copies bytes of demographic.csv into pre-initialized byte[] demo
                demo = IOUtils.toByteArray(zipIn);
                // Holds byte[]demo as Inputstream
                InputStream is = new ByteArrayInputStream(demo);
                // InputStreamReader reads byte[]demo
                InputStreamReader isr = new InputStreamReader(is);
                // Encapsulates InputStreamReader as BufferedReader buffering characters to provide efficient reading of bytes
                brDemo = new BufferedReader(isr);

            }
            if (fileName.equals("app-lookup.csv")) {
                // IOUtils library copies bytes of app-lookup.csv into pre-initialized byte[]appLookup
                appLookup = IOUtils.toByteArray(zipIn);
                // Holds byte[]demo as Inputstream
                InputStream is = new ByteArrayInputStream(appLookup);
                // InputStreamReader reads byte[]appLookup, UTF-8 enables proper storage of non-ascii characters
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                // Encapsulates InputStreamReader as BufferedReader buffering characters to provide efficient reading of bytes
                brAppLookup = new BufferedReader(isr);

            }
            if (fileName.equals("app.csv")) {
                // IOUtils library copies bytes of app.csv into pre-initialized byte[] app
                app = IOUtils.toByteArray(zipIn);
                // Holds byte[]demo as Inputstream
                InputStream is = new ByteArrayInputStream(app);
                // InputStreamReader reads byte[]app
                InputStreamReader isr = new InputStreamReader(is);
                // Encapsulates InputStreamReader as BufferedReader buffering characters to provide efficient reading of bytes
                brApp = new BufferedReader(isr);

            }
            zipIn.closeEntry();
        }
        zipIn.close();

        // Retrieve action chosen by admin
        String function = (String) request.getParameter("function");
        BootstrapController bCtrl = new BootstrapController();

        // Admin choose to Bootstrap
        if (function.equals("bootstrap")) {
            // Initialize HashMap to store error messages after bootstrapping
            HashMap<String, TreeMap<Integer, ArrayList<String>>> errorMap = bCtrl.bootstrap(brDemo, brAppLookup, brApp, function);

            // Validation for wrong file input or errors during reading of files
            if (errorMap == null) {
                String error = "Please choose zip with the correct csv files inside";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("bootstrapMain.jsp");
                view.forward(request, response);
                return;
            }

            // Returns all error in respective files to be displayed in bootstrapMain.jsp
            session.setAttribute("errorListDemo", errorMap.get("errorListDemo"));
            session.setAttribute("errorListAppLookup", errorMap.get("errorListAppLookup"));
            session.setAttribute("errorListApp", errorMap.get("errorListApp"));
            response.sendRedirect("bootstrapMain.jsp");

            // Admin choose to Add additional File
        } else if (function.equals("addFile")) {

            // Initialize HashMap to store error messages after bootstrapping
            HashMap<String, TreeMap<Integer, ArrayList<String>>> errorMap = bCtrl.addFiles(brDemo, brApp, function);
            // Validation for wrong file input or errors during reading of files
            if (errorMap == null) {
                String error = "Please choose zip with the correct csv files inside";
                request.setAttribute("error", error);
                RequestDispatcher view = request.getRequestDispatcher("bootstrapMain.jsp");
                view.forward(request, response);
                return;
            }

            // Returns all error in respective files to be displayed in bootstrapMain.jsp
            session.setAttribute("errorListDemo", errorMap.get("errorListDemo"));
            session.setAttribute("errorListAppLookup", errorMap.get("errorListAppLookup"));
            session.setAttribute("errorListApp", errorMap.get("errorListApp"));
            response.sendRedirect("bootstrapMain.jsp");
        } else {

            // Validation for function
            String error = "Please choose a correct function";
            request.setAttribute("error", error);
            RequestDispatcher view = request.getRequestDispatcher("bootstrapMain.jsp");
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
