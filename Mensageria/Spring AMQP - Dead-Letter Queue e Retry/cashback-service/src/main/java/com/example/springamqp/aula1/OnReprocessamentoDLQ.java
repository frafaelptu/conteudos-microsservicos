package com.example.springamqp.aula1;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OnReprocessamentoDLQ {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @RabbitListener(queues = "orders.reprocessamento.dlq")
    public void onReprocessamento(Message failedMessage){
        String exchange_fila = (String) failedMessage.getMessageProperties().getHeaders().get("x-first-death-exchange");
        //throw new RuntimeException("Erro reprocessamento");
        rabbitTemplate.convertAndSend(exchange_fila, "", failedMessage);


    }
}
