<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="CSS/button.css">
        <title>SMUA || Dashboard</title>
    </head>
    <body class="page-body page-fade gray">
        <!--Navigation file-->
        <%@include file="nav.jsp" %>
        <!--Body of Dashboard-->
        <div class="row">
            <div class="col-md-12">
                <center>
                    <img src="images/portalbackg.jpg" alt="" style="width:100%; height:auto"/> 
                </center>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-primary">
                    <div class="panel-body">
                        <%                            DateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm:ss");
                            Date date = new Date();
                            String currentDate = dateFormat.format(date);
                        %>
                        <h3><%=currentDate%></h3>
                        <h4>Welcome, <%=verifiedUser.getUsername()%></h4>
                    </div>
                </div>
            </div>
        </div>    
        <div class="row">        
            <div class="col-sm-3">
                <div class="tile-title tile-black hvr-wobble-horizontal">                   
                    <a href="usageTimeCat.jsp">
                        <div class="icon" >
                            <i class="glyphicon glyphicon-search"></i>
                        </div>

                        <div class="title">
                            <h3>Usage Time Category</h3>
                            <p>Generate basic application report by date.</p>
                        </div>
                    </a>
                </div>
            </div>
            <div class="col-sm-3">
                <div class="tile-title tile-red hvr-wobble-horizontal">
                    <a href="usageTimeCatDemo.jsp">
                        <div class="icon" >
                            <i class="glyphicon glyphicon-search"></i>
                        </div>

                        <div class="title">
                            <h3>Usage Time Demo</h3>
                            <p>Generate basic application report by filters.</p>
                        </div>
                    </a>
                </div>
            </div>
            <div class="col-sm-3">
                <div class="tile-title tile-aqua hvr-wobble-horizontal">
                    <a href="appCategory.jsp">
                        <div class="icon" >
                            <i class="glyphicon glyphicon-search"></i>
                        </div>

                        <div class="title">
                            <h3>App Category</h3>
                            <p>Generate basic application report by app category.</p>
                        </div>
                    </a>
                </div>
            </div>
            <div class="col-sm-3">
                <div class="tile-title tile-green hvr-wobble-horizontal">
                    <a href="diurnalPattern.jsp">
                        <div class="icon" >
                            <i class="glyphicon glyphicon-search"></i>
                        </div>

                        <div class="title">
                            <h3>Diurnal Pattern</h3>
                            <p>Generate basic application report by filters.</p>
                        </div>
                    </a>
                </div>
            </div>
        </div>

        <div class="row">        
            <div class="col-sm-3">
                <div class="tile-title tile-orange hvr-wobble-horizontal">
                    <a href="smartphoneOveruse.jsp">
                        <div class="icon" >
                            <i class="glyphicon glyphicon-search"></i>
                        </div>
                        <div class="title">
                            <h3>SmartPhone Overuse</h3>
                            <p>Generate user's smartphone overuse statistics.</p>
                        </div>
                    </a>
                </div>
            </div>
            
            <div class="col-sm-3">
                <div class="tile-title tile-brown hvr-wobble-horizontal">
                    <a href="topKApps.jsp">
                    <div class="icon" >
                        <i class="glyphicon glyphicon-search"></i>
                    </div>
                    <div class="title">
                        <h3>Top-K Apps</h3>
                        <p>Generate Top K Applications by school filter.</p>
                    </div>
                    </a>
                </div>
            </div>
            <div class="col-sm-3">
                <div class="tile-title tile-plum hvr-wobble-horizontal">
                    <a href="topKSchools.jsp">
                    <div class="icon" >
                        <i class="glyphicon glyphicon-search"></i>
                    </div>

                    <div class="title">
                        <h3>Top-K Schools</h3>
                        <p>Generate Top K Schools by app filter.</p>
                    </div>
                    </a>
                </div>
            </div>
            <div class="col-sm-3">
                <div class="tile-title tile-blue hvr-wobble-horizontal">
                    <a href="topKStudents.jsp">
                    <div class="icon" >
                        <i class="glyphicon glyphicon-search"></i>
                    </div>

                    <div class="title">
                        <h3>Top-K Students</h3>
                        <p>Generate Top K Students by app filter.</p>
                    </div>
                    </a>
                </div>
            </div>
        </div>



    </div>

</body>
</html>
