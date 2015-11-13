/**
 * 
 */

WirelessOrder.Staff = function(staff){

	var _staff = staff;
	
	_staff.hasPrivilege = function(privilege){
		for (var i = 0; i < _staff.role.privileges.length; i++) {
			if(_staff.role.privileges[i].codeValue === privilege.val){
				return true;
			}
		}
		return false;
	}
	
	return _staff;
}

//权限列表
WirelessOrder.Staff.Privilege = {
	ADD_FOOD : { val : 1000, desc : '点菜'},
	CANCEL_FOOD : { val : 1001, desc : '退菜'},
	DISCOUNT : { val : 1002, desc : '折扣'},
	GIFT : { val : 1003, desc : '赠送'},
	RE_PAYMENT : { val : 1004, desc : '反结账'},
	PAYMENT : { val : 1005, desc : '结账'},
	CHECK_ORDER : { val : 1006, desc : '查看账单'},
	TEMP_PAYMENT : { val : 1007, desc : '暂结'},
	PRICE_PLAN : { val : 1008, desc : '价格方案'},
	TRANSFER_FOOD : { val : 1009, desc : '转菜'}
};


