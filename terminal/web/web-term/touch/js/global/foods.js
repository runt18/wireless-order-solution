/**
 * 
 */
WirelessOrder.FoodList = function(source){

	var _foods = null;
	
	if(source){
		_foods = source;
		//按菜品id进行排序
		_foods.sort(function(obj1, obj2){
			if(obj1.id > obj2.id){
				return 1;
			}else if(obj1.id < obj2.id){
				return -1;
			}else{
				return 0;
			}
		});
	}else{
		_foods = [];
	}
	
	var SPECIAL_FLAG = 1 << 0;		/* 特价 */
	var RECOMMEND_FLAG = 1 << 1;	/* 推荐 */ 
	var SELL_OUT_FLAG = 1 << 2;		/* 售完 */
	var GIFT_FLAG = 1 << 3;			/* 赠送 */
	var CUR_PRICE_FLAG = 1 << 4;	/* 时价 */
	var COMBO_FLAG = 1 << 5;		/* 套菜 */
	var HOT_FLAG = 1 << 6;			/* 热销 */
	var WEIGHT_FLAG = 1 << 7;		/* 称重 */
	var COMMISION_FLAG = 1 << 8;	/* 提成 */
	var TEMP_FLAG = 1 << 9;			/* 临时 */
	var LIMIT_FLAG = 1 << 10;		/* 限量估清 */
	
	_foods.status = {
		isSpecial : function(food){
			return (food.status & SPECIAL_FLAG) != 0;
		},
		isRecommend : function(food){
			return (food.status & RECOMMEND_FLAG) != 0;
		},
		isSellout : function(food){
			return (food.status & SELL_OUT_FLAG) != 0;
		},
		isGift : function(food){
			return (food.status & GIFT_FLAG) != 0;
		},
		isCurPrice : function(food){
			return (food.status & CUR_PRICE_FLAG) != 0;
		},
		isCombo : function(food){
			return (food.status & COMBO_FLAG) != 0;
		},
		isHot : function(food){
			return (food.status & HOT_FLAG) != 0;
		},
		isWeight : function(food){
			return (food.status & WEIGHT_FLAG) != 0;
		},
		isCommision : function(food){
			return (food.status & COMMISION_FLAG) != 0;
		},
		isTemp : function(food){
			return (food.status & TEMP_FLAG) != 0;
		},
		isLimit : function(food){
			return (food.status & LIMIT_FLAG) != 0;
		}	
	};
	
	//二分查找菜品的index
	_foods.binaryIndex = function(searchElement){
		'use strict';

		var minIndex = 0;
		var maxIndex = this.length - 1;
		var currentIndex;
		var currentElement;
	
		while (minIndex <= maxIndex) {
			currentIndex = (minIndex + maxIndex) / 2 | 0;
			currentElement = this[currentIndex];
	
			if (currentElement.id < searchElement.id) {
				minIndex = currentIndex + 1;
			}else if (currentElement.id > searchElement.id) {
				maxIndex = currentIndex - 1;
			}else {
				return currentIndex;
			}
		}
	
		return ~maxIndex;
	};
	
	//判断菜品是否估清
	_foods.isSellout = function(index){
		return this.status.isSellout(_foods[index]);
	};
	
	//设置菜品是否估清
	_foods.setSellout = function(index, onOff){
		if(onOff){
			_foods[index].status |= SELL_OUT_FLAG;
		}else{
			_foods[index].status &= ~SELL_OUT_FLAG;
		}
	};
	
	//判断菜品是否限量估清
	_foods.isLimit = function(index){
		return this.status.isLimit(_foods[index]);
	};
	
	//设置菜品是否限量估清
	_foods.setLimit = function(index, onOff, limitAmount, limitRemain){
		if(onOff){
			_foods[index].status |= LIMIT_FLAG;
			_foods[index].foodLimitAmount = limitAmount;
			_foods[index].foodLimitRemain = limitRemain;
		}else{
			_foods[index].status &= ~LIMIT_FLAG;
			_foods[index].foodLimitAmount = 0;
			_foods[index].foodLimitRemain = 0;
		}
	};
	
	//查找相应厨房的菜品
	_foods.getByKitchen = function(kitchenId){
		var value = null;
		if(typeof kitchenId == 'string'){
			value = parseInt(kitchenId);
		}else if(typeof kitchenId == 'number'){
			value = kitchenId;
		}
		
		if(value != null){
			var result = new WirelessOrder.FoodList();
			_foods.forEach(function(e){
				if(e.kitchen.id === value){
					result.push(e);
				}
			});
			return result;
		}else{
			return null;
		}
	};
	
	//查找相应部门的菜品
	_foods.getByDept = function(deptId){
		var value = null;
		if(typeof deptId == 'string'){
			value = parseInt(deptId);
		}else if(typeof deptId == 'number'){
			value = deptId;
		}
		
		if(value != null){
			var result = new WirelessOrder.FoodList();
			_foods.forEach(function(e){
				if(e.kitchen.dept.id === value){
					result.push(e);
				}
			});
			return result;
		}else{
			return null;
		}
	};
	
	//根据拼音查找菜品
	_foods.getByPinyin = function(pinyin){
		var value = pinyin.trim().toLowerCase();
		var result = new WirelessOrder.FoodList();
		_foods.forEach(function(e){
			if(e.pinyin.indexOf(value) != -1){
				result.push(e);
			}
		});
		return result;	
	};
	
	//根据菜名查找菜品
	_foods.getByName = function(name){
		var value = name.trim();
		var result = new WirelessOrder.FoodList();
		_foods.forEach(function(e){
			if(e.name.indexOf(value) != -1){
				result.push(e);
			}
		});
		return result;
	};
	
	//根据助记码查找菜品
	_foods.getByAlias = function(aliasId){
		var value = null;
		if(typeof aliasId == 'string'){
			value = parseInt(aliasId);
		}else if(typeof aliasId == 'number'){
			value = aliasId;
		}
		if(value != null){
			for(var i = 0; i < _foods.length; i++){
				if(!isNaN(_foods[i].alias) && parseInt(_foods[i].alias) === value){
					return _foods[i];
				}
			}
		}else{
			return null;
		}
	};
	
	//根据菜品id查找
	_foods.getById = function(id){
		var index = this.binaryIndex({id : id});
		if(index >= 0){
			return _foods[index];
		}else{
			return null;
		}
	};
	
	return _foods;
};

