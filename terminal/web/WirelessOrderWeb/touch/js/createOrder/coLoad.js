/**
 * 初始化部分选择区域
 * @param c
 */
function cr_initDeptContent(c){
	var dc = getDom('divSelectDeptForOrder');
	var html = '';
	for(var i = 0; i < deptData.root.length; i++){
		html += ('<div class="button-base" onClick="cr_initKitchenContent({deptId:' + deptData.root[i].id + '})">' + deptData.root[i].name + '</div>');
	}
	dc.innerHTML = html;
}
/**
 * 初始化分厨选择区域
 * @param c
 */
function cr_initKitchenContent(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	var kc = getDom('divSelectKitchenForOrder');
	var html = ('<div class="button-base">全部分厨</div>');
	for(var i = 0; i < kitchenData.root.length; i++){
		if(typeof c.deptId == 'number' && c.deptId != -1){
			if(kitchenData.root[i].dept.id == c.deptId){
				html += ('<div class="button-base">' + kitchenData.root[i].name + '</div>');
			}
		}else{
			if(kitchenData.root[i].dept.id != -1){
				html += ('<div class="button-base">' + kitchenData.root[i].name + '</div>');
			}
		}
	}
	kc.innerHTML = html;
}
/**
 * 
 * @param c
 */
function cr_initFoodContent(c){
	var fc = getDom('divCFCOAllFood');
	fc.innerHTML = '';
	var ch = fc.clientHeight, cw = fc.clientWidth;
//	alert(fc.clientHeight +'  :   '+ fc.clientWidth)
//	alert(ch / (80 + 2) + '   :   ' + cw / (80 + 2))
	var ps = parseInt((ch / (80 + 3))) * parseInt((cw / (90 + 3)));
	var html = '';
	for(var i = 0; i < ps/*foodData.root.length*/; i++){
		html += ('<div class="divCFCOAllFood-main-box">' + foodData.root[i].name + '</div>');
	}
	fc.innerHTML = html;
}
