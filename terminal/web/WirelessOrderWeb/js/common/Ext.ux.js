/*************************************
 * 
 * 
 * 
 * 
 * 
 * 
 */


/*************************************
 * 部分通用显示格式
 */
Ext.ux.txtFormat = {
	barMsg : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{0}:&nbsp;<span id="{1}" style="color:green; font-size:15px; font-weight:bold;">{2}</span>',
	barTitle : '<span style="font-weight:bold; font-size:13px;">{0}</span>',
	typeName : '<div style="float:left; width:100px; font-size:12px;">&nbsp;{0}:&nbsp;<span id="{1}" style="color:green; font-weight:bold; font-size:13px;">{2}</span></div>',
	linkClassOne : '<a id="{0}" href="{1}" onClick="{2}" style="color:green; font-size:15px; font-weight:bold;">{3}</a>',
	renderer : '<a href="javascript:{0}({1})" {2}>{3}</a>',
	xh : '<font color="red">＊</font>',
	gridDou : function(_v){
		return _v == '' ? '0.00&nbsp;&nbsp;' : (parseFloat(_v).toFixed(2) + '&nbsp;&nbsp;');
	},
	getDate : function(){
		var nd = new Date(), r = '';
		r = String.format('{0}-{1}-{2} {3}:{4}:{5}', nd.getFullYear(), nd.getMonth(), nd.getDaysInMonth(),
				nd.getHours(), nd.getMinutes(), nd.getSeconds());
		return r;
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
        if(Ext.isGecko){   
            this.picker.el.dom.childNodes[0].style.width = '178px';   
            this.picker.el.dom.style.width = '178px';   
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
 *            groupName 需要分组显示时传入对应该数据的字段名(例如：'activityName')，不需要分组则不传入''
 * @param {}
 *            tbar 上方的工具条[{tbar1},{tbar2}]
 * @param {}
 *            bbar 下方的工具条（true显示，false不显示）
 * @return {}
 */
createGridPanel = function(id, title, height, width, url, cmData, readerData,
		baseParams, pageSize, groupName, tbar, bbar) {

	var g_ckbox = new Ext.grid.CheckboxSelectionModel({
				handleMouseDown : Ext.emptyFn	//只能通过点击复选框才能选中复选框
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
		var sb = new StringBuilder();
		sb.append("{");
		sb.append("header:'");
		sb.append(data[0]);
		sb.append("',dataIndex:'");
		sb.append(data[1] + "'");

		if (data.length > 2 && data[2] != null && data[2] != '') {
			sb.append(",width:");
			sb.append(data[2]);
		}
		if (data.length > 3 && data[3] != null && data[3] != '' && data[3].length > 0) {
			sb.append(",align:'");
			sb.append(data[3]);
			sb.append("'");
		}
		if (data.length > 4 && data[4] != null && data[4] != '' && data[4].length > 0) {
			sb.append(",renderer:");
			sb.append(data[4]);
		}

		sb.append("}");

		/** 将字符串转换成对象。再将转换后的对象付值给obj * */
		eval("g_cmData.push(" + sb.toString()+")");

	}

	/** 构造列模型 * */
	var g_cm = new Ext.grid.ColumnModel(g_cmData);

	/** 支持排序 * */
	g_cm.defaultSortable = true;

	/** 服务器地址 * */
	var g_proxy = new Ext.data.HttpProxy({
				url : url
			});

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
				root : 'root'
			}, g_readerData);

	var g_store = null;
//	var b_groupBtn = null;

	if (typeof(groupName) != 'undefined' && groupName != '') {
		/** 分组数据源 * */
		g_store = new Ext.data.GroupingStore({
					proxy : g_proxy,
					reader : g_reader,
					sortInfo : {
						field : groupName,
						direction : "ASC"
					},
					groupField : groupName
				});
	} else {
		/** 普通数据源 **/
		g_store = new Ext.data.Store({
			proxy : g_proxy,
			reader : g_reader
		});
	}

	/** 条件查询参数 * */
	for (var n = 0; n < baseParams.length; n++) {
		var param = baseParams[n];

		g_store.baseParams[param[0]] = param[1];
	}
	
	/** 加载数据 * */
//	g_store.load({
//				params : {
//					start : 0,
//					limit : pageSize
//				}
//			});

	/** 构造下工具条 * */
	var g_bbar = '';
//	if(bbar==false){
//		g_bbar = new Ext.PagingToolbar({
//				pageSize : pageSize,
//				store : g_store
//			});
//	}else{
			g_bbar = new Ext.PagingToolbar({
				beforePageText: '第',
				afterPageText: '页 , 共 {0} 页',	
				pageSize : pageSize,
				store : g_store,
				displayInfo : true,
				displayMsg : '第 {0} 至 {1} 条记录, 共 {2} 条',
				emptyMsg : '没有记录'
			});
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
		trackMouseOver : true,// 鼠标悬浮
		autoSizeColumns: true,// 自动分配列宽
		viewConfig : {
			forceFit : true
		}, // 自动延伸
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
	
	// 是否加载数据
	if (cmData[0][2]){
		g_store.load({params : { start:0, limit:pageSize} });
	}
	
	return g_gridPanel;
};

/**
 * 
 */
Ext.ux.showMsg = function(msg){
	if(msg == null || typeof(msg) == 'undefined'){
		return false;
	}
	msg.msg = msg.code == '' || parseInt(msg.code) == 1111 ? msg.msg : String.format('错误代码:{0}<br/>错误信息:{1}', msg.code, msg.msg);
	Ext.MessageBox.show({
		title : msg.title,
		msg : msg.msg,
		autoWidth : true,
		buttons : Ext.MessageBox.OK
	});
};

/**
 * 
 */
Ext.ux.formatFoodName = function(record, iname, name){
	var img = '';
	if (record.get('special') == true) {
		img += '&nbsp;<img src="../../images/icon_tip_te.png"></img>';
	}
	if (record.get('recommend') == true) {
		img += '&nbsp;<img src="../../images/icon_tip_jian.png"></img>';
	}
	if (record.get('stop') == true) {
		img += '&nbsp;<img src="../../images/icon_tip_ting.png"></img>';
	}
	if (record.get('gift') == true) {
		img += '&nbsp;<img src="../../images/forFree.png"></img>';
	}
	if (record.get('currPrice') == true) {
		img += '&nbsp;<img src="../../images/currPrice.png"></img>';
	}
	if (record.get('combination') == true) {
		img += '&nbsp;<img src="../../images/combination.png"></img>';
	}
	if (record.get('temporary') == true) {
		img += '&nbsp;<img src="../../images/tempDish.png"></img>';
	}
	if (record.get('hot') == true) {
		img += '&nbsp;<img src="../../images/hot.png"></img>';
	}
	record.set(iname, record.get(name) + img);
	record.commit();
};

