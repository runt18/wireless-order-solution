var left;
$(function() {
	left = parseInt($("#list").css("left"));
	$(".side_btn").click(function() {
		move(this.id);
	});

});

function move(sign) {
	var tleft = left;
	switch (sign) {
	case "right_btn": {
		var len = $("#list .item").length * 800;
		if ((len - Math.abs(left)) > 800) {
			tleft -= 800;
		} else {
			tleft = left;
		}
		break;
	}
	case "left_btn": {
		if (left == 0) {
			tleft = 0;
		} else {
			tleft += 800;
		}
		break;
	}
	}

	if ((tleft - left) != 0) {
		$("#list").animate( {
			left : tleft + "px"
		}, 500, function() {
			left = tleft;

		});
	} else {
		$("#list").animate( {
			left : 0 + "px"
		}, 500, function() {
			left = 0;

		});
	}

}
