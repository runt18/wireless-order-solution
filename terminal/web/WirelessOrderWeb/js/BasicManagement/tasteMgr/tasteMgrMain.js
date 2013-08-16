var tasteAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加口味',
	handler : function(btn) {
		tasteInsertHandler();
	}
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn) {
		location.href = 'BasicMgrProtal.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn) {
	}
});

Ext.onReady(function() {
	initTasteGrid();
	initTasteOperatorWin();
	
	var centerPanel = new Ext.Panel({
		title : '口味管理',
		region : 'center',
		layout : 'fit',
		frame : true,
		items : [tasteGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    tasteAddBut,
			    { xtype:'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;' },
				'->', 
				pushBackBut, 
				{ xtype:'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;' }, 
				logOutBut 
			]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){	
				Ext.getCmp('btnSerachForTasteBasic').handler(); 
			}
		}]
	});
	
	initMainView(null, centerPanel, null);
	getOperatorName("../../");
});


/**
 * 
 */
function tasteDeleteHandler() {
	var data = Ext.ux.getSelData(tasteGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	Ext.MessageBox.show({
		msg : '确定删除?',
		buttons : Ext.MessageBox.YESNO,
		icon: Ext.MessageBox.QUESTION,
		fn : function(btn) {
			if (btn == 'yes') {
				Ext.Ajax.request({
					url : '../../OperateTaste.do',
					params : {
						'dataSource' : 'delete',
						
						id : data['id']
					},
					success : function(response, options) {
						var jr = Ext.decode(response.responseText);
						if (jr.success == true) {							
							Ext.example.msg('提示', jr.msg);
							Ext.getCmp('btnSerachForTasteBasic').handler();
						} else {
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(response, options) {
						Ext.ux.showMsg(Ext.decode(response.responseText));
					}
				});
			}
		}
	});
};

function tasteInsertHandler(){
	tasteOperatorWin.otype = Ext.ux.otype['insert'];
	tasteOperatorWin.show();
	tasteOperatorWin.center();
	tasteOperatorWin.setTitle("添加新口味信息");
	operatorWinData({
		otype : Ext.ux.otype['set'],
		data : {}
	});
}
function tasteUpdateHandler(){
	var data = Ext.ux.getSelData(tasteGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	tasteOperatorWin.otype = Ext.ux.otype['update'];
	tasteOperatorWin.show();
	tasteOperatorWin.center();
	tasteOperatorWin.setTitle("修改口味信息 -- " + data['name']);
	operatorWinData({
		otype : Ext.ux.otype['set'],
		data : data
	});
}

function operatorWinData(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	var tasteId = Ext.getCmp('hideTasteId');
	var tasteAlias = Ext.getCmp('numTasteAlias');
	var tasteName = Ext.getCmp('txtTasteName');
	var tastePrice = Ext.getCmp('numTastePrice');
	var tasteRate = Ext.getCmp('numTasteRate');
	var tasteCate = Ext.getCmp('comboTasteCate');
	
	if(c.otype == Ext.ux.otype['set']){
		tasteId.setValue(c.data['id']);
		tasteAlias.setValue(c.data['alias']);
		tasteName.setValue(c.data['name']);
		tasteCate.setValue(typeof c.data['cateValue'] == 'undefined' ? 0 : c.data['cateValue']);
		tasteCate.fireEvent('select', tasteCate);
		if(c.data['typeValue'] == 1){
			tasteCate.setDisabled(true);
		}else{
			tasteCate.setDisabled(false);
		}
		tastePrice.setValue(typeof c.data['price'] == 'undefined' ? 0 : c.data['price']);
		tasteRate.setValue(typeof c.data['rate'] == 'undefined' ? 0 : c.data['rate']);
		
		tasteAlias.clearInvalid();
		tasteName.clearInvalid();
		tastePrice.clearInvalid();
		tasteRate.clearInvalid();
	}else if(c.otype == Ext.ux.otype['get']){
		c.data = {
			id : tasteId.getValue(),
			alias : tasteAlias.getValue(),
			name : tasteName.getValue(),
			price : tastePrice.getValue(),
			rate : tasteRate.getValue(),
			cateValue : tasteCate.getValue()
		};
		return data;
	}
}