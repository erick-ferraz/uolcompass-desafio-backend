package br.com.uolcompass.entrypoints.config.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitMQConfig {

    public static final String EXCHANGE      = "notification.events";
    public static final String SEND_QUEUE    = "notification.send";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationSendQueue() {
        return QueueBuilder.durable(SEND_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SEND_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue notificationSendDlq() {
        return new Queue(SEND_QUEUE + ".dlq");
    }

    @Bean
    public Binding bindNotificationSend() {
        return BindingBuilder.bind(notificationSendQueue())
                .to(notificationExchange()).with(SEND_QUEUE);
    }
}
