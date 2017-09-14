package com.hundsun.robot.services.account.util;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by zhaojp18008 on 2017/2/21.
 */
public class HttpUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    private static final CloseableHttpClient httpClient;
    public static final String CHARSET = "UTF-8";

    /** 连接超时时间 */
    public final static int connectTimeout = 15000;

    /** socket连接超时时间 */
    public final static int socketTimeout = 20000;

    /** 发送请求相应时间 */
    public final static int requestTimeout = 15000;

    static {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(15000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public static String doGet(String url, Map<String, Object> params)throws Exception {
        return doGet(url, params, CHARSET);
    }

    public static String doPost(String url, Map<String, Object> params) {
        return doPost(url, params, CHARSET, null);
    }

    /**
     * HTTP Get 获取内容
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doGet(String url, Map<String, Object> params, String charset) throws Exception {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            List<NameValuePair> pairs = mapToList(params);
            if (params != null && !params.isEmpty()) {
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            }
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            LOG.info("\r\n" + new Date().toString() + "--");
            LOG.info(result + "\r\n");
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        //return null;
    }

    /**
     * HTTP Post 获取内容
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset 编码格式
     * @param authorization 认证
     * @return 页面内容
     */
    public static String doPost(String url, Map<String, Object> params, String charset, String authorization) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            List<NameValuePair> pairs = mapToList(params);
            HttpPost httpPost = new HttpPost(url);
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
            }
            if (!StringUtil.isBlank(authorization)) {
                httpPost.setHeader("Authorization", authorization);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            LOG.info(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sendPost(String url, Map<String, String> params,
                                  String charSet, String charsetReturn, HttpHost proxy,
                                  String authorization, String interfacename) {
        try {
            HttpPost post = new HttpPost(url);
            RequestConfig.Builder builder = RequestConfig.custom();
            if (proxy != null) {
                builder.setProxy(proxy);
                RequestConfig requestConfig = builder
                        .setSocketTimeout(socketTimeout)
                        .setConnectTimeout(connectTimeout)
                        .setConnectionRequestTimeout(requestTimeout)
                        .setExpectContinueEnabled(false)
                        .setRedirectsEnabled(true).build();
                post.setConfig(requestConfig);
            }

            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setHeader("Authorization", authorization);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                int n = 0;
                for (Map.Entry<String, String> set : params.entrySet()) {
                    if (n == 0) {
                        n++;
                        sb.append(set.getKey() + "=" + set.getValue());
                    } else {
                        sb.append("&" + set.getKey() + "=" + set.getValue());
                    }
                    nvps.add(new BasicNameValuePair(set.getKey(), set
                            .getValue()));
                }
            }
            post.setEntity(new UrlEncodedFormEntity(nvps, charSet));
            LOG.info("\n功能名称："+interfacename+"\n"+ "post  url = ["
                    + (url.endsWith("?") ? url : url + "?") + sb.toString()
                    + "]");

            HttpResponse response = httpClient.execute(post);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = null;
            try {
                entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity, charsetReturn);
                    LOG.info("result = " + result);
                    return result;

                }
            } catch (Exception e) {
                LOG.error("HttpClient   请求 http状态码 status = [" + status
                        + "]  获取HttpEntity ", e);
            } finally {
                if (entity != null) {
                    entity.getContent().close();
                }
            }
        } catch (ClientProtocolException e) {
            LOG.error("HttpClient   请求  ClientProtocolException ", e);
        } catch (IOException e) {
            LOG.error("HttpClient   请求  IOException ", e);
        }
        return null;
    }

    public static String sendGet(String url, Map<String, String> params, String charSet, HttpHost proxy,
                                 String authorization, String interfacename) {
        try {
            StringBuffer urlbuf = new StringBuffer(url);
            if (params != null) {
                int n = 0;
                for (Map.Entry<String, String> set : params.entrySet()) {
                    if (!urlbuf.toString().contains("?")) {
                        urlbuf.append("?");
                    }
                    if (n != 0) {
                        urlbuf.append("&");
                    }
                    urlbuf.append(set.getKey()).append("=").append(set.getValue());
                    n++;
                }
            }
            LOG.info("get = " + urlbuf.toString());
            HttpGet get = new HttpGet(urlbuf.toString());
            get.setHeader("Content-Type", HttpConstant.CONTENT_TYPE_FORM);
            get.setHeader("Authorization", authorization);
            // HttpUriRequest get = new HttpGet(urlbuf.toString());
            RequestConfig.Builder builder = RequestConfig.custom();
            if (proxy != null) {
                builder.setProxy(proxy);
            }

            RequestConfig defaultConfig = builder.setSocketTimeout(HttpConstant.SOCKET_TIMEOUT)
                    .setConnectTimeout(HttpConstant.CONNECT_TIMEOUT)
                    .setConnectionRequestTimeout(HttpConstant.REQUEST_TIMEOUT).setExpectContinueEnabled(false)
                    .setRedirectsEnabled(true).build();
            get.setConfig(defaultConfig);

            HttpResponse response = httpClient.execute(get);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = null;
            try {
                entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity, charSet);
                    LOG.info(interfacename + "result = " + result);
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.info("HttpClient   请求 http状态码 status = [" + status + "]  ");
            } finally {
                if (entity != null) {
                    entity.getContent().close();
                }
            }
        } catch (ClientProtocolException e) {
            LOG.info("HttpClient   请求  ClientProtocolException ");
        } catch (IOException e) {
            LOG.info("HttpClient   请求  IOException ");
        }
        return null;
    }

    private static List<NameValuePair> mapToList(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String value = entry.getValue().toString();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
        }
        return pairs;
    }

    public static String doXmlPost(String url, String xml) {
        HttpPost post = new HttpPost(url);
        try {
            StringEntity entity = new StringEntity(xml);
            post.setEntity(entity);
            post.setHeader("Content-Type", "text/xml;charset=UTF-8");
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            String result = acquireResult(httpResponse, post);
            LOG.info("\r\n" + new Date().toString() + "--");
            LOG.info(result + "\r\n");
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 获取Http Get/Post请求中返回的数据
     * @param response 服务器返回response
     * @param requestBase HttpGet/HttpPost 对象
     *
     * @return String 服务器返回数据
     * */
    private static String acquireResult(CloseableHttpResponse response, HttpRequestBase requestBase)
            throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            requestBase.abort();
            throw new RuntimeException("HttpClient,error status code :" + statusCode);
        }
        HttpEntity entity = response.getEntity();
        String result = null;
        if (entity != null) {
            result = EntityUtils.toString(entity, "utf-8");
        }
        EntityUtils.consume(entity);
        return result;
    }

    /**
     * cifangf 是对"App Key:App Secret"进行 Base64 编码后的字符串（区分大小写，包含冒号，但不包含双引号,采用
     * UTF-8 编码）。 例如: Authorization: Basic eHh4LUtleS14eHg6eHh4LXNlY3JldC14eHg=
     * 其中App Key和App Secret可在开放平台上创建应用后获取。
     */
    public static String Base64(String appkey,String  appsecret,String basic) {
        String str = appkey+":"+appsecret;
        byte[] encodeBase64 = new byte[0];
        try {
            encodeBase64 = Base64.encodeBase64(str
                    .getBytes(HttpUtil.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        LOG.info("\n功能名称：AppKey:AppSecret Base64编码"+"\n"+new String(encodeBase64));
        return basic+new String(encodeBase64);
    }
}
