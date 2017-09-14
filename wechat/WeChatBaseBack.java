package com.hundsun.robot.services.account.dto.wechat;

/**
 * Created by ouakira on 2017/7/5.
 */
public class WeChatBaseBack {
    private String errcode;

    private String errmsg;

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
