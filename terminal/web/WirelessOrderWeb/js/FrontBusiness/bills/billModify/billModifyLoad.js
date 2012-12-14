// keyboard select handler
var dishKeyboardSelect = function(relateItemId) {
	if (relateItemId == "orderNbr") {
		var curDishNbr = Ext.getCmp("orderNbr").getValue() + "";

		if (curDishNbr == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3], dishesDisplayData[i][4],
						dishesDisplayData[i][5], dishesDisplayData[i][6],
						dishesDisplayData[i][7], dishesDisplayData[i][8],
						dishesDisplayData[i][9] ]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][1] + "").substring(0,
						curDishNbr.length) == curDishNbr) {
					dishesDisplayDataShow.push([ dishesDisplayData[i][0],
							dishesDisplayData[i][1], dishesDisplayData[i][2],
							dishesDisplayData[i][3], dishesDisplayData[i][4],
							dishesDisplayData[i][5], dishesDisplayData[i][6],
							dishesDisplayData[i][7], dishesDisplayData[i][8],
							dishesDisplayData[i][9] ]);
				}
			}
		}

		dishesDisplayStore.reload();
	}
};

function dishSpellOnLoad() {
	// keyboard input dish spell
	$("#orderSpell").bind("keyup", function() {
		var curDishSpell = Ext.getCmp("orderSpell").getValue().toUpperCase() + "";
		if (curDishSpell == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([
					dishesDisplayData[i][0],
					dishesDisplayData[i][1],
					dishesDisplayData[i][2],
					dishesDisplayData[i][3],
					dishesDisplayData[i][4],
					dishesDisplayData[i][5],
					dishesDisplayData[i][6],
					dishesDisplayData[i][7],
					dishesDisplayData[i][8],
					dishesDisplayData[i][9] 
				]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][2] + "").substring(0, curDishSpell.length).toUpperCase() == curDishSpell) {
					dishesDisplayDataShow.push([
						dishesDisplayData[i][0],
						dishesDisplayData[i][1],
						dishesDisplayData[i][2],
						dishesDisplayData[i][3],
						dishesDisplayData[i][4],
						dishesDisplayData[i][5],
						dishesDisplayData[i][6],
						dishesDisplayData[i][7],
						dishesDisplayData[i][8],
						dishesDisplayData[i][9] 
					]);
				}
			}
		}
		dishesDisplayStore.reload();
	});
}

// on page load function
function billModifyOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	// keyboard input dish number
	$("#orderNbr").bind("keyup", function() {
		dishKeyboardSelect("orderNbr");
	});

	// update table status
	// 对"拼台""外卖"，台号特殊处理
	var tableNbr = "000";
	if (category == "拼台") {
		tableNbr = Request["tableNbr"] + "，" + Request["tableNbr2"];
	} else if (category == "外卖" /* && Request["tableStat"] == "free" */) {
		tableNbr = "外卖";
	} else {
		tableNbr = Request["tableNbr"];
	}

	var personCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNbr + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = personCount + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("minCostDivTS").innerHTML = Request["minCost"] + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["minCost"] == "0") {
		document.getElementById("minCostDivTS").style["display"] = "none";
		document.getElementById("minCostImgTS").style["display"] = "none";
	}
	document.getElementById("serviceRateDivTS").innerHTML = Request["serviceRate"] + "%" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["serviceRate"] == "0") {
		document.getElementById("serviceRateDivTS").style["display"] = "none";
		document.getElementById("serviceRateImgTS").style["display"] = "none";
	}
	
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
	
	// 已点菜式查询
	// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,折扣率,￥口味价钱,口味编号2,口味编号3,時,是否临时菜,菜名ORIG]
	// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,￥单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,口味编号3,￥口味价钱,時,是否临时菜]
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
//				alert(Ext.util.JSON.encode(resultJSON.other.order));
				Ext.getCmp('serviceRate').setValue(orderBasicMsg.serviceRate * 100);
				Ext.getCmp('numErasePrice').setValue(orderBasicMsg.erasePuotaPrice);
				billGenModForm.getForm().findField('payManner').setValue(orderBasicMsg.payManner);
				
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
//				Ext.MessageBox.show({
//					msg : resultJSON.msg,
//					width : 300,
//					buttons : Ext.MessageBox.OK
//				});
				Ext.ux.showMsg(resultJSON);
			}
		},
		failure : function(response, options) {
			
		}
	});
	
	
};
