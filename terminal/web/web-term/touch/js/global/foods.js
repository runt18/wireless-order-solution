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

WirelessOrder.TasteList = function(tastelist){
	var _tastes;
	
	if(tastelist){
		_tastes = tastelist;
		
		//按菜品id进行排序
		_tastes.sort(function(obj1, obj2){
			if(obj1.id > obj2.id){
				return 1;
			}else if(obj1.id < obj2.id){
				return -1;
			}else{
				return 0;
			}
		});
		
	}else{
		_tastes = [];
	}
	
	
	_tastes.getById = function(tasteId){
		for(var i = 0; i < _tastes.length; i++){
			if(_tastes[i].id == tasteId){
				return _tastes[i];
			}
		}
	}			
					
	return _tastes;	
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
		
		_food.hasPopTastes = function(){
			return typeof this.popTastes != 'undefined' && this.popTastes.length > 0;
		};
		
		return _food;
	};
})();

WirelessOrder.Taste = function(taste){
	
	if(taste instanceof WirelessOrder.Taste){
		return taste;
	}else{
		var _instance = $.extend(true, {}, taste);
		
		//判断计算方式是否用价钱
		_instance.isCalcByPrice = function(){
			return _instance.calcValue == WirelessOrder.Taste.CalcType.BY_PRICE.val;
		}
		
		//判断taste是否相同
		_instance.isTasteMatch = function(taste){
		
		}
		
		return _instance;
	}
};

WirelessOrder.Taste.CalcType = {
	BY_PRICE : {val : 0, name : "按价格"},
	BY_RATE : {val : 1, name : "按比例"}
}

WirelessOrder.TasteGroup = function(tasteGroup, attchedFood){
	var _instance;
	
	if(tasteGroup){
		_instance = $.extend(true, {}, tasteGroup);
	}else{
		_instance = {};
		_instance.normalTaste = { 
			name : '',
			price : 0			
		};
		
		_instance.normalTasteContent = [];
	}
	
	_instance.addTaste = function(taste){
		//是否有相同的taste
		var duplicated = false;
		_instance.normalTasteContent.forEach(function(eachTaste, index){
			if(eachTaste.id === taste.id){
				duplicated = true;
			}
		});
		
		if(duplicated){
			return false;
		}else{
			_instance.normalTasteContent.push(new WirelessOrder.Taste(taste));
			return true;
		}
	};
	
	_instance.removeTaste = function(taste){
		_instance.normalTasteContent.forEach(function(eachTaste, index){
			if(eachTaste.id === taste.id){
				_instance.normalTasteContent.splice(index, 1);
			}
		});
	}
	
	//保存口味组
	_instance.setTastes = function(tastes){
		_instance.normalTasteContent = [];
		tastes.forEach(function(eachTaste, index){
			_instance.addTaste(eachTaste);
		});
	};
	
	//判断taste是否有普通口味
	_instance.hasNormalTaste = function(){
		return typeof _instance.normalTasteContent !== 'undefined' && _instance.normalTasteContent.length > 0;
	};
	
	
	_instance.getPrice = function(){
		//获取到口味组的价格	
		var tastePrice = 0;
		
		if(_instance.hasNormalTaste()){
			_instance.normalTasteContent.forEach(function(eachTaste, index){
				tastePrice += eachTaste.isCalcByPrice() ? eachTaste.price : (attchedFood.unitPrice * eachTaste.rate);
			});
		}
		
		if(_instance.hasTmpTaste()){
			tastePrice += getTmpTastePrice();
		}
		
		return tastePrice;

	};
	
	_instance.getPref = function(){
		// 获取到拼接后的口味名称  xx, xx
		
		
		if(_instance.normalTasteContent.length > 0){
			var normalTasteName = '';
			_instance.normalTasteContent.forEach(function(eachTaste, index){
				if(normalTasteName == ''){
					normalTasteName += eachTaste.name;
				}else{
					normalTasteName += ',' + eachTaste.name;
				}
			});
			return normalTasteName + (_instance.hasTmpTaste() ? "," + getTmpTastePref() : '');
		}else{
			if(_instance.hasTmpTaste()){
				return getTmpTastePref();
			}else{
				return '';
			}
		}
	};
	
	
	_instance.hasTmpTaste = function(){
		return typeof _instance.tmpTaste !== 'undefined';
	};
	
	function getTmpTastePref(){
		if(_instance.hasTmpTaste()){
			return _instance.tmpTaste.name;
		}else{
			return "";
		}
	}
	
	function getTmpTastePrice(){
		if(_instance.hasTmpTaste()){
			return parseInt(_instance.tmpTaste.price);
		}else{
			return 0;
		}
	}
	
	//设置临时口味
	_instance.setTmpTaste = function(name, price){
		if((typeof name == 'undefined' || name == null || name.length == 0 ) && (typeof price == 'undefined' || price == '' || price == 0)){
			delete _instance.tmpTaste;
		}else{
			_instance.tmpTaste = {
				name : name ? name : '',
				price : price ? price : 0 
			};
		}
	}
	
	
	function isNormalMatch(tasteGroup){
		if(_instance.hasNormalTaste() && tasteGroup.hasNormalTaste()){
			if(_instance.normalTasteContent.length === tasteGroup.normalTasteContent.length){
				//将自身tasteGroup的id放入数组
				var eachTastes = [];
				_instance.normalTasteContent.forEach(function(eachTaste, index){
					eachTastes.push(eachTaste.id);
				});
				
				//将传回来的tasteGroup的id放入数组
				var eachTasteGroupTastes = [];
				tasteGroup.normalTasteContent.forEach(function(eachTaste, index){
					 eachTasteGroupTastes.push(eachTaste.id);
				});
				
				//排序两个数组,按升序
				eachTastes.sort(function(a, b){return a-b;});
				eachTasteGroupTastes.sort(function(a, b){return a-b;});
				
				//比对两个数组是否相等
				for(var i = 0; i > eachTastes.length; i++){
					if(eachTastes[i].id !== eachTasteGroupTastes[i].id){
						return false;
					}
				}
				
				return true;
			}else{
				return false;				
			}
		}else if(!_instance.hasNormalTaste() && !tasteGroup.hasNormalTaste()){
			return true;
		}else{
			return false;
		}
	}
	
	function isTmpMatch(tasteGroup){
		if(_instance.hasTmpTaste() && tasteGroup.hasTmpTaste()){
			return (_instance.tmpTaste.name === tasteGroup.tmpTaste.name) && (_instance.tmpTaste.price === tasteGroup.tmpTaste.price);
		}else if(!_instance.hasTmpTaste() && !tasteGroup.hasTmpTaste()){
			return true;
		}else{
			return false;
		}
	}
	
	//判断口味组是否相同
	_instance.equals = function(tasteGroup){
		return isNormalMatch(tasteGroup) && isTmpMatch(tasteGroup);
	}
	
	
	return _instance;
};


WirelessOrder.ComboOrderFood = function(comboFood, parentFood){
	
	var _instance = $.extend(true, {}, comboFood);
	
	_instance.addTaste = function(taste){
		if(typeof _instance.tasteGroup === 'undefined'){
			_instance.tasteGroup = new WirelessOrder.TasteGroup();
		}
		
		_instance.tasteGroup.addTaste(taste);
		parentFood.addTaste(taste);
	}
	
	
	_instance.removeTaste = function(taste){
		//清空主菜的口味
		if(parentFood){
			if(parentFood.hasTasteGroup()){
				parentFood.removeTaste(taste);
			}
		}
		
		if(typeof _instance.tasteGroup === 'undefined'){
			return false;
		}else{
			_instance.tasteGroup.removeTaste(taste);
			return true;
		}
		
	}
	
	_instance.setFoodUnit = function(unit){
		this.foodUnit = unit;
	}
	
	//是否有单位
	_instance.hasFoodUnit = function(){
		return typeof _instance.foodUnit != 'undefined';
	}
	
	//是否有临时口味
	_instance.hasTempTaste = function(){
		if(typeof _instance.tasteGroup === 'undefined'){
		   _instance.tasteGroup = new WirelessOrder.TasteGroup();
		}
		
		return _instance.tasteGroup.hasTmpTaste();
	}
	
	_instance.hasPopTastes = function(){
		return typeof this.popTastes != 'undefined' && this.popTastes.length > 0;
	}
	
	
	//添加临时口味
	_instance.setTastes = function(tastes){
		if(typeof _instance.tasteGroup === 'undefined'){
		   _instance.tasteGroup = new WirelessOrder.TasteGroup();
		}
		
		_instance.tasteGroup.setTastes(tastes);
		 
		tastes.forEach(function(eachTaste, index){
			parentFood.addTaste(eachTaste);
		});
	}
	
	_instance.hasTasteGroup = function(){
		return typeof _instance.tasteGroup !== 'undefined'; 
	}
	
	_instance.getTastePref = function(){
		if(this.hasTasteGroup()){
			return this.tasteGroup.getPref();
		}else{
			return '';
		}
		
	}
	
	
	_instance.setTempTaste = function(name, price){
		if(typeof _instance.tasteGroup === 'undefined'){
			//创建tasteGroup
			_instance.tasteGroup = new WirelessOrder.TasteGroup(null, _orderFood);
		}
		
		_instance.tasteGroup.setTmpTaste(name, price);
	}
	
	
	return _instance;
};

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
	
	
	_orderFood.addTaste = function(taste){
		if(!_orderFood.isTemporary){
			if(typeof _orderFood.tasteGroup === 'undefined'){
				//创建tasteGroup
				_orderFood.tasteGroup = new WirelessOrder.TasteGroup(null, _orderFood);
			}
			
			//判断taste是否经过wirelessOrder.Taste包装,如果不是,就要重新进行包装
			_orderFood.tasteGroup.addTaste(taste);
		}		
	}
	
	_orderFood.removeTaste = function(taste){
		if(typeof _orderFood.tasteGroup === 'undefined'){
			return false;
		}else{
			//判断taste是否经过wirelessOrder.Taste包装,如果不是,就要重新进行包装
			_orderFood.tasteGroup.removeTaste(taste);
			return true;
		}
		
		
	}
	
	//加入口味组
	_orderFood.setTastes = function(tastes){
		if(typeof _orderFood.tasteGroup === 'undefined'){
			//创建tasteGroup
			_orderFood.tasteGroup = new WirelessOrder.TasteGroup(null, _orderFood);
		}
		
		_orderFood.tasteGroup.setTastes(tastes);
	}
	
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
	
	//是否有临时口味
	_orderFood.hasTempTaste = function(){
		if(typeof _orderFood.tasteGroup === 'undefined'){
		   _orderFood.tasteGroup = new WirelessOrder.TasteGroup();
		}
		
		return _orderFood.tasteGroup.hasTmpTaste();
	}
	
	//添加临时口味
	_orderFood.setTempTaste = function(name, price){
		if(typeof _orderFood.tasteGroup === 'undefined'){
			//创建tasteGroup
			_orderFood.tasteGroup = new WirelessOrder.TasteGroup(null, _orderFood);
		}
		
		_orderFood.tasteGroup.setTmpTaste(name, price);
	}
	
	
	_orderFood.getPrice = function(){
		if(_orderFood.isWeight()){
			return _orderFood.unitPrice * _orderFood.count + (_orderFood.hasTasteGroup() ? _orderFood.tasteGroup.getPrice() : 0);
		}else{
			
		 	return _orderFood.count * (_orderFood.unitPrice + (_orderFood.hasTasteGroup() ? _orderFood.tasteGroup.getPrice() : 0));
		}
	};
	
	_orderFood.hasTasteGroup = function(){
		return typeof _orderFood.tasteGroup !== 'undefined';
	}
	
	_orderFood.getTasteGroup = function(){
		if(_orderFood.hasTasteGroup()){
			return _orderFood.tasteGroup;
		}else{
			return null;
		}
	}
	
	function isUnitMatch(food){
		if(_orderFood.isUnit() && food.isUnit()){
			return _orderFood.foodUnit.id === food.foodUnit.id;
		}else if(!_orderFood.isUnit() && !food.isUnit()){
			return true;
		}else{
			return false;
		}
	}
	
	function isTgMatch(food){
		if(_orderFood.hasTasteGroup() && food.hasTasteGroup()){
			return _orderFood.tasteGroup.equals(food.tasteGroup);
		}else if(!_orderFood.hasTasteGroup() && !food.hasTasteGroup()){
			return true;
		}else{
			return false;
		}
	}
	
	_orderFood.equals = function(food){
		if(_orderFood.isGifted() !== food.isGifted()){
			return false;
		}else if(_orderFood.isTemp() !== food.isTemp()){
			return false;
		}else if(_orderFood.isTemp() && food.isTemp()){
			return _orderFood.name === food.name && _orderFood.price === food.price;
		}else if(!_orderFood.isTemp() && !food.isTemp()){ 
			return _orderFood.id === food.id &&	isTgMatch(food) && isUnitMatch(food);
		}else{
			return true;
		}
	}
	
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
			_orderFood.setFoodUnit(_orderFood.getFoodUnit()[0]);
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
			if(_orderFoods[i].equals(foodData)){
				index = i;
				break;	
			}
		}
		
		if(index >= 0){
			_orderFoods[index].addCount(1);
			//将改变的菜品置为选中状态
			_orderFoods.select(index);
			//重新赋值唯一标示
			foodData.unique = _orderFoods[index].unique;
			
		}else{

			//生成唯一标示
			foodData.unique = new Date().getTime();
			
			var multiPrice = WirelessOrder.foods.getById(foodData.id).multiUnitPrice;
			if(!foodData.hasFoodUnit() && multiPrice.length > 0){
				foodData.setFoodUnit(multiPrice[0]);	
			}
			
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
								
								foodData.combo.push(new WirelessOrder.ComboOrderFood({
									comboFood : rt.root[j]
								}, foodData));
								
							}
							
							foodData.combo.forEach(function(e){
								var multiPrice = WirelessOrder.foods.getById(e.comboFood.id).multiUnitPrice;
								if(!e.hasFoodUnit() && multiPrice.length > 0){
									e.setFoodUnit(multiPrice[0]);	
								}
							});
							
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
		_orderFoods.push(new WirelessOrder.OrderFood({
			unique : new Date().getTime(),
			id : (new Date().getTime() + '').substring(5, 9),
			alias : (new Date().getTime() + '').substring(5, 9),
			name : c.name,
			count : c.count,
			unitPrice : c.price,
			kitchen : {
				id : c.kitchen
			}
		}, true));
		_orderFoods.select(-1);
	};
	
	_orderFoods.addAllTaste = function(taste){
		for(var i = 0; i < _orderFoods.length; i++){
			_orderFoods[i].addTaste(taste);	
		}
	}
	
	_orderFoods.removeAllTaste = function(taste){
		for(var i = 0; i < _orderFoods.length; i++){
			_orderFoods[i].removeTaste(taste);	
		}
	}
	
	
	return _orderFoods;
};
