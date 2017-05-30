<%@page import="se.entity.User"%>
<%
    User verifiedUser = (User) session.getAttribute("user");
    if (verifiedUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>