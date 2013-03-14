Ext.onReady(function(){
	var pe = Ext.query('#divMemberRechargeContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var memeberCardAliasID = {
		xtype : 'numberfield',
//		id : 'numberMemberCardAliasID',
		fieldLabel : '会员卡号' + Ext.ux.txtFormat.xh,
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号',
		width : 300,
		allowBlank : false,
		blankText : '会员卡不能为空.'
	};
	
	new Ext.Panel({
		renderTo : 'divMemberRechargeContent',
		width : mw,
		height : mh,
		frame : true,
		items : [{
			xtype : 'panel',
			layout : 'column',
			defaults : {
				xtype : 'form',
				layout : 'form',
				labelWidth : 80,
				labelAlign : 'right',
				columnWidth : .33,
				defaults : {
					xtype : 'numberfield',
					width : 100
				}
			},
			items : [{
				columnWidth : .7,
				items : [memeberCardAliasID]
			}, {
				columnWidth : .3,
				items : [{
					xtype : 'button',
					text : '读卡',
					minWidth : 100
				}]
			}, {
				items : [{
					fieldLabel : '总余额'
				}]
			}, {
				items : [{
					fieldLabel : '基础余额'
				}]
			}, {
				items : [{
					fieldLabel : '赠送余额'
				}]
			}]
		}]
	});
});
