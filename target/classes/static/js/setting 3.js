$(function(){
    // 绑定uploadForm表单的提交按钮
    $("#uploadForm").submit(upload);
});

function upload() {
    // JQuery手动使用ajax传输
    $.ajax({
        url: "http://upload-z2.qiniup.com", // 七牛云专门用来上传的域名，具体见官方文档中产品介绍-存储区域部分
        method: "post",
        processData: false, // 不要把表单的内容转为字符串（因为带有图片）
        contentType: false, // 不让JQuery设置上传类型
        data: new FormData($("#uploadForm")[0]), // 获取图片
        success: function(data) { // 七牛云返回的就是个JSON
            if(data && data.code == 0) {
                // 更新头像访问路径，这里仍然是使用异步请求
                $.post(
                    // CONTEXT_PATH + "/user/header/url",
                    CONTEXT_PATH + "/user/header/url/" + $("input[name='key']").val(),
                    //  {"fileName":$("input[name='key']").val()}, // 这种形式会变为 ?fileName=xxx
                    function(data) {
                        data = $.parseJSON(data); // 解析应用服务器传来的字符串，转为JSON
                        if(data.code == 0) {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!");
            }
        }
    });
    return false;
}