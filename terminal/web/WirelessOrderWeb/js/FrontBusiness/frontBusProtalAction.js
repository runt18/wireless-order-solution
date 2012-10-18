var protalFuncReg = function() {
	$("#order").each(function(){
		$(this).bind("click", function(){
			if (!isPrompt){
				location.href = "TableSelect.html?pin=" + currPin + "&restaurantID=" + restaurantID;
			}
		});
	});

	$("#bill").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				billVerifyWin.show();
				isPrompt = true;
			}
		});
	});

	$("#shift").each(function(){
		$(this).bind("click", function(){
			if (!isPrompt){
				Ext.Ajax.request({
					url : "../../QueryShift.do",
					params : {
						"pin" : currPin
					},
					success : function(response, options){
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						if (resultJSON.success == true) {
							shiftWin.show();
							isPrompt = true;

							// update the shift data
							// 后台：  ["开始日期","结帐日期","账单数","现金金额","现金实收","刷卡金额",
							// "刷卡实收","会员卡金额","会员卡实收","签单金额","签单实收",
							// "挂账金额","挂账实收","实收金额","折扣金额","赠送金额"
							var dataInfo = resultJSON.data;
							var shiftList = dataInfo.substr(1, dataInfo.length - 2).split(",");
							
							var oprName = "";
							for(var i = 0; i < emplData.length; i++){
								if (emplData[i][0] == currPin){
									oprName = emplData[i][1];
								}
							};
							
							document.getElementById("shiftOperator").innerHTML = oprName;
							document.getElementById("shiftBillCount").innerHTML = shiftList[2].substr(1, shiftList[2].length - 2);
							document.getElementById("shiftStartTime").innerHTML = shiftList[0].substr(1, shiftList[0].length - 2);
							document.getElementById("shiftEndTime").innerHTML = shiftList[1].substr(1, shiftList[1].length - 2);
							document.getElementById("amount1").innerHTML = shiftList[3].substr(1, shiftList[3].length - 2);
							document.getElementById("actual1").innerHTML = shiftList[4].substr(1, shiftList[4].length - 2);
							document.getElementById("amount2").innerHTML = shiftList[5].substr(1, shiftList[5].length - 2);
							document.getElementById("actual2").innerHTML = shiftList[6].substr(1, shiftList[6].length - 2);
							document.getElementById("amount3").innerHTML = shiftList[7].substr(1, shiftList[7].length - 2);
							document.getElementById("actual3").innerHTML = shiftList[8].substr(1, shiftList[8].length - 2);
							document.getElementById("amount4").innerHTML = shiftList[9].substr(1, shiftList[9].length - 2);
							document.getElementById("actual4").innerHTML = shiftList[10].substr(1, shiftList[10].length - 2);
							document.getElementById("amount5").innerHTML = shiftList[11].substr(1, shiftList[11].length - 2);
							document.getElementById("actual5").innerHTML = shiftList[12].substr(1, shiftList[12].length - 2);
							document.getElementById("discountAmt").innerHTML = shiftList[14].substr(1, shiftList[14].length - 2);
							document.getElementById("freeAmt").innerHTML = shiftList[15].substr(1, shiftList[15].length - 2);
							document.getElementById("payAmt").innerHTML = shiftList[13].substr(1, shiftList[13].length - 2);
							shiftStartTiem = shiftList[0].substr(1, shiftList[0].length - 2);
							shiftEndTiem = shiftList[1].substr(1, shiftList[1].length - 2);
						} else {
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					},
					failure : function(response, options){
						
					}
				});
			}
		});
	});

	$("#dailySettle").each(function(){
		$(this).bind("click", function(){
			if (!isPrompt) {
				Ext.Ajax.request({
					url : '../../QueryDailySettleByNow.do',
					params : {
						pin : pin
					},
					success : function(res, opt){
//						alert(res.responseText);
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							shiftCheckDate = jr;
							dailySettleCheckTableWin.show();
						}else{
							Ext.Msg.show({
								title : '错误',
								msg : '加载日结信息失败.'
							});
						}
					},
					failure : function(res, opt){
						Ext.Msg.show({
							title : '错误',
							msg : '加载日结信息失败.'
						});
					}
				});
			}
		});
	});
};