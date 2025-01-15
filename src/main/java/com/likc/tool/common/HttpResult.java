package com.likc.tool.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class HttpResult implements Serializable {
    private static final long serialVersionUID = -7421171673609899440L;
    /**
     * 响应状态码
     */
    private int httpCode;

    /**
     * 响应数据
     */
    private String data;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    public HttpResult(int code, String content, Map<String, String> headers) {
        this.httpCode = code;
        this.data = content;
        this.headers = headers;
    }

    public HttpResult(int code, String content) {
        this.httpCode = code;
        this.data = content;
    }

    public HttpResult(int code) {
        this.httpCode = code;
        this.data = null;
    }
}
