<?php 
session_start(); 
$authnum=random(4);//��֤���ַ�. 
$_SESSION['code']=$authnum; 

//������֤��ͼƬ 
Header("Content-type: image/PNG"); 
$im = imagecreate(80,30); //imagecreate() �½�ͼ�񣬴�СΪ x_size �� y_size �Ŀհ�ͼ�� 
$red = imagecolorallocate($im, 255,255,255); //���ñ�����ɫ 
$white = imagecolorallocate($im, mt_rand(0,200),mt_rand(0,200),mt_rand(0,200));//����������ɫ 
$gray = imagecolorallocate($im, 0,0,0); //�����ӵ���ɫ



/* 
//int imagecolorallocate ( resource image, int red, int green, int blue ) 
//imagecolorallocate() Ϊͼ�������ɫ,�������ɸ����� RGB �ɷ���ɵ���ɫ��image ������ imagecreatetruecolor() �����ķ���ֵ��red��green �� blue �ֱ�������Ҫ����ɫ�ĺ죬�̣����ɷ֡���Щ������ 0 �� 255 ����������ʮ�����Ƶ� 0x00 �� 0xFF��imagecolorallocate() ���뱻�����Դ���ÿһ������ image �������ͼ���е���ɫ�� 
//��һ�ζ� imagecolorallocate() �ĵ��û���䱳��ɫ�� 
//*/ 

imagefill($im,80,30,$red); 
/*imagefill() �� image ͼ������� x��y��ͼ�����Ͻ�Ϊ 0, 0������ color ��ɫִ��������䣨���� x, y ����ɫ��ͬ�����ڵĵ㶼�ᱻ��䣩�� 
//����λ������֤�����ͼƬ 
//λ�ý���*/ 

imagerectangle($im,0,0,79,29,$gray);

for ($i = 0; $i < strlen($authnum); $i++) 
{ 
// $i%2 == 0?$top = -1:$top = 3; 

imagestring($im, 5, 22*$i+4, 5, substr($authnum,$i,1), $white); 
//int imagestring ( resource image, int font, int x, int y, string s, int col) 
//imagestring() �� col ��ɫ���ַ��� s ���� image �������ͼ��� x��y ���괦��ͼ������Ͻ�Ϊ 0, 0������� font �� 1��2��3��4 �� 5����ʹ���������塣 
} 
for($i=0;$i<200;$i++) //����������� 
{ 
imagesetpixel($im, rand()%100 , rand()%80 , $gray); 
//int imagesetpixel ( resource image, int x, int y, int color) 
//imagesetpixel() �� image ͼ������ color ��ɫ�� x, y ���꣨ͼ�����Ͻ�Ϊ 0, 0���ϻ�һ���㡣 
} 
imagepng($im); //�� PNG ��ʽ��ͼ���������������ļ� 
imagedestroy($im);//����һͼ�� 

//������������� 
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