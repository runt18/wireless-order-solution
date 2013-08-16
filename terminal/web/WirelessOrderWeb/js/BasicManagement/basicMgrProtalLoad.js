function cancelReasonRenderer (){
	return ''
		   + '<a href="javascript:updateCancelReasonHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteCancelReasonHandler()">删除</a>';
}
function initWin(){
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
						cancelReason : Ext.encode(cancelReason)
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

// on page load function
function loginOnLoad() {

	var Request = new URLParaQuery();
	restaurantID = Request["restaurantID"];

	// for local test
	if (restaurantID == undefined) {
		restaurantID = "11";
	}

	// protal function register
	protalFuncReg();

	// update the operator name
/*	if (currPin != "") {
		getOperatorName(currPin, "../../");
	}*/
	getOperatorName("../../");
	// mouse over & mouse off -- heightlight the icon
	$("#menuMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/menuMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/menuMgr.png) no-repeat 50%");
		});
	});

	$("#kitchenMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/kitchenMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/kitchenMgr.png) no-repeat 50%");
		});
	});

	$("#departmentMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/departmentMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/departmentMgr.png) no-repeat 50%");
		});
	});

	$("#regionMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/regionMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/regionMgr.png) no-repeat 50%");
		});
	});

	$("#tableMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/tableMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/tableMgr.png) no-repeat 50%");
		});
	});

	$("#tasteMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/tasteMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/tasteMgr.png) no-repeat 50%");
		});
	});

	$("#terminalMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/terminalMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/terminalMgr.png) no-repeat 50%");
		});
	});

	$("#staffMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/staffMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/staffMgr.png) no-repeat 50%");
		});
	});
	
	$("#discountMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/discountMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/discountMgr.png) no-repeat 50%");
		});
	});
	
	$("#priceMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/foodPricePlanMrg_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/foodPricePlanMrg.png) no-repeat 50%");
		});
	});
	
	$("#cancelReasonMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/cancelReasonMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/cancelReasonMgr.png) no-repeat 50%");
		});
	});
	
	$("#printScheme").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/cancelReasonMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/cancelReasonMgr.png) no-repeat 50%");
		});
	});
	
};