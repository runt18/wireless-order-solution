var protalFuncReg = function() {
	$("#order").each(function(){
		$(this).bind("click", function(){
				location.href = "TableSelect.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});

	bindActiveEvent('bill', 
		'url(../../images/bill_select.png) no-repeat 50%',
		'url(../../images/bill.png) no-repeat 50%',
		function(){
			var lm = new Ext.LoadMask(document.body, {
				msg : '正在验证权限, 请稍等......'
			});
			lm.show();
			verifyStaff('../../', '1006', function(res){
				lm.hide();
				if(res.success){
					location.href = "Bills.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');					
				}else{
					res['icon'] = Ext.MessageBox.WARNING;
					Ext.ux.showMsg(res);
				}
			});
		}
	);

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
	
	// mouse over & mouse off -- heightlight the icon
	$("#order").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/order_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/order.png) no-repeat 50%");
		});
	});

	$("#shift").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/shift_selected.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/shift.png) no-repeat 50%");
		});
	});
	
	$("#dailySettle").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/dailySettle_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/dailySettle.png) no-repeat 50%");
		});
	});
};