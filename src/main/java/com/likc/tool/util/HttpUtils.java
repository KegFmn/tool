package com.likc.tool.util;

import com.ama.recharge.common.BizException;
import com.ama.recharge.common.HttpResult;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class HttpUtils {
    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";

    // 连接存活时间，单位秒
    private static final int CONNECT_TIME_TO_LIVE = 60;

    // 连接池限制
    private static final int CONNECTION_POOL_MAX = 200;

    // preRouter限制
    private static final int PER_ROUTE_MAX = 50;

    // 设置连接超时时间，单位毫秒。
    private static final int CONNECT_TIMEOUT = 20000;

    // 请求获取数据的超时时间(即响应时间)，单位毫秒。
    private static final int SOCKET_TIMEOUT = 20000;


    private static final CloseableHttpClient httpClient = initHttpClient();

    // 初始化client实例
    private static CloseableHttpClient initHttpClient() {
        return HttpClients
                .custom()
                .setDefaultRequestConfig(customRequestConfig()) // 自定义请求配置
                .setConnectionManager(poolingHttpClientConnectionManager()) // 自定义连接管理器
                .evictIdleConnections(CONNECT_TIME_TO_LIVE, TimeUnit.SECONDS) // 删除空闲连接时间
                .disableAutomaticRetries() // 关闭自动重试
                .build();
    }

    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(CONNECTION_POOL_MAX);
        connectionManager.setDefaultMaxPerRoute(PER_ROUTE_MAX);
        return connectionManager;
    }

    private static RequestConfig customRequestConfig() {
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        return RequestConfig
                .custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }


    /**
     * 发送get请求；不带请求头和请求参数
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpResult get(String url) {
        try {
            return doBaseGet(url, null, null);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    public static HttpResult get(String url, Map<String, String> query) {
        try {
            return doBaseGet(url, null, query);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    public static HttpResult get(String url, Map<String, String> query, Map<String, String> headers)  {
        try {
            return doBaseGet(url, headers, query);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    // 发送get请求；带请求头和请求参数
    public static HttpResult doBaseGet(String url, Map<String, String> headers, Map<String, String> query) throws Exception {
        // 创建http对象
        HttpGet httpGet = new HttpGet();
        return doRequestWithQuery(httpGet, url, headers, query);
    }

    // 发送post请求；不带请求头和请求参数
    public static HttpResult postJson(String url) {
        try {
            return doBasePost(url, null, null, null, null);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    // 发送post请求；带请求参数
    public static HttpResult postForm(String url, Map<String, String> form)  {
        try {
            return doBasePost(url, null, null, form, null);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    // 带请求头的json的post请求
    public static HttpResult postJson(String url, Map<String, String> headers, Object body) {
        try {
            return doBasePost(url, headers, null, null, body);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    // 发送post请求
    public static HttpResult postJson(String url, Object body) {
        try {
            return doBasePost(url, null, null, null, body);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    // 发送post请求；带请求头和请求参数
    public static HttpResult doBasePost(
            String url,
            Map<String, String> header,
            Map<String, String> query,
            Map<String, String> form,
            Object body
    ) throws Exception {
        // 创建http对象
        HttpPost httpPost = new HttpPost();
        return doRequestWithEntity(httpPost, url, header, query, form, body);
    }

    // 发送put请求；不带请求参数
    public static HttpResult put(String url) {
        try {
            return doBasePut(url, null, null, null, null);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    public static HttpResult put(String url, String body) {
        try {
            return doBasePut(url, null, null, null, body);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    public static HttpResult put(String url, Map<String, String> headers, String body) {
        try {
            return doBasePut(url, headers, null, null, body);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    // 发送put请求；带请求参数
    public static HttpResult doBasePut(
            String url,
            Map<String, String> header,
            Map<String, String> query,
            Map<String, String> form,
            String body
    ) throws Exception {
        HttpPut httpPut = new HttpPut(url);
        return doRequestWithEntity(httpPut, url, header, query, form, body);
    }

    // 发送delete请求；不带请求参数
    public static HttpResult doDelete(String url) {
        try {
            return doBaseDelete(url, null, null);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    public static HttpResult doDeleteWithQuery(String url, Map<String, String> query) {
        try {
            return doBaseDelete(url, null, query);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");

    }

    public static HttpResult doDeleteWithHeaderAndQuery(String url, Map<String, String> header, Map<String, String> query) {
        try {
            return doBaseDelete(url, header, query);
        } catch (Exception e) {
            log.error("请求失败", e);
        }

        throw new BizException("请求失败");
    }

    public static HttpResult doBaseDelete(
            String url,
            Map<String, String> headers,
            Map<String, String> query
    ) throws Exception {
        HttpDelete httpDelete = new HttpDelete(url);
        return doRequestWithQuery(httpDelete, url, headers, query);
    }

    private static void packageURI(String url, Map<String, String> params, HttpRequestBase httpRequestBase) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (MapUtils.isNotEmpty(params)) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue());
            }
        }
        httpRequestBase.setURI(uriBuilder.build());
    }

    private static HttpResult doRequestWithQuery(
            HttpRequestBase httpRequestBase,
            String url,
            Map<String, String> headers,
            Map<String, String> query
    ) throws Exception {
        packageHeader(headers, httpRequestBase);
        packageURI(url, query, httpRequestBase);
        return getHttpClientResult(httpRequestBase);
    }

    private static HttpResult doRequestWithEntity(
            HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase,
            String url,
            Map<String, String> header,
            Map<String, String> query,
            Map<String, String> form,
            Object body
    ) throws Exception {
        if (Objects.nonNull(body) && MapUtils.isNotEmpty(form)) {
            throw new IllegalArgumentException("表单和请求体单次请求只支持其中一种");
        }

        // 创建http对象
        packageHeader(header, httpEntityEnclosingRequestBase);
        packageURI(url, query, httpEntityEnclosingRequestBase);
        packageUrlEncodeForm(form, httpEntityEnclosingRequestBase);
        packageJsonBody(body, httpEntityEnclosingRequestBase);
        return getHttpClientResult(httpEntityEnclosingRequestBase);
    }

    // 封装请求头
    private static void packageHeader(Map<String, String> headers, HttpRequestBase httpMethod) {
        // 设置请求头
		/*httpPost.setHeader("Cookie", "");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");*/
        // 封装请求头
        if (MapUtils.isNotEmpty(headers)) {
            Set<Map.Entry<String, String>> entrySet = headers.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    // 封装请求参数
    private static void packageUrlEncodeForm(Map<String, String> params, HttpEntityEnclosingRequestBase httpMethod) throws UnsupportedEncodingException {
        // 封装请求参数
        if (MapUtils.isNotEmpty(params)) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // 设置到请求的http对象中
            httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, ENCODING));
        }
    }

    private static void packageJsonBody(Object body, HttpEntityEnclosingRequestBase httpRequest) {
        if (Objects.nonNull(body)) {
            httpRequest.setEntity(new StringEntity(JsonUtils.to(body), ContentType.APPLICATION_JSON));
        }
    }

    // 获得响应结果
    private static HttpResult getHttpClientResult(HttpRequestBase httpMethod) throws Exception {
        // 执行请求
        @Cleanup
        CloseableHttpResponse httpResponse = httpClient.execute(httpMethod);

        // 获取返回结果
        if (Objects.nonNull(httpResponse) && Objects.nonNull(httpResponse.getStatusLine())) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(httpResponse.getEntity(), ENCODING);
            Map<String, String> headerMap = Arrays.stream(httpResponse.getAllHeaders()).collect(Collectors.toMap(Header::getName, Header::getValue, (v1, v2) -> v1));
            return new HttpResult(statusCode, content, headerMap);
        }

        return new HttpResult(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}
