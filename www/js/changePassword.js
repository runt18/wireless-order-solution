function changePassword(newPwd,pwd2,random) {       
    var content = ' <div class="add_foot">' +
                    '<div class="title">' +
	                    '<div class="title_left"><font class="font">修改密码</font></div>' +
	                    '<div class="title_right"></div>' +
	                '</div>' +
	                '<form name="changePasswordForm" action=""  method="post" onkeydown="KeyDown()">' +
	                      '<input type="hidden" id="isChangePassword" name="isChangePassword" value="true" />' +
	                      '<input type="hidden" id="random" name="random" value="' + random + '" />' +
	                      '<div class="add_foot_Content" style="height:200px;">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;旧密码：<input type="password" id="oldPassword" name="oldPassword" size="25" height="20" style="width:140px" onfocus="this.select()" /></div>' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;新密码：<input type="password" id="newPassword" name="newPassword" size="25" height="20" style="width:140px" onfocus="this.select()" value="' + newPwd + '" /></div>' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;确认新密码：<input type="password" id="confirmPassword" name="confirmPassword" size="25" height="20" style="width:140px" onfocus="this.select()" value="' + newPwd + '" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:51px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;权限密码：<input type="password" id="pwd2" name="pwd2" size="25" height="20" style="width:140px;" onfocus="this.select()" value="' + pwd2 + '" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:51px">&nbsp;确认权限密码：<input type="password" id="confirm_pwd2" name="confirm_pwd2" size="25" height="20" style="width:140px;" onfocus="this.select()" value="' + pwd2 + '" /></div>' +
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
    var pwd2 = document.getElementById("pwd2").value;
    var confirm_pwd2 = document.getElementById("confirm_pwd2").value;
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
//    if (pwd2 == undefined || pwd2 == null || pwd2 == "") {
//        alert("权限密码不能为空！");
//        return false;
//    }
    if (confirmPassword != newPassword) {
        alert("两次输入的新密码不一致！");
        return false;
    }
    if (pwd2 != confirm_pwd2) {
        alert("两次输入的权限密码不一致！");
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

function editRestaurant(id, restaurant_name, address, tele1, tele2) {
    var editType = "editRestaurant";
    var title = "修改餐厅信息";

    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="restaurantForm" action=""  method="post" onkeydown="editRestaurantKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:180px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">&nbsp;&nbsp;名称：<input type="text" id="restaurant_name" name="restaurant_name" value="' + restaurant_name + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                             '<div class="pop_Content1">&nbsp;&nbsp;地址：<input type="text" id="address" name="address" value="' + address + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                              '<div class="pop_Content1">电话1：<input type="text" id="tele1" name="tele1" value="' + tele1 + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                             '<div class="pop_Content1">电话2：<input type="text" id="tele2" name="tele2" value="' + tele2 + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitRestaurantData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("restaurant_name").focus();
}

function submitRestaurantData() {
    var restaurant_name = document.getElementById("restaurant_name").value;
//    var address = document.getElementById("address").value;
//    var tele1 = document.getElementById("tele1").value;
//    var tele2 = document.getElementById("tele2").value;
    if (restaurant_name == undefined || restaurant_name == null || restaurant_name == "") {
        alert("名称不能为空！");
        document.getElementById("restaurant_name").focus();
        return;
    }
//    if (address == undefined || address == null || address == "") {
//        alert("地址不能为空！");
//        document.getElementById("address").focus();
//        return;
//    }
//    if (tele1 == undefined || tele1 == null || tele1 == "") {
//        alert("电话1不能为空！");
//        document.getElementById("tele1").focus();
//        return;
//    }
//    if (tele2 == undefined || tele2 == null || tele2 == "") {
//        alert("电话2不能为空！");
//        document.getElementById("tele2").focus();
//        return;
//    }
    document.restaurantForm.submit();
}

function editRestaurantKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitRestaurantData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}
