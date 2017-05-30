<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="assets/css/button.css">
        <title>SMUA || Admin Dashboard</title>
    </head>
    <body class="page-body page-fade gray">
        <!--Navigation file-->
        <%@include file="adminNav.jsp" %>
        <!--Body of Dashboard-->
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-primary">
                    <div class="panel-body">
                        <%                            DateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm:ss");
                            Date date = new Date();
                            String currentDate = dateFormat.format(date);
                        %>
                        <h3><%=currentDate%></h3>
                        <h4>Welcome, <%=user.getUsername()%></h4>
                    </div>
                </div>
            </div>
        </div>    
        <div class="row">        
            <div class="col-sm-12">
                <div class="tile-title tile-black hvr-wobble-horizontal">                   
                    <a href="bootstrapMain.jsp">
                        <div class="icon" >
                            <i class="glyphicon glyphicon-upload"></i>
                        </div>

                        <div class="title">
                            <h3>Bootstrap</h3>
                            <p>so far in our blog, and our website.</p>
                        </div>
                    </a>
                </div>
            </div>
            
    </div>

</body>
</html>
