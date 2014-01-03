

function initView(){
	var height = document.documentElement.clientHeight;
	var width = document.documentElement.clientWidth;
	var mainNav = $('#divMainNav'), info = $('#divRestaurantInfo');
	mainNav.css('top', (height - mainNav.height()) / 2 - 10);
	info.css('top', (height - info.height()) / 2 - 10);
	info.css('left', width - info.width() - 10);
}

$(function(){
	
	window.onresize = initView;
	window.onresize();
	
});