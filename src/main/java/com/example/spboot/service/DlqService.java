package com.example.spboot.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.spboot.dto.MessageResponse;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;

@Service
public class DlqService {

    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(DlqService.class);

    public DlqService(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public MessageResponse replay(int messageCount){
        int safeLimit = Math.min(messageCount, 100);

        return rabbitTemplate.execute(channel -> {
            int replayed = 0;

            for(int i=0; i<safeLimit; i++){
                GetResponse response = channel.basicGet("garbage-queue", false);

                if(response == null) break;

                Map<String, Object> headers = response.getProps().getHeaders();
                Object xOriginalExchange = headers.get("x-original-exchange");
                Object xOriginalRoutingKey = headers.get("x-original-routingKey");

                if(xOriginalExchange == null || xOriginalRoutingKey == null){

                    log.warn("{} id'li mesaj x-original header'lari olmadigi icin silindi. Payload: {}",
                    response.getProps().getMessageId(),
                    new String(response.getBody(), StandardCharsets.UTF_8));

                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    continue;
                }

                String exchange = xOriginalExchange.toString();
                String routingKey = xOriginalRoutingKey.toString();

                Map<String, Object> newHeaders = new HashMap<>(headers);
                Object oldCount = newHeaders.get("x-replay-count");

                int replayCount = (oldCount == null) ? 0 : ((Number) oldCount).intValue();
                if(replayCount >= 3){
                    channel.basicPublish("replay.exceeded", "", response.getProps(), response.getBody());
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    continue;
                }

                String exceptionMessage = headers.get("x-exception-message").toString();
                if(exceptionMessage.contains("Gecersiz mail!") || exceptionMessage.contains("Failed to convert Message content")){
                    log.warn("{} id'li mesaj poison oldugu icin silindi. Sebep: {} Payload: {}",
                        response.getProps().getMessageId(),
                        exceptionMessage,
                        new String(response.getBody(), StandardCharsets.UTF_8));
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    continue;
                }

                newHeaders.put("x-replay-count", replayCount + 1);

                AMQP.BasicProperties newProps = response.getProps().builder()
                .headers(newHeaders)
                .build();
                
                channel.basicPublish(exchange, routingKey, newProps, response.getBody());
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                log.info("{} id'li mesaj {}. kez basariyla replay edildi.", response.getProps().getMessageId(), (replayCount + 1) );
                replayed++;
            }

            return new MessageResponse(replayed + " mesaj replay edildi");
        });
    }

}
