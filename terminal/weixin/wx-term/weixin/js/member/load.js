$(function(){
	$.ajaxSetup({
		timeout : 10 * 1000      //设置timeout时间为10s
	})
	
	function fnDateInChinese(date){
		var month = date.substring(5, 7);
		var day = date.substring(8, 10);
		var time = date.substring(11, date.length - 3);
		
		return month+ '月' +day + '日' + ' ' + time;
		
	}
	
	var member;
	var currentMemberLevelData;

	function refresh(){
		//获取用户头像
		$.ajax({
			url : '../../../WXOperateMember.do',
			type : 'post',
			data : {
				dataSource : 'getUserMsg',   
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data){
				if(data.success){
					$('#headImg_div_member').css({
						'background': 'url("' + data.root[0].headimgurl + '")',
						'background-size' : '100% 100%'
					});
					
					$('#userName_div_member').text(data.root[0].nickName);
					
				}else{
					$('#headImg_div_member').css({
						'background': 'url("../../images/userHead.jpg")',
						'background-size' : '100% 100%'
					});
				}		
			},
			error : function(){
				$('#headImg_div_member').css({
					'background': 'url("../../images/userHead.jpg")',
					'background-size' : '100% 100%'
				});
			}
		});
		
		//获取餐厅图片
		$.ajax({
			url : '../../../WxOperateRestaurant.do',
			data : {
				dataSource : 'getByCond',
				oid : Util.mp.oid,
				fid : Util.mp.fid,
				sessionId : Util.mp.params.sessionId
			},
			type : 'post',
			dataType : 'json',
			success : function(data, status, req){
				if(data.success){
					if(data.root[0].wxCardImg){
						$('#divMemberCard').css({
							'background': 'url("' + data.root[0].wxCardImg.image + '")',
							'background-size' : '100% 100%'
						});
					}else{
						$('#divMemberCard').css({
							'background': 'url("../../images/VIP.jpg")',
							'background-size' : '100% 100%'
						});
					}
					
				}else{
					$('#divMemberCard').css({
						'background': 'url("../../images/VIP.jpg")',
						'background-size' : '100% 100%'
					});
				}
			},
			error : function(req, status, err){
				$('#divMemberCard').css({
					'background': 'url("../../images/VIP.jpg")',
					'background-size' : '100% 100%'
				});
			}
		});
		
		//获取赠送余额说明
		$.ajax({
			url : '../../../WxOperateRepresent.do',
			data : {
				dataSource : 'getByCond',
				fid : Util.mp.fid
			},
			type : 'post',
			dataType : 'json',
			success : function(res, status, req){
				if(res.success){
					$('#memberExtraBalanceDesc_div_member').show();
					$('#memberExtraBalanceDesc_div_member').html(res.root[0].giftDesc ? res.root[0].giftDesc : '');
					if(!res.root[0].giftDesc){
						$('#memberExtraBalanceDesc_div_member').hide();		
					}
				}else{
					$('#memberExtraBalanceDesc_div_member').hide();		
				}
			},
			error : function(req, status, err){
				$('#memberExtraBalanceDesc_div_member').hide();		
			}
		});
		
		//获取用户最近消费和最近充值
		$.ajax({
			url : '../../../WXQueryMemberOperation.do',
			type : 'post',
			data : {
				dataSource : 'recent',
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data, status, xhr){
				
				if(data.other.nearByCharge >= 0){
					$('#newRecharge_font_member').text("(最近充值 : " + data.other.nearByCharge + "元)");
				}
				
				if(data.other.nearByConsume >= 0){
					$('#memberConsume_font_member').html("(最新消费 : " + data.other.nearByConsume + "元)");
				}
				
				
			},
			error : function(data, errotType, eeor){
				Util.dialog.show({msg: '服务器请求超时, 请刷新.'});
			}
		});	
		
		//获取用户信息
		$.ajax({
			url : '../../../WXOperateMember.do',
			type : 'post',
			data : {
				dataSource : 'getInfo',
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data, status, xhr){
				if(data.success){
					member = data.other.member;
					member.restaurant = data.other.restaurant;
					//添加会员等级当前位置
					currentMemberLevelData = {y : 0, memberTypeName : '您的积分', currentPoint:true, x:member.totalPoint, pointThreshold:member.totalPoint, discount:{type :2},chargeRate:-1, exchangeRate:-1, marker:{symbol:'url(images/currentPosition.png)'}, color : 'red', dataLabels : {x:-1, align : 'right', style : {fontWeight: 'bold',color: 'red'}}};
					
					if(member.name != ''){
						if(member.isRaw){
							$('#userName_div_member').text(member.name + ' (未完善)');
						}else{
							$('#userName_div_member').text(member.name);
						}
						
					}
					
					if(member.mobile != ''){
						$('#memberMobile_font_member').text("手机号 : " + member.mobile);
					}else{
						$('#memberMobile_font_member').text("微信卡号  : " + member.weixinCard);
					}
					
					$('#memberLbelName_font_member').text(member.memberType.name);
					$('#banlance_font_member').text(member.baseBalance);
					$('#extra_font_member').text(member.extraBalance);
					$('#memberCommission_font_member').text("(总佣金 : " + member.totalCommission +"元)")
					$('#memberFans_font_member').html("(粉丝数 : " + member.fansAmount + ")");
					
					
					$('#point_font_member').text(member.point);
				}
			}
		});
		
		//获取用户的优惠券张数
		$.ajax({
			url : '../../../WxOperateCoupon.do',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				status : 'issued',
				filter : '1',
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data, status, xhr){
				if(data.success){
					if(data.root.length > 0){
						$('#couponAmount_font_member').text(data.root.length);
						$('#myCouponAmount_font_member').text("(优惠券 : " + data.root.length + "张)");
					}else{
						$('#couponAmount_font_member').text('0');
						$('#myCouponAmount_font_member').text("(优惠券:0)");
					}
				}
			}
		});
	}
	
	refresh();
	
	//点击头像事件
	$('#headImg_div_member').click(function(){
		var memberMsgDialog;
		memberMsgDialog = new WeDialogPopup({
			titleText : '会员资料',
			contentCallback : function(container){
				wxLoadDialog.instance().show();
				if(member.isRaw){
					var bindMember = '<div class="weui_cells weui_celss_form weui_cells_radio" style="color:black;">'
										+'<div class="weui_cell">'
											+'<div class="weui_cell_hd"> '
												+'<label class="weui_label">姓名:</label>'
											+'</div>'
											+'<div class="weui_cell_bd weui_cell_primary">' 
												+'<input id="name_input_member" class="weui_input" placeholder="请输入姓名"></input>'
			
												+'</div>'
										+'</div>'
										
										+'<div class="weui_cell">'
											+'<div class="weui_cell_hd"> '
												+'<label  class="weui_label">手机:</label>'
											+'</div>'
											+'<div class="weui_cell_bd weui_cell_primary">' 
												+'<input id="mobile_input_member" style="border:0" class="weui_input" type="number" placeholder="请输入手机号"></input>'
											+'</div>'
										+'</div>'
										
										+'<div class="weui_cell">'
											+'<div class="weui_cell_hd"> '
												+'<label  class="weui_label">生日:</label>'
											+'</div>'
											+'<div class="weui_cell_bd weui_cell_primary">' 
												+'<input id="birthday_input_member" style="border:0" class="weui_input" type="date" placeholder="请选择生日"></input>'
											+'</div>'
										+'</div>'
										 +'<div class="weui_cell weui_cell_select">' 
									 	+' <div class="weui_cells_title" style="color:black;">性别:</div>'
						                  +'<div class="weui_cell_bd weui_cell_primary">'  
						                      +'<select id="sex_select_member" class="weui_select" name="select1">'  
						                          +'<option selected="" value="0">先生</option>'  
						                           +'<option value="1">女士</option>' 
						                        +'</select>'
						                    +'</div>'
						                +'</div>'
							                
						                 +'<div class="weui_cell weui_cell_select">' 
										 +' <div class="weui_cells_title" style="color:black;">年龄段:</div>'
						                  +'<div class="weui_cell_bd weui_cell_primary">'  
						                      +'<select id="age_select_member" class="weui_select" name="select1">'  
						                          +'<option selected="" value="5">00后</option>'  
						                          +'<option selected="" value="4">90后</option>'  
						                          +'<option selected="" value="3">80后</option>'  
						                          +'<option selected="" value="2">70后</option>'  
						                          +'<option selected="" value="1">60后</option>'  
						                          +'<option selected="" value="6">50后</option>'  
						                        +'</select>'
						                    +'</div>'
						                +'</div>'
									 +'</div>';
									 
					container.find('[id="dialogContent_div_dialogPopup"]').html(bindMember);
					
					container.find('[id="age_select_member"]').val('3');
				}else{
					var table = '<table style="color:black">'
									+'<tr align="left">' 
										+'<td>姓名 : '+ member.name + '</td>'
										+'<td>性别 : '+ member.sexText + '</td>'
									+'</tr>'
									+'<tr align="left">' 
										+'<td>年龄段 : '+ member.ageText + '</td>'
										+'<td>生日 : '+ member.birthdayFormat + '</td>'
									+'</tr>'
									+'<tr align="left">' 
									+'<td>微信卡号: '+ member.weixinCard + '</td>'
									+'<td></td>'
									+'</tr>'
									+'<hr/>'
									+'<tr height="5px"></tr>'
									+'<tr>' 
										+'<td colspan="2"><img width="100%" height="90%" src="http://qr.topscan.com/api.php?text='+ member.weixinCard +'"></td>'
									+'</tr>'
								+'</table>';
					
					container.find('[id="dialogContent_div_dialogPopup"]').html(table);
					
				}
				
				wxLoadDialog.instance().hide();
				
			},
			leftText : '确定',
			left : function(container){
				if(member.isRaw){
					var errorDialog;
					errorDialog = new WeDialogPopup({
						titleText : '绑定失败',
						content : '<font color="black">姓名或者手机号或者生日不能为空</font>',
						leftText : '确认',
						left : function(){
							errorDialog.close();
						}
					});
					
					var memberName = container.find('[id="name_input_member"]').val();
					if(memberName == ''){
						errorDialog.open();
						return;
					}
					

					var sex = container.find('[id="sex_select_member"]').val();
					
					var mobile = container.find('[id="mobile_input_member"]').val();
					if(mobile == ''){
						errorDialog.open();
						return;
					}
					if(!/^1[3,5,8][0-9]{9}$/.test(mobile)){
						var mobileDialog;
						mobileDialog = new WeDialogPopup({
							titleText : '提示',
							content : '<font color="black">请输入 11 位纯数字的有效手机号码</font>',
							leftText : '确认',
							left : function(){
								mobileDialog.close();
							}
						});
						mobileDialog.open();
						return;
					}
					
					var birthday = container.find('[id="birthday_input_member"]').val();
					if(birthday == ''){
						errorDialog.open();
						return;
					}
					var age = container.find('[id="age_select_member"]').val();
					
					$.ajax({
						url : '../../../WXOperateMember.do',
						type : 'post',
						data : {
							dataSource : 'bind',
							oid : Util.mp.oid,
							fid : Util.mp.fid,
							mobile : mobile,
							name : memberName,
							birthday : birthday.substring(5),
							sex : sex,
							age : age
						},
						dataType : 'json',
						success : function(data, status, xhr){
							if(data.success){
								wxLoadDialog.success().show();
								
								setTimeout(function(){
									wxLoadDialog.success().hide();
									memberMsgDialog.close();
									refresh();
								}, 200);
							}else{
								var bindErrorDialog;
								bindErrorDialog = new WeDialogPopup({
									titleText : '绑定失败',
									content : '<font color="black">'+ data.msg +'</font>',
									leftText : '确认',
									left : function(){
										bindErrorDialog.close();
									}
								});
								bindErrorDialog.open();
							}					
						}
					});
				}else{
					memberMsgDialog.close();
				}
			}
		});
		memberMsgDialog.open();
	});
	
	//消费记录
	$('#consume_div_member').click(function(){
		
		var consumeDialog;
		consumeDialog = new WeDialogPopup({
			titleText : '消费记录',
			contentCallback : function(container){
				$.ajax({
					url : '../../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'consumeDetails',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					dataType : 'json',
					beforeSend  : function(){
						wxLoadDialog.instance().show();
					},
					success : function(data, status, xhr){
						if(data.success){
							var content = $('<table/>');
							content.addClass('d-list');		
							
							var tr = '<tr class="d-list-title">'
										+'<td style="width:45%;text-align: center;">消费时间</td>'
										+'<td style="width:30%;text-align:center;">消费额</td>'
										+'<td style="width:20%;text-align:center;">积分</td>'
									 +'</tr>';
							
							if(data.root.length > 0){
								
								var template = '<tr class="d-list-item-consume">' +
												'<td style="text-align: center;">{date}</td>' +
												'<td>{balance}</td>' +
												'<td>{point}</td>' +
											   '</tr>';
											   
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(template.format({
										date : fnDateInChinese(temp.operateDateFormat) + '</br><font style="font-size:13px;">账单号:' + temp.orderId + '</font>',
										balance : (checkDot(temp.deltaTotalMoney)?parseFloat(temp.deltaTotalMoney).toFixed(2) : temp.deltaTotalMoney) + '元',
										point : temp.deltaPoint.toFixed(0) + '分'
									}));
								}
								content.html(tr + html.join(''));
							}else{
								content.html('暂无消费记录');
							}
							
							container.find('[id="dialogContent_div_dialogPopup"]').html(content);
						}else{
							container.find('[id="dialogContent_div_dialogPopup"]').html(data.msg);				
						}
					},
					error : function(data, errotType, eeor){
						container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">服务器请求失败, 请稍候再试.</font>');
					},
					complete : function(){
						wxLoadDialog.instance().hide();
					}
				});				
			},
			leftText : '确认',
			left : function(){
				wxLoadDialog.instance().hide();
				consumeDialog.close();
			}
		});
		consumeDialog.open();
	});
	
	
	//代言记录
	$('#recomment_div_member').click(function(){
		var commentDialog;
		commentDialog = new WeDialogPopup({
			titleText : '代言记录',
			contentCallback : function(container){
				
				$.ajax({
					url : '../../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'recommendDetail',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					datatype : 'josn',
					beforeSend : function(){
						wxLoadDialog.instance().show();
					},
					success : function(data, status, res){
						if(data.success){
							var content = $('<table/>');
							content.addClass('d-list');	
							
							var tr = '<tr class="d-list-title">'
								+'<td style="width:39%;text-align: center;">关注时间</td>'
								+'<td style="width:27%;text-align:center;">粉丝名</td>'
								+'<td style="width:18%;text-align:center">金额</td>'
								+'<td style="width:15%;text-align:center">积分</td>'
							 +'</tr>';
							 
							if(data.root.length > 0){
								
								var template = '<tr style="color:#26A9D0;border-bottom:1px solid #999;">' + 
								'<td style="width:39%;text-align: center;line-height:30px;">{subscribeDate}</td>' + 
								'<td style="width:20%;text-align:center;line-height:30px;">{subscribeMember}</td>' +
								'<td style="width:20%;text-align:center;line-height:30px;">{recommendMoney}元</td>' +
								'<td style="width:20%;text-align:center;line-height:30px;">{recommendPoint}分</td>' +
								'</tr>';
		
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(template.format({
										subscribeDate : new Date(temp.subscribeDate).format('yyyy-MM-dd'),
										subscribeMember : temp.subscribeMember,
										recommendMoney : temp.recommendMoney,
										recommendPoint : temp.recommendPoint
									}));
								}
		
								content.html(tr + html.join(''));
							}else{
								content.html('暂无记录');
							}
							
							container.find('[id="dialogContent_div_dialogPopup"]').html(content);
						}else{
							container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">加载失败,请重新刷新</font>');
						}
					},
					error : function(req, status, error){
						
						container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">服务器请求失败, 请稍候再试.</font>');
					},
					complete : function(){
						wxLoadDialog.instance().hide();
					}
				});
			},
			leftText : '确认',
			left : function(){
				wxLoadDialog.instance().hide();
				commentDialog.close();
			}
		});
		commentDialog.open();
		
	});
	
	//佣金记录
	$('#commission_div_member').click(function(){
		var commissionDialog;
		commissionDialog = new WeDialogPopup({
			titleText : '佣金记录',
			contentCallback : function(container){
				$.ajax({
					url : '../../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'commissionDetail',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					datatype : 'json',
					beforeSend : function(){
						wxLoadDialog.instance().show();
					},
					success : function(data, status, res){
						if(data.success){
							var content = $('<table/>');
							content.addClass('d-list');		
							
							var tr = '<tr class="d-list-title">'
										+'<td style="width:39%;text-align: center;">消费时间</td>'
										+'<td style="width:30%;text-align:center;">消费人</td>'
										+'<td style="width:20%;text-align:center;">佣金额</td>'
									 +'</tr>';
									 
							if(data.root.length > 0){
								var template = '<tr style="color:#26A9D0;border-bottom:1px solid #999;">' + 
								'<td style="width:39%;text-align: center;line-height:30px;">{operateDateFormat}</td>' + 
								'<td style="width:30%;text-align:center;line-height:30px;">{consumeMemberName}</td>' +
								'<td style="width:20%;text-align:center;line-height:30px;">{deltaTotalMoney}元</td>' +
								'</tr>';
								
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(template.format({
										operateDateFormat : new Date(temp.operateDateFormat).format('yyyy-MM-dd'),
										consumeMemberName : temp.member.name,
										deltaTotalMoney : temp.deltaTotalMoney
									}));
								}
								
								content.html(tr + html.join(''));
							}else{
								content.html('暂无记录');
							}
							
							container.find('[id="dialogContent_div_dialogPopup"]').html(content);
							
						}else{
							container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">加载出错,请重新刷新</font>');
						}
						
					},
					error : function(res, status, err){
						
						container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">服务器请求失败, 请稍候再试.</font>');
					},
					complete : function(){
						wxLoadDialog.instance().hide();
					}
				});
			},
			leftText : '确认',
			left : function(){
				commissionDialog.close();
			}
		});
		commissionDialog.open();
	});
	
	
	//充值记录
	$('#recharge_div_member').click(function(){
		
		var rechargeDialog;
		rechargeDialog = new WeDialogPopup({
			titleText : '充值记录',
			contentCallback : function(container){
				$.ajax({
					url : '../../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'chargeDetails',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					dataType : 'json',
					beforeSend : function(){
						wxLoadDialog.instance().show();
					},
					success : function(data, status, xhr){
						if(data.success){
							
							var content = $('<table/>');
							content.addClass('d-list');		
							
							var tr = '<tr class="d-list-title">'
										+'<td style="width:42%;text-align: center;">充值时间</td>'
										+'<td style="width:30%;text-align:center;">实收</td>'
										+'<td style="width:20%;text-align:center;">实充</td>'
									 +'</tr>';
						
							if(data.root.length > 0){
								var templet = '<tr class="d-list-item">'
									+ '<td style="text-align: center;">{date}</td>'
									+ '<td>{chargeMoney}</td>'
									+ '<td>{deltaTotalMoney}</td>'
									+ '</tr>';
								
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(templet.format({
										date : fnDateInChinese(temp.operateDateFormat),
										chargeMoney : (checkDot(temp.chargeMoney)?parseFloat(temp.chargeMoney).toFixed(2) : temp.chargeMoney) + '元',
										deltaTotalMoney : (checkDot(temp.deltaTotalMoney)?parseFloat(temp.deltaTotalMoney).toFixed(2) : temp.deltaTotalMoney) + '元'
									}));
								}
								content.html(tr + html.join(''));
							}else{
								content.html('暂无记录');
							}
							
							container.find('[id="dialogContent_div_dialogPopup"]').html(content);
							
						}else{
							container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">'+ data.msg +'</font>');					
						}
						
					},
					error : function(data, errotType, eeor){
						container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">服务器请求失败, 请稍候再试.</font>');
					},
					complete : function(){
						wxLoadDialog.instance().hide();
					}
				});
			},
			leftText : '确认',
			left : function(){
				wxLoadDialog.instance().hide();
				rechargeDialog.close();
			}
		});
		rechargeDialog.open();
	});
	
	//我的优惠券
	$('#myCoupon_div_member').click(function(){
		window.location.href = '../../html/myCoupon/myCoupon.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
	});
	
	//优惠券使用记录
	$('#myCouponUse_div_member').click(function(){
		
		var couponUseDialog;
		couponUseDialog = new WeDialogPopup({
			titleText : '优惠券使用记录',
			contentCallback : function(container){
				$.ajax({
					url : '../../../WXQueryMemberOperation.do',
					type : 'post',
					type : 'post',
					data : {
						dataSource : 'couponConsumeDetails',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					dataType : 'json',
					beforeSend : function(){
						wxLoadDialog.instance().show();
					},
					success : function(data, status, xhr){
						if(data.success){
							var content = $('<table/>');
							content.addClass('d-list');		
							
							var tr = '<tr class="d-list-title">'
										+'<td style="width:50%;text-align:center;">时间</td>'
										+'<td style="width:50%;text-align:center;">类别</td>'
									 +'</tr>';
							
							if(data.root.length > 0){
								
								var template = '<tr class="d-list-item-consume">' +
												'<td style="text-align: center;">{date}</td>' +
												'<td style="text-align: center;">{couponName}</td>' +
											   '</tr>';
											   
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(template.format({
										date : temp.operateDate,
										couponName : temp.couponName + ',' + temp.couponPrice.toFixed(0) + '元</br><font style="font-size:13px;">操作:' + temp.operateText + '</font>',
									}));
								}
								content.html(tr + html.join(''));
							}else{
								content.html('暂无优惠券使用记录');
							}
							
							container.find('[id="dialogContent_div_dialogPopup"]').html(content);
						}else{
							container.find('[id="dialogContent_div_dialogPopup"]').html(data.msg);
						}
					},
					error : function(data, errotType, eeor){
						container.find('[id="dialogContent_div_dialogPopup"]').html('<font color="black">服务器请求失败, 请稍候再试.</font>');
					},
					complete : function(){
						wxLoadDialog.instance().hide();
					}
				});
				
			},
			leftText : '确认',
			left : function(){
				couponUseDialog.close();
			}
		});
		couponUseDialog.open();
		
		
	});
	
	//会员等级
	$('#level_div_member').click(function(){
		
		var memberLevelDialog;
		memberLevelDialog = new WeDialogPopup({
			titleText : '会员等级',
			contentCallback : function(container){
				wxLoadDialog.instance().show();
				$.post('../../../WXQueryMemberOperation.do', {dataSource : 'chart', rid:member.restaurant.id}, function(result){
					if(typeof result == 'string'){
						result = eval('(' + result + ')');
					}
					
					if(result.success){
						memberLevelData = result.root;
						memberLevelData.push(currentMemberLevelData);
						
						chartDatas = eval('(' + result.other.chart + ')');
				
						yAxisData = chartDatas.data;
						
						if(yAxisData.length > 0){
							
							//动态变化chart高度
							$('#divMemberLevelChart').height(yAxisData.length * (document.body.clientWidth > 330 ? 70 : 95) + 140);
							
							var chartMinAndMax;
							
							if(yAxisData[yAxisData.length-1].x >= currentMemberLevelData.x){
								chartMinAndMax = yAxisData[yAxisData.length-1].x;
							}else{
								chartMinAndMax = currentMemberLevelData.x;
							}
							//添加用户等级位置
							yAxisData.push(currentMemberLevelData);
							
							member_loadMemberTypeChart({minY:-chartMinAndMax * 0.15, maxY:chartMinAndMax * 1.2, series:yAxisData});		
						}else{
							container.find('[id="dialogContent_div_dialogPopup"]').html('<div>会员等级建立中...</div>');
						}
					}else{
						container.find('[id="dialogContent_div_dialogPopup"]').html('<div>会员等级建立中...</div>');
					}
				}).error(function() {
					container.find('[id="dialogContent_div_dialogPopup"]').html('<div>会员等级建立中...</div>');
				}).complete(function(){
					wxLoadDialog.instance().hide();
				});	
			
				function member_loadMemberTypeChart(c){
				 	var chart = {
						    chart: {
						        type: 'spline',
						        inverted: true,
						        width : container.find('[data-type="dialogContent_div_dialogPopup"]').width()+ 10,
						        renderTo : 'dialogContent_div_dialogPopup'
						    },
						    title: {
						        text: ''
						    },
						    xAxis: {
						    	reversed : false,
						        title: {
						            enabled: false,
						            text: '积分',
						            align : 'high'
						        },
						        labels: {
						            formatter: function() {
						                return this.value;
						            }
						        },
						        max : c.maxY,
						        min: c.minY,
						        showLastLabel: true
						    },
						    yAxis: {
						        title: {
						            text: '等级'
						        },
						        labels: {
						            formatter: function() {
						                return '' ;
						            }
						        },
						        lineWidth: 2
						    },
						    legend: {
						        enabled: false
						    },
						    tooltip: {
						    	enabled : false,
						        headerFormat: '<b>{series.name}</b><br/>',
						        pointFormat: '{point.x} km: {point.y}°C',
						        followPointer : true
						    },
							plotOptions : {
								spline : {
									cursor : 'pointer',
									dataLabels : {
										x: 5,
										y : 37,							
										align : 'right',
										enabled : true,
										style : {
											fontWeight: 'bold', 
											color: 'green'
										},
										formatter : function(){
							                return getLevelChartInfo(this.x, this.point.memberTypeName);
										}
									},
									marker: {
										radius: 8,
					                    lineColor: 'white',
					                    lineWidth: 1
						            }
								}
							},			    
						    credits : {
						    	enabled : false
						    },         
						    exporting : {
						    	enabled : false
						    },			    
						    series:	[{data:c.series}]	    
						};
						new Highcharts.Chart(chart);
				}
				
				function getLevelChartInfo(x,point){
					var temp = {};
					if(point){
						temp = currentMemberLevelData;
					}else{
						for (var i = 0; i < memberLevelData.length; i++) {
							if(memberLevelData[i].pointThreshold == x){
								temp = memberLevelData[i];
								break;
							}
						}	
					}
					
					var pointFormat;
					if(document.body.clientWidth > 330){
						pointFormat = '<span style="font-size : 12px;">' + temp.memberTypeName + (temp.pointThreshold >0 || point? '--' + temp.pointThreshold +'分' :'')+ '</span>'
							+ (temp.discount && temp.discount.type != 2 ? '<br/>' + '<font style="font-size: 13px;color:maroon">' + temp.discount.name : '') + '</font>'
							+ (temp.chargeRate > 1 ? '<br/>'+ '<font style="font-size: 13px;color:maroon">' + temp.chargeRate +'倍充值优惠，充100送'+parseInt((temp.chargeRate*100 - 100))+'元':'')  + '</font>' 
							+ (temp.desc  ? '<br/>'+ '<font style="font-size: 13px;color:maroon">'+ temp.desc : '')  + '</font>' 
							;
					}else{
						pointFormat = '<span style="font-size : 12px;">' + temp.memberTypeName + (temp.pointThreshold >0 || point? '--' + temp.pointThreshold +'分' :'')+ '</span>'
							+ (temp.discount && temp.discount.type != 2 ? '<br/>' + '<font style="font-size: 13px;color:maroon">' + temp.discount.name : '') + '</font>' 
							+ (temp.desc  ? '<br/>'+ '<font style="font-size: 13px;color:maroon">'+ temp.desc : '')  + '</font>' 
							;
					}
					
					return pointFormat;		
				}
			},
			leftText : '确认',
			left : function(){
				wxLoadDialog.instance().hide();
				memberLevelDialog.close();
			}
		});
		memberLevelDialog.open();
		
	});	
	
	function fnDateInChinese(date){
		var month = date.substring(5, 7);
		var day = date.substring(8, 10);
		var time = date.substring(11, date.length - 3);
		
		return month+ '月' +day + '日' + ' ' + time;
		
	}
})