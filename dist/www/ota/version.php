<?php
$file_content = file_get_contents("WirelessOrderTerminal.jad");

$file_array = split("\r\n", $file_content);

foreach ($file_array as $value1){
	$item = split(":", $value1);
	if($item[0] == "MIDlet-Version"){
		echo trim($item[1]);
	}
}
?>
