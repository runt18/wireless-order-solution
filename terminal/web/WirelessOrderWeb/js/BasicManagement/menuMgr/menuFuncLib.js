setFiledDisabled = function(start, idList){
	var st = true;
	st = typeof(start) == 'boolean' ? start : st;
	for(var i = 0; i < idList.length; i++){
		var tp = Ext.getCmp(idList[i]);
		if(tp){
			tp.setDisabled(st);
		}
	}
};





