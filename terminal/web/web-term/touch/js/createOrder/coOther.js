/**
 * 搜索包
 */
co.s = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = getDom(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){
				co.s.fileValue = co.s.file.value;
				var data = null, temp = null;
				if(co.s.fileValue.trim().length > 0){
					data = [];
					temp = foodData.root.slice(0);
					for(var i = 0; i < temp.length; i++){
						if(temp[i].name.indexOf(co.s.fileValue.trim()) != -1){
							data.push(temp[i]);
						}
					}				
				}
				co.s.sp.init({
					data : data,
					callback : function(){
						co.s.sp.getFirstPage();
					}
				});
				data = null;
				temp = null;
			};
		}
		if(!co.s.sp){
			co.s.sp = new Util.padding({
				renderTo : 'divCFCOAllFood',
				displayId : 'divDescForCreateOrde-padding-msg',
				templet : function(c){
					return Templet.co.boxFood.format({
						dataIndex : c.dataIndex,
						id : c.data.id,
						name : c.data.name,
						unitPrice : c.data.unitPrice,
						click : 'co.insertFood({foodId:'+c.data.id+', callback:co.s.callback})',
						foodState : (c.data.status & 1 << 3) != 0 ? '赠' : ((c.data.status & 1 << 2) != 0 ? '停' : ''),
						color : (c.data.status & 1 << 3) != 0 ? 'green' : 'FireBrick'
					});
				}
			});
		}
		return this.file;
	},
	valueBack : function(){
		this.file.value = this.file.value.substring(0, this.file.value.length - 1);
		this.file.oninput(this.file);
		this.file.focus();
	},
	select : function(){
		this.file.select();
	},
	clear : function(){
		this.file.value = '';
		this.file.oninput(this.file);
		this.file.select();
	},
	callback : function(){
		co.s.clear();
	}
};

/**
 * 
 */
co.s.entry = function(){
	if(!co.s.init({file:'txtSearchFileForCreateOrder'})){
		Util.msg.alert({
			title : '错误',
			msg : '程序异常, 搜索功能无法使用, 请刷新页面后重试.',
			time : 3
		});
		return;
	}
	
	Util.toggleToolbarDisplay({
		el : 'divSearchToolbarForCreateOrder',
		type : 'show'
	});
	//
	co.s.clear();
};

/**
 * 
 */
co.s.end = function(){
	co.s.file = null;
	Util.toggleToolbarDisplay({
		el : 'divSearchToolbarForCreateOrder',
		type : 'hide'
	});
	var select = false, all = $('#divSelectKitchenForOrder > div[data-type=kitchen-select]');
	for(var i = 0; i < all.length; i++){
		if($(all[i]).hasClass('div-deptOrKitchen-select')){
			select = true;
			all[i].onclick();
			break;
		}
	}
	if(!select){
		select = $('#divSelectKitchenForOrder > div[data-value=-1]')[0];
		select.onclick();
	}
	select = null;
};

/**
 * 临时菜
 */
co.tf = {
	dom : null,
	kd : [],
	kc : null,
	nameDom : null,
	priceDom : null,
	countDom : null,
	kitchenDom : null,
	keyDom : null,
	setKeyDom : function(e){
		this.keyDom = getDom(e.id);
	},
	clearDom : function(){
		this.nameDom.value = '';
		this.priceDom.value = '';
		this.countDom.value = 1;
		
		this.nameDom = null;
		this.priceDom = null;
		this.countDom = null;
		
		var sl = $('#divKitchenSelectForCOTF > div[class*=div-tempFood-select]');
		for(var i = 0; i < sl.length; i++){
			$(sl.length[i]).removeClass('div-tempFood-select');
		}
		this.kitchenDom = null;
		
		this.keyDom = null;
	},
	ininKitchen : function(){
		if(!this.kc){
			this.kc = getDom('divKitchenSelectForCOTF');
		}
		var html = [];
		for(var i = 0; i < this.kd.length; i++){
			html.push('<div data-value="'+this.kd[i].id+'" class="main-box-base" onClick="co.tf.selectKitchen({event:this, kitchenId:'+this.kd[i].id+'})">'+this.kd[i].name+'</div>');
		}
		this.kc.innerHTML = html.join('');
	},
	init : function(c){
		this.kd = c.kd;
		this.ininKitchen();
		
		this.nameDom = getDom('txtNameForCOTF');
		this.priceDom = getDom('txtPriceForCOTF');
		this.countDom = getDom('txtCountForCOTF');
		var sl = $('#divKitchenSelectForCOTF > div[class*="div-tempFood-select"]');
		if(sl.length == 1){
			this.kitchenDom = sl[0];
		}
		
		this.nameDom.focus();
	}
};
/**
 * 
 */
co.tf.keyBoy = function(c){
	if(this.keyDom){
		if(c.type === 1){
			this.keyDom.value = this.keyDom.value.substring(0, this.keyDom.value.length - 1);
		}else if(c.type === 2){
			this.keyDom.value = '';
		}else{
			this.keyDom.value = this.keyDom.value + '' + c.value;
		}
		this.keyDom.focus();
	}
};
/**
 * 
 */
co.tf.selectKitchen = function(c){
	var sl = $('#divKitchenSelectForCOTF > div[class*=div-tempFood-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-tempFood-select');
	}
	$(c.event).addClass('div-tempFood-select');
	this.kitchenDom = c.event;
};


/**
 * 
 */
co.tf.entry = function(){
	Util.dialongDisplay({
		renderTo : 'divTempFoodForCreateOrder',
		type : 'show'
	});
	this.init({
		kd : allowTempKitchen.root.concat()
	});
};
/**
 * 
 */
co.tf.save = function(){
	var err = '';
	if(this.nameDom.value.trim().length == 0){
		err += '<font color="red">菜名</font>不能为空.';
	}
	if(this.priceDom.value.trim().length == 0 || isNaN(this.priceDom.value.trim())){
		err += '<br>菜品<font color="red">价钱</font>不能空且为数值类型.';
	}
	if(this.countDom.value.trim().length == 0 || isNaN(this.countDom.value.trim())){
		err += '<br>菜品<font color="red">数量</font>不能空且为数值类型.';
	}
	if(!this.kitchenDom){
		err += '<br>打印<font color="red">分厨</font>不能为空.';
	}
	
	if(err == ''){
		co.initNewFoodContent({
			record : {
				isTemporary : true,
				id : (new Date().getTime()+'').substring(5, 8),
				alias : (new Date().getTime()+'').substring(5, 8),
				name : this.nameDom.value.trim(),
				count : parseFloat(this.countDom.value.trim()),
				unitPrice : parseFloat(this.priceDom.value.trim()),
				isHangup : false,
				kitchen : {
					id : this.kitchenDom.getAttribute('data-value')
				},
				tasteGroup : {
					tastePref : '无口味',
					price : 0,
					normalTasteContent : []
				}
			}
		});
		//
		co.tf.back();
	}else{
		Util.msg.alert({
			time : 3,
			msg : err
		});
	}
};
/**
 * 
 */
co.tf.back = function(){
	Util.dialongDisplay({
		renderTo : 'divTempFoodForCreateOrder',
		type : 'hide'
	});
	this.clearDom();
};


//弹出助记码
co.s.foodAlias = function(){
	Util.dialongDisplay({
		renderTo : 'divFoodAlias',
		type : 'show'
	});
	$("#txtFoodAlias").focus();
};


co.s.findByAlias = function(c){
	var alias = getDom('txtFoodAlias');
	var data = null, temp = null;
	temp = foodData.root.slice(0);
	for(var i = 0; i < temp.length; i++){
		if(eval(temp[i].alias == alias.value)){
			data = temp[i];
		}
	}
	if(data == null){
		Util.msg.alert({
			title : '提示',
			msg : '此编码无对应菜品'
		});
		
	}else{
		co.insertFood({foodId : data.id});
	}
	alias.value = "";
	data = null;
	temp = null;
};
co.fa = {};
co.fa.back = function(){
	Util.dialongDisplay({
		renderTo : 'divFoodAlias',
		type : 'hide'
	});
	getDom('txtFoodAlias').value = '';
};

