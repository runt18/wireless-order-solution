/**
 * 
 */
function operationMemberData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var memberID = Ext.getCmp('numberMemberID');
	var memberCardAliasID = Ext.getCmp('numberMemberCardAliasID');
	var memberType = Ext.getCmp('comboMemberType');
	var point = Ext.getCmp('numberMmeberPoint');
	var totalBalance = Ext.getCmp('numberTotalBalance');
	var baseBalance = Ext.getCmp('numberBaseBalance');
	var extraBalance = Ext.getCmp('numberExtraBalance');
	var lastStaff = Ext.getCmp('txtLastStaff');
	var lastModDate = Ext.getCmp('txtLastModDate');
	var comment = Ext.getCmp('txtOperationComment');
	var status = Ext.getCmp('comboMemberStatus');
	if(c.type == Ext.ux.otype['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		memberID.setValue(data['id']);
		memberCardAliasID.setValue(data['memberCard.aliasID']);
		memberType.setValue(data['memberTypeID']);
		point.setValue(data['point']);
		totalBalance.setValue(data['totalBalance']);
		baseBalance.setValue(data['baseBalance']);
		extraBalance.setValue(data['extraBalance']);
		lastStaff.setValue(data['staff.name']);
		lastModDate.setValue(data['lastModDateFormat']);
		comment.setValue(data['comment']);
		status.setValue(data['statusValue']);
	}else if(c.type == Ext.ux.otype['get']){
		data = {
			id : memberID.getValue(),
			memberType : {
				typeID : memberType.getValue()
			},
			memberCard : {
				aliasID : memberCardAliasID.getValue()
			},
			status : status.getValue()
		};
		c.data = data;
	}
	memberCardAliasID.clearInvalid();
	memberType.clearInvalid();
	status.clearInvalid();
	return c;
};

/**
 * 
 */
function operationMemberBasicMsg(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	if(c.type == Ext.ux.otype['set']){
		data = c.data;
		operationMemberData({
			type : c.type,
			data : data
		});
		operationClientDataToMember({
			type : c.type,
			data : data['client']
		});
	}else if(c.type == Ext.ux.otype['get']){
		data = operationMemberData({
			type : c.type
		}).data;
		data.client = operationClientDataToMember({
			type : c.type
		}).data;
		c.data = data;
	}
	return c;
}