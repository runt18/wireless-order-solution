<?php
$file_content = file("WirelessOrderTerminal.jad");

$no = count($file_content);

for($index = 0; $index < $no; $index++){
	$item = split(":", $file_content[$index]);
	if($item[0] == "MIDlet-Version"){
		echo trim($item[1]);
	}
}
?>
