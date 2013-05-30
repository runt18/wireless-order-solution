function stockTaskNavHandler(e){
	var nav = Ext.getCmp('stockTaskNavWin');
	if(typeof nav != 'undefined'){
		var index = e.change + nav.getLayout().activeItem.index;
		if(index < 0){
			alert("这是第一步")
		}else if(index > 2){
			alert("这是最后一步")
		}else{
			nav.getLayout().setActiveItem(index);
		}
		
		stockTaskNavWin.setTitle(nav.getLayout().activeItem.mt);
	}
}