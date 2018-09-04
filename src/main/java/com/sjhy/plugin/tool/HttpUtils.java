package com.sjhy.plugin.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.constants.MsgValue;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Http工具类，按需添加方法
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 14:59
 */
public final class HttpUtils {
    /**
     * 用户设备标识
     */
    private static final String USER_AGENT = "EasyCode";
    /**
     * 服务器地址
     */
    private static final String HOST_URL = "http://www.shujuhaiyang.com/easyCode";
    /**
     * http客户端
     */
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    /**
     * 请求超时时间设置(10秒)
     */
    private static final int TIMEOUT = 10 * 1000;

    /**
     * 状态码
     */
    private static final String STATE_CODE = "code";

    /**
     * 私有构造方法
     */
    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * get请求
     *
     * @param uri 请求地址
     * @return 返回请求结果
     */
    public static String get(String uri) {
        HttpGet httpGet = new HttpGet(HOST_URL + uri);
        httpGet.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        httpGet.setConfig(getDefaultConfig());
        return handlerRequest(httpGet);
    }

    /**
     * post json请求
     *
     * @param uri   地址
     * @param param 参数
     * @return 请求返回结果
     */
    public static String postJson(String uri, Map<String, Object> param) {
        HttpPost httpPost = new HttpPost(HOST_URL + uri);
        httpPost.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        httpPost.setConfig(getDefaultConfig());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (!CollectionUtil.isEmpty(param)) {
                httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(param), "utf-8"));
            }
            return handlerRequest(httpPost);
        } catch (JsonProcessingException e) {
            Messages.showWarningDialog("JSON解析出错！", MsgValue.TITLE_INFO);
            ExceptionUtil.rethrow(e);
        }
        return null;
    }

    private static RequestConfig getDefaultConfig() {
        return RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build();
    }

    /**
     * 统一处理请求
     *
     * @param request 请求对象
     * @return 响应字符串
     */
    private static String handlerRequest(HttpUriRequest request) {
        try {
            CloseableHttpResponse response = HTTP_CLIENT.execute(request);
            String body = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Messages.showWarningDialog("连接到服务器错误！", MsgValue.TITLE_INFO);
                return null;
            }
            HttpClientUtils.closeQuietly(response);
            // 解析JSON数据
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            if (jsonNode.get(STATE_CODE).asInt() == 1) {
                return jsonNode.get("data").toString();
            }
            // 获取错误消息
            String msg = jsonNode.get("msg").asText();
            Messages.showWarningDialog(msg, MsgValue.TITLE_INFO);
        } catch (IOException e) {
            Messages.showWarningDialog("无法连接到服务器！", MsgValue.TITLE_INFO);
            ExceptionUtil.rethrow(e);
        }
        return null;
    }
}
