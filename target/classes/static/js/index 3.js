$(function () {
    // 绑定事件
    $("#publishBtn").click(publish);
});

// link to publishBtn
function publish() {
    // Edit window is hidden by default
    $("#publishModal").modal("hide");


    // 发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
//    var token = $("meta[name='_csrf']").attr("content");
//    var header = $("meta[name='_csrf_header']").attr("content");
//    $(document).ajaxSend(function(e, xhr, options){
//        xhr.setRequestHeader(header, token);
//    });

    // jQuery selector
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    // post ajax
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
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
            setTimeout(function () {
                $("#hintModal").modal("hide");
                location.reload();
            }, 1500);
        }
    )

    $("#hintModal").modal("show");
    setTimeout(function () {
        $("#hintModal").modal("hide");
    }, 2000);
}