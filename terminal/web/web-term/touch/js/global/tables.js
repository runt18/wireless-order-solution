/**
 * 
 */
function TableList(source){

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
			if(e.statusValue == TableList.Status.BUSY.val){
				_busyAmount++;
			}else if(e.statusValue == TableList.Status.IDLE.val){
				_idleAmount++;
			}
			if(e.isTempPaid){
				_tmpPaidAmount++;
			}
		});
	}else{
		_tables = [];		
	}
	
	_tables.isBusy = function(index){
		return this[index].statusValue == TableList.Status.BUSY.val;
	};
	
	_tables.isIdle = function(index){
		return this[index].statusVale == TableList.Status.IDLE.val;
	};
	
	_tables.getByRegion = function(regionId){
		var result = new TableList();
		for(var i = 0; i < _tables.length; i++){
			if(_tables[i].region.id == regionId){
				result.push(_tables[i]);
			}
		}
		return result;
	};
	
	_tables.getByStatus = function(status){
		var result = new TableList();
		for(var i = 0; i < _tables.length; i++){
			if(_tables[i].statusValue == status.val){
				result.push(_tables[i]);
			}
		}
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
}

TableList.Status = {
	IDLE : { val : 0, desc : '空闲'},
	BUSY : { val : 1, desc : '就餐'}
};


