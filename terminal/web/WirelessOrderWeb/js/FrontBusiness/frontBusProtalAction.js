var protalFuncReg = function() {
	$("#order").each(function(){
		$(this).bind("click", function(){
				location.href = "TableSelect.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});

	$("#bill").each(function() {
		$(this).bind("click", function() {
				// 密码校验
				billVerifyWin.show();
		});
	});

	$("#shift").each(function(){
		$(this).bind("click", function(){
			Ext.Ajax.request({
				url : '../../QueryDailySettleByNow.do',
				params : {
					
					queryType : 0
				},
				success : function(res, opt){
					var jr = Ext.util.JSON.decode(res.responseText);
					if(jr.success){
						shiftCheckDate = jr;
						shiftCheckDate.otype = 0;
						dailySettleCheckTableWin.show();
						dailySettleCheckTableWin.center();
					}else{
						Ext.Msg.show({
							title : '错误',
							msg : '加载交班信息失败.'
						});
					}
				},
				failure : function(res, opt){
					Ext.Msg.show({
						title : '错误',
						msg : '加载交班信息失败.'
					});
				}
			});
		});
	});

	$("#dailySettle").each(function(){
		$(this).bind("click", function(){
			Ext.Ajax.request({
				url : '../../QueryDailySettleByNow.do',
				params : {
					
					queryType : 1
				},
				success : function(res, opt){
					var jr = Ext.util.JSON.decode(res.responseText);
					if(jr.success){
						shiftCheckDate = jr;
						shiftCheckDate.otype = 1;
						dailySettleCheckTableWin.show();
						dailySettleCheckTableWin.center();
					}else{
						Ext.Msg.show({
							title : '错误',
							msg : '加载日结信息失败.'
						});
					}
				},
				failure : function(res, opt){
					Ext.Msg.show({
						title : '错误',
						msg : '加载日结信息失败.'
					});
				}
			});
		});
	});
};