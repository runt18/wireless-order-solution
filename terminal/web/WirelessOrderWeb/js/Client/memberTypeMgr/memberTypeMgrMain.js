var btnInsertMemberType = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加会员类型',
	handler : function(e){
		insertMemberTypeHandler();
	}
});

var btnPushBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(e){
		location.href = './ClientMain.html?restaurantID=' + restaurantID + '&isNewAccess=false&' + '&pin=' + pin;
	}
});

var btnLogOut =  new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(e){
		
	}
});

/**********************************************************************/

insertMemberTypeHandler = function(){
	memberTypeOperationHandler({
		type : mtObj.operation['insert']
	});
};

updateMemberTypeHandler = function(){
	memberTypeOperationHandler({
		type : mtObj.operation['update']
	});
};

deleteMemberTypeHandler = function(){
	memberTypeOperationHandler({
		type : mtObj.operation['delete']
	});
};

memberTypeOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	memberTypeWin.otype = c.type;
	
	if(c.type == mtObj.operation['insert']){
		
		memberTypeWin.setTitle('添加会员类别');
		memberTypeWin.show();
		memberTypeWin.center();
		
	}else if(c.type == mtObj.operation['update']){
		
		memberTypeWin.setTitle('修改会员类别');
		memberTypeWin.show();
		memberTypeWin.center();
		
	}else if(c.type == mtObj.operation['delete']){
		alert(c.type);
	}
	
};

/**********************************************************************/
memberTypeRenderer = function(){
	return ''
		   + '<a href="javascript:updateMemberTypeHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteMemberTypeHandler()">删除</a>';
};

var memberTypeGridTbar = new Ext.Toolbar({
	items : [{
		xtype: 'tbtext',
		text : '过滤:'
	}, {
		xtype : 'combo',
		id : 'comboSearchType',
		readOnly : true,
		forceSelection : true,
		width : 80,
		value : 0,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : [[0, '全部'], [1, '会员类型'], [2, '折扣方式']]
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(thiz, record, index){
				
			}
		}
	}, '->', {
		text : '搜索',
		id : 'btnSearchMemberType',
		iconCls : 'btn_search',
		handler : function(e){
			
		}
	}, {
		text : '添加',
		id : 'btnInsertMemberType',
		iconCls : 'btn_add',
		handler : function(e){
			insertMemberTypeHandler();
		}
	}, {
		text : '修改',
		id : 'btnUpdateMemberType',
		iconCls : 'btn_edit',
		handler : function(e){
			updateMemberTypeHandler();
		}
	}, {
		text : '删除',
		id : 'btnDeleteMemberType',
		iconCls : 'btn_delete',
		handler : function(e){
			deleteMemberTypeHandler();
		}
	}]
});

var memberTypeGrid = createGridPanel(
	'memberTypeGrid',
	'',
	'',
	'',
	'../../QueryClient.do',
	[
		[true, false, true, false], 
		['类型编号', 'clientID'],
		['类型名称', 'name', 150],
		['折扣方式', 'clientType.name'],
		['折扣率', 'sexDisplay'],
		['折扣方案', 'mobile'],
		['积分比率', 'tele'],
		['操作', 'operation', 200, 'center', 'memberTypeRenderer']
	],
	['clientID', 'name', 'clientType.name', 'clientType.typeID', 'birthdayFormat', 'birthday',
	 'memberAccount', 'sexDisplay', 'sex', 'mobile', 'tele', 'company', 
	 'tastePref', 'clientID', 'taboo', 'comment', 'contactAddress', 'IDCard'],
	[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
	30,
	'',
	memberTypeGridTbar
);	
memberTypeGrid.region = 'center';


/**********************************************************************/
var memberTypeWin;
Ext.onReady(function(){
	
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	var centerPanel = new Ext.Panel({
		title : '会员类型管理',
		region : 'center',
		layout : 'border',
		items : [memberTypeGrid],
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'}, 
			    btnInsertMemberType,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    '->',
			    btnPushBack,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnLogOut
			]
		})
	});

	 new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerPanel,
		{
			region : 'south',
			height : 30,
			layout : 'form',
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	 });
	 
	 memberTypeInit();
});

