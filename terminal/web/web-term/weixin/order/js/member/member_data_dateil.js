var params = {member:{}};
function initView(){
	var html = [], year = new Date().getFullYear();
	var birthYear = $('#selBrithYear'), birthMonth = $('#selBrithMonth'), birthDay = $('#selBrithDay');
	//
	for(var minYear = year - 18 - 60, maxYear = year - 18, i = maxYear; i >= minYear; i--){
		html.push('<option>'+i+'</option>');
	}
	birthYear.html(html);
	//
	html = [];
	for(var i = 1; i <= 12; i++){
		html.push('<option>'+i+'</option>');
	}
	birthMonth.html(html);
	//
	html = [];
	for(var i = 1; i <= 31; i++){
		html.push('<option>'+i+'</option>');
	}
	birthDay.html(html);
};
function setBirthDate(birthDate){
	var birthYear = Util.getDom('selBrithYear'), birthMonth = Util.getDom('selBrithMonth'), birthDay = Util.getDom('selBrithDay');
	if(birthDate.trim().length > 0){
		var temp = birthDate.split('-');
		birthYear.value = temp[0];
		birthMonth.value = temp[1];
		birthDay.value = temp[2];
	}else{
		birthYear.value = '';
		birthMonth.value = '';
		birthDay.value = '';
	}
}
function loadMemberData(){
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'getInfo',
			oid : Util.mp.oid,
			fid : Util.mp.fid
		},
		dataType : 'json',
		success : function(data, status, xhr){
			if(data.success){
				params.member = data.other.member;
				$('#spanMemberName').html(params.member.name);
				$('#spanMemberMobile').html(params.member.mobile);
				
				var sex = document.getElementsByName('radioMemberSex');
				for(var i = 0; i < sex.length; i++){
					if(eval(sex[i].getAttribute('value') == params.member.sexValue)){
						sex[i].checked = true;
						break;
					}
				}
				
				setBirthDate(params.member.birthdayFormat);
			}else{
				Util.dialog.show({msg: data.msg});
				// 7400
			}
		},
		error : function(data, errotType, eeor){
			Util.dialog.show({title:'错误', msg: '服务器请求失败, 请稍候再试.'});
		}
	});
}
$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	//
	initView();
	//
	loadMemberData();
});