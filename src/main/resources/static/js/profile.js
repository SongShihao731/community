$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{
				"entityType":3,
				"entityId":$(btn).prev().val()
			},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0) {
					// 关注成功
					window.location.reload();
				}else {
					// 关注失败
					alert(data.msg);
				}
			}
		)
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{
				"entityType":3,
				"entityId":$(btn).prev().val()
			},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0) {
					// 取关成功
					window.location.reload();
				}else {
					// 取关失败
					alert(data.msg);
				}
			}


		)
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}