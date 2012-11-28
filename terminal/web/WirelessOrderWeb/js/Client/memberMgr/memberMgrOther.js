checkSelect = function(e){
	if(e == null || typeof e == 'undefined'){
		return;
	}
	
	for(var i = 0; i < mObj.ctSelect.idList.length; i++){
		if(mObj.ctSelect.idList[i] != e.getId())
			Ext.getCmp(mObj.ctSelect.idList[i]).setValue(false);
	}
	
	var btnBindClient = Ext.getCmp('btnBindClient');
	var memberBasicPanel = Ext.getCmp('memberBasicPanel');
	
	
	if(e.getId() == mObj.ctSelect.radioBJM.id){
		// 不记名
		btnBindClient.setVisible(false);
		
	}else if(e.getId() == mObj.ctSelect.radioXJ.id){
		// 新建
		btnBindClient.setVisible(false);
		
		for(var i = 0; i < memberBasicPanel.items.length; i++){
			if(typeof memberBasicPanel.items.get(i).abc != 'undefined')
				alert(memberBasicPanel.items.get(i).abc);
		}
	}else if(e.getId() == mObj.ctSelect.radioBD.id){
		// 绑定
		btnBindClient.setVisible(true);
	}
	
};