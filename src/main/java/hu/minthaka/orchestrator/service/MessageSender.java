package hu.minthaka.orchestrator.service;

import hu.minthaka.common.dto.BaseMessage;
import hu.minthaka.common.enums.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageSender {

  @Autowired
  private KafkaTemplate<String, BaseMessage> kafkaTemplate;

  public void sendMessage(BaseMessage command, Topic topic) {
    if (topic == Topic.INVALID) {
      return;
    }

    Message<BaseMessage> message = MessageBuilder
        .withPayload(command)
        .setHeader(KafkaHeaders.TOPIC, topic.getName())
        .setHeader(KafkaHeaders.KEY, command.messageID().toString())
        .setHeader("messageID", command.messageID())
        .build();

    this.kafkaTemplate.send(message)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("Sent message to offset: {}", result.getRecordMetadata().offset());
            log.info("Outbox message to offset: {}", result.getProducerRecord().value());
          } else {
            log.error("Unable to send message", ex);
          }
        });
  }


}
