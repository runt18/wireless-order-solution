

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

/**
 * 加载日期
 */
function loadBookDate(){
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

	
	$('#bookDate4AfterDay').html('<div class="bookDateDetail" data-value=0>后天<br>'+ afterday +'</div>');
	$('#bookDate4Today').html('<div class="bookDateDetail bookDateCheck" data-value=1>今天<br>'+ today +'</div>');
	$('#bookDate4Tomorrow').html('<div class="bookDateDetail" data-value=2>明天<br>'+ tomorrow +'</div>');	
}

/**
 * 加载区域
 */
function loadRegions(){
	var bookRegionTemplate = '<li class="box-horizontal">' +
		'<div data-for="{div4Region}" class="bookRadioSelect">{regionName} </div>' +
		'<div class="event"><input id="{region}" value={regionId} data-value={regionName} {defaultCheck} type="radio" name="bookRegion" class="radioInput"> </div>' +
	'</li>';	
	var regions;
	
	$.post('../../WXOperateBook.do', {
		fid : Util.mp.fid,
		dataSource : 'region'
	}, function(data){
		if(data.success){
			regions = data.root;
			var html = ['<li style="line-height: 40px;">选择座位</li>'];
			for (var i = 0; i < regions.length; i++) {
				html.push(bookRegionTemplate.format({
					div4Region : 'region' + regions[i].id,
					region : 'region' + regions[i].id,
					regionId : regions[i].id,
					regionName : regions[i].name,
					defaultCheck : i == 0 ? 'checked="checked"' : ""
				}));
			}
			
			$('#ul4BookRegion').html(html.join(""));
			
			//预订区域 & 类型
			$('.bookRadioSelect').on('click', function(){
				$('#' + $(this).data("for")).click();
			})
		}
	}, 'json');	
}

/**
 * 加载会员信息
 */
function loadBookMember(){
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
}

/**
 * 提交预订信息
 */
function commitBook(){
	var date, time, region, sex, name, phone, bookType, count;
	$('.bookDateDetail').each(function(){
		if($(this).hasClass("bookDateCheck")){
			date = bookDates[$(this).data('value')];
		}
	})
	time = $('#bookTime').val();
	if(time.indexOf('PM') > 0){
		var hourString = time.substring(0, time.indexOf(':'));
		var hour = parseInt(hourString)+12;
		var minute = time.substr(time.indexOf(':')+1, 2);
		time = hour + ":" + minute + ':' + "59"
	}else if(time.indexOf('AM') > 0){
		var hour = time.substring(0, time.indexOf(':'));
		var minute = time.substr(time.indexOf(':')+1, 2);	
		time = hour + ":" + minute + ':' + "59"
	}else{
		Util.dialog.show({msg: '请选择时间'});
		return;
	}
	
	region = $('input[name="bookRegion"]:checked').data("value");
	
	count = $('input[name="bookPerson"]:checked').val();
	//count = $('#txtBookPeopleCount').val();
	name = $('#txtBookName').val();
	phone = $('#txtBookPhone').val();
	
	bookType = $('input[name="bookType"]:checked').val();
	
	
	if(!name){
		Util.dialog.show({msg: '请填写姓名'});
		return;
	}	
	if(!phone){
		Util.dialog.show({msg: '请填写电话'});
		return;
	}	
	
	var foods = "";
	for(var i = 0; i < params.orderData.length; i++){
		temp = params.orderData[i];
		if(i > 0) foods += '&';
		foods += (temp.id + ',' + temp.count);
	}
	
	$.post('../../WXOperateBook.do', {
		fid : Util.mp.fid,
		dataSource : 'insert',
		bookDate : date + " " + time,
		member : name,
		phone : phone,
		count : count,
		region : region,
		foods : foods
	}, function(data){
		if(data.success){
			Util.dialog.show({msg: '预订成功'});
		}
	}, 'json');
}

/**
 * 选只订座的方式
 */
function operateSeat(){
	$('#div4bookFoodList').html('');	
	$('#bookFoodTotalMoney').html('');
}

/**
 * 去预订点菜
 */
function toBookFood(){
	$('#div4BookDetail').hide();
	$('#div4FoodList').show();
	
	//清空数据
	var li = $('.select-food');

	if(li.length > 0){
		li.each(function(e){
			$(this).removeClass("select-food");
		});
	}
	params.orderData.length = 0;
	$('#spanDisplayFoodCount').html('');
	
	//加载底部栏
	var navBottomSwiper = new Swiper('#divBottomNav', {
		paginationClickable : true,
		slidesPerView : 'auto',
		disableAutoResize : true
	});
	var li = $('#divBottomNav li');
	var tw = $('#divBottomNav').width();
	tw = li.length > 4 ? parseInt(tw / 4) : parseInt(tw / li.length);
	for (var i = 0; i < li.length; i++) {
		li[i].style.width = tw + 'px';
	}	
}

/**
 * 返回预订信息
 * @param hasFoods
 */
function toBookDetail(hasFoods){
	$('#div4FoodList').hide();	
	$('#div4BookDetail').show();
	if(hasFoods && params.orderData.length > 0){
		var html = [], temp, sumPrice = 0;
		
		for(var i = 0; i < params.orderData.length; i++){
			temp = params.orderData[i];
			sumPrice += (temp.unitPrice * temp.count);
			html.push(bookFoodShoppingBox.format({
				id : temp.id,
				name : temp.name,
				unitPrice : temp.unitPrice,
				count : temp.count
			}));
		}
		$('#div4bookFoodList').html(html.join(''));	
		$('#bookFoodTotalMoney').html('¥'+sumPrice);
	}else{
		$('#div4bookFoodList').html('');	
		$('#bookFoodTotalMoney').html('');
	}
	
	$('html, body').animate({scrollTop: 1000}, 'fast');  
}

/**
 * 操作预定菜列表
 * @param c
 */
function operateBookFood(c){
	var displayCount = null;
	if(typeof c.event != 'undefined'){
		displayCount = $(c.event.parentNode).find('div[data-type=count]');
	}
	
	if(c.otype == 'plus'){
		for(var i = 0; i < params.orderData.length; i++){
			if(params.orderData[i].id == c.id){
				params.orderData[i].count ++;					
				if(displayCount){
					displayCount.css('display', 'block').html(params.orderData[i].count);
				}				
				break;
			}
		}
		calcBookFoodTotalMoney();	
	}else if(c.otype == 'cut'){
		for(var i = 0; i < params.orderData.length; i++){
			temp = params.orderData[i];
			if(temp.id == c.id){
				if(temp.count - 1 == 0){
					Util.dialog.show({
						msg : '是否删除该菜品?',
						callback : function(btn){
							if(btn == 'yes'){
								var tl = $('#div4bookFoodList > div');
								for(var j = 0; j < tl.length; j++){
									if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
										$(tl[j]).remove();
										break;
									}
								}
								
								params.orderData.splice(i, 1);
								
								calcBookFoodTotalMoney();
							}
						}
					});
					
/*					if(confirm("是否删除该菜品？"))
					 {
						var tl = $('#div4bookFoodList > div');
						for(var j = 0; j < tl.length; j++){
							if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
								$(tl[j]).remove();
								break;
							}
						}
						
						params.orderData.splice(i, 1);
					 }*/

				}else{
					params.orderData[i].count--;					
					if(displayCount){
						displayCount.css('display', 'block').html(params.orderData[i].count);
					}
					calcBookFoodTotalMoney();
				}
				break;
			}
		}
	}else if(c.otype == 'claer'){
		Util.dialog.show({
			msg : '是否清空已选菜品?',
			callback : function(btn){
				if(btn == 'yes'){
					//清空数据
					var li = $('.select-food');

					if(li.length > 0){
						li.each(function(e){
							$(this).removeClass("select-food");
						});
					}
					params.orderData.length = 0;
					$('#spanDisplayFoodCount').html('');
					operateShoppingCart({otype:'hide'});
				}
			}
		});
	}
	
}

/**
 * 计算预订菜总价钱
 */
function calcBookFoodTotalMoney(){
	var sumPrice = 0;
	for(var i = 0; i < params.orderData.length; i++){
		sumPrice += (params.orderData[i].unitPrice * params.orderData[i].count);
	}
	$('#bookFoodTotalMoney').html('¥'+sumPrice);	
}

$(function(){
	$(".bookTime").timepicki();
	
	loadBookMember();
	
	loadBookDate();
	
	loadRegions();
	
	//预订时间
	$('.bookDateDetail').on('click', function(){
		$('.bookDateDetail').removeClass('bookDateCheck');
		$(this).addClass('bookDateCheck');
	})
	
});