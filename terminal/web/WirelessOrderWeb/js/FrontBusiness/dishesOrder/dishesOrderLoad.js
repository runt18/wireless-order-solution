//刪退菜處理函數
function dishOptTasteHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {
		var ds = orderedStore.getAt(rowIndex).data;
		if (ds.status == 1) {
			Ext.MessageBox.show({
				msg : '已点菜品不能修改口味',
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		} else if (ds.temporary == true) {
			Ext.MessageBox.show({
				msg : '临时菜不支持口味选择',
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		} else {
			if(orderedGrid.getSelectionModel().getSelections().length == 1){
				dishOrderCurrRowIndex_ = rowIndex;			
				choosenTasteWin.show();
				choosenTasteWin.center(); 				
			}else{
				Ext.example.msg('提示', '请选中一条数据再进行操作!');
			}
		}
	}
};

function dishGridRefresh() {	
	if (typeof(orderedData.root) != 'undefined' && orderedData.root.length > 0) {		
		// 底色处理，已点菜式原色底色
		for ( var i = 0; i < orderedData.root.length; i++) {
			if (orderedData.root[i].status == 1) {
				orderedGrid.getView().getRow(i).style.backgroundColor = '#FFFF93';
			} else if (orderedData.root[i].status == 2) {
				orderedGrid.getView().getRow(i).style.backgroundColor = '#FFE4CA';
			} else {

			}
		}

		// 底色处理，已点菜式原色底色
		for ( var i = 0; i < orderedData.root.length; i++) {
			if (orderedData.root[i].status == 1) {
				document.getElementById('tasteLink' + i).onclick = function() {
					Ext.MessageBox.show({
						msg : '已点菜品不能修改口味',
						width : 300,
						buttons : Ext.MessageBox.OK
					});
					return false;
				};
			} else if (orderedData.root[i].status == 2) {
				
			} else {

			}
		}
	}
};

function tableStuLoad() {
	
	var Request = new URLParaQuery();
	if(Request['category'] == CATE_TAKE_OUT){
		orderedForm.buttons[1].setDisabled(true);
	}
	
	// update table status
	var Request = new URLParaQuery();

	// 对'拼台''外卖'，台号特殊处理
	var tableNbr = '000';
	if (category == '4') {
		dishesOrderNorthPanel.findById('tableNbrFrom').setWidth(140);
		tableNbr = Request['tableNbr'] + '，' + Request['tableNbr2'];
	} else if (category == '2' && Request['tableStat'] == 'free') {
		tableNbr = '外卖';
	} else {
		tableNbr = Request['tableNbr'];
	}
	
	var personCount = Request['personCount'];
	personCount = personCount == '' || parseInt(personCount) == 0 ? 1 : personCount;
	document.getElementById('tblNbrDivTS').innerHTML = tableNbr;
	
	dishesOrderNorthPanel.findById('tablePersonCount').setValue(personCount);
	document.getElementById('minCostDivTS').innerHTML = Request['minCost'];
	
	if (Request['minCost'] == '0') {
		document.getElementById('minCostDivTS').style['display'] = 'none';
		document.getElementById('minCostImgTS').style['display'] = 'none';
	}
	document.getElementById('serviceRateDivTS').innerHTML = (Request['serviceRate'] * 100) + '%';
	if (Request['serviceRate'] == '0') {
		document.getElementById('serviceRateDivTS').style['display'] = 'none';
		document.getElementById('serviceRateImgTS').style['display'] = 'none';
	}
	
	var status = Request['tableStat'];
	
	if (status == 'free') {
		centerPanel.setTitle('新下单');
	} else {
		centerPanel.setTitle('改单');
	}
	
	// update the operator name
	getOperatorName(Request['pin'], '../../');
};

// 以点菜式数据
// 菜品状态: 1：已点，2：新点，3：修改
// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3,￥口味价钱,菜品状态,時,是否临时菜,菜名ORIG]
// orderedData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
// orderedData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,
// 口味编号3,口味价钱,是否时价,是否临时菜]
function orderedDishesOnLoad() {
	var Request = new URLParaQuery();
	// 外卖不查询已点菜式
	if (Request['tableStat'] == 'free') {
//	if (category == '2' && Request['tableStat'] == 'free') {
		
	} else {
		
		// “并台”特殊处理，如果并台+新下单，清空已点菜式
//		if (Request['category'] == '3' && Request['tableStat'] == 'free') {
//			orderedData.root.length = 0;
//			return;
//		}
		
		Ext.Ajax.request({
			url : '../../QueryOrder.do',
			params : {
				'pin' : Request['pin'],
				'tableID' : Request['tableNbr']
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				if (resultJSON.success == true) {
					
					orderedData = resultJSON;
					
					// 更新菜品状态为已点菜
					for(var i = 0; i < orderedData.root.length; i++){
						orderedData.root[i].status = 1;
					}
					
					orderedStore.loadData(orderedData);
					
					dishGridRefresh();
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
	}

	if (Request['tableStat'] == 'free') {
		// orderedGrid.getTopToolbar().addSeparator();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addItem(countAddImgBut);
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addItem(countMinusImgBut);
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addItem(countEqualImgBut);
	} else {
		orderedGrid.getTopToolbar().addSeparator();
		orderedGrid.getTopToolbar().addItem(new Ext.Toolbar.TextItem({xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}));
		orderedGrid.getTopToolbar().addItem(printTotalImgBut);
		orderedGrid.getTopToolbar().addItem(new Ext.Toolbar.TextItem({xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}));
		orderedGrid.getTopToolbar().addItem(printDetailImgBut);
	}
};

// loading taste 
function tasteOnLoad() {
	Ext.Ajax.request({
		url : '../../QueryMenu.do',
		params : {
			pin : pin,
			restaurantID : restaurantID,
			type : 2
		},
		success : function(response, options) {
			var rj = Ext.util.JSON.decode(response.responseText);
			if (rj.success == true) {
				tasteMenuData.root = [];
				allTasteData.root = [];
				ggTasteData.root = [];
				
				for(var i = 0; i < rj.root.length; i++){
					tasteMenuData.root.push(rj.root[i]);
					if(rj.root[i].tasteCategory == 0){
						allTasteData.root.push(rj.root[i]);
					}else if(rj.root[i].tasteCategory == 2){
						ggTasteData.root.push(rj.root[i]);
					}
				}
				allTasteGridForTabPanel.getStore().loadData(allTasteData);
				ggForTabPanel.getStore().loadData(ggTasteData);
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.util.JSON.decode(response.responseText));
		}
	});
};