function editStaff(id, alias_id, name, pwd,random) {
    var editType = "addStaff";
    var title = "添加员工";
    if (id != "") {
        editType = "editStaff";
        title = "修改员工";
    }
    var aliasId = "";
    if (id == "") {
        aliasId = '<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />'
    }
    else {
        aliasId = '<input type="hidden" id="alias_id" name="alias_id" value="' + alias_id + '"/>' + alias_id + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="staffForm" action="staff.php"  method="post" onkeydown="editStaffKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<input type="hidden" id="random" name="random" value="' + random + '" />' +                
	                      '<div class="add_foot_Content" style="height:180px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">编号：' + aliasId + '</div>' +
	                            '<div class="pop_Content1">姓名：<input type="text" id="name" name="name" value="' + name + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;密码：<input type="password" id="pwd" name="pwd" value="' + pwd + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                            '<div class="pop_Content1">确认密码：<input type="password" id="confirm_pwd" name="confirm_pwd" value="' + pwd + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitStaffData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
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

function submitStaffData() {
    var alias_id = document.getElementById("alias_id").value;
    var name = document.getElementById("name").value;
    var pwd = document.getElementById("pwd").value;
    var confirm_pwd = document.getElementById("confirm_pwd").value;
    if (alias_id == undefined || alias_id == null || alias_id == "") {
        alert("编号不能为空！");
        document.getElementById("alias_id").focus();
        return;
    }
    if (name == undefined || name == null || name == "") {
        alert("姓名不能为空！");
        document.getElementById("name").focus();
        return;
    }
    if (pwd == undefined || pwd == null || pwd == "") {
        alert("密码不能为空！");
        document.getElementById("pwd").focus();
        return;
    }
    if (confirm_pwd == undefined || confirm_pwd == null || confirm_pwd == "") {
        alert("确认密码不能为空！");
        document.getElementById("confirm_pwd").focus();
        return;
    }
   
    if (pwd != confirm_pwd) {
        alert("两次输入的密码不一致！");
        document.getElementById("confirm_pwd").focus();
        return;
    }
    document.staffForm.submit();
}

function editStaffKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitStaffData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function deleteStaff(id) {
    if (confirm("确认删除？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "staff.php";
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "id";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteStaff";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }

}

function showHideCondition(select) {
    //    alert(select);
    var option = select.options[select.selectedIndex];   
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";   
}
function initializeStaff() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var keyword = document.getElementById("keyword");
    var keyword_value = document.getElementById("keyword_value").value;
    for (var i = 0; i < keyword_type.options.length; i++) {
        if (keyword_type.options[i].value == keyword_type_value) {
            keyword_type.options[i].selected = true;
            break;
        }
    }  
    keyword.value = keyword_value; 
}