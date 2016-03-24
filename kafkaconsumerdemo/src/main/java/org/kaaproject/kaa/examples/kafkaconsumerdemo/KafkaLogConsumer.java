package org.kaaproject.kaa.examples.kafkaconsumerdemo;

import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.kaaproject.kaa.schema.sample.logging.LogData;
import org.kaaproject.kaa.server.common.log.shared.RecordDataConverter;

import java.util.Collections;
import java.util.Properties;

public class KafkaLogConsumer extends ShutdownableThread {
    private final KafkaConsumer<Integer, String> consumer;
    private final String topic;

    public KafkaLogConsumer(String topic) {
        super("KafkaLogConsumerExample", false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "DemoConsumer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
    }

    @Override
    public void doWork() {
        consumer.subscribe(Collections.singletonList(this.topic));
        ConsumerRecords<Integer, String> records = consumer.poll(1000);
        for (ConsumerRecord<Integer, String> record : records) {
            testDecoder(record.value());
        }
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    private static void testDecoder(String message) {
        try {
            RecordDataConverter<LogData> converter = new RecordDataConverter<>(LogData.class);
            LogData data = converter.decode(message, "event");
            System.out.println(data.getLevel());
            System.out.println(data.getMessage());
            System.out.println(data.getTag());
        } catch (Exception e) {
            System.out.println("Error parsing log message, " + e.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        KafkaLogConsumer consumerThread = new KafkaLogConsumer("kaa");
        consumerThread.start();
    }
}
