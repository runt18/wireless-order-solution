Ext.BLANK_IMAGE_URL = "../../js/extjs/resources/images/default/s.gif";

var salesSubGrid = null;
var salesSubMune = null;
var salesSubWin = null;

salesSubPanelnit = function(){
	
	var cmData = [[true, false], ['部门',''], ['营业额',''], ['折扣额',''], ['赠送额',''],
			['成本',''], ['成本率',''], ['毛利',''], ['毛利率',''],
			['销量',''], ['均价',''], ['单位成本','']];
	var url = '../../SalesSubStatistics.do?tiem='+new Date();
	var readerData = ['name','card'];
	var baseParams = [];
	var pageSize = 10;
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
			{xtype:'tbtext',text:'类别:1231231'},
			{xtype:'tbtext',text:'&nbsp;&nbsp;'},	
			{xtype:'tbtext',text:'日期:'},
			new Ext.form.DateField({
//				xtype : "datefield",			
				format : "Y-m-d",
				id : "begDateMStatDept",
				width : 100,
				readOnly : true	
			}),
			{xtype:'tbtext',text:'&nbsp;&nbsp;至&nbsp;&nbsp;'},	
			{
				xtype : "datefield",
				format : "Y-m-d",
				id : "endDateMStatDept",
				width : 100,
				readOnly : true	
			},
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;&nbsp;'},
			{
				xtype : 'radio',
				hideLabel : true,
				width : 100,
				boxLabel : "按销量排序",
				name : 'salesSubGridBorderByRadio',
				inputValue : '1'
			},			
			{
				xtype : 'radio',
				hideLabel : true,
				width : 100,
				boxLabel : "按毛利排序",
				name : 'salesSubGridBorderByRadio',
				inputValue : '2'
			},
			'->',
			{
				text : '搜索',
				width : 150,
				handler : function(){
					salesSubGrid.getStore().reload();
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
			height : 22,
			items : [
			'->',
			{
				xtype : 'radio',
				hideLabel : true,
				width : 100,
				boxLabel : "分类",
				name : 'salesSubMuneTreeTypeRadio',
				inputValue : '1'
			},
			{
				xtype : 'radio',
				hideLabel : true,
				width : 100,
				boxLabel : "全部",
				name : 'salesSubMuneTreeTypeRadio',
				inputValue : '2'
			}
			]
		});
		
		var salesSubMuneTree = new Ext.tree.TreePanel({
			border : false,
			rootVisible : true,
			root: new Ext.tree.AsyncTreeNode({					
//	            expanded: true,
	            text:'全部',
	            leaf:false,
	            children: [{text:'子节点一',leaf:true},{id:'child2',text:'子节点二', leaf:false,children:[{text:"111"}]}] 
	        }),
	        tbar : salesSubMuneTree_tbar
		});
		
		salesSubMune = new Ext.Panel({
			region : 'west',
			width : 200,
			layout : 'fit',
			border : false,
			items : [salesSubMuneTree]
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
			width : 1000,
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
					salesSubWin.hide();
				}
			}
			],
			listeners : {
				show : function(){
//					salesSubGrid.getStroe().reload();
				}
			}
		});
	}
	salesSubWin.show();
};