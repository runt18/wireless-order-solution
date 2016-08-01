function CreateFastOrderFood(param){
	
	var _loadedUrlCache = {};
	var _foodData = null;
	var _orderData = [];   //已点食物
	
	param = param || {
		confirm  : function(selectedFood, container){},     //选好了的回调事件
		confirmText : null,                                 //选好了的文字
		onCartChange : function(selectFood){}               //购物车变动的回调事件
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
						if(_orderData.length == 0){
							wxLoadDialog.success('购物车没有菜品').show();
							setTimeout(function(){
								wxLoadDialog.success().hide();
							}, 800);
						}else{
							updateCart();
							$(".maskbox").fadeToggle(300);
							$(".orderdetail").slideToggle(300);
						}
						
					});
					
					//选好了
					$('#confirm_div_fastOrderFood').click(function(){
						if(param.confirm && typeof param.confirm == 'function'){
							param.confirm(_orderData);
						}
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
		var kitchenBox = '<li data-value="{id}" data-type="kitchenBox"><div class="allnum" id="kitchenNum_div_food">0</div>' +
				'<h4>{name}</h4></li>';
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
					changeKitchenAmount();
					
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
		var foodBox = '<div class="clearfix ptb10 borderbf2 ogood" data-type="eachFood_div_food"  data-value={foodId}>'
							+'<div class="col-xs-4 plr10" style="">'
								+'<img src={image} alt="" class="pull-left img-responsive" style="width:74px;height:74px;">'
							+'</div>'
							+'<div class="col-xs-8 clearPadding" style="position:relative;">'
								+'<div>'
									+'<h4 class="font14 mt0" data-type="foodName">{name}</h4>'
									+'<div class="text-muted font10"><font>{foodCnt}</font>人点过</div>'
									+'<p class="text-dining font14 clearMargin">￥<span class="unitprice" data-type="foodPrice">{unitPrice}</span></p>	'
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
							+ '<div data-type="standard_div_food" style="width:100%;" class="box-horizontal">{unitPriceHtml}</div>'
						+'</div>'
		
		var unitPrice = '<div multiUnit-Id={multiId} class="unit_css_fastOrderFood" data-value={foodId} data-type="unitPrice" href="#">'
						+ '<ul class="m-b-list" style="margin-top:10px;margin-left:-2px;font-size:12px;"><a data-type="unitPrice_a_food">{unitPrice}</a>&nbsp;/&nbsp;{unitName}</ul>'
						+ '</div>';
						
						
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
							
							var multiUnit = [];	
							if(_foodData[i].multiUnitPrice.length > 0){
								_foodData[i].multiUnitPrice.forEach(function(e, index){
									multiUnit.push(unitPrice.format({
										multiId : e.id,
										unitPrice : e.price,
										unitName : e.unit,
										foodId : _foodData[i].id
									}));
								});
							}
							
							var noImage = 'noImage.jpg';
							
							foodHtml.push(foodBox.format({
								foodId : _foodData[i].id,
								image : noImage,
								name : (_foodData[i].name.length > 9 ? _foodData[i].name.substring(0, 8) + "…" : _foodData[i].name),
								unitPrice :  (_foodData[i].status & (1 << 4)) != 0 ? '时价' : _foodData[i].unitPrice,
								foodCnt : parseInt(_foodData[i].foodCnt),
								count : count,
								unitPriceHtml : multiUnit.slice(0, 3).join('')
							}));
							
						} 
						
						$('#foodList_div_fastOrderFood').html(foodHtml.join(''));
						
						
						//多规格的点击事件
						$('#foodList_div_fastOrderFood').find('[data-type="eachFood_div_food"]').each(function(index, element){
							$(element).find('[data-type="unitPrice"]').each(function(index, unitPriceElement){
								unitPriceElement.onclick = function(){
									
									if($(unitPriceElement).hasClass('selectUnitPrice_css_fastOrderFood')){
										$(unitPriceElement).addClass('selectUnitPrice_css_fastOrderFood');
									}else{
										$(element).find('[data-type="unitPrice"]').removeClass('selectUnitPrice_css_fastOrderFood');
										$(unitPriceElement).addClass('selectUnitPrice_css_fastOrderFood');
										$(unitPriceElement).parent().parent().find('[data-type="foodPrice"]').html(parseInt($(unitPriceElement).find('[data-type="unitPrice_a_food"]').text()));
									}
									
									if(_orderData.length > 0){
										for(var i = 0; i < _orderData.length; i++){
											if($(unitPriceElement).attr('data-value') == _orderData[i].food.id){
												_orderData[i].food.unitPrice = parseInt($(unitPriceElement).find('[data-type="unitPrice_a_food"]').text());
												
												for(var k = 0; k < _orderData[i].food.multiUnitPrice.length; k++){
													if($(unitPriceElement).attr('multiUnit-Id') == _orderData[i].food.multiUnitPrice[k].id){
														_orderData[i].selectedUnitPrice = _orderData[i].food.multiUnitPrice[k];
													}
												}
											}else{
												for(var i = 0; i < _foodData.length; i++){
													if($(unitPriceElement).attr('data-value') == _foodData[i].id){
														_foodData[i].unitPrice = parseInt($(unitPriceElement).find('[data-type="unitPrice_a_food"]').text());
													}
												}
											}
										}
									}else{
										for(var i = 0; i < _foodData.length; i++){
											if($(unitPriceElement).attr('data-value') == _foodData[i].id){
												_foodData[i].unitPrice = parseInt($(unitPriceElement).find('[data-type="unitPrice_a_food"]').text());
											}
										}
									}
									
									changeOrderAmount();
								}
							});
						});

						$('#foodList_div_fastOrderFood').find('[data-type="eachFood_div_food"]').find('[data-type="unitPrice"]:first').click();
						
						//加
						$('#foodList_div_fastOrderFood').find('[data-type="plus"]').each(function(index, element){
							element.onclick = function(){
								var selectedUnitPrice = null;
								$(element).parent().parent().parent().parent().find('[data-type="unitPrice"]').each(function(index, unitPriceElement){
									if($(unitPriceElement).hasClass('selectUnitPrice_css_fastOrderFood')){
										selectedUnitPrice = $(unitPriceElement).attr('multiUnit-Id')
									}
								});
								
								foodPlus(element, selectedUnitPrice);						
							}
						})
						
						//减
						$('#foodList_div_fastOrderFood').find('[data-type="cut"]').each(function(index, element){
							element.onclick = function(){
								var selectedUnitPrice = null;
								$(element).parent().parent().parent().parent().find('[data-type="unitPrice"]').each(function(index, unitPriceElement){
									if($(unitPriceElement).hasClass('selectUnitPrice_css_fastOrderFood')){
										selectedUnitPrice = $(unitPriceElement).attr('multiUnit-Id')
									}
								});
								foodCut(element, selectedUnitPrice);
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
	function foodPlus(e, selecedUnitPrice){
		var _this = $(e);
		var input = _this.parent().prev();
		var foodId =  $(e).attr('data-value');
		var amount = parseInt(input.val());
		var unitPriceId = 
		
		amount++;
		input.val(amount);
		operateFood(foodId, amount, selecedUnitPrice);
		changeOrderAmount();
		changeKitchenAmount();
	}
	
	//菜品减少
	function foodCut(e, selecedUnitPrice){
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
		
		operateFood(foodId, amount, selecedUnitPrice);
		changeOrderAmount();
		changeKitchenAmount();
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
		    	var unitPriceName = null;
		    	if(_orderData[i].selectedUnitPrice){
		    		unitPriceName = _orderData[i].selectedUnitPrice.price + '/' + _orderData[i].selectedUnitPrice.unit;
		    	}
		    	
				html.push(cartFoodTemplate.format({
					id : _orderData[i].food.id,
					name : unitPriceName ? _orderData[i].food.name + '(' + unitPriceName + ')' : _orderData[i].food.name,
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
					changeKitchenAmount();
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
					changeKitchenAmount();
					updateCart();
					if(_orderData.length == 0){
						$(".maskbox").fadeToggle(300);
						$(".orderdetail").slideToggle(300);
					}
					
		    	}
		    });
		}else{
			$('#orderdetail').html('');
		}
		
		
	}
	
	function operateFood(id, num, selecedUnitPrice){
		var cartFood = null;
		var unitPrice = null; 
		for(var i = 0; i < _orderData.length; i++){
			if(_orderData[i].food.id == id){
				cartFood = _orderData[i];
				break;
			}
		}
		
		for(var j = 0; j < _foodData.length; j++){
			for(var k = 0; k < _foodData[j].multiUnitPrice.length; k++){
				if(selecedUnitPrice == _foodData[j].multiUnitPrice[k].id){
					unitPrice = _foodData[j].multiUnitPrice[k];
				}
			}
		}
		
		
		if(cartFood){
			if(num == 0){
				_orderData.splice(i, 1);
			}else{
				cartFood.count = num;
				cartFood.selectedUnitPrice = unitPrice;
			}
			
		}else{
			for(var i = 0; i < _foodData.length; i++){
				if(_foodData[i].id == id){
					_orderData.push({
						food : _foodData[i],
						count : 1,
						selectedUnitPrice : unitPrice
					});
					break;
				}
			}
		}
		if(param.onCartChange){
			param.onCartChange(_orderData);
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
		$('#allprice').html(allPrice.toFixed(2));
	}
	
	function changeKitchenAmount(){
		 var kitchen = $('#keptList_ul_fastOrderFood').find('[data-type="kitchenBox"]');
		 for(var i = 0; i < kitchen.length; i++){
			 var selectFoods = [];
			  var allCount = 0;
			 for(var j = 0; j < _orderData.length; j++){
				 if($(kitchen[i]).attr('data-value') == _orderData[j].food.kitchen.id){
					 selectFoods.push(_orderData[j]);
					 allCount += _orderData[j].count;
				 }
			 }
			 if(selectFoods.length > 0 ){
				 $(kitchen[i]).find('[id="kitchenNum_div_food"]').html('&nbsp' + allCount);
				 $(kitchen[i]).find('[id="kitchenNum_div_food"]').show();
			 }else{
				 $(kitchen[i]).find('[id="kitchenNum_div_food"]').html('');
			 	$(kitchen[i]).find('[id="kitchenNum_div_food"]').hide();
			 }

		 }
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
