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
				$('#divMemberCouponConsume').css('display', 'block');
				member = data.other.member;
				member.restaurant = data.other.restaurant;
				initMemberMsg({data:member});
				//设置会员排名
				$('#fontDefeatMemberCount').text(member.rank);
				$.ajax({
					url : '../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'hasCouponDetails',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					dataType : 'json',
					success : function(data, status, xhr){
						if(data.root.length > 0){
							$('#divMyCoupon').prepend('<img src="../../images/WXnew.png" style="margin-top: 10px;"></img>');
						}else{
							$('#divMyCoupon img').html('');
						}
					},
					error : function(data, errotType, eeor){
						Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
					}
				});
			}else{
				if(data.code == 7400){
					$('#divMemberCard').css('display', 'block');
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
	
var autoWidth = function()
{
	 calcFloatDivs();
};
window.onresize = autoWidth;

	
});