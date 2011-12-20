<?php
$xml = simplexml_load_file("AndroidManifest.xml");

foreach($xml->attributes("http://schemas.android.com/apk/res/android") as $key => $value){
	if($key == "versionName"){
		echo $value, "</br>", "http://".$_SERVER[ "HTTP_HOST" ].substr($_SERVER["REQUEST_URI"], 0, strrpos($_SERVER["REQUEST_URI"], "/") + 1)."WirelessOrderTerminal_Android.apk";
	}
}
?>
