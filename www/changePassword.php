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
					$password2 = md5($pwd2);
					if($pwd2 == "")
						{
						$password2 = "";
							}
					$sql = "Update restaurant SET pwd2='" .$password2. "' WHERE id=$restaurant_id";	
					$_SESSION["pwd2"] = $password2;	
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
$editType = $_POST["editType"];
if($editType == "editRestaurant")
{
	//$alias_id = $_POST["alias_id"];
	$id = $_POST["id"];
	$restaurant_name = $_POST["restaurant_name"];
	$address = $_POST["address"];
	$tele1 = $_POST["tele1"];
	$tele2 = $_POST["tele2"];
	
	$sql = "UPDATE restaurant SET restaurant_name='$restaurant_name',address='$address',tele1 = '$tele1',tele2 = '$tele2' WHERE id=$id";		
	
	if($db->Execute($sql))
	{
		echo "<script>alert('保存成功！');window.parent.frames(0).location.reload();;</script>";
		$_SESSION["restaurant_name"] = $restaurant_name;
		$_SESSION["address"] = $address;
		$_SESSION["tele1"] = $tele1;
		$_SESSION["tele2"] = $tele2;			
	}
	else{
		echo "<script>alert('保存失败！');</script>";
	}
}
?>

