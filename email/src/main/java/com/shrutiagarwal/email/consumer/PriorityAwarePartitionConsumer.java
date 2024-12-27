package com.shrutiagarwal.email.consumer;

import com.shrutiagarwal.email.service.MessageHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static com.shrutiagarwal.email.constants.Constants.GROUP_ID;
import static com.shrutiagarwal.email.constants.Constants.TOPIC;

/**
 * A Kafka consumer implementation for prioritizing message processing across
 * partitions of a single topic based on their priority levels.
 * <p>
 * This consumer listens to a Kafka topic with multiple partitions and ensures that:
 * - Messages from the highest-priority partition (Partition 0) are processed first.
 * - Messages from lower-priority partitions are processed only if no higher-priority messages are pending.
 * - Partitions are dynamically paused and resumed based on message availability.
 * </p>
 */
@Component
@Slf4j
public class PriorityAwarePartitionConsumer {

    private final ConsumerFactory consumerFactory;
    private final MessageHandlerService messageHandlerService;

    /**
     * Constructor for PriorityAwarePartitionConsumer.
     *
     * @param consumerFactory        Factory to create Kafka consumers.
     * @param messageHandlerService  Service to process messages received from Kafka.
     */
    public PriorityAwarePartitionConsumer(ConsumerFactory consumerFactory, MessageHandlerService messageHandlerService) {
        this.consumerFactory = consumerFactory;
        this.messageHandlerService = messageHandlerService;
    }

    /**
     * Event listener triggered when the application starts.
     * Initializes the Kafka consumer, assigns specific partitions, and begins consuming messages
     * with priority-based processing logic.
     *
     * @param applicationStartedEvent Event triggered when the application starts.
     */
    @EventListener
    public void onAppStarted(ApplicationStartedEvent applicationStartedEvent) {
        // Create a Kafka consumer using the ConsumerFactory
        KafkaConsumer<String, String> consumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer(GROUP_ID, "email-consumer");

        // Define partitions for the topic, corresponding to priority levels
        TopicPartition PARTITION_PRIORITY_1 = new TopicPartition(TOPIC, 0); // Highest priority
        TopicPartition PARTITION_PRIORITY_2 = new TopicPartition(TOPIC, 1); // Medium priority
        TopicPartition PARTITION_PRIORITY_3 = new TopicPartition(TOPIC, 2); // Lowest priority

        // Explicitly assign partitions to the consumer
        consumer.assign(Arrays.asList(PARTITION_PRIORITY_1, PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));

        // Continuous message consumption and processing loop
        while (true) {
            try {
                // Fetch end offsets for all partitions to determine how many messages are pending
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Arrays.asList(
                        PARTITION_PRIORITY_1, PARTITION_PRIORITY_2, PARTITION_PRIORITY_3
                ));

                // Get the current consumer position (offset) for each partition
                long positionP1 = consumer.position(PARTITION_PRIORITY_1);
                long positionP2 = consumer.position(PARTITION_PRIORITY_2);
                long positionP3 = consumer.position(PARTITION_PRIORITY_3);

                // Check if Partition 1 (highest priority) has pending messages
                boolean hasPriority1Messages = (endOffsets.get(PARTITION_PRIORITY_1) - positionP1) > 0;

                if (hasPriority1Messages) {
                    // Pause lower-priority partitions while processing high-priority messages
                    consumer.pause(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                } else {
                    // Check if Partition 2 (medium priority) has pending messages
                    boolean hasPriority2Messages = (endOffsets.get(PARTITION_PRIORITY_2) - positionP2) > 0;

                    if (hasPriority2Messages) {
                        // Pause the lowest-priority partition and process medium-priority messages
                        consumer.pause(Arrays.asList(PARTITION_PRIORITY_3));
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2));
                    } else {
                        // Resume all partitions if no higher-priority messages are pending
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                    }
                }

                // Poll for new messages from active partitions
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(record -> processRecord(record)); // Process each message

            } catch (Exception e) {
                log.error("Error while consuming messages: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Processes an individual Kafka record by logging its details and delegating message handling.
     *
     * @param record Kafka consumer record containing the message to be processed.
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        log.debug("Record Received: Offset: {}, Key: {}, Value: {}", record.offset(), record.key(), record.value());
        processMessage(record.value());
    }

    /**
     * Delegates message processing to the MessageHandlerService.
     *
     * @param message The message to be processed.
     */
    private void processMessage(String message) {
        // Delegate the message to the service for further processing
        messageHandlerService.handleEmailRequest(message);
    }
}
