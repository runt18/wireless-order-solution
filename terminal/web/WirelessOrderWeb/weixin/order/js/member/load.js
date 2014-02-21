$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	Util.lm.show();
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'getInfo',
			oid : Util.mp.oid,
			fid : Util.mp.fid
		},
		dataType : 'json',
		success : function(data, status, xhr){
			Util.lm.hide();
			if(data.success){
				$('#divMemberCard').css('display', 'block');
				$('#divMemberContent').css('display', 'block');
				$('#divMemberTypeContent').css('display', 'block');
				$('#divMemberPointContent').css('display', 'block');
				$('#divMemberBalanceContent').css('display', 'block');
				$('#divMemberCouponContent').css('display', 'block');
				member = data.other.member;
				member.restaurant = data.other.restaurant;
				initMemberMsg({data:member});
			}else{
				if(data.code == 7400){
					$('#divVerifyAndBind').css('display', 'block');
				}else{
					Util.dialog.show({msg: data.msg});
				}
			}
		},
		error : function(data, errotType, eeor){
			Util.lm.hide();
			Util.dialog.show({title:'错误', msg: '服务器请求失败, 请稍候再试.'});
		}
	});
	
});