function billModifyOnLoad() {
	getOperatorName(pin, "../../");
	// 加载餐厅配置概要信息
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		params : {
			"restaurantID" : restaurantID
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			sysSetting = jr.other.systemSetting;
			var eraseQuota = parseInt(sysSetting.setting.eraseQuota);
			if(eraseQuota > 0){
//				Ext.getDom('fontShowEraseQuota').innerHTML = eraseQuota.toFixed(2);
//				Ext.getCmp('numErasePrice').setDisabled(false);
			}else{
//				Ext.getDom('fontShowEraseQuota').innerHTML = 0.00;
//				Ext.getCmp('numErasePrice').setDisabled(true);
			}
		},
		failure : function(res, opt) { 
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			orderID : Request["orderID"],
			queryType: 'Today'
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				orderedData = resultJSON;
				orderedStore.loadData(orderedData);
				// 加载账单基础信息
				orderBasicMsg = resultJSON.other.order;
//				Ext.getCmp('serviceRate').setValue(orderBasicMsg.serviceRate * 100);
//				Ext.getCmp('numErasePrice').setValue(orderBasicMsg.erasePuotaPrice);
//				billGenModForm.getForm().findField('payManner').setValue(orderBasicMsg.payManner);
				
				Ext.Ajax.request({
					url : '../../QueryDiscountTree.do',
					params : {
						pin : pin,
						restaurantID : restaurantID
					},
					success : function(res, opt) {
						discountData = eval(res.responseText);
						
						Ext.Ajax.request({
							url : '../../QueryDiscountPlan.do',
							params : {
								pin : pin,
								restaurantID : restaurantID
							},
							success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								discountPlanData = {root:[]};
								for(var i = 0; i < jr.root.length; i++){
									if(jr.root[i].rate > 0 && jr.root[i].rate < 1){
										discountPlanData.root.push(jr.root[i]);
									}
								}
								var discount = Ext.getCmp('comboDiscount');
								discount.store.loadData({root:discountData});
								
								discount.setValue(orderBasicMsg.discountID);
								
								billListRefresh();
							},
							failure : function(res, opt) {
								Ext.MessageBox.show({
									title : '警告',
									msg : '加载折扣方案信息失败.',
									width : 300
								});
							}
						});
					},
					failure : function(res, opt) {
						Ext.MessageBox.show({
							title : '警告',
							msg : '加载折扣方案信息失败.',
							width : 300
						});
					}
				});
			} else {
				Ext.ux.showMsg(resultJSON);
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
};
