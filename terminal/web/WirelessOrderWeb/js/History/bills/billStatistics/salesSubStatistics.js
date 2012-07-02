Ext.BLANK_IMAGE_URL = "../../js/extjs/resources/images/default/s.gif";

//var salesSubGrid = null;
//var salesSubMune = null;
//var salesSubWin = null;
var salesSubQueryType = 0;
var salesSubOrderType = 0;
var salesSubDeptId = -1;

salesSubPanelnit = function(){
	
	if(!salesSubGrid){	
		
	}
	
	if(!salesSubMune){
		
	}	
};

salesSub = function(){	
	
//	salesSubPanelnit();
	
	var cmData = [[true, false, false, true], 
	              ['部门','item'], ['销量','salesAmount','','right','Ext.ux.txtFormat.gridDou'], ['均价','avgPrice','','right','Ext.ux.txtFormat.gridDou'], ['单位成本','avgCost','','right','Ext.ux.txtFormat.gridDou'],
	              ['营业额','income','','right','Ext.ux.txtFormat.gridDou'], ['折扣额','discount','','right','Ext.ux.txtFormat.gridDou'], ['赠送额','gifted','','right','Ext.ux.txtFormat.gridDou'],
	              ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
	              ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou']];
	var url = '../../SalesSubStatistics.do?tiem='+new Date();
	var readerData = ['item','income','discount','gifted','cost','costRate','profit','profitRate','salesAmount','avgPrice','avgCost'];
	var baseParams = [['pin', pin], ['restaurantID', restaurantID]];
	var pageSize = 15;
	var id = 'salesSub_grid';
	var title = '';
	var height = '';
	var width = '';
	var groupName = '';
	
	var salesSubGrid_tbar = new Ext.Toolbar({
		buttonAlign : 'left',
		height : 26,
		items : [
		{xtype:'tbtext',text:'类别:<span id="salesSubShowType"></span'},
		{xtype:'tbtext',text:'&nbsp;&nbsp;'},	
		{xtype:'tbtext',text:'日期:'},
		{
			xtype : 'datefield',		
			format : 'Y-m-d',
			id : 'salesSubBegDate',
//			value : new Date().getFirstDateOfMonth(),
			width : 100,
			readOnly : true,
			listeners : {
				blur : function(){									
//					Ext.ux.checkDateForBeginAndEnd(true, 'salesSubBegDate', 'salesSubEndDate');
					salesSubSearchCheckDate(true, 'salesSubBegDate', 'salesSubEndDate');
				}
			}
		},
		{xtype:'tbtext',text:'&nbsp;&nbsp;至&nbsp;&nbsp;'},	
		{
			xtype : 'datefield',
			format : 'Y-m-d',
			id : 'salesSubEndDate',
			width : 100,
//			value : new Date(),
			readOnly : true,
			listeners : {
				blur : function(){									
//					Ext.ux.checkDateForBeginAndEnd(false, 'salesSubBegDate', 'salesSubEndDate');
					salesSubSearchCheckDate(false, 'salesSubBegDate', 'salesSubEndDate');
				}
			}
		},
		{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;&nbsp;'},
		{
			xtype : 'radio',
			hideLabel : true,
			width : 100,
			boxLabel : "按毛利排序",
			name : 'salesSubGridOrderByRadio',
			id : 'salesSubGridOrderByRadioProsit',
			checked : true,
			inputValue : '0',
			listeners : {
				check : function(e){
					if(e.getValue() == true){
						salesSubOrderType = e.getRawValue();
					}
				}
			}
		},
		{
			xtype : 'radio',
			hideLabel : true,
			width : 100,
			boxLabel : '按销量排序',
			id : 'salesSubGridOrderByRadioSales',
			name : 'salesSubGridOrderByRadio',				
			inputValue : '1',
			listeners : {
				check : function(e){
					if(e.getValue() == true){
						salesSubOrderType = e.getRawValue();
					}
				},
				blur : function(e){
					
				}
			}
		},						
		'->',
		{
			text : '搜索',
			id : 'salesSubBtnSearch',
			width : 150,
			handler : function(){
				
//				Ext.ux.checkDateForBeginAndEnd(true, 'salesSubBegDate', 'salesSubEndDate');
//				Ext.ux.checkDateForBeginAndEnd(false, 'salesSubBegDate', 'salesSubEndDate');					
				
				var bd = Ext.getCmp('salesSubBegDate').getValue();
				var ed = Ext.getCmp('salesSubEndDate').getValue();
				if(bd == '' && ed == ''){
					Ext.getCmp('salesSubEndDate').setValue(new Date());
					salesSubSearchCheckDate(false, 'salesSubBegDate', 'salesSubEndDate');
				}else if(bd != '' && ed == ''){
					salesSubSearchCheckDate(true, 'salesSubBegDate', 'salesSubEndDate');
				}else if(bd == '' && ed != ''){
					salesSubSearchCheckDate(false, 'salesSubBegDate', 'salesSubEndDate');
				}
									
				
				var gs = salesSubGrid.getStore();
				gs.baseParams['dateBeg'] = Ext.getCmp('salesSubBegDate').getRawValue();
				gs.baseParams['dataEnd'] = Ext.getCmp('salesSubEndDate').getRawValue();
				gs.baseParams['queryType'] = salesSubQueryType;
				gs.baseParams['orderType'] = salesSubOrderType;
				gs.baseParams['deptID'] = salesSubDeptId;
				gs.removeAll();
				gs.load({params:{start:0,limit:15}});
				
				salesSubSetColumn();
			}
		}
		]
	});
	
	var salesSubGrid = createGridPanel(id,title,height,width,url,cmData,readerData,baseParams,pageSize,groupName,salesSubGrid_tbar);
	salesSubGrid.region = 'center';
	salesSubGrid.getStore().on('load', function(store, records, options ){
		if(store.getCount() > 0){
			var sumRow = salesSubGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < salesSubGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = salesSubGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
		}
	});
	
	var salesSubMuneTree_tbar = new Ext.Toolbar({
		buttonAlign : 'left',
		height : 23,
		items : [
		'->',
		{
			xtype : 'radio',
			hideLabel : true,
			width : 80,
			boxLabel : '部门分类',
			id : 'salesSubMuneTreeTypeRadioDept',
			name : 'salesSubMuneTreeTypeRadio',
			inputValue : '0',
			checked : true,
			listeners : {
				check : function(e){						
					if(e.getValue() == true){						
						salesSubSetDisplay(false, e.boxLabel,  e.getRawValue(), true, '部门' );
						Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);							
						Ext.getCmp('salesSubMuneTree').root.select();							
						salesSubDeptId = '';
					}
				}
			}
		},
		{
			xtype : 'radio',
			hideLabel : true,
			width : 80,
			boxLabel : '全部菜品',
			name : 'salesSubMuneTreeTypeRadio',
			inputValue : '1',
			listeners : {
				check : function(e){						
					if(e.getValue() == true){							
						salesSubSetDisplay(true, e.boxLabel, e.getRawValue(), false,'菜品' );
						Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);	
						salesSubDeptId = '';
						var root = Ext.getCmp('salesSubMuneTree').getRootNode();							
						if(root.hasChildNodes()){
							for(var i = 0; i <  root.childNodes.length; i++){
								salesSubDeptId += (i > 0 ? ',' : '');
	        					salesSubDeptId += root.childNodes[i].attributes.deptID;
							}
						}
					}
				}
			}
		}
		]
	});
	
	var salesSubMuneTree = new Ext.tree.TreePanel({
		id : 'salesSubMuneTree',
		border : false,
		rootVisible : true,
		height : 410,	
		animate : true,
		loader:new Ext.tree.TreeLoader({    
	          dataUrl:'../../QueryDeptTree.do?time='+new Date(),
	          baseParams : {
					'restaurantID' : restaurantID
				}
	       }),
		root: new Ext.tree.AsyncTreeNode({
			expanded : true,
            text : '全部',
            leaf : false,
            deptID : '-1'
		}),
        listeners : {
        	click : function(e){
        		if(e.attributes.deptID == '' || e.attributes.deptID == '-1'){
        			salesSubSetDisplay(false, '部门分类', 0, true, '部门');
        			Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);
        			salesSubDeptId = '';
        			if(e.hasChildNodes()){
        				for(var i = 0; i < e.childNodes.length; i++){
        					salesSubDeptId += (i > 0 ? ',' : '');
        					salesSubDeptId += e.childNodes[i].attributes.deptID;
        				}
        			}	        				        		
        		}else{
        			salesSubSetDisplay(false, e.text, 1, false, '菜品');
        			salesSubDeptId = e.attributes.deptID;
        		}	        		
        	}
        }
	});
	var salesSubMune = new Ext.Panel({
		region : 'west',
		width : 160,			
		border : false,
		items : [{xtype:'panel', tbar:salesSubMuneTree_tbar, border:false}, salesSubMuneTree]
	});
	
	
	
//	if(!salesSubWin){
	var salesSubWin = new Ext.Window({
			title : '销售统计',
			layout : 'border',
//			closeAction : 'hide',
			resizable : false,
			modal : true,
			closable : false,
			constrainHeader : true,
			draggable : false,
			width : 1200,
			height : 500,
			items : [salesSubMune,salesSubGrid],
			buttons : [
			{
				text : '打印',
				disabled : true,
				handler : function(){
					
				}
			},
			{
				text : '退出',
				handler : function(){
					salesSubWin.close();
				}
			}
			],
			listeners : {
				show : function(){
//					Ext.getCmp('salesSubBtnSearch').handler();
				}
			}
		});
//	}
		
	salesSubWin.show();	
//	Ext.getCmp('salesSubBegDate').setValue('');
//	Ext.getCmp('salesSubEndDate').setValue('');
	Ext.getCmp('salesSubMuneTreeTypeRadioDept').setValue(true);
//	Ext.getCmp('salesSubMuneTree').root.reload();
	Ext.getCmp('salesSub_grid').getStore().removeAll();
	salesSubSetColumn();
};

salesSubSearchCheckDate = function(_s, _bid, _eid, _num){
	var beginDate = Ext.getCmp(_bid);
	var endDate = Ext.getCmp(_eid);
	var bdv = null, edv = null;
	var day = typeof(_num) != 'undefined' ? _num : 40;
	
	if(typeof(beginDate) == 'undefined' || typeof(endDate) == 'undefined'){
		return false;
	}
	
	bdv = beginDate.getValue();
	edv = endDate.getValue();
//	alert('222333444  '+Ext.ux.daysBetween(edv.format('Y-m-d'), bdv.format('Y-m-d')));
	if(bdv == '' && edv == ''){
		return false;
	}else{		
		if(_s){
			if(edv == '' || bdv > edv ){
				endDate.setRawValue(beginDate.getRawValue());
			}else if((bdv.add(Date.DAY, day) < edv )){
				endDate.setRawValue(beginDate.getValue().add(Date.DAY, day).format('Y-m-d'));
			}
		}else if(!_s){
			if(bdv == '' || edv < bdv){
				beginDate.setRawValue(endDate.getRawValue());
			}else if((edv.add(Date.DAY, (day * -1)) > bdv )){
				beginDate.setRawValue(endDate.getValue().add(Date.DAY, (day * -1)).format('Y-m-d'));
			}
		}
	}
};

salesSubSetColumn = function(){
	var grid = Ext.getCmp('salesSub_grid');
	var colHide = true, colWidth = 0;				
	if(salesSubQueryType == 0){
		colHide = true;
		colWidth = 0;
	}else{
		colHide = false;
		colWidth = 80;
	}					
	grid.getColumnModel().setHidden(2, colHide);
	grid.getColumnModel().setHidden(3, colHide);
	grid.getColumnModel().setHidden(4, colHide);
	grid.getColumnModel().setColumnWidth(2, colWidth);
	grid.getColumnModel().setColumnWidth(3, colWidth);
	grid.getColumnModel().setColumnWidth(4, colWidth);
	
};

salesSubSetDisplay = function(_tree, _queryTypeName, _queryType, _orderType, _colName){
	Ext.getCmp('salesSubMuneTree').setDisabled(_tree);
	Ext.getDom('salesSubShowType').innerHTML = _queryTypeName;
	salesSubQueryType = _queryType;
	Ext.getCmp('salesSubGridOrderByRadioSales').setDisabled(_orderType);
	Ext.getCmp('salesSub_grid').getColumnModel().setColumnHeader(1, _colName);
};

