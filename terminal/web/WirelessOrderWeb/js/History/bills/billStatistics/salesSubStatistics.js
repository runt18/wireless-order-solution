Ext.BLANK_IMAGE_URL = "../../js/extjs/resources/images/default/s.gif";

var salesSubGrid = null;
var salesSubMune = null;
var salesSubWin = null;
var salesSubQueryType = 0;
var salesSubOrderType = 0;
var salesSubDeptId = -1;

salesSubPanelnit = function(){
	
	salesSubQueryType = 0;
	salesSubOrderType = 0;
	salesSubDeptId = -1;
	
	var cmData = [[true, false, false, true], ['部门','item'], ['营业额','income','','right','Ext.ux.txtFormat.gridDou'], ['折扣额','discount','','right','Ext.ux.txtFormat.gridDou'], ['赠送额','gifted','','right','Ext.ux.txtFormat.gridDou'],
			['成本','cost','','right','Ext.ux.txtFormat.gridDou'], ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
			['销量','salesAmount','','right','Ext.ux.txtFormat.gridDou'], ['均价','avgPrice','','right','Ext.ux.txtFormat.gridDou'], ['单位成本','avgCost','','right','Ext.ux.txtFormat.gridDou']];
	var url = '../../SalesSubStatistics.do?tiem='+new Date();
	var readerData = ['item','income','discount','gifted','cost','costRate','profit','profitRate','salesAmount','avgPrice','avgCost'];
	var baseParams = [['pin', pin], ['restaurantID', restaurantID]];
	var pageSize = 15;
	var id = 'salesSub_grid';
	var title = '';
	var height = '';
	var width = '';
	var groupName = '';
	
	if(!salesSubGrid){	
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
				value : new Date().getFirstDateOfMonth(),
				width : 100,
				readOnly : true,
				listeners : {
					blur : function(){									
						Ext.ux.checkDateForBeginAndEnd(true, 'salesSubBegDate', 'salesSubEndDate', 40);
					}
				}
			},
			{xtype:'tbtext',text:'&nbsp;&nbsp;至&nbsp;&nbsp;'},	
			{
				xtype : 'datefield',
				format : 'Y-m-d',
				id : 'salesSubEndDate',
				width : 100,
				value : new Date(),
				readOnly : true,
				listeners : {
					blur : function(){									
						Ext.ux.checkDateForBeginAndEnd(false, 'salesSubBegDate', 'salesSubEndDate', 40);
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
					}
				}
			},						
			'->',
			{
				text : '搜索',
				id : 'salesSubBtnSearch',
				width : 150,
				handler : function(){
					
					Ext.ux.checkDateForBeginAndEnd(true, 'salesSubBegDate', 'salesSubEndDate', 40);
					Ext.ux.checkDateForBeginAndEnd(false, 'salesSubBegDate', 'salesSubEndDate',40);
					
					var gs = salesSubGrid.getStore();
					gs.baseParams['dateBeg'] = Ext.getCmp('salesSubBegDate').getRawValue();
					gs.baseParams['dataEnd'] = Ext.getCmp('salesSubEndDate').getRawValue();
					gs.baseParams['queryType'] = salesSubQueryType;
					gs.baseParams['orderType'] = salesSubOrderType;
					gs.baseParams['deptID'] = salesSubDeptId;
					gs.load({params:{start:0,limit:15}});
				}
			}
			]
		});
		
		salesSubGrid = createGridPanel(id,title,height,width,url,cmData,readerData,baseParams,pageSize,groupName,salesSubGrid_tbar);
		salesSubGrid.region = 'center';		
	}
	
	if(!salesSubMune){
		var salesSubMuneTree_tbar = new Ext.Toolbar({
			buttonAlign : 'left',
			height : 23,
			items : [
			'->',
			{
				xtype : 'radio',
				hideLabel : true,
				width : 90,
				boxLabel : '部门分类',
				name : 'salesSubMuneTreeTypeRadio',
				inputValue : '0',
				checked : true,
				listeners : {
					check : function(e){
						if(e.getValue() == true){
							Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);						
							Ext.getCmp('salesSubGridOrderByRadioSales').disable();
							salesSubMuneTree.enable();
							salesSubQueryType = e.getRawValue();
							Ext.getDom('salesSubShowType').innerHTML = e.boxLabel;
							
							salesSubGrid.getColumnModel().setHidden(9,true);
							salesSubGrid.getColumnModel().setHidden(10,true);
							salesSubGrid.getColumnModel().setHidden(11,true);
							
							salesSubGrid.getColumnModel().setColumnHeader(1, '部门');
							
						}
					}
				}
			},
			{
				xtype : 'radio',
				hideLabel : true,
				width : 90,
				boxLabel : '全部菜品',
				name : 'salesSubMuneTreeTypeRadio',
				inputValue : '1',
				listeners : {
					check : function(e){
						if(e.getValue() == true){
							Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);						
							Ext.getCmp('salesSubGridOrderByRadioSales').enable();
							salesSubMuneTree.disable();
							salesSubQueryType = e.getRawValue();
							Ext.getDom('salesSubShowType').innerHTML = e.boxLabel;
							
							salesSubGrid.getColumnModel().setHidden(9, false);
							salesSubGrid.getColumnModel().setHidden(10, false);
							salesSubGrid.getColumnModel().setHidden(11, false);
							
							salesSubGrid.getColumnModel().setColumnHeader(1, '菜品');
							
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
			loader:new Ext.tree.TreeLoader({    
		          dataUrl:'../../QueryDeptTree.do?time='+new Date(),
		          baseParams : {
						'restaurantID' : restaurantID
					}
		       }),
			root: new Ext.tree.AsyncTreeNode({
				expanded : true,
	            text : '全部',
	            leaf : false
			}),
	        listeners : {
	        	click : function(e){
	        		salesSubDeptId = e.id;
	        	}
	        }
		});
		salesSubMune = new Ext.Panel({
			region : 'west',
			width : 200,			
			border : false,
			items : [{xtype:'panel',tbar:salesSubMuneTree_tbar, border:false}, salesSubMuneTree]
		});
	}	
};

salesSub = function(){
	
	salesSubPanelnit();
	
	if(!salesSubWin){
		salesSubWin = new Ext.Window({
			title : '销售统计',
			layout : 'border',
			closeAction : 'hide',
			resizable : false,
			modal:true,
			closable:false,
			constrainHeader:true,
			draggable:false,
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
					Ext.getCmp('salesSubBtnSearch').handler();
				}
			}
		});
	}
	
	
	salesSubWin.show();
	
	Ext.getCmp('salesSubMuneTree').root.reload();
};