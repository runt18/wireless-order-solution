$(function(){
	Util.html('', function(html){ $(document.body).append(html);  });
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
			if(data.success){
				$('#divMemberCard').css('display', 'block');
				$('#divMemberContent').css('display', 'block');
				member = data.other.member;
//				alert(JSON.stringify(member))
				member.restaurant = data.other.restaurant;
				initMemberMsg({data:member});
			}else{
				if(data.code == 7400){
					$('#divVerifyAndBind').css('display', 'block');
				}else{
					alert(data.msg);
				}
			}
		},
		error : function(data, errotType, eeor){
			alert('服务器请求失败, 请稍候再试.');
		}
	});
	
});