/**
 * 
 */
function addNewFoodMaterial() {
	var materialData = Ext.ux.getSelData(materialBasicGrid);
	foodMaterialControlCenter({
		materialId : materialData['id'],
		materialName : materialData['name'],
		materialCateName : materialData['cateName'],
		count : 1
	});
	
}
/**
 * 
 * @param stroc
 * @param reocrd
 */
function deleteFoodMaterialHandler(stroc, reocrd){
	Ext.Msg.show({
		title : '提示',
		msg : '是否删除该原料',
		buttons : Ext.Msg.YESNO,
		icon : Ext.MessageBox.QUESTION,
		fn : function(e){
			if(e=='yes'){
				stroc.remove(reocrd);
			}
		}
	});
}
/**
 * 
 */
function operateFoodMaterialCount(c){
	var m = Ext.menu.MenuMgr.get('menuFoodMaterialCount');
	if(m){
		m.showAt([c.x, c.y]);
	}
}
/**
 * 
 * @param c
 */
function foodMaterialControlCenter(c) {
	if(c == null || typeof c == 'undefined' || (typeof c.count == 'undefined' && typeof c.otype == 'undefined')){
		Ext.example.msg('提示', '操作失败, 系统参数错误, 请联系客服人员.');
		return;
	}
	var foodData = Ext.ux.getSelData(foodBasicGrid);
	var fmStore = foodMaterialGrid.getStore();
	var data = foodMaterialGrid.getSelectionModel().getSelected();
	
	if (!foodData) {
		Ext.example.msg('提示', '请选中一道菜品再进行操作!');
		return;
	}
	// 删除操作, 优先处理
	if(c.otype == Ext.ux.otype['delete']){
		deleteFoodMaterialHandler(fmStore, data);
		return;
	}
	
	var mid = 0;
	if(typeof c.materialId == 'undefined'){
		mid = data.get('materialId');
	}else{
		mid = c.materialId;
	}
	var hasRecord = false;
	var sindex = 0;
	if (fmStore.getCount() > 0) {
		var temp = null;
		for ( var i = 0; i < fmStore.getCount(); i++) {
			temp = fmStore.getAt(i);
			if (temp.get('materialId') == mid) {
				hasRecord = true;
				if(c.otype == Ext.ux.otype['set']){
					// 直接设置
					temp.set('consumption', c.count);
					temp.commit();
					sindex = i;
				}else{
					if((temp.get('consumption') + c.count) <= 0){
						deleteFoodMaterialHandler(fmStore, temp);
					}else{
						temp.set('consumption', temp.get('consumption') + c.count);
						temp.commit();
						sindex = i;
					}
				}
				break;
			}
		}
	}
	if (!hasRecord) {
		fmStore.add(new FoodMaterialRecord({
			foodId : foodData['id'],
			materialId : mid,
			materialName : c.materialName,
			materialCateName : c.materialCateName,
			consumption : c.count
		}));
		sindex = fmStore.getCount() - 1;
	}
	foodMaterialGrid.getSelectionModel().selectRow(sindex);
}

