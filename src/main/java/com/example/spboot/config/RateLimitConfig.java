package com.example.spboot.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public ProxyManager<byte[]> proxyManager() {
        RedisURI redisUri = RedisURI.create(redisHost, redisPort);
        RedisClient redisClient = RedisClient.create(redisUri);
        var connection = redisClient.connect(ByteArrayCodec.INSTANCE);
        return Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(
                        //kova doldugunda ipyi tutmamiza gerek kalmicak
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1))
                )
                .build();
    }

}
