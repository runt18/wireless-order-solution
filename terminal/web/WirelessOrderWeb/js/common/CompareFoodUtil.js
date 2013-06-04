/**
 * 对比菜品所有口味信息,包括普通口和临时口味
 * @param source
 * @param about
 * @returns {Boolean}
 */
function compareTasteGroup(source, about){
	if((source == null && about == null) || (typeof source == 'undefined' && typeof about == 'undefined')){
		return true;
	}
	var cs = true;
	var bn = source.normalTasteContent, an = about.normalTasteContent;
	var bt = source.tmpTaste, at = about.tmpTaste;
	// 对比菜品普通口味信息
	cs = cs ? compareNormalTaste(bn, an) : cs;
	// 对比临时口味
	cs = cs ? compareTempTaste(bt, at) : cs;
	return cs;
}

/**
 * 对比菜品普通口味信息
 * @param c1
 * @param c2
 * @returns {Boolean}
 */
function compareNormalTaste(c1, c2){
	if(c1 == null && c2 == null){
		return true;
	}
	if(c1 == null || c2 == null || typeof c1 == 'undefined' || typeof c2 == 'undefined'){
		return false;
	}
	var cs = true;
	if(c1.length == 0 && c2.length == 0){
		cs = true;
	}else if(c1.length != c2.length){
		cs = false;
	}else if(c1.length == c2.length){
		c1.sort(function(a, b){
			return eval(a['id'] > b['id']) ? 1 : -1;
		});
		c2.sort(function(a, b){
			return eval(a['id'] > b['id']) ? 1 : -1;
		});
		for(var i = 0; i < c1.length; i++){
			if(eval(c1[i]['id'] != c2[i]['id'])){
				cs = false;
				break;
			}
		}
	}
	return cs;
}
/**
 * 对比临时口味
 * @param source
 * @param about
 * @returns {Boolean}
 */
function compareTempTaste(source, about){
	if((source == null && about == null) || (typeof source == 'undefined' && typeof about == 'undefined')){
		return true;
	}
	if(source == null || about == null || typeof source == 'undefined' || typeof about == 'undefined'){
		return false;
	}
	var cs = false;
	cs = source['id'] == about['id'];
	return cs;
}

/**
 * 对比菜品数据类型, 已点菜, 新点菜
 * @param source
 * @param about
 * @param dataType
 * @returns
 */
function compareDataType(source, about, dataType){
	if((source == null && about == null) || (typeof source == 'undefined' && typeof about == 'undefined')){
		return true;
	}
	if(source == null || about == null || typeof source == 'undefined' || typeof about == 'undefined'){
		return false;
	}
	if(eval(source['id'] == about['id']  && source['dataType'] == about['dataType'])){
		if(typeof dataType == 'number')
			return eval(source['dataType'] == dataType);
		else
			return true;
	}else{
		return false;
	}
};