<?php
$xml = simplexml_load_file("AndroidManifest.xml");
$ver_info = "e点通无线点餐终端（Android版）";
foreach($xml->attributes("http://schemas.android.com/apk/res/android") as $key => $value){
	/**
	 * The format to this string is as below.
	 * ver_num</br>ver_info</br>url
	 */
	if($key == "versionName"){
		echo $value, "</br>", 
			 $ver_info, "</br>",
			 "http://".$_SERVER[ "HTTP_HOST" ].substr($_SERVER["REQUEST_URI"], 0, strrpos($_SERVER["REQUEST_URI"], "/") + 1)."WirelessOrderTerminal_Android.apk";
	}
}
?>
