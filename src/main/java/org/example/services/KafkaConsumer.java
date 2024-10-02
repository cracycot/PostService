package org.example.services;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "posts", groupId = "1")
    public void listen(ConsumerRecord<Long, String> record) {
        System.out.println("recieved messeg" + record.value());
    }
}
