ss.extra = '';
ss.iteratorData = [];


ss.init = function(){
	if(!this.initFlag===true){
		this.initFlag = true;
		
		ss.stoptp = new Util.padding({
			renderTo : 'divAllFoodForStopSet',
			displayId : 'divDescForStopSet-padding-msg',
			templet : function(c){
				return Templet.ss.boxFood.format({
					dataIndex : c.dataIndex,
					id : c.data.id,
					name : c.data.name,
					unitPrice : c.data.unitPrice,
					click : 'ss.insertFood({foodId:'+c.data.id+', type:\'deSellOut\'})'
				});
			}
		});
		ss.normaltp = new Util.padding({
			renderTo : 'divAllFoodForStopSet',
			displayId : 'divDescForStopSet-padding-msg',
//			data : foodData.root.slice(0),
			templet : function(c){
				return Templet.ss.boxFood.format({
					dataIndex : c.dataIndex,
					id : c.data.id,
					name : c.data.name,
					unitPrice : c.data.unitPrice,
					click : 'ss.insertFood({foodId:'+c.data.id+', type:\'sellOut\'})'
				});
			}
		});
		ss.tp = ss.normaltp;
	}
};
/**
 * 更新沽清菜列表
 * @param {} c
 */
ss.updateData = function(c){
	Util.LM.show();
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'stop'
		},
		success : function(data, status, xhr) {
			ss.stoptp.init({
				data : data.root
			});
			allStopFood = data.root;
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				msg : err
			});
		}
	});
	
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'unStop'
		},
		success : function(data, status, xhr) {
			Util.LM.hide();
			ss.normaltp.init({
				data : data.root
			});
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				msg : err
			});
		}
	});
	
};


ss.initDeptContent = function(){
	var deptView = $('#divSelectDeptForStopSet');
	var html = [];
	for(var i = 0; i < deptData.root.length; i++){
		html.push(Templet.ss.dept.format({
			value : deptData.root[i].id,
			text : deptData.root[i].name
		}));
	}
	deptView.html(html.join(''));
	deptView.find('div[data-value=-1]')[0].onclick({deptId:-1});
};


ss.showFoodByCond = function(c){
	if(ss.extra == ''){
		return;
	}

	var showFoodDatas = [];
	for (var i = 0; i < ss.iteratorData.length; i++) {
		var tempFoodData = ss.iteratorData[i];
		if(eval(ss.extra)){
			showFoodDatas.push(tempFoodData);
		}
	}
	ss.tp.init({
		data : showFoodDatas
	});
	ss.tp.getFirstPage();
	
};


ss.initKitchenContent = function(c){
	var kitchenView = $('#divSelectKitchenForStopSet');
	var deptViewList = $('#divSelectDeptForStopSet > div');
	deptViewList.removeClass('div-deptOrKitchen-select');
	for(var i = 0; i < deptViewList.length; i++){
		if(eval(deptViewList[i].getAttribute('data-value') == c.deptId)){
			$(deptViewList[i]).addClass('div-deptOrKitchen-select');
		}
	}
	
	var html = [];
	var tempFoodData = []; // 菜品数据
	var temp = null;
	if(c.deptId == -1){
		for(var i = 0; i < kitchenAllFoodData.root.length; i++){
			temp = kitchenAllFoodData.root[i];
			
			html.push(Templet.ss.kitchen.format({
				value : kitchenAllFoodData.root[i].id,
				text : kitchenAllFoodData.root[i].name
			}));
			tempFoodData = tempFoodData.concat(temp.id != -1 ? temp.foods : []);
		}
	}else{
		html.push(Templet.ss.kitchen.format({
			value : -1,
			text : '全部'
		}));
		for(var i = 0; i < kitchenAllFoodData.root.length; i++){
			if(kitchenAllFoodData.root[i].dept.id == c.deptId){
				temp = kitchenAllFoodData.root[i];
				html.push(Templet.ss.kitchen.format({
					value : kitchenAllFoodData.root[i].id,
					text : kitchenAllFoodData.root[i].name
				}));
				tempFoodData = tempFoodData.concat(temp.foods);
			}
		}
	}
	kitchenView.html(html.join(''));
	
	temp = null;
	
	
/*	
	dExtra = '';
	kExtra = '';
	if(c.deptId != null && c.deptId != 'undefined' && c.deptId != -1){
		dExtra += 'tempFoodData.kitchen.dept.id == ' + c.deptId;
	}*/
	ss.iteratorData = tempFoodData;
	ss.showFoodByCond();
	
};




ss.findFoodByKitchen = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('#divSelectKitchenForStopSet > div[data-type=kitchen-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-deptOrKitchen-select');
	}
	$(c.event).addClass('div-deptOrKitchen-select');
	
	var tempFoodData = [];
	
	var temp = null;
	if(c.kitchenId == -1){
		var dl = $('.div-deptOrKitchen-select[data-type=dept-select]');
		if(dl.length == 0){
			for(var i = 0; i < kitchenAllFoodData.root.length; i++){
				tempFoodData = tempFoodData.concat(kitchenAllFoodData.root[i].foods);
			}
		}else{
			for(var i = 0; i < kitchenAllFoodData.root.length; i++){
				temp = kitchenAllFoodData.root[i];
				if(temp.dept.id == parseInt(dl[0].getAttribute('data-value'))){
					tempFoodData = tempFoodData.concat(temp.foods);		
				}
			}
		}
	}else{
		for(var i = 0; i < kitchenAllFoodData.root.length; i++){
			temp = kitchenAllFoodData.root[i];
			if(typeof c.kitchenId == 'number' && c.kitchenId != -1){
				if(temp.id == c.kitchenId){
					tempFoodData = tempFoodData.concat(temp.foods);
				}
			}else{
				tempFoodData.concat();
			}
		}
	}
	temp = null;
	
/*	kExtra = '';
	if(c.kitchenId != null && c.kitchenId != 'undefined' && c.kitchenId != -1){
		kExtra += 'tempFoodData.kitchen.id == ' + c.kitchenId;
	}*/
	ss.iteratorData = tempFoodData;
	ss.showFoodByCond();
	
};

ss.entry = function(){
	Util.sellOutCond = true;
	Util.toggleContentDisplay({
		el : 'divStopSet',
		type : 'show'
	});
	
	$('#divNewFoodForStopSet').css('height', $('#divCenterForCreateOrde').height());
	
	ss.init();
	
	ss.updateData();
	
	ss.initDeptContent();
	
	
//	$('#divNorthForStopSet > div[data-type=stop]').first();
	setTimeout("ss.searchData({event:$('#divBtnSellFood'), isStop:false})", 400);
};
ss.back = function(){
	Util.sellOutCond = false;
	Util.toggleContentDisplay({
		el : 'divStopSet',
		type : 'hide'
	});
	
	ss.newFood = [];
	ss.cancelSellOutFood = [];
	ss.callback = null;
	ss.initNewFoodContent();
	var sells = $('div[data-type=sellOut]');
	for(var i = 0; i < sells.length; i++){
		$(sells[i]).removeClass('div-sellOut-select');
	}
	sells = null;
	updateFoodData();
	ss.extra = '';
};


ss.searchData = function(c){
	ss.newFood = [];
	ss.cancelSellOutFood = [];
	ss.initNewFoodContent();
	ss.extra = '';
	if(c.isStop === true){
		ss.tp = ss.stoptp;
	}else{
		ss.tp = ss.normaltp;
	}
	
	var sells = $('div[data-type=sellOut]');
	for(var i = 0; i < sells.length; i++){
		$(sells[i]).removeClass('div-sellOut-select');
	}
	$(c.event).addClass('div-sellOut-select');
	
	if(c.isStop != null && c.isStop === true){
		ss.extra += '(tempFoodData.status & 1 << 2) != 0';
	}else{
		ss.extra += '(tempFoodData.status & 1 << 2) == 0';
	}
	ss.showFoodByCond();
	
};

