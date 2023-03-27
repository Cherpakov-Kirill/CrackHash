package ru.nsu.fit.crackhash.worker.rebbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableRabbit
public class RabbitMqProducer {

    @Value("${crackHashManager.rabbitmq.queue.workers-to-manager}")
    private String senderQueue;

    private final AmqpTemplate amqpTemplate;

    private Channel channel;

    private final Map<String, Long> requestConsumerTagMap;

    public RabbitMqProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
        this.requestConsumerTagMap = new ConcurrentHashMap<>();
    }

    public void cacheConsumerTag(Channel channel, Long tag, CrackHashManagerRequest message) {
        if (this.channel == null) this.channel = channel;
        requestConsumerTagMap.put(getKey(message.getRequestId(), message.getPartNumber()), tag);
    }

    public void sendMessage(CrackHashWorkerResponse response) {
        log.info("Result of {} part of {} task was sent", response.getPartNumber(), response.getRequestId());
        makeAck(response.getRequestId(), response.getPartNumber());
        amqpTemplate.convertAndSend(senderQueue, response);
    }

    private void makeAck(String requestId, int partNumber) {
        String key = getKey(requestId, partNumber);
        Long tag = requestConsumerTagMap.get(key);
        try {
            channel.basicAck(tag, false);
            requestConsumerTagMap.remove(key, tag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getKey(String requestId, int partNumber) {
        return requestId + "-" + partNumber;
    }
}
