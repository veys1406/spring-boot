package com.example.spboot.rabbitmq;

import java.time.Duration;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.spboot.dto.MailUsername;

@Component 
public class MailConsumer {
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<Object, Object> redisTemplate;
    
    public MailConsumer(JavaMailSender javaMailSender, RedisTemplate<Object, Object> redisTemplate) {
        this.javaMailSender = javaMailSender;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = "mail-queue")
    public void print(MailUsername mailUsername, @Header(AmqpHeaders.MESSAGE_ID) String message_id){// exception atarsa nack atmazsa ack

        if(redisTemplate.hasKey(message_id)){// nulla karsi korumuyor
            System.out.println("Bu mesaj zaten gonderildi -> "+ message_id);
            return;
        }


        if( (mailUsername.getUserMail()==null) || !(mailUsername.getUserMail().contains("@"))){
            throw new AmqpRejectAndDontRequeueException("Gecersiz mail!");
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(mailUsername.getUserMail());
        simpleMailMessage.setFrom("noreply@spboot.local");
        simpleMailMessage.setSubject("Hosgeldin!");
        simpleMailMessage.setText("Aramiza hosgeldin " + mailUsername.getUsername() + ".");
        
        //at-least-once
        javaMailSender.send(simpleMailMessage);// exception firlatabilir
        redisTemplate.opsForValue().set(message_id, "mailSent", Duration.ofDays(1));
        
        

        System.out.println(message_id);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(mailUsername.getUserMail() +" "+ mailUsername.getUsername());

    }

}
