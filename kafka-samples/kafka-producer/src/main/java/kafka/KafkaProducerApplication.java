package kafka;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import kafka.model.Transaction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaProducerApplication {

    private final Producer<String, String> producer;
    final String outTopic;

    public KafkaProducerApplication(final Producer<String, String> producer,
                                    final String topic) {
        this.producer = producer;
        outTopic = topic;
    }

    public Future<RecordMetadata> produce(final Transaction transaction, final String message) {
//        final String[] parts = message.split("-");

//        if (parts.length > 1) {
//            key = parts[0];
//            value = parts[1];
//        } else {
//            key = null;
//            value = parts[0];
//        }

        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(outTopic, transaction.getPurchaseChannel().getChannel().name(), message);
//        TODO: add headers for additional routing capabilities
        producerRecord.headers().add("STORE_NAME", transaction.getPurchaseChannel().getStoreName().getBytes(StandardCharsets.UTF_8));
        return producer.send(producerRecord);
    }

    public void shutdown() {
        producer.close();
    }

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties envProps = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

        return envProps;
    }

    public void printMetadata(final Collection<Future<RecordMetadata>> metadata,
                              final String fileName) {
        System.out.println("Offsets and timestamps committed in batch from " + fileName);
        metadata.forEach(m -> {
            try {
                final RecordMetadata recordMetadata = m.get();
                System.out.println("Record written to offset " + recordMetadata.offset() + " timestamp " + recordMetadata.timestamp());
            } catch (InterruptedException | ExecutionException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "This program takes two arguments: the path to an environment configuration file and" +
                            "the path to the file with records to send");
        }

        final Properties props = KafkaProducerApplication.loadProperties(args[0]);
        final String topic = props.getProperty("output.topic.name");
        final Producer<String, String> producer = new KafkaProducer<>(props);
        final KafkaProducerApplication producerApp = new KafkaProducerApplication(producer, topic);

        String filePath = args[1];
        try {
            int numMessagesToSend = Integer.parseInt(props.getProperty("numMessagesToSend"));
            while (numMessagesToSend > 0) {
                //            List<String> linesToProduce = Files.readAllLines(Paths.get(filePath));
                Transaction transaction = TransactionGenerator.generateRandomOrder();
                JAXBContext context = JAXBContext.newInstance(Transaction.class);
                Marshaller marshaller = context.createMarshaller();

                StringWriter sw = new StringWriter();
                marshaller.marshal(transaction,sw);
                String xmlContent = sw.toString();
                System.out.println(xmlContent);
                Future<RecordMetadata> metadata = producerApp.produce(transaction, sw.toString());
                producerApp.printMetadata(List.of(metadata), "testFile");
//            List<Future<RecordMetadata>> metadata = linesToProduce.stream()
//                    .filter(l -> !l.trim().isEmpty())
//                    .map(producerApp::produce)
//                    .collect(Collectors.toList());
//            producerApp.printMetadata(metadata, filePath);
                numMessagesToSend--;
            }


        } finally {
            producerApp.shutdown();
        }
    }
}
