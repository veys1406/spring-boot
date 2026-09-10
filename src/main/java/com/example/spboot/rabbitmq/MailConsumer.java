package com.example.spboot.rabbitmq;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.spboot.dto.MailUsername;

@Component 
public class MailConsumer {
    private final JavaMailSender javaMailSender;
    
    public MailConsumer(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @RabbitListener(queues = "mail-queue")
    public void print(MailUsername mailUsername){// exception atarsa nack atmazsa ack

        if( (mailUsername.getUserMail()==null) || !(mailUsername.getUserMail().contains("@"))){
            throw new AmqpRejectAndDontRequeueException("Gecersiz mail!");
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(mailUsername.getUserMail());
        simpleMailMessage.setFrom("noreply@spboot.local");
        simpleMailMessage.setSubject("Hosgeldin!");
        simpleMailMessage.setText("Aramiza hosgeldin " + mailUsername.getUsername() + ".");
        
        javaMailSender.send(simpleMailMessage);// exception firlatabilir

        System.out.println(mailUsername.getUserMail() +" "+ mailUsername.getUsername());

    }

}
