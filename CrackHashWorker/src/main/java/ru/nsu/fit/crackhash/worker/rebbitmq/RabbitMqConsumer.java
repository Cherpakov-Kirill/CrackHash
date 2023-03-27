package ru.nsu.fit.crackhash.worker.rebbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.fit.crackhash.worker.service.WorkerService;

@Slf4j
@Component
@EnableRabbit
public class RabbitMqConsumer {

    private final WorkerService workerService;

    private final RabbitMqProducer rabbitMqProducer;

    public RabbitMqConsumer(WorkerService workerService, RabbitMqProducer rabbitMqProducer) {
        this.workerService = workerService;
        this.rabbitMqProducer = rabbitMqProducer;
    }

    @RabbitListener(queues = "ManagerToWorkers")
    public void processQueue(CrackHashManagerRequest message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        workerService.initTask(message);
        rabbitMqProducer.cacheConsumerTag(channel, tag, message);
    }
}
