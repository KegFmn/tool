package com.likc.tool.config;


import com.likc.tool.util.HttpUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

/**
 * 异常日志消息提醒
 *
 */

@Service
public class LarkAlarmAppender extends AbstractAlarmAppender {

    //这里替换为你的机器人的webHookUrl
    private final String WEBHOOK_URL = "https://open.feishu.cn/open-apis/bot/v2/hook/0ae08605-3493-4f92-a5f4-1f20b599f0ee";

    /**
     * 可以改写monitor方法来实现给其他软件发送通知、或者发邮件
     * @param messageText 消息文本
     */
    @Override
    protected void monitor(String messageText) {
        String message = String.format("{\"msg_type\":\"interactive\",\"card\"" +
                ":{\"elements\":[{\"tag\":\"div\",\"text\":{\"content\":\"%s\",\"tag\":\"lark_md\"}}]," +
                "\"header\":{\"title\":{\"content\":\"线上日志告警\",\"tag\":\"plain_text\"}}}}",
                StringEscapeUtils.escapeJson(messageText));
        HttpUtils.postJson(WEBHOOK_URL, message);
    }
}
