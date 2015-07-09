Ext.onReady(function(){
	
	var memberTitle = '<tr>' 
					+ '<th class="table_title text_center">会员操作</th>'
			 		+ '<th class="table_title text_center">现金</th>'
			 		+ '<th class="table_title text_center">刷卡</th>'
			 		+ '<th class="table_title text_center">账户实充/扣额</th>'
			 		+ '</tr>';
	var memberTrModel = '<tr>'
					+ '<th>会员充值</th>'
					+ '<td class="text_right">{0}</td>'
					+ '<td class="text_right">{1}</td>'
					+ '<td class="text_right">{2}</td>'
					+ '</tr>'
					+ '<tr>'
					+ '<th>会员退款</th>'
					+ '<td class="text_right">{3}</td>'
					+ '<td class="text_right">{4}</td>'
					+ '<td class="text_right">{5}</td>'
					+ '</tr>';
	var title = '<tr>'
			  + '<th class="table_title text_center">部门汇总</th>'
			  + '<th class="table_title text_center">折扣总额</th>'
			  + '<th class="table_title text_center">赠送总额</th>'
			  + '<th class="table_title text_center">应收总额</th>'
			  + '</tr>';
	var trModel = '<tr>'
				+ '<th>{0}</th>'
				+ '<td class="text_right">{1}</td>'
				+ '<td class="text_right">{2}</td>'
				+ '<td class="text_right">{3}</td>'
				+ '</tr>';
				
	var trPayIncomeModel = '<tr>'
				+ '<th>{0}</th>'
				+ '<td class="text_right">{1}</td>'
				+ '<td class="text_right">{2}</td>'
				+ '<td class="text_right">{3}</td>'
				+ '</tr>';				
	
	var pelement = Ext.query('#businessStatisticsDIV')[0].parentElement;
	var mw = parseInt(pelement.style.width);
	var mh = parseInt(pelement.style.height);
	
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
//				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
//				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('btnSearchForBusinessStatisticsSummaryInformation').handler();
		}
	});
	
	var bssifLoadMarsk = new Ext.LoadMask(document.body, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});
	
	new Ext.Panel({
		renderTo : 'businessStatisticsDIV',
		id : 'businessStatisticsDIVPanel',
		border : false,
		width : mw,
		height : mh,
		layout : 'border',
		items : [{
			region : 'west',
			width : '50%',
			frame : true,
			height : 800,
			contentEl : 'divBusinessStatisticsSummaryInformation'
		}, {
			region : 'center',
			frame : true,
			autoScroll : true,
			id : 'businessStatisticsSummaryInformationCenterPanel',
			listeners : {
				render : function(thiz){
					var memberTrEmpty = String.format(memberTrModel, '----', '----', '----', '----', '----', '----');
					var empty = String.format(trModel, '---', '---', '---', '---');
					var table;
					if(eval(businessStatic == 2)){
						table = String.format('<table border="1" class="tb_base">{0}{1}</table>', 
								memberTitle, memberTrEmpty);
					}else{
						table = String.format('<table border="1" class="tb_base">{0}{1}</table><br><table border="1" class="tb_base">{2}{3}</table>', 
								memberTitle, memberTrEmpty, title, empty);
					}

					thiz.body.update(table);
				}
			}
		}],
		tbar : new Ext.Toolbar({
			id : 'businessStatisticTbar',
			height : 26,
			items : [ {
				xtype : 'tbtext',
				text : '日期:'
			}, dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, endDate, '->', {
				text : '搜索',
				id : 'btnSearchForBusinessStatisticsSummaryInformation',
				iconCls : 'btn_search',
				handler : function(e){
					var paramsOnDuty = '', paramsOffDuty = '';
					var requestUrl = '../../BusinessStatistics.do';
					var params = {
							dataSource : dataSource,
							queryPattern : queryPattern,
							onDuty : paramsOnDuty,
							offDuty : paramsOffDuty,
							dutyRange : dutyRange,
							staffId : staffId
					};
					if(queryPattern == 1){
						if(!beginDate.isValid() || !endDate.isValid()){
							return;
						}
						params.onDuty = beginDate.getValue().format('Y-m-d 00:00:00');
						params.offDuty = endDate.getValue().format('Y-m-d 23:59:59');
					}else if(queryPattern == 2){
						params.onDuty = onDuty.format('Y-m-d H:i:s');
						params.offDuty = offDuty.format('Y-m-d H:i:s');
					}else if(queryPattern == 3){
						params.onDuty  = onDuty.format('Y-m-d 00:00:00');
						params.offDuty = offDuty.format('Y-m-d 23:59:59');
					}else if(queryPattern == 4){
						beginDate.clearInvalid();
						endDate.clearInvalid();
						requestUrl = '../../QueryDailySettleByNow.do';
						params = {queryType : queryType};
						Ext.getCmp('total_exoprt_excel').hide();
						Ext.getCmp('btnSearchForBusinessStatisticsSummaryInformation').hide();
					}else if(queryPattern == 5){
						params.onDuty = onDuty;
						params.offDuty = offDuty;
					}else{
						return;
					}
					
					bssifLoadMarsk.show();
					Ext.Ajax.request({
						url : requestUrl,
						params : params,
						success : function(response, options) {
							var jr = Ext.decode(response.responseText);
							if(jr.success){
								business = jr.other.business;
								var deptStat = business.deptStat;
								
								var trContent = '';
								if(businessStatic != 2){
									for(var i = 0; i < deptStat.length; i++){
										var temp = deptStat[i];
										trContent += (String.format(trModel, 
												temp.dept.name, 
												temp.discountPrice.toFixed(2), 
												temp.giftPrice.toFixed(2), 
												temp.income.toFixed(2)
											)
										);
									}
								}

								
								var memberTrDate = String.format(memberTrModel, business.memberChargeByCash.toFixed(2), business.memberChargeByCard.toFixed(2), business.memberAccountCharge.toFixed(2),
																business.memberRefund.toFixed(2), 0.00, business.memberAccountRefund.toFixed(2));
								var table;
								if(eval(businessStatic == 2)){
									table = String.format('<table border="1" class="tb_base">{0}{1}</table>', 
											memberTitle, memberTrDate);
								}else{
									table = String.format('<table border="1" class="tb_base">{0}{1}</table><br><table border="1" class="tb_base">{2}{3}</table>', 
											memberTitle, memberTrDate, title, trContent);
								}			
								
								//是否有预订金额
								if(business.bookIncome > 0){
									table += '<br><table border="1" class="tb_base"><tr><th class="table_title text_center">预订总金额:</th><th class="table_title text_center">'+ business.bookIncome +'</th></tr></table>';
								}
								
								Ext.getCmp('businessStatisticsSummaryInformationCenterPanel').body.update(table);
								
								if(dutyRange == "range"){
									Ext.getDom('bssiOnDuty').innerHTML = beginDate.getValue().format('Y-m-d');
								    Ext.getDom('bssiOffDuty').innerHTML = endDate.getValue().format('Y-m-d');
								}else{
									Ext.getDom('bssiOnDuty').innerHTML = business.paramsOnDuty;
									Ext.getDom('bssiOffDuty').innerHTML = business.paramsOffDuty;
								}

								Ext.getDom('bssiOrderAmount').innerHTML = business.orderAmount;
								
								Ext.getDom('bssiEraseAmount').innerHTML = business.eraseAmount;
								Ext.getDom('bssiEraseIncome').innerHTML = business.eraseIncome.toFixed(2);
								
								Ext.getDom('bssiDiscountAmount').innerHTML = business.discountAmount;
								Ext.getDom('bssiDiscountIncome').innerHTML = business.discountIncome.toFixed(2);
								
								Ext.getDom('bssiGiftAmount').innerHTML = business.giftAmount;
								Ext.getDom('bssiGiftIncome').innerHTML = business.giftIncome.toFixed(2);
								
								Ext.getDom('bssiCouponAmount').innerHTML = business.couponAmount;
								Ext.getDom('bssiCouponIncome').innerHTML = business.couponIncome.toFixed(2);
								
								Ext.getDom('bssiCancelAmount').innerHTML = business.cancelAmount;
								Ext.getDom('bssiCancelIncome').innerHTML = business.cancelIncome.toFixed(2);
								
								Ext.getDom('bssiPaidAmount').innerHTML = business.paidAmount;
								Ext.getDom('bssiPaidIncome').innerHTML = business.paidIncome.toFixed(2);
								
								Ext.getDom('bssiServiceAmount').innerHTML = business.serviceAmount;
								Ext.getDom('bssiServiceIncome').innerHTML = business.serviceIncome.toFixed(2);
								
								
								var trPayTypeContent='<tr>'
								  + '<th class="table_title text_center">收款方式</th>'
								  + '<th class="table_title text_center">账单数</th>'
								  + '<th class="table_title text_center">应收总额</th>'
								  + '<th class="table_title text_center">实收总额</th>'
								  + '</tr>';								
								//输出付款方式集合
								var totalCount = 0, totalShouldPay = 0, totalActual = 0, trPayIncomeData;
								for(var i = 0; i < business.paymentIncomes.length; i++){
									var temp = business.paymentIncomes[i];
									totalCount += temp.amount;
									totalShouldPay += temp.total;
									totalActual += temp.actual;
									
									trPayTypeContent += (String.format(trPayIncomeModel, 
											temp.payType, 
											temp.amount, 
											temp.total.toFixed(2), 
											temp.actual.toFixed(2)
										)
									);
									
								}
								//汇总
								trPayTypeContent += (String.format(trPayIncomeModel, 
									'总计', 
									totalCount, 
									totalShouldPay.toFixed(2), 
									totalActual.toFixed(2)
								));
								
								Ext.getDom('businessStatisticsSummaryPayIncome').innerHTML = trPayTypeContent;
								
							}else{
								Ext.ux.showMsg(jr);								
							}
							bssifLoadMarsk.hide();
						},
						failure : function(response, options) {
							Ext.ux.showMsg(Ext.decode(response.responseText));
							bssifLoadMarsk.hide();
						}
					});
				}
			}, '-', {
				text : '导出',
				id : 'total_exoprt_excel',
//				hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var paramsOnDuty = '', paramsOffDuty;
					if(queryPattern == 1 || queryPattern == 3){
						if(!beginDate.isValid() || !endDate.isValid()){
							return;
						}
						paramsOnDuty = beginDate.getValue().format('Y-m-d 00:00:00');
						paramsOffDuty = endDate.getValue().format('Y-m-d 23:59:59');
					}else if(queryPattern == 2){
						paramsOnDuty = onDuty.format('Y-m-d H:i:s');
						paramsOffDuty = offDuty.format('Y-m-d H:i:s');
					}else{
						return;
					}
					var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&queryPattern={6}&dataType={7}&isCookie=true';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							-10, 
							restaurantID, 
							'business',
							paramsOnDuty,
							paramsOffDuty,
							queryPattern,
							dataSource
						);
					window.location = url;
				}
			}]
		}),
		listeners : {
			render : function(){
				if(queryPattern == 1){
					dateCombo.setDisabled(false);
					beginDate.setDisabled(false);
					endDate.setDisabled(false);
				}else if(queryPattern == 2 || queryPattern == 3 || queryPattern == 4 || queryPattern == 5){
					beginDate.setValue(onDuty);
					endDate.setValue(offDuty);
					
					beginDate.clearInvalid();
					endDate.clearInvalid();
					
					Ext.getCmp('btnSearchForBusinessStatisticsSummaryInformation').handler();
					
					dateCombo.setDisabled(true);
					beginDate.setDisabled(true);
					endDate.setDisabled(true);
					
					if(queryPattern == 5){
						Ext.getCmp('businessStatisticTbar').hide();
					}
				}else if(queryPattern == 6){
					Ext.getCmp('businessStatisticTbar').hide();
				}
			}
		}
	});
	
	
	Ext.getDom('divBusinessStatisticsSummaryInformation').parentNode.style.overflowY = 'auto';

});

function getDutyRange(){
	var dutyRangeForPrinter = {};
	dutyRangeForPrinter.onDutyFormat = business.paramsOnDuty;
	dutyRangeForPrinter.offDutyFormat = business.paramsOffDuty;
	return dutyRangeForPrinter;
}

function loadPaymentGeneral(c){
	onDuty = c.onDuty;
	offDuty = c.offDuty;
	dataSource = c.dataSource;
	queryPattern = c.queryPattern;
	businessStatic = c.businessStatic;
	staffId = c.staffId;
	Ext.getCmp('btnSearchForBusinessStatisticsSummaryInformation').handler();
}
