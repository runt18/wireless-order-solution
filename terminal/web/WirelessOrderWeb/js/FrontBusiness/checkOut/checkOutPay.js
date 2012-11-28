var paySubmit = function(submitType) {
	
	checkOurListRefresh();
	
	var canSubmit = true;
	// var actualPrice = checkOutForm.findById("actualCount").getValue();
	var forFree = document.getElementById("forFree").innerHTML;
	var change = document.getElementById("change").innerHTML;
	var cancelledFoodAmount = document.getElementById("backFoodAmount_div").innerHTML;
	var actualPrice = document.getElementById("actualCount").value;
	var countPrice = document.getElementById("totalCount").innerHTML;
	var shouldPay = document.getElementById("shouldPay").innerHTML;
	var serviceRate = document.getElementById("serviceCharge").value;
	var eraseQuota = document.getElementById("txtEraseQuota").value;
	var submitPrice = -1;
	var discountID = Ext.getCmp('comboDiscount');

	var payManner = -1;
	var tempPay;

	// 现金
	if (submitType == 1) {
		submitPrice = actualPrice;
	} else {
		submitPrice = originalTotalCount;
	}

	// 暂结，调整参数
	if (submitType == 6) {
		tempPay = "true";
		payManner = 1;
	} else {
		tempPay = "";
		payManner = submitType;
	}
	
	// 服务费率
	if (serviceRate < 0 || serviceRate > 100) {
		setFormButtonStatus(false);
		Ext.Msg.alert("提示", "<b>服务费率范围是0%至100%！</b>");
		canSubmit = false;
	}
	
	// 抹数金额
	if(!isNaN(eraseQuota) && eraseQuota >=0 && eraseQuota > restaurantData.setting.eraseQuota){
		setFormButtonStatus(false);
		Ext.Msg.alert("提示", "<b>抹数金额大于设置上限，不能结帐！</b>");
		canSubmit = false;
	}
	
	// 会员卡结帐，检查余额；现金校验
	if (submitType == 3 && parseFloat(countPrice) > parseFloat(mBalance) && payType == 2) {
		setFormButtonStatus(false);
		Ext.Msg.alert("提示", "<b>会员卡余额小于合计金额，不能结帐！</b>");
		canSubmit = false;
	} else if (submitType == 1 && parseFloat(actualPrice + eraseQuota) < parseFloat(shouldPay)) {
		setFormButtonStatus(false);
		Ext.Msg.alert("提示", "<b>实缴金额小于应收金额，不能结帐！</b>");
		canSubmit = false;
	}
	
	
	
	if (canSubmit) {
		Ext.Ajax.request({
			url : "../../PayOrder.do",
			params : {
				"pin" : Request["pin"],
				"tableID" : Request["tableNbr"],
				"cashIncome" : submitPrice,
				"payType" : payType,
				'discountID' : discountID.getValue(),
				"payManner" : payManner,
				"tempPay" : tempPay,
				"memberID" : actualMemberID,
				"comment" : checkOutForm.findById("remark").getValue(),
				"serviceRate" : serviceRate,
				'eraseQuota' : eraseQuota
			},
			success : function(response, options) {
				var resultJSON = Ext.decode(response.responseText);
				var dataInfo = resultJSON.data;
				
				if (resultJSON.success == true) {
					var interval = 5;
					var action = '';
					
					if (submitType == 6) {
						Ext.example.msg('提示', dataInfo);
					}else{
						
						action = '&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
						
						new Ext.Window({
							title : '<center>结账信息</center>',
							width : 700,
							modal : true,
							closable : false,
							resizable : false,
							layout : 'column',
							defaults : {
								xtype : 'label',
								columnWidth : .25,
								style : 'vertical-align:middle; line-height:36px; padding-left:20px; font-size:15px; font-weight: bold;'
							},
							items : [{
								html : '应收：￥<font color="red">'+countPrice+'</font>'
							}, {
								html : '实收：￥<font color="red">'+shouldPay+'</font>'
							}, {
								html : '收款：￥<font color="red">'+actualPrice+'</font>'
							}, {
								html : '找零：￥<font color="red">'+change+'</font>'
							}, {
								html : '抹数金额：￥<font color="red">'+eraseQuota+'</font>'
							}, {
								html : '退菜金额：￥<font color="red">'+cancelledFoodAmount+'</font>'
							}, {
								html : '赠送：￥<font color="red">'+forFree+'</font>'
							}, {
								html : '服务费：￥<font color="red">'+serviceRate+'</font>'
							}, {
								columnWidth : 1,
								style : 'font-size:22px; line-height:40px; text-align:center;',
								html : (dataInfo + '.' + action)
							}],
							buttonAlign : 'center',
							buttons : [{
								text : '确&nbsp;&nbsp;定',
								handler : function(e){
									if (submitType != 6) {
										location.href = "TableSelect.html?pin="
												+ Request["pin"] + "&restaurantID="
												+ restaurantID;
									}
								},
								listeners : {
									render : function(e){
										e.getEl().setWidth(200, true);
									}
								}
							}],
							listeners : {
								show : function(){
									new Ext.util.TaskRunner().start({
										run: function(){
											if(interval < 1){
												location.href = "TableSelect.html?pin="
													+ Request["pin"] + "&restaurantID="
													+ restaurantID;
											}
											Ext.getDom('returnInterval').innerHTML = interval;
											interval--;
									    },
									    interval : 1000
									});
								}
							}
						}).show(document.body);						
						
					}
				} else {
					var dataInfo = resultJSON.data;
					Ext.MessageBox.show({
						msg : dataInfo,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
				setFormButtonStatus(false);
			},
			failure : function(response, options) {
				setFormButtonStatus(false);

				Ext.MessageBox.show({
					msg : "Unknow page error",
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		});
	}

};
