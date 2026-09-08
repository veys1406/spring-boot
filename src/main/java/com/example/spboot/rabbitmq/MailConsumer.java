package com.example.spboot.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.spboot.dto.MailUsername;

@Component 
public class MailConsumer {
    
    @RabbitListener(queues = "mail-queue")
    public void print(MailUsername mailUsername){
        System.out.println(mailUsername.getUserMail() +" "+ mailUsername.getUsername());
    }

}
