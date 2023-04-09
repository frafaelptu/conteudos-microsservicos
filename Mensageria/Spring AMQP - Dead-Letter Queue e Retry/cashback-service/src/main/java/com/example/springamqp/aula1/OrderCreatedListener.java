package com.example.springamqp.aula1;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Component
public class OrderCreatedListener {

    @RabbitListener(queues = "orders.v1.order-created.generate-cashback")
    public void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("Id recebido " + event.getId());
        System.out.println("Valor recebido " + event.getValue());
        System.out.println(Date.from(Instant.now()));
        if (event.getValue().compareTo(new BigDecimal("10000")) >= 0){
           throw new RuntimeException("Pedido não pode ser processo id: " + event.getId());
        }
    }

}
