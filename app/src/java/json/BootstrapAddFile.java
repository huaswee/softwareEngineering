package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import is203.JWTException;
import is203.JWTUtility;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import se.ctr.BootstrapController;
import se.dao.AdminDAO;
import se.dao.AppDAO;
import se.dao.AppLookupDAO;
import se.dao.DemographicDAO;

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

@WebServlet(name = "BootstrapAddFile", urlPatterns = {"/json/update"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 200,
        maxFileSize = 1024 * 1024 * 400,
        maxRequestSize = 1024 * 1024 * 800)
public class BootstrapAddFile extends HttpServlet {

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
        // Initialize PrintWriter Object
        try (PrintWriter out = response.getWriter()) {

            // Gson object to modify default compact display to prettyprintng display
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // JsonObject to store number of records inserted and errors
            JsonObject results = new JsonObject();

            // Initialize ArrayList to store all error messages
            ArrayList<String> errorArray = new ArrayList<String>();

            String token = request.getParameter("token");
            Part filePart = null;

            // Validation of generated token input
            if (token == null) {
                errorArray.add("missing token");
            } else if (token.length() == 0) {
                errorArray.add("blank token");
            } else {
                try {
                    String username = null;
                    if ((username = JWTUtility.verify(token, "2046001611999925")) == null) {
                        errorArray.add("invalid token");
                    } else {
                        if (AdminDAO.getAdmin(username) == null) {
                            errorArray.add("invalid token");
                        }
                    }
                } catch (JWTException e) {
                    errorArray.add("invalid token");
                }
            }

            // Check if user submitted correct file by checking multipart type
            if (request.getContentType() == null || !request.getContentType().toLowerCase().contains("multipart/form-data")) {
                errorArray.add("missing file");
            } else {
                // Retrieves <input type="file" name="file">
                filePart = request.getPart("bootstrap-file");

                // Validation for file
                if (filePart == null) {
                    errorArray.add("missing file");
                } else {
                    if (filePart.getSize() == 0) {
                        errorArray.add("blank file");
                    }
                }
            }

            // Validation of file and token successful , no errors
            if (errorArray.size() == 0) {
                InputStream formFile = filePart.getInputStream();
                // Puts InputStream into ZipInputStream to extract files in Zip File
                ZipInputStream zipIn = new ZipInputStream(formFile);
                ZipEntry entry = null;

                // Initialize Bytes Arrays to store content for each CSV file
                byte[] demo;
                byte[] app;

                // Initialize BufferedReader to read Bytes
                BufferedReader brDemo = null;
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

                    if (fileName.equals("app.csv")) {
                        // IOUtils library copies bytes of app.csv into pre-initialized byte[]app
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

                BootstrapController bCtrl = new BootstrapController();
                // Initialize HashMap to store error messages after bootstrapping
                HashMap<String, TreeMap<Integer, ArrayList<String>>> errorMap = bCtrl.addFiles(brDemo, brApp, "addFile");

                // Validation for wrong file input or errors during reading of files
                if (errorMap == null) {
                    results.addProperty("status", "error");
                    results.addProperty("Message", "invalid file");
                    out.println(gson.toJson(results));
                    return;
                }

                // Initialize TreeMap to store errors for each file in sequence
                TreeMap<Integer, ArrayList<String>> errorListDemo = errorMap.get("errorListDemo");
                TreeMap<Integer, ArrayList<String>> errorListApp = errorMap.get("errorListApp");

                Integer appRowsInserted = null;
                Integer demoRowsInserted = null;

                if (errorListApp != null) {
                    appRowsInserted = AppDAO.getAppsInserted();
                }
                if (errorListDemo != null) {
                    demoRowsInserted = DemographicDAO.getAllUsers().size();
                }

                // Initialize new JsonArray to store JsonObject for successfuly inserted rows
                JsonArray rowsInserted = new JsonArray();

                // Initialize new JsonArray for error messages for all files
                JsonArray resultJson = new JsonArray();

                // If zipFile submitted by user contains app.csv, appRowsInserted != null
                if (appRowsInserted != null) {
                    // Initialize temp JsonObject to add rows inserted for app.csv
                    JsonObject temp = new JsonObject();
                    temp.addProperty("app.csv", appRowsInserted);
                    // Adds JsonObject to JsonArray
                    rowsInserted.add(temp);

                    Iterator<Integer> iterApp = errorListApp.keySet().iterator();

                    // Iterating through TreeMapp errorListApp to store errors for app into JsonObject
                    while (iterApp.hasNext()) {
                        int lineCount = iterApp.next();
                        ArrayList<String> errors = errorListApp.get(lineCount);

                        // Convert all String errors into JsonPrimitive Type to be added into JsonArray
                        JsonArray errorJson = new JsonArray();
                        for (String e : errors) {
                            errorJson.add(new JsonPrimitive(e));
                        }

                        // Initialize new temp JsonObject to store all JsonObjects for app
                        JsonObject tempObject = new JsonObject();
                        tempObject.addProperty("file", "app.csv");
                        tempObject.addProperty("line", lineCount);
                        tempObject.add("message", errorJson);
                        resultJson.add(tempObject);
                    }
                }

                // If zipFile submitted by user contains demographics.csv, demoRowsInserted != null
                if (demoRowsInserted != null) {
                    // Initialize temp JsonObject to add rows inserted for demographics.csv
                    JsonObject temp = new JsonObject();
                    temp.addProperty("demographics.csv", demoRowsInserted);
                    // Adds JsonObject to JsonArray
                    rowsInserted.add(temp);

                    Iterator<Integer> iterDemo = errorListDemo.keySet().iterator();

                    // Iterating through TreeMapp errorListApp to store errors for app into JsonObject
                    while (iterDemo.hasNext()) {
                        int lineCount = iterDemo.next();
                        ArrayList<String> errors = errorListDemo.get(lineCount);

                        // Convert all String errors into JsonPrimitive Type to be added into JsonArray
                        JsonArray errorJson = new JsonArray();
                        for (String e : errors) {
                            errorJson.add(new JsonPrimitive(e));
                        }

                        // Initialize new temp JsonObject to store all JsonObjects for app
                        JsonObject tempObject = new JsonObject();
                        tempObject.addProperty("file", "demographic.csv");
                        tempObject.addProperty("line", lineCount);
                        tempObject.add("message", errorJson);
                        resultJson.add(tempObject);
                    }

                }

                // Check for existing errors
                if (errorListApp.size() != 0 || errorListDemo.size() != 0) {
                    results.addProperty("status", "error");
                    results.add("num-record-loaded", rowsInserted);
                    results.add("error", resultJson);
                } else {
                    // Bootstrap successful
                    results.addProperty("status", "success");
                    results.add("num-record-loaded", rowsInserted);
                }
                
                // Clear data store in memory
                DemographicDAO.clearMemory();
                AppLookupDAO.clearMemory();
                AppDAO.clearMemory();
                out.println(gson.toJson(results));
                
            } else {
                // If input error exists
                // Sorts errorArray
                Collections.sort(errorArray);
                
                // Converts errorArray into JsonArray
                JsonArray errorJsonArray = (JsonArray) new Gson().toJsonTree(errorArray,
                        new TypeToken<ArrayList<String>>() {
                        }.getType());
                
                // Stores error messages for input errors into final display JsonObject
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
