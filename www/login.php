<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>e点通餐饮管理平台</title>
<link href="css/general.css" rel="stylesheet" type="text/css" />
<link href="css/main.css" rel="stylesheet" type="text/css" />
 
<style type="text/css"> 
body {
  color: white;
}

#postBar {
    position:fixed;
    right:45%;
    bottom:0;
    _position:absolute;
    _top:expression(document.documentElement.clientHeight + document.documentElement.scrollTop - this.offsetHeight);
}

#login {
	background-color: #278295;
	height: 168px;
	width: 423px;
	text-align:center;
	margin-left:auto;
	margin-right:auto;
	margin-top: 20%;
}
#button{
	float: left;
	background-image: url(images/button_bg.png);
	background-repeat: repeat-x;
	height: 25px;
	font-family: "宋体";
	font-size: 12px;
	color: #000;
	text-decoration: none;
}
#save{
	float: left;
	height: 25px;
	font-family: "宋体";
	font-size: 12px;
	color: #FFF;
	text-decoration: none;
}
</style>
 
<script type="text/javascript" src="js/utils.js"></script><script type="text/javascript" src="js/validator.js"></script><script language="JavaScript"> 
<!--
// 这里把JS用到的所有语言都赋值到这里
var process_request = "正在处理您的请求...";
var todolist_caption = "记事本";
var todolist_autosave = "自动保存";
var todolist_save = "保存";
var todolist_clear = "清除";
var todolist_confirm_save = "是否将更改保存到记事本？";
var todolist_confirm_clear = "是否清空内容？";
var user_name_empty = " 会员姓名不能为空!";
var password_invaild = "密码必须同时包含字母及数字且长度不能小于6!";
var Email_empty = "Email地址不能为空!";
var Email_error = "Email地址格式不正确!";
var password_error = "两次输入的密码不一致!";
var captcha_empty = "您没有输入验证码!";
 
if (window.parent != window)
{
  window.top.location.href = location.href;
}
 
//-->
</script>
</head>
<body style="background: #278295">

<div style="margin:0 auto; text-align:right; font-size: small;">e点通&reg;餐饮管理平台(v0.9.9)</div>

<form method="post" action="chk.php" name='theForm' onsubmit="return validate()">
  <table cellspacing="0" cellpadding="0" style="margin-top: 100px" align="center">
  <tr>
    <td width="129"><a href="http://www.digi-e.com" target="_blank"><img src="images/login.png" width="178" height="256" border="0" alt="智易科技" /></a></td>
    <td	 style="padding-left: 50px">
      <table>
      <tr>
        <td>会员帐号：</td>
        <td><input type="text" name="username" style="width:188px" /></td>
		
      </tr>
      <tr>
        <td>会员密码：</td>
        <td><input type="password" name="password" style="width:188px" /></td>
		
      </tr>
            <tr>
        <td>验证码：</td>
        <td><input type="text" name="code" class="code" style="width:188px" /></td>
		
      </tr>
      <tr>
      <td colspan="2" align="right" style="padding-right:5px"><img src="button.php" onclick="this.src='button.php?act='+Math.random()" style="cursor: pointer;"  title="看不清？点击更换另一个验证码。"/>
      </td>
      </tr>
            <tr><td colspan="2" id="save">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" value="1" name="remember" id="remember" /><label 
                    for="remember">&nbsp;&nbsp;请保存我这次的登录信息</label></td></tr>
      <tr>
        <td>&nbsp;</td><td>
                          <input  id="button" type="submit" onclick="javascript:document.getElementById('loginType').value = 'Web';" value="进入会员中心" class="button"  style="width:85px;" />
						<input  id="button" type="submit" onclick="javascript:document.getElementById('loginType').value = 'WebTerminal';" value="进入web点菜系统" class="button"  style="width:105px;" /></td></tr>
      
      </table>
    </td>
  </tr>
  </table>
  <input type="hidden" name="act" value="signin" />
  <input type="hidden" id="loginType" name="loginType" value="Web" />
</form>
<script language="JavaScript"> 
  document.forms['theForm'].elements['username'].focus();
  

   //检查表单输入的内容
  function validate()
  {	
    var validator = new Validator('theForm');
    validator.required('username', user_name_empty);
    //validator.required('password', password_empty);
    if (document.forms['theForm'].elements['captcha'])
    {
      validator.required('captcha', captcha_empty);
    }
    return validator.passed();
  }    
</script>

<div style="height:100%;"></div>
<div id="postBar"><span style="color: #FFFFFF; font-size: small">版权所有(c) 2011 智易科技</span></div>



</body>
