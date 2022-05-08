package com.kuokuor.shiqu.event;

import com.alibaba.fastjson.JSONObject;
import com.kuokuor.shiqu.commom.constant.Constants;
import com.kuokuor.shiqu.dao.MessageDao;
import com.kuokuor.shiqu.entity.Message;
import com.kuokuor.shiqu.utils.EmailUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费者
 */
@Component
public class EventConsumer {

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private MessageDao messageDao;

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

    /**
     * 消费 评论(回复)-关注-点赞-收藏 事件
     *
     * @param record
     */
    @KafkaListener(topics = {Constants.TOPIC_COMMENT, Constants.TOPIC_FOLLOW,
            Constants.TOPIC_LIKE, Constants.TOPIC_COLLECT})
    private void handleCommentMessage(ConsumerRecord record) {
        // 消息内容为空
        if (record == null || record.value() == null) {
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        // 消息格式错误
        if (event == null) {
            return;
        }

        // 发送通知(往message表里插入内容)
        Message message = new Message();
        // 消息发送者[系统Id为0]
        message.setFromId(Constants.SYSTEM_USER_ID);
        // 消息接收者
        message.setToId(event.getEntityUserId());
        // 处理系统通知标题(此处存主题)
        message.setConversationId(event.getTopic());
        // 未读
        message.setState(0);
        // 时间
        message.setCreateTime(new Date());
        // 设置内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());//事件触发者
        content.put("entityType", event.getEntityType());//操作类型
        content.put("entityId", event.getEntityId());//实体类型

        // 存入其他数据
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        //内容
        message.setContent(JSONObject.toJSONString(content));

        //存入
        messageDao.insertMessage(message);
    }

}
