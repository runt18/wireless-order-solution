


function foodAmountOperateRenderer(v, c, r){
	return Ext.ux.txtFormat.gridDou(v)
		+ '<a href="javascript:foodAmountOperateHandler({otype:0,count:1,grid:orderedGrid});"><img src="../../images/btnAdd.gif" border="0" title="菜品数量+1"/></a>&nbsp;'
		+ '<a href="javascript:foodAmountOperateHandler({otype:0,count:-1,grid:orderedGrid});"><img src="../../images/btnDelete.png" border="0" title="菜品数量-1"/></a>&nbsp;'
		+ '<a onClick="foodAmountSetHandler({x:event.clientX,y:event.clientY})"><img src="../../images/icon_tb_setting.png" border="0" title="菜品数量设置"/></a>&nbsp;'
		+ '<a href="javascript:foodAmountDeleteHandler()"><img src="../../images/btnCancel.png" border="0" title="删除菜品"/></a>';
}

var orderedGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		text : '数量+1',
		iconCls : 'btn_add',
		handler : function(){
			foodAmountOperateHandler({
				otype : 0,
				count : 1
			});
		}
	}, '-', {
		text : '数量-1',
		iconCls : 'btn_delete',
		handler : function(){
			foodAmountOperateHandler({
				otype : 0,
				count : -1
			});
		}
	}, '-', {
		text : '数量设置',
		id : 'btnOperationFoodCount',
		iconCls : 'icon_tb_setting',
		handler : function(e){
			foodAmountSetHandler({
				x : e.getEl().getX(),
				y : (e.getEl().getY() + e.getEl().getHeight())
			});
		}
	}, '-', {
		text : '删除菜品',
		iconCls : 'btn_cancel',
		handler : function(){
			foodAmountDeleteHandler();
		}
	}]
});

var orderedGrid = createGridPanel(
	'orderSingleGridPanel',
	'已点菜列表',
	'',
	'',
	'',
	[
	    [true, false, false, false],
	    ['菜名', 'displayFoodName'], 
		['口味', 'tasteGroup.tastePref'] , 
		['数量', 'count', , 'right', 'foodAmountOperateRenderer'],
		['单价', 'unitPrice', , 'right', 'Ext.ux.txtFormat.gridDou'],
		['折扣率', 'discount', , 'right', 'Ext.ux.txtFormat.gridDou'],
		['下单时间', 'orderDateFormat'],
		['服务员', 'waiter']
	],
	OrderFoodRecord.getKeys(),
	[],
	0,
	'',
	orderedGridTbar
);
orderedGrid.region = 'center';
orderedGrid.buttonAlign = 'center';
orderedGrid.buttons = [new Ext.Button({
	text : '提交',
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	},
	handler : function() {
		submitOrderHandler({grid:orderedGrid});
	}
}), new Ext.Button({
	text : "返回",
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	},
	handler : function() {
		if (orderIsChanged == false) {
			location.href = "Bills.html?pin=" + pin + "&restaurantID=" + restaurantID;
		} else {
			Ext.MessageBox.show({
				msg : "账单修改还未提交，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "Bills.html?pin=" + pin + "&restaurantID=" + restaurantID;
					}
				}
			});
		}
	}
})];
orderedGrid.getStore().on('load', function(thiz, rs){
	for(var i = 0; i < rs.length; i++){
		Ext.ux.formatFoodName(rs[i], 'displayFoodName', 'name');				
	}
});

function billModifyOnLoad() {
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
				Ext.getDom('fontShowEraseQuota').innerHTML = eraseQuota.toFixed(2);
				Ext.getCmp('numErasePrice').setDisabled(false);
			}else{
				Ext.getDom('fontShowEraseQuota').innerHTML = 0.00;
				Ext.getCmp('numErasePrice').setDisabled(true);
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
			orderID : orderID,
			queryType: 'Today'
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				orderedGrid.order = resultJSON.other.order;
				orderedGrid.order.orderFoods = resultJSON.root;
				orderedGrid.getStore().loadData(resultJSON);
				
				// 加载账单基础信息
				Ext.getCmp('txtSettleTypeFormat').setValue(orderedGrid.order.settleTypeText);
				Ext.getCmp('serviceRate').setValue(orderedGrid.order.serviceRate * 100);
				Ext.getCmp('numErasePrice').setValue(orderedGrid.order.erasePrice);
				var payManner = document.getElementsByName('radioPayType');
				for(var i = 0; i < payManner.length; i++){
					if(payManner[i].value == orderedGrid.order.payTypeValue){
						payManner[i].checked = true;
						break;
					}
				}
				
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
								
								discount.setValue(orderedGrid.order.discount.id);
								
//								billListRefresh();
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
