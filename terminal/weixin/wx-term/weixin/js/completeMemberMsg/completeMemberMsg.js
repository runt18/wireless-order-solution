function CompleteMemberMsg(param){
	var param = param || {
		sessionId : null,
		completeFinish : function(){}
	};
	var completeMemberDialog = new DialogPopup({

		titleText : '请完善会员资料',
		content : '<div style="width: 100%;" data-fix="container">'
					+'<ul class="m-b-list">'
						+'<li class="none-line" style="line-height: 50px;padding-top: 10px;">'
							+'手机号码: <input data-type="mobileNum_input_member" style="font-size: 20px;padding: 3px 5px 3px 5px;width: 120px;"  type="tel"  maxlength="11"/>'			
						+'</li>'		
						+'<li class="none-line" style="line-height: 50px;">'	
							+'会员姓名: <input data-type="mobileName_input_member" style="font-size: 20px;padding: 3px 5px 3px 5px;width: 120px;">'			
						+'</li>'	
						+'性别:'
						+'<li><div data-type="personSex" data-value="0" class="region_css_book selectedRegion_css_book" style="width:40%;float:left;height:32px;line-height:32px;margin:0 10px;" href="#">'	
							+'<ul class="m-b-list">先生</ul>'
						+'</div>'
						+'<div data-type="personSex" data-value="1" class="region_css_book" style="width:40%;float:left;height:32px;line-height:32px;margin:0 10px;" href="#">'
							+'<ul class="m-b-list">女士</ul>'
						+'</div>'
						+'</li>'
						+'<div style="clear:both;"></div>'
						+'<li class="none-line" style="line-height: 50px;">'	
							+'年龄段:<select data-type="age_select_member" style="font-size: 20px;padding: 3px 5px 3px 5px;width: 120px;margin-left:35px;">'
								+'<option value ="5">00后</option>'  
								+'<option value ="4">90后</option>' 
								+'<option value="3">80后</option>' 
								+'<option value="2">70后</option>' 
								+'<option value="1">60后</option>' 
								+'<option value="6">50后</option>' 
								+'</select>'			
						+'</li>'
						+'<li class="none-line" style="line-height: 50px;">'
								+'会员生日: <input data-type="birthdayMonth_input_member"  type="tel" class="txtVerifyMobile" style="font-size:20px;width:45px;" data-fix="input">月'
							+'<input data-type="birthdayDay_input_member"  type="tel" class="txtVerifyMobile" style="font-size:20px;width:45px;">日'
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
			
			//传入的是sessionId 的时候
			if(param.sessionId){
				$.ajax({
					url : '../../WXOperateMember.do',
					type : 'post',
					data : {
						dataSource : 'bind',
						sessionId : param.sessionId,
						mobile : mobile,
						name : name,
						birthday : birthday,
						sex : sex,
						age : age
					},
					dataType : 'json',
					success : function(data, status, xhr){
						if(data.success){
							if(param.completeFinish && typeof param.completeFinish == 'function'){
								completeMemberDialog.close(function(){
									param.completeFinish();
								}, 200);
							}else{
								completeMemberDialog.close();
							}
						}else{
							Util.dialog.show({msg: data.msg});
						}					
					}
				});
			}else{
				//使用oid fid
				$.ajax({
					url : '../../WXOperateMember.do',
					type : 'post',
					data : {
						dataSource : 'bind',
						oid : Util.mp.oid,
						fid : Util.mp.fid,
						sessionId : param.sessionId,
						mobile : mobile,
						name : name,
						birthday : birthday,
						sex : sex,
						age : age
					},
					dataType : 'json',
					success : function(data, status, xhr){
						if(data.success){
							if(param.completeFinish && typeof param.completeFinish == 'function'){
								completeMemberDialog.close(function(){
									param.completeFinish();
								}, 200);
							}else{
								completeMemberDialog.close();
							}
						}else{
							Util.dialog.show({msg: data.msg});
						}					
					}
				});
			}
		},
		
		dismissible : true
	
	});
	

	this.open = function(){
		completeMemberDialog.open();
	};
	
	this.close = function(){
		completeMemberDialog.close();
	}
}