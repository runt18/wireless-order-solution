<?php
include("conn.php");
mysql_query("SET NAMES utf8"); 
$isChangePassword = $_POST["isChangePassword"];
if($isChangePassword == "true")
{
	$oldPassword = $_POST["oldPassword"];
	$newPassword = $_POST["newPassword"];
	$pwd2 = $_POST["pwd2"];
	$random = $_POST["random"];
	$restaurant_id = $_SESSION["restaurant_id"];
	$sql = "SELECT * FROM restaurant WHERE id=$restaurant_id AND pwd='" .md5($oldPassword) ."'";
	
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		if($newPassword != $random && $pwd2 != $random)
		{
			$sql = "Update restaurant SET pwd='" .md5($newPassword). "', pwd2='" .md5($pwd2). "' WHERE id=$restaurant_id";	
			$_SESSION["pwd2"] = md5($pwd2);	
		}
		else if($newPassword != $random && $pwd2 == $random)
			{
				$sql = "Update restaurant SET pwd='" .md5($newPassword). "' WHERE id=$restaurant_id";		
			}
			else if($newPassword == $random && $pwd2 != $random)
				{
					$sql = "Update restaurant SET pwd2='" .md5($pwd2). "' WHERE id=$restaurant_id";	
					$_SESSION["pwd2"] = md5($pwd2);	
				}
				else
				{
					echo "<script>alert('未做任何更改！');changePassword('$newPassword','$pwd2','$random')</script>";
					return;
				}
		
		if($db->Execute($sql))
		{
			echo "<script>alert('修改成功！');</script>";
			
		}
		else{
			echo "<script>alert('修改失败！');</script>";
		}
		
	}
	else
	{		
		echo "<script>alert('旧密码错误！');changePassword('$newPassword','$pwd2','$random')</script>";	
	}		
}	
?>

