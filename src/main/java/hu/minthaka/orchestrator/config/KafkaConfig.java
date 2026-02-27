package hu.minthaka.orchestrator.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  @Bean
  public NewTopic doctorCommandTopic() {
    return TopicBuilder.name("doctor.command")
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic doctorAnswersTopic() {
    return TopicBuilder.name("doctor.answer")
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic appointmentAnswersTopic() {
    return TopicBuilder.name("appointment.answer")
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic appointmentCommandTopic() {
    return TopicBuilder.name("appointment.command")
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic mailCommandsTopic() {
    return TopicBuilder.name("mail.command")
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic mailAnswersTopic() {
    return TopicBuilder.name("mail.answer")
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "health-doctor-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // 1. Configure the Jackson Deserializer
    JacksonJsonDeserializer<Object> jacksonDeserializer = new JacksonJsonDeserializer<>();

    Map<String, Object> configs = new HashMap<>();

    // 1. Trust everything in your common DTO package + the short tokens
    // Using "*" is also fine if you trust all producers in your Docker network
    configs.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");

    // 2. Define all your mappings here
    configs.put(JacksonJsonDeserializer.TYPE_MAPPINGS,
        "doctor:hu.minthaka.common.dto.DoctorMessageDTO," +
            "appointment:hu.minthaka.common.dto.AppointmentMessageDTO," +
            "mail:hu.minthaka.common.dto.MailMessageDTO"
    );

    // Apply configuration to the deserializer
    jacksonDeserializer.configure(configs, false);

    // 2. Wrap in ErrorHandlingDeserializer
    // This prevents the consumer from stopping if a message is completely unparseable
    ErrorHandlingDeserializer<Object> errorHandlingDeserializer =
        new ErrorHandlingDeserializer<>(jacksonDeserializer);

    return new DefaultKafkaConsumerFactory<>(
        props,
        new StringDeserializer(),
        errorHandlingDeserializer
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}