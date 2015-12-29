$(function(){
	var bookDates = [],
	bookFoodShoppingBox = '<div data-value="{id}" class="div-fl-f-sc-box box-horizontal">'
		+ '<div data-type="msg" class="div-full">'
			+ '<div><b>{name}</b></div>'
			+ '<div>价格: <span>￥{unitPrice}</span></div>'
		+ '</div>'
		+ '<div data-type="cut" onclick="operateBookFood({otype:\'cut\', id:{id}, event:this})">-</div>'
		+ '<div data-type="count">{count}</div>'
		+ '<div data-type="plus" onclick="operateBookFood({otype:\'plus\', id:{id}, event:this})">+</div>'
		+ '</div>';
	
	
	$(".bookTime").timepicki();
	
	//加载会员信息
	(function loadBookMember(){
		$.post('../../WXOperateMember.do', {
			dataSource : 'getInfo',
			oid : Util.mp.oid,
			fid : Util.mp.fid		
		}, function(data){
			if(data.success){
				var member = data.other.member;
				$('#txtBookName').val(member.name);
				$('#txtBookPhone').val(member.mobile);
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

		
		$('#bookDate4AfterDay').html('<div class="bookDateDetail" data-type="bookTime" data-value=2>后天<br>'+ afterday +'</div>');
		$('#bookDate4Today').html('<div class="bookDateDetail bookDateCheck" data-type="bookTime" data-value=0>今天<br>'+ today +'</div>');
		$('#bookDate4Tomorrow').html('<div class="bookDateDetail" data-type="bookTime" data-value=1>明天<br>'+ tomorrow +'</div>');	
		
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
			dataSource : 'region'
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
					 $('#ul4BooskRegion').append(li);
					 $('#ul4BooskRegion').trigger('create').trigger('refresh');
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
					 $('#ul4BooskRegion').append(li);
					 $('#ul4BooskRegion').trigger('create').trigger('refresh');
				}
				
				//预订区域的点击事件
				$('#ul4BooskRegion').find('[data-type="bookRegions"]').each(function(index, element){
					$('#ul4BooskRegion').find('[data-type="bookRegions"]:first').addClass('selectedRegion_css_book');
					element.onclick = function(){
						if($(element).hasClass('selectedRegion_css_book')){
							$(element).addClass('selectedRegion_css_book');
						}else{
							$('#ul4BooskRegion').find('[data-type="bookRegions"]').removeClass('selectedRegion_css_book');
							$(element).addClass('selectedRegion_css_book');
						}
					}
				});
				
				
				//预订区域 & 类型
				$('.bookRadioSelect').on('click', function(){
					$('#' + $(this).data("for")).click();
				});
			}
		}, 'json');	
	})();
	
	
	//预订提交
	$('#commit_a_book').click(function(){
		var date, time, region, name, phone, count;
		$('.bookDateDetail').each(function(){
			if($(this).hasClass("bookDateCheck")){
				date = bookDates[$(this).data('value')];
			}
		});
		time = $('#bookTime').val();
		if(time.indexOf('PM') > 0){
			var hourString = time.substring(0, time.indexOf(':'));
			var hour = parseInt(hourString) + 12;
			var minute = time.substr(time.indexOf(':') + 1, 2);
			time = hour + ":" + minute + ':' + "59";
		}else if(time.indexOf('AM') > 0){
			var hour = time.substring(0, time.indexOf(':'));
			var minute = time.substr(time.indexOf(':')+1, 2);	
			time = hour + ":" + minute + ':' + "59";
		}else{
			Util.dialog.show({msg: '请选择时间', btn:'yes'});
			return;
		}
		
		//预订区域
		$('#ul4BooskRegion').find('[data-type="bookRegions"]').each(function(index, element){
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
		
		name = $('#txtBookName').val();
		phone = $('#txtBookPhone').val();
		
		if(!name){
			Util.dialog.show({msg: '请填写姓名', btn:'yes'});
			return;
		}	
		if(!phone){
			Util.dialog.show({msg: '请填写电话', btn:'yes'});
			return;
		}	
		
//			var foods = "";
//			for(var i = 0; i < params.orderData.length; i++){
//				temp = params.orderData[i];
//				if(i > 0){
//					foods += '&';
//				}
//				foods += (temp.id + ',' + temp.count);
//			}
//			
		$.post('../../WxOperateBook.do', {
			fid : Util.mp.fid,
			dataSource : 'insert',
			bookDate : date + " " + time,
			member : name,
			phone : phone,
			count : count,
			region : region,
//				foods : foods
		}, function(data){
			if(data.success){
				Util.dialog.show({msg: '预订成功', btn:'yes',
					callback : function(btn){
						if(btn == 'yes'){
							Util.jump('orderList.html?book=1');
						}
					}
				});
			}
		}, 'json');
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
	
	
//	//选只订座的方式
//	function operateSeat(){
//		$('#div4bookFoodList').html('');	
////		$('#bookFoodTotalMoney').html('');
//	}
	
	
//	//去预订点菜
//	function toBookFood(){
//		$('#div4BookDetail').hide();
//		$('#div4FoodList').show();
//		
//		//清空数据
//		var li = $('.select-food');
//
//		if(li.length > 0){
//			li.each(function(e){
//				$(this).removeClass("select-food");
//			});
//		}
//		params.orderData.length = 0;
//		$('#spanDisplayFoodCount').html('');
//		
//		//加载底部栏
//		var navBottomSwiper = new Swiper('#divBottomNav', {
//			paginationClickable : true,
//			slidesPerView : 'auto',
//			disableAutoResize : true
//		});
//		var li = $('#divBottomNav li');
//		var tw = $('#divBottomNav').width();
//		tw = li.length > 4 ? parseInt(tw / 4) : parseInt(tw / li.length);
//		for (var i = 0; i < li.length; i++) {
//			li[i].style.width = tw + 'px';
//		}	
//	}
	
	
//	//返回预订信息
//	function toBookDetail(hasFoods){
//		$('#div4FoodList').hide();	
//		$('#div4BookDetail').show();
//		if(hasFoods && params.orderData.length > 0){
//			var html = [], temp, sumPrice = 0;
//			
//			for(var i = 0; i < params.orderData.length; i++){
//				temp = params.orderData[i];
//				sumPrice += (temp.unitPrice * temp.count);
//				html.push(bookFoodShoppingBox.format({
//					id : temp.id,
//					name : temp.name,
//					unitPrice : temp.unitPrice,
//					count : temp.count
//				}));
//			}
//			$('#div4bookFoodList').html(html.join(''));	
////			$('#bookFoodTotalMoney').html('¥'+sumPrice);
//		}else{
//			$('#div4bookFoodList').html('');	
////			$('#bookFoodTotalMoney').html('');
//		}
//		
//		$('html, body').animate({scrollTop: 1000}, 'fast');  
//	}
	
	
//	//操作预定菜列表
//	function operateBookFood(c){
//		var displayCount = null;
//		if(typeof c.event != 'undefined'){
//			displayCount = $(c.event.parentNode).find('div[data-type=count]');
//		}
//		
//		if(c.otype == 'plus'){
//			for(var i = 0; i < params.orderData.length; i++){
//				if(params.orderData[i].id == c.id){
//					params.orderData[i].count ++;					
//					if(displayCount){
//						displayCount.css('display', 'block').html(params.orderData[i].count);
//					}				
//					break;
//				}
//			}
//			calcBookFoodTotalMoney();	
//		}else if(c.otype == 'cut'){
//			for(var i = 0; i < params.orderData.length; i++){
//				temp = params.orderData[i];
//				if(temp.id == c.id){
//					if(temp.count - 1 == 0){
//						Util.dialog.show({
//							msg : '是否删除该菜品?',
//							callback : function(btn){
//								if(btn == 'yes'){
//									var tl = $('#div4bookFoodList > div');
//									for(var j = 0; j < tl.length; j++){
//										if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
//											$(tl[j]).remove();
//											break;
//										}
//									}
//									
//									params.orderData.splice(i, 1);
//									
//									calcBookFoodTotalMoney();
//								}
//							}
//						});
//						
//	/*					if(confirm("是否删除该菜品？"))
//						 {
//							var tl = $('#div4bookFoodList > div');
//							for(var j = 0; j < tl.length; j++){
//								if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
//									$(tl[j]).remove();
//									break;
//								}
//							}
//							
//							params.orderData.splice(i, 1);
//						 }*/
//
//					}else{
//						params.orderData[i].count--;					
//						if(displayCount){
//							displayCount.css('display', 'block').html(params.orderData[i].count);
//						}
//						calcBookFoodTotalMoney();
//					}
//					break;
//				}
//			}
//		}else if(c.otype == 'claer'){
//			Util.dialog.show({
//				msg : '是否清空已选菜品?',
//				callback : function(btn){
//					if(btn == 'yes'){
//						//清空数据
//						var li = $('.select-food');
//
//						if(li.length > 0){
//							li.each(function(e){
//								$(this).removeClass("select-food");
//							});
//						}
//						params.orderData.length = 0;
//						$('#spanDisplayFoodCount').html('');
//						operateShoppingCart({otype:'hide'});
//					}
//				}
//			});
//		}
//	}
	
	

//	//计算预订菜总价钱
//	function calcBookFoodTotalMoney(){
//		var sumPrice = 0;
//		for(var i = 0; i < params.orderData.length; i++){
//			sumPrice += (params.orderData[i].unitPrice * params.orderData[i].count);
//		}
//		$('#bookFoodTotalMoney').html('¥'+sumPrice);	
//	}
	
});
