/**
 * 
 */
WirelessOrder.TableList = function(source){

	//就餐餐台数量
	var _busyAmount = 0;
	//空闲餐台数量
	var _idleAmount = 0;
	//已暂结餐台数量
	var _tmpPaidAmount = 0;
	
	var _tables;
	if(source){
		_tables = source.slice(0);
		_tables.forEach(function(e){
			if(e.statusValue == WirelessOrder.TableList.Status.BUSY.val){
				_busyAmount++;
			}else if(e.statusValue == WirelessOrder.TableList.Status.IDLE.val){
				_idleAmount++;
			}
			if(e.isTempPaid){
				_tmpPaidAmount++;
			}
			
			//判断是否就餐
			e.isBusy = function(){
				return parseInt(this.statusValue) === WirelessOrder.TableList.Status.BUSY.val;
			};
			
			//判断是否空闲
			e.isIdle = function(){
				return parseInt(this.statusValue) === WirelessOrder.TableList.Status.IDLE.val;
			};
			
			//判断是否一般状态
			e.isNormal = function(){
				return this.categoryValue == WirelessOrder.TableList.Category.NORMAL.val;
			};
			//判断是否搭台
			e.isJoin = function(){
				return this.categoryValue == WirelessOrder.TableList.Category.JOIN.val;
			};
			//判断是否酒席
			e.isFeast = function(){
				return this.categoryValue == WirelessOrder.TableList.Category.JOIN.val;
			};
		});
	}else{
		_tables = [];		
	}
	
	//根据区域查找tables
	_tables.getByRegion = function(regionId){
		var result = new WirelessOrder.TableList();
		for(var i = 0; i < _tables.length; i++){
			if(_tables[i].region.id == regionId){
				result.push(_tables[i]);
			}
		}
		return result;
	};
	
	//根据餐台状态查找tables
	_tables.getByStatus = function(status){
		var result = new WirelessOrder.TableList();
		for(var i = 0; i < _tables.length; i++){
			if(_tables[i].statusValue == status.val){
				result.push(_tables[i]);
			}
		}
		return result;
	};
	
	//根据alias查找table
	_tables.getByAlias = function(alias){
		for(var i = 0; i < _tables.length; i++){
			if(_tables[i].alias == alias){
				return _tables[i];
			}
		}
		return null;
	};
	
	//根据id查找table
	_tables.getById = function(id){
		for(var i = 0; i < _tables.length; i++){
			if(_tables[i].id == id){
				return _tables[i];
			}
		}
		return null;
	};
	
	//模糊查找餐台
	_tables.getByFuzzy = function(fuzzy){
		var result = new WirelessOrder.TableList();
		if(typeof fuzzy == 'number'){
			fuzzy = (fuzzy + '').trim();
		}else if(typeof fuzzy == 'string'){
			fuzzy = fuzzy.trim();
		}
		_tables.forEach(function(e){
			if(e.name.indexOf(fuzzy) != -1){
				result.push(e);
			}else if((e.alias + '').indexOf(fuzzy) != -1){
				result.push(e);
			}
		});

		return result;
	};
	
	_tables.getIdleAmount = function(){
		return _idleAmount;
	};
	
	_tables.getBusyAmount = function(){
		return _busyAmount;
	};
	
	_tables.getTmpPaidAmount = function(){
		return _tmpPaidAmount;
	};
	
	return _tables;
};

WirelessOrder.TableList.Status = {
	IDLE : { val : 0, desc : '空闲'},
	BUSY : { val : 1, desc : '就餐'}
};

WirelessOrder.TableList.Category = {
	NORMAL : { val : 1, desc : '一般'},
	JOIN : { val : 3, desc : '搭台'},
	FEAST : { varl : 5, desc : '酒席费'}
};

