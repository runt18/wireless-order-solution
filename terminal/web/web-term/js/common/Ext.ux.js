/*************************************
 * 
 * 
 * 
 * 
 * 
 * 
 */
Ext.ux.errorCode = {
	ORDER_EXPIRED : 9195 //账单已过期
};

/*************************************
 * 部分通用显示格式
 */
Ext.ux.txtFormat = {
	barMsg : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{0}:&nbsp;<span id="{1}" style="color:green; font-size:15px; font-weight:bold;">{2}</span>',
	barTitle : '<span style="font-weight:bold; font-size:13px;">{0}</span>',
	typeName : '<div style="float:left; width:100px; font-size:12px;">&nbsp;{0}:&nbsp;<span id="{1}" style="color:green; font-weight:bold; font-size:13px;">{2}</span></div>',
	longerTypeName : '<div style="float:left; width:150px; font-size:12px;">&nbsp;{0}:&nbsp;<span id="{1}" style="color:green; font-weight:bold; font-size:13px;">{2}</span></div>',
	attrName : '<div style="float:left; width:300px; font-size:12px;">&nbsp;{0}:&nbsp;<span id="{1}" style="color:green; font-weight:bold; font-size:13px;">{2}</span></div>',
	tbarName : '<div style="float:left; width:60px; font-size:12px;"><span id="{0}" style="color:red; font-weight:bold; font-size:16px;">{1}</span><span id="{2}" style="color:green; font-weight:bold; font-size:13px;">{3}</span></div>',
	linkClassOne : '<a id="{0}" href="{1}" onClick="{2}" style="color:green; font-size:15px; font-weight:bold;">{3}</a>',
	renderer : '<a href="javascript:{0}({1})" {2}>{3}</a>',
	xh : '<font color="red">＊</font>',
	deleteSuccess : '<font color="red">{0}</font>&nbsp;删除成功',
	operateSuccess : '<font color="red">{0}</font>&nbsp;{1}',
	gridDou : function(_v){
		return _v == '' ? '0.00&nbsp;&nbsp;' : (parseFloat(_v).toFixed(2) + '&nbsp;&nbsp;');
	},
	getDate : function(){
		var nd = new Date(), r = '';
		r = String.format('{0}-{1}-{2} {3}:{4}:{5}', nd.getFullYear(), nd.getMonth(), nd.getDaysInMonth(),
				nd.getHours(), nd.getMinutes(), nd.getSeconds());
		return r;
	},
	percent : function(v){
		return v.toFixed(2) * 100 + '%&nbsp;&nbsp;';
	}
};

/**
 * JSONReader Config
 */
Ext.ux.readConfig = {
	totalProperty : 'totalProperty',
	root : 'root'
};

/*************************************
 * 解决DateField在Firefox宽度过长问题
 */
Ext.override(Ext.menu.DateMenu,{   
    render : function(){   
        Ext.menu.DateMenu.superclass.render.call(this);   
        this.picker.el.dom.childNodes[0].style.width = '178px';   
        this.picker.el.dom.style.width = '178px';   
    }   
});
/**
 * 重写HttpProxy.loadResponse()
 * 	增加回传参数,响应结果(ajax.response.responseText)
 */
Ext.override(Ext.data.HttpProxy, {
	loadResponse : function(o, success, response){
        delete this.activeRequest;
        if(!success){
            this.fireEvent("loadexception", this, o, response);
            o.request.callback.call(o.request.scope, null, o.request.arg, false, response);
            return;
        }
        var result;
        try {
            result = o.reader.read(response);
        }catch(e){
            this.fireEvent("loadexception", this, o, response, e);
            o.request.callback.call(o.request.scope, null, o.request.arg, false, response);
            return;
        }
        this.fireEvent("load", this, o, o.request.arg, response);
        o.request.callback.call(o.request.scope, result, o.request.arg, true, response);
    }
});
/**
 * 重写Store.loadRecords()
 * 	增加回传参数,响应结果(ajax.response.responseText)
 */
Ext.override(Ext.data.Store, {
	loadRecords : function(o, options, success, response){
		if(!o || success === false){
			if(success !== false){
                this.fireEvent("load", this, [], options, response);
            }
            if(options.callback){
                options.callback.call(options.scope || this, [], options, false, response);
            }
            return;
        }
        var r = o.records, t = o.totalRecords || r.length;
        if(!options || options.add !== true){
            if(this.pruneModifiedRecords){
                this.modified = [];
            }
            for(var i = 0, len = r.length; i < len; i++){
                r[i].join(this);
            }
            if(this.snapshot){
                this.data = this.snapshot;
                delete this.snapshot;
            }
            this.data.clear();
            this.data.addAll(r);
            this.totalLength = t;
            this.applySort();
            this.fireEvent("datachanged", this);
        }else{
            this.totalLength = Math.max(t, this.data.length+r.length);
            this.add(r);
        }
        this.fireEvent("load", this, r, options, response);
        if(options.callback){
            options.callback.call(options.scope || this, r, options, true, response);
        }
    }
});
/*************************************
 * 字符串高效拼接(类似StringBuffer)
 */
function StringBuilder() {
	this.__string__ = new Array();
}
StringBuilder.prototype.append = function(str) {
	this.__string__.push(str);
};
StringBuilder.prototype.toString = function() {
	return this.__string__.join("");
};

/**
 * 分页工具栏
 * @param pageSize
 * @param store
 * @returns
 */
function createPagingBar(pageSize, store){
	if(pageSize == null || typeof pageSize != 'number' || store == null || typeof store == 'undefined'){
		return null;
	}
	var pt = new Ext.PagingToolbar({
		pageSize : pageSize,
		store : store
	});
	return pt;
}

/*************************************
 * 
 * 创建GridPanel
 * 
 * @param {}
 *            id GridPanel的唯一编号
 * @param {}
 *            title GridPanel的标题
 * @param {}
 *            height GridPanel的高度
 * @param {}
 *            width GridPanel的宽度
 * @param {}
 *            url 服务器地址
 * @param {}
 *            cmData 设定显示的列 ---------
 *            数据格式[[是否自动生成行号,是否可以多选,是否加载数据,是否分页]['列名','数据的字段名','列宽','对齐方式','自定义显示']]
 * @param {}
 *            readerData 要显示列的对应该数据的字段名 ---------
 *            数据格式['activityName','activityAddress','contact','startDate','endDate']
 * @param {}
 *            baseParams 参数集合 ---------
 *            数据格式[['key1','value1'],['key2','value2']]
 * @param {}
 *            pageSize 每面显示几条数据
 * @param {}
 *            group 需要分组显示时传入对应该数据的字段名(例如：{name:'groupName', hide:[true|false]})，不需要分组则不传入''
 * @param {}
 *            tbar 上方的工具条[{tbar1},{tbar2}]
 * @param {}
 *            bbar 下方的工具条（true显示，false不显示）
 *            
 *            rid 设置可以重复数据
 * @return {}
 */
function createGridPanel(id, title, height, width, url, cmData, readerData,
		baseParams, pageSize, group, tbar, bbar, rid) {

	var g_ckbox = new Ext.grid.CheckboxSelectionModel({
//				handleMouseDown : Ext.emptyFn	//只能通过点击复选框才能选中复选框
			}); 
	var g_rowNum = new Ext.grid.RowNumberer(); // 自动行号

	/** 列模型的格式 * */
	var g_cmData = new Array();
	if (cmData[0][0])
		g_cmData.push(g_rowNum);
	if (cmData[0][1])
		g_cmData.push(g_ckbox);
	for (var i = 1; i < cmData.length; i++) {
		
		data = cmData[i];
		
		var columnData = {};
		columnData.header = data[0];
		columnData.dataIndex = data[1];
		
		if(group != null && typeof group != 'undefined' && group.name == data[0]){
			if(typeof group.hide == 'boolean'){
				columnData.hidden = group.hide;
				columnData.hideable = !group.hide;
			}
		}
		
		if (data[2]) {
			columnData.width = data[2];
		}
		if (data[3]) {
			columnData.align = data[3];
		}
		if (data[4]) {
			if(typeof data[4] === 'string'){
				columnData.renderer = eval(data[4]);
			}else{
				columnData.renderer = data[4];
			}
		}
		if (data[5]) {
			columnData.sortable = data[5];
		}else{
			columnData.sortable = false;
		}
		g_cmData.push(columnData);
	}

	/** 构造列模型 * */
	var g_cm = new Ext.grid.ColumnModel(g_cmData);

	/** 支持排序 * */
	g_cm.defaultSortable = true;

	/** 服务器地址 * */
	var g_proxy;
	if(url != null && typeof url != 'undefined' && url != ''){
		g_proxy = new Ext.data.HttpProxy({
			url : url
		});
	}else{
		g_proxy = new Ext.data.MemoryProxy({});
	}

	/** 数据的格式 * */
	var g_readerData = new Array();
	for (var k = 0; k < readerData.length; k++) {
		var rd = readerData[k];
		if (rd != '' && rd.length > 0) {
			var sb_rd = new StringBuilder();
			sb_rd.append("{name:'");
			sb_rd.append(rd);
			sb_rd.append("'}");
			eval("g_readerData.push(" + sb_rd.toString()+")");
		}
	}
	
	/** 读取返回数据 * */
	var g_reader = new Ext.data.JsonReader({
				totalProperty : 'totalProperty',
				root : 'root',
				idProperty : typeof rid == 'string' ? rid : ''
			}, g_readerData);

//	var b_groupBtn = null;
	var g_store = null;
	if (group != null && typeof group.name != 'undefined' && group.name != '') {
		/** 分组数据源 * */
		g_store = new Ext.data.GroupingStore({
			autoLoad : false,
			proxy : g_proxy,
			reader : g_reader,
			remoteSort : typeof group.sort != 'undefined' ? null : true,
			groupOnSort : typeof group.sort != 'undefined' ? null : false,
			sortInfo : {
				field : typeof group.sort != 'undefined' ? group.sort : group.name,
				direction : "ASC"
			},
			groupField : group.name
		});		
	} else {
		/** 普通数据源 **/
		g_store = new Ext.data.Store({
			autoLoad : false,
			proxy : g_proxy,
			reader : g_reader
		});
	}

	/** 条件查询参数 * */
	for (var n = 0; n < baseParams.length; n++) {
		var param = baseParams[n];
		g_store.baseParams[param[0]] = param[1];
	}
	
	/** 构造下工具条 * */
	var g_bbar = '';
//	if(bbar==false){
//		g_bbar = new Ext.PagingToolbar({
//				pageSize : pageSize,
//				store : g_store
//			});
//	}else{
//			g_bbar = new Ext.PagingToolbar({
//				beforePageText: '第',
//				afterPageText: '页 , 共 {0} 页',	
//				pageSize : pageSize,
//				store : g_store,
//				displayInfo : true,
//				displayMsg : '第 {0} 至 {1} 条记录, 共 {2} 条',
//				emptyMsg : '没有记录'
//			});
			g_bbar = createPagingBar(pageSize, g_store);
//	}
	/** 构造数据列表 * */
	var g_gridPanel = new Ext.grid.GridPanel({
		id : id, // 编号
		title : title, // 标题
		ds : g_store, // 数据源
		cm : g_cm, // 列模型
		sm : cmData[0][1] ? g_ckbox : null, // 全选
//		stripeRows : true, // 奇偶行颜色
		loadMask : { msg: '数据请求中,请稍等......' }, // 加载数据时遮蔽表格
		border : true, // 加上边框
		frame : true, // 显示天蓝色圆角框
//		animCollapse : false, // 收缩/展开
//		animate : false, // 动画效果
		autoScroll : true,
		height : height, // 高度
		width : width, // 宽度
		cls : 'renderStyle',
		trackMouseOver : true,// 鼠标悬浮
		autoSizeColumns: true,// 自动分配列宽
		viewConfig : {
			forceFit : true
		}, // 自动延伸
		view : group == null || typeof group.name == 'undefined' ? null :  new Ext.grid.GroupingView({
	        forceFit : true,
	        enableGroupingMenu : false,
	        hideGroupedColumn : false,
	        startCollapsed: true,
	        showGroupName: false
//	        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
	    }),
		margins : {
			top : 0,
			bottom : 0,
			right : 0,
			left : 0
		},
		cmargins : {
			top : 0,
			bottom : 0,
			right : 0,
			left : 0
		},
		bbar : cmData[0][3] ? g_bbar : null, // 加载下工具条
		tbar : typeof tbar != 'undefined' ? (Ext.isArray(tbar)==true?tbar[0]:tbar) : null // 加载上工具条
	});
	
	//添加多条工具条
	g_gridPanel.on('render',function(){
		if(typeof tbar != 'undefined' && tbar!=''){
			if(Ext.isArray(tbar)==true)
			{
				for(var i=1; i<tbar.length; i++){
					var bar = tbar[i];
					bar.render(g_gridPanel.tbar);
				}
				tbar = null;
			}
		}
	});
	
	/** 加载数据 * */
	if (cmData[0][2]){
		g_store.load({params : { start:0, limit:pageSize} });
	}
	
	return g_gridPanel;
};

/**
 * 
 */
Ext.ux.showMsg = function(msg){
	if(msg == null || typeof msg == 'undefined'){
		return false;
	}
	if(msg.success){
		Ext.example.msg(msg.title, msg.msg);
	}else{
		msg.msg = String.format('响应代码:{0}<br/>响应信息:{1}', msg.code, msg.msg);
		Ext.MessageBox.show({
			title : msg.title,
			msg : msg.msg,
			autoWidth : true,
			buttons : Ext.Msg.OK,
			icon : typeof msg.icon != 'undefined' ? msg.icon : Ext.Msg.WARNING,
			fn : msg.callBack != null && typeof msg.callBack == 'function' ? msg.callBack : null
		});		
	}
};

Ext.ux.checkFoodStatus = {
	isSpecial : function(s){ return (s & 1 << 0) != 0; },
	isRecommend : function(s){ return (s & 1 << 1) != 0; },
	isStop : function(s){ return (s & 1 << 2) != 0; },
	isGift : function(s){ return (s & 1 << 3) != 0; },
	isCurrPrice : function(s){ return (s & 1 << 4) != 0; },
	isCombo : function(s){ return (s & 1 << 5) != 0; },
	isHot : function(s){ return (s & 1 << 6) != 0; },
	isWeigh : function(s){ return (s & 1 << 7) != 0; },
	isCommission : function(s){ return (s & 1 << 8) != 0; },
	isLimit : function(s){ return (s & 1 << 10) != 0; }
};
Ext.ux.cfs = Ext.ux.checkFoodStatus;

/**
 * 格式化菜品显示名称(包含状态)
 * type:0表示点菜列表,1表示已点菜列表, 其他表示纯菜品状态 
 */
Ext.ux.formatFoodName = function(record, iname, name, type){
	var img = '';
	var status = record.get('status');
	if(Ext.ux.cfs.isSpecial(status))
		img += '&nbsp;<img src="../../images/icon_tip_te.png"></img>';
	if(Ext.ux.cfs.isRecommend(status)) 
		img += '&nbsp;<img src="../../images/icon_tip_jian.png"></img>';
	if(Ext.ux.cfs.isStop(status))
		img += '&nbsp;<img src="../../images/icon_tip_ting.png"></img>';
	if(type == 0){
		if(Ext.ux.cfs.isGift(status) && Ext.ux.staffGift)
			img += '&nbsp;<img src="../../images/forFree.png"></img>';	
	}else if(type == 1){
		if(Ext.ux.cfs.isGift(status) && record.get('isGift'))
			img += '&nbsp;<img src="../../images/forFree.png"></img>';	
	}else{
		if(Ext.ux.cfs.isGift(status))
			img += '&nbsp;<img src="../../images/forFree.png"></img>';			
	}

	if(Ext.ux.cfs.isCurrPrice(status))
		img += '&nbsp;<img src="../../images/currPrice.png"></img>';
	if(Ext.ux.cfs.isCombo(status))
		img += '&nbsp;<img src="../../images/combination.png"></img>';
	if(Ext.ux.cfs.isHot(status))
		img += '&nbsp;<img src="../../images/hot.png"></img>';
	if(Ext.ux.cfs.isWeigh(status))
		img += '&nbsp;<img src="../../images/weight.png"></img>';
	if(Ext.ux.cfs.isCommission(status))
		img += '&nbsp;<img src="../../images/commission.png"></img>';
	if(Ext.ux.cfs.isLimit(status))
		img += '&nbsp;<img src="../../images/limitCount.png"></img>';	
	if (record.get('temporary') || record.get('isTemporary'))
		img += '&nbsp;<img src="../../images/tempDish.png"></img>';
	
	record.set(iname, record.get(name) + img);
	record.commit();
};

/**
 * 
 */
Ext.ux.checkPaddingTop = function(e){
	if(Ext.isIE)
		e.getEl().dom.parentNode.style.paddingTop = '2px';
	else
		e.getEl().dom.parentNode.style.paddingTop = '5px';
};

//获取本月第一天
function getCurrentMonthFirst(){
	 var date=new Date();
	 date.setDate(1);
	 return date;
}
//获得某月的天数 
function getMonthDays(myMonth){ 
	var now = new Date(); //当前日期 
	var nowDay = now.getDate(); //当前日 
	var nowMonth = now.getMonth(); //当前月 
	var nowYear = now.getYear(); //当前年 
	nowYear += (nowYear < 2000) ? 1900 : 0; //
	var monthStartDate = new Date(nowYear, myMonth, 1); 
	var monthEndDate = new Date(nowYear, myMonth + 1, 1); 
	var days = (monthEndDate - monthStartDate)/(1000 * 60 * 60 * 24); 
	return days; 
}
/**
 * 创建常用时间集
 */
Ext.ux.createDateCombo = function(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	var now = new Date(); //当前日期 
	//var nowDay = now.getDate(); //当前日
	var nowDayOfWeek = now.getDay(); //今天本周的第几天 
	var nowMonth = now.getMonth(); //当前月 
	var nowYear = now.getYear(); //当前年 
	nowYear += (nowYear < 2000) ? 1900 : 0; //
	var comboDate = new Ext.form.ComboBox({
		xtype : 'combo',
		id : typeof _c.id == 'undefined' ? null : _c.id,
		forceSelection : true,
		width : typeof _c.width != 'undefined' ? _c.width : 85,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text']
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				if(_c.data != null && typeof _c.data != 'undefined'){
					thiz.store.loadData(_c.data);
				}else{
					thiz.store.loadData([[0,'今天'], [1,'前一天'], [5,'本周'], [6, '上周'], [7, '本月'], [8, '上月']]);					
				}
			},
			select : function(thiz, record, index){
				if(typeof _c.beginDate == 'undefined' || typeof _c.endDate == 'undefined'){
					return false;
				}
				if(record == null){
					var selfRecord = {data : {value : 1}};
					record = selfRecord;
				}
					
				var dateBegin = typeof _c.beginDate == 'string' ? Ext.getCmp(_c.beginDate) : _c.beginDate;
				var dateEnd = typeof _c.endDate == 'string' ? Ext.getCmp(_c.endDate) : _c.endDate;
				dateEnd.setValue(now);
				if(record.data.value == 0){//今天
					
				}else if(record.data.value == 1){//前一天
					now.setDate(now.getDate()-1);
					dateEnd.setValue(now);
				}else if(record.data.value == 2){//最近7天
					now.setDate(now.getDate()-7);
				}else if(record.data.value == 3){//最近一个月
					now.setMonth(now.getMonth()-1);
				}else if(record.data.value == 12){//最近二个月
					now.setMonth(now.getMonth()-2);
				}else if(record.data.value == 4){//最近三个月
					now.setMonth(now.getMonth()-3);
				}else if(record.data.value == 5){//本周
					now.setDate(now.getDate() - (nowDayOfWeek - 1));
					//为避免当天无数据显示, 删除当天 
					if(nowDayOfWeek != 1){
						var nowWeek = new Date();
						nowWeek.setDate(nowWeek.getDate()-1);
						dateEnd.setValue(nowWeek);
					}
					
				}else if(record.data.value == 6){//上周
					now.setDate(now.getDate() - nowDayOfWeek);
					dateEnd.setValue(now);
					now.setDate(now.getDate() - 6);
				}else if(record.data.value == 7){//本月
					//为避免当天无数据显示, 删除当天 
					if(now.getDate() != 1){
						var nowWeek = new Date();
						nowWeek.setDate(nowWeek.getDate()-1);
						dateEnd.setValue(nowWeek);
					}					
					now.setDate(now.getDate() - (now.getDate() -1));
				
				}else if(record.data.value == 8){//上个月
					//FIXME 月份加减遇12时
					dateEnd.setValue(new Date(nowYear, nowMonth-1, getMonthDays(nowMonth-1)));
					now = new Date(nowYear, nowMonth-1, 1);
				}else if(record.data.value == 9){//最近半年
					now.setMonth(now.getMonth()-6);
				}else if(record.data.value == 10){//无限期
					now = "";
				}
				dateBegin.setValue(now);
				dateBegin.clearInvalid();
				if(typeof _c.callback == 'function'){
					_c.callback();
				}
				now = new Date();
			}
		}
	});
	return comboDate;
};


Ext.ux.ToastWindowMgr = {  
    positions: []   
};  
  
Ext.ux.ToastWindow = function(c){
	c.time = 1;
	return new Ext.Window({
  		width:c.width?c.width:250,  
        height:c.height?c.height:150,  
        layout:'fit',  
        modal : false,  
        plain: true,  
        shadow:false, //去除阴影  
        draggable:false, //默认不可拖拽  
        resizable:false,  
        closable: true,  
        closeAction:'hide', //默认关闭为隐藏  
        autoHide:c.autohide?autohide : 10 , //15秒后自动隐藏，false则不自动隐藏 
        title : '温馨提示',
		  html: c.html,
//		 contentEl : c.contentEl, 
        constructor: function(conf){  
         Ext.Window.superclass.constructor.call(this, conf);  
            this.initPosition(true);  
        },  
        initEvents: function() {  
         Ext.Window.superclass.initEvents.call(this);  
            //自动隐藏  
            if(false !== this.autoHide){  
                var task = new Ext.util.DelayedTask(this.hide, this), second = (parseInt(this.autoHide) || 3) * 1000;  
                this.on('beforeshow', function(self) {  
                    task.delay(second);  
                });  
            }  
            this.on('beforeshow', this.showTips);  
            this.on('beforehide', this.hideTips);
            //window大小改变时，重新设置坐标  
            Ext.EventManager.onWindowResize(this.initPosition, this);
            //window移动滚动条时，重新设置坐标  
            Ext.EventManager.on(window, 'scroll', this.initPosition, this);
        },  
        //参数flag为true时强制更新位置  
        initPosition: function(flag) {
         //不可见时，不调整坐标
            if(true !== flag && this.hidden){  
                return false;  
            }  
            var doc = document, bd = (doc.body || doc.documentElement);  
            //Ext取可视范围宽高(与上面方法取的值相同), 加上滚动坐标  
            var left = bd.scrollLeft + Ext.lib.Dom.getViewWidth()-4-this.width;  
            var top = bd.scrollTop + Ext.lib.Dom.getViewHeight()-4-this.height;  
            this.setPosition(left, top);  
        },  
        showTips: function() {  
        	c.time ++;
        	if(c.time > 3){
        		return ;
        	}
            var self = this;  
//            if(!self.hidden){return false;}  
            //初始化坐标
            self.initPosition(true);   
            self.el.slideIn('b', {  
                callback: function() {   
               //显示完成后,手动触发show事件,并将hidden属性设置false,否则将不能触发hide事件   
                    self.fireEvent('show', self);  
                    self.hidden = false;  
                }  
            });
            //不执行默认的show
            return false;   
        },  
        hideTips: function() {  
        	c.time = 1;
            var self = this;  
//            if(self.hidden){return false;}  
            self.el.slideOut('b', {  
                callback: function() {  
                    //渐隐动作执行完成时,手动触发hide事件,并将hidden属性设置true  
//                    self.fireEvent('hide', self);  
                    self.hidden = true;  
                }  
            });
            //不执行默认的hide
            return false;  
        },
        listeners : c.listeners?c.listeners : {}
     });	
};




/**
 * 创建通用列模型的工具类
 */
Ext.ux.createRocord = function(o, r){
	if(r != null && typeof r != 'undefined'){
		if(r != Ext.data.Record){
			for(var i = 0; i < r.getKeys().length; i++){
				o.push(r.getKeys()[i]);
			}
		}
	}
	var f = Ext.extend(Ext.data.Record, {});
	var p = f.prototype;
    p.fields = new Ext.util.MixedCollection(false, function(field){
        return field.name;
    });
    for(var i = 0, len = o.length; i < len; i++){
        p.fields.add(new Ext.data.Field(o[i]));
    }
    f.getField = function(name){
        return p.fields.get(name);
    };
    f.getKeys = function(){
    	return p.fields.keys;
    };
    return f;
};

/**
 * tab右键关闭
 */
Ext.ux.TabCloseMenu = function(){
    var tabs = null, menu = null, ctxItem = null;
    this.init = function(tp){
        tabs = tp;
        tabs.on('contextmenu', onContextMenu);
    };

    function onContextMenu(ts, item, e){
        if(!menu){ // create context menu on first right click
            menu = new Ext.menu.Menu({
            	items : [{
                id: tabs.id + '-close',
                text: '关闭当前',
                handler : function(){
                    tabs.remove(ctxItem);
                }
            },{
                id: tabs.id + '-close-others',
                text: '关闭其他',
                handler : function(){
                    tabs.items.each(function(item){
                        if(item.closable && item != ctxItem){
                            tabs.remove(item);
                        }
                    });
                }
            }],
            ignoreParentClicks : true
            });
        }
        ctxItem = item;
        var items = menu.items;
        items.get(tabs.id + '-close').setDisabled(!item.closable);
        var disableOthers = true;
        tabs.items.each(function(){
            if(this != item && this.closable){
                disableOthers = false;
                return false;
            }
        });
        items.get(tabs.id + '-close-others').setDisabled(disableOthers);
        menu.showAt(e.getPoint());
    }
};


//查找出多条会员信息时
Ext.ux.select_getMemberByCertainCallback = {};
Ext.ux.select_getMemberByCertain = function(c){
	Ext.ux.select_getMemberByCertainCallback = c.callback;
	Ext.ux.select_getMemberByCertainWin = new Ext.Window({
		closable : false, //是否可关闭
		resizable : false, //大小调整
		title : '有多条会员信息相符合, 请选择输入号码的来源:',
		modal : true,
		width : 300,			
		items : [{
			xtype : 'panel',
			frame : true,
			border : true,
			//0: 模糊搜索, 1 : 根据手机号, 2: 微信卡号, 3:实体卡号
			html:'<div align="center"><a href="javascript:Ext.ux.select_getMemberByCertainCallback({otype:'+ c.otype +', sType:1})" style="font-size:18px;">手机号</a>'
				+'&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:Ext.ux.select_getMemberByCertainCallback({otype:'+ c.otype +', sType:2})" style="font-size:18px;">微信卡号</a>'
				+'&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:Ext.ux.select_getMemberByCertainCallback({otype:'+ c.otype +', sType:3})" style="font-size:18px;">实体卡号</a>'
				+'</div>'
		}],
		bbar : ['->',{
			text : '取消',
			iconCls : 'btn_close',
			handler : function(e){
				Ext.ux.select_getMemberByCertainWin.hide();
			}				
		}]	
	});
	Ext.ux.select_getMemberByCertainWin.show();
}

//MonthPickerPlugin.js文件,年月选择的关键代码  
Ext.ux.MonthPickerPlugin = function() {   
    var picker;   
    var oldDateDefaults;   
  
    this.init = function(pk) {   
        picker = pk;   
        picker.onTriggerClick = picker.onTriggerClick.createSequence(onClick);   
        picker.getValue = picker.getValue.createInterceptor(setDefaultMonthDay).createSequence(restoreDefaultMonthDay);   
        picker.beforeBlur = picker.beforeBlur.createInterceptor(setDefaultMonthDay).createSequence(restoreDefaultMonthDay);   
    };   
  
    function setDefaultMonthDay() {   
        oldDateDefaults = Date.defaults.d;   
        Date.defaults.d = 1;   
        return true;   
    }   
  
    function restoreDefaultMonthDay(ret) {   
        Date.defaults.d = oldDateDefaults;   
        return ret;   
    }   
  
    function onClick(e, el, opt) {   
        var p = picker.menu.picker;   
        p.activeDate = p.activeDate.getFirstDateOfMonth();   
        if (p.value) {   
            p.value = p.value.getFirstDateOfMonth();   
        }   
  
        p.showMonthPicker();   
           
        if (!p.disabled) {   
            p.monthPicker.stopFx();   
            p.monthPicker.show();   
   // if you want to click,you can the dblclick event change click  
            p.mun(p.monthPicker, 'click', p.onMonthClick, p);   
            p.mun(p.monthPicker, 'click', p.onMonthDblClick, p);   
            p.onMonthClick = p.onMonthClick.createSequence(pickerClick);   
            p.onMonthDblClick = p.onMonthDblClick.createSequence(pickerDblclick);   
            p.mon(p.monthPicker, 'click', p.onMonthClick, p);   
            p.mon(p.monthPicker, 'click', p.onMonthDblClick, p);   
        }   
    }   
  
    function pickerClick(e, t) {   
        var el = new Ext.Element(t);   
        if (el.is('button.x-date-mp-cancel')) {   
            picker.menu.hide();   
        } else if(el.is('button.x-date-mp-ok')) {   
            var p = picker.menu.picker;   
            p.setValue(p.activeDate);   
            p.fireEvent('select', p, p.value);   
        }   
    }   
  
    function pickerDblclick(e, t) {   
        var el = new Ext.Element(t);   
        if (el.parent()   
            && (el.parent().is('td.x-date-mp-month')   
            || el.parent().is('td.x-date-mp-year'))) {   
  
            var p = picker.menu.picker;   
            p.setValue(p.activeDate);   
            p.fireEvent('select', p, p.value);   
        }   
    }   
};   
  
Ext.preg('monthPickerPlugin', Ext.ux.MonthPickerPlugin);  


Ext.ux.cr = Ext.ux.createRocord;

var TableRecord = Ext.ux.cr(['id', 'alias', 'rid', 'name', 'customNum', 'minimumCost', 'serviceRate', 'categoryValue',
    'categoryText', 'statusValue', 'statusText', 'region']);
var TasteRecord = Ext.ux.cr(['id','alias','name','price','rate', 'cateValue','cateText','calcText','typeValue', 'typeText']);
var FoodBasicRecord = Ext.ux.cr(['id','alias','name','displayFoodName','pinyin','unitPrice','status','stockStatusValue',
    'kitchen', 'kitchen.alias','kitchen.name','kitchen.id','operator','tasteRefType','desc','img', 'rid', 'commission', 'limitCount', 'multiUnitPrice', 'printKitchenId']);
var FoodTasteRecord = Ext.ux.cr(['taste', 'taste.id', 'taste.alias', 'taste.name', 'taste.rank', 'taste.price', 'taste.rate', 
    'taste.calcValue', 'taste.calcText', 'taste.cateValue', 'taste.cateText', 'food', 'food.id', 'food.name']);
var ComboFoodRecord = Ext.ux.cr(['parentId', 'parentName', 'amount'], FoodBasicRecord);
var MaterialCateRecord = Ext.ux.cr(['id', 'rid', 'name', 'typeValue', 'typeText']);
var MaterialRecord = Ext.ux.cr(['pinyin', 'id', 'rid', 'cateId', 'cateName', 'price', 'name', 'stock', 'lastModStaff', 'lastModDate', 
    'lastModDateFormat', 'statusValue', 'statusText']);
var MaterialDeptRecord = Ext.ux.cr(['price', 'restaurantId', 'stock', 'cost', 'dept', 'material', 'materialId', 'materialName', 'materialPinyin']); 
var FoodMaterialRecord = Ext.ux.cr(['rid', 'foodId', 'foodName', 'materialId', 'consumption', 'materialName', 'materialCateName']);
var StockRecord = Ext.ux.cr(['id', 'restaurantId', 'oriStockId', 'oriStockDateFormat',  'cateTypeText', 'cateTypeValue', 'birthDateFormat',
    'deptIn', 'deptOut', 'supplier', 'approverName', 'approverDateFormat', 'operatorName', 'amount', 'price', 'actualPrice', 'totalAmount', 
    'totalPrice', 'typeValue', 'typeText', 'statusValue', 'statusText', 'comment', 'subTypeValue', 'subTypeText', 'stockDetails']);
var StockDetailRecord = Ext.ux.cr(['id', 'stockActionId', 'stock', 'price', 'amount', 'totalPrice',  
    'material.cateName', 'material.name', 'material', 'material.id']);
var SupplierRecord = Ext.ux.cr(['supplierID', 'restaurantId', 'name', 'tele', 'addr', 'contact', 'comment']);
var KitchenRecord = Ext.ux.cr(['id', 'alias', 'rid', 'name', 'isAllowTmp', 'typeValue', 'dept', 'dept.name']);
var OrderRecord = Ext.ux.cr(['restaurantName', 'id', 'seqId', 'rid', 'birthDateFormat', 'orderDate', 'orderDateFormat', 'categoryValue', 'categoryText', 'waiter',
    'statusValue', 'statusText', 'settleTypeValue', 'settleTypeText', 'payTypeValue', 'payTypeText', 'discount', 'pricePlan', 
    'table', 'table.alias','table.name','table.region.name', 'member', 'customNum', 'comment', 'repaidPrice', 'receivedCash', 'serviceRate', 'discountPrice', 
    'cancelPrice', 'giftPrice', 'totalPrice', 'erasePrice', 'couponPrice', 'actualPrice', 'orderFoods', 'childOrders', 'actualPriceBeforeDiscount']);
var OrderFoodRecord = Ext.ux.cr(['dataType', 'orderId', 'orderDateFormat', 'count', 'discount', 'isTemporary', 'isGift','totalPrice', 'totalPriceBeforeDiscount',
    'tasteGroup', 'tasteGroup.tastePref', 'tasteGroup.tastePrice', 'waiter', 'actualPrice', 'operation'], FoodBasicRecord);
var FoodPricePlanRecord = Ext.ux.cr(['planId', 'foodId', 'rid', 'unitPrice', 'foodAlias', 'foodName', 
    'kitchenId', 'kitchenAlias', 'kitchenName', 'pricePlan', 'pricePlan.name']);
var DeptRecord = Ext.ux.cr(['id', 'name', 'rid', 'typeValue']);
var DiscountPlanRecord = Ext.ux.cr(['id', 'rate', 'kitchen', 'kitchen.id', 'kitchen.name', 'discount', 'discount.id', 'discount.name']);
var StockTakeDetailRecord = Ext.ux.cr(['id', 'material', 'material.name', 'actualAmount', 'expectAmount', 'deltaAmount', 'stockInTotal', 'stockOutTotal']);

var deltaReportRecord = Ext.ux.cr(['id', 'materialName','primeAmount', 'stockInTotal', 'stockOutTotal', 'finalAmount', 'expectConsumption', 'actualConsumption', 'deltaAmount']);

var StockTakeRecord = Ext.ux.cr(['id', 'rid', 'dept', 'dept.name', 'cateTypeValue', 'cateTypeText', 'statusValue', 'statusText', 'detail',
    'operator', 'operatorId', 'approver', 'approverId', 'startDateFormat', 'finishDateFormat', 'comment', 'materialCate', 'materialCate.name']);
var TableRecord = Ext.ux.cr(['id', 'alias', 'rid', 'name', 'customNum', 'minimumCost', 'serviceRate', 
    'categoryValue', 'categoryText', 'statusValue', 'statusText', 'region', 'region.name']);
var SalesSubStatRecord = Ext.ux.cr(['salesAmount', 'income', 'tasteIncome', 'discount', 'gifted', 'giftedAmount','avgPrice','avgCost']);
var MemberBasicRecord = Ext.ux.cr(['id', 'rid', 'totalConsumption','ageVal', 'fansAmount', 'ageText', 'totalCharge', 'totalPoint', 'lastConsumption', 'baseBalance', 'extraBalance', 'totalBalance', 'usedBalance', 'point', 'usedPoint', 'sexText', 'sexValue', 'memberType', 'memberType.name',
    'consumptionAmount', 'name', 'branchName', 'tele', 'mobile', 'birthday', 'referrer', 'birthdayFormat', 'idCard', 'branchId', 'company', 'tastePref', 'taboo', 'contactAddress', 'comment', 'createDate', 'createDateFormat', 'memberCard', 'publicComment', 'privateComment', 'acctendtioned', 'weixinCard']);
var MemberOperationRecord = Ext.ux.cr(['id', 'seq', 'rid', 'staffId', 'staffName', 'orderId', 'deltaBaseMoney', 'deltaExtraMoney', 'deltaPoint', 'remainingBaseMoney', 'remainingExtraMoney', 'remainingPoint', 'comment', 'deltaTotalMoney', 'remainingTotalMoney', 'operateDateFormat',
    'member', 'branchName', 'member.name','member.mobile', 'member.memberType.name', 'operateTypeText', 'operateTypeValue', 'payTypeText', 'payTypeValue', 'payMoney', 'chargeTypeText', 'chargeTypeValue', 'chargeMoney']);

var MOSummaryRecord = Ext.ux.cr(['chargeMoney', 'consumeAmount', 'payMoney', 'consumePoint', 'pointConsume', 'pointAdjust', 'moneyAdjust', 'member', 'member.name', 'member.memberType.name']);
var CouponRecord = Ext.ux.cr(['createStaff', 'birthDate', 'couponId', 'restaurantId', 'couponType','couponType.expiredFormat', 'orderId', 'orderDate', 'statusText', 'statusValue', 'member', 'member.name', 'member.mobile']);
var saleMaterial_Record = Ext.ux.cr(['foodName', 'amount', 'rate', 'consume']);




















