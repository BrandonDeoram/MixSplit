package com.demo.MixSplit.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "audio-processing-queue";
    public static final String EXCHANGE_NAME = "audio-processing-exchange";
    public static final String ROUTING_KEY = "audio.process";

    @Bean
    public Queue queue() {
        System.out.println("Creating queue: " + QUEUE_NAME);
        return new Queue(QUEUE_NAME); // Durable queue
    }

    @Bean
    public TopicExchange exchange() {
        System.out.println("Creating exchange: " + EXCHANGE_NAME);
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        System.out.println("Creating binding between queue and exchange.");
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        System.out.println("Initializing RabbitAdmin");
        return new RabbitAdmin(rabbitTemplate);
    }
}