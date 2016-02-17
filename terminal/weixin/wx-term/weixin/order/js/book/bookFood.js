$(function(){
	var bookDates = [];
	
	//加载会员信息
	(function loadBookMember(){
		$.post('../../WXOperateMember.do', {
			dataSource : 'getInfo',
			oid : Util.mp.oid,
			fid : Util.mp.fid
		}, function(data){
			if(data.success){
				var member = data.other.member;
				$('#txtBookName_input_book').val(member.name);
				$('#txtBookPhone_input_book').val(member.mobile);
			}
		}, 'json');
	})();
	
	//加载日期
	(function loadBookDate(){
	    var Week = ['日','一','二','三','四','五','六'];  
		var now = new Date();
		var month = now.getMonth() + 1;
		
		var today = month + "-" + now.getDate() + "周" + Week[now.getDay()];  
		bookDates.push(now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate());
		
		now.setDate(now.getDate() + 1);
		var tomorrow = (now.getMonth() + 1) + "-" + now.getDate() + "周" + Week[now.getDay()];
		bookDates.push(now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate());

		now.setDate(now.getDate() + 1);
		var afterday = (now.getMonth() + 1) + "-" + now.getDate() + "周" + Week[now.getDay()];
		bookDates.push(now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate());

		
		$('#bookDate4AfterDay_div_book').html('<div class="bookDateDetail" data-type="bookTime" data-value=2>后天<br>'+ afterday +'</div>');
		$('#bookDate4Today_div_book').html('<div class="bookDateDetail bookDateCheck" data-type="bookTime" data-value=0>今天<br>'+ today +'</div>');
		$('#bookDate4Tomorrow_div_book').html('<div class="bookDateDetail" data-type="bookTime" data-value=1>明天<br>'+ tomorrow +'</div>');	
		
		//预订时间
		$('#bookTime_div_book').find('[data-type="bookTime"]').each(function(index, element){
			element.onclick = function(){
				if($(element).hasClass('bookDateCheck')){
					$(element).addClass('bookDateCheck');
				}else{
					$('#bookTime_div_book').find('[data-type="bookTime"]').removeClass('bookDateCheck');
					$(element).addClass('bookDateCheck');
				}
			}
		});
	})();
	
	//加载区域
	(function loadRegions(){
		var regions;
		$.post('../../WxOperateBook.do', {
			fid : Util.mp.fid,
			dataSource : 'region',
			branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
		}, function(data){
			if(data.success){
				regions = data.root;
				var html = ['<li style="line-height: 40px;">选择座位</li>'];
				//部分区域
				var partialregions = [];
				//下表
				var index = [];
				
				//i % 3 = 0 的下标
				for (var i = 0; i < regions.length; i++) {
					 if(i % 3 == 0){
						 index.push(i);
					 }
				}
				//除掉数组第一位的0
				index.splice(0, 1);
				if(index.length > 0){
					for(var j = 0; j < index.length; j++){
						partialregions = regions.slice(index[j]-3, index[j]);
							 var li = '';
							 var bookRegion= '';
							 
							 
						 for(var i = 0; i < partialregions.length; i++){
							 bookRegionTemplate = '<div style="width:30%;" data-type="bookRegions" data-value="' + partialregions[i].name + '" class="region_css_book" href="#">'
												+'<ul class="m-b-list">'+ partialregions[i].name.slice(0, 4) +  '</ul>'
												+'</div>'
							bookRegion += bookRegionTemplate;
					 		li = '<li class="box-horizontal">' + bookRegion +  '</li>';
						}
						 $('#ul4BooskRegion_ul_book').append(li);
						 $('#ul4BooskRegion_ul_book').trigger('create').trigger('refresh');
					 }
				
					if((regions.length - index[index.length-1]) > 0){
						partialregions = regions.slice(index[index.length-1], regions.length);
						 var li = '';
						 var bookRegion= '';
						 for(var i = 0; i < partialregions.length; i++){
							 var bookRegionTemplate = '<div style="width:30%;" data-type="bookRegions" data-value="' + partialregions[i].name + '" class="region_css_book" href="#">'
													+'<ul class="m-b-list">'+ partialregions[i].name.slice(0, 4) +  '</ul>'
													+'</div>'
							 bookRegion += bookRegionTemplate;
							 li = '<li class="box-horizontal">' + bookRegion +  '</li>';
						}
						 $('#ul4BooskRegion_ul_book').append(li);
						 $('#ul4BooskRegion_ul_book').trigger('create').trigger('refresh');
					}
				}else{
					 var li = '';
					 var bookRegion= '';
					 for(var i = 0; i < regions.length; i++){
						 var bookRegionTemplate = '<div style="width:30%;" data-type="bookRegions" data-value="' + regions[i].name + '" class="region_css_book" href="#">'
												+'<ul class="m-b-list">'+ regions[i].name.slice(0, 4) +  '</ul>'
												+'</div>'
						 bookRegion += bookRegionTemplate;
						 li = '<li class="box-horizontal">' + bookRegion +  '</li>';
					}
					$('#ul4BooskRegion_ul_book').append(li);
					$('#ul4BooskRegion_ul_book').trigger('create').trigger('refresh');
				}
				
				
				//预订区域的点击事件
				$('#ul4BooskRegion_ul_book').find('[data-type="bookRegions"]').each(function(index, element){
					$('#ul4BooskRegion_ul_book').find('[data-type="bookRegions"]:first').addClass('selectedRegion_css_book');
					element.onclick = function(){
						if($(element).hasClass('selectedRegion_css_book')){
							$(element).addClass('selectedRegion_css_book');
						}else{
							$('#ul4BooskRegion_ul_book').find('[data-type="bookRegions"]').removeClass('selectedRegion_css_book');
							$(element).addClass('selectedRegion_css_book');
						}
					}
				});
				
				
				//预订区域 & 类型的点击事件
				$('.bookRadioSelect').on('click', function(){
					$('#' + $(this).data("for")).click();
				});
			}
		}, 'json');		
	})();
	
	
	//预订提交
	$('#commit_a_book').click(function(){
		var date, time, region, name, phone, count, pay;
		$('.bookDateDetail').each(function(){
			if($(this).hasClass("bookDateCheck")){
				date = bookDates[$(this).data('value')];
			}
		});

		
		//预订时间
		$('#selectBookTime_ul_book').find('[data-type="time"]').each(function(index, element){
			if($(element).hasClass('selectedRegion_css_book')){
				time = $(element).attr('data-value') + ':' + "00";
			}
		});
		
		
		//预订区域
		$('#ul4BooskRegion_ul_book').find('[data-type="bookRegions"]').each(function(index, element){
			if($(element).hasClass('selectedRegion_css_book')){
				region = $(element).attr('data-value');
			}
		});
		
		//预订人数
		$('#bookPersonAmoung_div_book').find('[data-type="personAmount"]').each(function(index, element){
			if($(element).hasClass('selectedRegion_css_book')){
				count = parseInt($(element).attr('data-value'));
			}
		});
		
		//支付方式
		$('#payMethod_div_book').find('[data-type="payMethod"]').each(function(index, element){
			if($(element).hasClass('selectedRegion_css_book')){
				if($(element).attr('data-value') == 'pay'){
					pay = false;
				}else{
					pay = true;
				}
			}
		});
		
		name = $('#txtBookName_input_book').val();
		phone = $('#txtBookPhone_input_book').val();
		
		if(!name){
			var dialog = new DialogPopup({
					content : '请填写姓名',
					titleText : '温馨提示',
					left : function(){
						dialog.close();
					}
				})
			dialog.open();
			return;
		}	
		if(!phone){
			var dialog = new DialogPopup({
				content : '请填写电话',
				titleText : '温馨提示',
				left : function(){
					dialog.close();
				}
			})
			dialog.open();
			return;
		}	
		
		var foods = "";
		if($('#bookTypeOnlyTable_input_book').attr('checked') == 'checked'){
			foods = "";
		}else{
			if(bookFoods.length < 1){
				var dialog = new DialogPopup({
					content : '请选择菜品',
					titleText : '温馨提示',
					left : function(){
						dialog.close(function(){
							$('#bookFood_li_book').click();
						}, 200);
					}
				})
				dialog.open();
				return;
			}else{
				for(var i = 0; i < bookFoods.length; i++){
					temp = bookFoods[i];
					if(i > 0){
						foods += '&';
					}
					foods += (temp.id + ',' + temp.count);
				}
			}
		}
		Util.lm.show();	
		$.post('../../WxOperateBook.do', {
			fid : Util.mp.fid,
			oid : Util.mp.oid,
			dataSource : 'insert',
			bookDate : date + " " + time,
			member : name,
			phone : phone,
			count : count,
			region : region,
			foods : foods,
			wxPay : pay,
			branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
		}, function(data){
			Util.lm.hide();	
			if(data.success){
				Util.lm.hide();
				var bookId = data.root[0].bookId;
				
				if(data.root[0].wxPay){
					//微信支付预订金额
					$.post('../../WxOperateBook.do', {
						bookId : bookId,
						fid : Util.mp.fid,
						oid : Util.mp.oid,
						dataSource : 'wxPay',
						branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
					}, function(result){
						if(result.success){
							payParam = result.other;
							if (typeof WeixinJSBridge == "undefined") {
								if (document.addEventListener) {
									document.addEventListener('WeixinJSBridgeReady', onBridgeReady,	false);
								} else if (document.attachEvent) {
									document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
									document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
								}
							} else {
								onBridgeReady();
							}
							var dialog = new DialogPopup({
								content : '预订成功',
								titleText : '温馨提示',
								left : function(){
									dialog.close(function(){
										Util.jump('orderList.html?book=1', typeof Util.mp.extra != 'undefined' ? Util.mp.extra : '');
									}, 200);
								}
							})
							dialog.open();
						}else{
							payParam = null;
							var dialog = new DialogPopup({
								content : result.msg,
								titleText : '微信支付失败',
								left : function(){
									dialog.close();
								}
							})
							dialog.open();
						} 
					}, 'json');
				}else{
					var dialog = new DialogPopup({
						content : '预订成功',
						titleText : '温馨提示',
						left : function(){
							dialog.close(function(){
								Util.jump('orderList.html?book=1', typeof Util.mp.extra != 'undefined' ? Util.mp.extra : '');
							}, 200);
						}
					})
					dialog.open();
				}
			}
		}, 'json');
	});
	
	//微信支付的参数
	var payParam = null;
	//微信支付回调函数
	function onBridgeReady(){
		if(payParam){
			WeixinJSBridge.invoke('getBrandWCPayRequest', {
				// 以下参数的值由BCPayByChannel方法返回来的数据填入即可
				"appId" : payParam.appId,
				"timeStamp" : payParam.timeStamp,
				"nonceStr" : payParam.nonceStr,
				"package" : payParam.package,
				"signType" : payParam.signType,
				"paySign" : payParam.paySign
				}, function(res) {
//					alert(res.err_msg);
//					alert(JSON.stringify(res));
					if (res.err_msg == "get_brand_wcpay_request:ok") {
						// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
					} 
				});
		}
	}
	
	//选择预订时间的点击事件
	$('#selectBookTime_ul_book').find('[data-type="time"]').each(function(index, element){
		element.onclick = function(){
			if($(element).hasClass('selectedRegion_css_book')){
				$(element).addClass('selectedRegion_css_book');
			}else{
				$('#selectBookTime_ul_book').find('[data-type="time"]').removeClass('selectedRegion_css_book');
				$(element).addClass('selectedRegion_css_book');
			}
		}
	});
	
	//预订人数的点击事件
	$('#bookPersonAmoung_div_book').find('[data-type="personAmount"]').each(function(index, element){
		element.onclick = function(){
			if($(element).hasClass('selectedRegion_css_book')){
				$(element).addClass('selectedRegion_css_book');
			}else{
				$('#bookPersonAmoung_div_book').find('[data-type="personAmount"]').removeClass('selectedRegion_css_book');
				$(element).addClass('selectedRegion_css_book');
			}
		}
	});
	
	//付款方式的点击事件
	$('#payMethod_div_book').find('[data-type="payMethod"]').each(function(index, element){
		element.onclick = function(){
			if($(element).hasClass('selectedRegion_css_book')){
				$(element).addClass('selectedRegion_css_book');
			}else{
				$('#payMethod_div_book').find('[data-type="payMethod"]').removeClass('selectedRegion_css_book');
				$(element).addClass('selectedRegion_css_book');
			}
		}
	});
	
	
	//底部栏的返回
	$('#back_a_book').click(function(){
		pickFoodComponent.close();
		$('#div4BookDetail_div_book').show();
		$('#div4bookFoodList_div_book').show();	
		$('#bottoms').hide();
	});
	
	//渲染菜品
	function initFood(food){
		var bookFoodShoppingBox = '<div data-value="{id}" class="div-fl-f-sc-box box-horizontal">'
			+ '<div data-type="msg" class="div-full">'
				+ '<div><b>{name}</b></div>'
				+ '<div>价格: <span>￥{unitPrice}</span></div>'
			+ '</div>'
			+ '<div data-type="cut" data-value={id}>-</div>'
			+ '<div data-type="count">{count}</div>'
			+ '<div data-type="plus" data-value={id}>+</div>'
			+ '</div>';
		
		var html = [], temp, sumPrice = 0;
		
		for(var i = 0; i < food.length; i++){
			temp = food[i];
			sumPrice += (temp.unitPrice * temp.count);
			html.push(bookFoodShoppingBox.format({
				id : temp.id,
				name : temp.name,
				unitPrice : temp.unitPrice,
				count : temp.count
			}));
		}
		$('#div4bookFoodList_div_book').html(html.join(''));	
		
		//菜品数量增加
		$('#div4bookFoodList_div_book').find('[data-type="plus"]').each(function(index, element){
			element.onclick = function(){
				for(var i = 0; i < bookFoods.length; i++){
					if($(element).attr('data-value') == bookFoods[i].id){
						bookFoods[i].count++;
						$(element).parent().find('[data-type="count"]').text(bookFoods[i].count);
					}
				}
			}
		});
		
		//菜品数量减少
		$('#div4bookFoodList_div_book').find('[data-type="cut"]').each(function(index, element){
			element.onclick = function(){
				for(var i = 0; i < bookFoods.length; i++){
					if($(element).attr('data-value') == bookFoods[i].id){
						bookFoods[i].count--;
						if(bookFoods[i].count < 1){
							var dialog = new DialogPopup({
									content : '是否删除该菜品',
									titleText : '温馨提示',
									left : function(){
										dialog.close(function(){
											bookFoods.splice(i, 1);
											$(element).parent().remove();
										}, 200);
									}
								})
							dialog.open();
							break;
						}else{
							$(element).parent().find('[data-type="count"]').text(bookFoods[i].count);
						}
					}
				}
			}
		});
		
	}
	
	
	//只订桌
	$('#bookTable_li_book').click(function(){
		$('#div4bookFoodList_div_book').hide();	
		$('#bookTypeOnlyTable_input_book').attr('checked', 'checked');
	})
	
	//预订点菜
	var bookFoods = [];     //预订的菜品
	var pickFoodComponent = null;
	$('#bookFood_li_book').click(function(){
		 $('#bookTypeTableAndFood_input_book').attr('checked', 'checked');
		 document.getElementById('displayFoodCount_div_fastOrderFood').innerHTML ='';
		 document.getElementById('displayFoodCount_div_fastOrderFood').style.visibility = 'hidden';
		 pickFoodComponent = new PickFoodComponent({
			 bottomId : 'bottoms',
			 confirm : function(selectedFoods, comment){
				if(selectedFoods){
					if(bookFoods.length > 0){
						for(var i = 0; i < selectedFoods.length; i++){
							bookFoods.push(selectedFoods[i]);
						}
					}else{
						bookFoods = selectedFoods;
					}
					initFood(bookFoods);
					pickFoodComponent.close();
					$('#div4BookDetail_div_book').show();
					$('#div4bookFoodList_div_book').show();	
					$('#bottoms').hide();
				}else{
					$('#div4bookFoodList_div_book').html('');	
				}
				
				$('html, body').animate({scrollTop: 1000}, 'fast');  
			 },
			 onCartChange : function(orderFoodData){
				 if(orderFoodData.length > 0){
					 document.getElementById('displayFoodCount_div_fastOrderFood').innerHTML = orderFoodData.length;
					 document.getElementById('displayFoodCount_div_fastOrderFood').style.visibility = 'visible';
				 }else{
					 document.getElementById('displayFoodCount_div_fastOrderFood').innerHTML ='';
					 document.getElementById('displayFoodCount_div_fastOrderFood').style.visibility = 'hidden';
				 }
			 }
		 });
		 $('#div4BookDetail_div_book').hide();
		 $('#bottoms').show();
		 pickFoodComponent.open(function(container){
		 	orderContainer = container;
		 });
		
	});
	
	
	//打开购物车
	$('#shoppingCar_li_member').click(function(){
		pickFoodComponent.openShopping();
	});
	
});
