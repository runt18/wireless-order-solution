function CreateFastOrderFood(param){
	
	var _loadedUrlCache = {};
	var _foodData = null;
	var _orderData = [];   //已点食物
	
	param = param || {
		confirm  : function(selectedFood, container){},
		payType : null
	}
	
	
	this.open = function(afterOpen){
//		if(_loadedUrlCache[_o.loadUrl]){
//			_init(_loadedUrlCache[_o.loadUrl]);
//			_open(afterOpen);
//		}else{
			$('<div/>').load('../order/fastOrderFood.html', function(response, status, xhr){
//				_loadedUrlCache[_o.loadUrl] = response;
				if(xhr.status == '200'){
					var root = $(response);
					$('body').append(root);
					
					root.trigger('create').trigger('refresh');
					
					
					//获取数据
					initKitchen();
					
					//设置开始样式
					window.onresize = setView;
					window.onresize();
					
					//购物车点击
					$(".ocartbox,.maskbox").click(function(){
						updateCart();
						$(".maskbox").fadeToggle(300);
						$(".orderdetail").slideToggle(300);
					});
					
					//选好了
					$('#confirm_div_fastOrderFood').click(function(){
						param.confirm(_orderData);
					});
					
				}else{
//					alert('无法打开页面\r\n' + 'url : ' + _o.loadUrl + ' ,status : ' + xhr.status + ' ,statusText : ' + xhr.statusText);
				}
			});
//		}
			
			
		if(afterOpen && typeof afterOpen == 'function'){
			afterOpen();
		}
	
	}
	
	
	this.close = function(afterClose, timeout){
	
		if(afterClose && typeof afterClose == 'function'){
			if(timeout){
				setTimeout(afterClose, timeout);
			}else{
				afterClose();
			}
		}
	
	}
	
	//加载厨房
	function initKitchen(){
		var ketchenHtml = [];
		var kitchenList = $('#keptList_ul_fastOrderFood');
		var kitchenBox = '<li data-value="{id}" data-type="kitchenBox"><h4>{name}</h4><div class="allnum" id="allnum">0</div></li>';
		$.ajax({
			url : '../../WxQueryDept.do', 
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'kitchen',
				fid : Util.mp.fid,
				oid : Util.mp.oid,
				sessionId : Util.mp.params.sessionId,
				branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
			},
			success : function(data, status, xhr){
				Util.lm.hide();
				if(data.success){
					_kitchenData = data.root;
					if(_kitchenData.length > 0){
						//默认显示加载第一个厨房的菜品数据
						_kitchenId = _kitchenData[0].id;
						_kitchenData.forEach(function(e, index){
							ketchenHtml.push(kitchenBox.format({
								id : e.id,
								name : e.name.substring(0, 4)
							}));
							
						});
					}
					kitchenList.html(ketchenHtml.join(''));
					kitchenList.find('[data-type="kitchenBox"]').each(function(index, element){
						element.onclick = function(){
							filterFood(element);
						}
					});
					kitchenList.find('[data-type="kitchenBox"]')[0].click();
				}else if(data.code == '7546'){
					sessionTimeout();
				}else{
					Util.showErrorMsg(data.msg);
				}
			},
			error : function(req, status, err){
				if(err.code == '7546'){
					sessionTimeout();
				}else{
					Util.showErrorMsg(err.msg);					
				}
			}
		});
	}
	
	//点击厨房的操作
	function filterFood(e){
		if($(e).hasClass('active')){
			return;
		}else{
			$('#keptList_ul_fastOrderFood').find('[data-type="kitchenBox"]').removeClass('active');
			$(e).addClass('active');
		}
		
		initFood($(e).attr('data-value'));		
		
	}
	
	//加载菜品
	function initFood(keptId){
		var foodBox = '<div class="clearfix ptb10 borderbf2 ogood"  data-type={foodId}>'
							+'<div class="col-xs-4 plr10" style="">'
								+'<img src={image} alt="" class="pull-left img-responsive" style="width:74px;height:74px;">'
							+'</div>'
							+'<div class="col-xs-8 clearPadding" style="position:relative;">'
								+'<div>'
									+'<h4 class="font14 mt0" data-type="foodName">{name}</h4>'
									+'<div class="text-muted font10"><font>{foodCnt}</font>人点过</div>'
									+'<p class="text-dining font14 clearMargin">￥<span class="unitprice" data-type="unitprice">{unitPrice}</span><span class="text-muted">&nbsp;/份</span></p>	'
								+'</div>'			
								+'<div class="opmbox clearfix">'
									+'<div class="oplusbox pull-left ominu">'
										+'<i data-value={foodId} data-type="cut" class="icon iconfont text-dining" style="font-size:27px;position:relative;top:-2px;">&#xe608;</i>'
									+'</div>'
									+'<input type="text" class="onum pull-left font13" value={count} readonly="readonly" name="amount">'			
									+'<div class="oplusbox pull-left oplus">'
										+'<i data-value={foodId} data-type="plus" class="icon iconfont text-dining" style="font-size:27px;">&#xe605;</i>'
									+'</div>'
								+'</div>'
							+'</div>'
						+'</div>'
		
		
		var requestParams = {
			fid : Util.mp.fid,
			oid : Util.mp.oid,
			start : 0,
			limit : 20,
			kitchenId : typeof keptId != 'undefined' ? keptId : -1,
			branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : '',
			sessionId : Util.mp.params.sessionId
		};
		
		if(keptId){
			if(keptId == -10){
				//明星菜
				requestParams.dataSource = 'star';
			}else if(keptId == -9){
				//我的最爱
				requestParams.dataSource = 'favor';
			}else if(keptId == -8){
				//向你推荐
				requestParams.dataSource = 'recommend';
			}else{
				requestParams.dataSource = 'normal';
			}
		}
		
		wxLoadDialog.instance().show();
		$.ajax({
			url : '../../WxQueryFood.do',
			dataType : 'json',
			type : 'post',
			data : requestParams,
			success : function(data, status, xhr){
				_foodData = data.root;
				if(data.success){
					if(data.root && data.root.length > 0){
						var foodHtml = [];
						
						var count = null;
						var temp = null;
						for(var i = 0; i < _foodData.length; i++){
							count = getOrderFoodCount(_foodData[i].id);
							
							var noImage = 'noImage.jpg';
							
							foodHtml.push(foodBox.format({
								foodId : _foodData[i].id,
								image : noImage,
								name : (_foodData[i].name.length > 9 ? _foodData[i].name.substring(0, 8) + "…" : _foodData[i].name),
								unitPrice :  (_foodData[i].status & (1 << 4)) != 0 ? '时价' : _foodData[i].unitPrice,
								foodCnt : parseInt(_foodData[i].foodCnt),
								count : count
							}));
							
						} 
						
						$('#foodList_div_fastOrderFood').html(foodHtml.join(''));
						
						//加
						$('#foodList_div_fastOrderFood').find('[data-type="plus"]').each(function(index, element){
							element.onclick = function(){
								foodPlus(element);						
							}
						})
						
						//减
						$('#foodList_div_fastOrderFood').find('[data-type="cut"]').each(function(index, element){
							element.onclick = function(){
								foodCut(element);
							}						
						});
						
					}else{
						$('#foodList_div_fastOrderFood').html('没有记录');
					}
				}else if(data.code == '7546'){
					sessionTimeout();
				}else{
					Util.showErrorMsg(data.msg);
				}
				wxLoadDialog.instance().hide();
			},
			error : function(xhr, status, error){
				if(err.code == '7546'){
					sessionTimeout();
				}else{
					Util.showErrorMsg(err.msg);					
				}
				wxLoadDialog.instance().hide();
			}
		});
		
	}
	
	//菜品加
	function foodPlus(e){
		var _this = $(e);
		var input = _this.parent().prev();
		var foodId =  $(e).attr('data-value');
		var amount = parseInt(input.val());
		var unitPrice = $(e).parent().parent().parent().find('[data-type="unitprice"]').text();
		var name = $(e).parent().parent().parent().find('[data-type="foodName"]').text();
		amount++;
		input.val(amount);
		operateFood(foodId, amount);
		changeOrderAmount();
	}
	
	//菜品减少
	function foodCut(e){
		var _this = $(e);
		var input = _this.parent().next();
		var foodId =  $(e).attr('data-value');
		var amount=parseInt(input.val());
		
		amount--;
		if(amount < 0){
			input.val(0);
		}else{
			input.val(amount);
		}
		
		operateFood(foodId, amount);
		changeOrderAmount();
	}
	
	function getOrderFoodCount(id){
		var count = 0 ;
		for(var i = 0; i < _orderData.length; i++){
			if(_orderData[i].food.id == id){
				count = _orderData[i].count;
			}
		}
		return count;
	}
	
	
	//更新购物车
	function updateCart(){
		if(_orderData.length > 0){
			var cartFoodTemplate = '<div class="clearfix borderbottom ptb10 plr15 cartgoods" foodId = {id}>\
			      <div class="col-xs-7 clearPadding">{name}</div>\
			      <div class="col-xs-5 clearPadding text-right">\
			        <div class="pmbox plus pull-right"><i class="icon iconfont text-dining" foodId = {id} data-type="cartAdd" style="font-size:24px;">&#xe605;</i></div>\
			        <div class="numbox Padding5 pull-right">{amount}</div>\
			        <div class="pmbox minu pull-right"><i class="icon iconfont text-dining" foodId = {id} data-type="cartCut" style="font-size:24px;position:relative;top:-2px;">&#xe608;</i></div>\
			        <div class="pricebox pull-right mr15 lh24 text-dining">￥<span>{totalPrice}</span></div>\
			      </div>\
			    </div>';
			    
			var html = [];
		    for(var i = 0; i < _orderData.length; i++){
				html.push(cartFoodTemplate.format({
					id : _orderData[i].food.id,
					name : _orderData[i].food.name,
					amount : _orderData[i].count,
					totalPrice : _orderData[i].food.unitPrice * _orderData[i].count
				}));	    
		    }
		    $('#orderdetail').html(html.join(''));
		    
		    //购物车里面的加
		    $('#orderdetail').find('[data-type="cartAdd"]').each(function(index, element){
		    	element.onclick = function(){
		    		var input = $(element).parent().next();
					var amount = parseInt(input.text());
					amount++;
					input.text(amount);
					operateFood($(element).attr('foodId'), amount);
					changeOrderAmount();
					updateCart();
		    	}
		    });
		    
		    //购物车里面的减
		    $('#orderdetail').find('[data-type="cartCut"]').each(function(index, element){
		    	element.onclick = function(){
		    		var input = $(element).parent().prev();
					var amount = parseInt(input.text());
					amount--;
					input.text(amount);
					operateFood($(element).attr('foodId'), amount);
					changeOrderAmount();		
					updateCart();
		    	}
		    });
		}else{
			$('#orderdetail').html('');
		}
		
		
	}
	
	function operateFood(id, num){
		var cartFood = null;
		for(var i = 0; i < _orderData.length; i++){
			if(_orderData[i].food.id == id){
				cartFood = _orderData[i];
				break;
			}
		}
		
		if(cartFood){
			if(num == 0){
				_orderData.splice(i, 1);
			}else{
				cartFood.count = num;
			}
			
		}else{
			for(var i = 0; i < _foodData.length; i++){
				if(_foodData[i].id == id){
					_orderData.push({
						food : _foodData[i],
						count : 1
					});
					break;
				}
			}
		}
	}
	
	
	function changeOrderAmount(){ 
		var count = 0;
		var allPrice = 0;
		for(var i = 0; i < _orderData.length; i++){
			count += _orderData[i].count;
			allPrice += _orderData[i].food.unitPrice * _orderData[i].count;
		}
		$('#allnum').html(count);
		$('#allprice').html(allPrice);
	}
	
	//设置样式
	function setView(){
		var width = document.documentElement.clientWidth;
		var height = document.documentElement.clientHeight;
		
		$('#keptList_ul_fastOrderFood').css('height', height - 55);
		$('#foodList_div_fastOrderFood').css('height', height - 55);
		$('#allproduct').css('height', height - 55);
		$('.maskbox').css('height', height);
		
		$(".orderdetail").hide();
	}
	
	//超时提醒
	function sessionTimeout(){
		var sessionTimeoutPopup;
		sessionTimeoutPopup = new WeDialogPopup({
			titleText : '温磬提示',
			content : ('<span style="display:block;text-align:center;">链接已过期,请重新扫码</span>'),
			leftText : '确认',
			left : function(){
				sessionTimeoutPopup.close();
			},
			afterClose : function(){
				wx.closeWindow();
			}
		});
		sessionTimeoutPopup.open();
	}
}	
