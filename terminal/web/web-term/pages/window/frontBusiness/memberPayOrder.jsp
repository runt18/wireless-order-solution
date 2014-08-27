<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	<meta http-equiv="cache-control" content="no-cache">
	<title>会员结账</title>
	<script type="text/javascript">
		var orderID = '<%=request.getParameter("orderID") %>';
		orderID = Ext.util.Format.trim(orderID).length == 0 || orderID == 'null' ? null : orderID;
	</script>
	<script type="text/javascript" src="../../js/window/frontBusiness/memberPayOrder.js"></script>
</head>
<body>
	<div id="divMemberPayOrderContent"></div>
</body>
</html>