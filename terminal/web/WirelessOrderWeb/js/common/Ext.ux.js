/*******************************************************************************
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

/*******************************************************************************
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
 *            数据格式[[true,true]['列名','数据的字段名','列宽','指定自定义的方法去改变值的显示方式']]
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
		baseParams, pageSize, groupName,tbar,bbar) {

	this.g_ckbox = new Ext.grid.CheckboxSelectionModel({
				handleMouseDown : Ext.emptyFn	//只能通过点击复选框才能选中
			}); // 复选框
	this.g_rowNum = new Ext.grid.RowNumberer(); // 自动行号

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

		if (data.length > 2 && data[2] != '' && data[2] != NaN) {
			sb.append(",width:");
			sb.append(data[2]);
		}
		if (data.length > 3 && data[3] != '' && data[3].length > 0) {
			sb.append(",renderer:");
			sb.append(data[3]);
		}

		sb.append("}");

		/** 将字符串转换成对象。再将转换后的对象付值给obj * */
		eval("g_cmData.push(" + sb.toString()+")");

	}

	/** 构造列模型 * */
	this.g_cm = new Ext.grid.ColumnModel(g_cmData);

	/** 支持排序 * */
	g_cm.defaultSortable = true;

	/** 服务器地址 * */
	this.g_proxy = new Ext.data.HttpProxy({
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
	var b_groupBtn = null;

	if (groupName == '') {
		/** 普通数据源 * */
		g_store = new Ext.data.Store({
					proxy : g_proxy,
					reader : g_reader
				});
	} else {
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
	}

	/** 条件查询参数 * */
	for (var n = 0; n < baseParams.length; n++) {
		var param = baseParams[n];

		g_store.baseParams[param[0]] = param[1];
	}
	
	g_store.baseParams['start'] = 0;
	g_store.baseParams['limit'] = pageSize;
	/** 加载数据 * */
//	g_store.load({
//				params : {
//					start : 0,
//					limit : pageSize
//				}
//			});

	/** 构造下工具条 * */
	var g_bbar = "";
//	if(bbar==false){
//		g_bbar = new Ext.PagingToolbar({
//				pageSize : pageSize,
//				store : g_store
//			});
//	}else{
			g_bbar = new Ext.PagingToolbar({
				pageSize : pageSize,
				store : g_store,
//				plugins : new Ext.ux.PageSizePlugin(),
				displayInfo : true,
				displayMsg : '显示第{0}~{1}条记录，共{2}条',
				emptyMsg : "没有记录"
//				items : [groupName == '' ? '' : b_groupBtn, b_printBtn]
//				,items : b_groupBtn
			});
//	}
	/** 构造数据列表 * */
	var g_gridPanel = new Ext.grid.GridPanel({
		id : id, // 编号
		title : title, // 标题
		ds : g_store, // 数据源
		cm : g_cm, // 列模型
		sm : cmData[0][1] ? g_ckbox : null, // 全选
		stripeRows : true, // 奇偶行颜色
		loadMask : { msg: '数据请求中，请稍后...' }, // 加载数据时遮蔽表格
		border : true, // 加上边框
		frame : true, // 显示天蓝色圆角框
		animCollapse : false, // 收缩/展开
		animate : false, // 动画效果
		autoScroll : true,
		height : height, // 高度
		width : width, // 宽度
		// autoExpandColumn:2, // 让第二列的宽度自动伸展
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
//		view : groupName == '' ? new Ext.grid.GridView : g_groupView, // 数据分组显示
//		bbar : g_bbar, // 加载下工具条
		tbar : typeof tbar != 'undefined' ? (Ext.isArray(tbar)==true?tbar[0]:tbar) :null // 加载上工具条
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
	
	return g_gridPanel;
};
