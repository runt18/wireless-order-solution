<?php

//产生随机数函数 
function random($length) { 
	$hash = array();
	$number = "";
	$chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz'; 
	$max = strlen($chars) - 1; 
	while(count($hash)<=$length){
		$hash[] = mt_rand(0,$max);
		$hash = array_unique($hash);
	}
	for ($j=0;$j<6;$j++){
		$number.=substr($chars,$hash[$j],1);
	}
	return $number;
} 

//产生随机数函数 
function randomNumber($length) { 
	$hash = array();
	$number = "";
	$chars = '0123456789'; 
	$max = strlen($chars) - 1; 
	while(count($hash)<=$length){
		$hash[] = mt_rand(0,$max);
		$hash = array_unique($hash);
	}
	for ($j=0;$j<6;$j++){
		$number.=substr($chars,$hash[$j],1);
	}
	return $number;
} 

?>