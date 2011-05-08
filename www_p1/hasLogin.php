<?php
session_start();
if(!$_SESSION["restaurant_id"])
	{
	header('Location: login.php');
		}
?>