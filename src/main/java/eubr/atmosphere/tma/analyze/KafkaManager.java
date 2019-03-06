package eubr.atmosphere.tma.analyze;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eubr.atmosphere.tma.analyze.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.TrustworthinessScore;

public class KafkaManager {

    private final static String TOPIC = "topic-planning";
    private final static String BOOTSTRAP_SERVERS =
            PropertiesManager.getInstance().getProperty("bootstrapServers");

    final Producer<Long, String> producer;

    public KafkaManager() {
        this.producer = createProducer();
    }

    public void addItemKafka(TrustworthinessScore score) throws InterruptedException, ExecutionException {
        long time = System.currentTimeMillis();
        JsonElement jsonElement = new Gson().toJsonTree(score);
        final ProducerRecord<Long, String> record =
                new ProducerRecord<>(TOPIC, time, jsonElement.toString());
        RecordMetadata metadata = this.producer.send(record).get();
        long elapsedTime = System.currentTimeMillis() - time;
        System.out.printf("sent record(key=%s value=%s) " +
                        "meta(partition=%d, offset=%d) time=%d\n",
                record.key(), record.value(), metadata.partition(),
                metadata.offset(), elapsedTime);
        this.producer.flush();
    }

    private Producer<Long, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                            BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                                        LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                                    StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    // TODO: THIS IS SYNCHRONOUS! I NEED TO CHECK IF THE ASYNCHRONOUS OPTION IS BETTER FOR US
    private void runProducer(final int sendMessageCount) throws Exception {
        long time = System.currentTimeMillis();

        try {
            for (long index = time; index < time + sendMessageCount; index++) {
                final ProducerRecord<Long, String> record =
                        new ProducerRecord<>(TOPIC, index,
                                    "Hello Mom " + index);
                RecordMetadata metadata = this.producer.send(record).get();
                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf("sent record(key=%s value=%s) " +
                                "meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(),
                        metadata.offset(), elapsedTime);
            }
        } finally {
            this.producer.flush();
            this.producer.close();
        }
    }
}
