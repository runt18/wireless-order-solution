<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="cache-control" content="no-cache">
<title>会员取款</title>
	<script type="text/javascript">
		var tm_rechargeMemberMobile = '<%=request.getParameter("memberMobile") %>';
	</script>
	<script type="text/javascript" src="../../js/window/client/takeMoney.js"></script>
</head>
<body>
	<div id = "divMemberTakeMoney"></div>
</body>
</html>