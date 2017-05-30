<%@page import="se.entity.User"%>
<%
    User user = (User)session.getAttribute("user");
    if(user == null) {
        response.sendRedirect("login.jsp");
        return;
    } else if(!user.isAdmin()) {
        response.sendRedirect("main.jsp");
        return;
    }
%>