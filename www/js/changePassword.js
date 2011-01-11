function changePassword(newPwd) {       
    var content = ' <div class="add_foot">' +
                    '<div class="title">' +
	                    '<div class="title_left"><font class="font">修改密码</font></div>' +
	                    '<div class="title_right"></div>' +
	                '</div>' +
	                '<form name="changePasswordForm" action=""  method="post" onkeydown="KeyDown()">' +	   
	                      '<input type="hidden" id="isChangePassword" name="isChangePassword" value="true" />' +                   
	                      '<div class="add_foot_Content">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;旧密码：<input type="password" id="oldPassword" name="oldPassword" size="25" height="20" style="width:140px" onfocus="this.select()" /></div>' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;新密码：<input type="password" id="newPassword" name="newPassword" size="25" height="20" style="width:140px" onfocus="this.select()" value="' + newPwd + '" /></div>' +
	                            '<div class="pop_Content1">确认新密码：<input type="password" id="confirmPassword" name="confirmPassword" size="25" height="20" style="width:140px" onfocus="this.select()" value="' + newPwd + '" /></div>' +
	                        '</div>' +
	                        '<span class="pop_action-span"><a href="#" onclick="pSubmitData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                        '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                '</form>' +
	                '<div>';
    showMessageBox(content, 342, 350);
    document.getElementById("oldPassword").focus();

}

function pSubmitData() {
    if (pValidateData()) {
        document.changePasswordForm.submit();
    }
}

function pValidateData() {
    var oldPassword = document.getElementById("oldPassword").value;
    var newPassword = document.getElementById("newPassword").value;
    var confirmPassword = document.getElementById("confirmPassword").value;
    //    alert(foodCode);

    if (oldPassword == undefined || oldPassword == null || oldPassword == "") {
        alert("旧密码不能为空！");
        return false;
    }
    if (newPassword == undefined || newPassword == null || newPassword == "") {
        alert("新密码不能为空！");
        return false;
    }
    if (newPassword.length > 30) {
        alert("新密码的最大长度不能超过30个英文字符！");
        return false;
    }
    if (confirmPassword != newPassword) {
        alert("两次输入的新密码不一致！");
        return false;
    }  
    return true;
}

function KeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        pSubmitData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}
