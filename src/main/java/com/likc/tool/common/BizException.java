package com.likc.tool.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizException extends RuntimeException {
    private Integer code;
    private String msg;
    private String enMsg;
    private Object data;

    public BizException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BizException(String msg, String enMeg) {
        super(msg);
        this.msg = msg;
        this.enMsg = enMeg;
    }
}


