<?php 
session_start(); 
$authnum=random(4);//验证码字符. 
$_SESSION['code']=$authnum; 

//生成验证码图片 
Header("Content-type: image/PNG"); 
$im = imagecreate(80,30); //imagecreate() 新建图像，大小为 x_size 和 y_size 的空白图像。 
$red = imagecolorallocate($im, 255,255,255); //设置背景颜色 
$white = imagecolorallocate($im, mt_rand(0,200),mt_rand(0,200),mt_rand(0,200));//设置文字颜色 
$gray = imagecolorallocate($im, 0,0,0); //设置杂点颜色



/* 
//int imagecolorallocate ( resource image, int red, int green, int blue ) 
//imagecolorallocate() 为图像分配颜色,代表了由给定的 RGB 成分组成的颜色。image 参数是 imagecreatetruecolor() 函数的返回值。red，green 和 blue 分别是所需要的颜色的红，绿，蓝成分。这些参数是 0 到 255 的整数或者十六进制的 0x00 到 0xFF。imagecolorallocate() 必须被调用以创建每一种用在 image 所代表的图像中的颜色。 
//第一次对 imagecolorallocate() 的调用会填充背景色。 
//*/ 

imagefill($im,80,30,$red); 
/*imagefill() 在 image 图像的坐标 x，y（图像左上角为 0, 0）处用 color 颜色执行区域填充（即与 x, y 点颜色相同且相邻的点都会被填充）。 
//将四位整数验证码绘入图片 
//位置交错*/ 

imagerectangle($im,0,0,79,29,$gray);

for ($i = 0; $i < strlen($authnum); $i++) 
{ 
// $i%2 == 0?$top = -1:$top = 3; 

imagestring($im, 5, 22*$i+4, 5, substr($authnum,$i,1), $white); 
//int imagestring ( resource image, int font, int x, int y, string s, int col) 
//imagestring() 用 col 颜色将字符串 s 画到 image 所代表的图像的 x，y 座标处（图像的左上角为 0, 0）。如果 font 是 1，2，3，4 或 5，则使用内置字体。 
} 
for($i=0;$i<200;$i++) //加入干扰象素 
{ 
imagesetpixel($im, rand()%100 , rand()%80 , $gray); 
//int imagesetpixel ( resource image, int x, int y, int color) 
//imagesetpixel() 在 image 图像中用 color 颜色在 x, y 坐标（图像左上角为 0, 0）上画一个点。 
} 
imagepng($im); //以 PNG 格式将图像输出到浏览器或文件 
imagedestroy($im);//销毁一图像 

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
    for ($j=0;$j<4;$j++){
    	$number.=substr($chars,$hash[$j],1);
    }
    return $number;
} 
?>