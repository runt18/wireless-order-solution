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
		
		_foods.forEach(function(e, index){
			e = new WirelessOrder.FoodWrapper(e);
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
			}else if (currentElement.id > searchElement.id) {
				maxIndex = currentIndex - 1;
			}else {
				return currentIndex;
			}
		}
	
		return ~maxIndex;
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

(function(){
	
	var SPECIAL_FLAG = 1 << 0;		/* 特价 */
	var RECOMMEND_FLAG = 1 << 1;	/* 推荐 */ 
	var SELL_OUT_FLAG = 1 << 2;		/* 售完 */
	var GIFT_FLAG = 1 << 3;			/* 赠送 */
	var CUR_PRICE_FLAG = 1 << 4;	/* 时价 */
	var COMBO_FLAG = 1 << 5;		/* 套菜 */
	var HOT_FLAG = 1 << 6;			/* 热销 */
	var WEIGHT_FLAG = 1 << 7;		/* 称重 */
	//var COMMISION_FLAG = 1 << 8;	/* 提成 */
	//var TEMP_FLAG = 1 << 9;		/* 临时 */
	var LIMIT_FLAG = 1 << 10;		/* 限量估清 */
	
	WirelessOrder.FoodWrapper = function(food){
		var _food = food;
		//是否热销
		_food.isHot = function(){
			return (this.status & HOT_FLAG) != 0;
		};
		//是否推荐
		_food.isRecommend = function(){
			return (this.status & RECOMMEND_FLAG) != 0;
		};	
		//是否特价
		_food.isSpecial = function(){
			return (this.status & SPECIAL_FLAG) != 0;
		};
		
		//是否赠送
		_food.isAllowGift = function(){
			return (this.status & GIFT_FLAG) != 0;
		};
		
		//是否套菜
		_food.isCombo = function(){
			return (this.status & COMBO_FLAG) != 0;
		};
		
		//是否限量估清
		_food.isLimit = function(){
			return (this.status & LIMIT_FLAG) != 0;
		};
		
		//设置限量估清
		_food.setLimit = function(onOff, limitAmount, limitRemain){
			if(onOff){
				this.status |= LIMIT_FLAG;
				this.foodLimitAmount = limitAmount;
				this.foodLimitRemain = limitRemain;
			}else{
				this.status &= ~LIMIT_FLAG;
				this.foodLimitAmount = 0;
				this.foodLimitRemain = 0;
			}
		};
		
		//是否称重
		_food.isWeight = function(){
			return (this.status & WEIGHT_FLAG) != 0;
		};
		
		//是否停售
		_food.isSellout = function(){
			return (this.status & SELL_OUT_FLAG) != 0;
		};
		
		//设置是否停售
		_food.setSellout = function(onOff){
			if(onOff){
				this.status |= SELL_OUT_FLAG;
			}else{
				this.status &= ~SELL_OUT_FLAG;
			}
		};
		
		//是否时价
		_food.isCurPrice = function(){
			return (this.status & CUR_PRICE_FLAG) != 0;
		};
		
		//是否多单位
		_food.hasFoodUnit = function(){
			return typeof this.multiUnitPrice[0] !== 'undefined';
		};
		
		//获取菜品单位
		_food.getFoodUnit = function(){
			return this.multiUnitPrice;
		};
		
		return _food;
	};
})();


WirelessOrder.OrderFood = function(food, isTemporary){
	
	var _orderFood;
	
	//深拷贝food对象
	_orderFood = new WirelessOrder.FoodWrapper($.extend(true, {}, food));
	
	//设置菜品单位
	_orderFood.setFoodUnit = function(unit){
		this.foodUnit = unit;
		this.unitPrice = unit.price;
	};
	
	//是否已设置多单位
	_orderFood.isUnit = function(){
		return typeof this.foodUnit !== 'undefined';
	};
	
	//减少菜品数量
	_orderFood.removeCount = function(count){
		this.count -= count;
		this.count = this.count >= 0 ? this.count : 0; 
	};
	
	//增加菜品数量
	_orderFood.addCount = function(count){
		this.count += count;
	};
	
	//设置菜品数量
	_orderFood.setCount = function(count){
		if(count >= 0){
			this.count = count;
		}
	};
	
	//设置是否叫起
	_orderFood.setHangup = function(isHang){
		if(typeof isHang === 'boolean'){
			this.isHangup = isHang;
		}
	};
	
	//判断菜品是否叫起
	_orderFood.isHang = function(){
		return typeof this.isHangup === 'boolean' && this.isHangup;
	};
	
	//判断是否已赠送
	_orderFood.isGifted = function(){
		return typeof this.isGift === 'boolean' && this.isGift;
	};
	
	//设置是否赠送
	_orderFood.setGift = function(onOff, loginStaff){
		if(typeof onOff === 'boolean'){
			if(!this.isAllowGift()){
				throw '【' + this.name + '】不允许赠送';
			}
			if(!loginStaff.hasPrivilege(WirelessOrder.Staff.Privilege.GIFT)){
				throw '您没有赠送权限';
			}
			this.isGift = onOff;
		}
	};
	
	//判断菜品是否临时菜
	_orderFood.isTemp = function(){
		return typeof this.isTemporary === 'boolean' && this.isTemporary;
	};
	
	//-------------初始化------------------------

	//初始化菜品数量
	if(typeof _orderFood.count === 'undefined'){
		_orderFood.setCount(1);
	}
	//初始化不叫起
	if(typeof _orderFood.isHangup === 'undefined'){
		_orderFood.setHangup(false);
	}
	//初始化是否临时菜
	if(isTemporary){
		_orderFood.isTemporary = true;
	}else{
		_orderFood.isTemporary = false;
		//初始化是否时价
		if(typeof _orderFood.isCurrPrice === 'undefined' && _orderFood.isCurPrice()){
			_orderFood.isCurrPrice = true;
		}
		if(typeof _orderFood.foodUnit === 'undefined' && _orderFood.hasFoodUnit()){
			//菜品有多单位，并且未设置单位，则初始化菜品是首个单位价钱
			_orderFood.setFoodUnit(food.getFoodUnit()[0]);
		}
	}

	return _orderFood;

};

WirelessOrder.Order = function(){
	
	var _orderFoods = [];
	
	//选中某个菜品
	_orderFoods.select = function(index){
		this.forEach(function(e){
			e.selected = false;
		});
		if(index >= 0 && index < this.length){
			this[index].selected = true;
		}else if(index < 0){
			this[this.length - 1].selected = true;
		}
	};
	
	//获取选中的菜品 
	_orderFoods.getSelected = function(){
		for(var i = 0; i < this.length; i++){
			if(this[i].selected){
				return this[i];
			}
		}
		return null;
	};
	
	//清空所有菜品
	_orderFoods.clear = function(){
		this.length = 0;
		_wxCode = null;
	};
	
	//判断是否有菜品
	_orderFoods.isEmpty = function(){
		return this.length == 0;
	}

	//增加菜品
	_orderFoods.add = function(food){
		
		var	foodData = new WirelessOrder.OrderFood(food);

		var index = -1;
		for(var i = 0; i < _orderFoods.length; i++){
			//对比是否同一个菜
			if(_orderFoods[i].id == foodData.id){
				//再对比口味 & 赠送属性 & 单位 & 时价
				if(_orderFoods[i].tasteGroup.normalTasteContent.length == 0 && !_orderFoods[i].isGift && !_orderFoods[i].foodUnit && !_orderFoods[i].isAllowGift()){
					index = i;
					break;				
				}
			}
		}
		
		if(index >= 0){
			_orderFoods[index].addCount(1);
			//将改变的菜品置为选中状态
			_orderFoods.select(index);
			//重新赋值唯一标示
			foodData.unique = _orderFoods[index].unique;
			
		}else{

			foodData.tasteGroup = {
				tastePref : '无口味',
				price : 0,
				normalTasteContent : []
			};
			//生成唯一标示
			foodData.unique = new Date().getTime();
			
			//是否为套菜
			foodData.combo = [];
			if(foodData.isCombo()){
				//获取对应套菜
				$.ajax({
					url : '../QueryFoodCombination.do',
					type : 'post',
					async : false,
					dataType : 'json',
					data : {
						foodID : foodData.id
					},
					success : function(rt, status, xhr){
						if(rt.success && rt.root.length > 0){
							//组合子菜给套菜
							for (var j = 0; j < rt.root.length; j++) {
								
								foodData.combo.push({
									comboFood : rt.root[j],
									tasteGroup : {
										normalTasteContent : [],
										tastePref : ''
									}
								});
							}
						}
					},
					error : function(request, status, err){
						alert(request.msg);
					}
				}); 
			}		
			
			_orderFoods.push(foodData);
			//将改变的菜品置为选中状态
			_orderFoods.select(-1);
			
		}
	};
	
	//增加OrderFood菜品
	_orderFoods.addOrderFood = function(orderFood){
		this.push(new WirelessOrder.OrderFood(orderFood));
		this.select(-1);
	};
	
	//增加临时菜
	_orderFoods.addTemp = function(c){
		c = c || {
			name : '',		//名称
			price : 0,		//价钱
			count : 0,		//数量
			kitchen : 0		//分厨
		}
		this.push(new WirelessOrder.OrderFood({
			unique : new Date().getTime(),
			id : (new Date().getTime() + '').substring(5, 9),
			alias : (new Date().getTime() + '').substring(5, 9),
			name : c.name,
			count : c.count,
			unitPrice : c.price,
			kitchen : {
				id : c.kitchen
			},
			tasteGroup : {
				tastePref : '无口味',
				price : 0,
				normalTasteContent : []
			}
		}, true));
		this.select(-1);
	};
	
	return _orderFoods;
};
