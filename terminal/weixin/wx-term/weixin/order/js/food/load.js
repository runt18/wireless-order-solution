$(function(){
	
	$.ajax({
		url : '../../WxInterface.do',
		dataType : 'json',
		type : 'post',
		data : {
			dataSource : 'jsApiSign',
			url: location.href.split('#')[0],
			fid : Util.mp.fid
		},
		success : function(data, status, xhr){
			wx.config({
			    debug: false,  // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
			    appId: data.other.appId,  // 必填，公众号的唯一标识
			    timestamp: data.other.timestamp,  // 必填，生成签名的时间戳
			    nonceStr: data.other.nonceStr,  // 必填，生成签名的随机串
			    signature: data.other.signature, // 必填，签名，见附录1
			    jsApiList: ["scanQRCode"]  // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
			});		
		},
		error : function(xhr, errorType, error){
			alert('操作失误, 请重新进入');
		}
	});	
	
	 //自助点餐
	 var pickFoodComponent = new PickFoodComponent({
		 confirm : function(orderFoodData, comment, container){
			if(orderFoodData.length == 0){
				var dialog = new DialogPopup({
					content : '你的购物车没有菜品,请先选菜',
					left : function(){
						dialog.close();
					}
				})
				dialog.open();
				return;
			}
			Util.lm.show();
			$.ajax({
				url : '../../WXOperateMember.do',
				type : 'post',
				data : {
					dataSource : 'getByCond',
					oid : Util.mp.oid,
					fid : Util.mp.fid
				},
				dataType : 'json',
				success : function(data){
//					$("#shoppingCarSelect_li_fastOrderFood").removeAttr("disabled");
					if(data.success){
						Util.lm.hide();
						if(data.root[0].isRaw){
							var dialogMemberBind = new DialogPopup({
								titleText : '请完善会员资料',
								content : '<div style="width: 100%;">'
											+'<ul class="m-b-list">'
												+'<li class="none-line" style="line-height: 50px;padding-top: 10px;">'
													+'手机号码: <input data-type="mobileNum_input_member" style="font-size: 20px;padding: 3px 5px 3px 5px;width: 120px;"  type="tel"  maxlength="11"/>'			
												+'</li>'		
												+'<li class="none-line" style="line-height: 50px;">'	
													+'会员姓名: <input data-type="mobileName_input_member" style="font-size: 20px;padding: 3px 5px 3px 5px;width: 120px;">'			
												+'</li>'	
												+'性别:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<div data-type="personSex" data-value="0" class="region_css_book selectedRegion_css_book" style="width:31%;" href="#">'	
													+'<ul class="m-b-list">先生</ul>'
												+'</div>'
												+'<div data-type="personSex" data-value="1" class="region_css_book" style="width:31%;" href="#">'
													+'<ul class="m-b-list">女士</ul>'
												+'</div>'
												+'</li>'
												+'<li class="box-horizontal none-line" style="line-height: 50px;font-size:18px;">'
												+'<li class="none-line" style="line-height: 50px;">'	
													+'年龄段: &nbsp;&nbsp;&nbsp;&nbsp;<select data-type="age_select_member" style="font-size: 20px;padding: 3px 5px 3px 5px;width: 120px;">'
														+'<option value ="5">00后</option>'  
														+'<option value ="4">90后</option>' 
														+'<option value="3">80后</option>' 
														+'<option value="2">70后</option>' 
														+'<option value="1">60后</option>' 
														+'<option value="6">50后</option>' 
														+'</select>'			
												+'</li>'
												+'<li class="none-line" style="line-height: 50px;">'
														+'会员生日: <input data-type="birthdayMonth_input_member"  type="tel" class="txtVerifyMobile" style="font-size:20px;width:45px;">&nbsp;月'
													+'&nbsp;<input data-type="birthdayDay_input_member"  type="tel" class="txtVerifyMobile" style="font-size:20px;width:45px;">&nbsp;日'
												+'</li>'
											+'</ul>'	
										 +'</div>',
								contentCallback : function(diaologDiv){
									diaologDiv.find('[data-type="age_select_member"]').val("3");
									
									//会员生日判定
									//月
									diaologDiv.find('[data-type="birthdayMonth_input_member"]').on('blur', function(){
										var month = parseInt(diaologDiv.find('[data-type="birthdayMonth_input_member"]').val());
										if(month < 1 || month > 12){
											Util.dialog.show({title:'提示', msg: '月份输入错误,请重新输入', btn : 'yes', callback : function(){
												diaologDiv.find('[data-type="birthdayMonth_input_member"]').val('');
												$diaologDiv.find('[data-type="birthdayMonth_input_member"]').focus();
											}});
										}
									});
									//日
									diaologDiv.find('[data-type="birthdayDay_input_member"]').on('blur', function(){
										var month = parseInt(diaologDiv.find('[data-type="birthdayDay_input_member"]').val());
										if(month < 1 || month > 31){
											Util.dialog.show({title:'提示', msg: '天数输入错误,请重新输入', btn : 'yes', callback : function(){
												diaologDiv.find('[data-type="birthdayDay_input_member"]').val('');
												diaologDiv.find('[data-type="birthdayDay_input_member"]').focus();
											}});
										}
									});
									
									//性别点击
									diaologDiv.find('[data-type="personSex"]').each(function(index, element){
										element.onclick = function(){
											if($(element).hasClass('selectedRegion_css_book')){
												$(element).addClass('selectedRegion_css_book');
											}else{
												diaologDiv.find('[data-type="personSex"]').removeClass('selectedRegion_css_book');
												$(element).addClass('selectedRegion_css_book');
											}
										}
									});
									
								},
								leftText : '确认',
								left : function(diaologDiv){
									var mobile = diaologDiv.find('[data-type="mobileNum_input_member"]').val().trim();
									if(!/^1[3,5,8][0-9]{9}$/.test(mobile)){
										Util.dialog.show({
											msg: '请输入 11 位纯数字的有效手机号码',
											callback : function(){
												diaologDiv.find('[data-type="mobileNum_input_member"]').select();
											}
										});
										return;
									}
									
									var name = diaologDiv.find('[data-type="mobileName_input_member"]').val();
									if(name == ''){
										Util.dialog.show({
											msg: '会员名不能为空',
											callback : function(){
												diaologDiv.find('[data-type="mobileName_input_member"]').select();
											}
										});
										return;
									}
									
									var month =  diaologDiv.find('[data-type="birthdayMonth_input_member"]').val();
									var day = diaologDiv.find('[data-type="birthdayDay_input_member"]').val();
									if(month == '' || day == ''){
										Util.dialog.show({
											msg: '生日不能为空',
											callback : function(){
												diaologDiv.find('[data-type="birthdayMonth_input_member"]').focus();
											},
											btn: 'yes'
										});
										return;
									}
									
									
									var birthday = month + '-' + day;
									var age = diaologDiv.find('[data-type="age_select_member"]').val();
									var sex;
									diaologDiv.find('[data-type="personSex"]').each(function(index, element){
										if($(element).hasClass('selectedRegion_css_book')){
											sex = $(element).attr('data-value');
										}
									});
									
									
									$.ajax({
										url : '../../WXOperateMember.do',
										type : 'post',
										data : {
											dataSource : 'bind',
											oid : Util.mp.oid,
											fid : Util.mp.fid,
											mobile : mobile,
											name : name,
											birthday : birthday,
											sex : sex,
											age : age
										},
										dataType : 'json',
										success : function(data, status, xhr){
											if(data.success){
												dialogMemberBind.close(function(){
													commit();
												}, 200);
											}else{
												Util.dialog.show({msg: data.msg});
											}					
										}
									});
								}
							});
							dialogMemberBind.open();
						}else{
							commit();
						}
					}
				}
			});
			
			function commit(){
			  var foods = "";
			  var unitId = 0;
			  orderFoodData.forEach(function(e, index){
				 if(index > 0){
					 foods += '&';
				 }
				 if(e.unitPriceId){
					 unitId = e.unitPriceId;
				 }else{
					 unitId = 0;
				 }
				 foods += (e.id + ',' + e.count + ',' + unitId);
			  });
			   Util.lm.show();
			  $.ajax({
				url : '../../WxOperateOrder.do',
				dataType : 'json',
				type : 'post',
				data : {
					dataSource : 'insertOrder',
					oid : Util.mp.oid,
					fid : Util.mp.fid,
					foods : foods,
					comment : comment
				},
				success : function(data, status, xhr){
					if(data.success){
						 Util.lm.hide();
						var dialogOrder = new DialogPopup({
							titleText : '温馨提示',
							leftText : '自助扫码',
							content : '<font style="font-weight:bold;font-size:25px;color:blue;">订单号: ' + data.other.code + '</font><br><font style="color:green;">1.您可呼叫服务员来确认订单</font><br><font style="color:green;">2.您可选择自助扫描二维码下单</font>',
							left : function(){	
								wx.scanQRCode({ 
								    needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
								    scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
								    success: function (res) {
								    	if(res.resultStr.split('?').length == 1){
								    		//url不带餐桌号就要输入台号
								    		dialogOrder.close(function(){
												var dialog = new DialogPopup({
									    			titleText : '请输入台号',
									    			leftText : '确认提交',
								 	    			content : '<h3>请输入台号:<a data-type="numberInput_a_load" style="color:red;"></a></h3><br/>'
										    					+'<div data-type="numberKyes_div_load" style="margin-top:-40px;"></div>',
										    		contentCallback : function(dialogDiv){
										    			dialogDiv.find('[data-type="numberKyes_div_load"]').width(dialogDiv.width());
									    				var numKeys = new Array("7", "8", "9", "0", "4", "5", "6", "删除", "1", "2", "3", "清空");
										    			var keys = "";
										    			for(var i = 0; i < numKeys.length; i++){
										    				var eachButton = '<input type="button" class="a_demo_two" style="width:60px;height:55px;font-size:18px;" value="' + numKeys[i] + '">';
										    				if(i % 4 == 0){
										    					keys += '<br/>';
										    				}
										    				keys += eachButton;
										    			}	
										    			dialogDiv.find('[data-type="numberKyes_div_load"]').append(keys);
										    			dialogDiv.find('[data-type="numberKyes_div_load"] input').each(function(index, element){
										    				element.onclick = function(){
										    					if($(element).val() == '删除'){
										    						var s = dialogDiv.find('[data-type="numberInput_a_load"]').text();
										    						dialogDiv.find('[data-type="numberInput_a_load"]').text(s.substring(0, s.length - 1));
										    					}else if($(element).val() == '清空'){
										    						dialogDiv.find('[data-type="numberInput_a_load"]').text('');
										    					}else{
										    						dialogDiv.find('[data-type="numberInput_a_load"]').text(dialogDiv.find('[data-type="numberInput_a_load"]').text() + $(element).val());
										    					}
										    				}
										    			});
										    		},
										    		left : function(dialogDiv){
										    			if(dialogDiv.find('[data-type="numberInput_a_load"]').text() == ""){
										    				Util.dialog.show({ msg : '餐桌号不能为空', btn :'yes'});
										    			}else{
										    				 Util.lm.show();
															 $.ajax({
																url : '../../WxOperateOrder.do',
																dataType : 'json',
																type : 'post',
																data : {
																	dataSource : 'self',
																	oid : Util.mp.oid,
																	fid : Util.mp.fid,
																	wid : data.other.id,
																	tableAlias : dialogDiv.find('[data-type="numberInput_a_load"]').text()
			//																	qrCode :  res.resultStr.split('?')[0]
																},
																success : function(data, status, xhr){
																	Util.lm.hide();
																	if(data.success){
																		dialog.close(function(){
																				//刷新界面
																			pickFoodComponent.refresh();
																			pickFoodComponent.closeShopping();
																			
																			var dialogClose = new DialogPopup({
																				content : '下单成功',
																				titleText : '温馨提示',
																				left : function(){
																					dialogClose.close();
																					window.location.reload();
																					 $('#foodOrderList').click();
																				}
																			})
																			dialogClose.open();
																		}, 200);
																	}else{
																		Util.dialog.show({ msg : data.msg });
																	}
																},
																error : function(xhr, errorType, error){
																	Util.lm.hide();
																	Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
																}
															});
										    			}
										    		}
									    		});
									    		dialog.open(); 
											}, 200);
								    	}else{
										 	 dialogOrder.close(function(){
										 	 	 Util.lm.show();
										 	 	 $.ajax({
													url : '../../WxOperateOrder.do',
													dataType : 'json',
													type : 'post',
													data : {
														dataSource : 'self',
														oid : Util.mp.oid,
														fid : Util.mp.fid,
														wid : data.other.id,
														tableAlias : res.resultStr.split('?')[1],
														qrCode :  res.resultStr.split('?')[0]
													},
													success : function(data, status, xhr){
														Util.lm.hide();
														if(data.success){
															//刷新界面
															pickFoodComponent.refresh();
															pickFoodComponent.closeShopping();
															
															var dialogClose = new DialogPopup({
																content : '下单成功',
																titleText : '温馨提示',
																left : function(){
																	dialogClose.close();
																	window.location.reload();
																	 $('#foodOrderList').click();
																}
															})
															dialogClose.open();
														}else{
															Util.dialog.show({ msg : data.msg });
														}
													},
													error : function(xhr, errorType, error){
														Util.lm.hide();
														Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
													}
												});
										 	 }, 200);
								    	}
								    }
								});
							},
							afterClose : function(){
								dialogOrder.close();
								window.location.reload();
							 	$('#foodOrderList').click();
							}
						});
						dialogOrder.open();	
				 	}else{
				 		Util.dialog.show({ msg : data.msg });
				 	}
				},
				error : function(xhr, errorType, error){
					Util.lm.hide();
					Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
				}
			  });
			}
		 },
		 bottomId : 'bottom',
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
	 
	 pickFoodComponent.open();
	 
	  //打开购物车
	  $('#shoppingCar_li_member').click(function(){
		  pickFoodComponent.openShopping();
	  });
	  
	  
	  $('#foodOrderList').click(function(){
		  Util.jump('orderList.html', Util.mp.extra);
	  });
});
