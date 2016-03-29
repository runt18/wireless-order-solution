<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<html>
<head>
	
	<meta http-equiv="cache-control" content="no-cache">
	
	<title>营业统计</title>
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