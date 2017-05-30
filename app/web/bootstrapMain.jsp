<%@page import="java.util.Iterator"%>
<%@page import="se.dao.AppDAO"%>
<%@page import="se.dao.AppLookupDAO"%>
<%@page import="se.dao.DemographicDAO"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMUA || Bootstrap</title>
    </head>
    <body class="page-body">
        <!--Navigation File-->
        <%@include file="adminNav.jsp" %>
        <!--Boarder-->
        <ol class="breadcrumb bc-2">
            <li>
                <i class="entypo-folder"></i>
                <a href="adminMain.jsp">Dashboard</a>
            </li>
            <li class="active">
                <strong>Bootstrap</strong>
            </li>
        </ol>
        <!--Form-->
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-primary" data-collapsed="0">

                    <div class="panel-heading">
                        <div class="panel-title">
                            Bootstrap Data
                        </div>
                    </div>

                    <div class="panel-body">
                        <form action="BootstrapServlet" enctype="multipart/form-data" method="post" class="form-horizontal form-groups-bordered">
                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Zip-File to Upload</label>
                                <div class="col-sm-5">
                                    <input type="file" name="zipFile" class="form-control file2 inline btn btn-primary" data-label="<i class='glyphicon glyphicon-file'></i> Browse" />
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Bootstrap Function</label>
                                <div class="col-sm-5">
                                    <select name="function" class="form-control">
                                        <option value="bootstrap">bootstrap</option>
                                        <option value="addFile">add file</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-5">
                                    <%
                                        //Error Message Display
                                        String error = (String) request.getAttribute("error");
                                        if (error != null) {
                                            out.println(error + "<br/>");
                                        }

                                    %>
                                    <input type="image" src="images/Icon_BS_Upload.png"  width="80" height="80" alt="upload" />
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        <!--Display Results-->
        <div class="row">
            <div class="col-md-12">
                <%                    TreeMap<Integer, ArrayList<String>> errorListDemo = (TreeMap) session.getAttribute("errorListDemo");
                    TreeMap<Integer, ArrayList<String>> errorListAppLookup = (TreeMap) session.getAttribute("errorListAppLookup");
                    TreeMap<Integer, ArrayList<String>> errorListApp = (TreeMap) session.getAttribute("errorListApp");

                    if (errorListDemo == null && errorListAppLookup == null && errorListApp == null) {
                        return;
                    }
                %>
                <h4><b>Bootstrapped!</b></h4>
                <%
                    if (errorListAppLookup != null) {

                        Iterator<Integer> iterAppLookup = errorListAppLookup.keySet().iterator();
                        int appLookupRowsInserted = AppLookupDAO.getAllAppLookup().size();
                %>
                <h5><b>App-Lookup</b></h5>
                <table class='table table-bordered'>
                    <tr>
                        <td>File : app-lookup.csv</td>
                    </tr>
                    <tr>
                        <td>Rows Inserted : <%=appLookupRowsInserted%></td>
                    </tr>
                </table>
                    <table class='table table-bordered'>
                    <%
                        while (iterAppLookup.hasNext()) {
                            int lineCount = iterAppLookup.next();
                            ArrayList<String> errors = errorListAppLookup.get(lineCount);
                    %>
                    <tr>
                        <td>Line : <%=lineCount%></td>
                        <td>
                            <ul>

                                <%
                                    for (String e : errors) {
                                %>
                                <li><%=e%></li>

                                <%
                                    }
                                %>
                            </ul>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </table>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <%
                    if (errorListApp != null) {
                        Iterator<Integer> iterApp = errorListApp.keySet().iterator();
                        int appRowsInserted = AppDAO.getAppsInserted();
                %>
                <h5><b>App</b></h5>
                <table class='table table-bordered'>
                    <tr>
                        <td>File : app.csv</td>
                    </tr>
                    <tr>
                        <td>Rows Inserted : <%=appRowsInserted%></td>
                    </tr>
                </table>
                    <table class='table table-bordered'>
                    <%
                        while (iterApp.hasNext()) {
                            int lineCount = iterApp.next();
                            ArrayList<String> errors = errorListApp.get(lineCount);
                    %>
                    <tr>
                        <td>Line : <%=lineCount%></td>
                        <td>
                            <ul>
                                <%
                                    for (String e : errors) {
                                %>
                                <li><%=e%></li>

                                <%
                                    }
                                %>
                            </ul>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </table>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">

                <%
                    if (errorListDemo != null) {
                        Iterator<Integer> iterDemo = errorListDemo.keySet().iterator();
                        int demoRowsInserted = DemographicDAO.getAllUsers().size();
                %>
                <h5><b>Demographics</b></h5>
                <table class='table table-bordered'>
                    <tr>
                        <td>File : demographics.csv</td>
                    </tr>
                    <tr>
                        <td>Rows Inserted : <%=demoRowsInserted%></td>
                    </tr>
                </table>
                    <table class='table table-bordered'>

                    <%
                        while (iterDemo.hasNext()) {
                            int lineCount = iterDemo.next();
                            ArrayList<String> errors = errorListDemo.get(lineCount);
                    %>
                    <tr>
                        <td>Line : <%=lineCount%></td>
                        <td>
                            <ul>
                                <%
                                    for (String e : errors) {
                                %>
                                <li><%=e%></li>

                                <%
                                    }
                                %>
                            </ul>
                        </td>
                    </tr>

                    <%
                            }
                        }
                    %>
                </table>                   
            </div>
        </div>
        <%            session.removeAttribute("errorListDemo");
            session.removeAttribute("errorListAppLookup");
            session.removeAttribute("errorListApp");

            DemographicDAO.clearMemory();
            AppLookupDAO.clearMemory();
            AppDAO.clearMemory();
        %>


        <!-- Imported scripts on this page -->
        <script src="assets/js/fileinput.js"></script>
    </body>
</html>
