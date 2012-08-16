var Request = new URLParaQuery();

// on page load function
function checkOutOnLoad() {

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
	document.getElementById("serviceRateDivTS").innerHTML = (Request["serviceRate"] * 100)
			+ "%";
	+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
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
			"pin" : Request["pin"],
			"tableID" : Request["tableNbr"]
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				// 1,获取已点菜式
//				var josnData = resultJSON.data;
//				var orderList = josnData.split("，");
//				for ( var i = 0; i < orderList.length; i++) {
//					var orderInfo = orderList[i].substr(1, orderList[i].length - 2).split(",");
//					// 实价 = 单价 + 口味价钱
//					var singlePrice = parseFloat(orderInfo[6].substr(2, orderInfo[6].length - 3));
//					var tastePrice = parseFloat(orderInfo[14].substr(2, orderInfo[14].length - 3));
//					var acturalPrice = 0.0;
//					acturalPrice = singlePrice + tastePrice;
//					acturalPrice = "￥" + acturalPrice.toFixed(2);
//					checkOutData.push([ 
//					    orderInfo[2],// 厨房编号
//					    orderInfo[0].substr(1, orderInfo[0].length - 2), // 菜名
//					    orderInfo[3].substr(1, orderInfo[3].length - 2),// 口味
//						orderInfo[5],// 数量
//						acturalPrice, // 实价
//						orderInfo[19],// 时间
//						orderInfo[20],// 服务员 
//						orderInfo[7],// 特
//						orderInfo[8],// 荐							
//						orderInfo[9], // 停
//						orderInfo[10], // 赠
//						orderInfo[14].substr(2, orderInfo[14].length - 3),// 口味价钱
//						orderInfo[6].substr(2, orderInfo[6].length - 3), // 单价
//						orderInfo[15], // 時
//						orderInfo[16] // 臨
//					]);
//				}
				
				checkOutData = resultJSON;
				
				// 2,获取折扣率
				Ext.Ajax.request({
					url : "../../QueryMenu.do",
					params : {
						"pin" : Request["pin"],
						"type" : "3"
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						if (resultJSON.success == true) {
							var discountJSONData = resultJSON.data;
							var discountList = discountJSONData.split("，");
							for ( var i = 0; i < discountList.length; i++) {
								var discountInfo = discountList[i].substr(1,discountList[i].length - 2).split(",");
								discountData.push([
									discountInfo[0], // 厨房编号
									discountInfo[3],// 一般折扣1
									discountInfo[4],// 一般折扣2
									discountInfo[5],// 一般折扣3
									discountInfo[6],// 会员折扣1
									discountInfo[7],// 会员折扣2
									discountInfo[8], // 会员折扣3
									discountInfo[1] // 厨房id
								]);
							}
							
							// 3,请求口味
							Ext.Ajax.request({
								url : "../../QueryMenu.do",
								params : {
									"pin" : Request["pin"],
									"type" : "2"
								},
								success : function(response,options) {
									var resultTasteJSON = Ext.util.JSON.decode(response.responseText);
									if (resultTasteJSON.success == true) {
										if (resultTasteJSON.data != "") {
											var josnTasteData = resultTasteJSON.data;
											var tasteList = josnTasteData.split("，");

											for ( var i = 0; i < tasteList.length; i++) {
												var tasteInfo = tasteList[i].substr(1,tasteList[i].length - 2).split(",");
												// 后台格式：[1,"加辣","￥2.50"]，[2,"少盐","￥0.00"]，[3,"少辣","￥5.00"]
												// 前后台格式有差异，口味编号前台存储放在最后一位
												dishTasteData.push([
													tasteInfo[1].substr(1,tasteInfo[1].length - 2), // 口味
													tasteInfo[2].substr(1,tasteInfo[2].length - 2), // 价钱
													tasteInfo[0] // 口味编号
												]);
											}
										}
//										for(k in checkOutData){
//											alert(k+'    :   '+checkOutData[k]);
//										}
										// 4,显示
										for ( var i = 0; i < checkOutData.root.length; i++) {
											var tpItem = checkOutData.root[i];
											var KitchenNum = tpItem.kitchenId;
											var discountRate = 1;
											for ( var j = 0; j < discountData.length; j++) {
												if (KitchenNum == discountData[j][0]) {
													// 默认“一般”的“折扣1”
													discountRate = discountData[j][1];													
												}
											}
											
											// 特价，送 --
											// 折扣率
											// --1
											if (tpItem.special == true || tpItem.gift == true) {
												tpItem.discount = parseFloat("1").toFixed(2);
											} else {
												tpItem.discount = parseFloat(discountRate).toFixed(2);
											}
											
											if(tpItem.special == true || tpItem.gift == true){
												// 特价和赠送菜品不打折
												tpItem.totalPrice = parseFloat(tpItem.unitPrice * tpItem.count);
											}else{
												tpItem.totalPrice = parseFloat((tpItem.unitPrice * tpItem.discount + tpItem.tastePrice) * tpItem.count);
											}											
											
											checkOutDataDisplay.root.push(tpItem);
										}
										
										// 根据“特荐停”重新写菜名
										for ( var i = 0; i < checkOutDataDisplay.root.length; i++) {
											var tpItem = checkOutDataDisplay.root[i];
											if (tpItem.special == true) {
												// 特
												tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_te.png'></img>";
											}
											if (tpItem.recommed == true) {
												// 荐
												tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_jian.png'></img>";
											}
											if (tpItem.soldout == true) {
												// 停
												tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_ting.png'></img>";
											}
											if (tpItem.gift == true) {
												// 赠
												tpItem.foodName = tpItem.foodName + "<img src='../../images/forFree.png'></img>";
											}
											if (tpItem.currPrice == true) {
												// 時
												tpItem.foodName = tpItem.foodName + "<img src='../../images/currPrice.png'></img>";
											}
											if (tpItem.temporary == true) {
												// 臨
												tpItem.foodName = tpItem.foodName + "<img src='../../images/tempDish.png'></img>";
											}
										}

										checkOutStore.reload();
										
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
											failure : function(response,options) {  }
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
								failure : function(response,options) { }
							});
						} else {
							var dataInfo = resultJSON.data;
							Ext.MessageBox.show({
								msg : dataInfo,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					},
					failure : function(response, options) { }
				});
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) { }
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
//			alert(response.responseText)
			createBackFoodDetail(Ext.util.JSON.decode(response.responseText));
		},
		failure : function(response, options){
			createBackFoodDetail(null);
		}
	});
};

function moneyCount(opt) {
	var shouldPay = document.getElementById("shouldPay").innerHTML;
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

			// “找零”计算
			if (actualPay != "" && actualPay != "0.00") {
				if (opt == "button") {
					change_out = (parseFloat(actualPay) - parseFloat(shouldPay_out)).toFixed(2);
				} else {
					change_out = "0.00";
				}
			}
//			alert('totalCount_out: '+totalCount_out+'   shouldPay_out: '+shouldPay_out+'change_out: '+change_out);
			document.getElementById("totalCount").innerHTML = totalCount_out;
			document.getElementById("shouldPay").innerHTML = shouldPay_out;
			document.getElementById("change").innerHTML = change_out;
			if (opt == "button") {
			} else {
				document.getElementById("actualCount").value = shouldPay_out;
			}
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


