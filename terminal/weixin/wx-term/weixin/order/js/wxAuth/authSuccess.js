
	
$(function(){
	
	//从普通url获取当前桌信息
	function common_urlParaQuery() {
		var name, value, i, key = 0;
		var str = location.href;
		
		if(str.indexOf("#") > 0){
			str = str.substring(0,str.indexOf("#"));
		}
		var num = str.indexOf("?");
		if(num > 0){
			str = str.substr(num + 1);
			var arrtmp = str.split("&");
			for (i = 0; i < arrtmp.length; i++) {
				num = arrtmp[i].indexOf("=");
				if (num > 0) {
					name = arrtmp[i].substring(0, num);
					value = arrtmp[i].substr(num + 1);
					this[name] = value;
				}
			}
		}
	}

	var Request = new common_urlParaQuery();
	var rid = Request["rid"];
	var authCode = Request["auth_code"];
	
	$.ajax({
		url : '../../WxAuth.do',
		type : 'post',
		data : { rid : rid, auth_code : authCode },
		success : function(result){
			console.log('Fixme : should be success');
			$('#authFailure').show();
		},
		error : function(result){
			$.ajax({
				url : '../../WXQueryInfo.do',
				type : 'post',
				data : {
					dataSource : 'restInfo',
					rid : rid
				},
				dataType : 'json',
				success : function(data){
					$('#loading_div_authResponse').hide();
					if(data.success){
						var rest = data.root[0];
						$('#wxRestLogo_img_authResponse').attr('src', rest.headImgUrl);
						$('#wxNickName_label_authResponse').text(rest.nickName);
						$('#authSuccess_div_authResponse').show();
					}else{
	//					$('#wxRestLogo_img_authResponse').attr('src', 'images/member_card.jpg');
	//					$('#wxNickName_label_authResponse').text('品饺');
						$('#authFailure').show();			
					}
				},
				error : function(xhr, status){
					console.log('failure');
				}
			});		
		}
	});	
	
	//重新绑定
	$('#reAuth_button_authResponse').click(function(){
		$.ajax({
			url : '../../WxAuthLogin.do',
			type : 'post',
			data : { rid : rid },
			success : function(result){
				console.log('FIXME : should be success');
			},
			error : function(result){
				location.href = result.responseText;
			}
		});
	});

});	
	

	