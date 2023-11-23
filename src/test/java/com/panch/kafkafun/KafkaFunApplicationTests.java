package com.panch.kafkafun;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaFunApplicationTests {

	@Autowired
	SomeService someService;

//	@Test
//	public void contextLoads() {
//		try {
//			throw new RuntimeException("dick");
//		} catch (Exception e){
//			System.out.println(e);
//		} catch (RuntimeException e){
//			System.out.println(e);
//		}
//	}

	@Test
	public void asyncTest() throws Exception {
		someService.foo();
		someService.foo();
		TimeUnit.SECONDS.sleep(3);
	}

	@Test
	public void testAutoCommit() throws Exception {
		System.out.println("Start auto");
		ContainerProperties containerProps = new ContainerProperties("topic1");
		final CountDownLatch latch = new CountDownLatch(4);
		containerProps.setMessageListener(new AcknowledgingMessageListener<Integer, String>() {

			@Override
			public void onMessage(ConsumerRecord<Integer, String> message, Acknowledgment acknowledgment) {
				System.out.println("received: " + message);
				try {
					someService.handle(message.value());
					System.out.println("handled: " + message);
					latch.countDown();
//					acknowledgment.acknowledge();
				} catch (RejectedExecutionException e) {
					System.err.println("RejectedExecutionException");
				}

//				throw new RuntimeException();
//				try {
//					TimeUnit.MILLISECONDS.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}

		});
		KafkaMessageListenerContainer<Integer, String> container = createContainer(containerProps);
		container.setBeanName("testAuto");
		container.start();
		Thread.sleep(1000); // wait a bit for the container to start
		KafkaTemplate<Integer, String> template = createTemplate();
//		template.setDefaultTopic("topic1");
//		template.sendDefault(0, "foo");
//		template.sendDefault(2, "bar");
//		template.sendDefault(0, "baz");
//		template.sendDefault(2, "qux");
//		template.flush();
		assertTrue(latch.await(60, TimeUnit.SECONDS));
		TimeUnit.MILLISECONDS.sleep(5000);
		container.stop();
		System.out.println("Stop auto");
	}

	private KafkaMessageListenerContainer<Integer, String> createContainer(
			ContainerProperties containerProps) {
		containerProps.setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
//		containerProps.setAckMode(AbstractMessageListenerContainer.AckMode.RECORD);
		Map<String, Object> props = consumerProps();
		DefaultKafkaConsumerFactory<Integer, String> cf =
				new DefaultKafkaConsumerFactory<Integer, String>(props);
		KafkaMessageListenerContainer<Integer, String> container =
				new KafkaMessageListenerContainer<>(cf, containerProps);
		return container;
	}

	private KafkaTemplate<Integer, String> createTemplate() {
		Map<String, Object> senderProps = senderProps();
		ProducerFactory<Integer, String> pf =
				new DefaultKafkaProducerFactory<Integer, String>(senderProps);
		KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);
		return template;
	}

	private Map<String, Object> consumerProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return props;
	}

	private Map<String, Object> senderProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return props;
	}

}
