function like(btn, entityType, entityId, entityUserId, postId) {
    // 异步请求
    $.post(
        // 路径
        CONTEXT_PATH + "/like",
        // 携带的参数
        {
            "entityType": entityType,
            "entityId": entityId,
            "entityUserId":entityUserId,
            "postId":postId
        },
        // 处理数据
        function (data) {
            // 转为json
            data = $.parseJSON(data);
            if (data.code == 0) {
                // 成功
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
            }else {
                // 失败
                alert(data.msg)
            }
        }
    );
}