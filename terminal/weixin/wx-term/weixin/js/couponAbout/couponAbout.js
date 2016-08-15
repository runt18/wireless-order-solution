
$(function(){
	
	var params = null;
	if(Util.mp.params.pid){
		params = {
			dataSource : 'getByCond',
			pid : Util.mp.params.pid, 
			fid : Util.mp.params.r,
			oid : Util.mp.params.m
		};
		//如果url parameter中包含‘pid’
	}
	
	if(params){
		$.ajax({
			url : '../../../WxOperatePromotion.do',
			dataType : 'json',
			type : 'post',
			data : params,
			success : function(data, statuc, xhr){
				if(data.success && data.root.length > 0){
					$('#divInfoContent').html(data.root[0].entire);
				}else{
					$('#divInfoContent').html('暂无活动信息');
				}
			},
			error : function(xhr, errorType, error){
				//微信帐号还未与餐厅会员绑定
				$('#divInfoContent').html('微信帐号还未激活');
			}
		});
	}

});