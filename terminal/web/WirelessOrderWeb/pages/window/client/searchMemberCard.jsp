<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	<meta http-equiv="cache-control" content="no-cache">
	<title>查找会员</title>
	<script type="text/javascript">
		var sreachMemberCardCallback = '<%=request.getParameter("callback") %>';
		var s_searchMemberCardGridPanel;
	</script>
	<script type="text/javascript" src="../../js/window/client/searchMemberCard.js"></script>
</head>
<body>
	<div id="divSearchMemberCardContent"></div>
</body>
</html>