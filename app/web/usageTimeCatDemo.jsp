<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMUA || Usage Time Category and Demographics</title>
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
                <strong>Usage Time Category and Demographics</strong>
            </li>
        </ol>
        <!--Form-->
        <div class="row">
            <div class="col-md-12">	
                <div class="panel panel-primary" data-collapsed="0">
                    <div class="panel-heading">
                        <div class="panel-title">
                            Usage Time Category and Demographics
                        </div>
                        <div class="panel-options">
                            <a href="#" data-rel="collapse"><i class="entypo-down-open"></i></a>
                        </div>
                    </div>

                    <div class="panel-body">
                        <form role="form" class="form-horizontal form-groups-bordered" action="BasicAppUtility" method="post">
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
                                <label class="col-sm-3 control-label">Select First Filter</label>
                                <div class="col-sm-5">
                                    <select name="firstSort" class="form-control">
                                        <option value="null"> None</option>
                                        <option value="year">Year</option>
                                        <option value="gender">Gender</option>
                                        <option value="school">School</option>
                                        <option value="cca">CCA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Second Filter</label>

                                <div class="col-sm-5">
                                    <select name="secondSort" class="form-control">
                                        <option value="null"> None</option>
                                        <option value="year">Year</option>
                                        <option value="gender">Gender</option>
                                        <option value="school">School</option>
                                        <option value="cca">CCA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Third Filter </label>
                                <div class="col-sm-5">
                                    <select name="thirdSort" class="form-control">
                                        <option value="null"> None</option>
                                        <option value="year">Year</option>
                                        <option value="gender">Gender</option>
                                        <option value="school">School</option>
                                        <option value="cca">CCA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label">Select Fourth Filter </label>
                                <div class="col-sm-5">
                                    <select name="fourthSort" class="form-control">
                                        <option value="null"> None</option>
                                        <option value="year">Year</option>
                                        <option value="gender">Gender</option>
                                        <option value="school">School</option>
                                        <option value="cca">CCA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-5">
                                    <input type="hidden" name="reportType" value="usageTimeCatDemo"/>
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
            <div class="col-md-12">
                <%		    ArrayList<String[]> results = (ArrayList) session.getAttribute("results");

                    if (results != null && results.size() > 0) {


                %>
                <div class="col-md-12">
                    <h4>Results</h4>
                    <%                         String inputs = (String) session.getAttribute("searchInput");
                    %>
                    <h5><b>Search Inputs :</b></h5>
                    <%=inputs%><br/><br/>

                    <table class="table responsive">

                        <%		for (String[] dataArr : results) {

                        %>
                        <tr>

                            <%		    for (String data : dataArr) {
                            %>
                            <td><%=data%></td>
                            <%
                                }
                            %>
                        </tr>
                        <%
                            }
                        %>
                    </table></div></div>
                    <%
                        }
                        session.removeAttribute("results");
                        session.removeAttribute("searchInput");

                    %>
    </body>
</html>