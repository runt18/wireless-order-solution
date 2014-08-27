

var generalPanel = new Ext.Panel({
	height : 160,
	contentEl : 'smsGeneral',
	region : 'north',
	title : '总使用记录'
});
function initAdjustSmsWin(){
	if(!adjustSmsWin){
		var numAdjustSms = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'numAdjustSms',
			fieldLabel : '',
			style : 'color:red;',
			width : 100,
			allowBlank : false,
			blankText : '不能为空, 0 则取消操作.',
			validator : function(value){
				var adjust = document.getElementsByName('radioAdjustSms');
				for(var i=0; i< adjust.length; i++){
					if(adjust[i].checked){
						adjust = adjust[i].value;
						break;
					}
				}
				if(adjust == 2){
					var data = Ext.ux.getSelData(memberBasicGrid);
					if(Math.abs(value) > data['point']){
						Ext.getCmp('numAdjustSms').setValue(data['point']);
					}
					return true;
				}else{
					return true;
				}
			},
			listeners : {
				render : function(){
					Ext.getCmp('radioAdjustSmsIncrease').setValue(true);
				}
			}
		});
		adjustSmsWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			closable : false,
			resizable : false,
			width : 200,
			height : 146,
			layout : 'fit',
			frame : true,
			items : [{
				layout : 'column',
				frame : true,
				defaults : {
					columnWidth : .33,
					layout : 'form',
					labelWidth : 60
				},
				items : [{
					items : [{
						xtype : 'radio',
						id : 'radioAdjustSmsIncrease',
						name : 'radioAdjustSms',
						inputValue : 1,
						hideLabel : true,
						boxLabel : '增加',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustSmsLabel('增加积分');
								}
							}
						}
					}]
				}, {
					items : [{
						xtype : 'radio',
						name : 'radioAdjustSms',
						inputValue : 2,
						hideLabel : true,
						boxLabel : '减少',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustSmsLabel('减少积分');
								}
							}
						}
					}]
				}, {
					columnWidth : 1,
					items : [{
						xtype : 'textfield',
						id : 'numMemberSmsForNow',
						fieldLabel : '当前短信',
						style : 'color:green;',
						width : 100,
						disabled : true
					}]
				}, {
					columnWidth : 1,
					items : [numAdjustSms]
				}]
			}],
			bbar : ['->', {
				text : '保存',
				iconCls : 'btn_save',
				handler : function(){
					var data = Ext.ux.getSelData(memberBasicGrid);
					if(!numAdjustSms.isValid()){
						return;
					}
					if(numAdjustSms.getValue() == 0){
						adjustSmsWin.hide();
						Ext.example.msg('提示', '你输入的积分为0, 无需调整');
						return;
					}
					Ext.Msg.show({
						title : '重要',
						msg : '是否'+Ext.query('label[for="numAdjustSms"]')[0].innerHTML+numAdjustSms.getValue(),
						buttons : Ext.Msg.YESNO,
						icon: Ext.MessageBox.QUESTION,
						fn : function(btn){
							if(btn=='yes'){
								var adjust = document.getElementsByName('radioAdjustSms');
								for(var i=0; i< adjust.length; i++){
									if(adjust[i].checked){
										adjust = adjust[i].value;
										break;
									}
								}
								Ext.Ajax.request({
									url : '../../OperateMember.do',
									params : {
										dataSource : 'adjustSms',
										
										memberId : data['id'],
										point : numAdjustSms.getValue(),
										adjust : adjust
									},
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										if(jr.success){
											adjustSmsWin.hide();
											Ext.example.msg(jr.title, jr.msg);
											Ext.getCmp('btnSearchMember').handler();
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
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					adjustSmsWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					adjustSmsWin.hide();
				}
			}],
			listeners : {
				hide : function(){
					numAdjustSms.setValue();
					Ext.getCmp('radioAdjustSmsIncrease').setValue(true);
				}
			}
		});
	}
}

smsUsedDetailGrid = createGridPanel(
	'',
	'短信明细',
	'',
    '',
    '../../QuerySmsDetail.do',
    [
	    [true, false, true, true], 
	    ['使用日期','modifiedFormad',130],
	    ['类型','operationText',100],
	    ['数量','delta',60, 'right'],
	    ['剩余条数','remaining', 60, 'right'], 
	    ['操作人','staffName',,'center']
	],
	['id', 'modifiedFormad', 'operationValue', 'operationText', 'delta', 'remaining', 'staffName'],
    '',
    smsDetailpageRecordCount,
    '',
    ''
);
smsUsedDetailGrid.region = 'center';


Ext.onReady(function(){
	
	new Ext.Panel({
		renderTo : 'divSmsUseView',
		layout : 'fit',
		height : parseInt(Ext.getDom('divSmsUseView').parentElement.style.height.replace(/px/g,'')),
		border : false,
		items : [{
			layout : 'border',
			border : false,
			items : [generalPanel,smsUsedDetailGrid]
		}]
	}).doLayout();
});