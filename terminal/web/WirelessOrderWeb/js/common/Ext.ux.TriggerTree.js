Ext.ux.TriggerTree = Ext.extend(Ext.form.TriggerField, {
	triggerClass: 'x-form-ref-trigger',
	initComponent: function(){
		Ext.ux.TriggerTree.superclass.initComponent.call(this);
		this.tree = new Ext.tree.TreePanel({
			width : 200,
			height : 280,
			autoScroll : true,
			rootVisible : true,
			frame : true,
			bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
			loader : new Ext.tree.TreeLoader({
				dataUrl : '../../QueryClientTypeTree.do',
				baseParams : {
					restaurantID : restaurantID
				}
			}),
			root : new Ext.tree.AsyncTreeNode({
				expanded : true,
				text : '全部类型',
			    leaf : false,
			    border : true
			})
		});
		this.menu = new Ext.menu.Menu({
			items : [new Ext.menu.Adapter(this.tree)]
 		});
		this.tree.on('click', this.treeClick, this);
	},
	onTriggerClick : function(){
		this.menu.show(this.el, 'tl-bl?');
	},
	treeClick : function(e){
		if (!this.tree.rendered||this.isLoading) {
            return null;
        }
		this.setValue(e.text);
	},
	setValue : function(val){
		this.value = val;
		Ext.ux.TriggerTree.superclass.setValue.call(this, val); 
		this.lastSelectionText = val;
		return this;
	},
	getValue : function(node){
		
	},
	setRawValue : function(node){
		
	},
	getRawValue : function(node){
		
	}
});