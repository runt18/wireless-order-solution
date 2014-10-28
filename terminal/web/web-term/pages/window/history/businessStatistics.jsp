<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	
	<meta http-equiv="cache-control" content="no-cache">
	
	<title>营业统计</title>
	<script type="text/javascript">
	var dateType = {
		USE_SELF_DATE : 1, //用统计框自带的时间控件
		USE_TIME_FORMAT : 2, //时间格式化传来的long型参数
		USE_DATE_FORMAT : 3, //日期格式化传来的long型参数
		NO_TRANSFER : 4, //不用传时间参数到后台
		USE_NOPARST : 5 //不格式化传来的参数
	};
		var dataSource = '<%=request.getParameter("dataSource") %>';
		var queryPattern = <%=request.getParameter("queryPattern") %>;
		
		dataSource = dataSource != 'null' && dataSource != '' ? dataSource : 'today';
		var dutyRange = '<%=request.getParameter("dutyRange") %>';
		var queryType = '<%=request.getParameter("queryType") %>';
		var staffId = '<%=request.getParameter("staffId") %>';
		//标示是否显示部门的汇总。2：交款
		var businessStatic = '<%=request.getParameter("businessStatic") %>';
		
		queryPattern = queryPattern == null ? dateType.USE_SELF_DATE : parseInt(queryPattern);
		//TODO
		if(queryPattern == dateType.USE_TIME_FORMAT || queryPattern == dateType.USE_DATE_FORMAT){
			var onDuty = '<%=request.getParameter("onDuty") %>';
			var offDuty = '<%=request.getParameter("offDuty") %>';
			if(onDuty != 'null' && offDuty != 'null'){
				onDuty = new Date(parseInt(onDuty));
				offDuty =  new Date(parseInt(offDuty));
			}else{
				onDuty = null;
				offDuty = null;
			}
		}else if(queryPattern == dateType.USE_NOPARST){
			onDuty = '<%=request.getParameter("onDuty") %>';
			offDuty = '<%=request.getParameter("offDuty") %>';
		}
		var business;
	</script>
	<script type="text/javascript" src="../../js/window/history/businessStatistics.js"></script>
	
</head>
<body>
	<div id="businessStatisticsDIV"></div>
	<!--  -->
	<div id="divBusinessStatisticsSummaryInformation" >
		<table id="businessStatisticsSummaryInformation" border="1" class="tb_base">
			<tr>
				<th>开始时间:</th>
				<td width="210px" colspan="2" id="bssiOnDuty">---</td>
			</tr>
			<tr>
				<th>结束时间:</th>
				<td colspan="2" id="bssiOffDuty">---</td>
			</tr>
			<tr>
				<th>账单总数:</th>
				<td colspan="2" id="bssiOrderAmount">---</td>
			</tr>
		</table>
		<br>
		<table id="businessStatisticsSummaryPayIncome" border="1" class="tb_base">
			<tr>
				<th class="table_title text_center">收款方式</th>
				<th class="table_title text_center">账单数</th>
				<th class="table_title text_center">应收总额</th>
				<th class="table_title text_center">实收总额</th>
			</tr>
		</table>
		<br>
		<table border="1" class="tb_base" style="width : 100%;">				
			<tr>
				<th class="table_title text_center">操作类型</th>
				<th class="table_title text_center">账单数</th>
				<th class="table_title text_center">金额</th>
	
			</tr>
			<tr>
				<th>抹数</th>
				<td class="text_right" id="bssiEraseAmount">---</td>
				<td class="text_right" id="bssiEraseIncome">---</td>
			</tr>
			<tr>
				<th>折扣</th>
				<td class="text_right" id="bssiDiscountAmount">---</td>
				<td class="text_right" id="bssiDiscountIncome">---</td>
			</tr>
			<tr>
				<th>赠送</th>
				<td class="text_right" id="bssiGiftAmount">---</td>
				<td class="text_right" id="bssiGiftIncome">---</td>
			</tr>
			<tr>
				<th>退菜</th>
				<td class="text_right" id="bssiCancelAmount">---</td>
				<td class="text_right" id="bssiCancelIncome">---</td>
			</tr>
			<tr>
				<th>反结帐</th>
				<td class="text_right" id="bssiPaidAmount">---</td>
				<td class="text_right" id="bssiPaidIncome">---</td>
			</tr>
			<tr>
				<th>服务费收入</th>
				<td class="text_right" id="bssiServiceAmount">---</td>
				<td class="text_right" id="bssiServiceIncome">---</td>
			</tr>
			<tr>
				<th>优惠劵</th>
				<td class="text_right" id="bssiCouponAmount">---</td>
				<td class="text_right" id="bssiCouponIncome">---</td>
			</tr>
		</table>

	</div>	
	 
</body>
</html>