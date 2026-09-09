package com.example.spboot.rabbitmq;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component 
public class OwnRecoverer implements MessageRecoverer {
    private static final Logger log = LoggerFactory.getLogger(OwnRecoverer.class);

    @Override 
    public void recover(Message message, Throwable throwable){
        log.error("Mail gonderilemedi, DLQ'ya gidiyor: {}",message,throwable);
        throw new AmqpRejectAndDontRequeueException(throwable.getMessage());
    }

}
