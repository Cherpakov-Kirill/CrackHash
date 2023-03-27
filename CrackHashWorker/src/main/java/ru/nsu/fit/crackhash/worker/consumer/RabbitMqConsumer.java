package ru.nsu.fit.crackhash.worker.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

import java.io.IOException;

@Slf4j
@Component
@EnableRabbit
public class RabbitMqConsumer {

    @RabbitListener(queues = "crack-hash-queue")
    public void processQueue(CrackHashManagerRequest message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received from crack-hash-queue: " + message);
        channel.basicAck(tag, false);
    }
}
