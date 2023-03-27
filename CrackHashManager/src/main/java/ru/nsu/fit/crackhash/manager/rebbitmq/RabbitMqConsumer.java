package ru.nsu.fit.crackhash.manager.rebbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.fit.crackhash.manager.service.CrackService;

import java.io.IOException;

@Slf4j
@Component
@EnableRabbit
public class RabbitMqConsumer {

    private final CrackService crackService;

    public RabbitMqConsumer(CrackService crackService) {
        this.crackService = crackService;
    }

    @RabbitListener(queues = "WorkersToManager")
    public void processQueue(CrackHashWorkerResponse message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        crackService.handleResult(message);
        channel.basicAck(tag, false);
    }
}
