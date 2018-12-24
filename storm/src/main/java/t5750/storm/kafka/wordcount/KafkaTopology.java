package t5750.storm.kafka.wordcount;

import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

import t5750.storm.util.KafkaUtil;
import t5750.storm.util.ZkUtil;

/**
 * Storm Kafka Integration (0.8.x)<br/>
 * http://storm.apache.org/releases/1.2.2/storm-kafka.html
 */
public class KafkaTopology {
	public static void main(String[] args)
			throws AlreadyAliveException, InvalidTopologyException {
		// zookeeper hosts for the Kafka clustere
		// ZkHosts zkHosts = new ZkHosts("192.168.100.142:2181");
		// 注意这里的Spout的来源是kafka(kafka数据流入storm)
		// SpoutConfig kafkaConfig = new SpoutConfig(zkHosts, "words_topic",
		// "","id7");
		// Specify that the kafka messages are String
		// kafkaConfig.scheme = new SchemeAsMultiScheme((Scheme)new
		// StringScheme());
		// We want to consume all the first messages in the topic everytime
		// we run the topology to help in debugging. In production, this
		// property should be false
		// kafkaConfig.forceFromStart = true;
		BrokerHosts hosts = new ZkHosts(ZkUtil.CONNECT_ADDR);
		SpoutConfig spoutConfig = new SpoutConfig(hosts, KafkaUtil.TOPIC_WORDS,
				"/" + KafkaUtil.TOPIC_WORDS, UUID.randomUUID().toString());
		spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		// Now we create the topology
		TopologyBuilder builder = new TopologyBuilder();
		// set the kafka spout class
		builder.setSpout("KafkaSpout", new KafkaSpout(spoutConfig), 1);
		// configure the bolts
		builder.setBolt("SentenceBolt", new SentenceBolt(), 1)
				.globalGrouping("KafkaSpout");
		builder.setBolt("PrinterBolt", new PrinterBolt(), 1)
				.globalGrouping("SentenceBolt");
		// create an instance of LocalCluster class for executing topology in
		// local mode.
		LocalCluster cluster = new LocalCluster();
		Config conf = new Config();
		// Submit topology for execution
		cluster.submitTopology("KafkaToplogy", conf, builder.createTopology());
		try {
			// Wait for some time before exiting
			System.out.println("Waiting to consume from kafka");
			Thread.sleep(10000);
		} catch (Exception exception) {
			System.out.println("Thread interrupted exception : " + exception);
		}
		// kill the KafkaTopology
		cluster.killTopology("KafkaToplogy");
		// shut down the storm test cluster
		cluster.shutdown();
	}
}
