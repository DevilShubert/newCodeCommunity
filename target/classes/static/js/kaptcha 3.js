function refresh_kaptcha() {
    // 加的这个random值为了防止浏览器缓存
    var path = CONTEXT_PATH + "/kaptcha?p=" + Math.random();
    // jQuery选择器，选择kaptcha结点，并修改属性src为path
    $("#kaptcha").attr("src", path);
}