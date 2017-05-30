<%@page import="java.util.ArrayList"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=1,initial-scale=1,user-scalable=1" />
        <title>SMUA</title>
        <!-- Custom CSS -->
        <link rel="stylesheet" type="text/css" href="assets/css/login.css" />
        <!-- Google Font -->
        <link href="http://fonts.googleapis.com/css?family=Lato:100italic,100,300italic,300,400italic,400,700italic,700,900italic,900" rel="stylesheet" type="text/css">
        <!-- Bootstrap Core CSS -->
        <link type="text/css" rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
        <!-- jQuery Library -->
        <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.10.0/jquery.min.js"></script>
        <!-- Bootstrap Core JS -->
        <script src="http://netdna.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
        <style>body{background-image: url("images/bg.jpg");
            }</style>

    </head>
    <body>

        <section class="container">
            <section class="login-form">
                <form method="post" action="Login" role="login">
                    <section>
                        <center>
                            <img src="images/Logo.png" alt="" height="300" width="300"/>
                            <center>
                                </section>

                                <label>Username</label>			
                                <input type="text" name="username" required class="form-control" />

                                <label>Password</label>
                                <input type="password" name="password" required class="form-control" />

                                <button type="submit" name="go" class="btn btn-danger">Sign in</button>

                                <div>
                                    <%
                                        //Print Error Message
                                        ArrayList<String> errMsg = (ArrayList) request.getAttribute("errMsg");
                                        if (errMsg != null && errMsg.size() > 0) {
                                            for (String err : errMsg) {
                                                out.println("<p class='err' align='middle'>" + err + "</p>");
                                            }
                                        }
                                    %>
                                </div>
                                </form>
                                </section>
                                </section>

                                </body>
                                </html>