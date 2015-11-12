/**
 * 
 */
function FoodList(source){

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
			}
			else if (currentElement.id > searchElement.id) {
				maxIndex = currentIndex - 1;
			}
			else {
				return currentIndex;
			}
		}
	
		return ~maxIndex;
	};
	
	//判断菜品是否估清
	_foods.isSellout = function(index){
		return (_foods[index].status & 1 << 2) != 0;
	};
	
	//设置菜品是否估清
	_foods.setSellout = function(index, onOff){
		if(onOff){
			_foods[index].status |= (1 << 2);
		}else{
			_foods[index].status &= ~(1 << 2);
		}
	};
	
	//判断菜品是否限量估清
	_foods.isLimit = function(index){
		return (_foods[index].status & 1 << 10) != 0;
	};
	
	//设置菜品是否限量估清
	_foods.setLimit = function(index, onOff, limitAmount, limitRemain){
		if(onOff){
			_foods[index].status |= (1 << 10);
			_foods[index].foodLimitAmount = limitAmount;
			_foods[index].foodLimitRemain = limitRemain;
		}else{
			_foods[index].status &= ~(1 << 10);
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
			var result = new FoodList();
			_foods.forEach(function(e){
				if(e.kitchen.id === value){
					result.push(e);
				}
			});
			return result;
		}else{
			return null;
		}
	}
	
	//查找相应部门的菜品
	_foods.getByDept = function(deptId){
		var value = null;
		if(typeof deptId == 'string'){
			value = parseInt(deptId);
		}else if(typeof deptId == 'number'){
			value = deptId;
		}
		
		if(value != null){
			var result = new FoodList();
			_foods.forEach(function(e){
				if(e.kitchen.dept.id === value){
					result.push(e);
				}
			});
			return result;
		}else{
			return null;
		}
	}
	
	//根据拼音查找菜品
	_foods.getByPinyin = function(pinyin){
		var value = pinyin.trim().toLowerCase();
		var result = new FoodList();
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
		var result = new FoodList();
		_foods.forEach(function(e){
			if(e.name.indexOf(value) != -1){
				result.push(e);
			}
		});
		return result;
	}
	
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
	}
	
	return _foods;
}



