/**
 * 自定义验证表达式以及错误提示
 * 每个验证表达式都为一个json对象,键为类型,键的值又为一个json对象,
 * reg为验证的正则表达式,error为错误时提示信息,
 * 使用自定义验证表达式时如果没有设置错误提示则使用默认提示
 * @type 
 */
Ext.ux.RegText = {
	defaults : { reg : '' , error :'操作失败,请按提示操作!'},
	isNull : { reg : '' , error : '该项不能为空,请重新输入!'},
	exChar : { reg : /^[^(<|>|;|,|.|'|&|%|\\\\|~|!|@|#|$|%|\^|\*|\(|\)|=|+|{|}|\[|\]|\?|"|:|\-)]*?$/ , error : '不能包含特殊字符,请重新输入!'},
	basicFormat : { reg : /^[a-zA-Z][a-zA-Z0-9_]{4,15}$/ , error : '字母开头，允许5-16字节，允许字母数字下划线'},
	idCard : { reg : /^(\d{18,18}|\d{15,15}|\d{17,17}x)$/ , error : '身份证号只能是15位或18位数字'},
	email : { reg : /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/ , error : 'email格式不正确,例:xxxxx@xx.xxx'},
	mobile : { reg : /^1[3,5,8]\d{9}$/ , error : '请输入正确格式的11位手机号码'},
	password : { reg : /^[a-zA-Z0-9]{5,15}$/ , error : '请输入5-15位密码'},
	tel : { 
		reg : /((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/ 
		, error : '电话号码格式不正确,请重新输入!\n例:XXXX-XXXXXXXX'
	},
	phone : {
		reg : /^(139|138|137|136|135|134|147|150|151|152|157|158|159|182|183|187|188|130|131|132|155|156|185|186|145|133|153|180|181|189)\d{8}$/,
		error : '手机号格式不正确'
	}
};

/**
 * 
 * @param {} _id
 */
Ext.ux.getSelData = function(_component){
	if(typeof _component == 'string'){
		var score_grid = Ext.getCmp(_component);
		if(!score_grid){
			return false;
		}else{
			var records = score_grid.getSelectionModel().getSelections();
			if(records.length == 0 || records.length > 1){
				return false;
			}
			return records[0].data;
		}
	}else if(typeof _component == 'object'){
		var records = _component.getSelectionModel().getSelections();
		if(records.length == 0 || records.length > 1){
			return false;
		}
		return records[0].data;
	}else{
		return false;
	}
};

Ext.ux.getSelNode = function(_component){
	if(typeof _component == 'string'){
		var treePanelNodes = Ext.getCmp(_component);
		if(!treePanelNodes){
			return false;
		}else{
			var node = floatBarNodeId==""?treePanelNodes.getSelectionModel().getSelectedNode():treePanelNodes.getNodeById(floatBarNodeId);

			return node;
		}
	}else if(typeof _component == 'object'){
		var node = floatBarNodeId==""?_component.getSelectionModel().getSelectedNode():_component.getNodeById(floatBarNodeId);
		return node;
	}else{
		return false;
	}
};

/**
 * 
 */
Ext.ux.checkDateForBeginAndEnd = function(_s, _bid, _eid){
	var beginDate = Ext.getCmp(_bid);
	var endDate = Ext.getCmp(_eid);
	var bdv = null, edv = null;
	
	if(typeof(beginDate) == 'undefined' || typeof(endDate) == 'undefined'){
		return false;
	}
		
	bdv = beginDate.getRawValue();
	edv = endDate.getRawValue();
	if(bdv == '' && edv == ''){
		return false;
	}else{
		bdv = bdv.replace(/-/g, '');
		edv = edv.replace(/-/g, '');
		
		if(_s){
			if(edv == '' || bdv > edv){
				endDate.setRawValue(beginDate.getValue().format('Y-m-d'));
			}
		}else if(!_s){
			if(bdv == '' || edv < bdv){				
				beginDate.setRawValue(endDate.getValue().format('Y-m-d'));
			}
		}
	}
};

Ext.ux.checkBAE = Ext.ux.checkDateForBeginAndEnd;

/**
 * 求两个时间的天数差 日期格式为 YYYY-MM-dd   
 */
Ext.ux.daysBetween = function(DateOne,DateTwo){
	var OneMonth = DateOne.substring(5, DateOne.lastIndexOf('-'));  
    var OneDay = DateOne.substring(DateOne.length, DateOne.lastIndexOf('-')+1);  
    var OneYear = DateOne.substring(0, DateOne.indexOf('-'));  
    
    var TwoMonth = DateTwo.substring(5, DateTwo.lastIndexOf('-'));  
    var TwoDay = DateTwo.substring(DateTwo.length, DateTwo.lastIndexOf('-')+1);  
    var TwoYear = DateTwo.substring(0, DateTwo.indexOf('-'));  
  
    var cha=((Date.parse(OneMonth + '/' + OneDay + '/' + OneYear) - Date.parse(TwoMonth + '/' + TwoDay + '/' + TwoYear)) / 86400000);   
    return Math.abs(cha);
};

/**
 * 
 */
Ext.ux.checkDuft = function(_s, _bid, _eid, _num){
	var beginDate = Ext.getCmp(_bid);
	var endDate = Ext.getCmp(_eid);
	var bdv = null, edv = null;
	var day = typeof(_num) != 'undefined' ? _num : 40;
	
	if(typeof(beginDate) == 'undefined' || typeof(endDate) == 'undefined'){
		return false;
	}
	
	bdv = beginDate.getValue();
	edv = endDate.getValue();
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

Ext.ux.smsModule = false;
Ext.ux.smsCount = 0;
Ext.ux.checkSmStat = function(){
	Ext.Ajax.request({
		url : "../../QueryModule.do",
		params : {
			dataSource : 'checkModule',
			code : 4000
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.ux.smsModule = true;
				Ext.ux.smsCount = jr.code;
			}else{
				Ext.ux.smsModule = false;
			}
		},
		failure : function(res, opt){
		}
	});
}
Ext.ux.getCookie = function(cookieName) {
	var result = "";
	var mycookie = document.cookie;
	var start2 = mycookie.indexOf(cookieName + "=");
	if (start2 > -1) {
		start = mycookie.indexOf("=", start2) + 1;
		var end = mycookie.indexOf(";", start);
	
		if (end == -1) {
			end = mycookie.length;
		}
		//截取cookie
		result = unescape(mycookie.substring(start, end));
	}
	return result;
};

Ext.ux.setCookie = function(cookieName, cookieValue, days) {
	var date = new Date();
	date.setTime(date.getTime() + Number(days) * 3600 * 1000 * 24);
	
	document.cookie = cookieName + "=" + cookieValue + ";domain=" + document.domain + ";path=/;expires = " + date.toGMTString();
};

