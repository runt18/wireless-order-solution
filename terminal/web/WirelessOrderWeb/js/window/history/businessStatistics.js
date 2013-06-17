Ext.onReady(function(){
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
	
	var pelement = Ext.query('#businessStatisticsDIV')[0].parentElement;
	var mw = parseInt(pelement.style.width);
	var mh = parseInt(pelement.style.height);
	
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
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
		readOnly : true,
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
			frame : true,
			contentEl : 'businessStatisticsSummaryInformation'
		}, {
			region : 'center',
			frame : true,
			autoScroll : true,
			id : 'businessStatisticsSummaryInformationCenterPanel',
			listeners : {
				render : function(thiz){
					var empty = String.format(trModel, '---', '---', '---', '---');
					var table = String.format('<table border="1" class="tb_base">{0}{1}</table>', title, empty);
					thiz.body.update(table);
				}
			}
		}],
		tbar : new Ext.Toolbar({
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
					var paramsOnDuty = '', paramsOffDuty;
					
					if(queryPattern == 1){
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
					
					bssifLoadMarsk.show();
					Ext.Ajax.request({
						url : "../../BusinessStatistics.do",
						params : {
							dataSource : dataSource,
							queryPattern : queryPattern,
							pin : pin,
							restaurantID : restaurantID,
							onDuty : paramsOnDuty,
							offDuty : paramsOffDuty
						},
						success : function(response, options) {
							var jr = Ext.decode(response.responseText);
							if(jr.success){
								var business = jr.other.business;
								var deptStat = business.deptStat;
								
								var trContent = '';
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
								var table = String.format('<table border="1" class="tb_base">{0}{1}</table>', title, trContent);
								Ext.getCmp('businessStatisticsSummaryInformationCenterPanel').body.update(table);
								
								Ext.getDom('bssiOnDuty').innerHTML = business.onDutyToSimple;
								Ext.getDom('bssiOffDuty').innerHTML = business.offDutyToSimple;
								Ext.getDom('bssiOrderAmount').innerHTML = business.orderAmount;
								//
								Ext.getDom('bssiCashAmount').innerHTML = business.cashAmount;
								Ext.getDom('bssiCashIncome').innerHTML = business.cashIncome.toFixed(2);
								Ext.getDom('bssiCashIncome2').innerHTML = business.cashIncome2.toFixed(2);
								
								Ext.getDom('bssiCreditCardAmount').innerHTML = business.creditCardAmount;
								Ext.getDom('bssiCreditCardIncome').innerHTML = business.creditCardIncome.toFixed(2);
								Ext.getDom('bssiCreditCardIncome2').innerHTML = business.creditCardIncome2.toFixed(2);
								
								Ext.getDom('bssiMemeberCardAmount').innerHTML = business.memberCardAmount;
								Ext.getDom('bssiMemeberCardIncome').innerHTML = business.memberCardIncome.toFixed(2);
								Ext.getDom('bssiMemeberCardIncome2').innerHTML = business.memberCardIncome2.toFixed(2);
								
								Ext.getDom('bssiSignAmount').innerHTML = business.signAmount;
								Ext.getDom('bssiSignIncome').innerHTML = business.signIncome.toFixed(2);
								Ext.getDom('bssiSignIncome2').innerHTML = business.signIncome2.toFixed(2);
								
								Ext.getDom('bssiHangAmount').innerHTML = business.hangAmount;
								Ext.getDom('bssiHangIncome').innerHTML = business.hangIncome.toFixed(2);
								Ext.getDom('bssiHangIncome2').innerHTML = business.hangIncome2.toFixed(2);
								//
								Ext.getDom('bssiSumAmount').innerHTML = business.orderAmount;
								Ext.getDom('bssiSumIncome').innerHTML = parseFloat(business.cashIncome
																		+ business.creditCardIncome
																		+ business.memberCardIncome
																		+ business.signIncome
																		+ business.hangIncome).toFixed(2);
								Ext.getDom('bssiSumIncome2').innerHTML = parseFloat(business.cashIncome2
																		+ business.creditCardIncome2
																		+ business.memberCardIncome2
																		+ business.signIncome2
																		+ business.hangIncome2).toFixed(2);
								
								Ext.getDom('bssiEraseAmount').innerHTML = business.eraseAmount;
								Ext.getDom('bssiEraseIncome').innerHTML = business.eraseIncome.toFixed(2);
								
								Ext.getDom('bssiDiscountAmount').innerHTML = business.discountAmount;
								Ext.getDom('bssiDiscountIncome').innerHTML = business.discountIncome.toFixed(2);
								
								Ext.getDom('bssiGiftAmount').innerHTML = business.giftAmount;
								Ext.getDom('bssiGiftIncome').innerHTML = business.giftIncome.toFixed(2);
								
								Ext.getDom('bssiCancelAmount').innerHTML = business.cancelAmount;
								Ext.getDom('bssiCancelIncome').innerHTML = business.cancelIncome.toFixed(2);
								
								Ext.getDom('bssiPaidAmount').innerHTML = business.paidAmount;
								Ext.getDom('bssiPaidIncome').innerHTML = business.paidIncome.toFixed(2);
								
								Ext.getDom('bssiServiceIncome').innerHTML = business.serviceIncome.toFixed(2);
								
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
//				hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var paramsOnDuty = '', paramsOffDuty;
					if(queryPattern == 1){
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
					var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&queryPattern={6}&dataType={7}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							pin, 
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
				}else if(queryPattern == 2){
					beginDate.setValue(onDuty);
					endDate.setValue(offDuty);
					Ext.getCmp('btnSearchForBusinessStatisticsSummaryInformation').handler();
					
					dateCombo.setDisabled(true);
					beginDate.setDisabled(true);
					endDate.setDisabled(true);
				}
			}
		}
	});

});
