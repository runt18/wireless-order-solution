var left;
var buttonClick = "";
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
			buttonClick = "right_btn";
			var len = $("#list .item").length * 800;
			if ((len - Math.abs(left)) > 800) {
				tleft -= 800;
			} else {
				tleft = left;
			}
			break;
		}
		case "left_btn": {
			buttonClick = "left_btn";
			if (left == 0) {
				tleft = 0;
			} else {
				tleft += 800;
			}
			break;
		}
	}

	if ((tleft - left) != 0) {
		// table list move
		$("#list").animate({
			left : tleft + "px"
		}, 500, function() {
			left = tleft;
		});

		// page index change
		var currIndex = parseInt($("#pageIndexTL").html());
		$("#pageIndexTL").fadeTo(250, 0.1, function() {
			if (buttonClick == "right_btn") {
				$(this).html(currIndex + 1);
				$(this).fadeTo(250, 1);
			} else {
				$(this).html(currIndex - 1);
				$(this).fadeTo(250, 1);
			}
		});

	} else {
		// table list move
		$("#list").animate({
			left : 0 + "px"
		}, 500, function() {
			left = 0;
		});

		// page index change
		var currIndex = parseInt($("#pageIndexTL").html());
		if (currIndex != 1) {
			$("#pageIndexTL").fadeTo(250, 0.1, function() {
				$(this).html(1);
				$(this).fadeTo(250, 1);
			});
		}
	}

};

function restore() {
	// table list move
	$("#list").animate({
		left : 0 + "px"
	}, 1, function() {
		left = 0;
	});

	// page index change
	var currIndex = parseInt($("#pageIndexTL").html());
	if (currIndex != 1) {
		$("#pageIndexTL").fadeTo(250, 0.1, function() {
			$(this).html(1);
			$(this).fadeTo(250, 1);
		});
	}
};