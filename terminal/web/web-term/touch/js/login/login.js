
var Request = new Util_urlParaQuery();
var systemStatus = Request["status"]?parseInt(Request["status"]):2;

//刷新时去除#
if(location.href.indexOf('#') > 0){
	location.href = 'verifyLogin.jsp?status='+systemStatus;
}

//登陆界面数据对象
var lg = {
		restaurant : {},
		staffs : [],
		staffPaging : {},
		bbs : []
	};

var allStaff = '<a data-role="button" data-inline="true" class="loginName" data-index={dataIndex} onclick="selectedName(this)" data-value="{staffId}" data-theme="c"><div>{staffName}</div></a>';

$(function(){
	
	$(".numkeyboard").ioskeyboard({
	    keyboardRadix:80,//键盘大小基数，实际大小比为9.4，即设置为100时实际大小为940X330
	    keyboardRadixMin:40,//键盘大小的最小值，默认为60，实际大小为564X198
	    keyboardRadixChange:true,//是否允许用户改变键盘大小,该功能仅能完美支持Chrome26；仅当keyboardRadixMin不小于60时才较好支持Safari内核浏览器
	    clickeve:true,//是否绑定元素click事件
	    colorchange:true,//是否开启按键记忆功能，如果开启，将随着按键次数的增加加深相应按键的背景颜色
	    colorchangeStep:1,//按键背景颜色改变步伐，采用RBG值，默认为RGB(255,255,255),没按一次三个数字都减去步伐值
	    colorchangeMin:154//按键背影颜色的最小值，默认为RGB(154,154,154)
	});
	
	//pos && 体验端可以使用前后登陆
	if(systemStatus == 1 || systemStatus == 3){
		$('#btnLogin4Pos').show();
		$('#btnLogin4Touch').hide();
	}else{
		$('#btnLogin4Touch').show();
		$('#btnLogin4Pos').hide();		
	}
	
	Util.LM.show();
	
	if (getcookie(document.domain+"_digie_restaurant") != "" && getcookie(document.domain+"_digie_token") != ""){
		var account = getcookie(document.domain+"_digie_restaurant");
		var token = getcookie(document.domain+"_digie_token");
		//var rid = getcookie(document.domain+'_restaurant');
		
		$.ajax({
			url : '../VerifyRestaurant.do',
			data : {
				account : account,
				token : token && token != 'undefined'?token:''
			},	
			type : 'post',
			success : function(data, status, xhr){
				Util.LM.hide();
				if(data.success && data.root.length > 0){
					
					lg.restaurant = data.root[0];
					
					$('#txtRestaurantName').text(lg.restaurant.name);
					
					initStaffContent();
				}else{	
					Util.msg.alert({
						msg : data.msg?data.msg:"餐厅登陆失败",
						renderTo : 'restaurantLoginPage',
						fn : function(){
							$('#txtRestaurantAccount').removeAttr('autofocus');
							$('#loginRestaurantCmp').show();
							$('#txtRestaurantAccount').val(account);
							$('#txtRestaurantDynamicCode').focus();
						}
					});
					return;					
				}
			},
			error : function(request, status, error){
				Util.LM.hide();
				initStaffContent();
			}
		});		

	}else{
		Util.LM.hide();
		$('#loginRestaurantCmp').show();
	}	
	
	//餐厅登陆快捷方式
    $('#txtRestaurantDynamicCode').on('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	restaurantLoginHandler();
        }
    });		
    
    //设置QQ客服
    dynaLoadQQ();
});

$(document).on("pageinit",function(event){
	initBillboardContent({display:true});
});

//动态加载QQ客服
function dynaLoadQQ(){  
    var script = document.createElement('script');  
	script.setAttribute('type', 'text/javascript');  
	script.setAttribute('src', 'http://code.54kefu.net/kefu/js/b150/852550.js');  
	document.getElementsByTagName('head')[0].appendChild(script);  
    //调试QQ客服的位置
    $("#kfoutbox").css("margin-top","100px");
}  

/**
 * 登陆
 */
function staffLoginHandler(c){
	var pwd=$('#loginPassword');
	if(!lg.staff){
		Util.msg.alert({
			msg : '请选择一个员工.',
			renderTo : 'staffLoginPage'
		});
		return;
	}
	if(!pwd.val()){
		Util.msg.alert({
			msg : '请输入密码.',
			renderTo : 'staffLoginPage',
			fn : function(){
				pwd.focus();
			}
		});
		return;
	}
	
	Util.LM.show();
	$.ajax({
		url : '../OperateStaff.do',
		data : {
			pin : lg.staff.staffID,
			pwd : MD5(pwd.val().trim()),
			account : lg.restaurant.account,
			token : getcookie(document.domain + '_digie_token')
		},
		type : 'post',
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				if(c && c.part == 'basic'){
					location.href = '../pages/Mgr/DigieBasic.html';
				}else{
					location.href = 'tableSelect.jsp?status=' + systemStatus + '#tableSelectMgr';	
				}
			}else{
				
				if(data.code == 6901){//token问题
					window.location.reload();
				}else{//登陆问题
					Util.msg.alert({
						msg : data.msg,
						renderTo : 'staffLoginPage',
						fn : function(){
							pwd.focus();
						}
					});					
				}

			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				msg : err,
				renderTo : 'staffLoginPage'
			});
		}
	});
}

/**
 * 餐厅登录
 */
function restaurantLoginHandler(){
	var account = $('#txtRestaurantAccount');
	var code = $('#txtRestaurantDynamicCode');
	if(!account.val()){
		Util.msg.alert({
			msg : '请输入餐厅账号',
			renderTo : 'staffLoginPage',
			fn : function(){
				account.focus();
			}
		});
		return;
	}
	
	if(!code.val()){
		Util.msg.alert({
			msg : '请输入验证码',
			renderTo : 'staffLoginPage',
			fn : function(){
				code.focus();
			}
		});
		return;
	}	
	
	Util.LM.show();
	$.ajax({
		url : '../RestaurantLogin.do',
		data : {
			account : account.val(),
			code : code.val(),
			token : getcookie(document.domain+"_digie_token")
		},
		type : 'post',
		dataType : 'json',
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				if(data.root.length != 0){
					setcookie(document.domain+"_digie_restaurant", data.root[0].account);
					setcookie(document.domain+"_digie_token", data.other.token);
					
					lg.restaurant=data.root[0];
					Util.LM.show();
					initStaffContent();
				}else{
					Util.msg.alert({
						title : "温馨提示" ,
						msg : "餐厅帐号错误,请检查后重新输入",
						renderTo : 'restaurantLoginPage',
						time : 2
					});
				}
			}else{
				Util.msg.alert({
					msg : data.msg,
					renderTo : 'restaurantLoginPage'
				});
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				msg : err
			});
		}
	});
}

/**
 * 初始化员工登陆界面
 */
function initStaffContent(c){
	$.ajax({
		url : '../QueryStaff.do',
		data : {
			restaurantID : lg.restaurant.id
		},
		dataType : 'json',
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				lg.staffs = data.root;
				
	            $.mobile.changePage("#staffLoginPage",
	                    { transition: "fade" });
	            
	            //设置餐厅名字
	            $('#lab4RestaurantName').text(lg.restaurant.name);
	            
				if(data.root.length > 20){
					$('#staffPaddingBar').show();
				}
				$('#divAllStaffForUserLogin').height(75 * Math.ceil(data.root.length/5));
				$('#selectStaffCmp').height($('#selectStaffCmp-popup').height());
				
				lg.staffPaging = new WirelessOrder.Padding({
					renderTo : $('#divAllStaffForUserLogin'),
					limit : 20,
					itemLook : function(index, item){
						return allStaff.format({
							dataIndex : index,
							staffId : item.staffID,
							staffName : item.staffName
						});
					}
				});
				lg.staffPaging.data(data.root)	;
				
				$('.loginName').click(function(){
					$('.loginName').attr('data-theme', 'c');
					$(this).attr('data-theme', 'b');
					$('.loginName').buttonMarkup( "refresh" );
				});	 
	            
			}else{
				Util.msg.alert({
					msg : '获取餐厅员工信息失败, 请联系客服员.',
					renderTo : 'staffLoginPage'
				});
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				msg : '获取餐厅员工信息失败, 请联系客服员.',
				renderTo : 'staffLoginPage'
			});
		}
	});
	
}

/**
 * 初始化公告信息
 */
function initBillboardContent(c){
	c = c || {};
	if(!lg.restaurant.id){
		return;
	}
	$.post('../QueryBillboard.do', {dataSource:'loginInfo', rid: lg.restaurant.id}, function(result){
		if(result.success && result.root.length > 0){
			$('#btnDisplayBillboard').show();
			$('#btnDisplayBillboard .ui-btn-text').html(result.root.length +' 条公告');
			$('#btnDisplayBillboard').buttonMarkup('refresh');	
			lg.bbs =  result.root;
			var html = ['<li data-role="divider" data-theme="e">点击标题查看详情:</li>'];
			for (var i = 0; i < result.root.length; i++) {
				html.push('<li data-index={index} data-value={id} onclick="displayBillboard(this)"> <a>{status}{title}</a></li>'.format({
					index : i,
					id : result.root[i].id,
					status : '<font color="red" >＊</font>',
					title : result.root[i].title
				}));
				
			}
			$('#billboardList').html(html.join("")).trigger('create').listview('refresh');
			
			if(lg.bbs.length > 0 && c.display && !getcookie("alertBillboard")){
				setTimeout(function(){
					displayBillboard.curBillboardId = lg.bbs[0].id;
					
			    	$('#billboardTitle').text(lg.bbs[0].title);
			    	$('#billboardDesc').html(lg.bbs[0].desc);
			    	
			    	$('#billboardCmp').popup('open');
			    	
				}, 400);
			}
			
		}else{
			$('#btnDisplayBillboard').hide();
		}
	});
}
/**
 * 公告
 */
function displayBillboard(thiz){
	$('#billboardsCmp').popup({  
	    afterclose: function (event, ui) {  
	    	if(thiz){
		    	var billboard = lg.bbs[$(thiz).attr('data-index')];
		    	
		    	$('#billboardTitle').text(billboard.title);
		    	$('#billboardDesc').html(billboard.desc);
		    	
		    	$('#billboardCmp').popup('open');
		    	
		    	displayBillboard.curBillboardId = billboard.id;

	    	}
	    	thiz = null;
	    }  
	});	
	
	$('#billboardsCmp').popup('close');
}

/**
 * 读取了公告
 */
function knowedBillboard(){
	$('#billboardCmp').popup('close');
	setcookie("alertBillboard", true, null, getTodayRemainTime());
}

/**
 * 下一条公告
 */
function nextBillboard(){
	for (var i = 0; i < lg.bbs.length; i++) {
		if(lg.bbs[i].id == displayBillboard.curBillboardId && (i+1) < lg.bbs.length){
	    	$('#billboardTitle').text(lg.bbs[i+1].title);
	    	$('#billboardDesc').html(lg.bbs[i+1].desc);
	    	
	    	displayBillboard.curBillboardId = lg.bbs[i+1].id;
		}
	}
}

/**
 * 上一条公告
 */
function lastBillboard(){
	for (var i = 0; i < lg.bbs.length; i++) {
		if(lg.bbs[i].id == displayBillboard.curBillboardId && (i-1) >= 0){
	    	$('#billboardTitle').text(lg.bbs[i-1].title);
	    	$('#billboardDesc').html(lg.bbs[i-1].desc);
	    	
	    	displayBillboard.curBillboardId = lg.bbs[i-1].id;
		}
	}
}

/**
 * 获取当天剩余秒数
 * @returns {Number}
 */
function getTodayRemainTime(){
	return new Date(new Date().getFullYear() + "-" + (new Date().getMonth()+1) + "-" + new Date().getDate() + " 23:23:59").getTime() - new Date().getTime();
}

/**
 * 选择员工
 * @param thiz
 */
function selectedName(thiz){
	var btn = $(thiz);
	for (var i = 0; i < lg.staffs.length; i++) {
		if(parseInt(btn.attr('data-value')) == lg.staffs[i].staffID){
			lg.staff = lg.staffs[i];
		}
	}
	setTimeout(function(){
		$('#loginPassword').val('');
		$('#loginPassword').focus();		
	}, 200);
	$('#selectStaffCmp').popup('close');
	$('#lab4StaffName').text(btn.text());
}

/**
 * 打开员工选择
 */
function openStaffSelectCmp(){
	lg.staffPaging.data(lg.staffs);
//	$('#selectStaffCmp').parent().addClass("pop").addClass("in");
	$('#selectStaffCmp').popup('open');
	
}
	