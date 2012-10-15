loadDiscount = function(){
//	Ext.Ajax.request({
//		url : '../../QueryDiscountTree.do',
//		params : {
//			pin : pin,
//			restaurantID : restaurantID
//		},
//		success : function(res, opt){
//			var jr = eval(res.responseText);
//			discountData = jr;
//			discountData.push({discountID:-1, text:'不打折'});
//			
//			Ext.Ajax.request({
//				url : '../../QueryDiscountPlan.do',
//				params : {
//					pin : pin,
//					restaurantID : restaurantID
//				},
//				success : function(res, opt){
//					discountPlanData = Ext.util.JSON.decode(res.responseText);
//					
//					var discount = Ext.getCmp('comboDiscount');
//					discount.store.loadData({root:discountData});
//					discount.setValue(-1);
//					for(var i = 0; i < discountData.length; i++){
//						if(eval(discountData[i].isDefault == true)){
//							discount.setValue(discountData[i].discountID);
//							break;
//						}
//					}
//					discount.fireEvent('select', discount);
//				},
//				failure : function(res, pot){
//					Ext.ux.showMsg({
//						code : 9999,
//						msg : '加载分厨折扣信息失败.'
//					});
//				}
//			});
//		},
//		failure : function(res, pot){
//			Ext.ux.showMsg({
//				code : 9999,
//				msg : '加载折扣方案信息失败.'
//			});
//		}
//	});
};

// on page load function
function checkOutOnLoad() {	
	
	getOperatorName(pin, "../../");
	
	loadDiscount();
	
	// 1,update table status
	restaurantID = Request["restaurantID"];

	var tableNum = Request["tableNbr"];
	var persCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNum + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = persCount + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("minCostDivTS").innerHTML = Request["minCost"] + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["minCost"] == "0") {
		document.getElementById("minCostDivTS").style["display"] = "none";
		document.getElementById("minCostImgTS").style["display"] = "none";
	}
	document.getElementById("serviceRateDivTS").innerHTML = (Request["serviceRate"] * 100) + "%" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["serviceRate"] == "0") {
		document.getElementById("serviceRateDivTS").style["display"] = "none";
		document.getElementById("serviceRateImgTS").style["display"] = "none";
	}

	document.getElementById("serviceCharge").value = Request["serviceRate"] * 100;

	// 2,get the ordered dishes and discount
	// checkOutData [ 厨房编号,"菜名", "口味", 数量, "实价",特,荐,停,送 ,口味价钱,单价,時,是否临时菜]
	// checkOutDataDisplay ["菜名", "口味", 数量, "单价" ,"折扣率","实价",特,荐,停,送,時,是否临时菜]
	// 后台已点菜式
	// ["菜名",菜名编号,厨房编号,"口味",口味编号,数量,单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,
	// 口味编号3,口味价钱,是否时价,是否临时菜]
	// discountData [厨房编号,一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 后台折扣率 [厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			pin : Request["pin"],
			restaurantID : restaurantID,
			tableID : Request["tableNbr"]
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				// 1,获取已点菜式
				checkOutData = resultJSON;
				
				// 2,获取折扣率
				Ext.Ajax.request({
					url : '../../QueryDiscountTree.do',
					params : {
						pin : pin,
						restaurantID : restaurantID
					},
					success : function(response, options) {
//						var resultJSON = Ext.util.JSON.decode(response.responseText);
//						if (resultJSON.success == true) {
						
						var jr = eval(response.responseText);
						discountData = jr;
						discountData.push({discountID:-1, text:'不打折'});
						
						Ext.Ajax.request({
							url : '../../QueryDiscountPlan.do',
							params : {
								pin : pin,
								restaurantID : restaurantID
							},
							success : function(res, opt){
//								discountPlanData = Ext.util.JSON.decode(res.responseText);
								var jr = Ext.util.JSON.decode(res.responseText);
								discountPlanData = {root:[]};
								for(var i = 0; i < jr.root.length; i++){
									if(jr.root[i].rate > 0 && jr.root[i].rate < 1){
										discountPlanData.root.push(jr.root[i]);
									}
								}
								
								var discount = Ext.getCmp('comboDiscount');
								discount.store.loadData({root:discountData});
								discount.setValue(-1);
								for(var i = 0; i < discountData.length; i++){
									if(eval(discountData[i].isDefault == true)){
										discount.setValue(discountData[i].discountID);
										break;
									}
								}
//								discount.fireEvent('select', discount);
							
								// 3,请求口味
								Ext.Ajax.request({
									url : "../../QueryMenu.do",
									params : {
										pin : Request["pin"],
										restaurantID : restaurantID,
										type : 2
									},
									success : function(response,options) {
										var resultTasteJSON = Ext.util.JSON.decode(response.responseText);
										if (resultTasteJSON.success == true) {
											dishTasteData = resultTasteJSON;									
											
											for(var i = 0; i < checkOutData.root.length; i++) {
												checkOutDataDisplay.root.push(checkOutData.root[i]);
											}
											
											for(var i = 0; i < checkOutDataDisplay.root.length; i++) {
												var tpItem = checkOutDataDisplay.root[i];
												
												if(tpItem.special == true || tpItem.gift == true){
													tpItem.discount = parseFloat(1).toFixed(2);
												}else{
	//												tpItem.discount = parseFloat(tpItem.kitchen.discount1).toFixed(2);
													tpItem.discount = parseFloat(1).toFixed(2);
													for(var di = 0; di < discountPlanData.root.length; di++){
														if(discount.getValue() != -1 && discountPlanData.root[di].discount.id == discount.getValue() 
																&& discountPlanData.root[di].kitchen.kitchenID == tpItem.kitchen.kitchenID){
															tpItem.discount = parseFloat(discountPlanData.root[di].rate).toFixed(2);
															break;
														}
													}
												}
												
												tpItem.totalPrice = parseFloat((tpItem.unitPrice + tpItem.tastePrice) * tpItem.discount * tpItem.count);
												
												checkOutDataDisplay.root[i] = tpItem;
											}
											
											checkOutStore.loadData(checkOutDataDisplay);
											
											discount.fireEvent('select', discount);
											
											// 4,算总价
											var totalCount = 0;
											var forFreeCount = 0;
											for ( var i = 0; i < checkOutDataDisplay.root.length; i++) {
												var tpItem = checkOutDataDisplay.root[i];
												var singleCount = parseFloat(tpItem.totalPrice);
												if (tpItem.gift == true) {
													forFreeCount = forFreeCount + parseFloat(tpItem.discount) * tpItem.totalPrice;
												} else {
													totalCount = totalCount + singleCount;
												}
											}
											
											totalCount = totalCount.toFixed(2);
											forFreeCount = forFreeCount.toFixed(2);
											originalTotalCount = totalCount;
											document.getElementById("totalCount").innerHTML = totalCount;
											document.getElementById("forFree").innerHTML = forFreeCount;
											document.getElementById("shouldPay").innerHTML = totalCount;
											// 4,（尾数处理）
											// 后台：["餐厅名称","餐厅信息","电话1","电话2","地址",$(尾数处理),$(自动补打)]
											// 前台：restaurantData，格式一样
											Ext.Ajax.request({
												url : "../../QueryRestaurant.do",
												params : {
													"restaurantID" : restaurantID
												},
												success : function(response, options) {
													var resultJSON = Ext.util.JSON.decode(response.responseText);
													if (resultJSON.success == true) {
														var dataInfo = resultJSON.data;
														var restaurantInfo = dataInfo.split(",");
														restaurantData.push([
															restaurantInfo[0].substr(1, restaurantInfo[0].length - 2),// 餐厅名称
															restaurantInfo[1].substr(1, restaurantInfo[1].length - 2),// 餐厅信息
															restaurantInfo[2].substr(1, restaurantInfo[2].length - 2),// 电话1
															restaurantInfo[3].substr(1, restaurantInfo[3].length - 2),// 电话2
															restaurantInfo[4].substr(1, restaurantInfo[4].length - 2),// 地址
															restaurantInfo[5],// 尾数处理
															restaurantInfo[6] // 自动补打
														]);
														var sPay = document.getElementById("shouldPay").innerHTML;
	
														// 5,最低消费处理
														var minCost = Request["minCost"];
														if (parseFloat(minCost) > parseFloat(sPay)) {
															sPay = minCost;
															Ext.MessageBox.show({
																msg : "消费额小于最低消费额，是否继续结帐？",
																width : 300,
																buttons : Ext.MessageBox.YESNO,
																fn : function(btn) {
																	if (btn == "no") {
																		location.href = "TableSelect.html?pin="
																						+ Request["pin"]
																						+ "&restaurantID="
																						+ restaurantID;
																	}
																}
															});
														}
	
														// 6,尾数处理
														if (restaurantData[0][5] == 1) {
															sPay = sPay.substr(0, sPay.indexOf(".")) + ".00";
														} else if (restaurantData[0][5] == 2) {
															sPay = parseFloat(sPay).toFixed(0) + ".00";
														}
														
														document.getElementById("shouldPay").innerHTML = sPay;
														document.getElementById("actualCount").value = sPay;
														document.getElementById("change").innerHTML = "0.00";
	
														moneyCount("");
	
													} else {
														var dataInfo = resultJSON.data;
														Ext.MessageBox.show({
															msg : dataInfo,
															width : 300,
															buttons : Ext.MessageBox.OK
														});
													}
												},
												failure : function(response,options) {
													
												}
											});
										} else {
											var dataTasteInfo = resultTasteJSON.data;
											Ext.MessageBox.show({
												msg : dataTasteInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										}
									},
									failure : function(response,options) { 
										
									}
								});
							},
							failure : function(res, pot){
								Ext.ux.showMsg({
									code : 9999,
									msg : '加载分厨折扣信息失败.'
								});
							}
						});
//						} else {
//							var dataInfo = resultJSON.data;
//							Ext.MessageBox.show({
//								msg : dataInfo,
//								width : 300,
//								buttons : Ext.MessageBox.OK
//							});
//						}
					},
					failure : function(response, options) {
						
					}
				});
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) { 
			
		}
	});

	Ext.Ajax.request({
		url : '../../QueryDetail.do',
		params : {
			'time' : new Date(),
			'limit' : 200,
			'start' : 0,
			'pin' : Request['pin'],
			'queryType' : 'TodayByTbl',
			'tableAlias' : Request['tableNbr'],
			'restaurantID' : Request['restaurantID']
		},
		success : function(response, options){
			createBackFoodDetail(Ext.util.JSON.decode(response.responseText));
		},
		failure : function(response, options){
			createBackFoodDetail(null);
		}
	});
};

function moneyCount(opt) {
//	var shouldPay = document.getElementById("shouldPay").innerHTML;
	var actualPay = document.getElementById("actualCount").value;
	var minCost = Request["minCost"];
	var serviceRate = document.getElementById("serviceCharge").value;
	var totalCount = document.getElementById("totalCount").innerHTML;

	var totalCount_out = "0.00";
	var shouldPay_out = "0.00";
	var change_out = "0.00";

	if (serviceRate < 0 || serviceRate > 100) {
		Ext.MessageBox.show({
			msg : "服务费率范围是0%至100%！",
			width : 300,
			buttons : Ext.MessageBox.OK
		});
	} else {

		if (restaurantData[0] != undefined) {
			// “应收”加上服务费
			if (parseFloat(totalCount) < parseFloat(minCost)) {
				shouldPay_out = parseFloat(minCost) * (1 + parseFloat(serviceRate) / 100);
			} else {
				shouldPay_out = parseFloat(originalTotalCount) * (1 + parseFloat(serviceRate) / 100);
			}

			// “应收”尾数处理
			if (restaurantData[0][5] == 1) {
				if ((shouldPay_out + "").indexOf(".") != -1) {
					shouldPay_out = (shouldPay_out + "").substr(0, (shouldPay_out + "").indexOf(".")) + ".00";
				} else {
					shouldPay_out = shouldPay_out + ".00";
				}
			} else if (restaurantData[0][5] == 2) {
				shouldPay_out = parseFloat(shouldPay_out).toFixed(0) + ".00";
			} else {
				shouldPay_out = parseFloat(shouldPay_out).toFixed(2);
			}

			// “合计”加上服务费
			totalCount_out = (parseFloat(originalTotalCount) * (1 + parseFloat(serviceRate) / 100)).toFixed(2);
			
			if(restaurantData[0][5] == 1){
				shouldPay_out = parseFloat(parseInt(totalCount_out)).toFixed(2);
			}else if(restaurantData[0][5] == 2){
				shouldPay_out = parseFloat(parseFloat(totalCount_out).toFixed(0)).toFixed(2);
			}else{
				shouldPay_out = parseFloat(totalCount_out).toFixed(2);
			}
			
			// “找零”计算
			if (actualPay != "" && actualPay != "0.00") {
				change_out = "0.00";
			}
//			alert('totalCount_out: '+totalCount_out+'   shouldPay_out: '+shouldPay_out+'    change_out: '+change_out);
			document.getElementById("totalCount").innerHTML = totalCount_out;
			document.getElementById("shouldPay").innerHTML = shouldPay_out;
			document.getElementById("change").innerHTML = change_out;
			document.getElementById("actualCount").value = shouldPay_out;
		}
	}
};

createBackFoodDetail = function(_data){
	
	if(_data == null || typeof _data == 'undefined' || typeof _data.root == 'undefined' || _data.root.length == 0){
		return;
	}
	
	backFoodDetailData = {totalProperty:0, root:[]};
	var backFoodPrice = 0.00, sumAmount = 0.00;
	
	var item = null;
	for(var i = 0; i < _data.root.length ; i++){		
		item = _data.root[i];
		if(typeof(item.amount) != 'undefined' && parseFloat(item.amount) < 0){
			item.amount = Math.abs(item.amount);
			item.backFoodPrice = Math.abs(parseFloat(item['unit_price'] * item.amount));			
			backFoodDetailData.root.push(item);
			backFoodPrice += parseFloat(item.backFoodPrice);
			sumAmount += parseFloat(item.amount);
		}
	}
	
	if(backFoodDetailData.root.length <= 0){
		return;
	}
	backFoodPrice = backFoodPrice.toFixed(2);
	sumAmount = sumAmount.toFixed(2);
	backFoodDetailData.root.push({
		food_name : '汇总',		
		amount : sumAmount,
		backFoodPrice : backFoodPrice
	});
	backFoodDetailData.totalProperty = backFoodDetailData.root.length;
	
	Ext.getDom('backFoodAmount_div').innerHTML = backFoodPrice + '&nbsp;<a href="#" onClick="showBackFoodDetail()">查看</a>';
	
};

var showBackFoodDetailWin = null;
showBackFoodDetail = function(){
		 
	if(!showBackFoodDetailWin){
		
		var grid = new Ext.grid.GridPanel({
			id : 'showBackFoodDetailWinGrid',
			border : false,
			width : 900,
			height : 300,
			stripeRows : true,
			animate : false,
			animCollapse : true,
			loadMask : { msg: '数据请求中，请稍后...' },
			cm : new Ext.grid.ColumnModel([
			    new Ext.grid.RowNumberer(),
			    {header:'名称', dataIndex:'food_name', width:180},
			    {header:'日期', dataIndex:'order_date', width:130},			    
			    {header:'单价', dataIndex:'unit_price', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
			    {header:'退菜数量', dataIndex:'amount', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
			    {header:'退菜金额', dataIndex:'backFoodPrice', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
			    {header:'厨房', dataIndex:'kitchen', width:100},
			    {header:'服务员', dataIndex:'waiter', width:70},
			    {header:'备注', dataIndex:'comment', width:150}
			]),
			ds : new Ext.data.Store({
				proxy : new Ext.data.MemoryProxy(backFoodDetailData),
				reader : new Ext.data.JsonReader({
					totalProperty : 'totalProperty',
					root : 'root'
				},
				[
				 	{name:'food_name'},
				 	{name:'order_date'},
				 	{name:'unit_price'},
				 	{name:'amount'},
				 	{name:'backFoodPrice'},
				 	{name:'kitchen'},
				 	{name:'waiter'},
				 	{name:'comment'}
				]
				)
			})
		});		
		
		showBackFoodDetailWin  = new Ext.Window({
			title : '退菜明细',
			resizable : false,
			modal : true,
			closable : false,
			constrainHeader : true,
			draggable : false,
			items : [grid],
			buttons : [
				{
					text : '退出',
					handler : function(){
						showBackFoodDetailWin.hide();
					}
				}
			]			
		});
		
		grid.getStore().on('load', function(store){
			var count = store.getCount();
			if(count > 0){
				var sumRow = grid.getView().getRow(count - 1);
				sumRow.style.backgroundColor = '#EEEEEE';
				sumRow.style.color = 'green';
				for(var i = 0; i < grid.getColumnModel().getColumnCount(); i++){
					if(i == 0 || 3 || 4){
						var item = grid.getView().getCell(count-1, i);
						item.style.fontSize = '15px';
						item.style.fontWeight = 'bold';
						item = null;
					}
				}
			}
		});
	}
	
	showBackFoodDetailWin.show();
	Ext.getCmp('showBackFoodDetailWinGrid').getStore().reload();
};


