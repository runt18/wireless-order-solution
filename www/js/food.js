function deleteFood(id)
{
    if(confirm("确认删除？"))
    {
        var formDelete = document.createElement("form");
        formDelete.action = "food.php";
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "deleteId";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }
    
}
function editFood(fId, fCode, fName, fPrice, kitchen, kitchens) {
    var ks = "";
    var arr1 = kitchens.split("@");
    for (var i = 0; i < arr1.length; i++) {
        var arr2 = arr1[i].split("|");
        ks += (' <option value="' + arr2[0] + '">' + arr2[1] + '</option>');
    }
        var titleName;
    var isDisable = "";
	if(fId == "")
	{
	    titleName = "添加新菜";	    
	}
	else
	{
	    titleName = "修改菜谱";
	    isDisable = 'readonly = "readonly"';
	}
	var foodCode = "";
	if (fId == "") {
	    foodCode = '<input type="text" id="foodCode" name="foodCode" value="' + fCode + '" size="25" height="20" ' + isDisable +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />'
	}
	else {
	    foodCode = '<input type="hidden" id="foodCode" name="foodCode" value="' + fCode + '"/>' + fCode + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	}
   
    var content = ' <div class="add_foot">' +
                    '<div class="title">' +
	                    '<div class="title_left"><font class="font">' + titleName + '</font></div>' +
	                    '<div class="title_right"></div>' +
	                '</div>' +
	                '<form name="foodForm" action="food.php"  method="post" onkeydown="foodKeyDown()">' +
	                      '<input type="hidden" name="foodId" value="' + fId + '" />' +
	                      '<div class="add_foot_Content" style="height:185px;">' + 
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">编号：'+ foodCode + '</div>' +
	                            '<div class="pop_Content1">菜名：<input type="text" id="foodName" name="foodName" value="' + fName + '" size="25" height="20"/></div>' +
	                            '<div class="pop_Content1">价格：<input type="text" id="foodPrice" name="foodPrice" value="' + fPrice + '" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +  
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' + 
								'<div class="pop_Content1">厨房：<select id="kitchenSelect" name="kitchenSelect" style="width:85px;">' +
                                ks +								
								'<option value="255">空</option></select></div>' +	                          
	                        '</div>' +
	                        '<span class="pop_action-span"><a href="#" onclick="submitFoodData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                        '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                '</form>' +
	                '</div>';
    showMessageBox(content, 342, 350);
    if (fId == "") {
        document.getElementById("foodCode").focus();
    }
    else {
        document.getElementById("foodName").focus();
    }
//    alert(kitchen);
    document.all.kitchenSelect.value = kitchen;
}

function foodKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitFoodData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function clearNoNum(obj) {
    obj.value = obj.value.replace(/[^\d.]/g, "");  //清除“数字”和“.”以外的字符 
    obj.value = obj.value.replace(/^\./g, "");  //验证第一个字符是数字而不是. 
    obj.value = obj.value.replace(/\.{2,}/g, "."); //只保留第一个. 清除多余的. 
    obj.value = obj.value.replace(".", "$#$").replace(/\./g, "").replace("$#$", ".");
}

function submitFoodData()
{
    if(validateFoodData())
    {
        document.foodForm.submit();
    }
}

function validateFoodData() {
    var foodCode = document.getElementById("foodCode").value;
    var foodName = document.getElementById("foodName").value;
    var foodPrice = document.getElementById("foodPrice").value;
    //    alert(foodCode);
    
    if (foodCode == undefined || foodCode == null || foodCode == "") {
        alert("编号不能为空！");
        document.getElementById("foodCode").focus();
        return false;
    }
    if (foodName == undefined || foodName == null || foodName == "") {
        alert("菜名不能为空！");
        document.getElementById("foodName").focus();
        return false;
    }
    if (foodPrice == undefined || foodPrice == null || foodPrice == "") {
        alert("价格不能为空！");
        document.getElementById("foodPrice").focus();
        return false;
    }
    var fc = parseInt(foodCode);
//    alert(fc);
    if (fc > 65535) {
        alert("编号只能输入0~65535之间的数字！")
        return false;
    }
    return true;
}
function showHideCondition(select) {
//    alert(select);
    var option = select.options[select.selectedIndex];
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("kitchen").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";
    if (option.value == "is_Price") {
        document.getElementById("condition_type").style.display = "inline";
    }
     if (option.value == "is_kitchen") {
        document.getElementById("kitchen").style.display = "inline";
         document.getElementById("keyword").style.display = "none";
    }
    
}
function initializeFood() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var condition_type = document.getElementById("condition_type");
    var condition_type_value = document.getElementById("condition_type_value").value;
    var kitchen = document.getElementById("kitchen");
    var kitchen_value = document.getElementById("kitchen_value").value;
    var keyword = document.getElementById("keyword");
    var keyword_value = document.getElementById("keyword_value").value;
    for (var i = 0; i < keyword_type.options.length; i++) {
        if (keyword_type.options[i].value == keyword_type_value) {
            keyword_type.options[i].selected = true;
            break;
        }
    }
    for (var i = 0; i < condition_type.options.length; i++) {
        if (condition_type.options[i].value == condition_type_value) {
            condition_type.options[i].selected = true;
            break;
        }
    }
    for (var i = 0; i < kitchen.options.length; i++) {
        if (kitchen.options[i].value == kitchen_value) {
            kitchen.options[i].selected = true;
            break;
        }
    }
    keyword.value = keyword_value;
    if (keyword_type_value == "is_Price") {
        document.getElementById("condition_type").style.display = "inline";
    }
    if (keyword_type_value == "is_kitchen") {
        document.getElementById("kitchen").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}

function showFoodRanked()
{
     var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                              '<div class="title" style="width:100%">' +
	                          '<div class="title_left" style="width:565px"><font id="titleName" class="font" style="width:260px">点菜统计</font></div>' +
	                          '<div class="title_right" style="width:35px;float:left" ></div>' +
	                          '</div>' +	               
	                          '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' + 	                        
	                              '<iframe src="foodRanked.php" scrolling="no" style="width:100%;height:100%" />' +	                           
	                          '</div>' +	                      
						   '</div>';
	showMessageBox(content, 600, 350);
	
}
function foodRankedKeyDown() {   
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}

function showFoodSearch() {    
    var content = '<div id="Advanced_Search">' +
                    '<div class="title">' +
                        '<div class="title_left"><font class="font" style="width:200px;">高级搜索（菜谱）</font></div>' +
                        '<div class="title_right"></div>' +
                    '</div>' +
                     '<form name="searchForm" action="admin_food.php"  method="post" onkeydown="foodSearchKeyDown()">' +
                    '<div class="Advanced_Search_Content" style="height:210px">' +
                        '<div class="pop_Content" >' +
                            '<div class="pop_Content2">菜名：<input type="text" id="food_name" name="food_name" size="12" height="20" style="width:120px;"/></div>' +
                            '<div class="pop_Content2">餐厅：<select id="search_restaurant_type" name="search_restaurant_type" ><option value="Code" selected="selected">编号</option><option value="Name">名称</option></select>&nbsp;'+
                            '<input type="text" name="restaurant" size="20" height="20" style="width:120px;"/></div>' +                                                  
                            '<div class="pop_Content2">单价：<select id="search_condition_type" name="search_condition_type" ><option value="Equal">等于</option><option value="EqualOrGrater" selected="selected">大于等于</option><option value="EqualOrLess">小于等于</option></select>&nbsp;'+
                                '<input type="text" id="unit_price" name="unit_price" style="width:120px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +                           
                        '</div>' +
                        '<span class="action-span"><a href="#"  onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                         '<span class="action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                    '</div>' +
                    '</form>' +
                '</div>';  
    showMessageBox(content, 540, 188);
    document.getElementById("food_name").focus();
}

function foodSearchKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        document.searchForm.submit();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}



//function showHideAdminCondition(select) {
////    alert(select);
//    var option = select.options[select.selectedIndex];
//    document.getElementById("condition_type").style.display = "none";
//    document.getElementById("kitchen").style.display = "none";
//    document.getElementById("keyword").style.display = "inline";
//    document.getElementById("keyword").value = "";
//    if (option.value == "is_Price") {
//        document.getElementById("condition_type").style.display = "inline";
//    }
//     if (option.value == "is_kitchen") {
//        document.getElementById("kitchen").style.display = "inline";
//         document.getElementById("keyword").style.display = "none";
//    }
//    
//}

//function iniAdminFood() {
//    var keyword_type = document.getElementById("keyword_type");
//    var keyword_type_value = document.getElementById("keyword_type_value").value;
//    var condition_type = document.getElementById("condition_type");
//    var condition_type_value = document.getElementById("condition_type_value").value;
//    var keyword = document.getElementById("keyword");
//    var keyword_value = document.getElementById("keyword_value").value;
//    for (var i = 0; i < keyword_type.options.length; i++) {
//        if (keyword_type.options[i].value == keyword_type_value) {
//            keyword_type.options[i].selected = true;
//            break;
//        }
//    }
//    for (var i = 0; i < condition_type.options.length; i++) {
//        if (condition_type.options[i].value == condition_type_value) {
//            condition_type.options[i].selected = true;
//            break;
//        }
//    }   
//    keyword.value = keyword_value;
//    if (keyword_type_value == "is_Price") {
//        document.getElementById("condition_type").style.display = "inline";
//    }   
//}


