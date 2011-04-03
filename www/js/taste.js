function editTaste(id, alias_id, preference, price, old_alias_id) {
    var editType = "addTaste";
    var title = "添加口味";   
    if (id != "") {
        editType = "editTaste";
        title = "修改口味";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="tasteForm" action="taste.php"  method="post" onkeydown="editTasteKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<input type="hidden" name="old_alias_id" value="' + old_alias_id + '" />' +
	                      '<div class="add_foot_Content" style="height:180px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">序&nbsp;号：<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" style="position: relative; right: -20px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">口&nbsp;味：<input type="text" id="preference" name="preference" value="' + preference + '" onfocus="this.select()"  style="position: relative; right: -20px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">价&nbsp;格：<input type="text" id="price" name="price" value="' + price + '" onfocus="this.select()"  style="position: relative; right: -20px; width: 120px;"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +  
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +	                           
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitTasteData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
   
    document.getElementById("alias_id").focus();
}

function submitTasteData() {
    var alias_id = document.getElementById("alias_id").value;
    var preference = document.getElementById("preference").value;
    var price = document.getElementById("price").value;
    if (alias_id == undefined || alias_id == null || alias_id == "") {
        alert("序号不能为空！");
        return;
    }
    if (preference == undefined || preference == null || preference == "") {
        alert("口味不能为空！");
        return;
    }
    if (price == undefined || price == null || price == "") {
        alert("价格不能为空！");
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