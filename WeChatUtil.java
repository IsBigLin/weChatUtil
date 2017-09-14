package com.hundsun.robot.services.account.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hundsun.robot.services.account.contants.UrlContants;
import com.hundsun.robot.services.account.contants.WeChatContants;
import com.hundsun.robot.services.account.dto.AccessToken;
import com.hundsun.robot.services.account.dto.wechat.AccessTokenBack;
import com.hundsun.robot.services.account.dto.wechat.UserInfoBack;
import com.hundsun.robot.services.account.entity.Strategy;
import com.hundsun.robot.services.account.property.WeChatProperty;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ouakira on 2017/7/5.
 */
public class WeChatUtil {
    private static Logger logger = LoggerFactory.getLogger(WeChatUtil.class);

    /**
     * 网页授权
     * 微信授权通过code获取access_token
     *
     * @param property 微信appid和secret
     * @param code     微信授权登录获取的code
     * @return
     */
    public static AccessTokenBack getAccessTokenByCode(WeChatProperty property, String code) {
        logger.info("通过code获取access_token的公众号的appid为" + property.getAppid()+" code="+code);
        Map<String, Object> map = new HashMap<>();
        map.put("appid", property.getAppid());
        map.put("secret", property.getSecret());
        map.put("code", code);
        map.put("grant_type", WeChatContants.GRANT_TYPE);
        String str = HttpUtil.doPost(UrlContants.GET_ACCESS_TOKEN_BY_CODE, map);
        return JSON.parseObject(str, AccessTokenBack.class);
    }

    /**
     * 网页授权
     * 根据授权code得到的access_token和openid获取用户信息
     *
     * @param access_token 通过code获取的accestt_token
     * @param openid       微信用户的openid
     * @return
     */
    public static UserInfoBack getUserInfo(String access_token, String openid) {
        logger.debug("获取用户信息的opened为：" + openid);
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", access_token);
        map.put("openid", openid);
        map.put("lang", WeChatContants.LANG_ZH_CN);
        String str = HttpUtil.doPost(UrlContants.GET_USER_INFO, map);
        return JSON.parseObject(str, UserInfoBack.class);
    }

    /**
     * 获取token
     */
    private static AccessTokenBack getAccessToken(WeChatProperty property, RedisTemplate redisTemplate) {
        String key = "getWeChatAccessToken"+property.getSecret();
        AccessTokenBack accessTokenBack = (AccessTokenBack) redisTemplate.opsForValue().get(key);
        RedisConfig.releaseConnection(redisTemplate);
        if(accessTokenBack == null){
            Map<String, Object> map = new HashMap<>();
            map.put("appid", property.getAppid());
            map.put("secret", property.getSecret());
            map.put("grant_type", "client_credential");
            String str = null;
            try {
                str = HttpUtil.doGet(UrlContants.GET_ACCESSTOKEN, map);
                accessTokenBack = JSON.parseObject(str, AccessTokenBack.class);
                redisTemplate.opsForValue().set(key, accessTokenBack, Integer.parseInt(accessTokenBack.getExpires_in()), TimeUnit.SECONDS);
                RedisConfig.releaseConnection(redisTemplate);
            } catch (Exception e) {
                logger.error("获取微信AccessToken出错："+str, e);
            }
        }
        return accessTokenBack;
    }

    /**
     * @param msg
     * @return
     * @desc 推送信息
     */
    public static String sendMessage(WeChatProperty property, String msg, String openid, RedisTemplate redisTemplate) {
        try {
            AccessTokenBack accessTokenBack = getAccessToken(property, redisTemplate);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("touser", openid);
            jsonObject.put("msgtype", "text");
            JSONObject textJsonObject = new JSONObject();
            textJsonObject.put("content", msg);
            jsonObject.put("text", textJsonObject);
            String response = HttpClientUtil.sendPostByJson(UrlContants.SENG_MSG + "?access_token=" + accessTokenBack.getAccess_token(),
                    ValueUtil.getString(jsonObject),
                    HttpClientUtil.CHARSET, HttpClientUtil.CHARSET, null, null,
                    "发发消息");
            logger.info(response);
            return response;
        } catch (Exception e) {
            logger.error("get user info exception", e);
            return null;
        }
    }

    public static void createMenu(){
        try {
//            AccessTokenBack accessTokenBack = getAccessToken(property, redisTemplate);
            Map<String, Object> map = new HashMap<>();
            map.put("appid", "wxa847207a7414e49d");
            map.put("secret", "7ee325d4beeea6ab573ff7301ad145e7");
//            map.put("appid", "wx804b3901884f122d");
//            map.put("secret", "46c770568a2b8600af8ffd47379bbdb0");
            map.put("grant_type", "client_credential");
            String str = null;
            try {
                str = HttpUtil.doGet(UrlContants.GET_ACCESSTOKEN, map);
            } catch (Exception e) {
            }
            AccessTokenBack accessTokenBack = JSON.parseObject(str, AccessTokenBack.class);

            JSONArray firstContents = new JSONArray();
            JSONObject firstContent = new JSONObject();
            firstContent.put("name", "策略PK-dev");
            firstContent.put("type", "view");
//            String authUrl = "http://iseedemo.yjifs.com/dist/#/pkRobot";
            String authUrl = "https://fulldev.yjifs.com/robotpk/#/";
//            String authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx804b3901884f122d&redirect_uri=https://fulldev.yjifs.com/weixin/code.html&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
            firstContent.put("url", authUrl);
            firstContents.add(firstContent);
            JSONObject firstButton = new JSONObject();
            firstButton.put("button", firstContents);
            String response = HttpClientUtil.sendPostByJson(UrlContants.CREATE_MENU + "?access_token=" + accessTokenBack.getAccess_token(),
                    firstButton.toString(),
                    HttpClientUtil.CHARSET, HttpClientUtil.CHARSET, null, null,
                    "创建菜单");
            logger.info(response);
        } catch (Exception e) {
            logger.error("get user info exception", e);
        }
    }
    public static void getMenu(){
        try {
//            AccessTokenBack accessTokenBack = getAccessToken(property, redisTemplate);
            Map<String, Object> map = new HashMap<>();
            map.put("appid", "wx804b3901884f122d");
            map.put("secret", "46c770568a2b8600af8ffd47379bbdb0");
            map.put("grant_type", "client_credential");
            String str = null;
            try {
                str = HttpUtil.doGet(UrlContants.GET_ACCESSTOKEN, map);
            } catch (Exception e) {
            }
            AccessTokenBack accessTokenBack = JSON.parseObject(str, AccessTokenBack.class);

            JSONObject firstButton = new JSONObject();
            String response = HttpClientUtil.sendPostByJson(UrlContants.GET_MENU + "?access_token=" + accessTokenBack.getAccess_token(),
                    firstButton.toString(),
                    HttpClientUtil.CHARSET, HttpClientUtil.CHARSET, null, null,
                    "获取菜单");
            logger.info(response);
        } catch (Exception e) {
            logger.error("get user info exception", e);
        }
    }

    public static void main(String[] args){
        createMenu();
        getMenu();
    }
}
