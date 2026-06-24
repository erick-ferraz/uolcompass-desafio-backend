package br.com.uolcompass.entrypoints.config.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class TransferenceRabbitMQConfig {

    public static final String EXCHANGE = "transference.events";

    public static final String QUEUE_INITIATED    = "transference.initiated";
    public static final String QUEUE_DEBITED      = "transference.debited";
    public static final String QUEUE_COMPLETED    = "transference.completed";
    public static final String QUEUE_FAILED       = "transference.failed";
    public static final String QUEUE_COMPENSATED  = "transference.compensated";

    public enum TransferenceQueue {
        INITIATED(QUEUE_INITIATED),
        DEBITED(QUEUE_DEBITED),
        COMPLETED(QUEUE_COMPLETED),
        FAILED(QUEUE_FAILED),
        COMPENSATED(QUEUE_COMPENSATED);

        public final String queueName;
        public final String dlqName;

        TransferenceQueue(String queueName) {
            this.queueName = queueName;
            this.dlqName   = queueName + ".dlq";
        }
    }

    @Bean
    public TopicExchange transferenceExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Declarables transferenceDeclarables() {
        TopicExchange exchange = transferenceExchange();

        var declarables = Arrays.stream(TransferenceQueue.values())
                .flatMap(tq -> {
                    Queue queue = QueueBuilder.durable(tq.queueName)
                            .withArgument("x-dead-letter-exchange", "")
                            .withArgument("x-dead-letter-routing-key", tq.dlqName)
                            .build();
                    Queue dlq = new Queue(tq.dlqName);
                    Binding binding = BindingBuilder.bind(queue).to(exchange).with(tq.queueName);
                    return Stream.of(queue, dlq, binding);
                })
                .toList();

        return new Declarables(declarables);
    }
}
