﻿

/**
 * 修改部门信息
 */
function initCouponTypeWin(){

	operateCouponTypeWin = Ext.getCmp('operateCouponTypeWin');
	if(!operateCouponTypeWin){
		operateCouponTypeWin = new Ext.Window({
			id : 'operateCouponTypeWin',
			title : '添加优惠劵类型',
			closable : false,
			resizable : false,
			modal : true,
			width : 245,			
			items : [{
				xtype : 'form',
				layout : 'form',
				width : 245,
				labelWidth : 65,
				frame : true,
				items : [{
					xtype : 'textfield',
					id : 'txtCouponTypeName',
					width : 130,
					fieldLabel : '名称',
					allowBlank : false
				}, {
					xtype : 'numberfield',
					id : 'numCouponPrice',
					width : 130,
					fieldLabel : '代金额',
					allowBlank : false
				},{
					id : 'dateForExpired',
					xtype : 'datefield',
					width : 130,
					fieldLabel : '日期',
					format : 'Y-m-d',
					readOnly : false,
					allowBlank : false,
					blankText : '日期不能为空.'
				},{
					xtype : 'textarea',
					id : 'txtDirectionsForCouponType',
					fieldLabel : '备注',
					width : 130
				},{
					xtype : 'hidden',
					id : 'txtCouponTypeId'
				}]
			}],
			bbar : [
				'->',
				{
					text : '保存',
					id : 'btnSaveCoupon',
					iconCls : 'btn_save',
					handler : function(){
						
						var id = Ext.getCmp('txtCouponTypeId');
						var name = Ext.getCmp('txtCouponTypeName');
						var price = Ext.getCmp('numCouponPrice');
						var date = Ext.getCmp('dateForExpired');
						var dataSource = 'insert';
						
						if(operateCouponTypeWin.otype == 'insert') {
							dataSource = 'insert';
						}else if(operateCouponTypeWin.otype == 'update'){
							dataSource = 'update';
						}
						
						if(!name.isValid() || !price.isValid() || !date.isValid()){
							return;
						}
						Ext.Ajax.request({
							url : '../../OperateCouponType.do',
							params : {
								dataSource : dataSource,
								typeId : id.getValue(),
								typeName : name.getValue(),
								price : price.getValue(),
								date : date.getValue().getTime()
							},
							success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									operateCouponTypeWin.hide();
									couponTree.getRootNode().reload();
								}else{
									Ext.ux.showMsg(jr);
								}
							},
							failure : function(res, opt) {
								Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
							}
						});
						
					}
				}, {
					text : '关闭',
					id : 'btnCancelUpdateKitchen',
					iconCls : 'btn_close',
					handler : function(){
						operateCouponTypeWin.hide();
					}
				}
			],
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveCoupon').handler();
				 },
				 scope : this 
			 }]
		});
	}
};


function getMemberCounts(){
	var memberTypes = '';
	var checkMemberTypes = document.getElementsByName('memberType');
	for (var i = 0; i < checkMemberTypes.length; i++) {
		if(checkMemberTypes[i].checked && memberTypes == ''){
			memberTypes += checkMemberTypes[i].value;
		}else if(checkMemberTypes[i].checked && memberTypes != ''){
			memberTypes += (',' + checkMemberTypes[i].value);
		}
	}
	var gs = memberCountGrid.getStore();
	gs.load({params : {
		memberTypes : memberTypes
	}});
}

function initSendCouponWin(floatBarNodeId){
	memberCountGrid = createGridPanel(
		'memberCount',
		'',
		'300',
		'',
		'../../QueryMember.do',
		[
		    [true, false, false, false], 
			['会员类型', 'name',,'center'] , 
			['数量', 'memberCount',,'right']
		],
		['name','memberCount'],
		[ ['isPaging', false], ['dataSource', 'count']],
		0,
		'',
		''
	);
	memberCountGrid.region = 'center';
	memberCountGrid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = memberCountGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < memberCountGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = memberCountGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';	
				sumCell.style.color = 'green';
			}
			memberCountGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '发放总数';
		}
	});
	
	sendCouponWin = new Ext.Window({
		id : 'sendCouponWin',
		title : '发放优惠劵',
		closable : false,
		resizable : false,
		modal : true,
		width : 600,			
		items : [{
			xtype : 'form',
			layout : 'form',
			width : 590,
			frame : true,
			items : [{
				xtype : 'label',
				width : 130,
				fieldLabel : '会员类型'
			},{
				//所有厨房
				columnWidth : 1,
				layout : 'column',
				id : 'allMemberType',
				defaults : {
					columnWidth : .2,
					layout : 'form',
					labelWidth : 70
				}
			}, 	new Ext.Panel({
				layout : 'border',
				height : 300,
				border : false,
				frame : false,
				items : [memberCountGrid]
			})]
		}],
		listeners : {
			show : function(){
				Ext.Ajax.request({
					url : '../../QueryMemberType.do',
					params : {
						dataSource : 'normal'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						if(jr.success){
							for ( var i = 0; i < jr.root.length; i++) {
								
								var m = jr.root[i];
								var c = {items : [{
									xtype : "checkbox", 
									name : "memberType",
									boxLabel : m.name , 
									hideLabel : true, 
									inputValue : m.id ,
									listeners : {
										check : function(){
											getMemberCounts();
										}
									}
								}]};
								Ext.getCmp('allMemberType').add(c);
								if((i+1)%5 == 0){
									Ext.getCmp('allMemberType').add({columnWidth : 1});
								}
								Ext.getCmp('allMemberType').doLayout();
							
							}
							
						}else{
							Ext.ux.showMsg(jr);
						}
				
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				
					
				});
				
			}
		},
		bbar : ['->',{
				text : '发放',
				id : 'btnSaveFunc',
				iconCls : 'btn_save',
				handler : function(e){
					var memberTypes = '';
					var checkMemberTypes = document.getElementsByName('memberType');
					for (var i = 0; i < checkMemberTypes.length; i++) {
						if(checkMemberTypes[i].checked && memberTypes == ''){
							memberTypes += checkMemberTypes[i].value;
						}else if(checkMemberTypes[i].checked && memberTypes != ''){
							memberTypes += (',' + checkMemberTypes[i].value);
						}
					}
					var node = couponTree.getNodeById(floatBarNodeId);
					if(!node){
						Ext.ux.showMsg({title : '提示', msg : '请选中一个优惠劵',success : true});
						return;
					}
					
					if(memberTypes == ''){
						Ext.ux.showMsg({title : '提示', msg : '请选中一个会员类型', success : true});
						return;
					}
					
					Ext.Ajax.request({
						url : '../../OperateCoupon.do',
						params : {
							memberTypes : memberTypes,
							typeId : node.attributes.couponTypeId,
							dataSource : 'insert'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.getCmp('couponGrid').store.reload();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnCancelSendCoupon').handler();
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
				id : 'btnCancelSendCoupon',
				iconCls : 'btn_close',
				handler : function(){
/*					sendCouponWin.hide();
					var checkMemberTypes = document.getElementsByName('memberType');
					for (var i = 0; i < checkMemberTypes.length; i++) {
						if(checkMemberTypes[i].checked){
							checkMemberTypes[i].checked = false;
						}
					}
					memberCountGrid.getStore().removeAll();*/
					sendCouponWin.destroy();
				}
			}]
		});
	}
	
function couponTypeOperation(c){
	if(c == null || typeof c == 'undefined'){
		return;
	}
	var id = Ext.getCmp('txtCouponTypeId');
	var name = Ext.getCmp('txtCouponTypeName');
	var price = Ext.getCmp('numCouponPrice');
	var date = Ext.getCmp('dateForExpired');
	
	var data = c.data == null || typeof c.data == 'undefined' ? {attributes : {}} : c.data;
	
	if(c.otype == 'insert'){
		operateCouponTypeWin.otype = 'insert';
		price.enable();
	}else if(c.otype == 'update'){
		if(!data){
			Ext.example.msg('提示', '请选中一个数据再进行操作.');
			return;
		}
		operateCouponTypeWin.otype = 'update';
		price.disable();
	}
	
	operateCouponTypeWin.show();
	operateCouponTypeWin.center();
	
	id.setValue(data.attributes.couponTypeId);
	name.setValue(data.text);
	price.setValue(data.attributes.price);
	date.setValue(data.attributes.date);
	name.focus(true, 100);
	
	name.clearInvalid();
	price.clearInvalid();
	date.clearInvalid();
}

function floatBarUpdateHandler(){
	couponTypeOperation({otype : 'update', data : Ext.ux.getSelNode(couponTree)});	
}

function floatBarSendCouponHandler(){
	initSendCouponWin(floatBarNodeId);
	sendCouponWin.show();
}

function floatBarDeleteHandler(){
	var node = Ext.ux.getSelNode(couponTree);
	if(!node){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + node.text,
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateCouponType.do',
					params : {
						typeId : node.attributes.couponTypeId,
						dataSource : 'delete'
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, node.text));
							couponTree.getRootNode().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}
	);
}


var couponTree;
var updateDeptWin;
var couponGrid, memberCountGrid;
var operateCouponTypeWin, sendCouponWin;
var bar = {treeId : 'couponTypeTree', option :[{name : '发放', fn : "floatBarSendCouponHandler()"}, {name : '修改', fn : "floatBarUpdateHandler()"}, {name : '删除', fn : "floatBarDeleteHandler()"}]};
Ext.onReady(function() {
	initCouponTypeWin();
	couponTree = new Ext.tree.TreePanel({
		title : '优惠劵信息',
		id : 'couponTypeTree',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryCouponType.do',
			baseParams : {
				dataSource : 'tree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部类型',
	        leaf : false,
	        border : true,
	        couponTypeId : -1,
	        listeners : {
	        	load : function(){
//	        		var treeRoot = couponTree.getRootNode().childNodes;
	        		
	        	}
	        }
		}),
		tbar : [
		    '->', 
		    {
				text : '添加',
				iconCls : 'btn_add',
				handler : function(e){
					couponTypeOperation({otype : 'insert', data : null});				
				}
			},{
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					couponTree.getRootNode().reload();
				}
			}
		],
		listeners : {
			click : function(e){
				Ext.getCmp('btnSearchCoupon').handler();
				Ext.getDom('couponTypeNameShowType').innerHTML = e.text;
			}
		}
	});
	
	var coupon_dateCombo = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'coupon_statusCombo',
		forceSelection : true,
		width : 100,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text']
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				thiz.store.loadData([[1,'已发放'], [2,'已使用'], [3,'已过期']]);					
			},
			select : function(thiz, record, index){
				Ext.getCmp('btnSearchCoupon').handler();
			}
		}
	});
	
	var couponGridTbar = new Ext.Toolbar({
		height : 26,
		items : [
		    {xtype:'tbtext', text:String.format(Ext.ux.txtFormat.typeName, '类型', 'couponTypeNameShowType', '----')},
		    {xtype:'tbtext', text:'&nbsp;&nbsp;'},
			{
				xtype : 'tbtext',
				text : '状态:'
			}, coupon_dateCombo,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '会员信息:'
			},{
		    	xtype : 'textfield',
		    	id : 'txtSearchMemberDetail'
	    	},
			'->',
			{
				text : '搜索',
				id : 'btnSearchCoupon',
				iconCls : 'btn_search',
				handler : function(){
					var couponTypeId = '';
					
					var sn = couponTree.getSelectionModel().getSelectedNode();
					couponTypeId = !sn ? couponTypeId : sn.attributes.couponTypeId;
					var memberDetail = Ext.getCmp('txtSearchMemberDetail').getValue();
					var gs = couponGrid.getStore();
					gs.baseParams['couponTypeId'] = couponTypeId;
					gs.baseParams['memberName'] = isNaN(memberDetail)?memberDetail : '';
					gs.baseParams['memberMobile'] = !isNaN(memberDetail)?memberDetail : '';
					gs.baseParams['status'] = coupon_dateCombo.getValue();
					
					gs.load({
						params : {
							start : 0,
							limit : couponPageRecordCount
						}
					});
				}
			}
		]
	});
	
	couponGrid = createGridPanel(
		'couponGrid',
		'持有信息',
		'',
		'',
		'../../QueryCoupon.do',
		[
		    [true, false, false, true], 
			['会员姓名', 'member.name'] , 
			['会员号码', 'member.mobile'] , 
			['到期时间', 'couponType.expiredFormat'],
			['状态', 'statusText']
		],
		CouponRecord.getKeys(),
		[ ['isPaging', true]],
		couponPageRecordCount,
		'',
		couponGridTbar
	);
	couponGrid.region = 'center';
	couponGrid.on('rowdblclick', function(){
		updateKitchen();
	});
	
	new Ext.Panel({
		renderTo : 'divDept',
		layout : 'border',
		width : parseInt(Ext.getDom('divDept').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divDept').parentElement.style.height.replace(/px/g,'')),
		items : [couponTree, couponGrid]
	});
	showFloatOption(bar);
});