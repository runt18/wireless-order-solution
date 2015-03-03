
var Request = new Util_urlParaQuery();
var systemStatus = Request["status"];

//刷新时去除#
if(location.href.indexOf('#') > 0){
	location.href = systemStatus?'index.htm?status='+systemStatus : 'index.htm';
}

var lg = {
		restaurant : {},
		staffs : [],
		staffPaging : {}
	};

var allStaff = '<a data-role="button" data-inline="true" class="loginName" onclick="selectedName(this)" data-value="{staffId}" data-theme="c"><div>{staffName}</div></a>';


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
	
	//pos
	if(parseInt(systemStatus) == 1){
		$('#btnLogin4Pos').show();
		$('#btnLogin4Touch').hide();
	}else{
		$('#btnLogin4Touch').show();
		$('#btnLogin4Pos').hide();		
	}
	
	Util.LM.show();
	if (getcookie("digie_restaurant") != ""){
		var account = getcookie("digie_restaurant");
		var token = getcookie("digie_token")
		var restaurantEntity;
		
		//FIXME 过渡
		if(account.length > 10){
			restaurantEntity = JSON.parse(account);
		}else{
			restaurantEntity = {account : account};
		}
		
		$.ajax({
			url : '../VerifyRestaurant.do',
			data : {
				account : restaurantEntity.account,
				token : token
			},	
			type : 'post',
			success : function(data, status, xhr){
				Util.LM.hide();
				if(data.success && data.root.length > 0){
					//把token存入cookie
					setcookie("digie_token", data.other.token);
					//FIXME 更新cookie
					setcookie("digie_restaurant", data.root[0].account);
					
					lg.restaurant = data.root[0];
					
					$('#txtRestaurantName').text(lg.restaurant.name);
					
					//验证员工是否登陆状态
					$.ajax({
						url : '../VerifyLogin.do',
						success : function(data, status, xhr){
							Util.LM.hide();
							if(data.success){
								location.href = 'tableSelect.html';	
							}else{	
								initStaffContent();
							}
						},
						error : function(request, status, error){
							Util.Lm.hide();
							initStaffContent();
						}
					});
				}else{	
					Util.msg.alert({
						msg : data.msg?data.msg:"",
						renderTo : 'restaurantLoginPage',
						fn : function(){
							$('#popupLogin').show();
							$('#txtRestaurantAccount').focus();
						}
					});
					return;					
				}
			},
			error : function(request, status, error){
				Util.Lm.hide();
				initStaffContent();
			}
		});		

	}else{
		Util.LM.hide();
		$('#popupLogin').show();
		$('#txtRestaurantAccount').focus();
	}	
	
});

/**
 * 登陆
 */
function staffLoginHandler(){
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
			comeFrom : 3,
			pwd : MD5(pwd.val().trim()),
			account : lg.restaurant.account,
			token : getcookie("digie_token")
		},
		type : 'post',
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				setcookie("digie_token", data.other.token);
				location.href = 'tableSelect.html?status=1';	
			}else{
				Util.msg.alert({
					msg : data.msg,
					renderTo : 'staffLoginPage',
					fn : function(){
						pwd.focus();
					}
				});
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
			token : getcookie("digie_token")
		},
		type : 'post',
		dataType : 'json',
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				if(data.root.length != 0){
					setcookie("digie_restaurant", data.root[0].account);
					setcookie("digie_token", data.other.token);
					
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
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				lg.staffs = data.root;
				
//				data.root = data.root.splice(17);
	            $.mobile.changePage("#staffLoginPage",
	                    { transition: "fade" });
	            
	            //设置餐厅名字
	            $('#lab4RestaurantName').text(lg.restaurant.name);
	            
				if(data.root.length > 20){
					$('#staffPaddingBar').show();
				}
				$('#divAllStaffForUserLogin').height(75 * Math.ceil(data.root.length/5));
				$('#selectStaffCmp').height($('#selectStaffCmp-popup').height());
				
				lg.staffPaging = Util.to.padding({
					renderTo : "divAllStaffForUserLogin",
					data : data.root,
					limit : 20, 
					templet : function(c){
						return allStaff.format({
							staffId : c.data.staffID,
							staffName : c.data.staffName ,
						});
					}
				});
				lg.staffPaging.getFirstPage();		
				
				$('.loginName').click(function(){
					$('.loginName').attr('data-theme', 'c');
					$(this).attr('data-theme', 'b');
					$('.loginName').buttonMarkup( "refresh" );
				});	 
	            
			}else{
				Util.msg.alert({
					msg : '获取餐厅员工信息失败, 请联系客服员.',
					renderTo : 'staffLoginPage',
				});
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				msg : '获取餐厅员工信息失败, 请联系客服员.',
				renderTo : 'staffLoginPage',
			});
		}
	});
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
	lg.staffPaging.init({
		data : lg.staffs
	});
	lg.staffPaging.getFirstPage();
	$('#selectStaffCmp').parent().addClass("pop").addClass("in");
	$('#selectStaffCmp').popup('open');
	
}
	