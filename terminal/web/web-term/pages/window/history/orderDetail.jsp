<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script type="text/javascript">
	var orderId =  '<%=request.getParameter("orderId") %>';
 	var queryType =  '<%=request.getParameter("queryType") %>';
 	var foodStatus = '<%=request.getParameter("foodStatus") %>';
 	<%--	if(reload){
		load();
	} --%>
	
</script>
<script type="text/javascript" src="../../js/window/history/orderDetail.js"></script>
</head>
<body>
<div id="divOrderDetail"></div>
</body>
</html>