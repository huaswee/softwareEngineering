<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMUA || Diurnal Pattern</title>
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
            <li>Basic App Report</li>
            <li class="active">
                <strong>Diurnal Pattern</strong>
            </li>
        </ol>
        <!--Form-->
        <div class="row">
            <div class="col-md-12">	
                <div class="panel panel-primary" data-collapsed="0">
                    <div class="panel-heading">
                        <div class="panel-title">
                            Diurnal Pattern
                        </div>
                        <div class="panel-options">
                            <a href="#" data-rel="collapse"><i class="entypo-down-open"></i></a>
                        </div>
                    </div>

                    <div class="panel-body">
                        <form role="form" class="form-horizontal form-groups-bordered" action="BasicAppUtility" method="post">
                            <div class="form-group">

                                <label class="col-sm-3 control-label">Date</label>
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
                                <label class="col-sm-3 control-label">Select School</label>
                                <div class="col-sm-5">
                                    <select name="school" class="form-control">
                                        <option value="null">None</option>
                                        <option value="business">business</option>
                                        <option value="socsc">socsc</option>
                                        <option value="economics">economics</option>
                                        <option value="law">law</option>
                                        <option value="accountancy">accountancy</option>
                                        <option value="sis">sis</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Gender</label>

                                <div class="col-sm-5">
                                    <select name="gender" class="form-control">
                                        <option value="null">None</option>
                                        <option value="M">M</option>
                                        <option value="F">F</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Year</label>
                                <div class="col-sm-5">
                                    <select name="year" class="form-control">
                                        <option value="null">None</option>
                                        <option value="2011">2011</option>
                                        <option value="2012">2012</option>
                                        <option value="2013">2013</option>
                                        <option value="2014">2014</option>
                                        <option value="2015">2015</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-5">
                                    <input type="hidden" name="reportType" value="diurnalPattern"/>
                                    <%

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
            <%	    LinkedHashMap<String, Integer> results = (LinkedHashMap) session.getAttribute("results");

                if (results != null) {
            %>
            <div class="col-md-12">
                <h4>Results</h4>
                <%
                    String inputs = (String) session.getAttribute("searchInput");
                %>
                <h5><b>Search Inputs :</b></h5>
                <%=inputs%><br/><br/>

                <table class="table responsive">
                    <tr>
                        <th>Diurnal Period</th>
                        <th>Average App Usage Time</th>
                    </tr>
                    <%
                        for (String time : results.keySet()) {
                            int averageTime = results.get(time);
                    %>
                    <tr>
                        <td><%=time%></td>
                        <td><%=averageTime%></td>
                    </tr>
                    <%
                            }

                        }
                        session.removeAttribute("results");
                        session.removeAttribute("searchInput");
                    %>
                </table>
            </div>
        </div>

    </body>
</html>
