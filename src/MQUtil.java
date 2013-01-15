

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

public class MQUtil {
	Channel channel;
	Connection conn;
	ConnectionFactory factory;

	public MQUtil() throws IOException {

		String host = "192.168.174.104";
		String username = "guest";
		String password = "guest";
		String virtualHost = "/";
		int port = 5672;

		this.factory = new ConnectionFactory();
		this.factory.setUsername(username);
		this.factory.setPassword(password);
		this.factory.setVirtualHost(virtualHost);
		this.factory.setHost(host);
		this.factory.setPort(port);

		this.conn = this.factory.newConnection();
		this.channel = this.conn.createChannel();
	}

	public MQUtil(String ip) throws IOException {

		String host = ip;
		String username = "guest";
		String password = "guest";
		String virtualHost = "/";
		int port = 5672;

		this.factory = new ConnectionFactory();
		this.factory.setUsername(username);
		this.factory.setPassword(password);
		this.factory.setVirtualHost(virtualHost);
		this.factory.setHost(host);
		this.factory.setPort(port);

		this.conn = this.factory.newConnection();
		this.channel = this.conn.createChannel();
	}

	public void reconnection() throws IOException {
		if (!this.conn.isOpen()) {
			this.conn = this.factory.newConnection();
		}
		if (!this.channel.isOpen()) {
			this.channel = this.conn.createChannel();
		}
	}

	public void publish(String exchangeName, String queueName, String key,
			String msg) throws IOException {
		while (true)
			try {
				this.channel.exchangeDeclare(exchangeName, "direct", true);
				this.channel.queueDeclare(queueName, true, false, false, null);
				this.channel.queueBind(queueName, exchangeName, key);
				this.channel
						.basicPublish(exchangeName, key,
								MessageProperties.PERSISTENT_TEXT_PLAIN,
								msg.getBytes());

				return;
			} catch (Exception localException) {
			}
	}

	public void fanout(String exchangeName, String msg) throws IOException {
		this.channel.exchangeDeclare(exchangeName, "fanout");
		this.channel.basicPublish(exchangeName, "",
				MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
	}

	public QueueingConsumer buildComsumer(String queueName) throws IOException {
		boolean autoAck = false;
		QueueingConsumer consumer = new QueueingConsumer(this.channel);
		this.channel.basicConsume(queueName, autoAck, consumer);
		return consumer;
	}

	public QueueingConsumer buildFanoutComsumer(String exchangeName)
			throws IOException {
		this.channel.exchangeDeclare(exchangeName, "fanout");
		String queueName = this.channel.queueDeclare().getQueue();
		this.channel.queueBind(queueName, exchangeName, "");

		QueueingConsumer consumer = new QueueingConsumer(this.channel);
		this.channel.basicConsume(queueName, true, consumer);
		return consumer;
	}

	protected void finalize() {
		try {
			this.channel.close();
			this.conn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		MQUtil mq = new MQUtil();
		for (int i = 0; i < 50; i++) {
			String msg = Integer.toString(i);
			mq.publish("ex1", "q1", "m1", msg);
		}
	}
}