package com.example.spboot.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component 
public class MailConsumer {
    
    @RabbitListener(queues = "mail-queue")
    public void print(String message){
        System.out.println(message);
    }

}
