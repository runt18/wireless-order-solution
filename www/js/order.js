
function showSearch() {

    //    var title = '<div class="title">' +
    //                    '<div class="title_left"><font class="font">高级搜索</font></div>' +
    //                    '<div class="title_right"></div>' +
    //                '</div>';
    //    var content = '<div class="Advanced_Search_Content">' +
    //                    '<div class="Content">' +
    //                        '<div class="Content1">日期：<input type="text" name="keyword" size="20" height="20"/>&nbsp;<img src="images/data.png" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="keyword" size="20" height="20"/>&nbsp;<img src="images/data.png" /></div>' +
    //                        '<div class="Content1">价格：<input type="text" name="keyword" size="20" height="20"/>&nbsp;<img src="images/data.png" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="keyword" size="20" height="20"/>&nbsp;<img src="images/data.png" /></div>' +
    //                        '<div class="Content2">台号：<input type="text" name="keyword" size="10" height="20"/></div>' +
    //                    '</div>' +
    //                    '<span class="action-span1"><a href="#">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
    //                    '<span class="action-span"><a href="#">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
    //                '</div>';
    var content = '<div id="Advanced_Search">' +
                    '<div class="title">' +
                        '<div class="title_left" style="width: 451px;"><font class="font">高级搜索</font></div>' +
                        '<div class="title_right" style="float:left"></div>' +
                    '</div>' +
                     '<form id="searchForm" name="searchForm" action="order.php"  method="post" onkeydown="searchOrderKeyDown()">' +
                    '<div class="Advanced_Search_Content" style="width:480px;">' +
                        '<div class="pop_Content">' +
                            '<div class="pop_Content1">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
                            '<div class="pop_Content1">金额：<input type="text" name="priceFrom" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="priceTo" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                            '<div class="pop_Content2">台号：<input type="text" name="alias_id" size="10" height="20" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                        '</div>' +
                        '<span class="action-span" style="margin-left:110px;"><a href="#"  onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                         '<span class="action-span1" style="margin-right:100px;"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                    '</div>' +
                    '</form>' +
                '</div>';
    showMessageBox(content, 540, 188);
    document.getElementById("dateFrom").focus();
    //    window.addEvent('domready', function() {
    //        myCal1 = new Calendar({ dateFrom: 'd/m/Y' }, { direction: 1, tweak: { x: 6, y: 0} });
    //    });
}


function searchOrderKeyDown() {
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
function showOrderDetail(id, alias, date, totalPrice, count, foods, isPaid,owner_name) {
    var f = "";
    var fs = foods.split(",");
    for (var i = 0; i < fs.length; i++) {
        var str = fs[i].split("|");
        //        f += "<li><table width='100%' align='left'><tr><td align='left' width='70%'>菜" + (i + 1) + "：" + str[0] + "</td><td align='right' width='30%'>￥" + str[1] + "</td></tr></table></li>"
        f += "<li><table width='100%' align='left'><tr><td align='left' width='70%'>菜" + (i + 1) + "：" + str[0] + "(" + formatNum(str[1]) + ")</td><td align='right' width='30%'>￥" + str[2] + "</td></tr></table></li>";
        // f += "<li>菜" + (i + 1) + "：" + str[0] + "&nbsp;&nbsp;&nbsp;&nbsp;￥" + str[1] + "</td></tr></table></li>";

    }
	var isPaidStr = "";
	if(isPaid == "0"){
		isPaidStr = "未结帐";
	}else{
		isPaidStr = "已结帐";
	}
    var content = '<div id="Has_order">' +
					  '<div class="title">' +
						'<div class="title_left"><font class="font" style="width:160px">查看 （' + id + '号帐单）</font></div>' +
						'<div class="title_right"></div>' +
					  '</div>' +
					  '<div class="Has_order_Content">' +
						'<div class="pop_Content">' +
		                  '<div class="pop_Content3">帐单号：' + id + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;台号：' + alias + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;日期：' + date + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;人数：' + count + '</div>' +
		                  '<div class="pop_Content3">服务员：' +  owner_name + '</div>' +
		                  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;状态：' +  isPaidStr + '</div>' +
						'</div>' +
						'<div class="title1"><font class="font1">已点菜</font></div>' +
						  '<div class="cls_container">' +
							'<ul>' +
							 f +
							'</ul>' +
							'<ul>' +
							  '<li>' +
								'<div align="right" style="font-size:15px"><strong>合计：￥' + totalPrice + '</strong></div>' +
							  '</li>' +
							'</ul>' +
						  '</div>' +
						'<span class="action-span" style="height:40px;margin-left:60px;"><a href="#" onclick="closeWindow()">确&nbsp;&nbsp;&nbsp;&nbsp;定</a></span><span class="action-span"><a href="#" onclick="printreport()">打&nbsp;&nbsp;&nbsp;&nbsp;印</a></span>' +
					  '</div>' +
					'</div>';
    showMessageBox(content, 342, 350);
}

function deleteOrder(id) {
    if (confirm("确认删除" + id + "号帐单的信息？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "order.php";
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

function showHideCondition(select) {
    //    alert(select);
    var option = select.options[select.selectedIndex];
    if (option.value == "is_Price" || option.value == "is_day") {
        document.getElementById("condition_type").style.display = "inline";
    }
    else {
        document.getElementById("condition_type").style.display = "none";
    }
    var obj = document.getElementById("keyword");
    obj.value = "";
    obj.onclick = null;
//    obj.onblur = null;
    if (option.value == "is_day") {       
        obj.onclick = showCal;
//        obj.onblur = hidCal;
    }   
}
function showCal() {
    ShowCalendar('keyword');
}
//function hidCal() {
//    if(document.all.Calendar)
//    {
//        document.all.Calendar.style.visibility='hidden';
//    }
//}

function ininOriginal() {
    var keyword_type = document.getElementById("keyword_type");
    var condition_type = document.getElementById("condition_type");
    var keyword = document.getElementById("keyword");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var condition_type_value = document.getElementById("condition_type_value").value;
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
    if (keyword_type_value == "is_Price" || keyword_type_value == "is_day") {
        document.getElementById("condition_type").style.display = "inline";
    }
    if (keyword_type_value == "is_day") {
        //        obj.setAttribute("onclick", "javascript:ShowCalendar('keyword')");
        keyword.onclick = showCal;
    }
}

function formatNum(num) {
    var num1 = num.substr(num.length - 1, 1);
    if (num1 == "0") {
        num = num.substr(0, num.length - 1);
        var num2 = num.substr(num.length - 1, 1);
        if (num2 == "0") {
            num = num.substr(0, num.length - 2);
        }
    }

    return num;
}

function showOrderStat(statType)
{
    var title = "";
    if(statType == "daily")
    {
        title = "日结汇总";
    }
    else
    {
        title = "月结汇总";
    }
     var content = ' <div class="add_foot" style="height:450px">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font id="dynamicTitle" style="font-size: 16px;font-weight: normal;color: #FFF;margin-left: 15px;line-height: 30px;text-align: left;" >' + title + '</font></div>' +	                       
	                        '<div class="title_right"></div>' +
	                    '</div>' +	               
	                      '<div class="add_foot_Content" style="height:370px;text-align:center;">' + 	                        
	                            '<iframe src="orderStat.php?StatType=' + statType +'" scrolling="no" style="width:100%;height:100%" />' +	                           
	                        '</div>' +	                      
	                '</div>';
	showMessageBox(content, 350, 350);
}

function orderStatKeyDown() {   
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}

function addTitle(title)
{
//    alert(parent.document.getElementById('title').innerText);
//    var t1 = parent.document.getElementById('title').innerText;
//    t1 += title;
//    alert(t1);
    parent.document.getElementById('dynamicTitle').innerText += ("                  总营业额：￥" + title);
}



function showAdminSearch() {    
    var content = '<div id="Advanced_Search">' +
                    '<div class="title">' +
                        '<div class="title_left"><font class="font" style="width:200px;">高级搜索（帐单）</font></div>' +
                        '<div class="title_right"></div>' +
                    '</div>' +
                     '<form name="searchForm" action="admin_order.php"  method="post" onkeydown="searchOrderKeyDown()">' +
                    '<div class="Advanced_Search_Content" style="height:210px">' +
                        '<div class="pop_Content" >' +
                            '<div class="pop_Content2">台号：<input type="text" id="alias_id" name="alias_id" size="20" height="20" style="width:120px;" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                           '<div class="pop_Content2">餐厅：<select id="search_restaurant_type" name="search_restaurant_type" ><option value="Code" selected="selected">编号</option><option value="Name">名称</option></select>&nbsp;'+
                            '<input type="text" id="restaurant" name="restaurant" size="20" height="20" style="width:120px;"/></div>' +  
                            '<div class="pop_Content1">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
                            '<div class="pop_Content1">金额：<input type="text" name="priceFrom" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="priceTo" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +                           
                        '</div>' +
                        '<span class="action-span"><a href="#"  onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                         '<span class="action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                    '</div>' +
                    '</form>' +
                '</div>';  
                showMessageBox(content, 540, 188);
                document.getElementById("alias_id").focus();
}


