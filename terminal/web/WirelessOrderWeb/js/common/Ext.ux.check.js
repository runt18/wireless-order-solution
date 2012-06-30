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
	mobile : { reg : /^1[3,5]\d{9}$/ , error : '请输入11位手机号码'},
	password : { reg : /^[a-zA-Z0-9]{5,15}$/ , error : '请输入5-15位密码'},
	phone : { 
		reg : /((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/ 
		, error : '电话号码格式不正确,请重新输入!\n例:XXXX-XXXXXXXX'
	}
};

/**
 * 批量验证
 * 验证方法,只做验证,不支持其他任何操作
 * @param {} 
 * jsonList : 自定义验证配置对象的集合,格式为数组
 * item : 支持配置
 * id : 被验证的控件id,不允许为空
 * rtype : 验证类型,默认为 null,只作判空验证
 * reg : 自定义表达式,与 rtype不兼容
 * error : 自定错误提示,与reg绑定,可为空         
 * 
 */
Ext.ux.RegCheck = function(jsonList){
	
	if(jsonList == null || typeof(jsonList.length) == 'undefined' || jsonList.length == 0){
		return true;
	}
	
	var state = true;
	
	var sr = Ext.ux.RegText;
	// "空约束"检查
	var check = function(val){
		// 机制处理(不管任何情况下都先将值转为字符串)
		val += '';	
		// 去空格
		val = val.replace(/(^\s*)|(\s*$)/g, '');
				
		// ts -> tempState
		var ts = val == "" ? false : true;
		
		state = ts ? state : ts;
		return ts;
	};
	
	var fs = function(val){
		var ts = true;
		ts = val != null ? (val != 'undefined' ? ts : false) : false;
		return ts;
	};
		
	// 添加验证信息,obj[0]:Ext元素,obj[1]:错误提示
	var addError = function(obj){
		state = false;
		obj[0].isValid(false);
		if(fs(obj[1].error)){
			obj[0].markInvalid(obj[1].error);
		}else{
			obj[0].markInvalid(sr.defaults.error);
		}
	};
		
	for(var i = 0; i < jsonList.length; i++){
		// 获得单个配置对象
		var item = jsonList[i];
		
		// 如果找不到配置必须项则立刻终止操作
		if(item.id == null || item.id == 'undefined')
			item.id = '';
		// 得到被验证对象	
		var extObj = Ext.getCmp(item.id);
		// 如果被验证对象不存在则立刻终止操作
		if(extObj != null){
					
			// 清除上一次验证提示	
			extObj.clearInvalid();
						
			if(fs(item.handler) && typeof(item.handler) == 'function'){
				// 执行自定义验证方法,并把自己传下去(Ext对象)						
				if(check(extObj.getValue()) != false){
					// 检查用户是否设置通过验证
					state = eval(item.handler(extObj,state));
				}else{
					addError([extObj,sr.isNull]);
				}
			}else{						
				// 如果该控件value去空格为空则立刻终止操作并加错误提示
				if(check(extObj.getValue()) != false){					
					if(fs(item.rtype))	{
						// 获取可支持的验证工具集合
						var cr = sr[item.rtype];
						if(fs(cr) && fs(cr.error)){
							if(cr.reg.test(extObj.getValue()) == false){
								addError([extObj,cr]);	
							}
						}			
					}else{
						// 执行自定义表达式验证						
						if(fs(item.reg)){
							if(typeof(item.reg) != 'string'){
								if((item.reg).test(extObj.getValue()) == false){
									if(fs(item.error)){
										addError([extObj,item]);
									}else{
										addError([extObj]);
									}
								}
							}
						}
					}
				}else{			
					// 添加空检查,如果存在自定义错误提示则使用,否则加载默认
					if(fs(item.error)){
						addError([extObj,item]);
					}
					else{	
						addError([extObj,sr.isNull]);	
					}
				}
			}
		}
	}
	return state;
};


/**
 * 
 * @param {} _id
 */
Ext.ux.getSelData = function(_id){
	var score_grid = Ext.getCmp(_id);
	var records = score_grid.getSelectionModel().getSelections();
	if(records.length==0||records.length>1){		
		Ext.Msg.show({title:'提示',autoWidth:true,msg:'操作提示,请选中一条数据，再进行操作！',buttons:Ext.Msg.OK});
		return false;
	}
	var index = records.length-1;
	
	return records[index].data;
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
			if(edv == '' || (bdv + 1) > edv){
				endDate.setRawValue(beginDate.getValue().add(Date.DAY, 1).format('Y-m-d'));
			}
		}else if(!_s){
			if(bdv == '' || (edv - 1) < bdv){
				beginDate.setRawValue(endDate.getValue().add(Date.DAY,-1).format('Y-m-d'));
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