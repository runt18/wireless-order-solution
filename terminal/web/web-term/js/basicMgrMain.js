
//--------------------------月结-----------------
var unStockActionCount = 0, unStockTakeCount = 0;
var settle = {
	id : 'monthSettleDetail',
//	region : 'north',
	frame : true,
	height : 160,
	border : false,
	bodyStyle : 'font-size:14px;',
	html : '<div align="center" style="font-size:30px;">当前会计月份 : <label id="labCurrentMonth" style="color:green"> </label>&nbsp;月<br> ' +
			'未审核的库单 : <label id="labStockAction" style="color:red" >0</label>&nbsp;张</br>' +
			'未审核的盘点 : <label id="labStockTake" style="color:red" >0</label>&nbsp;张<br>' +
			'<span style="font-size:20px;color:green;">注 : 月结之前请确认商品和原料的参考成本是否正确</span></div>'
/*	,
	buttons:[{
		text : '月结',
		handler: function() {
		 	var stockActionCount = Ext.getDom('labStockAction').innerHTML;
		 	var stockTakeCount = Ext.getDom('labStockTake').innerHTML;
		 	if(eval(stockActionCount + '+' + stockTakeCount) > 0){
		 		Ext.MessageBox.alert('提示', '还有未审核的库单或盘点单');
		 	}else{
		 		Ext.Ajax.request({
		 			url : '../../UpdateCurrentMonth.do',
		 			params : {
		 				
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
	}]*/
};
/*var form = new Ext.form.FormPanel({
	height : 200,
	width : 400,
	region : 'center',
	id : 'formMonthSettle',
	frame : true,
	border : true,
	items : [settle],
	buttons:[{
		text : '月结',
		handler: function() {
		 	var stockActionCount = Ext.getDom('labStockAction').innerHTML;
		 	var stockTakeCount = Ext.getDom('labStockTake').innerHTML;
		 	if(eval(stockActionCount + '+' + stockTakeCount) > 0){
		 		Ext.MessageBox.alert('提示', '还有未审核的库单或盘点单');
		 	}else{
		 		Ext.Ajax.request({
		 			url : '../../UpdateCurrentMonth.do',
		 			params : {
		 				
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
});*/
var monthSettleWin = new Ext.Window({
	title : '月结操作',
	id : 'winMonthSettle',
	width : 700,
	height : 215,
	closable : false,
	resizable : false,
	modal : true,
	border : 'fit',
	items : [settle],
	listeners : {
		show : function(thiz){
/*			thiz.center();
			thiz.load({
				url : '../InventoryManagement_Module/MonthSettle.html',
				scripts : true,
				params : {
					loadPage : true
				}
			});*/
			Ext.Ajax.request({
				url : '../../QueryCurrentMonth.do',
				params : {
					restaurantID : restaurantID
				},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					if(jr.success){
						Ext.getDom('labCurrentMonth').innerHTML = jr.msg;
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
	},
	bbar : ['->',{
		text : '月结',
		iconCls : 'btn_save',
		handler : function(){
			var stockActionCount = Ext.getDom('labStockAction').innerHTML;
		 	var stockTakeCount = Ext.getDom('labStockTake').innerHTML;
		 	if(eval(stockActionCount + '+' + stockTakeCount) > 0){
		 		Ext.MessageBox.alert('提示', '还有未审核的库单或盘点单');
		 	}else{			
				Ext.Ajax.request({
		 			url : '../../UpdateCurrentMonth.do',
		 			success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.ux.showMsg(jr);
//							monthlySet = true;
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
	},{
		text : '取消',
		iconCls : 'btn_close',
		handler : function(){
			monthSettleWin.hide();	
			Ext.Ajax.request({
				url : '../../OperateMaterial.do',
				params : {
					dataSource : 'cancelMonthSettle'
				}
			});
		}
	}]
});

//--------------------退菜原因--------------
var bmObj = {};
bmObj.operation = {
	'insert' : 'INSERT',
	'update' : 'UPDATE',
	'select' : 'SELECT',
	'delete' : 'DELETE',
	'set' : 'SET',
	'get' : 'GET'
};
function cancelReasonRenderer (){
	return ''
		   + '<a href="javascript:updateCancelReasonHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteCancelReasonHandler()">删除</a>';
}
updateCancelReasonHandler = function(){
	cancelReasonOperationHandler({
		type : bmObj.operation['update']
	});
};

deleteCancelReasonHandler = function(){
	cancelReasonOperationHandler({
		type : bmObj.operation['delete']
	});
};

cancelReasonOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPanel.otype = c.type;
	
	if(c.type == bmObj.operation['insert']){
		oCancelReasonData({
			type : bmObj.operation['set']
		});
		oPanel.setTitle('添加退菜原因');
		oPanel.show();
		cancelReasonWin.syncSize();
		cancelReasonWin.doLayout();
	}else if(c.type == bmObj.operation['update']){
		var sd = Ext.ux.getSelData(crGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个原因再进行操作.');
			oPanel.hide();
			cancelReasonWin.doLayout();
			return;
		}
		oCancelReasonData({
			type : bmObj.operation['set'],
			data : sd
		});
		oPanel.setTitle('修改退菜原因');
		oPanel.show();
		cancelReasonWin.syncSize();
		cancelReasonWin.doLayout();
	}else if(c.type == bmObj.operation['delete']){
		var sd = Ext.ux.getSelData(crGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个原因再进行操作.');
			oPanel.hide();
			cancelReasonWin.doLayout();
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除退菜原因?',
			buttons :Ext.Msg.YESNO,
			icon: Ext.MessageBox.QUESTION,
			fn : function(btn){
				if(btn == 'yes'){
					Ext.Ajax.request({
						url : '../../DeleteCancelReason.do',
						params : {
							id : sd['id']
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnCloseCancelPanel').handler();
								Ext.getCmp('btnRefreshCRGrid').handler();
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
		});
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

oCancelReasonData = function(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('numCancelReasonID');
	var reason = Ext.getCmp('txtCancelReason');
	if(c.type == bmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		reason.setValue(data['reason']);
	}else if(c.type == bmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			id : id.getValue(),
			reason : reason.getValue()
		};
		c.data = data;
	}
	reason.clearInvalid();
	reason.focus(true, 100);
	return c;
};
function initCancelReasonWin(){
	var crGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnRefreshCRGrid',
			iconCls : 'btn_refresh',
			handler : function(){
				oPanel.hide();
				cancelReasonWin.doLayout();
				crGrid.getStore().reload();
			}
		}, '-', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				cancelReasonOperationHandler({
					type : bmObj.operation['insert']
				});
			}
		}, '-', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updateCancelReasonHandler();
			}
		}, '-', {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deleteCancelReasonHandler();
			}
		}]
	});
	crGrid = createGridPanel(
		'clientBasicGrid',
		'',
		'',
		'',
		'../../QueryCancelReason.do',
		[
			[true, false, false, false], 
			['原因', 'reason'],
			['操作', 'operation', 60, 'center', 'cancelReasonRenderer']
		],
		['id', 'reason', 'restaurantID'],
		[['restaurantID', restaurantID]],
		0,
		'',
		crGridTbar
	);
	crGrid.region = 'center';
	crGrid.getStore().on('load', function(thiz, rs){
		for(var i = 0; i < rs.length; i++){
			if(rs[i].get('restaurantID') == 0){
				thiz.remove(rs[i]);
				crGrid.getView().refresh();
				break;
			}
		}
	});
	
	oPanel = new Ext.Panel({
		title : '&nbsp;',
		hidden : true,
		frame : true,
		region : 'south',
		layout : 'column',
		autoHeight : true,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 35
		},
		items : [{
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				id : 'txtCancelReason',
				width : 260,
				fieldLabel : '原因',
				allowBlank : false,
				blankText : '原因内容不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '原因内容不能为空.';
					}
				}
			}]
		}, {
//			columnWidth : .4,
			hidden : true,
			items : [{
				xtype : 'numberfield',
				id : 'numCancelReasonID',
				fieldLabel : '编号',
				width : 60,
				disabled : true
			}]
		}
		],
		buttonAlign : 'center',
		buttons : [{
			text : '保存',
			handler : function(){
				var txtCancelReason = Ext.getCmp('txtCancelReason');
				if(!txtCancelReason.isValid()){
					return;
				}
				var cancelReason = oCancelReasonData({
					type :  bmObj.operation['get']
				}).data;
				var action;
				if(oPanel.otype == bmObj.operation['insert']){
					action = '../../InsertCancelReason.do';
					(delete cancelReason['id']);
				}else if(oPanel.otype == bmObj.operation['update']){
					action = '../../UpdateCancelReason.do';
				}else{
					return;
				}
				
				Ext.Ajax.request({
					url : action,
					params : {
						id : cancelReason['id'],
						reason : cancelReason['reason']
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnCloseCancelPanel').handler();
							Ext.getCmp('btnRefreshCRGrid').handler();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
			}
		}, {
			text : '取消',
			id : 'btnCloseCancelPanel',
			handler : function(){
				oPanel.hide();
				cancelReasonWin.doLayout();
			}
		}]
	});
	
	cancelReasonWin = new Ext.Window({
		title : '退菜原因管理',
		modal : true,
		resizable : false,
		closable : false,
		draggable : false,
		width : 350,
		height : 390,
		layout : 'border',
		items : [crGrid, oPanel],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				cancelReasonWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				cancelReasonWin.hide();
			}
		}],
		listeners : {
			beforeshow : function(){
				oPanel.hide();
			}
		}
	});
};

//-----------------收款设置-----------------
var formatPrice = new Ext.Window({
//	title : '收款设置',
	modal : true,
	resizable : false,
	closable : false,
	width : 300,
	items : [{
		xtype : 'panel',
	    layout : 'column',
	    frame : true,
	    defaults : {
	    	xtype : 'panel',
	    	layout : 'form'
	    },
	    items : [{
	    	columnWidth : 1,
	    	height : 22,
	    	style : 'color:#15428B;',
	    	html : '金额尾数处理方式设置:'
	    }, {
	    	columnWidth : .38,
	    	labelWidth : 40,
	    	items : [{
	    		xtype : 'radio',
				id : 'rdoFormatTypeNOACTION',
				name : 'rdoFormatType',
				inputValue : 0,
				fieldLabel : '不处理',
				labelSeparator : '',
				listeners : {
					render : function(e){
						Ext.ux.checkPaddingTop(e);
						Ext.getDom('rdoFormatTypeNOACTION').onclick = function(){
							e.setValue(true);
							formatPrice.priceTail = e.inputValue;
						};
					}
				}
			}]
	    }, {
	    	columnWidth : .27,
	    	labelWidth : 30,
	    	items : [{
				xtype : 'radio',
				id : 'rdoFormatTypeDECIMALCUT',
				name : 'rdoFormatType',
				inputValue : 1,
				fieldLabel : '抹零',
				labelSeparator : '',
				listeners : {
					render : function(e){
						Ext.ux.checkPaddingTop(e);
						Ext.getDom('rdoFormatTypeDECIMALCUT').onclick = function(){
							e.setValue(true);
							formatPrice.priceTail = e.inputValue;
						};
					}
				}
			}]
	    }, {
	    	columnWidth : .33,
	    	labelWidth : 55,
	    	items : [{
				xtype : 'radio',
				id : 'rdoFormatTypeDECIMALROUND',
				name : 'rdoFormatType',
				inputValue : 2,
				fieldLabel : '四舍五入',
				labelSeparator : '',
				checked : true,
				listeners : {
					render : function(e){
						Ext.ux.checkPaddingTop(e);
						Ext.getDom('rdoFormatTypeDECIMALROUND').onclick = function(){
							e.setValue(true);
							formatPrice.priceTail = e.inputValue;
						};
					}
				}
			}]
	    }, {
	    	columnWidth : 1,
	    	html : '<hr/>'
	    }, {
	    	columnWidth : 1,
	    	height : 22,
	    	style : 'color:#15428B;',
	    	html : '抹数金额设置:'
	    }, {
	    	columnWidth : .45,
	    	labelWidth : 70,
	    	items : [{
	    		xtype : 'checkbox',
	    		id : 'chbEraseQuotaStatus',
	    		fieldLabel : '是否可抹数',
	    		listeners : {
	    			render : function(e){
	    				Ext.ux.checkPaddingTop(e);
	    			},
	    			check : function(e){
	    				var eraseQuota = Ext.getCmp('numberEraseQuota');
	    				eraseQuota.clearInvalid();
	    				if(e.getValue()){
	    					if(eraseQuota.getValue() == '')
	    						eraseQuota.setValue(0);
	    					eraseQuota.setDisabled(false);
	    				}else{
	    					eraseQuota.setValue(0);
	    					eraseQuota.setDisabled(true);
	    				}
	    			}
	    		}
	    	}]
	    }, {
	    	columnWidth : .55,
	    	labelWidth : 55,
	    	items : [{
	    		xtype : 'numberfield',
	    		id : 'numberEraseQuota',
	    		width : 60,
	    		fieldLabel : '抹数上限',
	    		validator : function(v){
	    			if(v >= 0 && v%1 == 0){
	    				return true;
	    			}else{
	    				return '请输入大于等于 0 的整数金额';
	    			}
	    		}
	    	}]
	    }]
	}],
	bbar : [ '->', {
		text : '保存',
		id : 'btnSavePriceTail',
		iconCls : 'btn_save',
		handler : function(e){
//			alert(formatPrice.priceTail);
			var eraseQuota = Ext.getCmp('numberEraseQuota');
			var eraseQuotaStatus = Ext.getCmp('chbEraseQuotaStatus');
			
			if(eraseQuotaStatus.getValue()){
				if(!eraseQuota.isValid()){
					return;
				}
			}
			
			var btnSave = Ext.getCmp('btnSavePriceTail');
			var btnClose = Ext.getCmp('btnClosePriceTail');
			btnSave.setDisabled(true);
			btnClose.setDisabled(true);
			
			Ext.Ajax.request({
				url : '../../UpdatePriceTail.do',
				params : {
					restaurantID : restaurantID,
					priceTail : formatPrice.priceTail,
					eraseQuota : eraseQuota.getValue()
				},
				success : function(response, options){
					var jr = Ext.util.JSON.decode(response.responseText);
					if(jr.success){
						Ext.example.msg(jr.title, jr.msg);
						formatPrice.hide();
					}else{
						Ext.ux.showMsg(jr);
					}
					btnSave.setDisabled(false);
					btnClose.setDisabled(false);
				},
				failure : function(response, options) {
					btnSave.setDisabled(false);
					btnClose.setDisabled(false);
					Ext.ux.showMsg(Ext.decode(response.responseText));
				}
			});
		}
	}, {
		text : '关闭',
		id : 'btnClosePriceTail',
		iconCls : 'btn_close',
		handler : function(e){
			formatPrice.hide();
		}
	}],
	listeners : {
		show : function(){
			Ext.Ajax.request({
				url : '../../QuerySystemSetting.do',
				params : {
					restaurantID : restaurantID
				},
				success : function(response, options){
					var jr = Ext.util.JSON.decode(response.responseText);
					
					if(jr.success){
						var ss = jr.other.systemSetting;
						if(ss.setting.priceTailValue == 0){
							Ext.getDom('rdoFormatTypeNOACTION').onclick();
						}else if(ss.setting.priceTailValue == 1){
							Ext.getDom('rdoFormatTypeDECIMALCUT').onclick();
						}else if(ss.setting.priceTailValue == 2){
							Ext.getDom('rdoFormatTypeDECIMALROUND').onclick();
						}
						var eraseQuota = Ext.getCmp('numberEraseQuota');
						var eraseQuotaStatus = Ext.getCmp('chbEraseQuotaStatus');
						eraseQuota.setValue(ss.setting.eraseQuota);
						eraseQuotaStatus.setValue(eval(ss.setting.eraseQuota > 0));
					}else{
						Ext.ux.showMsg(jr);
					}
				},
				failure : function(response, options) {
					var jr = Ext.util.JSON.decode(response.responseText);
					Ext.ux.showMsg(jr);
				}
			});
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSavePriceTail').handler();
		}
	}]
});

//---------------------餐厅设置-----------------
var resturantMgr = new Ext.Window({
	modal:true,
    layout      : 'form',
    labelWidth:100,
    width       : 300,
	closable:false,
	resizable:false,
    plain       : true,
    listeners:{
		 "show":function(){
			 Ext.Ajax.request({
				   url: '../../OperateRestaurant.do',
				   params : {
				   		dataSource : 'getByCond',
				   		byId : true
				   },
				   success: function(response,options){
					   var resultJSON = Ext.util.JSON.decode(response.responseText);
					   if(resultJSON.success && resultJSON.root.length > 0){
						   Ext.getCmp('restaurant_info').setValue(resultJSON.root[0].info);
						   Ext.getCmp('address').setValue(resultJSON.root[0].address);
						   Ext.getCmp('tel1').setValue(resultJSON.root[0].tele1);
						   Ext.getCmp('tel2').setValue(resultJSON.root[0].tele2);   
					   } 
				   },
				   failure: function(response,options){
					   
				   }
				});
		 }
	  },
    items:[
	          {
	        	  layout:'form',
        		  autoHeight : true, // important!!
        		  autoWidth : true,
        		  border : false,
        		  anchor : '98%', 
        		  style : 'color:#15428B;',
	        	  items:[
	        	         {
	        	        	 layout:'form',
	        	        	 labelWidth : 100,
	        	     		 border : false,
	        	     		 frame : true,
	        	        	 items:[
	        	        	        {
	        	        	        	xtype:'textarea',
	        	        	        	fieldLabel:'餐厅公告',
	        	        	        	width:155,
	        	        	        	id:'restaurant_info',
	        	        	        	allowBlank:true
	        	        	        },
	        	        	        {
	        	        	        	xtype:'textfield',
	        	        	        	fieldLabel:'餐厅地址',
	        	        	        	width:155,
	        	        	        	id:'address',
	        	        	        	allowBlank:true
	        	        	        },
	        	        	        {
	        	        	        	xtype:'textfield',
	        	        	        	fieldLabel:'餐厅电话1',
	        	        	        	width:155,
	        	        	        	id:'tel1',
	        	        	        	allowBlank:true
	        	        	        },
	        	        	        {
	        	        	        	xtype:'textfield',
	        	        	        	fieldLabel:'餐厅电话2',
	        	        	        	width:155,
	        	        	        	id:'tel2'
	        	        	        }
	        	        	   ]
	        	         }
	        	     ]
	          	}
           ],
	bbar:['->',
		{
			text :'保存',
			iconCls : 'btn_save',
			handler:function(){
				var restaurant_info = Ext.getCmp('restaurant_info').getValue();
				var address = Ext.getCmp('address').getValue();
				var tel1 = Ext.getCmp('tel1').getValue();
				var tel2 = Ext.getCmp('tel2').getValue();
				Ext.Ajax.request({
					   url: '../../OperateRestaurant.do',
					   success: function(response,options){
						   var resultJSON = Ext.util.JSON.decode(response.responseText);
						   if(resultJSON.success){
							   resturantMgr.hide();
							   Ext.getCmp('restaurant_info').setValue("");
							   Ext.getCmp('address').setValue("");
							   Ext.getCmp('tel1').setValue("");
							   Ext.getCmp('tel2').setValue("");
						   }
						   Ext.example.msg('提示', resultJSON.msg);
					   },
					   failure: function(response,options){
						   
					   },
					   params: {
						   restaurantID:restaurantID,
						   restaurant_info:restaurant_info,
						   address:address,
						   tel1:tel1,
						   tel2:tel2,
						   dataSource : 'systemUpdate'
					   }
					});
			},
			id:'btn_save'
		},
		{
			text :'关闭',
			iconCls : 'btn_close',
			handler:function(e){
				resturantMgr.hide();
			}
		}
	]
});

function operateWXInfo(){
	if(!wx.info.win){
		var edit = new Ext.form.HtmlEditor({
			fieldLabel : '餐厅简介',
			hideLabel : true,
			width : 500,
			height : 440,
			enableAlignments: false,
	        enableColors: true,
	        enableFont: false,
	        enableFontSize: true,
	        enableFormat: true,
	        enableLinks: false,
	        enableLists: false,
	        enableSourceEdit: true,
//	        fontFamilies: ["宋体", "隶书", "黑体"],
	        plugins : [new Ext.ux.plugins.HEInsertImage({
	        	url : '../../OperateImage.do?dataSource=upload&ossType=8'
	        })]
		});
		var btnPreview = new Ext.Button({
			text : '预览',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				center.body.update(edit.getValue());
			}
		});
		var btnClear = new Ext.Button({
			text : '清空',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				edit.setValue();
				center.body.update(edit.getValue());
			}
		});
		var btnSave = new Ext.Button({
			text : '保存',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				wx.lm.show();
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'updateInfo',
						info : edit.getValue()
					},
					success : function(res, opt){
						wx.lm.hide();
						var jr = Ext.decode(res.responseText);
						Ext.ux.showMsg(jr);
						if(jr.success){
							wx.info.win.hide();
						}
					},
					fialure : function(res, opt){
						wx.lm.hide();
						Ext.ux.showMsg(res.responseText);
					}
				});
			}
		});
		var btnClose = new Ext.Button({
			text : '关闭',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				wx.info.win.hide();
			}
		});
		var west = new Ext.form.FormPanel({
			region : 'west',
			width : 510,
			items : [edit],
			buttonAlign : 'center',
			buttons : [btnPreview, btnClear, btnSave, btnClose]
		});
		var center = new Ext.Panel({
			id : 'showDivHtml',
			region : 'center',
			style : 'background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
			bodyStyle : 'overflow-y: auto; word-wrap:break-word;',
			html : '&nbsp;'
		});
		
		wx.info.win = new Ext.Window({
			title : '&nbsp;',
			closable : false,
			resizeble : false,
			modal : true,
			width : 850,
			items : [{
				layout : 'border',
				frame : true,
				border : false,
				height : 500,
				items : [west, center]
			}],
			listeners : {
				show : function(){
					wx.lm.show();
					Ext.Ajax.request({
						url : '../../OperateRestaurant.do',
						params : {
							dataSource : 'getInfo'
						},
						success : function(res, opt){
							wx.lm.hide();
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								wx.info.setValue(jr.other.info);
							}else{
								wx.info.setValue('');
								Ext.ux.showMsg(jr);
							}
						},
						fialure : function(res, opt){
							wx.lm.hide();
							Ext.ux.showMsg(res.responseText);
						}
					});
				}
			}
		});
		
		wx.info.setValue = function(v){
			edit.setValue(v);
			btnPreview.handler();
			edit.focus();
		};
		wx.info.clearValue = function(){
			btnClear.handler();
		};
	}
	
	wx.info.win.show();
}

function operateWXLogo(){
	if(!wx.logo.win){
		var uploadMask = new Ext.LoadMask(document.body, {
			msg : '正在上传图片...'
		});
		var box = new Ext.BoxComponent({
			xtype : 'box',
	 	    columnWidth : 1,
	 	    height : 200,
	 	    autoEl : {
	 	    	tag : 'img',
	 	    	title : '图片预览.'
	 	    }
		});
		var imgFile = Ext.ux.plugins.createImageFile({
			img : box,
			width : 468,
			height : 200
		});
		var btnUpload = new Ext.Button({
 	        text : '上传图片',
 	        listeners : {
 	        	render : function(thiz){
 	        		thiz.getEl().setWidth(100, true);
 	        	}
 	        },
 	        handler : function(e){
 	        	var check = true, img = '';
	        	if(Ext.isIE){
	        		Ext.getDom(imgFile.getId()).select();
	        		img = document.selection.createRange().text;
	        	}else{
	 	        	img = Ext.getDom(imgFile.getId()).value;
	        	}
	        	if(typeof(img) != 'undefined' && img.length > 0){
		 	        var type = img.substring(img.lastIndexOf('.') + 1, img.length);
		 	        check = false;
		 	        for(var i = 0; i < Ext.ux.plugins.imgTypes.length; i++){
		 	        	if(type.toLowerCase() == Ext.ux.plugins.imgTypes[i].toLowerCase()){
		 	        		check = true;
			 	           	break;
			 	        }
		 	        }
		 	        if(!check){
			 	       	Ext.example.msg('提示', '图片类型不正确.');
			 	        return;
	 	        	}
	        	}else{
	        		Ext.example.msg('提示', '未选择图片.');
	 	        	return;
	        	}
 	        	uploadMask.show();
 	        	Ext.Ajax.request({
 	        		url : '../../OperateImage.do?dataSource=upload&ossType=7',
	 	   			isUpload : true,
	 	   			form : form.getForm().getEl(),
	 	   			success : function(response, options){
	 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
						Ext.Ajax.request({
							url : '../../OperateRestaurant.do',
							params : {
								dataSource : 'updateLogo',
								logo : jr.root[0].imageId
							},
							success : function(res, opt){
								uploadMask.hide();
								var jr = Ext.decode(res.responseText);
								Ext.example.msg('提示', jr.msg);
							},
							fialure : function(res, opt){
								wx.lm.hide();
								Ext.ux.showMsg(res.responseText);
							}
						});	 	   				
	 	   				
	 	   			},
	 	   			failure : function(response, options){
	 	   				uploadMask.hide();
	 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
	 	   			}
 	        	});
 	        }
		});
		var btnClose = new Ext.Button({
 	        text : '关闭',
 	        listeners : {
 	        	render : function(thiz){
 	        		thiz.getEl().setWidth(100, true);
 	        	}
 	        },
 	        handler : function(e){
 	        	wx.logo.win.hide();
 	        }
		});
		var form = new Ext.form.FormPanel({
			columnWidth : 1,
			labelWidth : 60,
			fileUpload : true,
			items : [imgFile],
			listeners : {
 	    		render : function(e){
 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
	 	  		}
 	    	},
 	    	buttonAlign : 'center',
 	    	buttons : [btnUpload, btnClose]
		});
		
		wx.logo.win = new Ext.Window({
			title : '&nbsp;',
			closable : false,
//			closeAction:'hide',
			resizeble : false,
			modal : true,
			width : 500,
			items : [{
				frame : true,
				layout : 'column',
				items : [box, {
					columnWidth: 1, 
					height: 20,
					html : '<sapn style="font-size:13px;color:green;">提示: 单张图片大小不能超过100KB.</span>'
				}, form]
			}],
			listeners : {
				show : function(){
					Ext.Ajax.request({
						url : '../../OperateRestaurant.do',
						params : {
							dataSource : 'getLogo'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							imgFile.setImg(jr.other.logo);
						},
						fialure : function(res, opt){
							wx.lm.hide();
							Ext.ux.showMsg(res.responseText);
						}
					});
				}
			}
		});
	}
	
	wx.logo.win.show();
}


function warnModule(msg){
	var info = {success : false, code : 9986, title : '提示'};
	info.msg = msg;
	Ext.ux.showMsg(info);
}

var verifyCodeWin;
function showWeixinVerifyCode(){
	$('#weixinVerifyCode').html();
	wx.lm.show();
	
	if(!verifyCodeWin){
		verifyCodeWin = new Ext.Window({
			title : '志易云服务验证二维码',
			closable : true,
			resizeble : false,
			closeAction : 'hide',
			modal : true,
			width : 500,
			height : 500,
			items : [new Ext.Panel({
				contentEl : 'weixinVerifyCode'
			})]
		});		
	}
	
	//使用JQuery跨域调用action
	$.ajax({
	    type : "get",
	    async:false,
	    url : "http://wx.e-tones.net/wx-term/FinanceQRCode.do?restaurantId="+restaurantID,
	    dataType : "jsonp",
	    jsonp: "callbackparam",//服务端用于接收callback调用的function名的参数
	    jsonpCallback:"success_jsonpCallback",//(可选)callback的function名称, 不设置时有默认的名称
	    success : function(json){
	    	wx.lm.hide();
			var jr = json;
			var img = '<img alt="" src="'+ jr.other.qrcode_url +'" width="480px" height="480px">';
			$('#weixinVerifyCode').html(img);
			verifyCodeWin.show();
	    },
	    error:function(){
	    	wx.lm.hide();
	    	Ext.ux.showMsg({title : '提示', msg:'请求超时'});
	    }
	});		
}

