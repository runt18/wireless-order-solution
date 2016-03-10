$(function(){
	var height = window.innerHeight || document.body.clientHeight || document.documentElement.clientHeight;
//	var height = $('window').height() || $('document.body').height();
	console.log(height);
	$('#bigbox').css({height : height - 50});
})