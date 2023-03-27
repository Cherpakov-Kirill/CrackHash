package ru.nsu.fit.crackhash.worker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2XmlMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    @Value("${crackHashManager.rabbitmq.queue.workers-to-manager}")
    private String senderQueue;

    @Value("${crackHashManager.rabbitmq.queue.manager-to-workers}")
    private String listenerQueue;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(xmlMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Queue managerToWorkersQueue() {
        return new Queue(listenerQueue, true);
    }

    @Bean
    public Queue workersToManagerQueue() {
        return new Queue(senderQueue, true);
    }

    @Bean
    public Jackson2XmlMessageConverter xmlMessageConverter() {
        return new Jackson2XmlMessageConverter();
    }

}
