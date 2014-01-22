var Templet = {
	foodBox : '<div class="box-horizontal box-food-list">'
		+ '<div data-r="l">'
			+ '<div onclick="foodShowAbout({id:{id}, event:this, otype:\'show\'})" style="background-image: url({img});"></div>'
		+ '</div>'
		+ '<div data-r="c" class="box-food-list-c">'
			+ '<div data-r="t"><b>{name}</b></div>'
			+ '<div data-r="m">价格: <span>￥{unitPrice}</span></div>'
			+ '<div data-r="b">积分: <font>{point}</font></div>'
		+ '</div>'
		+ '<div data-r="r" class="box-horizontal box-food-list-r">'
			+ '<div data-r="l" data-type="count" data-value="{id}" style="display:{display}">{count}</div>'
			+ '<div data-r="r" onclick="operateFood({otype:\'add\', id:{id}, event:this})">+</div>'
		+ '</div>'
		+ '</div>',
	shoppingBox : '<div data-value="{id}" class="div-fl-f-sc-box box-horizontal">'
		+ '<div data-type="msg" class="div-full">'
			+ '<div><b>{name}</b></div>'
			+ '<div>价格: <span>￥{unitPrice}</span></div>'
		+ '</div>'
		+ '<div data-type="cut" onclick="operateFood({otype:\'cut\', id:{id}, event:this})">-</div>'
		+ '<div data-type="add" onclick="operateFood({otype:\'add\', id:{id}, event:this})">+</div>'
		+ '<div data-type="count">{count}</div>'
		+ '</div>',
	deptBox : '<div data-value="{id}" onclick="filtersKitchen(this)">{name}</div>',
	kitchenBox : '<div data-value="{id}" onclick="filtersFood(this)">{name}</div>'
};

function initView(){
	var height = document.documentElement.clientHeight;
	$('#divMainView').css('height', height);
	$('#divShoppingCart').css('height', height);
	$('#divFoodShowAbout').css('height', height);
}

function initEvent(){
	Util.getDom('divNavKitchen-mask').onclick = function(){operateKitchenSearch({event:this, otype:'hide'}); };
//	if(params.isLinux === true){
//		$.dom('divNavKitchen-mask').ontouchstart = function(){operateKitchenSearch({event:this, otype:'hide'}); };
//	}else if(params.isWindow === true){
//		$.dom('divNavKitchen-mask').onclick = function(){ operateKitchenSearch({event:this, otype:'hide'}); };
//	}
}
/**
 * 
 */
function getOrderFoodCount(id){
	var count = 0 ;
	for(var i = 0; i < params.orderData.length; i++){
		if(params.orderData[i].id == id){
			count = params.orderData[i].count;
		}
	}
	return count;
}
/**
 * 
 * @param c
 */
function initFoodData(c){
	c = c == null ? {} : c;
	var requestParams = {
		dataSource : params.dataSource,
		fid : Util.mp.fid,
		kitchenId : typeof c.kitchenAlias != 'undefined' ? c.kitchenId : params.kitchenId,
		isPaging : params.isPaging,
		start : typeof c.start != 'undefined' ? c.start : params.start,
		limit : typeof c.limit != 'undefined' ? c.limit : params.limit
	};
	Util.lm.show();
	$.ajax({
		url : '../../WXQueryFood.do',
		dataType : 'json',
		type : 'post',
		data : requestParams,
		success : function(data, status, xhr){
			Util.lm.hide();
			if(requestParams.start == 0 && requestParams.limit == 10){
				params.foodData = data.root;
			}else{
				params.foodData = params.foodData.concat(data.root);
			}
			if(data.root.length > 0){
				var html = [], count = 0;
				for(var i = 0; i < data.root.length; i++){
					var temp = data.root[i];
					count = getOrderFoodCount(temp.id);
					html.push(Templet.foodBox.format({
						id : temp.id,
						img : temp.img,
						name : temp.name,
						unitPrice : temp.unitPrice.toFixed(2),
						point : parseInt(temp.unitPrice),
						count : count,
						display : count > 0 ? 'block' : 'none'
					}));
				}
				count = null;
				Util.getDom('divOperateFoodPaging').insertAdjacentHTML('beforeBegin', html.join(''));
				Util.getDom('divOperateFoodPaging').innerHTML = '点击加载更多.';
			}else{
				Util.getDom('divOperateFoodPaging').innerHTML = '没有记录.';
			}
			if(typeof c.callback == 'function'){
				c.callback(data);
			}
		},
		error : function(xhr, errorType, error){
			Util.lm.hide();
			alert('加载菜品信息失败.');
		}
	});
}

function initDeptData(){
	$.ajax({
		url : '../../WXQueryDept.do',
		dataType : 'json',
		type : 'post',
		data : {
			dataSource : 'kitchen',
			fid : Util.getParam('r')
		},
		success : function(data, status, xhr){
			params.kitchenData = data.root;
		},
		error : function(xhr, errorType, error){
			alert('加载分厨信息失败.');
		}
	});
}

$(function(){
	window.onresize = initView;
	window.onresize();
	
	initEvent();
	
	initFoodData();
	
	initDeptData();
});
