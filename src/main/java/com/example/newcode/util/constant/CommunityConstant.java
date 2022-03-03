package com.example.newcode.util.constant;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间：12小时
     */
    long DEFAULT_EXPIRED_SECONDS = 3600 * 12l;

    /**
     * 记住状态的登录凭证超时时间：1个月
     */
    long REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30l;

    /**
     * 目标是一条帖子的comment
     */
    int ENTITY_TARGET_POST = 1;

    /**
     * 目标是一条评论的comment
     */
    int ENTITY_TARGET_COMMENT = 2;


}
