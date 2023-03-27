package ru.nsu.fit.crackhash.manager.rebbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

@Slf4j
@Component
@EnableRabbit
public class RabbitMqProducer {

    @Value("${crackHashManager.rabbitmq.queue.manager-to-workers}")
    private String senderQueue;

    private final AmqpTemplate amqpTemplate;

    public RabbitMqProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendMessage(CrackHashManagerRequest request) {
        log.info("Set {} part of {} task request was sent", request.getPartNumber(), request.getRequestId());
        amqpTemplate.convertAndSend(senderQueue, request);
    }
}
