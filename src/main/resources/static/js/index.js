$(function(){
	$("#publishBtn").click(publish);
});

// link to publishBtn
function publish() {
	// Edit window is hidden by default
	$("#publishModal").modal("hide");

	// jQuery selector
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	// post ajax
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		// Callback
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				$("#hintBody").text("发送成功!");
			} else {
				$("#hintBody").text(data.msg);
			}

			// 关闭消息提示
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 1500);
		}
	)

	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}