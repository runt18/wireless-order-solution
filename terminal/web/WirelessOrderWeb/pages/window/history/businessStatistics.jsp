<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	
	<meta http-equiv="cache-control" content="no-cache">
	
	<title>营业统计</title>
	<script type="text/javascript">
		var dataSource = '<%=request.getParameter("dataSource") %>';
		var queryPattern = <%=request.getParameter("queryPattern") %>;
		
		dataSource = dataSource != 'null' && (dataSource == 'today' || dataSource == 'history') ? dataSource : 'today';
		var dutyRange = '<%=request.getParameter("dutyRange") %>';
		var queryType = '<%=request.getParameter("queryType") %>';
		queryPattern = queryPattern == null ? 1 : parseInt(queryPattern);
		if(queryPattern == 2 || queryPattern == 3){
			var onDuty = '<%=request.getParameter("onDuty") %>';
			var offDuty = '<%=request.getParameter("offDuty") %>';
			if(onDuty != 'null' && offDuty != 'null'){
				onDuty = new Date(parseInt(onDuty));
				offDuty =  new Date(parseInt(offDuty));
			}else{
				onDuty = null;
				ofDuty = null;
			}
		}
		var business;
	</script>
	<script type="text/javascript" src="../../js/window/history/businessStatistics.js"></script>
	
</head>
<body>
	<div id="businessStatisticsDIV"></div>
	<!--  -->
	<table id="businessStatisticsSummaryInformation" border="1" class="tb_base">
		<tr>
			<th>开始时间:</th>
			<td width="210px" colspan="3" id="bssiOnDuty">---</td>
		</tr>
		<tr>
			<th>结束时间:</th>
			<td colspan="3" id="bssiOffDuty">---</td>
		</tr>
		<tr>
			<th>账单总数:</th>
			<td colspan="3" id="bssiOrderAmount">---</td>
		</tr>
		<tr style="height:15px;">
			<td colspan="4"></td>
		</tr>
		<tr>
			<th class="table_title text_center">收款方式</th>
			<th class="table_title text_center">账单数</th>
			<th class="table_title text_center">应收总额</th>
			<th class="table_title text_center">实收总额</th>
		</tr>
		<tr>
			<th>现金</th>
			<td class="text_right" id="bssiCashAmount">---</td>
			<td class="text_right" id="bssiCashIncome">---</td>
			<td class="text_right" id="bssiCashIncome2">---</td>
		</tr>
		<tr>
			<th>刷卡</th>
			<td class="text_right" id="bssiCreditCardAmount">---</td>
			<td class="text_right" id="bssiCreditCardIncome">---</td>
			<td class="text_right" id="bssiCreditCardIncome2">---</td>
		</tr>
		<tr>
			<th>会员卡</th>
			<td class="text_right" id="bssiMemeberCardAmount">---</td>
			<td class="text_right" id="bssiMemeberCardIncome">---</td>
			<td class="text_right" id="bssiMemeberCardIncome2">---</td>
		</tr>
		<tr>
			<th>签单</th>
			<td class="text_right" id="bssiSignAmount">---</td>
			<td class="text_right" id="bssiSignIncome">---</td>
			<td class="text_right" id="bssiSignIncome2">---</td>
		</tr>
		<tr>
			<th>挂账</th>
			<td class="text_right" id="bssiHangAmount">---</td>
			<td class="text_right" id="bssiHangIncome">---</td>
			<td class="text_right" id="bssiHangIncome2">---</td>
		</tr>
		<tr>
			<th class="table_title">合计</th>
			<td class="text_right" id="bssiSumAmount">---</td>
			<td class="text_right" id="bssiSumIncome">---</td>
			<td class="text_right" id="bssiSumIncome2">---</td>
		</tr>
		<tr style="height:15px;">
			<td colspan="4"></td>
		</tr>
		<tr>
			<th class="table_title text_center">操作类型</th>
			<th class="table_title text_center">账单数</th>
			<th class="table_title text_center">金额</th>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>抹数</th>
			<td class="text_right" id="bssiEraseAmount">---</td>
			<td class="text_right" id="bssiEraseIncome">---</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>折扣</th>
			<td class="text_right" id="bssiDiscountAmount">---</td>
			<td class="text_right" id="bssiDiscountIncome">---</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>赠送</th>
			<td class="text_right" id="bssiGiftAmount">---</td>
			<td class="text_right" id="bssiGiftIncome">---</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>退菜</th>
			<td class="text_right" id="bssiCancelAmount">---</td>
			<td class="text_right" id="bssiCancelIncome">---</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>反结帐</th>
			<td class="text_right" id="bssiPaidAmount">---</td>
			<td class="text_right" id="bssiPaidIncome">---</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>服务费收入</th>
			<td class="text_right" id="bssiServiceAmount">---</td>
			<td class="text_right" id="bssiServiceIncome">---</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<th>优惠劵</th>
			<td class="text_right" id="bssiCouponAmount">---</td>
			<td class="text_right" id="bssiCouponIncome">---</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	
	<!-- 
	<table id="businessStatisticsSummaryInformationByDept" border="1" class="tb_base">
		<tr>
			<th class="table_title text_center">部门汇总</th>
			<th class="table_title text_center">折扣总额</th>
			<th class="table_title text_center">赠送总额</th>
			<th class="table_title text_center">实收总额</th>
		</tr>
	</table>
	 -->
	 
</body>
</html>