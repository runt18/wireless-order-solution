<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	<meta http-equiv="cache-control" content="no-cache">
	
	<title>会员消费记录</title>
	<script type="text/javascript">
		var cdd_memberOperationOnMobile = '<%=request.getParameter("memberMobile") %>';
		var cdd_modal = '<%=(request.getParameter("modal") == null ? "" : request.getParameter("modal")) %>';
		cdd_modal = cdd_modal != null && Ext.util.Format.trim(cdd_modal).length > 0 ? eval(Ext.util.Format.trim(cdd_modal)) : false;
	</script>
	<script type="text/javascript" src="../../js/window/client/memberOperation.js"></script>
	
</head>
<body>
	<div id="divMemberOperationContent"></div>
</body>
</html>