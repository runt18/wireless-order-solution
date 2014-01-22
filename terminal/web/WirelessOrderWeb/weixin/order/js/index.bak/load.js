

function initView(){
	var height = document.documentElement.clientHeight;
	$('#divMainView').css('height', height);
//	mainNav.css('top', (height - mainNav.height()) / 2 - 10);
//	info.css('top', (height - info.height()) / 2 - 10);
//	info.css('left', width - info.width() - 10);
}

$(function(){
	window.onresize = initView;
	window.onresize();
	
	$('#divMNFood')[0].onclick = function(){
		params.skip('food.html');
	};
	$('#divMNRfood')[0].onclick = function(){
		params.skip('rfood.html');
	};
	$('#divMNMember')[0].onclick = function(){
		params.skip('member.html');
	};
	$('#divMNSales')[0].onclick = function(){
		params.skip('sales.html');
	};
	$('#divMNAbout')[0].onclick = function(){
		params.skip('about.html');
	};
});