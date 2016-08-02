$(function(){
//	var template = {
//		order : 
//	
//	};
	
	new CreateTabPanel([{
		tab : '订单列表',
		onActive : function(){
			console.log(0 + '------active');
		},
		onDeactive : function(){
			console.log(0 + '------Deactive');
		}
	}, {
		tab : '预订列表',
		onActive : function(){
			console.log(1 + '------active');	
		},
		onDeactive : function(){
			console.log(1 + '------Deactive');
		}
	}]).open();
});