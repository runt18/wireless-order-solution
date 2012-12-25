/**
 * 
 */
/**
function loadFoodMaterial() {
	var foodID = menuStore.getAt(currRowIndex).get("foodID");
	Ext.Ajax.request({
		url : "../../QueryFoodMaterial.do",
		params : {
			"pin" : pin,
			"foodID" : foodID
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if(eval(jsonResult.success) == true){
//				materialStore.loadData(jsonResult);
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}
 */

/**
 * 获得所有口味
 */
/**
loadAllTaste = function() {
	Ext.Ajax.request({
		url : '../../QueryTaste.do',
		params : {
			'pin' : pin,
			'type' : 0,
			'isCombo' : false,
			'isPaging' : false 
		},
		success : function(response, options) {
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.getCmp('allTasteGrid').getStore().loadData(jsonResult);
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.util.JSON.decode(response.responseText));
		}
	});
};
 */

setFiledDisabled = function(start, idList){
	var st = true;
	st = typeof(start) == 'boolean' ? start : st;
	for(var i = 0; i < idList.length; i++){
		var tp = Ext.getCmp(idList[i]);
		if(tp){
			tp.setDisabled(st);
		}
	}
};





