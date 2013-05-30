function stockTaskNavHandler(e){
	var nav = Ext.getCmp('stockTaskNavWin');
	if(typeof nav != 'undefined'){
		var btnPrevious = Ext.getCmp('btnPreviousForStockNav');
		var btnNext = Ext.getCmp('btnNextForStockNav');
		var act = nav.getLayout().activeItem;
		var index = e.change + act.index;
		if(index < 0){
			alert("这是第一步")
		}else if(index > 1){
			alert("这是最后一步")
		}else{
			nav.getLayout().setActiveItem(index);
		}
		
		stockTaskNavWin.setTitle(nav.getLayout().activeItem.mt);
		act = nav.getLayout().activeItem;
		
		if(act.index >= 1){
			btnNext.setText('完成');
		}else{
			btnNext.setText('下一步');
		}
		if(act.index <= 0){
			btnPrevious.setDisabled(true);
		}else{
			btnPrevious.setDisabled(false);
		}
	}else{
		Ext.Msg.show({
			title : '错误',
			msg : '操作失败, 系统参数错误, 请刷新页面后重试.'
		});
	}
}