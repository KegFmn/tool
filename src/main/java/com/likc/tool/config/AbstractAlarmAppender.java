package com.likc.tool.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.likc.tool.util.TimeUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * error日志处理类
 *
 */
public abstract class AbstractAlarmAppender extends AppenderBase<LoggingEvent> {

    @Override
    protected void append(LoggingEvent eventObject) {
        try {
            Level level = eventObject.getLevel();
            if (Level.ERROR != level) {
                // 只处理error级别的报错
                return;
            }
            //构造消息通知内容
            String message = initMessage(eventObject);
            //发送消息通知
            monitor(message);
        } catch (Exception e) {
            addError("日志报警异常，异常原因:{}", e);
        }
    }

    /**
     * 拼接异常消息体
     */
    String initMessage(LoggingEvent eventObject) throws UnknownHostException {
        //获取异常堆栈信息
        IThrowableProxy proxy = eventObject.getThrowableProxy();
        String track = "";
        String trackMessage = "";
        if (proxy != null) {
            Throwable t = ((ThrowableProxy) proxy).getThrowable();
            //避免堆栈消息过长，截取前1000个字符
            track = t.toString().length() <= 500 ? t.toString() : t.toString().substring(0, 500);
            trackMessage = Arrays.toString(t.getStackTrace()).length() <= 500 ? Arrays.toString(t.getStackTrace()) : Arrays.toString(t.getStackTrace()).substring(0, 500);
        }


        InetAddress localHost = InetAddress.getLocalHost();

        String template = "**异常主机**: %s \n**异常来源**: %s \n**日志内容**: %s \n**异常时间**: %s \n**异常描述**: %s \n**详细信息**: %s";
        return String.format(template,
                localHost.getHostName(),
                //LoggerName表示生成该日志记录器的名字，即打印日志的类的完整类路径
                eventObject.getLoggerName(),
                //日志内容
                eventObject.getFormattedMessage(),
                //异常时间
                TimeUtils.formatLocalDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"),
                //异常描述(异常类型)
                track,
                //异常堆栈信息
                trackMessage);
    }

    protected abstract void monitor(String messageText);
}