package com.example.springamqp.aula1;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;

@RestController
@RequestMapping(value = "/v1/orders")
public class OrderController {

    @Autowired
    private OrderRepository orders;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping
    public Order create(@RequestBody Order order) {
        orders.save(order);

        final int priority;

        if (order.getValue().compareTo(new BigDecimal("5000")) >= 0) {
            priority = 10;
        } else {
            priority = 0;
        }


        final MessagePostProcessor processor = message -> {
            MessageProperties properties = message.getMessageProperties();
            properties.setPriority(priority);
            return message;
        };

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getValue());
        rabbitTemplate.convertAndSend("orders.v1.order-created", "", event, processor);

        return order;
    }

    @GetMapping
    public Collection<Order> list() {
        return orders.findAll();
    }

    @GetMapping("{id}")
    public Order findById(@PathVariable Long id) {
        return orders.findById(id).orElseThrow();
    }

    @PutMapping("{id}/pay")
    public Order pay(@PathVariable Long id) {
        Order order = orders.findById(id).orElseThrow();
        order.markAsPaid();
        return orders.save(order);
    }

}
