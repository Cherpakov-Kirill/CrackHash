package ru.nsu.fit.crackhash.manager.rebbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.fit.crackhash.manager.model.CrackTaskRequest;
import ru.nsu.fit.crackhash.manager.repository.CrackTaskRequestRepository;

import java.util.List;

@Slf4j
@Component
public class RabbitMqProducer implements ConnectionListener {

    @Value("${crackHashManager.rabbitmq.queue.manager-to-workers}")
    private String senderQueue;

    private final AmqpTemplate amqpTemplate;

    private final CrackTaskRequestRepository crackTaskRequestRepository;

    public RabbitMqProducer(AmqpTemplate amqpTemplate, CrackTaskRequestRepository crackTaskRequestRepository, ConnectionFactory connectionFactory) {
        this.amqpTemplate = amqpTemplate;
        this.crackTaskRequestRepository = crackTaskRequestRepository;
        connectionFactory.addConnectionListener(this);
    }

    public void sendMessage(CrackHashManagerRequest request) {
        try {
            amqpTemplate.convertAndSend(senderQueue, request);
            log.info("Set {} part of {} task request was sent", request.getPartNumber(), request.getRequestId());
        } catch (Exception ex) {
            log.error("Failed to send request '{}', cached message", request.getRequestId());
            crackTaskRequestRepository.save(new CrackTaskRequest(request));
        }
    }

    @Override
    public void onCreate(Connection connection) {
        List<CrackTaskRequest> requests = crackTaskRequestRepository.findAll();
        for (CrackTaskRequest request : requests) {
            CrackHashManagerRequest message = request.getRequest();
            try {
                amqpTemplate.convertAndSend(senderQueue, message);
                crackTaskRequestRepository.delete(request);
                log.info("Set {} part of {} task request was sent", message.getPartNumber(), message.getRequestId());
            } catch (Exception ex) {
                log.error("Failed to resend request '{}'", message.getRequestId());
            }
        }
    }
}
