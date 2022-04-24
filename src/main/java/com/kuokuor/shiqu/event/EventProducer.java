package com.kuokuor.shiqu.event;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 生产者
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     *
     * @param event
     */
    public void fireEvent(Event event) {
        //将事件发布到指定主题(转成JSON全放进去)
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
