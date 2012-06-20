

var businessSumWin = null;

businessSumPanelnit = function(){

	
};

businessSum = function(){
	
	if(!businessSumWin){
		businessSumWin = new Ext.Window({
			title : '营业汇总',
//			layout : 'border',
//			closeAction : 'hide',
			resizable : false,
			modal:true,
			closable:false,
			constrainHeader:true,
			draggable:false,
			width : 900,
			height : 500,
//			items : [],
			buttons : [
			{
				text : '打印',
				handler : function(){
					
				}
			},
			{
				text : '退出',
				handler : function(){
					businessSumWin.hide();
				}
			}
			]
		});
	}
	businessSumWin.show();
};