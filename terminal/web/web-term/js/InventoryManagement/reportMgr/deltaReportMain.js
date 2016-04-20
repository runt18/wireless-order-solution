Ext.onReady(function(){
	
	var deptData = [];
	var PAGE_LIME = 20; 
	var materialTypeDate = [[1,'商品'],[2,'原料']];
	var materialTypeComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 90,
		id : 'comboMaterialType',
		value : 1,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : materialTypeDate
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		readOnly : false	,
		listeners : {
	        select : function(combo, record, index){ 
	        	
	        	drm_materialCateComb.reset();
	        	sDelta_materialComb.allowBlank = true;
	        	sDelta_materialComb.reset();
	        	sDelta_materialCateStore.load({  
		            params: {  
		            	type : combo.value,  
		            	dataSource : 'normal'
		            }  
	            });     
	            
	        	sDelta_materialStore.load({
	        		params: {
	        			cateType : combo.value,
	        			dataSource : 'normal'
	        		}
	        	});
			}  
		}
		
	});
	
	
	var sDelta_materialCateStore = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do?restaurantID=' + restaurantID}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		         {name : 'id'},
		         {name : 'name'}
		])
	});
	sDelta_materialCateStore.load({  
	    params: {  
	    	type : materialTypeComb.value,  
	    	dataSource : 'normal'
	    }
	}); 
	var drm_materialCateComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 90,
		id : 'drm_comboMaterialCate',
		store : sDelta_materialCateStore,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		readOnly : false,
		listeners : {
	        select : function(combo, record, index){ 
	        	sDelta_materialComb.allowBlank = true;
	        	sDelta_materialComb.reset();
	        	sDelta_materialStore.load({  
		            params: {  
		            	cateType : materialTypeComb.value,
		            	cateId : combo.value,  
		            	dataSource : 'normal'
		            }  
	            });     
			}
	
		}
		
	});
	
	
	
	var sDelta_materialStore = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},
	         {name : 'name'},
	         {name : 'pinyin'}
		])
	});
	sDelta_materialStore.load({  
	    params: { 
	    	cateType : materialTypeComb.value,
	    	dataSource : 'normal'
	    }  
	}); 
	var sDelta_materialComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 100,
		listWidth : 250,
		maxheight : 300,
		id : 'drm_comboMaterial',
		store : sDelta_materialStore,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		tpl:'<tpl for=".">' 
			+ '<div class="x-combo-list-item" style="height:18px;">'
			+ '{id} -- {name} -- {pinyin}'
			+ '</div>'
			+ '</tpl>',
		listeners : {
			beforequery : function(e){
				var combo = e.combo;
				if(!e.forceAll){
					var value = e.query;
					combo.store.filterBy(function(record){
						return record.get('name').indexOf(value) != -1
								|| (record.get('id')+'').indexOf(value) != -1
								|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
					});
					combo.expand();
					combo.select(0, true);
					return false;
				
				}
			},
			select : function(){
				Ext.getCmp('btnSearch').handler();		
			}
			
		}
	});
	var deltaReportDeptTree;
	
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	
	//部门combo
	var deptComb = new Ext.form.ComboBox({
		id : 'deptComb_comboBox_deltaReportMain',
		forceSelection : true,
		width : 110,
		maxheight : 300,
		store : new Ext.data.SimpleStore({
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				var data = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../OperateDept.do',
					params: { 
				    	dataSource : 'getByCond',
				    	inventory : true
				    }, 
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(-1);
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('btnSearch').handler();	
			}
			
		}
	});
	
	var deltaReportBar = new Ext.Toolbar({
		items : [
 		{
			xtype : 'label',
			text : '部门:'
		}, deptComb,
		{ xtype:'tbtext', text:'日期:'},
		{
			xtype : 'datefield',
			id : 'dr_beginDate',
			allowBlank : false,
			maxValue : new Date(),
			value : new Date(),
            width:100,  
            plugins: 'monthPickerPlugin',  
            format: 'Y-m'
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '类别:'},
		drm_materialCateComb,
		{xtype : 'tbtext', text : '货品:'},
		sDelta_materialComb,
		'->', {
			text : '刷新',
			id : 'btnSearch',
			iconCls : 'btn_refresh',
			handler : function(){
				var sgs = deltaReportGrid.getStore();
				sgs.baseParams['beginDate'] = Ext.getCmp('dr_beginDate').getValue().format('Y-m');
				sgs.baseParams['deptId'] = Ext.getCmp('deptComb_comboBox_deltaReportMain').getStore().getCount() > 0 ? Ext.getCmp('deptComb_comboBox_deltaReportMain').getValue() : '-1';
				sgs.baseParams['cateType'] = Ext.getCmp('comboMaterialType').getValue();
				sgs.baseParams['cateId'] = Ext.getCmp('drm_comboMaterialCate').getValue();
				sgs.baseParams['materialId'] = Ext.getCmp('drm_comboMaterial').getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : PAGE_LIME
					}
				});
			}
		}, {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				
				var url = "../../{0}?dataSource={1}&beginDate={2}&deptId={3}&materialId={4}&cateId={5}&cateType={6}";
				url = String.format(
					url,
					'ExportHistoryStatisticsToExecl.do',
					'detailReport',
					Ext.getCmp('dr_beginDate').getValue().format('Y-m'),
					Ext.getCmp('deptComb_comboBox_deltaReportMain').getValue(),
					Ext.getCmp('drm_comboMaterial').getValue(),
					Ext.getCmp('drm_comboMaterialCate').getValue(),
					Ext.getCmp('comboMaterialType').getValue()
				);
				window.location = url;
			}
		}
		]
	});
	
	var deltaReportGrid = createGridPanel(
			'',
			'货品列表',
			'',
			'',
			'../../QueryDeltaReport.do',
			[
				[true, false, false, true], 
				['品项名称', 'materialName', 130],
				['初期数量', 'primeAmount', 80,'right', Ext.ux.txtFormat.gridDou],
				['入库总数', 'stockInTotal',80,'right', Ext.ux.txtFormat.gridDou],
				['出库总数', 'stockOutTotal',80,'right', Ext.ux.txtFormat.gridDou],
				['期末数量', 'finalAmount',80,'right', Ext.ux.txtFormat.gridDou],
				['实际消耗', 'actualConsumption',80,'right', Ext.ux.txtFormat.gridDou],
				['理论消耗', 'expectConsumption',80,'right', Ext.ux.txtFormat.gridDou],
				['差异数', 'deltaAmount',80,'right', function(v){
					return v >= 0 ? '<span style="color:green;font-weight:bold;font-size:16px;">' + v.toFixed(2) + '</span>' : '<span style="color:red;font-weight:bold;font-size:16px;">' + v.toFixed(2) + '</span>';
				}]
			],
			deltaReportRecord.getKeys(),
			[['isPaging', true]],
			GRID_PADDING_LIMIT_20,
			'',
			deltaReportBar
		);		
	deltaReportGrid.region = 'center';
	deltaReportGrid.on('render', function(){
		Ext.getCmp('btnSearch').handler();
	});	
	
	new Ext.Panel({
		renderTo : 'divDeltaReport',
		height : parseInt(Ext.getDom('divDeltaReport').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [deltaReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler;
			}
		}]
	});
	
	
});