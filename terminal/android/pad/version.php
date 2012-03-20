<?php
$xml = simplexml_load_file("AndroidManifest.xml");

foreach($xml->attributes("http://schemas.android.com/apk/res/android") as $key => $value){
	/**
	 * The format to this string is as below.
	 * ver_num</br>ver_info</br>url
	 */
	if($key == "versionName"){
		header("Content-type: text/html; charset=UTF-8");
		echo $value, "</br>", 
			 "e点通发现新版本v".$value."，按确定进行版本升级", "</br>",
			 "http://".$_SERVER[ "HTTP_HOST" ].substr($_SERVER["REQUEST_URI"], 0, strrpos($_SERVER["REQUEST_URI"], "/") + 1)."WirelessOrderTerminal_Pad.apk";
	}
}
?>
