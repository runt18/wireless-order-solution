//点菜界面数据对象
var of = {
	table : {},
	order : {},
	wxCode : 0,			//微信账单号
	initFoods : [],		//进入点菜界面时的菜品数据
	newFood : null,		//新点菜的菜品数据
	commit : function(selectedFoods){},     //通用的一个回调方法  
	selectedOrderFood : {},
	//从哪个功能进入点菜
	orderFoodOperateType : 'normal'
};
	
/**
 * 入口, 加载点菜页面数据
 */
of.entry = function(c){
	
	c = c || {
		orderFoodOperateType : null,			//操作类型
		table : null,							//餐台
		comment : null,							//开台备注
		initFoods : null,						//初始菜品
		wxCode : null,							//微信账单号							
		commit : function(selectedFoods){}      //通用的commit
	};
	
	//设置入座的回调函数
	of.commit = c.commit;
	//设置点菜界面操作类型
	of.orderFoodOperateType = c.orderFoodOperateType;

	var param = null;
	if(c.table){
		if(c.table.id){
			param = '?table_id=' + c.table.id;
		}else if(c.table.alias){
			param = '?table_alias=' + c.table.alias;
		}
		
		if(c.comment){
			param += '&comment=' + c.comment;	
		}
	}
	
	//初始化菜品
	if(c.initFoods){
		of.initFoods = c.initFoods.slice(0);
	}else{
		of.initFoods = [];
	}
	
	//微信账单号
	if(c.wxCode){
		of.wxCode = c.wxCode;
	}else{
		of.wxCode = null;
	}
	
	//去点餐界面
	location.href = '#orderFoodMgr' + (param || '');

};
	
/**
 * 设置当前输入框
 * @param id
 */
function setInput(id, callback){
	focusInput = id;
	if(callback){
		callback();
	}
}	

/**
 * 回删单个字
 * @param id
 */
function deleteSingleWord(id){
	var string = $('#'+id);
	string.val(string.val().substring(0, string.val().length - 1));
	string.focus();
}

$(function(){
	//初始化新点菜区域
	function initNewFoodContent(){
		//已点菜列表					  
		var orderFoodCmpTemplet = '	<li data-icon={isGift} data-index={dataIndex} data-unique={unique} data-theme={dataTheme} class={dataClass} data-value={id} data-type="orderFoodCmp" ><a >'+
										'<h1 style="font-size:20px;">{name}</h1>' +
										'<div class="{hasComboFood}"><ul data-role="listview" data-inset="false" class="div4comboFoodList">{comboFoodList}</ul></div><br>' +
										'<span style="color:green;">{tasteDisplay}</span>' +
										'<div>' +
											'<span style="float: left;color: red;">{foodStatus}</span>' +
											'<span style="float: right;color: blue;margin-left:5px;">{multiPriceUnit}</span>' +
											'<span style="float: right;">￥{unitPrice}  X <font color="lime">{count}</font></span>' +
										'</div>' +
									'</a></li>';
									
		var html = [], sumCount = 0, sumPrice = 0;
		
		of.newFood.forEach(function(e, index){
			sumCount += e.count;
			sumPrice += e.getPrice();
			
			var foodStatus = [];
			if(e.isHang()){
				foodStatus.push('叫起');
			}
			
			if(e.isTemp()){
				foodStatus.push('临时菜');
			}
			
			var comboFoodLi = [];
			
			//是否为套菜
			if(!e.isTemp() && e.isCombo()){
				//套菜列表							
				var comboFoodLiTemplate = '<li class="ui-li ui-li-static">┕{name}<font color="blue">{unit}</font> X <font color="lime">{amount}</font><font color="green"> {tastes}</font></li>';
				for (var j = 0; j < e.combo.length; j++) {
					//列出套菜对应的子菜品
					comboFoodLi.push(comboFoodLiTemplate.format({
						name : e.combo[j].comboFood.name,
						//有单位时使用单位名
						unit : e.combo[j].foodUnit ? ' /' + e.combo[j].foodUnit.unit : '',
						amount : e.combo[j].comboFood.amount,
						tastes : e.combo[j].hasTasteGroup() && e.combo[j].getTastePref() != '' ? ('—' + e.combo[j].getTastePref()) : ""
					}));
				}
				
			}
			
			var orderFoodHtmlData = {
				dataIndex : index,
				dataTheme : e.selected ? 'e' : 'c',
				dataClass : e.selected ? 'ui-btn-up-e' : 'ui-btn-up-c',
				unique : e.unique,
				id : e.id,
				name : e.name,
				count : e.count,
				unitPrice : e.getPrice().toFixed(2),
				totalPrice : e.getPrice().toFixed(2),
				foodStatus : foodStatus.join('，'),
				isGift : typeof e.isGift == 'boolean' && e.isGift ? 'forFree' : 'false',
				multiPriceUnit : e.foodUnit ? "/" + e.foodUnit.unit : '',
				hasComboFood : comboFoodLi.length == 0 ? 'none' : '',
				comboFoodList : comboFoodLi.join("")
			};
			
			if(e.hasTasteGroup()){
				orderFoodHtmlData.tasteDisplay = e.getTasteGroup().getPref();
			}else{
				orderFoodHtmlData.tasteDisplay = '';
			}
			
			html.push(orderFoodCmpTemplet.format(orderFoodHtmlData));
		});
		
		//刷新新点菜列表
		$('#orderFoodsCmp').html(html.join('')).listview('refresh');
		//每个新点菜的click事件
		$('#orderFoodsCmp').find('[data-type="orderFoodCmp"]').each(function(index, element){
			element.onclick = function(){
				//高亮显示选中的菜品
				of.newFood.select($(element).attr('data-index'));
				initNewFoodContent();
			};
		});
		//刷新新点菜的汇总信息
		if(sumCount > 0){
			$('#divDescForCreateOrde div:first').html('总数量:<font color="green">{count}</font>, 合计:<font color="green">￥{price}</font>'.format({
				count : sumCount.toFixed(2),
				price : sumPrice.toFixed(2)
			}));		
		}else{
			$('#divDescForCreateOrde div:first').html('');
		}
	};
	
	
	//结账
	function submit(c){
		if(of.newFood.isEmpty()){
			Util.msg.alert({
				topTip : true,
				msg : '请选择菜品后再继续操作.'
			});
			return;
		}
		
		var foodData = [], type = true;
		if(c.force != undefined && c.force){
			//force insert
			type = 23;
			foodData = of.newFood.slice(0);
		}else if(of.table.statusValue == 1){
			//update order
			type = 7;
			foodData = of.newFood.slice(0).concat(of.order.orderFoods.slice(0));
		}else{
			//insert order
			type = 1;
			foodData = of.newFood.slice(0);
		}
		
		Util.LM.show();
		
		var orderDataModel = {};
		orderDataModel.tableID = of.table.id;
		orderDataModel.customNum = of.table.customNum;
		orderDataModel.comment = of.table.comment;
		orderDataModel.orderFoods = foodData;
		orderDataModel.categoryValue =  of.table.categoryValue;
		if(of.order){
			orderDataModel.id = of.order.id;
			orderDataModel.orderDate = of.order.orderDate;
		}
		
		$.ajax({
			url : '../InsertOrder.do',
			type : 'post',
			data : {
				commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
				type : type,
				notPrint : c.notPrint ? c.notPrint : false,
				orientedPrinter : getcookie(document.domain + '_printers'),			//特定打印机打印
				wxCode : of.wxCode ? of.wxCode : null								//微信账单号
			},
			success : function(data, status, xhr) {
				if (data.success){
	
					Util.LM.hide();	
					closePopTastePopup();
					closePinyin();
					closeHandWriting();
					Util.msg.alert({
						msg : data.msg,
						topTip : true
					});
					
					if(c.postSubmit){
						c.postSubmit();
					}
					
				}else {
					//为了防止有其他popup时，无法弹出‘强制提交’的popup，因此延迟执行
					setTimeout(function(){
						Util.LM.hide();
						Util.msg.alert({
							title : data.title,
							renderTo : 'orderFoodMgr',
							msg : data.msg, 
							buttons : 'YESBACK',
							btnEnter : '继续提交',
							fn : function(btn){
								if(btn == 'yes'){
									submit({
										force : true,
										postSubmit : function(){
											uo.entry({table : of.table});
										}
									});
								}
							}
						});
					}, 250);
				}
			},
			error : function(request, status, err) {
				Util.LM.hide();
				Util.msg.alert({
					title : '错误',
					renderTo : 'orderFoodMgr',
					msg : err
				});
			}
		});
	};
	
	//关闭可能的口味弹出
	function closePopTastePopup(){
		if(popTastePopup){
			popTastePopup.close();
		}
	}
	
	//手写关闭
	function closeHandWriting(){
		$('#orderHandCmp').hide();
	}
	
	//关闭拼音
	function closePinyin(){	
		$('#orderPinyinCmp').hide();	
	}

	
	//菜品分页
	var foodPaging = null;
	//拼音 & 手写 & 厨房搜索
	function search(value, qw){	
		//如果口味
		var data = null;
		function byConsumption(obj1, obj2){
			//菜品搜索结果按点菜数量排序
		    var val1 = obj1.foodCnt;
		    var val2 = obj2.foodCnt;
		    if (val1 < val2) {
		        return 1;
		    } else if (val1 > val2) {
		        return -1;
		    } else {
		        return 0;
		    } 
		}
		
		function byAlias(obj1, obj2){
			if(!isNaN(obj1.alias) && !isNaN(obj2.alias)){
				var alias1 = parseInt(obj1.alias);
				var alias2 = parseInt(obj2.alias);
				if(alias1 < alias2){
					return -1;
				}else if(alias1 > alias2){
					return 1;					
				}else{
					return 0;
				}
			}else if(isNaN(obj1.alias) && isNaN(obj2.alias)){
				var id1 = parseInt(obj1.id);
				var id2 = parseInt(obj2.id);
				if(id1 < id2){
					return -1;
				}else if(id1 > id2){
					return 1;					
				}else{
					return 0;
				}
			}else if(isNaN(obj1.alias) && !isNaN(obj2.alias)){
				return 1;
			}else if(!isNaN(obj1.alias) && isNaN(obj2.alias)){
				return -1;
			}else{
				return 0;
			}
		}
		
		if(qw == 'pinyin' && value.trim().length > 0){
			data = WirelessOrder.foods.getByPinyin(value);
			data.sort(byConsumption);
			
		}else if(qw == 'handWriting' && value.trim().length > 0){
			data = WirelessOrder.foods.getByName(value);
			data.sort(byConsumption);
							
		}else if(qw == 'byDept'){
			if(value.kitchenId != undefined && value.kitchenId != -1){
				if(data){
					data = data.getByKitchen(value.kitchenId);
				}else{
					data = WirelessOrder.foods.getByKitchen(value.kitchenId);
				}
			}
			if(value.deptId != undefined && value.deptId != -1){
				if(data){
					data = data.getByDept(value.deptId);
				}else{
					data = WirelessOrder.foods.getByDept(value.deptId);
				}
			}
			if(data){
				data.sort(byAlias);
			}else{
				data = WirelessOrder.foods.slice(0).sort(byAlias);				
			}
		}
			
		//创建菜品分页的控件
		if(foodPaging == null){
			//菜品列表
			var foodCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-index={dataIndex} data-value={id}>' +
									'<div style="height: 70px;">{name}<br>￥{unitPrice}' +
										'<div class="food-status-font {commonStatus}">' +
											'<font color="orange">{weigh}</font>' +
											'<font color="blue">{currPrice}</font>' +
											'<font color="FireBrick">{sellout}</font>' +
											'<font color="green">{gift}</font>' +
										'</div>'+
										'<div class="food-status-limit {limitStatus}">' +
											'<font color="orange">限: {foodLimitAmount}</font><br>' +
											'<font color="green">剩: {foodLimitRemain}</font>' +
										'</div>'+								
									'</div>'+
								  '</a>';
			
			foodPaging = new WirelessOrder.Padding({
				renderTo : $('#foodsCmp_div_orderFood'),
				displayTo : $('#foodPagingDesc_div_orderFood'),
				itemLook : function(index, item){
					return foodCmpTemplet.format({
						dataIndex : index,
						id : item.id,
						name : item.name.substring(0, 10),
						unitPrice : item.hasFoodUnit() ? item.getFoodUnit()[0].price : item.unitPrice,
						sellout : item.isSellout() ? '停' : '',
						currPrice : item.isCurPrice() ? '时' : '',		
						gift : item.isAllowGift() ? '赠' : ''	,
						weigh : item.isWeight() ? '称' : '',
						commonStatus : item.isLimit() ? 'none' : '',
						limitStatus : item.isLimit() ? '' : 'none',
						foodLimitAmount : item.foodLimitAmount,
						foodLimitRemain : item.foodLimitRemain
					});
				},
				itemClick : function(index, item){
					if(item.isCurPrice()){
						//是否时价
						openCurrentPriceWin(item);
						
					}else{
						//加入新点菜
						insertFood(item);
					}
				},
				onPageChanged : function(){
					setTimeout(function(){
						$(".food-status-font").css("position", "absolute");
					}, 250);	
				}
			});		
		}
		
		foodPaging.data(data);
	}

	
	//进入点菜界面
	$('#orderFoodMgr').on('pagebeforeshow', function(e){

		of.newFood.clear();
		of.order = null;
		of.table = null;
		
		var param = parseUrl(parseUrl(location.href).hash).params;
		//console.log(param);
		
		if(param.table_id || param.table_alias){
			Util.LM.show();
			$.ajax({
				url : '../QueryTable.do',
				type : 'post',
				data : {
					tableID : param.table_id || '', 
					alias : param.table_alias || ''
				},
				success : function(data, status, xhr){
					if(data.success && data.root.length > 0){
						of.table = data.root[0];
						if(param.comment){
							of.table.comment = param.comment;
						}
	
						//获取沽清菜品的数据
						$.post('../QueryMenu.do', {dataSource : 'stopAndLimit'}, function(result){
							if(result.success){
								updateSellout(result.root);
								//创建部门厨房按钮
								initDeptAndKitchen();
							}
							Util.LM.hide();
						});
						
						if(of.table.statusValue == 1){
							//餐台是就餐状态下需要获取原有的账单信息
							$.post('../QueryOrderByCalc.do', {tableID : of.table.id}, function(result){
								if(result.success){
									of.order = result.other.order;
								}
							});
						}
					}else{
						Util.msg.alert({
							renderTo : 'orderFoodMgr',
							msg : '没有找到此餐台信息'
						});
					}
				},
				error : function(request, status, err){
					Util.LM.hide();
					Util.msg.alert({
						renderTo : 'orderFoodMgr',
						msg : err
					});
				}
			});
		}else{
			//获取沽清菜品的数据
			$.post('../QueryMenu.do', {dataSource : 'stopAndLimit'}, function(result){
				if(result.success){
					updateSellout(result.root);
					//创建部门厨房按钮
					initDeptAndKitchen();
				}
			});
		}
	

		function updateSellout(selloutFoods){
			WirelessOrder.foods.forEach(function(e, index){
				//先把菜品全部变为不停售的, 因为可能之前是停售的, 现在不停售了
				e.setSellout(false);
				e.setLimit(false);
			});
			
			selloutFoods.forEach(function(e, index){
				var f = WirelessOrder.foods.getById(e.id);
				if(f){
					//更新估清菜品的状态和限量估清的数量
					f.status = e.status;
					if(f.isLimit()){
						f.setLimit(true, e.foodLimitAmount, e.foodLimitRemain);
					}
				}
			});
		}
		
		//进入点菜界面的时候渲染数据
		function initDeptAndKitchen(){
			if(of.table){
				$('#divNFCOTableBasicMsg').html(of.table.alias + '<br>' + of.table.name);
			}else{
				$('#divNFCOTableBasicMsg').html("1号" + '<br>' + "餐桌");
				document.getElementById("divNFCOTableBasicMsg").style.textAlign="center";
			}
				
			//正常点菜
			if(of.orderFoodOperateType == 'normal'){
				$('#normalOrderFood_a_orderFood').show();
				$('#btnOrderAndPay').show();
				$('#addBookOrderFood').hide();
				$('#bookSeatOrderFood_a_orderFood').hide();
				$('#multiOpenTable_a_tableSelect').hide();
				//快餐模式的牌子号
				$('#brand_a_orderFood').hide();
				//快餐模式的结账
				$('#fastPay_a_orderFood').hide();
			}else if(of.orderFoodOperateType == 'bookSeat'){
				$('#bookSeatOrderFood_a_orderFood').show();
				$('#addBookOrderFood').hide();
				$('#btnOrderAndPay').hide();
				$('#normalOrderFood_a_orderFood').hide();		
				$('#multiOpenTable_a_tableSelect').hide();
				//快餐模式的牌子号
				$('#brand_a_orderFood').hide();
				//快餐模式的结账
				$('#fastPay_a_orderFood').hide();
			}else if(of.orderFoodOperateType == 'addBook'){
				$('#addBookOrderFood').show();
				$('#bookSeatOrderFood_a_orderFood').hide();
				$('#normalOrderFood_a_orderFood').hide();
				$('#btnOrderAndPay').hide();	
				$('#multiOpenTable_a_tableSelect').hide();
				//快餐模式的牌子号
				$('#brand_a_orderFood').hide();
				//快餐模式的结账
				$('#fastPay_a_orderFood').hide();
			}else if(of.orderFoodOperateType == 'multiOpenTable'){
				$('#multiOpenTable_a_tableSelect').show();
				$('#addBookOrderFood').hide();
				$('#bookSeatOrderFood_a_orderFood').hide();
				$('#normalOrderFood_a_orderFood').hide();
				$('#btnOrderAndPay').hide();
				//快餐模式的牌子号
				$('#brand_a_orderFood').hide();
				//快餐模式的结账
				$('#fastPay_a_orderFood').hide();
			}else if(of.orderFoodOperateType == 'fast'){
				//下单
				$('#normalOrderFood_a_orderFood').hide();
				$('#btnOrderAndPay').hide();
				$('#addBookOrderFood').hide();
				$('#bookSeatOrderFood_a_orderFood').hide();
				$('#multiOpenTable_a_tableSelect').hide();
				//快餐模式的牌子号
				$('#brand_a_orderFood').show();
				//快餐模式的结账
				$('#fastPay_a_orderFood').show();
				//下单更多
				$('#orderFoodMore_a_orderFood').hide();
			}
				
				
			//渲染数据
			initDeptContent();
			initKitchenContent();
			
	
			//第一次加载不成功,延迟来一直加载达到菜品显示出来为止
			(function(){
				if($('#foodsCmp_div_orderFood')[0].clientHeight != 0){
					search({}, 'byDept');
				}else{
					setTimeout(arguments.callee, 500);
				}
			})();
			
			//初始化添加的菜品
			of.initFoods.forEach(function(e){
				of.newFood.addOrderFood(e);
			});
			
			initNewFoodContent();
		
		};
	
		//部门分页
		var deptPagingStart = 0;
		
		 // 初始化部门选择
		function initDeptContent(){
			var allDeptCmpId = new Date().getTime() + '_dept';
			var html = ['<a id="' + allDeptCmpId + '" data-role="button" data-inline="true" class="deptKitBtnFont" data-value="-1" data-type="deptCmp">全部部门</a>'];
			
			//部门列表
			var eachDeptClass = new Date().getTime() + '_deptClass';
			var deptCmpTemplet = '<a data-role="button" data-inline="true" class="deptKitBtnFont ' + eachDeptClass + '" data-type="deptCmp" data-value="{id}" >{name}</a>';
			//真实宽度
			var usefullWidth = document.body.clientWidth - 220;
			//每行显示部门的个数
			var displayDeptCount =  parseInt(usefullWidth / 88);	
			
			var deptPagingLimit = WirelessOrder.depts.length > displayDeptCount ? displayDeptCount-1 : displayDeptCount;
			
			var limit = WirelessOrder.depts.length >= deptPagingStart + deptPagingLimit ? deptPagingLimit : deptPagingLimit - (deptPagingStart + deptPagingLimit - WirelessOrder.depts.length);
			
			
			if(WirelessOrder.depts.length > 0){
				for (var i = 0; i < limit; i++) {
					var dName = WirelessOrder.depts[deptPagingStart + i].name;
					html.push(deptCmpTemplet.format({
						id : WirelessOrder.depts[deptPagingStart + i].id,
						name : dName.length > 4? dName.substring(0, 4) : dName
					}));
				}
			}	
			//显示部门分页按钮
			var deptPrevId = new Date().getTime() + '_prevPage';
			var deptNextId = new Date().getTime() + '_nextPage';
			if(WirelessOrder.depts.length > displayDeptCount){
				html.push('<a id="' + deptPrevId + '" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
						'<a id="' + deptNextId + '" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
			}	
			$("#deptsCmp").html(html.join('')).trigger('create').trigger('refresh');
			
			//全部部门的点击事件
			$('#' + allDeptCmpId ).click(function(){
				initKitchenContent();
				search({}, 'byDept'); 
			});
			
			//部门点击事件
			$('.' + eachDeptClass).each(function(index, element){
				element.onclick = function(){
					initKitchenContent(parseInt(element.getAttribute('data-value')));
					search({deptId : parseInt(element.getAttribute('data-value'))}, 'byDept');
				};
			});
			
			//部门分页下一页	
			$('#' + deptNextId).click(function(){
				deptPagingStart += deptPagingLimit;
				if(deptPagingStart > WirelessOrder.depts.length){
					deptPagingStart -= deptPagingLimit;
					return;
				}
				initDeptContent();
			});
		
			//部门分页上一页
			$('#' + deptPrevId).click(function(){
				deptPagingStart -= deptPagingLimit;
				if(deptPagingStart < 0){
					deptPagingStart += deptPagingLimit;
					return;
				}
				initDeptContent();
			});	
			
		}


		/**
		 * 初始化分厨选择
		 * @param c
		 */
	    function initKitchenContent(deptId){
			var sl = $('#deptsCmp a[data-type=deptCmp]');
			sl.attr('data-theme', 'c');
			//存放对应部门的厨房
			var kitchenPagingData = null;
			if(deptId != undefined){
				for(var i = 0; i < sl.length; i++){
					if($(sl[i]).attr('data-value') == deptId){
						$(sl[i]).attr('data-theme', 'b');
					}else{
						$(sl[i]).attr('data-theme', 'c');
					}
				}
				kitchenPagingData = [];
				for(var i = 0; i < WirelessOrder.kitchens.length; i++){
					var temp = WirelessOrder.kitchens[i];
					if(typeof deptId == 'number' && deptId != undefined){
						if(temp.dept.id == deptId){
							kitchenPagingData.push({
								id : temp.id,
								name : temp.name
							});
						}
					}else{
						if(temp.dept.id != -1){
							kitchenPagingData.push({
								id : temp.id,
								name : temp.name
							});
						}
					}
				}
			}else{
				for(var i = 0; i < sl.length; i++){
					if($(sl[i]).attr('data-value') == -1){
						$(sl[i]).attr('data-theme', 'b');
					}else{
						$(sl[i]).attr('data-theme', 'c');
					}
				}
				kitchenPagingData = WirelessOrder.kitchens.slice(0);
			}
			
			sl.buttonMarkup( "refresh" );
			
			//显示厨房分页
			showKitchenPaging(0, kitchenPagingData);
			
			closePopTastePopup();
			//关闭拼音
			closePinyin();
			//关闭手写
			closeHandWriting();
		}

		/**
		 * 显示厨房分页
		 */
	    function showKitchenPaging(kitchenPagingStart, kitchenPagingData){
			var kc = $("#kitchensCmp");
			var findAllKiechen = new Date().getTime() + '_allKitchen';
			var html = ['<a id="' + findAllKiechen + '" data-role="button" data-inline="true" data-type="kitchenCmp" data-value=-1 class="deptKitBtnFont">全部厨房</a>'];
			
			//真实宽度
			var usefullWidth = document.body.clientWidth - 220;
			//每行显示厨房的个数
			var displayKitchenCount =  parseInt(usefullWidth / 88);
			
			var kitchenPagingLimit = kitchenPagingData.length > displayKitchenCount ? displayKitchenCount-1 : displayKitchenCount;
			
			var limit = kitchenPagingData.length >= kitchenPagingStart + kitchenPagingLimit ? kitchenPagingLimit : kitchenPagingLimit - (kitchenPagingStart + kitchenPagingLimit - kitchenPagingData.length);
			
			var  eachFindKitchenClass = new Date().getTime() + '_eachKitchen';
			//厨房列表
			var kitchenCmpTemplate = '<a data-role="button" data-inline="true" class="deptKitBtnFont ' + eachFindKitchenClass + '" data-type="kitchenCmp" data-value={id} ">{name}</a>';
			
			if(kitchenPagingData.length > 0){
				for (var i = 0; i < limit ; i++) {
					var kName = kitchenPagingData[kitchenPagingStart + i].name;
					html.push(kitchenCmpTemplate.format({
						id : kitchenPagingData[kitchenPagingStart + i].id,
						name : kName.length > 4? kName.substring(0, 4) : kName
					}));
				}
			}
			
			var kitchenPrevId = new Date().getTime() + 'kitchenPrev';
			var kitchenNextId = new Date().getTime() + 'kitchenNext';
			//显示分页按钮
			if(kitchenPagingData.length > displayKitchenCount){
				html.push('<a id="' + kitchenPrevId + '" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
						'<a id="' + kitchenNextId + '" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
			}	
			kc.html(html.join("")).trigger('create').trigger('refresh');
			
			$('#kitchensCmp a').each(function(index, element){
				if($(element).attr('data-theme') != 'b'){
					$(element).attr('data-theme', 'c');
				}
			});
			
			$('#kitchensCmp a').attr('data-theme', 'c');
			$('#kitchensCmp a').buttonMarkup( 'refresh' );
			
			//全部厨房点击事件
			$('#' + findAllKiechen).click(function(){
				var dl = $('#deptsCmp a[data-theme=b]');
				search({deptId : parseInt(dl[0].getAttribute('data-value'))}, 'byDept');
				//将所有厨房的样式都变为未选中状态
				$('#kitchensCmp a[data-type=kitchenCmp]').each(function(index, element){
					$(element).attr('data-theme', 'c');
					$(element).buttonMarkup( 'refresh' );
				});
				var sl = $('#kitchensCmp a[data-type=kitchenCmp]:first');		
				sl.attr('data-theme', 'b');
				sl.buttonMarkup( "refresh" );
	
				closePopTastePopup();
				//关闭拼音
				closePinyin();
				//关闭手写
				closeHandWriting();
			});
			
			//厨房点击事件
			$('.' + eachFindKitchenClass).each(function(index, element){
				element.onclick = function(){
					var dl = $('#deptsCmp a[data-theme=b]');
					var kitchenId = parseInt(element.getAttribute('data-value'));
					var deptId = parseInt(dl[0].getAttribute('data-value'));
					if(kitchenId == -1){
						search({deptId : deptId}, 'byDept');
					}else{
						search({deptId : deptId, kitchenId : kitchenId}, 'byDept');
					}
				
					var sl = $('#kitchensCmp a[data-type=kitchenCmp]');
					sl.attr('data-theme', 'c');
					if(element){
						$(element).attr('data-theme', 'b');
					}
	
					sl.buttonMarkup( "refresh" );
						
					closePopTastePopup();
					//关闭拼音
					closePinyin();
					//关闭手写
					closeHandWriting();
				};
			});
			
			
			//厨房分页上一页
			$('#' + kitchenPrevId).click(function(){
				kitchenPagingStart -= kitchenPagingLimit;
				if(kitchenPagingStart < 0){
					kitchenPagingStart += kitchenPagingLimit;
					return;
				}
				showKitchenPaging(kitchenPagingStart, kitchenPagingData);
			});
			
			//厨房分页下一页
			$('#' + kitchenNextId).click(function(){
				kitchenPagingStart += kitchenPagingLimit;
				if(kitchenPagingStart > kitchenPagingData.length){
					kitchenPagingStart -= of.kitchenPagingLimit;
					return;
				}
				showKitchenPaging(kitchenPagingStart, kitchenPagingData);
			});
		};
		
		function parseUrl(url){
		    var a = document.createElement('a');
		    a.href = url;
		    return {
		        source: url,
		        protocol: a.protocol.replace(':', ''),
		        host: a.hostname,
		        port: a.port,
		        query: a.search,
		        params: (function () {
		            var ret = {},
		            seg = a.search.replace(/^\?/, '').split('&'),
		            len = seg.length, i = 0, s;
		            for (; i < len; i++) {
		                if (!seg[i]) { 
		                	continue; 
		                }
		                s = seg[i].split('=');
		                ret[s[0]] = s[1];
		            }
		            return ret;
		 
		        })(),
		        file: (a.pathname.match(/\/([^\/?#]+)$/i) || [, ''])[1],
		        hash: a.hash.replace('#', ''),
		        path: a.pathname.replace(/^([^\/])/, '/$1'),
		        relative: (a.href.match(/tps?:\/\/[^\/]+(.+)/) || [, ''])[1],
		        segments: a.pathname.replace(/^\\/, '').split('/')
		    };
		}
	});
	
	//点菜界面初初始化
	$('#orderFoodMgr').on('pageinit', function(){
		//初始化新点菜
		of.newFood = new WirelessOrder.Order();
		
		//判断是否显示'赠送'按钮
		if(WirelessOrder.login.hasPrivilege(WirelessOrder.Staff.Privilege.GIFT)){
			$('#giftFoodOperate_a_orderFood').show();
		}else{
			$('#giftFoodOperate_a_orderFood').hide();
		}
		
		//点菜页面的下一页
		$('#getNextPage_a_orderFood').click(function(){
			foodPaging.next();
		});
		
		//点菜页面的上一页
		$('#getPrevious_a_orderFood').click(function(){
			foodPaging.prev();
		});
		
		var updateTempTaste = false;
		//打开手写口味
		$('#addTaste_a_orderFood').click(function(){
			var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
			if(foodContent.length != 1){
				Util.msg.tip('请选中一道菜品');
				return;
			}
			
			//是否选中键盘
			of.selectedOrderFood = of.newFood[foodContent.attr('data-index')];
			
			closePopTastePopup();
			
			if(!of.selectedOrderFood.isTemporary){
				seajs.use('tempTaste', function(tempTaste){
					var tmpTaste = null;
					tmpTaste = tempTaste.newInstance({
						selectedFood : of.selectedOrderFood,
						confirm : function(selectedFood, name, price){
							of.selectedOrderFood.setTempTaste(name, price);
							initNewFoodContent();	
						}
					})
					tmpTaste.open();
				});
			}else{
				Util.msg.tip('临时菜不能使用手写口味')
			}
			
		});
	
		
		//保存选择分厨的id
		var selectedKitchen = null;	
		//保存临时菜
		$('#saveTemp_a_orderFood').click(function(){
			var name = $('#tempFoodName_input_addTemp');
			var price = $('#tempFoodPrice_input_addTemp');
			var count = $('#tempFoodCount_input_addTemp');
			
			if(!name.val()){
				Util.msg.alert({
					topTip : true,
					msg : '请临时菜名称'
				});		
				name.focus();
				return;
			}
			
			if(!price.val()){
				Util.msg.alert({
					topTip : true,
					msg : '请填写价格'
				});
				price.focus();
				return;	
			}else if(isNaN(price.val()) || parseFloat(price.val()) < 0){
				Util.msg.alert({
					topTip : true,
					msg : '请填写正确的价格'
				});
				price.focus();
				return;		
			}
		
			if(!count.val()){
				count.val(1);
			}else if(isNaN(count.val()) || parseFloat(count.val()) < 1){
				Util.msg.alert({
					topTip : true,
					msg : '请填写正确的数量'
				});
				return;		
			}	
			
			of.newFood.addTemp({
				name : name.val(),
				price : parseFloat(price.val()),
				count : parseFloat(count.val()),
				kitchen : selectedKitchen
			});
			
			initNewFoodContent();	
			
			$('#closeTemp_a_orderFood').click();
		});
	
		// 关闭临时菜
		$('#closeTemp_a_orderFood').click(function(){
			$('#addTempFoodCmp').hide();
			$('#shadowForPopup').hide();	
			$('#tempFoodName_input_addTemp').val('');
			$('#tempFoodPrice_input_addTemp').val('');
			$('#tempFoodCount_input_addTemp').val(1);
			//临时菜文本框detach
			HandWritingAttacher.instance().detach($('#tempFoodName_input_addTemp')[0]);
			//临时菜数量和价钱detach
			NumKeyBoardAttacher.instance().detach($('#tempFoodPrice_input_addTemp')[0]);
			NumKeyBoardAttacher.instance().detach($('#tempFoodCount_input_addTemp')[0]);
		});
	
		//弹出临时菜
		$('#addTemp_a_orderFood').click(function(){
			//初始化临时菜分厨
			var html = [];
			for (var i = 0; i < of.tempKitchens.length; i++) {
				html.push('<li class="tempFoodKitchen" id="' + of.tempKitchens[i].id + '"><a>' + of.tempKitchens[i].name +'</a></li>');
			}
				
			$('#tempFoodKitchensCmp').html(html.join("")).trigger('create');
			
			$('#tempFoodKitchensCmp').find('li').each(function(index, element){
				element.onclick = function(){
					selectedKitchen = element.id;
					$('#lab4TempKitchen').text($(element).find('a').text());
					$('#popupTempFoodKitchensCmp').popup('close');			
				};						
			});
			
			$('#tempFoodKitchensCmp').listview('refresh');
			
			//默认选中第一个厨房
			selectedKitchen = of.tempKitchens[0].id;
		    	
			//默认厨房
			$('#lab4TempKitchen').text(of.tempKitchens[0].name);
			
			$('#addTempFoodCmp').show();
			$('#shadowForPopup').show();
			
			//临时菜输入框弹出手写板控件
			HandWritingAttacher.instance().attach($('#tempFoodName_input_addTemp')[0]);
			
			$('#tempFoodName_input_addTemp').focus();
			
			//临时菜数量和价钱关联数字键盘控件
			NumKeyBoardAttacher.instance().attach($('#tempFoodPrice_input_addTemp')[0]);
			NumKeyBoardAttacher.instance().attach($('#tempFoodCount_input_addTemp')[0]);
			
			closePopTastePopup();
			//关闭拼音和手写搜索
			closePinyin();
			closeHandWriting();
			
		});
		
		//手写板控件
		var handWriting = null;

		//手写重写按钮的click事件
		$('#rewrite_a_orderFood').click(function(){
			if(handWriting){
				handWriting.rewrite();  
			}
			$('#searchWord_div_orderFood').html('');
			$('#handWritingInput_input_orderFood').focus();
		});
		
		//手写搜索的清空按钮事件
		$('#handDel_a_orderFood').click(function(){
			$('#handWritingInput_input_orderFood').val('');
			$('#handWritingInput_input_orderFood').trigger('input');
		});
		
		//监听手写搜索输入框值变化的input事件
		$("#handWritingInput_input_orderFood").on("input", function(){
			search(this.value, 'handWriting');
		});
		
		//监听拼音搜索输入框值变化的input时间
		$('#pinyinInput_input_orderFood').on("input", function(){
			search(this.value, 'pinyin');
		});
		
		//手写搜索的关闭按钮事件
		$('#handWritingClose_a_orderFood').click(function(){
			//模拟点击事件
			var kitchen = $('#kitchensCmp > a[data-theme=b]');
			if(kitchen.length > 0){
				kitchen.click();
			}else{
				kitchen = $('#kitchensCmp a[data-type=kitchenCmp]:first')[0];
				kitchen.click();
			}
			kitchen = null;		
			closeHandWriting();
		});
		
		//已点菜界面手写板按钮的click事件
		$('#handWriteBoard_a_orderFood').click(function(){	
			$('#orderHandCmp').show();
			if(handWriting == null){
				handWriting = createHandWriting();
			}
			handWriting.rewrite();
			
			closePopTastePopup();
			//关闭拼音
			closePinyin();
			$('#handWritingInput_input_orderFood').val('');
			$('#searchWord_div_orderFood').html('');
			$('#handWritingInput_input_orderFood').focus();
		});
		
		//手写板的创建
		function createHandWriting(){
			return new HandWritingPanel({
				renderTo : document.getElementById('handWritingPanel_th_orderFood'),
				result : function(data){
					var temp = data.slice(0, 6);			
					var zifu = "";
					for(var i = 0; i < temp.length; i++){								
						var eachCharactar = '<input type="button" style="width:33%;height:65%;font-size:30px;" value="' + temp[i] + '">';							
						if(i % 3 == 0 ){
							zifu += '<br>'; 
						}
						zifu += eachCharactar;										
					}
					document.getElementById('searchWord_div_orderFood').innerHTML = zifu;
					$('#searchWord_div_orderFood').find('input').each(function(index, element){
						element.onclick = function(){
							$('#handWritingInput_input_orderFood').val($('#handWritingInput_input_orderFood').val() + element.value);
							$('#handWritingInput_input_orderFood').trigger('input');
							$('#rewrite_a_orderFood').click();
						};
					});
				}
			});	
		}
		
		//拼音清空按钮事件
		$('#pinyinVal_a_orderFood').click(function(){
			$('#pinyinInput_input_orderFood').val('');
			$('#pinyinInput_input_orderFood').trigger('input');
		});
		
		//拼音删除按钮事件
		$('#pinyinDel_a_orderFood').click(function(){
			var s = $('#pinyinInput_input_orderFood').val();
			$('#pinyinInput_input_orderFood').val(s.substring(0, s.length - 1));
			$('#pinyinInput_input_orderFood').trigger('input');
		});
	
		//拼音关闭按钮
		$('#closePinyin_a_orderFood').click(function(){
			//模拟点击事件
			var kitchen = $('#kitchensCmp > a[data-theme=b]');
			if(kitchen.length > 0){
				kitchen.click();
			}else{
				kitchen = $('#kitchensCmp a[data-type=kitchenCmp]:first')[0];
				kitchen.click();
			}
			kitchen = null;	
			closePinyin();
		});
		
		//监听点菜界面拼音搜索按钮的事件
		$('#pinyinBoard_a_orderFood').click(function(){	
			$('#orderPinyinCmp').show();
			$('#pinyin_div_orderFood').show();	
			$('#pinyinInput_input_orderFood').val('');

			closePopTastePopup();
			//关闭手写
			closeHandWriting();
			createPinyinKeyboard();
			$('#pinyinInput_input_orderFood').focus();
		});
		
		//拼音键盘动态生成
		function createPinyinKeyboard(){
			var allKeys = new Array("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W"
								, "X", "Y", "Z");
			var keys = "";
			for(var i = 0; i < allKeys.length; i++){
				var eachKeys = '<input type="button" style="width:11%;height:21%;font-size:20px;" value="' + allKeys[i] + '">';
				if(i % 9 == 0){
					keys += '<br>';
				}
				keys += eachKeys;
			}	
			document.getElementById('pinyin_div_orderFood').innerHTML = keys;
			$('#pinyin_div_orderFood input').each(function(index, element){
				element.onclick = function(){
					$('#pinyinInput_input_orderFood').val($('#pinyinInput_input_orderFood').val() + element.value);
					$('#pinyinInput_input_orderFood').trigger('input');		
				};
			});	
		}
		
		//快餐模式下牌子号的点击事件
		$('#brand_a_orderFood').click(function(){
			var brandPopup = null;
			brandPopup = new NumKeyBoardPopup({
				header : '请输入牌号',
				left : function(){
					var brandNo;
					if($("#input_input_numKbPopup").val()){
						brandNo = parseInt($("#input_input_numKbPopup").val());
					}else{
						brandNo = 1;
					}
					var bandTemp = WirelessOrder.tables.slice(0);
					
					//遍历来判断输入的牌子号是否存在
					for(var i = 0; i < bandTemp.length; i++){
						if(brandNo == bandTemp[i].alias){
							of.table = bandTemp[i];
							break;
						}
					}
					if(of.table){
						submit({
							force : true,
							postSubmit : function(){
								pm.entry({table : of.table});
							}
						
						});
						brandPopup.close();
					}else{
						Util.msg.alert({
							msg : '没有此餐桌号.', 
							topTip : true
						});
					}
				},
				middleText : '先送',
				middle : function(){
					var brandNo;
					
					if($("#input_input_numKbPopup").val()){
						brandNo = parseInt($("#input_input_numKbPopup").val());
						
						var bandTemp = WirelessOrder.tables.slice(0);
						
						//遍历来判断输入的牌子号是否存在
						for(var i = 0; i < bandTemp.length; i++){
							if(brandNo == bandTemp[i].alias){
								of.table = bandTemp[i];
							}
						}
						
						if(of.table){
							brandPopup.close(function(){
								submit({
									notPrint : false,
									force : true,
									postSubmit : function(){
										//清空已点菜
										$('#orderFoodsCmp').html('');
										of.newFood.clear();
										//清空状态栏
										$('#divDescForCreateOrde div:first').html(''); 
										$('#orderFoodsCmp').listview('refresh');
										
										//更新餐台
										$.post('../QueryTable.do', {tableID : of.table.id}, function(result){
											of.table = result.root[0];
										});
										//更新账单
										$.post('../QueryOrderByCalc.do', {tableID : of.table.id}, function(result){
											of.order = result.other.order;
										});
									} 
								});
							}, 200);
						}else{
							Util.msg.alert({
								msg : '没有此餐桌号.', 
								topTip : true
							});
						}
					}else{
						Util.msg.alert({
							msg : '请输入牌子号.', 
							topTip : true
						});
					}
				},
				right : function(){
					brandPopup.close();
				}
			});
		
			brandPopup.open(function(self){
				self.find('[id=left_a_numKbPopup]').css({
					'width' : '32%',
					'float' : 'left'
				});
				self.find('[id=middle_a_numKbPopup]').css({
					'width' : '32%',
					'float' : 'left'
				});
				self.find('[id=right_a_numKbPopup]').css({
					'width' : '34%',
					'height' : '10%',
					'float' : 'left'
				});
				setTimeout(function(){
				//	self.find('[id=input_input_numKbPopup]').select();
					self.find('[id=input_input_numKbPopup]').focus();
					self.find('[id=input_input_numKbPopup]').select();
				}, 200);
			});
		
		});
		
		//快餐模式下的结账按钮
		$('#fastPay_a_orderFood').click(function(){
			var brandNo = 1;
			
			var bandTemp = WirelessOrder.tables.slice(0);
			//遍历来判断输入的牌子号是否存在
			for(var i = 0; i < bandTemp.length; i++){
				if(brandNo == bandTemp[i].alias){
					of.table = bandTemp[i];
					break;
				}
			}
			
			if(of.table){
				submit({
					force : true,
					postSubmit : function(){
						pm.entry({table : of.table});
					}
				});
			}else{
				Util.msg.alert({
					msg : '没有此餐桌号.', 
					topTip : true
				});
			}
		});
		
		
		//下单按钮事件
		$('#normalOrderFood_a_orderFood').click(function(){
			submit({
				notPrint : false,
				postSubmit : function(){
					uo.entry({table : of.table});
				}
			});
			
		});
		
		//下单并结账按钮事件
		$('#orderPay_li_orderFood').click(function(){
			$('#orderMore_div_orderFood').popup('close');
			submit({
				//设置无论从哪个界面进入点菜, 下单后都会去结账界面
				postSubmit : function(){
					pm.entry({table:of.table});
				}
			});
		});
		
		//下单不打印按钮事件
		$('#orderNotPrint_li_orderFood').click(function(){
			$('#orderMore_div_orderFood').popup('close');
			submit({
				notPrint : true,
				postSubmit : function(){
					uo.entry({table : of.table});
				}
			});	
		});
		
		
		//先送按钮事件
		$('#orderPre_li_orderFood').click(function(){
			$('#orderMore_div_orderFood').popup('close');
			submit({
				notPrint : false,
				force : true,
				postSubmit : function(){
					//清空已点菜
					$('#orderFoodsCmp').html('');
					of.newFood.clear();
					//清空状态栏
					$('#divDescForCreateOrde div:first').html('');
					$('#orderFoodsCmp').listview('refresh');
					
					//更新餐台
					$.post('../QueryTable.do', {tableID : of.table.id}, function(result){
						of.table = result.root[0];
					});
					//更新账单
					$.post('../QueryOrderByCalc.do', {tableID : of.table.id}, function(result){
						of.order = result.other.order;
					});
				} 
			});	
		});	
		
		//返回
		$('#orderFoodBack_a_orderFood').click(function(){
			ts.loadData();
		});
		
		//助记码初始化
		var aliasPopup = null;
		aliasPopup = new NumKeyBoardPopup({
			header : '输入助记码',
			left : function(){
				var alias = $('#input_input_numKbPopup');
			 	if(!alias.val()){
			 		Util.msg.alert({
			 			msg : '请填写助记码',
			 			topTip : true
			 		});
			 		alias.focus();
			 		return;
			 	}
				 	
			 	var data = WirelessOrder.foods.getByAlias(alias.val());
			 	if(data == null){
			 		Util.msg.alert({
			 			topTip : true,
			 			msg : '此编码无对应菜品'
			 		});
			 		alias.focus();
			 	}else{
			 		insertFood(WirelessOrder.foods.getById(data.id));
			 	}
			 	alias.val('');
			},
			right : function(){
				aliasPopup.close();
			}
		});
		
		//打开助记码
		$('#aliasOrderFood_a_orderFood').click(function(){
			aliasPopup.open(function(self){
				self.find('[id=middle_a_numKbPopup]').hide();
				$('#input_input_numKbPopup').focus();
				closePopTastePopup();
				closePinyin();
				closeHandWriting();
			});

		});
		
		//菜品数量+1
		$('#foodAmountAdd_a_orderFood').click(function(){
			operateFoodCount({count : 1});
		});
		
		//菜品数量减1
		$('#foodAmountCut_a_orderFood').click(function(){
			operateFoodCount({count : -1});
		});
		
		//打开数量设置
		$('#foodAmountSet_a_orderFood').click(function(){
			operateFoodCount({otype : 'set'});
		});
		
		//删除当前选中的菜
		$('#deleteFood_a_orderFood').click(function(){
			operateFoodCount({otype : 'delete'});
		});
		
		//选择口味
		$('#selectTaste_a_orderFood').click(function(){
			var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
			if(foodContent.length != 1){
				Util.msg.tip('请选中一道菜品');
				return;
			}
			
			//是否选中键盘
			of.selectedOrderFood = of.newFood[foodContent.attr('data-index')];
			
			closePopTastePopup();
			
			if(!of.selectedOrderFood.isTemporary){
				seajs.use('taste', function(taste){
					popTastePopup = taste.newInstance({
						selectedFood : of.selectedOrderFood,
						postTasteChanged : function(){
							initNewFoodContent();
						},
						postUnitClick : function(){
							initNewFoodContent();
						}
					});
					popTastePopup.open();
				});
			}else{
				Util.msg.tip('临时菜不能使用口味');
			}
		});
		
		//赠送
		$('#giftFoodOperate_a_orderFood').click(function(){
			giftFood();
		});
		
		//叫起
		$('#foopHangUp_a_orderFood').click(function(){
			foodHangUp({type:2});
		});
		
		//修改时价
		$('#updatePrice_li_orderFood').click(function(){
			$('#orderFoodOtherOperateCmp').popup('close');	
			setTimeout(function(){
				var selectedFood = of.newFood.getSelected();
				if(selectedFood && selectedFood.isCurPrice()){
				//if(of.selectedOrderFood.isCurPrice()){
					setTimeout(function(){
						var curPricePopup = null;
						curPricePopup = new NumKeyBoardPopup({
							header : '修改时价--' + selectedFood.name,
							left : function(){
								var unitPrice = parseFloat($('#input_input_numKbPopup').val());
								//重新设置时价
								selectedFood.unitPrice = unitPrice;
								initNewFoodContent();	
								curPricePopup.close();
							},
							right : function(){
								curPricePopup.close();
							}
						});
						curPricePopup.open(function(self){
							self.find('[id=input_input_numKbPopup]').val(selectedFood.unitPrice);
							self.find('[id=middle_a_numKbPopup]').hide();
							setTimeout(function(){
							//	self.find('[id=input_input_numKbPopup]').select();
								self.find('[id=input_input_numKbPopup]').focus();
								self.find('[id=input_input_numKbPopup]').select();
							}, 200);
							
						});
					}, 250);
				}else{
					Util.msg.tip('此菜品不能设置时价');
				}
			}, 300);
		});
		
		//全单口味
		$('#allFoodTaste_li_orderFood').click(function(){
			closePopTastePopup();
			$('#orderFoodOtherOperateCmp').popup('close');
			seajs.use('moreTastes', function(more){
			var moreTaste = null;
			moreTaste = more.newInstance({
				postTasteClick : function(chooseTaste){
					of.newFood.forEach(function(e){
						e.addTaste(chooseTaste);
					})
					initNewFoodContent();	
				},
				postTasteCancel : function(chooseTaste){
					of.newFood.forEach(function(e){
						e.removeTaste(chooseTaste);
					})
					initNewFoodContent();	
				}
			});
			
			moreTaste.open();
		});	
				
					
		});
		
		//全单叫起
		$('#allFoodHangUp_li_orderFood').click(function(){
			foodHangUp({type : 1});
		});
		
		//使用入座的回调
		$('#bookSeatOrderFood_a_orderFood').click(function(){
			of.commit(of.newFood);
		});
		
		//多台开席
		$('#multiOpenTable_a_tableSelect').click(function(){
			of.commit(of.newFood);
		});
		
	});
	
	/**
	 * 操作菜品数量
	 */
	function operateFoodCount(c){
		var selectedFood = of.newFood.getSelected();

		if(selectedFood == null){
			Util.msg.tip('请选中一道菜品');
			return;
		}
		
		if(typeof c.otype == 'string'){
			if(c.otype.toLowerCase() == 'delete'){
				Util.msg.alert({
					title : '重要',
					msg : '是否去除该菜品?',
					buttons : 'YESBACK',
					renderTo : 'orderFoodMgr',
					certainCallback : function(btn){
						if(btn == 'yes'){
							of.newFood.splice($('#orderFoodsCmp > li[data-theme=e]').attr('data-index'), 1);
							of.selectedOrderFood = null;
							initNewFoodContent();
						}
					}
				});
				
			}else if(c.otype.toLowerCase() == 'set'){
				//数量=初始化
				var foodCount = null;
				foodCount = new NumKeyBoardPopup({
					header : '请输入菜品数量',
					left : function(){
						var count = parseFloat($('#input_input_numKbPopup').val());
				    	if(count <= 0){
				    		foodCount.close(function(){
				    			operateFoodCount({
									otype : 'delete'
								});
				    		}, 200);

						}else{
							//重新设置数量
							selectedFood.setCount(count);
							initNewFoodContent();
							foodCount.close();
						}  
					},
					right : function(){
						foodCount.close();
					}
				});
				
				foodCount.open(function(self){
					self.find('[id=input_input_numKbPopup]').val(selectedFood.count);
					self.find('[id=middle_a_numKbPopup]').hide();
					setTimeout(function(){
						self.find('[id=input_input_numKbPopup]').focus();
						self.find('[id=input_input_numKbPopup]').select();
					}, 200);
					
				});
				
				
				of.selectedOrderFood = selectedFood;
				
			}
		}else{
			var nc = selectedFood.count + c.count;
			if(nc <= 0){
				operateFoodCount({
					otype : 'delete'
				});
			}else{
				selectedFood.setCount(nc);
			}
			initNewFoodContent();
		}
		
	};
	
	/**
	 * 赠送菜品
	 */
	function giftFood(){
		var selectedFood = of.newFood.getSelected();
		if(selectedFood){
			try{
				if(selectedFood.isGifted()){
					selectedFood.setGift(false, WirelessOrder.login);
				}else{
					selectedFood.setGift(true, WirelessOrder.login);
				}
			}catch(errMsg){
				Util.msg.alert({
					msg : errMsg,
					topTip : true
				});
			}
			initNewFoodContent();
		}else{
			Util.msg.tip('请选中一道菜品');
		}
		
	};
	
	/**
	 * 叫起
	 * params
	 *  type: 1:全单叫起 2:单个叫起
	 */
	function foodHangUp(c){
		if(c == null || typeof c.type != 'number'){
			return;
		}
		
		if(c.type == 1){
			//关闭'更多'控件
			$('#orderFoodOtherOperateCmp').popup('close');
			
			if(of.newFood.length > 0){
				var isHangup;
				if(of.newFood[0].isHang()){
					isHangup = false;
				}else{
					isHangup = true;
				}
				of.newFood.forEach(function(e){
					e.setHangup(isHangup);
				});
			}
			
			initNewFoodContent();
		}else if(c.type == 2){
			var selectedFood = of.newFood.getSelected();
			if(selectedFood){
				if(selectedFood.isHang()){
					selectedFood.setHangup(false);
				}else{
					selectedFood.setHangup(true);
				}
				initNewFoodContent();
			}else{
				Util.msg.tip('请选中一道菜品');
			}
		}
	}
	
	
	var popTastePopup = null;
	/**
	 * 点菜
	 */
	function insertFood(food){
		if(food.isSellout()){
			Util.msg.tip('菜品已停售');
		}else{
			//增加新点菜
			of.newFood.add(food);
			//最新添加的作为选中菜品
			of.selectedOrderFood = of.newFood.getSelected();
			//刷新新点菜
			initNewFoodContent();
			
			if(popTastePopup){
				popTastePopup.close();
			}
			
		
			seajs.use('taste', function(taste){
			
			popTastePopup = taste.newInstance({
				selectedFood : of.selectedOrderFood,
				postTasteChanged : function(){
					initNewFoodContent();
				},
				postUnitClick : function(){
					initNewFoodContent();
				}
			});
				popTastePopup.open();
			});
	
			
			//判断拼音键盘是否显示来清空
			if($("#orderPinyinCmp").is(":visible")){
				$('#pinyinVal_a_orderFood').click();	
				$('#handDel_a_orderFood').click();	
			}
			
			//判断手写键盘是否显示来重写
			if($("#orderHandCmp").is(":visible")){
				$('#rewrite_a_orderFood').click();
				$('#handDel_a_orderFood').click();
			}
		}
	};
	
	 //打开时价
	function openCurrentPriceWin(food){
		var curPricePopup = null;
		curPricePopup = new NumKeyBoardPopup({
			header : '输入时价--' + food.name,
			left : function(){
				var unitPrice = parseFloat($('#input_input_numKbPopup').val());
				//设置时价，深拷贝一次food，以免污染原始数据
				var curPriceFood = $.extend(true, {}, food);
				curPriceFood.unitPrice = unitPrice;
				insertFood(curPriceFood);
				curPricePopup.close();
			},
			right : function(){
				curPricePopup.close();
			}
		});
			
		curPricePopup.open(function(self){
			self.find('[id=input_input_numKbPopup]').val(food.unitPrice);
			self.find('[id=middle_a_numKbPopup]').hide();
			setTimeout(function(){
			//	self.find('[id=input_input_numKbPopup]').select();
				self.find('[id=input_input_numKbPopup]').focus();
				self.find('[id=input_input_numKbPopup]').select();
			}, 200);
		});
	};
	
});




