<%@include file="adminProtect.jsp"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="description" content="Neon Admin Panel" />
        <meta name="author" content="" />

        <!--Stylesheets-->
        <link rel="stylesheet" href="assets/js/jquery-ui/css/no-theme/jquery-ui-1.10.3.custom.min.css">
        <link rel="stylesheet" href="assets/css/font-icons/entypo/css/entypo.css">
        <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Noto+Sans:400,700,400italic">
        <link rel="stylesheet" href="assets/css/bootstrap.css">
        <link rel="stylesheet" href="assets/css/neon-core.css">
        <link rel="stylesheet" href="assets/css/neon-theme.css">
        <link rel="stylesheet" href="assets/css/neon-forms.css">
        <link rel="stylesheet" href="assets/css/custom.css">

        <script src="assets/js/jquery-1.11.0.min.js"></script>
        <script>$.noConflict();</script>

        <!--[if lt IE 9]><script src="assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
        <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
                <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
                <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
        <![endif]-->
    </head>
    <body>
        <!--START NAV STYLES-->
        <div class="page-container">	
            <!--SIDE NAV BAR-->
            <div class="sidebar-menu">
                <div class="sidebar-menu-inner">
                    <header class="logo-env">
                        <!-- logo -->
                        <div class="logo">
                            <a href="main.jsp"><img src="images/SMUATextLogo.png" width="120" alt="" /></a>
                            <br/>
                        </div>
                        <!-- logo collapse icon -->
                        <div class="sidebar-collapse">
                            <a href="#" class="sidebar-collapse-icon"><i class="entypo-menu"></i></a>
                        </div>
                    </header>

                    <ul id="main-menu" class="main-menu">
                        <!-- add class "multiple-expanded" to allow multiple submenus to open -->
                        <!-- class "auto-inherit-active-class" will automatically add "active" class for parent elements who are marked already with class "active" -->
                        <!--Dashboard-->
                        <li class="opened active">
                            <a href="adminMain.jsp">
                                <i class="entypo-gauge"></i>
                                <span class="title">Admin Dashboard</span>
                            </a>
                        </li>
                        <!--Bootstrap-->
                        <li>
                            <a href="bootstrapMain.jsp">
                                <i class="entypo-archive"></i>
                                <span class="title">BootStrap</span>
                            </a>
                        </li>
                        
                            </ul>
                </div>
            </div>
            <!--END OF SIDE NAV BAR-->
            <!--TOP BAR-->
            <div class="main-content">
                <div class="row">
                    <!-- Profile Info and Notifications -->
                    <div class="col-md-6 col-sm-8 clearfix">
                        <ul class="user-info pull-left pull-none-xsm">
                            <!-- Profile Info -->
                            
                        </ul>
                    </div>
                    <!-- Raw Links -->
                    
                    <div class="col-md-6 col-sm-4 clearfix hidden-xs">
                        <ul class="list-inline links-list pull-right">
                            <li class="profile-info dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <img src="images/Icon_Login_User.png" alt="" class="img-circle" width="44" />
                                    <%=user.getUsername()%>
                                </a>

                                <ul class="dropdown-menu">
                                    <!-- Reverse Caret -->
                                    <li class="caret"></li>

                                    <!-- Profile sub-links -->
                                    <li>
                                        <a href="Logout">
                                            <i class="entypo-user"></i>
                                            Logout
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            
                            <li class="sep"></li>
                            <li>
                                <a href="Logout">
                                    Log Out <i class="entypo-logout right"></i>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
                <hr />
                <!--END OF NAV STYLES-->

            <!-- Imported styles on this page -->
            <link rel="stylesheet" href="assets/js/select2/select2-bootstrap.css">
            <link rel="stylesheet" href="assets/js/select2/select2.css">
            <link rel="stylesheet" href="assets/js/daterangepicker/daterangepicker-bs3.css">
            <link rel="stylesheet" href="assets/js/zurb-responsive-tables/responsive-tables.css">


            <!-- Bottom scripts (common) -->
            <script src="assets/js/gsap/main-gsap.js"></script>
            <script src="assets/js/jquery-ui/js/jquery-ui-1.10.3.minimal.min.js"></script>
            <script src="assets/js/bootstrap.js"></script>
            <script src="assets/js/joinable.js"></script>
            <script src="assets/js/resizeable.js"></script>
            <script src="assets/js/neon-api.js"></script>

            <!-- Imported scripts on this page -->
            <script src="assets/js/select2/select2.min.js"></script>
            <script src="assets/js/selectboxit/jquery.selectBoxIt.min.js"></script>
            <script src="assets/js/bootstrap-datepicker.js"></script>
            <script src="assets/js/daterangepicker/daterangepicker.js"></script>
            <script src="assets/js/zurb-responsive-tables/responsive-tables.js"></script>


            <!-- JavaScripts initializations and stuff -->
            <script src="assets/js/neon-custom.js"></script>
                </body>
                </html>