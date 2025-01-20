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

    // 连接池限制
    private static final int CONNECTION_POOL_MAX = 200;

    // preRouter限制
    private static final int PER_ROUTE_MAX = 50;

    // 连接存活时间，单位秒
    private static final int CONNECT_TIME_TO_LIVE = 60;

    // 设置请求连接超时时间，单位秒。
    private static final int CONNECT_REQUEST_TIMEOUT = 60;

    // 请求socket的超时时间(即响应时间)，单位秒。
    private static final int SOCKET_TIMEOUT = 60;

    // 请求响应超时时间(即响应时间)，单位秒。
    private static final int RESPONSE_TIMEOUT = 60;

    private static final CloseableHttpClient httpClient = initHttpClient();

    // 初始化client实例
    private static CloseableHttpClient initHttpClient() {
        return HttpClients.custom()
                .disableAutomaticRetries() // 关闭自动重试
                .setConnectionManager(connectionManager()) // 自定义连接管理器
                .setDefaultRequestConfig(requestConfig()) // 自定义请求配置
                .evictIdleConnections(TimeValue.ofSeconds(CONNECT_TIME_TO_LIVE)) // 删除空闲连接时间
                .build();
    }

    private static PoolingHttpClientConnectionManager connectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(socketConfig())
                .setMaxConnTotal(CONNECTION_POOL_MAX)
                .setMaxConnPerRoute(PER_ROUTE_MAX)
                .build();
    }

    private static SocketConfig socketConfig() {
        return SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(SOCKET_TIMEOUT))
                .build();
    }

    private static RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(CONNECT_REQUEST_TIMEOUT))
                .setResponseTimeout(Timeout.ofSeconds(RESPONSE_TIMEOUT))
                .build();
    }

    /**
     *  发送get请求
     */
    public static HttpResult get(String url) {
        return doBaseGet(url, null, null);
    }

    public static HttpResult get(String url, Map<String, String> query) {
        return doBaseGet(url, null, query);
    }

    public static HttpResult get(String url, Map<String, String> query, Map<String, String> headers)  {
        return doBaseGet(url, headers, query);
    }

    /**
     *  发送post请求
     */
    public static HttpResult postJson(String url) {
        return doBasePost(url, null, null, null, null);
    }

    public static HttpResult postForm(String url, Map<String, String> form)  {
        return doBasePost(url, null, null, form, null);
    }

    public static HttpResult postJson(String url, Map<String, String> headers, Object body) {
        return doBasePost(url, headers, null, null, body);
    }

    public static HttpResult postJson(String url, Object body) {
        return doBasePost(url, null, null, null, body);
    }

    /**
     *  发送put请求
     */
    public static HttpResult put(String url) {
        return doBasePut(url, null, null, null, null);
    }

    public static HttpResult put(String url, String body) {
        return doBasePut(url, null, null, null, body);
    }

    public static HttpResult put(String url, Map<String, String> headers, String body) {
        return doBasePut(url, headers, null, null, body);
    }

    /**
     *  发送delete请求
     */
    public static HttpResult delete(String url) {
        return doBaseDelete(url, null, null);
    }

    public static HttpResult delete(String url, Map<String, String> query) {
        return doBaseDelete(url, null, query);
    }

    public static HttpResult delete(String url, Map<String, String> header, Map<String, String> query) {
        return doBaseDelete(url, header, query);
    }

    // get基础请求器
    public static HttpResult doBaseGet(String url, Map<String, String> headers, Map<String, String> query) {
        // 创建http对象
        HttpGet httpGet = new HttpGet(url);
        return doRequestWithQuery(httpGet, headers, query);
    }

    // post基础请求器
    public static HttpResult doBasePost(String url, Map<String, String> headers, Map<String, String> query, Map<String, String> form, Object body) {
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        return doRequestWithEntity(httpPost, headers, query, form, body);
    }

    // put基础请求器
    public static HttpResult doBasePut(String url, Map<String, String> headers, Map<String, String> query, Map<String, String> form, Object body) {
        HttpPut httpPut = new HttpPut(url);
        return doRequestWithEntity(httpPut, headers, query, form, body);
    }

    // delete基础请求器
    public static HttpResult doBaseDelete(String url, Map<String, String> headers, Map<String, String> query)  {
        HttpDelete httpDelete = new HttpDelete(url);
        return doRequestWithQuery(httpDelete, headers, query);
    }

    // query基础请求器
    private static HttpResult doRequestWithQuery(HttpUriRequest httpRequestBase, Map<String, String> headers, Map<String, String> query)  {
        packageHeader(headers, httpRequestBase);
        packageURI(query, httpRequestBase);
        return baseExecute(httpRequestBase);
    }

    // entity基础请求器
    private static HttpResult doRequestWithEntity(HttpUriRequest httpUriRequest, Map<String, String> header, Map<String, String> query, Map<String, String> form, Object body) {
        if (Objects.nonNull(body) && MapUtils.isNotEmpty(form)) {
            throw new IllegalArgumentException("表单和请求体单次请求只支持其中一种");
        }

        // 创建http对象
        packageHeader(header, httpUriRequest);
        packageURI(query, httpUriRequest);
        packageUrlEncodeForm(form, httpUriRequest);
        packageJsonBody(body, httpUriRequest);
        return baseExecute(httpUriRequest);
    }

    // 封装Uri
    private static void packageURI(Map<String, String> params, HttpUriRequest httpRequestBase) {
        if (MapUtils.isEmpty(params)) {
            return;
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(httpRequestBase.getUri());

            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue());
            }

            httpRequestBase.setUri(uriBuilder.build());
        } catch (Exception e) {
            log.error("封装请求URI失败", e);
            throw new BizException("封装请求URI失败");
        }
    }

    // 封装请求头
    private static void packageHeader(Map<String, String> headers, HttpUriRequest httpMethod) {
        if (MapUtils.isEmpty(headers)) {
            return;
        }

        // 封装请求头
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            // 设置到请求头到HttpRequestBase对象中
            httpMethod.setHeader(entry.getKey(), entry.getValue());
        }

    }

    // 封装请求参数
    private static void packageUrlEncodeForm(Map<String, String> params, HttpUriRequest httpMethod) {
        if (MapUtils.isEmpty(params)) {
            return;
        }

        // 封装请求参数
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        Set<Map.Entry<String, String>> entrySet = params.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        // 设置到请求的http对象中
        httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, Charset.forName(ENCODING)));
    }

    // 封装Json Body
    private static void packageJsonBody(Object body, HttpUriRequest httpRequest) {
        if (Objects.isNull(body)) {
            return;
        }

        httpRequest.setEntity(new StringEntity(JsonUtils.to(body), ContentType.APPLICATION_JSON));
    }

    // 底层请求器
    private static HttpResult baseExecute(ClassicHttpRequest httpMethod) {

        // 定义响应处理器
        HttpClientResponseHandler<HttpResult> responseHandler = response -> {
            String content = EntityUtils.toString(response.getEntity(), Charset.forName(ENCODING));
            Map<String, String> headerMap = Arrays.stream(response.getHeaders()).collect(Collectors.toMap(Header::getName, Header::getValue, (v1, v2) -> v1));
            return new HttpResult(response.getCode(), content, headerMap);
        };

        try {
            return httpClient.execute(httpMethod, responseHandler);
        } catch (IOException e) {
            log.error("请求失败", e);
            throw new BizException("请求失败");
        }
    }
}
