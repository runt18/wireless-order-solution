<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	<meta http-equiv="cache-control" content="no-cache">
	
	<title>会员信息</title>
	<script type="text/javascript">
	var cm_obj = {data : {} };
	cm_obj.otype = '<%=request.getParameter("otype") %>';
	</script>
	<script type="text/javascript" src="../../js/window/client/controlMember.js"></script>
	
</head>
<body>
	<div id="divControlMemberContent"></div>
</body>
</html>