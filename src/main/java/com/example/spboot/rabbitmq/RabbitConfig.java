package com.example.spboot.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.spboot.dto.MailUsername;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue queue(){
        return new Queue("mail-queue");
    }

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange("user.registered");
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("kullanici kaydoldu");
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
