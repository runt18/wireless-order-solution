function editKitchen(id, alias_id, name, discount1, discount2, member_discount_1, member_discount_2) {
    var editType = "addKitchen";
    var title = "添加厨房";
    if (id != "") {
        editType = "editKitchen";
        title = "修改厨房";
    }
    var aliasId = "";
    if (id == "") {
        aliasId = '<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />';
        discount = "1.00";
        member_discount_1 = "1.00";
        member_discount_2 = "1.00";
    }
    else {
        aliasId = '<input type="hidden" id="alias_id" name="alias_id" value="' + alias_id + '"/>' + alias_id + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="kitchenForm" action="kitchen.php"  method="post" onkeydown="editKitchenKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +	                      
	                      '<div class="add_foot_Content" style="height:230px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1" style="padding-left:32px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;编号：' + aliasId + '</div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;名称：<input type="text" id="name" name="name" value="' + name + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">一般折扣1：<input type="text" id="discount_1" name="discount_1" value="' + discount1 + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                             '<div class="pop_Content1" style="padding-left:32px">一般折扣2：<input type="text" id="discount_2" name="discount_2" value="' + discount2 + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">会员折扣1：<input type="text" id="member_discount_1" name="member_discount_1" value="' + member_discount_1 + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">会员折扣2：<input type="text" id="member_discount_2" name="member_discount_2" value="' + member_discount_2 + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitKitchenData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    if (id == "") {
        document.getElementById("alias_id").focus();
    }
    else {
        document.getElementById("name").focus();
    }

}

function submitKitchenData() {
    var alias_id = document.getElementById("alias_id").value;
    var name = document.getElementById("name").value;
    var discount_1 = document.getElementById("discount_1").value;
    var discount_2 = document.getElementById("discount_2").value;
    var member_discount_1 = document.getElementById("member_discount_1").value;
    var member_discount_2 = document.getElementById("member_discount_2").value;
    if (alias_id == undefined || alias_id == null || alias_id == "") {
        alert("编号不能为空！");
        return;
    }
    if (name == undefined || name == null || name == "") {
        alert("名称不能为空！");
        return;
    }
    if (discount_1 == undefined || discount_1 == null || discount_1 == "") {
        alert("一般折扣1不能为空！");
        return;
    }
    if (discount_2 == undefined || discount_2 == null || discount_2 == "") {
        alert("一般折扣2不能为空！");
        return;
    }
    if (member_discount_1 == undefined || member_discount_1 == null || member_discount_1 == "") {
        alert("会员折扣1不能为空！");
        return;
    }
    if (member_discount_2 == undefined || member_discount_2 == null || member_discount_2 == "") {
        alert("会员折扣2不能为空！");
        return;
    }
    var dis = parseFloat(discount_1);
    if (dis > 1 || dis < 0) {
        alert("一般折扣1的输入范围是0~1");
        document.getElementById("discount_1").focus();
        return;
    }
    dis = parseFloat(discount_2);
    if (dis > 1 || dis < 0) {
        alert("一般折扣2的输入范围是0~1");
        document.getElementById("discount_2").focus();
        return;
    }
    var dis1 = parseFloat(member_discount_1);
    if (dis1 > 1 || dis1 < 0) {
        alert("会员折扣1的输入范围是0~1");
        document.getElementById("member_discount_1").focus()
        return;
    }
    var dis2 = parseFloat(member_discount_2);
    if (dis2 > 1 || dis2 < 0) {
        alert("会员折扣2的输入范围是0~1");
        document.getElementById("member_discount_2").focus()
        return;
    }
    document.kitchenForm.submit();
}

function editKitchenKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitKitchenData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function deleteKitchen(id) {
    if (confirm("确认删除？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "kitchen.php";
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "id";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteKitchen";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }

}

function showHideCondition(select) {    
    var option = select.options[select.selectedIndex];
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";
    if (option.value == "discount1" || option.value == "discount2" || option.value == "member_discount1" || option.value == "member_discount2") {
        document.getElementById("condition_type").style.display = "inline";
    }
}
function initializeKitchen() {
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
    if (keyword_type_value == "discount1" || keyword_type_value == "discount2" || keyword_type_value == "member_discount1" || keyword_type_value == "member_discount2") {
        document.getElementById("condition_type").style.display = "inline";
    }
}