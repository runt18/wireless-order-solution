var unStockActionCount = 0, unStockTakeCount = 0;
var settle = {
	id : 'secondStepPanelSouth',
	region : 'center',
	frame : true,
	height : 300,
	border : false,
	bodyStyle : 'font-size:30px;text-align:center;',
	html : '<div align="center" ><br><br>当前会计月份 : <label id="labCurrentMonth" style="color:green"> </label>&nbsp;月<br> 未审核的库单 : <label id="labStockAction" style="color:red" >0</label>&nbsp;张</br>' +
			'未审核的盘点 : <label id="labStockTake" style="color:red" >0</label>&nbsp;张</div>'
};
var form = new Ext.form.FormPanel({
	height : 200,
	width : 400,
	region : 'center',
	id : 'formMonthSettle',
	frame : true,
	border : true,
	items : [settle],
	buttons:[{
		text : '确定',
		handler: function() {
		 	var stockActionCount = Ext.getDom('labStockAction').innerHTML;
		 	var stockTakeCount = Ext.getDom('labStockTake').innerHTML;
		 	if(eval(stockActionCount + '+' + stockTakeCount) > 0){
		 		Ext.MessageBox.alert('提示', '还有未审核的库单或盘点单');
		 	}else{
		 		Ext.Ajax.request({
		 			url : '../../UpdateCurrentMonth.do',
		 			params : {
		 				pin : pin
		 			},
		 			success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.ux.showMsg(jr);
							monthSettleWin.hide();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
		 		});
		 	
		 	}
		}
	},
	{
		text : '取消',
		handler : function(){
			monthSettleWin.hide();
		}
	}
	]
});
var monthSettleWin = new Ext.Window({
	title : '月结操作',
	id : 'winMonthSettle',
	layout : 'border',
	width : 500,
	height : 350,
	closable : false,
	resizable : false,
	modal : true,
	items : [form]
});


function monthSettleHandler(){
	monthSettleWin.show();
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		params : {
			restaurantID : restaurantID
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				//alert(jr.other.systemSetting.setting.intCurrentMonth);
				Ext.getDom('labCurrentMonth').innerHTML = jr.other.systemSetting.setting.intCurrentMonth;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	Ext.Ajax.request({
		url : '../../QueryStockTake.do',
		params : {
			pin : pin,
			status : 1
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getDom('labStockTake').innerHTML = jr.totalProperty;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	Ext.Ajax.request({
		url : '../../QueryStockAction.do',
		params : {
			pin : pin,
			status : 1
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getDom('labStockAction').innerHTML = jr.totalProperty;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.reponseText));
		}
	});

}