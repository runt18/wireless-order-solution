var Wireless = {
	ux : {staffGift : false}
};

/*var TempFoodModel = {
	isTemporary : 'isTemporary',
	id : 'id',
	foodName : 'foodName',
	count : 'count',
	unitPrice : 'unitPrice',
	isHangup : 'isHangup',
	dataType : 'dataType',
	kitchen : 'kitchen',
	cancelReason : 'cancelReason'
};

var NormalFoodModel = {
	isTemporary : 'isTemporary',
	id : 'id',
	count : 'count',
	tasteGroup : 'tasteGroup',
	discount : 'discount',
	kitchen : 'kitchen',
	isHangup : 'isHangup',
	cancelReason : 'cancelReason'
};*/

var foodModel = {
	isTemporary : 'isTemporary',
	id : 'id',
	foodName : 'foodName',
	unitPrice : 'unitPrice',
	count : 'count',
	tasteGroup : 'tasteGroup',
	kitchen : 'kitchen',
	isHangup : 'isHangup',
	cancelReason : 'cancelReason'
};


var orderDataModel = {
	tableAlias : null,
	customNum : null,
	orderFoods : null,
	categoryValue : null,
	id : null,
	orderDate : null
};

/**
 * 清除为空的字段
 * @param {} obj
 * @return {}
 */
Wireless.ux.commitOrderData = function(obj){
	for(var s in obj){
		if(typeof obj[s] == 'object' && obj[s] != null){
			Wireless.ux.commitOrderData(obj[s]);
		}else if (obj[s] == null){
			delete obj[s];
		};
	}
	return obj;
};

Wireless.ux.createOrder = function(c){
	
	var foodPara = '';
	var temp = null;
	for ( var i = 0; i < c.orderFoods.length; i++) {
		temp = c.orderFoods[i];
		foodPara += ( i > 0 ? '<<sh>>' : '');
		if (temp.isTemporary) {
			// 临时菜
			var foodName = temp.name;
			foodName = foodName.indexOf('<') > 0 ? foodName.substring(0,foodName.indexOf('<')) : foodName;
			foodPara = foodPara 
					+ '[' 
					+ 'true' + '<<sb>>'// 是否临时菜(true)
					+ temp.id + '<<sb>>' // 临时菜1编号
					+ foodName + '<<sb>>' // 临时菜1名称
					+ temp.count + '<<sb>>' // 临时菜1数量
					+ temp.unitPrice + '<<sb>>' // 临时菜1单价(原料單價)
					+ (typeof temp.isHangup != 'undefined' ?  temp.isHangup : false) +'<<sb>>' // 菜品状态,暂时没用
					+ (typeof temp.dataType != 'undefined'? temp.dataType : c.dataType) + '<<sb>>' // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
					+ temp.kitchen.id + '<<sb>>'	// 临时菜出单厨房
					+ (typeof temp.cancelReason != 'undefined' ?  temp.cancelReason : 0) //退菜原因
					+ ']';
		}else{
			// 普通菜
			var normalTaste = '', tmpTaste = '' , tasteGroup = temp.tasteGroup;
			for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
				var t = tasteGroup.normalTasteContent[j];
				normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.id + '<<stb>>' + t.cateValue + '<<stb>>' + t.cateStatusValue));
			}
			if(tasteGroup.tmpTaste != null && typeof tasteGroup.tmpTaste != 'undefined'){
				if(eval(tasteGroup.tmpTaste.id >= 0))
					tmpTaste = tasteGroup.tmpTaste.price + '<<sttt>>' + tasteGroup.tmpTaste.name  + '<<sttt>>' + tasteGroup.tmpTaste.id+ '<<sttt>>' + tasteGroup.tmpTaste.alias; 				
			}
			foodPara = foodPara 
					+ '['
					+ 'false' + '<<sb>>' // 是否临时菜(false)
					+ temp.id + '<<sb>>' // 菜品1编号
					+ temp.count + '<<sb>>' // 菜品1数量
					+ (normalTaste + ' <<st>> ' + tmpTaste) + '<<sb>>'
					+ temp.kitchen.id + '<<sb>>'// 厨房1编号
					+ '1' + '<<sb>>' // 菜品1折扣
//					+ temp.dataType  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
					+ (typeof temp.isHangup != 'undefined' ?  temp.isHangup : false) + '<<sb>>'
					+ (typeof temp.cancelReason != 'undefined' ?  temp.cancelReason : 0) //退菜原因
					+ ']';
		}
	}	
	temp = null;
	return '{' + foodPara + '}';
};