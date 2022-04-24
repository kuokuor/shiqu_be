package com.kuokuor.shiqu.event;

import com.alibaba.fastjson.JSONObject;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.utils.EmailUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消费者
 */
@Component
public class EventConsumer {

    @Autowired
    private EmailUtil emailUtil;

    /**
     * 消费发送邮件事件
     *
     * @param record
     */
    @KafkaListener(topics = {Constants.TOPIC_SEND_MAIL})
    public void handleSendMailMessage(ConsumerRecord record) {
        // 消息内容为空
        if (record == null || record.value() == null) {
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        // 消息格式错误
        if (event == null) {
            return;
        }

        // 从data中获得数据
        Map<String, Object> data = event.getData();
        if (data == null) {
            return;
        }

        String to = (String) data.get("to");
        String subject = (String) data.get("subject");
        String content = (String) data.get("content");
        // 三个数据有一个为空就不发
        if (to == null || subject == null || content == null) {
            return;
        }

        // 调用方法发送邮件
        emailUtil.sendMail(to, subject, content);

    }

}
