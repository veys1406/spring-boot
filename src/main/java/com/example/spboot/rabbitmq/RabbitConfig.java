package com.example.spboot.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.spboot.dto.MailUsername;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue mail_queue(){
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "message.rejected");
        return new Queue("mail-queue",true,false,false,arguments);
    }

    @Bean
    public DirectExchange mail_exchange(){
        return new DirectExchange("user.registered");
    }

    @Bean
    public Binding binding(Queue mail_queue, DirectExchange mail_exchange){
        return BindingBuilder.bind(mail_queue).to(mail_exchange).with("kullanici kaydoldu");
    }
    

    @Bean
    public Queue garbage_queue(){
        return new Queue("garbage-queue");
    }

    @Bean
    public FanoutExchange garbage_exchange(){
        return new FanoutExchange("message.rejected");
    }

    @Bean
    public Binding garbage_binding(Queue garbage_queue, FanoutExchange garbage_exchange){
        return BindingBuilder.bind(garbage_queue).to(garbage_exchange);
    }
    
    @Bean// converteri degistiriyoruz simplemessage convertere yerine json a donusturen bunu kullaniyoruz
    public JacksonJsonMessageConverter jacksonMessageConverter(DefaultClassMapper classMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("mailUsername", MailUsername.class);
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }
    
}
