package com.hundsun.robot.services.account.dto.wechat;

import java.io.Serializable;

/**
 * Created by ouakira on 2017/7/5.
 */
public class AccessTokenBack extends WeChatBaseBack implements Serializable{
    private static final long serialVersionUID = 538156257034374418L;
    private String access_token;

    private String openid;

    private String scope;

    private String refresh_token;

    private String expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }
}
