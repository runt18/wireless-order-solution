<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
HttpSession httpSession = request.getSession(true);
String sessionId = httpSession.getId();
httpSession.setAttribute("fid", "gh_6a79ab99c6b9");
httpSession.setAttribute("oid", "oM02TjtmLtadFjiGtlUuxTFjJhno");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script type="text/javascript" src="../jquery/jquery-1.8.2.min.js"></script>
<script type="text/javascript">
$(function(){
	location.href = 'waiter.html?1=1&sessionId=<%=sessionId %>';
});
</script>
</head>
<body>


</body>
</html>