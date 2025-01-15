package com.likc.tool.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code, String msg) {
        this(code, msg, null);
    }

    public static Result<Void> fail(String msg) {
        Result<Void> result = new Result<>();
        result.setCode(1);
        result.setMsg(msg);;
        return result;
    }

    public static Result<Void> success() {
        Result<Void> result = new Result<>();
        result.setCode(0);
        result.setMsg("成功");
        return result;
    }

    public static <T> Result<T> success(T d) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg("成功");
        result.setData(d);
        return result;
    }


}
