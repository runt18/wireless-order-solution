var md_memberDetailSreachMemberCardWin;
var md_memberDetailData;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberDeatilContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'md_numMemberCardAliasForMemberDetail',
		inputType : 'password',
		fieldLabel : '会员卡号' + Ext.ux.txtFormat.xh,
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号',
		width : 110,
		allowBlank : false,
		blankText : '会员卡不能为空, 请刷卡.',
		listeners : {
			render : function(e){
				
			}
		}
	};
	
	new Ext.Panel({
		renderTo : 'divMemberDeatilContent',
		width : mw,
		height : mh,
		frame : true,
		items : [{
			xtype : 'panel',
			layout : 'column',
			defaults : {
				xtype : 'panel',
				layout : 'form',
				labelWidth : 80,
				labelAlign : 'right',
				columnWidth : .5,
				defaults : {
					xtype : 'textfield',
					width : 110
				}
			},
			items : [ {
				columnWidth : .5,
				items : [memeberCardAliasID]
			}, {
				xtype : 'panel',
				columnWidth : .5,
				html : ['<div class="x-form-item" >',
				    '<input type="button" value="查找" onClick="" style="cursor:pointer; width:80px;" />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读卡" onClick="memberDetailLoadData()" style="cursor:pointer; width:80px;" />',
				    '</div>'
				].join('')
			}, {
				items : [{
					fieldLabel : '会员名称'
				}]
			}, {
				items : [{
					fieldLabel : '会员类型'
				}]
			}, {
				items : [{
					fieldLabel : '余额总额'
				}]
			}, {
				items : [{
					fieldLabel : '剩余积分'
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
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				memberDetailLoadData();
			}
		}]
	});
});

/**
 * 
 * @param _c
 */
function memberDetailLoadData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	var cardAlias = Ext.getCmp('md_numMemberCardAliasForMemberDetail');
	if(typeof _c.memberCard != 'undefined'){
		cardAlias.setValue(_c.memberCard);
	}else{
		if(!cardAlias.isValid()){
			return;
		}
	}
	var rd_mask_load_recharge = new Ext.LoadMask(document.body, {
		msg : '正在读卡, 请稍后......',
		remove : true
	});
	rd_mask_load_recharge.show();
	Ext.Ajax.request({
		url : '../../QueryMember.do',
		params : {
			pin : pin,
			restaurantID : restaurantID,
			dataSource : 'normal',
			params : Ext.encode({
				searchType : 2,
				searchOperation : 0,
				searchValue : cardAlias.getValue()
			})
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				rechargeOperateData = jr;
			}else{
				Ext.ux.showMsg(jr);
			}
			rd_mask_load_recharge.hide();
		},
		failure : function(res, opt){
			rd_mask_load_recharge.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}