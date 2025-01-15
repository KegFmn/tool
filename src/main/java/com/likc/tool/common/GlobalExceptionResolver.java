package com.likc.tool.common;


import com.likc.tool.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionResolver {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> res = new ArrayList<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            String field = error.getField();
            Object value = error.getRejectedValue();
            String defaultMessage = error.getDefaultMessage();
            res.add(String.format("错误字段 -> %s，错误值 -> %s，原因 -> %s", field, value, defaultMessage));
        }

        log.error("POST接口传参异常，url：{}，传参校验：{}", getRequestUri(), res);

        Result<Void> result = new Result<>();
        result.setCode(400);
        result.setMsg(JsonUtils.to(res));
        return result;
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("GET接口传参异常，url：{}，传参校验：{}", getRequestUri(), ex.getMessage());

        Result<Void> result = new Result<>();
        result.setCode(400);
        result.setMsg(ex.getMessage());
        return result;
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("数据库异常，url：{}", getRequestUri(), ex);

        Result<Void> result = new Result<>();
        result.setCode(1);
        result.setMsg("数据库异常");
        return result;
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = BizException.class)
    public Result<Object> commonBizExceptionHandler(BizException e) {
        Result<Object> result = Result.success(e.getData());
        result.setCode(e.getCode() == null ? 1 : e.getCode());
        result.setMsg(e.getMsg() == null ? e.getMessage() : e.getMsg());
        return result;
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = Exception.class)
    public Result<Void> commonExceptionHandler(Exception ex) {
        log.error("系统异常，url：{}", getRequestUri(), ex);

        Result<Void> result = Result.success();
        result.setCode(1);
        result.setMsg("系统异常");
        return result;
    }

    private String getRequestUri() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request.getRequestURI();
    }
}
