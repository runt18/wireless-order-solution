<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<% 
	float v = 1.9f; 
	response.setHeader("Pragma","No-cache"); 
	response.setHeader("Cache-Control","no-cache"); 
	response.setDateHeader("Expires", 0);  
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>智易点餐系统</title>
<!-- 客户端页面缓存清理 -->
<meta http-equiv="pragma" content="no-cache"> 
<meta http-equiv="cache-control" content="no-cache"> 
<meta http-equiv="expires" content="0">	

<meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
<!-- jquery mobile -->
<link rel="stylesheet" href="css/common/jquery.mobile-1.3.2.css">
<!-- 日期插件.css -->
<link rel="stylesheet" type="text/css" href="css/dateCmp/datebox.css" />
<!-- 时分插件.css -->
<link rel="stylesheet" type="text/css" href="css/timepicki.css" />
<!-- 数字键盘样式 -->
<link rel="stylesheet" href="css/calculate/datouwang.css">
<!-- 自定义样式 -->
<link rel="stylesheet" href="css/takeout/main.css?v=<%=v %>">
<link rel="stylesheet" href="css/table.css?v=<%=v %>">

<script type="text/javascript" src="../jquery/jquery-1.8.2.min.js"></script>
<!-- 解决jqm1.3.2的popup问题 -->
<script type="text/javascript">
	//Override of $.fn.animationComplete must be called before initialise jquery mobile js
	$(document).on('mobileinit', function() {
	    $.fn.animationComplete = function(callback) {
	      if ($.support.cssTransitions) {
	    	  var superfy = "WebKitTransitionEvent" in window ? "webkitAnimationEnd" : "animationend";
	          return $(this).one(superfy, callback);
	      }else{
	          setTimeout(callback, 0);
	          return $(this);
	      }
	    };
	});
</script>
<!-- seajs导包 -->
<script type="text/javascript" src="./js/common/sea.js"></script>
<script type="text/javascript">
	seajs.config({
		alias : {
			'diagPrinter' : './js/popup/diagPrinter/diagPrinter',   
			'readMember' : './js/popup/member/read',
			'addMember' : './js/popup/member/add',
			'recharge' : './js/popup/member/recharge',
			'consumeDetail' : './js/popup/consumeDetail/consumeDetail',
			'patchCard' : './js/popup/member/patchCard',
			'patchWxCard' : './js/popup/member/patchWxCard',
			'perfectMemberMsg' : './js/popup/member/perfect',
			'issueCoupon' : './js/popup/coupon/issuePopup',
			'useCoupon' : './js/popup/coupon/usePopup',
			'taste' : './js/popup/taste/taste',
			'tempTaste' : './js/popup/tempTaste/tempTaste',
			'moreTastes' : './js/popup/moreTaste/moreTaste',
			'wxOrderListPopup' : './js/popup/wxOrderList/wxOrderList',
			'printBind' : './js/popup/print/print',
			'feastPay' : './js/popup/feastPay/feastPay',
			'handlerTable' : './js/popup/handlerTable/handlerTable',
			'askTable' : './js/popup/table/ask'
		}
	});
</script>
<script type="text/javascript" src="js/common/jquery.mobile-1.3.2.min.js"></script>
<!-- 日期插件.js -->
<script type="text/javascript" src="js/common/datebox.core.js"></script>
<script type="text/javascript" src="js/common/jqm.datebox.language-CN.js"></script>
<!-- 时分插件.js -->
<script type="text/javascript" src="js/book/timepicki.js"></script>
<!-- 手写板控件 -->
<script type="text/javascript" src="js/handWriting/handWriting.js?v=<%=v %>"></script>
<!-- jqm弹出框控件 -->
<script type="text/javascript" src="js/popup/jqmPopup.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/Util.js?v=<%=v %>"></script>
<script type="text/javascript" src="../extjs/wireless.ux.js"></script>
<script type="text/javascript" src="js/orderFood/orderFood.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/checkOut/checkOut.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/stopSet/stopSet.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/payment/payment.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/books/book.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/wxOrder/wxOrder.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/tableSelect/tableSelect.js?v=<%=v %>"></script>
<!-- 共享数据 -->
<script type="text/javascript" src="js/global/share.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/tables.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/foods.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/staff.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/systemStatus.js?<%=v %>"></script>
<!-- 分页控件 -->
<script type="text/javascript" src="./js/padding/padding.js"></script>
<!-- 数字键盘控件 -->
<script type="text/javascript" src="js/numKeyBoard/numKeyBoard.js?v=<%=v %>"></script>
<!-- 餐台选择控件 -->
<!-- <script type="text/javascript" src="./js/popup/table/askTable.js"></script> -->
<!-- JqmPopup弹出控件 -->
<script type="text/javascript" src="./js/popup/jqmPopup.js?v=<%=v %>"></script>
<!-- JqmPopupDiv弹出控件 -->
<script type="text/javascript" src="./js/popup/jqmPopupDiv.js?v=<%=v %>"></script>
<!-- 明细控件绑定 -->
<script type="text/javascript" src="./js/popup/detail/detail.js?v=<%=v %>"></script>
<!-- 微信预定 -->
<script type="text/javascript" src="./js/popup/wxOrder/wxOrder.js?v=<%=v %>"></script>
<!-- 混合结账 -->
<script type="text/javascript" src="./js/popup/mixedPay/mixedPay.js?v=<%=v %>"></script>
<!-- 添加预订信息 -->
<script type="text/javascript" src="./js/popup/bookInfo/bookInfo.js?v=<%=v %>"></script>
<!-- 入座 -->
<script type="text/javascript" src="./js/popup/seat/seat.js?v=<%=v %>"></script>
<!-- 微信扫描 -->
<script type="text/javascript" src="./js/popup/wxPayment/wxPayment.js?v=<%=v %>"></script>
<!-- 跑马灯 -->
<script type="text/javascript" src="./js/runHorse/runHorse.js"></script>
<!-- 重连socket -->
<script type="text/javascript" src="./js/socket/reconnecting-websocket.js"></script>

<!--禁止触摸时选中文字  -->
<script type="text/javascript">
	document.onselectstart = function(){
		return false;	
	} 
</script>

</head>
<body>

<!-- 餐台选择 start -->
<div data-role="page" id="tableSelectMgr" data-theme="d">
	<div id="divPosOperation" data-role="header" data-theme="b" style="height: 44px;text-align: center;display: none;">
		<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal" style="margin-top: 2px;">
			<div style="float: left;">
				<img src="images/logo.png" height="40px">
			</div>
			<div style="float: left;">
				<img src="images/contact.png" height="40px">
			</div>
		</div>	
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<!-- ts.displayPrintConnection() -->
		 	<a id="diagPrinter_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont" >打印机诊断</a>
		 	<a data-role="button" data-inline="true" class="topBtnFont"  data-rel="popup" data-transtion="pop" href="#frontPageMemberOperation">会员</a>
		 	<a id="todayBill_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">账单</a>
		 	<a id="personSettle_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">交款</a>
			<a id="phraseSettle_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">交班</a>
			<a id="dailySettle_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">日结</a>
			<a data-role="button" data-inline="true" class="topBtnFont" id="btnToBasicPage" data-rel="popup" data-transtion="pop" href="#toBasicMgr">后台</a>
		 </div>
	</div>
	
	<div data-role="popup" id="frontPageMemberOperation" data-theme="d" class="payment_searchMemberType">
		<ul id="charge_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
			<li class="popupButtonList" id="searchMember_a_tableSelect"><a>会员查询</a></li>
			<li class="popupButtonList" id="addMember_a_tableSelect"><a >添加会员</a></li>
			<li class="popupButtonList" id="memberRecharge_a_tableSelect"><a >会员充值</a></li>
			<li class="popupButtonList" id="pointConsume_li_tableSelect"><a >积分消费</a></li>
			<li class="popupButtonList" id="consumeDetail_a_tableSelect"><a >消费明细</a></li>
			<li class="popupButtonList" id="patchCard_a_tableSelect"><a>补发实体卡</a></li>
			<li class="popupButtonList" id="patchWxCard_a_tableSelect"><a>补发电子卡</a></li>
			<li class="popupButtonList" id="memberWxBind_li_tableSelect"><a>微信会员绑定</a></li>
			<li class="popupButtonList" id="fastIssue_a_tableSelect"><a >快速发券</a></li>
			<li class="popupButtonList" id="fastUse_a_tableSelect"><a >快速用券</a></li>
		</ul>
	</div>	
	
	<div data-role="popup" id="toBasicMgr" data-theme="d" class="payment_searchMemberType">
		<ul id="charge_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
			<li class="popupButtonList" ><a href="../pages/Mgr/DigieBasic.html?page=1" target="_blank" onclick="$('#toBasicMgr').popup('close')">菜谱管理</a></li>
			<li class="popupButtonList" ><a href="../pages/Mgr/DigieBasic.html?page=2" target="_blank" onclick="$('#toBasicMgr').popup('close')">打印设置</a></li>
		</ul>
	</div>		
	
   <div id="tableAndRegionsCmp" class="ui-grid-a" style="height: 590px;background-color: rgb(135, 206, 235);">
   	<!-- 餐台数据 -->
     <div id="divTableShowForSelect" class="ui-block-a" style="width: -webkit-calc(100% - 120px);width: -moz-calc(100% - 120px);width: -ms-calc(100% - 120px);width: -o-calc(100% - 120px);height: inherit;">
    	
     </div>
     <div class="ui-block-b" style="width: 120px;height: inherit;border-left: 1px solid white;">
    	<a id="labTableStatus" data-role="button" data-theme="e" data-inline="true" class="regionBtn" data-icon="arrow-d" data-iconpos="right" data-rel="popup"  data-transition="pop" href="#popupAllStatusCmp">全部台</a>
    	<div style="height: 510px;overflow-y: auto;overflow-x: hidden;">
    	<div id="divSelectRegionForTS">
	    	<!-- <a data-role="button" data-inline="true" class="regionBtn" onclick="">区域1</a> -->
    	</div>
   		</div>
     </div>
   </div>
   
	<div data-role="popup" id="popupAllStatusCmp" data-theme="d" >
        <ul data-role="listview" data-inset="true" style="min-width:100px;" data-theme="b">
            <li id="idleTable_li_tableSelect" class="tempFoodKitchen" data-icon="false"><a>空闲台( <label id="idleTableAmount_label_tableSelect" style="color: #f7c942;">0</label> )</a></li>
            <li id="busyTable_li_tableSelect" class="tempFoodKitchen" data-icon="false"><a>就餐台( <label id="busyTableAmount_label_tableSelect" style="color: #f7c942;">0</label> )</a></li>
            <li id="allTable_li_tableSelect" class="tempFoodKitchen" data-icon="false"><a>全部台</a></li>
        </ul>
	</div>	

	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div class="bottomGeneralBar">
			<div id="loginStaffName_div_tableSelect" style="float: left;margin-left: 10px;">操作人: ---</div>
			<div style="float: left;margin-left: 15px;">空闲台: <font id="idleTableAmount_font_tableSelect" color="green">--</font></div>
			<div style="float: left;margin-left: 15px;">就餐台: <font id="busyTableAmount_font_tableSelect" color="green">--</font></div>
			<div style="float: left;margin-left: 15px;">暂结台: <font id="tmpPaidTableAmount_font_tableSelect" color="green">--</font></div>
			<div id="divDescForTableSelect-padding-msg" style="float: right;margin-right: 20px;">共--项</div>
		</div>	
		 <a id="logout_a_tableSelect" data-role="button" data-inline="true" class="bottomBtnFont">注销</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="location.reload()" >刷新</a>
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="WxOrder_a_tableSelect">
		 	<div>
		 		<div id="wxbookAmount_div_tableSelect" style="display:none;width:28px;height: 28px;border-radius: 14px;background-color: red;float: right;margin-top: -11px;line-height:28px;">0</div>
		 		微订
		 	</div>
		 	</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="book_a_tableSelect">
		 	<div>
		 		<!-- 预订单数 -->
		 		<div id="bookAmount_div_tableSelect" style="display:none;width:28px;height: 28px;border-radius: 14px;background-color: red;float: right;margin-top: -11px;line-height:28px;">0</div>
		 		预订
		 	</div>	
		 	</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="searchTable_a_tableSelect">查台</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="apartTable_a_tableSelect">拆台</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="tranTable_a_tableSelect">转台</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ts.stopSellMgr()">沽清</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#tableSelectOtherOperateCmp">更多</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="prevTablePage_a_tableSelect">上一页</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="nextTablePage_a_tableSelect">下一页</a>		 
		 </div>
	</div>

	 <!-- 餐台更多操作 -->
	<div data-role="popup" id="tableSelectOtherOperateCmp" data-theme="d">
        <ul data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
         	<li id="fastFood_li_tableSelect" class="tempFoodKitchen" style="display:none;"><a >快餐模式</a></li>
         	<li id="printBind_a_tableSelect" class="tempFoodKitchen"><a>打印机绑定</a></li>
         	<li class="tempFoodKitchen" id="feastPay_li_tableSelect"><a>酒席入账</a></li>
            <li class="tempFoodKitchen" id="multiOpen_li_tableSelect"><a >多台开席</a></li>
            <li class="tempFoodKitchen" id="MultiPayTable_li_tableSelect"><a>拼台</a></li>
        </ul>
	</div>		
	
</div>			

<!-- end 餐台选择  -->


<!-- 弹出框时, 挡住其他操作的div阴影 -->
<div id="shadowForPopup" style="z-index: 1101;opacity:0; position: absolute; top:0; left:0; width: 100%; height: 100%; background: #DDD;display: none;" ></div>

<!-- 微定订单列表start -->
<div data-role="page" id="wxOrderWin_div_wxOrder" data-theme="e">
	<div data-role="header" data-position="fixed" data-tap-toggle="false" data-theme="b" style="height: 40px;">
		<span id="" class="ui-btn-left" style="line-height: 40px;">
			微信预订列表
		</span>
	</div>
    <table style="width: 1024px;">
    	<tr>
    		<td style="width:20%;float:left;">
    			<input type="text" id="searchWxOrderNumber_input_wxOrder" placeholder="订单号">
    		</td>
    		<td style="line-height: 20px;padding: 0 3px;width:20%;float:left">
				<select  id="searchWxOrderStatus_select_wxOrder" style="font-size: 20px;">
					<option value="2">待确认</option>
					<option value="3">已下单</option>
				</select>
    		</td>    		
    	</tr>
    </table>	
	<div id="wxOrderListCmp_div_wxOrder" style="overflow-y: auto;">
		<table id="wxOrderListTitle_table_wxOrder" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
	         <thead>
	           <tr class="ui-bar-d">
	             <th style="width: 5px;"></th>
	             <th style="width: 120px;">订单编号</th>
	             <th style="width: 130px;">关联账单号</th>
	             <th style="width: 200px;">下单时间</th>
	             <th style="width: 150px;">会员名称</th>
				 <th style="width: 100px;">联系电话</th>
				 <th style="width: 140px;">台号</th>
				 <th style="width: 90px;">状态</th>
				 <th style="width: 230px;">操作</th>             
	           </tr>
	         </thead>
	         <!-- 菜品列表 -->
	         <tbody id="wxOrderList_tbody_wxOrder" style="font-weight:bold;color: blue">
	<!--            <tr>
					<td>1</td>
					<td>2014-12-11 18:06:53</td>
					<td>大厅</td>
					<td>李先生</td>
					<td>13533464033</td>
					<td>8</td>
					<td><a href="#">查看</a></td>
					<td>未处理</td>
					<td>小明</td>
					<td>
						<div data-role="controlgroup" data-type="horizontal"><a href="#" data-role="button">确认</a><a href=""  data-role="button" >取消</a></div>					
					</td>
	           </tr> -->
	         </tbody>
	    </table>
	</div>
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">

		 <a data-role="button" data-inline="true" class="bottomBtnFont" id="wxOrderBack_a_wxOrder">返回</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" id="wxOrderRefresh_a_wxOrder">刷新</a>
		 
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
			<a href="javascript: Util.to.scroll({content:'wxOrderWin_div_wxBook', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'wxOrderWin_div_wxBook', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>		 
		 </div>
	</div>	
</div>
<!-- end 微定订单列表 -->


<!-- 预订订单列表start -->
<div data-role="page" id="bookOrderListMgr" data-theme="e">
	<div data-role="header" data-position="fixed" data-tap-toggle="false" data-theme="b" style="height: 40px;">
		<span id="" class="ui-btn-left" style="line-height: 40px;">
			预订列表
		</span>
	</div>
    <table style="width: 1024px;">
    	<tr>
    		<td style="width:10%;">
			    <div style="padding: 0 10px" align="left" >
					<fieldset id="daySelect_fieldset_tableSelect" data-role="controlgroup" data-type="horizontal" data-theme="b" style="width:400px;">
						<label>
				        	<input type="radio" name="bookDateType" data-type="bookDate_all" value="" checked="checked">全部
				        </label>
				        <label>
				        	<input type="radio" id="bookDate_today" name="bookDateType" value="" >今天
				        </label>
				        <label>
				        	<input type="radio" id="bookDate_tomorrow" name="bookDateType" value="">明天
				        </label>
				        <label>
				        	<input type="radio" id="bookDate_afterday" name="bookDateType" value="">后天
				        </label>
				        <label>
				        	<input type="radio" name="bookDateType" data-type="condition" value="" id="openConditionDay_input_tableSelect">自定义
				        </label>				        
				    </fieldset>
			    </div>	    		
    		</td>
    		<td id="conditionDayBegin_td_tableSelect" style="width:15%;display: none">
    			<input type="date" data-role="datebox" id="conditionDayBeginDay_input_tableSelect" data-type="neither" data-options='{"mode": "datebox"}' style="font-size: 12px;line-height: 30px;">
    		</td>
    		<td id="conditionDayEnd_td_tableSelect" style="width:15%;display: none;">
    			<input type="date" data-role="datebox" id="conditionDayEndDay_input_tableSelect" data-type="neither" data-options='{"mode": "datebox"}' style="font-size: 12px;line-height: 30px;">
    		</td>    		
    		<td style="width:15%;">
    			<input type="text" id="searchBookPerson_input_tableSelect" placeholder="姓名">
    		</td>
    		<td style="width:15%;">
    			<input type="text" id="searchBookPhone_input_tableSelect" placeholder="电话" onkeypress="intOnly()">
    		</td>
    		<td style="line-height: 20px;padding: 0 3px;width:9%;">
				<select  id="searchBookStatus" style="font-size: 20px;">
					<option value="-1">订单状态</option>
					<option value="1">待确认</option>
					<option value="2">待入座</option>
					<option value="3">已入座</option>
				</select>
    		</td>    		
    	</tr>
    </table>	
	<div id="bookOrderListCmp" style="overflow-y: auto;">
	<table id="bookOrderLists" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
         <thead>
           <tr class="ui-bar-d">
             <th style="width: 5px;"></th>
             <th style="width: 200px;">预订日期</th>
             <th >区域</th>
             <th style="width: 130px;">预订人</th>
             <th >预订电话</th>
			 <th style="width: 80px;">预订人数</th>
			 <th style="width: 110px;">状态</th>
			 <th style="width: 90px;">经手人</th>
			  <th style="width: 100px;">详情</th>
			 <th style="width: 230px;">操作</th>             
           </tr>
         </thead>
         <!-- 菜品列表 -->
         <tbody id="bookOrderListBody" style="font-weight:bold;color: blue">
<!--            <tr>
				<td>1</td>
				<td>2014-12-11 18:06:53</td>
				<td>大厅</td>
				<td>李先生</td>
				<td>13533464033</td>
				<td>8</td>
				<td><a href="#">查看</a></td>
				<td>未处理</td>
				<td>小明</td>
				<td>
					<div data-role="controlgroup" data-type="horizontal"><a href="#" data-role="button">确认</a><a href=""  data-role="button" >取消</a></div>					
				</td>
           </tr> -->
         </tbody>
    </table>
	</div>
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">

		 <a data-role="button" data-inline="true" class="bottomBtnFont" id="bookBack_a_tableSelect">返回</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" id="bookRefresh_a_tableSelect">刷新</a>
		 
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="addBooksInfo">添加</a>
			<a href="javascript: Util.to.scroll({content:'bookOrderListCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'bookOrderListCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>		 
		 </div>
	</div>	
</div>
<!-- end 预订订单列表 -->

<!-- 已点菜界面 start-->
<div data-role="page" id="orderFoodListMgr" data-theme="e">
	<div data-role="header" data-position="fixed" data-tap-toggle="false" data-theme="b" style="height: 40px;">
		<span id="divNorthForUpdateOrder" class="ui-btn-left" style="line-height: 40px;"></span>
	</div>
	<div id="orderFoodListCmp" style="overflow-y: auto;">
	<table id="orderFoodLists" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
         <thead>
           <tr class="ui-bar-d">
             <th style="width: 5px;"></th>
             <th style="width: 25%;">菜名</th>
             <th >数量</th>
             <th style="width: 15%;">口味</th>
             <th >单价</th>
			 <!-- <th >总价</th> -->
			 <th style="width: 80px;">时间</th>
			 <th style="width: 230px;">操作</th>
			 <th style="width: 70px;">服务员</th>             
           </tr>
         </thead>
         <!-- 菜品列表 -->
         <tbody id="orderFoodListBody" style="font-weight:bold;color: blue">
<!--            <tr>
				<td>1</td>
				<td>利雅园虾饺</td>
				<td>1</td>
				<td>无口味</td>
				<td>100</td>
				<td>100.00</td>
				<td>2014-12-11 18:06:53</td>
				<td>
					<div data-role="controlgroup" data-type="horizontal">
					    <a href="#takeoutOrderDetail" data-role="button">退菜</a>
					    <a href="javascript:deleteOrder()"  data-role="button" >转菜</a>
					</div>					
				</td>
				<td>鸣人</td>
           </tr> -->
         </tbody>
       </table>
		</div>
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div  class="bottomGeneralBar">
			<div id="spanTotalPriceUO" style="float: left;margin-left: 10px;">消费总额:--</div>
			<!-- 暂结状态 -->
			<div id="spanToTempPayStatus" style="float: left;margin-left: 20px;"></div>
			<div id="divDescForUpdateOrder" style="float: right;margin-right: 20px;"></div>
		</div>	
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="uo.cancelForUO()">返回</a>
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="ss" onclick="uo.goToCreateOrder()">点菜</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="uo.tempPayForUO()">暂结</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont none" id="payOrder_a_checkOut">结账</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="detail_a_tableSelect">明细</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#popupDiscountCmp">折扣</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="checkOutTranTable_a_tableSelect">转台</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="memberRead_a_orderFood">会员</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#updateFoodOtherOperateCmp" onclick="uo.openMoreOperate()">更多</a>
			<a href="javascript: Util.to.scroll({content:'orderFoodListCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'orderFoodListCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>		 
		 </div>
	</div>
	
	<div data-role="popup" id="popupDiscountCmp" data-theme="d" >
        <ul id="discountCmp" data-role="listview" data-inset="true" style="min-width:200px;" data-theme="b">
        	<li class="tempFoodKitchen"><a >八八折</a></li>
        </ul>
	</div>	
	 <!-- 账单更多操作 -->
	<div data-role="popup" id="updateFoodOtherOperateCmp" data-theme="d">
        <ul data-role="listview" data-inset="true" style="min-width:170px;" data-theme="b">
            <li class="tempFoodKitchen" onclick="uo.showOperatePeople()"><a >修改人数</a></li>
            <li class="tempFoodKitchen" onclick="uo.openCommentOperate()"><a >修改备注</a></li>
            <li class="tempFoodKitchen" id="weixinWaiter_li_tableSelect"><a>补打微信店小二</a></li>
            <li class="tempFoodKitchen" onclick="uo.tempPayForPrintAll()"><a >补打总单</a></li>
            <li class="tempFoodKitchen" onclick="uo.printDetailPatch()"><a >补打明细</a></li>
            <li class="tempFoodKitchen" id="allTrantable_li_tableSelect"><a >全单转菜</a></li>
            <li class="tempFoodKitchen" onclick="uo.allFoodHurried()"><a >全单催菜</a></li>
            <li class="tempFoodKitchen" onclick="uo.cancelTable()"><a >撤台</a></li>
        </ul>
	</div>		
	
     <!-- 单条已点菜更多操作 -->
	<div data-role="popup" id="orderFoodMoreOperateCmp" data-theme="d">
        <ul data-role="listview" id="orderFood_moreOpe" data-inset="true" style="min-width:100px;" data-theme="b">
            <li style="line-height: 40px;" id="btnWeighFood" onclick="uo.weighAction()"><a >称重</a></li>
            <li style="line-height: 40px;" id="btnGiftFood" onclick="uo.giftAction()"><a >赠送</a></li>
            <li style="line-height: 40px;" onclick="uo.hurriedFoodAction()"><a >催菜</a></li>
            <!-- <li style="line-height: 40px;" id="addPrint_li_tableSelect"><a>补打</a></li>  -->
        </ul>
	</div>		

	
	
	<!-- 修改备注 -->
	<div id="orderFoodCommentCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: 100px 0px 0px -200px;min-width:400px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	       	 修改备注
	    </div>
	    <table style="width:80%;">
	    	<tr>
	    		<td>
	    			<input id="inputUpdateComment" type="text" placeholder="备注" data-type="txt" class="countInputStyle">
	    		</td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('inputUpdateComment')">D</a>
					</div>		    		
	    		</td>
	    	</tr>
	    </table>
	    	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.saveComment()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.closeComment()">取消</a>		 
			 </div>
	    </div>
	</div>		
	
	<div id="shadowForPopup" style="z-index: 1101;opacity:0; position: absolute; top:0; left:0; width: 100%; height: 100%; background: #DDD;display: none;" ></div>
	<div id="cancelFoodSet" class="ui-overlay-shadow ui-corner-all" style="max-width:650px;z-index: 1102;position: absolute; top: 150px; left: 45%; margin: -100px 0px 0px -250px;background-color: white;display: none;" align="center">	
	<!-- <div id="cancelFoodSet" data-role="popup" data-theme="c" data-dismissible="false" style="max-width:650px;" class="ui-corner-all" align="center"> -->
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	        	请确定退菜原因和退菜数量
	    </div>
		<div id="calculator4CancelFood" class="calculator" style="width: inherit;" align="center">
			<div class="top">
				<span class="clear" id="cancelAdd_span_checkOut">+</span>
				<span class="inputs">
					<input id="inputCancelFoodSet" class="numberInputStyle" >
				</span>
				<span class="clear" id="cancelReduce_span_checkout">-</span>
			</div>
		</div>	
		<!-- 退菜原因列表 -->
		<div style="max-height: 175px;overflow-y: auto;padding-top: 5px;">
	    	<div id="cancelFoodReasonCmp" style="width:inherit;" align="left" >
	    		<!-- <a data-role="button" data-inline="true" class="regionBtn" onclick="">无原因</a> -->
	    	</div>
    	</div>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" >
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.cancelFoodAction()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.closeCancelFoodCmp()">取消</a>		 
			 </div>
	    </div>
	</div>		
	
</div>	
<!-- end 已点菜界面 -->

<!-- 点菜界面 start-->
<div data-role="page" id="orderFoodMgr" >
	<div class="ui-grid-a" style="width: 100%;background-color: skyblue;border-bottom:1px solid white;">
		<div class="ui-block-a" data-role="controlgroup" data-corners="false" style="width:102px;">
			 <a id="divNFCOTableBasicMsg" data-role="button" data-inline="true" style="font-size:20px;" class="tableBasicMsg">112<br>虞美人</a>
			 <a data-role="button" data-inline="true" class="tableStatus">已点菜</a>		 
		</div>
		<div class="ui-block-b" style="width: -webkit-calc(100% - 102px);width: -moz-calc(100% - 102px);width: -ms-calc(100% - 102px);width: -o-calc(100% - 102px);">
			<!-- 部门 -->
			 <div id="deptsCmp" data-role="controlgroup" data-type="horizontal">
				 <!--<a data-role="button" data-inline="true" class="deptKitBtnFont">全部部门</a>
 				<a href="#" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow left</a>
				<a href="#" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow right</a>	 -->				 	 
			 </div>		
			<!-- 厨房 -->
			 <div id="kitchensCmp" data-role="controlgroup" data-type="horizontal" >
<!-- 				 <a data-role="button" data-inline="true" class="deptKitBtnFont">全部厨房</a>
				<a href="#" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow left</a>
				<a href="#" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow right</a> -->					 	 
			 </div>					 		
		</div>		
	</div>

	<div id="orderFoodCenterCmp" class="ui-grid-a" style="height: 470px;overflow: hidden;">
	     <div class="ui-block-a" style="width: 35%;height: inherit;background-color: skyblue;">
		   <div class="ui-grid-a" style="height: inherit;">
		     <div id="divOrderFoodsCmp"  class="ui-block-a" style="width: -webkit-calc(100% - 99px);width: -moz-calc(100% - 99px);width: -ms-calc(100% - 99px);width: -o-calc(100% - 99px);height: inherit;overflow-y: auto;">
		     	<!--已点菜列表  -->
				<ul id="orderFoodsCmp" data-type="sdf" data-role="listview" data-theme="d" data-inset="true">
<!-- 				    <li data-icon="forFree">
				    	<a href="#">
					    	<h1>忒观音</h1>
					    	<span style="color:gray;">打包,加水,加热</span>
					    	<div>
					    		<span style="float: left;color: red;">叫起</span>
					    		<span style="float: right;">￥89 X <font color="green">1</font></span>
					    	</div>
					    	<br>
							<div ><ul data-role="listview" data-inset="false" class="div4comboFoodList">
						            <li>┕饮料<font color="blue"> —加大,中牌</font></li>
						            <li>┕水果<font color="blue"> —加辣</font></li>
						            <li>┕甜品<font color="blue"> —临时口味</font></li>
						    </ul></div>
					    
				    </a></li>
				    <li data-icon="">
				    	<a href="#">
					    	<h1>炒番茄</h1>
					    	<span style="color:gray;">加热</span>
					    	<div>
					    		<span style="float: left;color: red;">叫起</span>
					    		<span style="float: right;">￥89 X <font color="green">1</font></span>
					    	</div>
				    </a></li> -->
				</ul>		    	
		     </div>
		     <div class="ui-block-b" style="width: 99px;height: inherit;background-color: skyblue;border-left: 1px solid white;border-right: 1px solid white;" >
				 <div data-role="controlgroup" >
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foodAmountAdd_a_orderFood">数量+1</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foodAmountCut_a_orderFood">数量-1</a>	
					 <a href="#" data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foodAmountSet_a_orderFood">数量=</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="deleteFood_a_orderFood">删除</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="selectTaste_a_orderFood">口味</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="addTaste_a_orderFood">手写口味</a>
					 <a id="giftFoodOperate_a_orderFood" data-role="button" data-inline="true" class="orderOperBtn" style="display: none;" data-theme="b">赠送</a>	
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foopHangUp_a_orderFood">叫起</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" data-rel="popup"  data-transition="pop" href="#orderFoodOtherOperateCmp">更多</a>
				 </div>		
		     </div>
		     <!-- 点菜更多操作 -->
			<div data-role="popup" id="orderFoodOtherOperateCmp" data-theme="d">
		        <ul data-role="listview" data-inset="true" style="min-width:130px;" data-theme="b">
		        	<li class="tempFoodKitchen" id="updatePrice_li_orderFood"><a>修改时价</a></li>
		            <li class="tempFoodKitchen" id="allFoodTaste_li_orderFood"><a >全单口味</a></li>
		            <li class="tempFoodKitchen" id="allFoodHangUp_li_orderFood"><a>全单叫起</a></li>
		        </ul>
			</div>		     
		   </div>			     
	     
	     </div>
	     
	     <!-- 菜品列表 -->
	     <div id="foodsCmp_div_orderFood" class="ui-block-b" style="width: 65%;background-color: skyblue;">

	     </div>			
	</div>
	
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div id="divDescForCreateOrde" class="bottomGeneralBar">
			<div style="float: left;margin-left: 20px;">总数量: --, 合计: ￥--.--</div>
			<div id="foodPagingDesc_div_orderFood" style="float: right;margin-right: 20px;">共0项, 第1/1页, 每页?项</div>
		</div>	
		
		<div id="normalOperateFoodCmp" style="height: 60px;padding-top: 25px;">		
			<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="orderFoodBack_a_orderFood">返回</a>
			 	<a onclick="Util.to.scroll({content:'divOrderFoodsCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			 	<a onclick="Util.to.scroll({content:'divOrderFoodsCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>
			 </div>
			 <div data-role="controlgroup" class="ui-btn-right" data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="addBookOrderFood">选好了</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="bookSeatOrderFood_a_orderFood">入座</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="multiOpenTable_a_tableSelect">多台开席</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="normalOrderFood_a_orderFood">下单</a>
			 	<a data-role="button" data-theme="e" data-inline="true" style="width:120px;" class="bottomBtnFont" id="brand_a_orderFood">牌号结账</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="fastPay_a_orderFood">结账</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#orderMore_div_orderFood" id="orderFoodMore_a_orderFood">下单>></a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="aliasOrderFood_a_orderFood">助记码</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="handWriteBoard_a_orderFood">手写板</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="pinyinBoard_a_orderFood">拼音</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup" data-position-to="window" id="addTemp_a_orderFood" data-transition="pop" data-theme="b"  >临时菜</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="getPrevious_a_orderFood">上一页</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="getNextPage_a_orderFood">下一页</a>
<!-- 			 	<a onclick="of.foodPaging.getPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="foodsPaging">L</a>
				<a onclick="of.foodPaging.getNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="foodsPaging">R</a> -->	
			 </div>

		</div>
		<!--下单>>操作  -->	
		<div data-role="popup" id="orderMore_div_orderFood" data-theme="d">
	        <ul data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
	       	    <li id="orderPay_li_orderFood" class="tempFoodKitchen"><a>下单并结账</a></li>
	            <li id="orderNotPrint_li_orderFood" class="tempFoodKitchen"><a >下单不打印</a></li>
	            <li id="orderPre_li_orderFood" class="tempFoodKitchen"><a >先送</a></li>       	     
	        </ul>
		</div>
		 
	</div>
	
	<!-- 拼音搜索 -->
	<div id="orderPinyinCmp" class="ui-overlay-shadow ui-corner-all pinyinWindow" > 		
	   	<div class="handWritingtext"><input type="text" value="" id="pinyinInput_input_orderFood" style="font-size: 15px;font-weight: bold;"></div>
	   	<div class="handWritingbutton"><a id="pinyinDel_a_orderFood"  data-role="button" data-theme="d" class="ui-corner-bottom">删除</a></div>
	   	<div class="handWritingbutton"><a id="pinyinVal_a_orderFood"  data-role="button" data-theme="d" class="ui-corner-bottom">清空</a></div>	
	    <div class="handWritingbutton"><a data-role="button" data-theme="e" class="ui-corner-bottom" id="closePinyin_a_orderFood">关闭</a></div>	
	   		
		<!-- 拼音键盘 -->
	  	<div id="pinyin_div_orderFood" style="width:100%;height:100%;display:none;margin-top:8%;" ></div>	
	</div>	
	
	<!-- 手写搜索 -->
	<div id="orderHandCmp" class="ui-overlay-shadow ui-corner-all handWritingWindow" >
	   	<div class="handWritingtext"><input type="text" value="" id="handWritingInput_input_orderFood" style="font-size: 15px;font-weight: bold;"></div>
	   	<div class="handWritingbutton"><a id="handDel_a_orderFood"  data-role="button" data-theme="d" class="ui-corner-bottom">清空</a></div>	
		<div class="handWritingbutton"><a data-role="button" data-theme="d" class="ui-corner-bottom" id="rewrite_a_orderFood">重写</a></div>
	    <div class="handWritingbutton"><a data-role="button" data-theme="e" class="ui-corner-bottom" id="handWritingClose_a_orderFood">关闭</a></div>	
		<div class="handWritingWord" style="clear:both;" id="searchWord_div_orderFood"></div>
		<div id="handWritingPanel_th_orderFood" class="handWritingPanel"></div>
	</div>		
	
	<!-- <div class="ui-popup-screen in" id="addTempFoodCmp-screen" style="display: none;"></div> -->
	<div id="addTempFoodCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: -100px 0px 0px -250px;min-width:400px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top">
	        <h1>添加临时菜</h1>
	    </div>
	    
	    <table style="width:85%;">
	    	<tr>
	    		<td style="width:40px;"><label>名称:</label></td>
	    		<td><input id="tempFoodName_input_addTemp" type="text" placeholder="填写临时菜名称" data-type="txt" class="countInputStyle" ></td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempFoodName_input_addTemp')">D</a>
					</div>	    		
	    		</td>
	    	</tr>
	    	<tr>
	    		<td style="width:40px;"><label>价格:</label></td>
	    		<td><input id="tempFoodPrice_input_addTemp" type="text" placeholder="填写临时菜价格" data-type="num"  class="countInputStyle" ></td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempFoodPrice_input_addTemp')">D</a>
					</div>	    		
	    		</td>
	    	</tr>	    	
	    	<tr>
	    		<td style="width:40px;"><label>数量:</label></td>
	    		<td><input id="tempFoodCount_input_addTemp" type="text" placeholder="填写临时菜数量" value="1" data-type="num"  class="countInputStyle" ></td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempFoodCount_input_addTemp')">D</a>
					</div>		    		
	    		</td>
	    	</tr>	    	
	    	<tr>
	    		<td style="width:40px;"><label >分厨:</label></td>
	    		<td colspan="2">
	    			<a href="#popupTempFoodKitchensCmp" data-rel="popup" data-role="button" data-inline="true" data-transition="pop" data-theme="b">选择分厨</a>
	    			<label id="lab4TempKitchen" style="font-size: 25px;font-weight: bold;color: green;">临时厨房</label>
	    		</td>

	    	</tr>	    	
	    </table>

		<!-- <a href="#popupMenu" onclick="secondPop()" data-rel="popup" data-role="button" data-inline="true" data-transition="pop" data-theme="b">选择分厨t</a> -->
		
		<div data-role="popup" id="popupTempFoodKitchensCmp" data-theme="d" >
	        <ul id="tempFoodKitchensCmp" data-role="listview" data-inset="true" style="min-width:200px;" data-theme="d">
	        
	        </ul>
		</div>		
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" >
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" id="saveTemp_a_orderFood" data-inline="true" class="countPopbottomBtn">确定</a>
				 <a  data-role="button" id="closeTemp_a_orderFood" data-inline="true" class="countPopbottomBtn">取消</a>		 
			 </div>
	    </div>			
	</div>
	

</div>
<!-- end 点菜界面-->

<!-- 沽清界面 start -->
<div data-role="page" id="stopSellMgr" >
	<div class="ui-grid-a" style="width: 100%;background-color: skyblue;border-bottom:1px solid white;">
		<div class="ui-block-a" data-role="controlgroup" data-corners="false" style="width:102px;">
			 <a id="divBtnSellFood" data-role="button" data-inline="true" class="tableStatus" onclick="ss.searchData({event:this, isStop:false})">在售菜品</a>
			 <a data-role="button" data-inline="true" class="tableStatus" onclick="ss.searchData({event:this,isStop:true})">沽清菜品</a>		 
		</div>
		<div class="ui-block-b" style="width: -webkit-calc(100% - 102px);width: -moz-calc(100% - 102px);width: -ms-calc(100% - 102px);width: -o-calc(100% - 102px);">
			<!-- 部门 -->
			 <div id="depts4StopSellCmp" data-role="controlgroup" data-type="horizontal"></div>		
			<!-- 厨房 -->
			 <div id="kitchens4StopSellCmp" data-role="controlgroup" data-type="horizontal" ></div>					 		
		</div>	

	</div>
	<div id="stopSellCmp" class="ui-grid-a" style="height: 470px;overflow: hidden;">
	     <div id="divFoods4StopSellCmp" class="ui-block-a" style="width: 350px;height: inherit;overflow-y: auto;background-color: skyblue;border-right: 1px solid white;">
	     	<!--已点菜列表  -->
			<ul id="toStopSellFoodCmp" data-role="listview" data-theme="d" data-inset="true"></ul>		    	
	     </div>
	     
	     <!-- 菜品列表 -->
	     <div id="foods4StopSellCmp" class="ui-block-b" style="width: -webkit-calc(100% - 350px);width: -moz-calc(100% - 350px);width: -ms-calc(100% - 350px);width: -o-calc(100% - 350px);background-color: skyblue;"></div>			
	</div>
	
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div class="bottomGeneralBar">
			<div id="count4StopSellFoods" style="float: left;margin-left: 20px;">总数量: 4份, 合计: ￥300.50</div>
			<div id="foods4StopSellCmp-padding-msg" style="float: right;margin-right: 20px;">共0项, 第1/1页, 每页?项</div>
		</div>	
		
		<div id="normalOperateFood4StopSellCmp" style="height: 60px;padding-top: 25px;">		
			<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.back()">返回</a>
			 	<a onclick="Util.to.scroll({content:'divFoods4StopSellCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			 	<a onclick="Util.to.scroll({content:'divFoods4StopSellCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>
			 </div>
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.resetFoodLimit()">限量重置</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : true})">沽清</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : false})">开售</a>
			 	<!-- <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="of.openAliasOrderFood()">助记码</a> -->
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="searchSelloutFood('on')">搜索</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.tp.prev()">上一页</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.tp.next()">下一页</a>
			 </div>
		</div>
		<!-- 搜索组件 -->
		<div id="searchSelloutFoodCmp" style="height: 60px;padding-top: 25px;display: none;">		
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal" style="margin-right: 410px;">
			 <table>
			 	<tr>
			 		<td>
			 			<input id="searchSelloutFoodInput"  type="text" placeholder="输入菜名"  style="font-size: 20px;font-weight: bold;width: 150px;">
			 		</td>
			 		<td>
						<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:60px;" align="center">
						    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="ss.s.valueBack()">D</a>
						</div>			 		
			 		</td>
			 		<td>
					 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : true})">沽清</a>
					 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : false})">开售</a>
			 			<a data-role="button" data-inline="true" class="bottomBtnFont" style="margin-top: -5px;" onclick="searchSelloutFood()">关闭</a>
			 		</td>
			 	</tr>
			 </table>
			 </div>
		 </div>			
	
		<!--限量沽清修改数量 -->
		<div id="orderFoodLimitCmp" data-role="popup" data-theme="c" data-dismissible="false" style="max-width:900px;" class="ui-corner-all" align="center">
		    <div data-role="header" data-theme="b" class="ui-corner-top">
		        <h1>输入剩余数量</h1>
		    </div>
		    <div style="min-height: 300px; overflow-y: auto;">
				<div class="calculator">
					<div class="top">
						<span class="clear">+</span>
						<span class="inputs">
							<input id="inputOrderFoodLimitCountSet" style="font-size: 20px;font-weight: bold;" onkeypress="intOnly()" onfocus="setInput('inputOrderFoodLimitCountSet')">
						</span>
						<span class="clear">-</span>
					</div>
					<div class="keys">
						<span>7</span>
						<span>8</span>
						<span>9</span>
						<span>0</span>
						
						<span>4</span>
						<span>5</span>
						<span>6</span>
						<span>.</span>
						
						<span>1</span>
						<span>2</span>
						<span>3</span>
						<span class="clear">C</span>
					</div>
				</div>		    
		    
			</div>	
			<div data-role="footer" data-theme="b" class="ui-corner-bottom">
				 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
					 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ss.setFoodLimitRemaining()">确定</a>
					 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ss.closeFoodLimitCmp()">取消</a>		 
				 </div>
		    </div>
		</div>			
		 
	</div>	
</div>
<!-- end 沽清界面  -->

<!-- 结账界面 start -->
<div data-role="page" id="paymentMgr" data-theme="e">
	<div id="paymentCmp" class="ui-grid-a">
		<div class="ui-block-a" style="width:330px;border-right: 2px solid gray;height: inherit;">
			<table id="orderDetialTable"  data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr" >
		         <thead>
		           <tr class="ui-bar-d">
		             <th >收款</th>
		             <th>金额</th>
		             <th >操作</th>
		           </tr>
		         </thead>
		         <tbody>	
		         	<tr>
		         		<td >实收:</td>
		         		<td id="actualPrice_td_payment" style="color: blue;">0.00</td>
		         		<td>----</td>
		         	</tr>
		         	<tr>
		         		<td>应收:</td>
		         		<td id="totalPrice">0.00</td>
		         		<td>----</td>
		         	</tr>
		         	<tr>
		         		<td>赠送:</td>
		         		<td id="forFree">0.00</td>
		         		<td ><label id="lab_replaceGiftBtn">----</label><a id="spanSeeGiftFoodAmount_label_tableSelect" href="#" style="display: none;">明细</a></td>
		         	</tr>
		         	<tr>
		         		<td>折扣:</td>
		         		<td id="discountPrice">0.00</td>
		         		<td ><label id="lab_replaceDiscountBtn">----</label><a id="spanSeeDiscountFoodAmount_label_tableSelect" href="#" style="display: none;">明细</a></td>
		         	</tr>
		         	<tr>
		         		<td>退菜:</td>
		         		<td id="spanCancelFoodAmount">0.00</td>
		         		<td ><label id="lab_replaceCancelBtn">----</label><a id="spanSeeCancelFoodAmount_label_tableSelect" href="#" style="display: none;" >明细</a></td>
		         	</tr>	
		         	<tr id="eraseQuota_tr_payment">
		         		<td colspan="2" style="width: 200px;line-height: 60px;font-size: 20px;text-align: right;" >抹数金额(上限:￥<font id="eraseQuota_font_payment" color="red">--</font>)：</td>
		         		<td width="70" style="padding-right: 5px;">
		         			<input id="erasePrice_input_payment" class="countInputStyle">
		         		</td>
		         	</tr>
		         	<tr>
		         		<td style="width: 90px;line-height: 60px;">备注:</td>
		         		<td colspan="2" style="padding-right: 5px;">
		         			<input id="remark">
		         		</td>
		         	</tr>	         	
		         </tbody>
		    </table>	    
		    <!-- 按钮 -->
		    <div align="center">
		    	<a id="cash_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">现金(+)</a>
		    	<a id="credit_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">刷卡</a>
		    	<a id="sign_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">签单</a>
		    	<a id="hang_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">挂账</a>		    	
		    	<a id="cashReceive_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">现金找零</a>
		    	<a id="mixed_a_payment" data-role="button" data-theme="b" data-rel="popup" data-position-to="window" data-transition="pop" data-inline="true" style="width: 45%;">其他结账</a>
		    	<a data-role="button" data-theme="b" id="memberBalance_a_payment" data-rel="popup" data-position-to="window" data-transition="pop" data-inline="true" style="width: 45%;">读取会员</a>
		    	<a id="wxPay_li_tableSelecy" data-role="button" data-theme="b" data-inline="true" data-rel="popup"  data-transition="pop" style="width: 45%;" href="#wxPayPopup_div_tableSelect">微信支付</a>
		    </div>
		    
		     <!-- 账单更多操作 -->
			<div data-role="popup" id="wxPayPopup_div_tableSelect" data-theme="d">
		        <ul data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
		            <li class="tempFoodKitchen" id="wx_li_payment""><a >二维码支付</a></li>
		            <li class="tempFoodKitchen" id="authCode_li_payment"><a >扫描枪支付</a></li>
		        </ul>
			</div>	
		    
		</div>
		<div class="ui-block-b" data-role="content" style="width : -webkit-calc(100% - 330px);width: -moz-calc(100% - 330px);width : -ms-calc(100% - 330px);width: -o-calc(100% - 330px);height: inherit;">
			<div id="divNorthForUpdateOrder"  data-role="header" data-tap-toggle="false" data-theme="b" style="height: 36px;overflow: hidden;">
				<span id="orderIdInfo" style="line-height: 35px;float: left;margin-left: 5px;">
					结账 -- 账单号:<font color="#f7c942">-----</font>
				</span>
				<span id="orderTableInfo" style="line-height: 35px;float: left;margin-left: 20px;">
					 餐桌号:<font color="#f7c942">-- (---)</font>
				</span>
				<span style="line-height: 35px;float: left;margin-left: 20px;">
					 当前服务费: <font id="spanDisplayCurrentServiceRate" color="#f7c942">--</font>
				</span>
				<span style="line-height: 35px;float: left;margin-left: 20px;">
					 人数 : <font id="orderCustomNum" color="#f7c942">1</font>
				</span>
			</div>
			<div id="payment_orderFoodListCmp" style="overflow-y: auto;">
			<table id="orderFoodsTable"  data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr" >
		         <thead>
		           <tr class="ui-bar-d">
		             <th style="width: 5px;"></th>
		             <th style="width: 25%;">菜名</th>
		             <th >数量</th>
		             <th style="width: 15%;">口味</th>
		             <th style="min-width: 68px;">口味价钱</th>
		             <th >单价</th>
					 <th style="min-width: 55px;">折扣率</th>
					 <th >总价</th>
					 <th >时间</th>
					 <th style="min-width: 55px;">服务员</th>             
		           </tr>
		         </thead>
		         <tbody id="payment_orderFoodListBody" style="font-weight:bold;color: blue">	
<!-- 		         	<tr>
		         		<td>1</td>
		         		<td >龙虾排骨</td>
		         		<td >1</td>
		         		<td>无口味</td>
		         		<td>0.00</td>
		         		<td>8.88</td>
		         		<td>0.5</td>
		         		<td >11.5</td>
		         		<td>14:23:23</td>
		         		<td>管理员</td>
		         	</tr> -->
		         </tbody>
		    </table>	
		    </div>		
		</div>
	</div>
	
	
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div  class="bottomGeneralBar" style="background-color:#f7c942">
			<div id="divDescForUpdateOrder" style="float: right;">
				<span id="orderCouponInfo" style="float: left;margin-right: 20px;"></span>	
				<span id="memberInfo_span_payment" style="float: left;margin-right: 20px;"></span>			
				<span id="orderDiscountDesc" style="float: left;margin-right: 20px;">
					当前折扣: <font color="green" >----</font>
				</span>						
			</div>
		</div>		
		 <a data-role="button" data-inline="true" class="bottomBtnFont" href="#tableSelectMgr" >返回</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="refreshOrderData({calc:true})" >刷新</a>
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="issueCoupon_a_orderFood">发券</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="useCoupon_a_orderFood">用券</a>
			<a data-role="button" data-theme="e" data-inline="true" style="width:150px;" class="bottomBtnFont" id="cashReceives_a_payment">现金找零</a>
		 	<a id="tempPay_a_payment" data-role="button" data-inline="true" class="bottomBtnFont">暂结(-)</a>
		 	<a id="updateOrder_a_payment" data-role="button" data-inline="true" class="bottomBtnFont">改单</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#discount_div_payment">折扣</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#servicePlan_div_payment">服务费</a>
			<a id="memberRead_a_payment" data-role="button" data-inline="true" class="bottomBtnFont">会员</a>
			<a href="javascript: Util.to.scroll({content:'payment_orderFoodListCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'payment_orderFoodListCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>			
		 </div>
	</div>	
	
	<div data-role="popup" id="discount_div_payment" data-theme="d" >
        <ul id="discount_ul_payment" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b"></ul>
	</div>	
	<div data-role="popup" id="servicePlan_div_payment" data-theme="d" >
        <ul id="servicePlan_ul_payment" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b"></ul>
	</div>	
	
<!-- 收款操作 -->	
	<div id="cashReceive_div_payment" data-role="popup"  data-overlay-theme="e" data-theme="d" data-dismissible="false" class="ui-corner-all" align="center">
	    <div data-role="header" class="ui-corner-top" data-theme="b">
	        <h1>输入收款显示找零</h1>
	    </div>
	    <div style="width: 100%;padding: 5px 10px;" >
	    <table id="inputReciptWinTable">
	    	<tr>
	    		<td>消费金额:</td>
	    		<td><label id="consume4CashReceive_a_payment"></label></td>
	    	</tr>
	    	<tr>
	    		<td>输入收款:</td>
	    		<td><input class="numberInputStyle" id="cashReceive_input_payment" onkeypress="intOnly()"></td>
	    	</tr>
	    	<tr>
	    		<td>找零:</td>
	    		<td><label id="cashBack_label_payment">0</label></td>
	    	</tr>	    		    	
	    </table>
	    </div>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a id="receivedCashConfirm_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">确定</a>
				 <a id="receivedCashCancel_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">取消</a>		 
			 </div>
	    </div>
	 </div>	
	 
	 <!-- 显示会员信息 -->
	<div id="showMemberInfoWin" data-role="popup"  data-overlay-theme="e" data-theme="d" data-dismissible="false" class="ui-corner-all" style="min-width:700px;" align="center">	
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="line-height: 35px;">
	        	会员信息
	    </div>    
	    <table>
	    	<tr>
	    		<td class="readMemberTd">会员名称:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainName">----</label></td>
	    		<td class="readMemberTd">基础余额:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainBalance">----</label></td>  
	    		<td class="readMemberTd">赠送余额:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainGift">----</label></td>  	
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">会员手机:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainPhone">----</label></td>       
	    		<td class="readMemberTd">会员类型:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainType">----</label></td>
	    		<td>
					<label>
				        <input type="checkbox" id="memberPaymentGiftPrice">赠送扣额:  
				    </label>	 
				    		
	    		</td>
	    		
	    		<td>
	    			<input id="useLimit_input_payment" onkeypress="intOnly()"> 
	    		</td>
	    	</tr> 
	    	<tr>
	    		<td class="readMemberTd">会员卡号:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainCard">----</label></td>   
    			<td class="readMemberTd">会员积分:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainPoint">----</label></td>   
	    		<td >
					<label>
				        <input type="checkbox" id="memberPaymentSendSMS">是否发送短信
				    </label>	    		
	    		</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainPoint"></label></td>   
	    	</tr>	    
	    </table>
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-corners="false" data-type="horizontal" class="bottomBarFullWidth ui-bar-b barBottomBeCorner">
				 <a id="memberPay_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">结账</a>
				 <a id="memberPayCancel_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">取消</a>		 
			 </div>
	    </div>	
	</div>		 
	 
</div>
<!-- end 结账界面 -->
</body>
</html>