package com.example.springamqp.aula1;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queueCashback() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "orders.v1.order-created.dlx");
        args.put("x-max-priority", 10);
        return new Queue("orders.v1.order-created.generate-cashback", true, false, false, args);
    }

    @Bean
    public Binding binding() {
        Queue queue = queueCashback();
        FanoutExchange exchange = new FanoutExchange("orders.v1.order-created");
        return BindingBuilder.bind(queue).to(exchange);
    }
    @Bean
    public FanoutExchange fanoutExchangeDLX() {
        return new FanoutExchange("orders.v1.order-created.dlx");
    }


    @Bean
    public Queue queueReprocessaDLQ() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "orders.v1.order-created.dlx");
        args.put("x-max-priority", 10);
        return new Queue("orders.reprocessamento.dlq", true, false, false, args);
    }

    @Bean
    public Queue queueCashbackDLQ() {
        return new Queue("orders.dead-letter");
    }

    @Bean
    public Binding bindingDLQ() {
        Queue queue = queueCashbackDLQ();
        FanoutExchange exchange = new FanoutExchange("orders.v1.order-created.dlx");
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(
            RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

}
