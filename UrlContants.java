package com.hundsun.robot.services.account.contants;

/**
 * Created by ouakira on 2017/7/5.
 */
public class UrlContants {
    public static final String GET_ACCESS_TOKEN_BY_CODE = "https://api.weixin.qq.com/sns/oauth2/access_token";

    public static final String GET_USER_INFO = "https://api.weixin.qq.com/sns/userinfo";

    //微信获取token
    public static final String GET_ACCESSTOKEN = "https://api.weixin.qq.com/cgi-bin/token";
    //微信发送消息
    public static final String SENG_MSG = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
    //微信创建菜单
    public static final String CREATE_MENU = "https://api.weixin.qq.com/cgi-bin/menu/create";
    public static final String GET_MENU = "https://api.weixin.qq.com/cgi-bin/menu/get";

}
