/**
 * 显示模板
 */
var Templet={
	ul : {
		allStaff : '<div data-value={id} class="main-box-base" style="line-height: 70px; text-align: center;" onClick="changeStaff({event:this, type:1, staffId:{id}})">{name}</div>'
	},
	ts : {
		boxTable : '<div data-index={dataIndex} data-alias={alias} class={dataClass} style="text-align : center;" '
			+ 'onclick="ts.selectTable({event : this, tableAlias : {alias}})">'
			+ '<div style="font-weight : bold; margin-top : 20px;">{tableName}</div>'
			+ '<div style="color: #462B77; font-size: 10px;">{alias}</div>'
			+ '</div>',
		region : '<div class="button-base" data-value={value} data-type="region-select" '
			+ 'onClick="ts.findTableByRegion({event:this, regionId:{value}})">{text}</div>'
	},
	co : {
		dept : '<div class="button-base-deptKitchen" data-value={value} data-type="dept-select" '
			+ 'onClick="co.initKitchenContent({event:this, deptId:{value}})">{text}</div>',
		kitchen : '<div class="button-base-deptKitchen" data-value={value} data-type="kitchen-select" '
			+ 'onClick="co.findFoodByKitchen({event:this, kitchenId:{value}})">{text}</div>',
		boxFood : '<div data-index={dataIndex} data-value={id} class="main-box-base" onClick="{click}">'
			+ '{name}'
			+ '<div style="width:55px;">¥:{unitPrice}</div>'
			+ '<div style="width:25px;height : 25px;position:absolute;right:0;bottom:0;font-size:25px;color:{color};">{foodState}</div>'
			+ '</div>',
		newFood : '<div data-index={dataIndex} data-value={id} data-type="newFood-select" onClick="co.selectNewFood({event:this, foodId:{id}})">'
			+ '<div style="line-height: 40px; ">{name} x {count}</div>'
  			+ '<div>{tasteDisplay}</div>'
  			+ '<div class="box-horizontal" style="text-align: right; padding-right: 5px;">'
  				+ '<div style="color: #FF0000;">{isTemporary}</div>'
  				+ '<div style="color: #FF0000; -webkit-box-flex: 1;">{isGift}</div>'
  				+ '<div style="color: #FF0000; -webkit-box-flex: 1;">{isHangup}</div>'
  				+ '<div style="min-width: 100px;">¥:{totalPrice}</div>'
  			+'</div>'
  			+ '</div>',
  	  	boxSelectTaste : '<div data-index={dataIndex} data-value={id} class="main-box-base" onClick="co.ot.insertTaste({event:this, tasteId:{id}})">'
  	  		+ '{name}'
  	  		+ '<div>{mark}:{markText}</div>'
  	  		+ '</div>',
  	  	boxSelectTasteFloat : '<div data-index={dataIndex} data-value={id} class="main-box-base" onClick="co.ot.insertTaste({event:this, tasteId:{id}, fTaste:true})">'
  	  		+ '{name}'
  	  		+ '<div>{mark}:{markText}</div>'
  	  		+ '</div>',  	  		
  	  	boxTasteCategory : '<div style="background-color:yellow;" data-index={dataIndex} data-value={id} class="main-box-base" onClick="co.ot.changeTasteCategory({event:this, tasteId:{id}})">'
  	  		+ '{name}'
  	  		+ '</div>',
  	  	boxNewTaste : '<div data-value={id} class="main-box-base" onClick="co.ot.deleteTaste({event:this, tasteId:{id}})">'
  	  		+ '{name}'
  	  		+ '<div>{mark}:{markText}</div>'
  	  		+ '</div>',
  	  	boxNewTempTaste : '<div data-value={id} class="main-box-base" onClick="co.ot.updateTaste({event:this, tasteId:{id}})">'
  	  		+ '{name}'
  	  		+ '<div>{mark}:{markText}</div>'
  	  		+ '</div>'  	  		
	},
	uo : {
		orderFood : '<tr data-index={dataIndex} data-value={id} id="truoFood{dataIndex}" onclick="selectUOFood(this)">'
			+ 'data-type="orderFood-select" >'
			+ '<td>{dataIndex}</td>'
			+ '<td>{name}</td>'
			+ '<td>{count}</td>'
			+ '<td>{tastePref}</td>'
			+ '<td>{actualPrice}</td>'
			+ '<td>{totalPrice}</td>'
			+ '<td>{orderDateFormat}</td>'
			+ '<td><div class="button-base cancelFoodBtn" id="btnuo{dataIndex}">退菜</div></td>'
			+ '<td>{waiter}</td>'
			+ '</tr>',
		changeDiscount : '<div data-value={id} class="main-box-base" onClick="uo.cd.select({id:{id}})">{name}</div>'
	},
	ss : {
		dept : '<div class="button-base" data-value={value} data-type="dept-select" '
			+ 'onClick="ss.initKitchenContent({event:this, deptId:{value}})">{text}</div>',
		kitchen : '<div class="button-base" data-value={value} data-type="kitchen-select" '
			+ 'onClick="ss.findFoodByKitchen({event:this, kitchenId:{value}})">{text}</div>',
		boxFood : '<div data-index={dataIndex} data-value={id} class="main-box-base" onClick="{click}">'
			+ '{name}<br>' 
			+ '¥{unitPrice}'
			+ '</div>',
		newFood : '<div data-index={dataIndex} data-value={id} data-type="newFood-select" onClick="ss.selectNewFood({event:this, foodId:{id}})">'
			+ '<div style="line-height: 40px; ">{name}</div>'
  			+ '<div style="text-align: right;padding-right: 5px;">'
  				+ '<div>¥:{unitPrice}</div>'
  			+'</div>'
  			+ '</div>',
  		newFoodType : 'sellOut'
	}
};

/**
 * 沽清之后更新数据
 */
function updateFoodData(){
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'foods',
			restaurantID : restaurantID
		},
		success : function(data, status, xhr){
			if(data.success){
				//重新赋值
				foodData = {root:data.root};
				var tempFoodData = foodData.root.slice(0);
				
				for(var j=0; j < kitchenData.root.length; j++){
					var temp=kitchenData.root[j];
					temp.foods=[];
					for(var i=0; i < tempFoodData.length; i++){
						if(tempFoodData[i].kitchen.id == temp.id){
							temp.foods.push(tempFoodData[i]);
						}else if(temp.id == -1){
							temp.foods = foodData.root.slice(0);
						}
					}
				}
				
				//清除没有菜品的厨房
				for(var i = kitchenData.root.length - 1; i >= 0; i--){
					if(kitchenData.root[i].foods.length <= 0){
						kitchenData.root.splice(i, 1);
					}
				}
				
				
			}
		}
	});
}


/**
 * 初始化菜品数据
 */
function initFoodData(){
	Util.LM.show();
	// 加载菜品数据
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'foodList'
		},
		success : function(data, status, xhr){
			
			Util.LM.hide();
			if(data.success){
				
				var deptNodes = data.root;
				deptData = {root:[]};
				
				foodData = {root:data.other.foodList};
				
				kitchenData = {totalProperty:0, root:[]};
				
				for (var i = 0; i < deptNodes.length; i++) {
					deptData.root.push(deptNodes[i].deptNodeKey);
					for (var j = 0; j < deptNodes[i].deptNodeValue.length; j++) {
						var kitNode = deptNodes[i].deptNodeValue[j];
						kitNode.kitchenNodeKey.foods = kitNode.kitchenNodeValue.foodList;
						
						kitchenData.root.push(kitNode.kitchenNodeKey);
					}
				}
				
				deptData.totalProperty = deptData.root.length;
				
				kitchenData.totalProperty = kitchenData.root.length;
				
				deptData.root.unshift({
					id : -1,
					name : '全部部门'
				});				
				kitchenData.root.unshift({
					id : -1,
					name : '全部分厨',
					dept : {
						id : -1
					},
					foods : foodData.root.slice(0)
				});
			}
			
			//清除没有菜品的厨房
			for(var i = kitchenData.root.length - 1; i >= 0; i--){
				if(kitchenData.root[i].foods.length <= 0){
					kitchenData.root.splice(i, 1);
				}
			}
			
			// 加载临时菜打印分厨
			$.ajax({
				url : '../QueryMenu.do',
				type : 'post',
				data : {
					dataSource : 'isAllowTempKitchen'
				},
				success : function(data, status, xhr){
					if(data.success){
						allowTempKitchen = data;
					}else{
						alert('初始化临时菜打印分厨数据失败.');
					}
				},
				error : function(request, status, err){
					alert('初始化临时菜打印分厨数据失败.');
				}
			});
			
			// 加载口味信息
			$.ajax({
				url : '../QueryMenu.do',
				type : 'post',
				data : {
					dataSource : 'tastes',
					restaurantID : restaurantID
				},
				success : function(data, status, xhr){
					if(data.success){
						tasteData = {};
						tasteData = {totalProperty:data.root.length, root:data.root.slice(0)};
					}else{
						alert('初始化口味数据失败.');
					}
				},
				error : function(request, status, err){
					alert('初始化口味数据失败.');
				}
			});
			
		}
	});
	

/*	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'foods',
			restaurantID : restaurantID
		},
		success : function(data, status, xhr){
			if(data.success){
				foodDataBase = data;
				// 加载口味信息
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {
						dataSource : 'tastes',
						restaurantID : restaurantID
					},
					success : function(data, status, xhr){
						if(data.success){
							tasteData = {};
							tasteData = {totalProperty:data.root.length, root:data.root.slice(0)};
						}else{
							alert('初始化口味数据失败.');
						}
					},
					error : function(request, status, err){
						alert('初始化口味数据失败.');
					}
				});
				
				var tmpKitchen=null;
				for(var i=0; i < data.root.length; i++){
					tmpKitchen={
						id : data.root[i].kitchen.id,
						alias : data.root[i].kitchen.alias,
						dept : {
							id : data.root[i].kitchen.dept.id
						}
					};
					data.root[i].kitchen = tmpKitchen;
				}
				tmpKitchen = null;
//				localStorage.setItem('foods', JSON.stringify(data));
				
				foodData = {root:[]};
				for(var i = 0; i < data.root.length; i++){
					if((data.root[i].status & 1 << 2) == 0)
						foodData.root.push(data.root[i]);
				}
				foodData.totalProperty = foodData.root.length;
				
				// 加载部门数据,分厨数据
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {
						dataSource : 'kitchens',
						restaurantID : restaurantID
					},
					success : function(data, status, xhr){
						Util.LM.hide();
						if(data.success){
							
							kitchenData = {totalProperty:data.root.length, root:data.root.slice(0)};
							kitchenFoodData = {totalProperty:data.root.length, root:data.root.slice(0)};
							kitchenAllFoodData = {totalProperty:data.root.length, root:data.root.slice(0)};
							var tempFoodData = foodData.root.slice(0);
							deptData.root.push(kitchenData.root[0].dept);
							for(var j=0; j < kitchenFoodData.root.length; j++){
								var temp=kitchenFoodData.root[j];
								temp.foods=[];
								for(var i=0; i < tempFoodData.length; i++){
									if(tempFoodData[i].kitchen.id == temp.id){
										temp.foods.push(tempFoodData[i]);
									}
								}
								var hasDept=false;
								for(var i=0; i < deptData.root.length; i++){
									if(deptData.root[i].id == temp.dept.id){
										hasDept=true;
									}
								}
								if(!hasDept){
									deptData.root.push(temp.dept);
								}
							}
							
							deptData.totalProperty = deptData.root.length;
							deptData.root.unshift({
								id : -1,
								name : '全部部门'
							});
							kitchenData.root.unshift({
								id : -1,
								name : '全部分厨',
								dept : {
									id : -1
								},
								foods : foodData.root.slice(0)
							});
							kitchenFoodData.root.unshift({
								id : -1,
								name : '全部分厨',
								dept : {
									id : -1
								},
								foods : foodData.root.slice(0)
							});
							
							
							tempFoodData = foodDataBase.root.slice(0);
							for(var j=0; j < kitchenAllFoodData.root.length; j++){
								var temp=kitchenAllFoodData.root[j];
								temp.foods=[];
								for(var i=0; i < tempFoodData.length; i++){
									if(tempFoodData[i].kitchen.id == temp.id){
										temp.foods.push(tempFoodData[i]);
									}
								}
							}
							//设置一个包含所有food的厨房s
							kitchenAllFoodData.root.unshift({
								id : -1,
								name : '全部分厨',
								dept : {
									id : -1
								},
								foods : foodDataBase.root.slice(0)
							});
							// 清理多余数据
							for(var i = kitchenFoodData.root.length - 1; i >= 0; i--){
								if(kitchenFoodData.root[i].foods.length <= 0){
									for(var k=kitchenData.root.length - 1; k >= 0; k--){
										if(kitchenData.root[k].id == kitchenFoodData.root[i].id){
											kitchenFoodData.root.splice(i, 1);
											kitchenData.root.splice(k, 1);
											break;
										}
									}
								}
							}
//							localStorage.setItem('dept', JSON.stringify(deptData));
//							localStorage.setItem('kitchen', JSON.stringify(kitchenData));
						}else{
							alert('初始化分厨数据失败.');
						}
					},
					error : function(request, status, err){
						Util.LM.hide();
						alert('初始化分厨数据失败.');
					}
				});
				
				// 加载临时菜打印分厨
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {
						dataSource : 'isAllowTempKitchen'
					},
					success : function(data, status, xhr){
						if(data.success){
							allowTempKitchen = data;
						}else{
							alert('初始化临时菜打印分厨数据失败.');
						}
					},
					error : function(request, status, err){
						alert('初始化临时菜打印分厨数据失败.');
					}
				});
				
			}else{
				Util.LM.hide();
				alert('初始化菜品数据失败.');
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			alert('初始化菜品数据失败.');
		}
	});
*/

}

/**
 * 初始化餐厅登陆界面
 */
function initRestaurantContent(){
	Util.dialongDisplay({
		renderTo : 'divRestaurantLogin',
		type : 'show',
		isTop : true
	});
}


/**
 * 初始化员工登陆界面
 */
function initStaffContent(c){
	$.ajax({
		url : '../QueryStaff.do',
		data : {
			restaurantID : restaurantID
		},
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				ln.restaurant=data.other.restaurant;
				Util.dialongDisplay({
					renderTo : 'divUserLogin',
					type : 'show',
					isTop : true
				});	
				var el = $("#divUserLogin");
				var bg = $('div[forbg=divUserLogin]');
				if(bg.length <= 0){
					el.before('<div forbg="divUserLogin" style="position: absolute; top:0; left:0; width: 100%; height: 100%;"></div>');
					bg = $('div[forbg=divUserLogin]');
					bg.css("background", 'url(../images/login_bg.jpg) no-repeat');
					bg.css("backgroundSize", 'cover');
					var html="<div class='box-vertical' style='width : 100%; height: 100%'>"
						+ "<div style='line-height : 100px; font-size : 50px; font-weight: bold; margin-left : 80px;'>"
						+ "<span style='color : red'>" + ln.restaurant.name + "</span>欢迎您</div>"
						+ "<div class='div-full'></div>"
						+ "<div style='line-height : 100px; text-align : right; margin-right : 100px; font-size : 20px;'>" 
						+ "智易科技：www.digi-e.com</div>"
						+ "</div>";
					bg.html(html);
				}
				if(bg.hasClass('dialong-lm-hide-top')){
					bg.removeClass('dialong-lm-hide-top');
				}
				bg.addClass('dialong-lm-show-top');
				if(data.root.length > 18){
					
				}
				var html=[];
				for(var i=0; i < data.root.length; i++){
					html.push(Templet.ul.allStaff.format({
						id : data.root[i].staffID,
						name : data.root[i].staffName
					}));
				}
				$('#divAllStaffForUserLogin').html(html.join(''));
			}else{
				Util.msg.alert({
					msg : '获取餐厅员工信息失败, 请联系客服员.'
				});
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				msg : '获取餐厅员工信息失败, 请联系客服员.'
			});
		}
	});
}

/**
 * onload
 */
$(function(){
	if (getcookie("digie_restaurant") != ""){
		var restaurant = JSON.parse(getcookie("digie_restaurant"));
		ln.restaurant = restaurant;
		restaurantID = restaurant.id;
		$.ajax({
			url : '../VerifyLogin.do',
			success : function(data, status, xhr){
				if(data.success){
					staffData = data.other.staff;
					loginSuccessCallback();
				}else{	
					initStaffContent();
				}
			},
			error : function(request, status, error){
				initStaffContent();
			}
		});
	}else{
		initRestaurantContent();
	}
	
	$('#divCFCOHandlerMore').toggle(
		function(){
			$('#divCFCOHandlerMoreContent').show();
		},
		function(){
			$('#divCFCOHandlerMoreContent').hide();
		}
	);
	
});

/**
 * 
 * @param c
 */
function changeStaff(c){
	if(c == null || typeof c.type != 'number'){
		return;
	}
	var sl=$('#divAllStaffForUserLogin > div');
	for(var i=0; i< sl.length; i++){
		$(sl[i]).removeClass('div-staff-select');
	}
	var pwd=getDom('txtStaffLoginPwd');
	var name=getDom('spanStaffNameDisplay');
	if(c.type == 1 && typeof c.staffId == 'number'){
		$(c.event).addClass('div-staff-select');
		name.innerHTML = c.event.innerText;
		pwd.value = '';
	}else if(c.type == 2){
		pwd.value='';
		name.innerHTML='----';
	}
	pwd=null;
	name=null;
}

/**
 * 
 */
function setValueToPwd(c){
	var pwd=getDom('txtStaffLoginPwd');
	if(c.type === 1){
		pwd.value=pwd.value.substring(0, pwd.value.length - 1);
	}else if(c.type === 2){
		pwd.value='';
	}else{
		pwd.value=pwd.value + '' + c.value;
	}
	pwd.focus();
}

/**
 * 餐厅登录
 */
function restaurantLoginHandler(){
	var account=getDom('txtRestaurantAccount');
	if(account.value.length == 0){
		Util.msg.alert({
			msg : '请输入餐厅帐号.'
		});
		return;
	}
	Util.LM.show();
	$.ajax({
		url : '../QueryRestaurants.do',
		data : {
			account : account.value
		},
		dataType : 'json',
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				if(data.root.length != 0){
					setcookie("digie_restaurant", JSON.stringify(data.root[0]));
					Util.dialongDisplay({
						renderTo : 'divRestaurantLogin',
						type : 'hide'
					});
					ln.restaurant=data.root[0];
					restaurantID=ln.restaurant.id;
					Util.LM.show();
					initStaffContent();
				}else{
					Util.msg.alert({
						title : "温馨提示" ,
						msg : "餐厅帐号错误,请检查后重新输入",
						time : 3
					});
				}
			}else{
				Util.msg.alert({
					msg : data.msg
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
 * 登陆
 */
function staffLoginHandler(c){
	var temp=null, staffId=0, sl=$('#divAllStaffForUserLogin > div');
	var pwd=getDom('txtStaffLoginPwd');
	for(var i=0; i< sl.length; i++){
		temp=$(sl[i]);
		if(temp.hasClass('div-staff-select')){
			staffId=temp.attr('data-value');
			break;
		}
	}
	if(staffId == 0){
		Util.msg.alert({
			msg : '请选择一个员工.'
		});
		return;
	}
	if(pwd.value.length == 0){
		Util.msg.alert({
			msg : '请输入密码.'
		});
		return;
	}
	
	Util.LM.show();
	$.ajax({
		url : '../OperateStaff.do',
		data : {
			pin : staffId,
			comeFrom : 3,
			pwd : MD5(pwd.value.trim())
		},
		success : function(data, status, xhr){
			Util.LM.hide();
			if(data.success){
				pin = staffId;
				staffData = data.other.staff;
//				for (var index = 0; index < staffData.role.privileges.length; index++) {
//					var temp = staffData.role.privileges[index];
//					if(temp.codeValue == 1003){
//						Wireless.ux.staffGift = true;
//						$('#divOperateGiftFood').show();
//						temp = null;
//						break;
//					}
//				}		
				//验证员工权限	
				$.ajax({
					url : "../QueryStaff.do",
					type : 'post',
					async:false,
					data : {
						"privileges" : 1003,
						"checkPrivilege" : true
					},
					success : function(jr, status, xhr){
						if(jr.success){
							if(jr.other.havePrivileges != null){
								Wireless.ux.staffGift = true;
								$('#divOperateGiftFood').show();
							}
						}
					},
					error : function(request, status, err){
					}
				}); 	
				
				if(c.refresh === true){
					loginSuccessCallback();
				}else{
					initTableData();
				}
				Util.dialongDisplay({
					renderTo : 'divUserLogin',
					type : 'hide'
				});
				var bg = $('div[forbg=divUserLogin]');
				if(bg.hasClass('dialong-lm-show-top')){
					bg.removeClass('dialong-lm-show-top');
				}
				bg.addClass('dialong-lm-hide-top');
			}else{
				Util.msg.alert({
					msg : data.msg
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
 * 
 */
function loginSuccessCallback(){
//	alert(foodData.root)
	//如果是注销再登录则不用再更新菜谱
	if(typeof foodData.root == 'undefined'){
		initFoodData();
		initTableData();
	}
	$('#spanStaffNameForDisplayToTS').html(staffData.staffName);
	Util.toggleContentDisplay({
		type:'show', 
		renderTo:'divTableSelect'
	});
	
	changeStaff({type:2});
}
/**
 * 
 */
function logout(){
	Util.LM.show();
	$.ajax({
		url : '../LoginOut.do',
		success : function(data, status, xhr){
			initStaffContent();
		}
	});
}

