var left;
$(function(){
    left = parseInt($("#list").css("left"));
    $(".side_btn").click(function(){
        move(this.id);
    });
    
});

function move(sign)
{
    var tleft = left;
    switch(sign)
    {
        case "right_btn":
        {
            var len = $("#list .item").length * 800;
            if((len - Math.abs(left)) > 800)
            {
                tleft -= 800;
            }
            else
            {
                tleft = left;
            }
            break;
        }
        case "left_btn":
        {
            if (left == 0)
            {
                tleft = 0;
            }
            else
            {
                tleft += 800;
            }
            break;
        }
    }
    
    if ((tleft - left) != 0)
    {
        $("#list").animate({
            left:tleft + "px"
        },500,function(){
            left = tleft;
            
        });
    }
	else
    {
        $("#list").animate({
            left:0 + "px"
        },500,function(){
            left = 0;
            
        });
    }
    
}

function addTable(tableCode)
{
      var content = ' <div class="add_foot">' +
            '<div class="title">' +
                '<div class="title_left"><font class="font">添加餐桌</font></div>' +
                '<div class="title_right"></div>' +
            '</div>' +
            '<form name="tableForm" action="table.php"  method="post" onkeydown="addTableKeyDown()">' +	                    
                  '<div class="add_foot_Content">' + 
                    '<div class="pop_Content">' +                        
                        '<div class="pop_Content1">餐桌号：<input type="text" id="tableCode" name="tableCode" size="25" value="' + tableCode + '"height="20"/></div>' +	                           
                    '</div>' +
                    '<span class="pop_action-span"><a href="#" onclick="submitTableData();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                    '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                  '</div>' +
            '</form>' +
            '</div>';
    showMessageBox(content,342,350);
    document.getElementById("tableCode").focus();
}
function deleteTable(tableCode)
{
             var content = ' <div class="add_foot">' +
            '<div class="title">' +
                '<div class="title_left"><font class="font">删除餐桌</font></div>' +
                '<div class="title_right"></div>' +
            '</div>' +           	                    
                  '<div class="add_foot_Content">' + 
                    '<div class="pop_Content">' +
                        '<div class="pop_Content1">餐桌号：<input type="text" id="tableCode" size="25" value="' + tableCode + '" height="20"/></div>' +	                           
                    '</div>' +
                    '<span class="pop_action-span"><a href="#" onclick="confirmDelete(document.getElementById(&quot;tableCode&quot;).value)">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                    '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                  '</div>' +            
            '</div>';
    showMessageBox(content,342,350);
}

function confirmDelete(id)
{
    if(confirm("确认删除" + id + "号台？"))
    {
        var formDelete = document.createElement("form");
        formDelete.action = "table.php";
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

function showMenu(id) {
    var content = ' <div class="add_foot">' +
            '<div class="title">' +
                '<div class="title_left"><font class="font" style="width:100%">操作（'+ id + '号台）</font></div>' +
                '<div class="title_right"></div>' +
            '</div>' +
            '<form name="tableForm" action="table.php"  method="post">' +

                  '<div class="add_foot_Content" style="text-align:center;">' +
				    '<table width="100%" border=0>' +
			        '<tr><td>' + 
                    '<div class="pop_action-span1" style="margin-right: 110px;text-align:center;"><a href="#" onclick="confirmDelete(&quot;' + id + '&quot;)">删&nbsp;&nbsp;&nbsp;&nbsp;除</a></div>' +
					'</td></tr>' + 
			        '<tr><td>' + 
                    '<div class="pop_action-span1" style="margin-right: 110px;text-align:center;"><a href="#">下&nbsp;&nbsp;&nbsp;&nbsp;单</a></div>' +
					'</td></tr>' + 
  				    '<tr><td>' + 
                    '<div class="pop_action-span1" style="margin-right: 110px;text-align:center;"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></div>' +
  					'</td></tr></table>' + 
                  '</div>' +
            '</form>';
			'</div>'
    showMessageBox(content, 342, 350);
}

function submitTableData() {
    var tableCode = document.getElementById("tableCode").value;  

    if (tableCode == undefined || tableCode == null || tableCode == "") {
        alert("餐桌号不能为空！");
        return;      
    }
    document.tableForm.submit();
}

function statTable(totalNumber, isPaidNumber) {
    document.getElementById("total_Table").innerText = totalNumber;
	document.getElementById("used_Table").innerText = totalNumber - isPaidNumber;
	document.getElementById("idle_Table").innerText = isPaidNumber;
}

function addTableKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitTableData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}