<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMUA || Top-K Students</title>
    </head>
    <body class="page-body">
        <!--Navigation File-->
        <%@include file="nav.jsp" %>
        <!--Boarder-->
        <ol class="breadcrumb bc-2">
            <li>
                <i class="entypo-folder"></i>
                <a href="main.jsp">Dashboard</a>
            </li>
            <li>Top-K Reports</li>
            <li class="active">
                <strong>Top-K Students</strong>
            </li>
        </ol>
        <!--Form-->
        <div class="row">
            <div class="col-md-12">	
                <div class="panel panel-primary" data-collapsed="0">
                    <div class="panel-heading">
                        <div class="panel-title">
                            Top-K Students
                        </div>
                        <div class="panel-options">
                            <a href="#" data-rel="collapse"><i class="entypo-down-open"></i></a>
                        </div>
                    </div>

                    <div class="panel-body">
                        <form role="form" class="form-horizontal form-groups-bordered" action="TopKUtility" method="post">
                            <div class="form-group">

                                <label class="col-sm-3 control-label">Start Date</label>
                                <div class="col-sm-5">
                                    <div class="input-group">
                                        <input type="text" class="form-control datepicker" data-format="yyyy-mm-dd" name="startDate" placeholder="YYYY-MM-DD">
                                        <div class="input-group-addon">
                                            <a href="#"><i class="entypo-calendar"></i></a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">End Date</label>
                                <div class="col-sm-5">
                                    <div class="input-group">
                                        <input type="text" class="form-control datepicker" data-format="yyyy-mm-dd" name="endDate"  placeholder="YYYY-MM-DD">
                                        <div class="input-group-addon">
                                            <a href="#"><i class="entypo-calendar"></i></a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select App Category</label>
                                <div class="col-sm-5">
                                    <select name="appCat" class="form-control">
                                        <option value="Books">Books</option>
                                        <option value="Education">Education</option>
                                        <option value="Fitness">Fitness</option>
                                        <option value="Games">Games</option>
                                        <option value="Information">Information</option>
                                        <option value="Library">Library</option>
                                        <option value="Local">Local</option>
                                        <option value="Others">Others</option>
                                        <option value="Social">Social</option>
                                        <option value="Tools">Tools</option>

                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="field-1" class="col-sm-3 control-label">Input Top-K</label>
                                <div class="col-sm-5">
                                    <select name="K" class="form-control">
                                        <option value="1">1</option>
                                        <option value="2">2</option>
                                        <option value="3">3</option>
                                        <option value="4">4</option>
                                        <option value="5">5</option>
                                        <option value="6">6</option>
                                        <option value="7">7</option>
                                        <option value="8">8</option>
                                        <option value="9">9</option>
                                        <option value="10">10</option>
                                    </select>
                                    <!--<input type="text" class="form-control" id="field-1" placeholder="Input a whole number" name="K">-->
                                </div>
                            </div>


                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-5">
                                    <input type="hidden" name="topKType" value="topKStudents"/>
                                    <%
                                        //Error Message Display
                                        ArrayList<String> errMsg = (ArrayList) request.getAttribute("err");

                                        if (errMsg != null && errMsg.size() > 0) {
                                            for (String err : errMsg) {
                                    %>

                                    <%=err%><br/><br/>

                                    <%
                                            }
                                        }

                                    %>
                                    <button type="submit" class="btn btn-default">Generate Report</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        <!--Result Display-->
        <div class="row">
            <div class="col-md-12">
                <%		    ArrayList<String[]> results = (ArrayList) session.getAttribute("results");
                    if (results != null && results.size() > 0) {
                %>
                <div class="col-md-12">
                    <h4>Results</h4>
                    <%
                        String inputs = (String) session.getAttribute("searchInput");
                    %>
                    <h5><b>Search Inputs :</b></h5>
                    <%=inputs%><br/><br/>
                    <table class="table table-bordered">
                        <tr>
                            <th>Rank</th>
                            <th>Student Name</th>
                            <th>Mac Address</th>
                            <th>Usage Time (In Seconds)</th>
                        </tr>
                        <%
                            for (String[] result : results) {
                        %>
                        <tr>
                            <td><%=result[0]%></td>
                            <td><%=result[1]%></td>
                            <td><%=result[2]%></td>
                            <td><%=result[3]%></td>
                        </tr>
                        <%
                            }
                        %>
                    </table>    
                </div>
            </div>
        </div>
        <%
            }
            session.removeAttribute("results");
            session.removeAttribute("searchInput");
        %>
    </body>
</html>
