package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.dataprovider.messaging.dto.TransferenceCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NotificationConsumerIT — notificação com mock externo")
class NotificationConsumerIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestMockConfig {
        @Bean
        @Primary
        RestTemplate mockRestTemplate() {
            return Mockito.mock(RestTemplate.class);
        }
    }

    @Test
    @DisplayName("Should send notification and persist sent flag in Redis")
    void shouldSendNotificationOnCompletedTransference() {
        when(restTemplate.postForEntity(
                eq("https://util.devi.tools/api/v1/notify"),
                any(),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        var event = new TransferenceCompletedEvent(1L, 10L, 20L, BigDecimal.valueOf(150));
        rabbitTemplate.convertAndSend(
                "transference.events",
                "transference.completed",
                event);

        await().untilAsserted(() -> {
            var notification = redisTemplate.opsForValue().get("notification:sent:1");
            assertThat(notification).isEqualTo("sent");
        });
    }
}
