function editTaste(id, alias_id, preference, price, rate,calc,category) {
    var editType = "addTaste";
    var title = "添加口味";
    if (id != "") {
        editType = "editTaste";
        title = "修改口味";        
    }
    var aliasId = "";
    if (id == "") {
        aliasId = '<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" ' + 
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />'
	    calc = "0";
	    category = "0";
    }
    else {
        aliasId = '<input type="hidden" id="alias_id" name="alias_id" value="' + alias_id + '"/>' + alias_id + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="tasteForm" action="taste.php"  method="post" onkeydown="editTasteKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +	                     
	                      '<div class="add_foot_Content" style="height:230px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;编号：' + aliasId + '</div>' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;口味：<input type="text" id="preference" name="preference" value="' + preference + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;价格：<input type="text" id="price" name="price" value="' + price + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +  
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +	         
	                                       '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;比例：<input type="text" id="rate" name="rate" value="' + rate + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +  
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +	        
	                               '<div class="pop_Content1">计算方式：<select style="width: 90px;position: relative;" id="calc" name="calc" ' +
	                            '><option value="0">按价格</option><option value="1">按比例</option></select></div>' +
	                               '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类别：<select style="width: 90px;position: relative;" id="category" name="category" ' +
	                            '><option value="0">口味</option><option value="1">做法</option><option value="2">规格</option></select></div>' +                     
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitTasteData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    if (id == "") {
        document.getElementById("alias_id").focus();
    }
    else {
        document.getElementById("preference").focus();
    }
    document.all.calc.value = calc;
    document.all.category.value = category;
    
}

function submitTasteData() {  
    var alias_id = document.getElementById("alias_id").value;
    var preference = document.getElementById("preference").value;
    var price = document.getElementById("price").value;
    if (alias_id == undefined || alias_id == null || alias_id == "") {
        alert("编号不能为空！");
        document.getElementById("alias_id").focus();
        return;
    }
    if (preference == undefined || preference == null || preference == "") {
        alert("口味不能为空！");
        document.getElementById("preference").focus();
        return;
    }
    if (price == undefined || price == null || price == "") {
        alert("价格不能为空！");
        document.getElementById("price").focus();
        return;
    }
    var id = parseInt(alias_id);
    if (id > 255 || id == 0) {
        alert("编号的输入范围是1~255");
        document.getElementById("price").focus();
        return;
    }
    document.tasteForm.submit();
}

function editTasteKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitTasteData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function deleteTaste(id) {
    if (confirm("确认删除？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "taste.php";
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "id";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteTaste";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }

}

function showHideCondition(select) {
    //    alert(select);
    var option = select.options[select.selectedIndex];
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";
    if (option.value == "price") {
        document.getElementById("condition_type").style.display = "inline";
    }
}
function initializeTaste() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var condition_type = document.getElementById("condition_type");
    var condition_type_value = document.getElementById("condition_type_value").value;  
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
    keyword.value = keyword_value;
    if (keyword_type_value == "price") {
        document.getElementById("condition_type").style.display = "inline";
    }   
}