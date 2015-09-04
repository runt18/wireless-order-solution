	var params = {
		foodData : [],
		orderData : [],
		dataSource : 'normal',
		isPaging : false,//默认不分页
		start : 0,
		limit : 10,
		kitchenData : {},
		kitchenId : -1,
		DNSC : 'divNavKitchen-item-select'
	};
	var shopCartInit = false;
	//购物车菜品总信息和下导航栏总高度 && 购物车每行菜品高度
	var generalHeight = 87, foodHeight = 51;
	//网页可见高度
	var htmlHeight;
	
	var isTakeout = false, to={};
	
	if(Util.mp.extra && Util.mp.extra==3){
		isTakeout = true;
		to={
				useNewAddress : false,
				defaultAddress : '',
				member : {},
				customerContects : []
		};		
	}


var Templet = {
	foodBox : '<div class="box-horizontal box-food-list">'
		+ '<div data-r="l">'
			+ '<div onclick="foodShowAbout({id:{id}, event:this, otype:\'show\'})" style="background-image: url({img});"></div>'
		+ '</div>'
		+ '<div data-r="c" class="box-food-list-c" onclick="fireOperateFood(this)">'
			+ '<div data-r="t"><b>{name}</b></div>'
			+ '<div data-r="m"><span>￥{unitPrice}</span></div>'
			+ '<div data-r="b" class={orderAction}><font>{foodCnt}</font>人点过</div>'
		+ '</div>'
		+ '<div data-r="r" class="box-horizontal box-food-list-r">'
			//+ '<div data-r="l" data-type="count" data-value="{id}" style="display:{display}">{count}</div>'
			//+ '<div data-r="r" onclick="operateFood({otype:\'add\', id:{id}, event:this})">+</div>'
			+ '<div data-r="r" {selected} data-value="{id}" onclick="operateFood({otype:\'add\', id:{id}, event:this})"></div>'
		+ '</div>'
		+ '</div>',
	shoppingBox : '<div data-value="{id}" class="div-fl-f-sc-box box-horizontal">'
		+ '<div data-type="msg" class="div-full">'
			+ '<div><b>{name}</b></div>'
			+ '<div>价格: <span>￥{unitPrice}</span></div>'
		+ '</div>'
		+ '<div data-type="cut" onclick="operateFood({otype:\'cut\', id:{id}, event:this})">-</div>'
		+ '<div data-type="count">{count}</div>'
		+ '<div data-type="plus" onclick="operateFood({otype:\'plus\', id:{id}, event:this})">+</div>'
		+ '</div>',
	deptBox : '<div data-value="{id}" onclick="filtersKitchen(this)">{name}</div>',
	kitchenBox2 : '<div data-value="{id}" onclick="filtersFood(this)">{name}</div>',
	kitchenBox : '<li><a data-value="{id}" onclick="filtersFood2(this)" class={star}>{name}</a></li>',
	contectBox : '<div data-value={id} class="takeout_address_added {hidden}" onclick="selectAddress(this)">'+ 
					'<div class="added_left">'+
						'<div style="float: left;width: 50%">收货人 : {name}</div>'+
						'<div style="float: right;width: 50%">{phone}</div>'+
						'<div >地址: {address}</div>'+
					'</div>'+
						'<div class="added_right">'+
						'<i class="foundicon-checkmark"></i>'+
				 	'</div>'+
				 '</div>'
};

function changeImg(e){
	if($(e).hasClass("select-food")){
		$(e).removeClass("select-food");
		return false;
	}else{
		$(e).addClass("select-food");
		return true;
	}
}

function fireOperateFood(thiz){
	operateFood({otype:'add', id:$($(thiz).next().find('div[data-r=r]')[0]).attr('data-value'), event:$(thiz).next().find('div[data-r=r]')[0]});
}


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
	//当点击的是明星菜时, 调用别的方法
	if(params.kitchenId && params.kitchenId == -10){
		requestParams.dataSource = 'isRecommend';
	}
	
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
						img : temp.img.thumbnail,
						name : (temp.name.length > 7? temp.name.substring(0,6)+"…" : temp.name),
						unitPrice : temp.unitPrice,
						foodCnt : parseInt(temp.foodCnt),
						count : count,
						selected : count > 0 ? 'class="select-food"' : '',
						display : count > 0 ? 'block' : 'none',
						orderAction : temp.foodCnt > 0 ? '' : 'html-hide'
					}));
				}
				count = null;
				Util.getDom('divOperateFoodPaging').insertAdjacentHTML('beforeBegin', html.join(''));
//				Util.getDom('divOperateFoodPaging').innerHTML = '点击加载更多.';
			}else{
				Util.getDom('divOperateFoodPaging').innerHTML = '没有记录.';
			}
			if(typeof c.callback == 'function'){
				c.callback(data);
			}
		},
		error : function(xhr, errorType, error){
			Util.lm.hide();
			Util.dialog.show({ msg : '加载菜品信息失败.' });
		}
	});
}

function initDeptData(){
	Util.lm.show();
	$.ajax({
		url : '../../WXQueryDept.do',
		dataType : 'json',
		type : 'post',
		data : {
			dataSource : 'kitchen',
			fid : Util.mp.params.r
		},
		success : function(data, status, xhr){
			Util.lm.hide();
			params.kitchenData = data.root;
			var temp, html = [], kitchenList = $('#ulKitchenList');
			if(params.kitchenData.length > 0){
				//html.push('<li><a data-value="-1" class="li-k-title" onclick="filtersFood2(this)">全部厨房</a></li>');
				
				//默认显示第一个厨房的菜品
				params.kitchenId = params.kitchenData[0].id; 
				initFoodData();
				
				for(var i = 0; i < params.kitchenData.length; i++){
					temp = params.kitchenData[i];
					html.push(Templet.kitchenBox.format({
						id : temp.id,
						//如果是明星菜则用橙色背景, 否则第一个默认选中用灰色背景
						star : temp.id == -10 ? 'star-kitchen-name' : (i == 0 ?'divNavKitchen-item-select':''),
						name : temp.name.substring(0, 4)
					}));
				}
			}
			kitchenList.html(html.join(''));
			temp = null;
			html = null;			
		},
		error : function(xhr, errorType, error){
			Util.lm.hide();
			Util.dialog.show({ msg : '加载分厨信息失败.' });
		}
	});
}

$(function(){
	window.onresize = initView;
	window.onresize();
	
	initEvent();
	
	//获取厨房后再获取菜品
	initDeptData();
	
    $(document).bind('click',function(e){
    	var e = e || window.event; //浏览器兼容性
        var elem = e.target || e.srcElement;
        while (elem) { //循环判断至跟节点，防止点击的是div子元素
            if ((elem.id && elem.id=='divShoppingCart') || elem.className=='dialog') {
                return;
            }
            elem = elem.parentNode;
        }
		operateShoppingCart({event:this, otype:'hide'});
    });	
    
    //网页可见高度
    htmlHeight = document.body.clientHeight;
    
    //固定外卖页面的高度
    $('#divTakeoutDetailBox').height(htmlHeight - 45);
    
    //外卖模式则加载会员信息
    if(isTakeout){
    	$('#food_order').text("我的外卖");
    	$.post('../../WXOperateMember.do', {dataSource:'getInfo', oid:Util.mp.oid, fid: Util.mp.fid}, function(result){
    		to.member = result.other.member;
	    	$('#to_name').val(to.member.name);
	    	$('#to_phone').val(to.member.mobile); 		
    	});
    	
    	$.post('../../WXQueryAddress.do', {oid:Util.mp.oid, fid: Util.mp.fid}, function(result){
    		if(result.success){
    			if(result.root.length > 0){
    				to.customerContects = result.root; 
    				for (var i = 0; i < to.customerContects.length; i++) {
						if(i == 0){
							to.customerContects[i].isDefault = true;
						}else{
							to.customerContects[i].isDefault = false;
						}
					}
    				loadCustomerAddress();
    			}else{
    		    	$('#divNewAddress4TO').show();
    			}
    		}
    	});
    	
    }
});
