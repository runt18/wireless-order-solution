checkSelect = function(e){
	if(e == null || typeof e == 'undefined'){
		return;
	}
	
	for(var i = 0; i < mObj.ctSelect.idList.length; i++){
		if(mObj.ctSelect.idList[i] != e.getId())
			Ext.getCmp(mObj.ctSelect.idList[i]).setValue(false);
	}
	
	var memberBasicPanel = Ext.getCmp('memberBasicPanel');
	
	if(e.getId() == mObj.ctSelect.radioBJM.id){
		// 不记名
		
	}else if(e.getId() == mObj.ctSelect.radioXJ.id){
		// 新建
//		for(var i = 0; i < memberBasicPanel.items.length; i++){
		for(var i = 0; i < memberBasicPanel.items.length; i++){
			if(typeof memberBasicPanel.items.get(i).abc != 'undefined')
				alert(memberBasicPanel.items.get(i).abc)
//			var item = memberBasicPanel.items.get(i).items;
//			if(item.length > 0)
//				alert(item.get(0).getId());
		}
		var aaa;
//		for(var k in memberBasicPanel.items){
//			aaa += (k+'  ')
//		}
//		alert(aaa)
	}else if(e.getId() == mObj.ctSelect.radioBD.id){
		// 绑定
		
	}
	
};