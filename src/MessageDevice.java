

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class MessageDevice {
	private MQUtil mq;
	public QueueingConsumer consumer;
	public String listenId;

	public MessageDevice() throws IOException {
		mq = new MQUtil();
	}
	
	public MessageDevice(String listenId) throws IOException {
		mq = new MQUtil();
		this.listenId = listenId;
		this.buildAutoDeleteQueue(listenId);
		//this.buildQueue(listenId);
		consumer = this.buildConsumer(listenId);
	}
	
	public MessageDevice(String listenId,String exchangeName) throws IOException {
		mq = new MQUtil();
		this.listenId = listenId;
		this.buildAutoDeleteQueue(listenId,exchangeName);
		//this.buildQueue(listenId);
		consumer = this.buildConsumer(listenId);
	}

	public void reConnect() throws IOException
	{
		if(!mq.conn.isOpen())
		{
			mq = new MQUtil();
		}
	}
	
	public void msgDroid(String droid, String msg) throws IOException {
		mq.publish("MapDroid", droid, droid, msg);
	}

	public void fanoutDroid(String exchangeName, String msg) throws IOException {
		mq.fanout(exchangeName, msg);
	}

	public void msgLogger(String msg) throws IOException {
		msgDroid("logger", msg);
	}

	public void msgMapCutter(String msg) throws IOException {
		fanoutDroid("mapCutter", msg);
	}

	public void msgPostman(String msg) throws IOException {
		msgDroid("postMan", msg);
	}

	public void msgGPSWorker(String msg) throws IOException {
		msgDroid("gpsWorker", msg);
	}

	public void msgGPSUnlocker(String msg) throws IOException {
		msgDroid("gpsUnlockerIn", msg);
	}

	public void msgGPSAdjustIn(String msg) throws IOException {
		msgDroid("gpsAdjustIn", msg);
	}

	public void msgDataCarrier(String msg) throws IOException {
		msgDroid("downloader", msg);
	}

	public void msgUnziper(String msg) throws IOException {
		msgDroid("unZiper", msg);
	}

	public void msgETLer(String msg) throws IOException {
		msgDroid("ETLer", msg);
	}

	public QueueingConsumer buildConsumer(String droid) throws IOException {
		return mq.buildComsumer(droid);
	}

	public QueueingConsumer buildFanoutConsumer(String droid)
			throws IOException {
		return mq.buildFanoutComsumer(droid);
	}

	public void buildQueue(String queueName) throws IOException {
		mq.channel.queueDeclare(queueName, true, false, false, null);
	}
	
	public void buildQueue(String queueName,String exchangeName) throws IOException {
		mq.channel.queueDeclare(queueName, true, false, false, null);
		mq.channel.queueBind(queueName, exchangeName, queueName);
	}

	public void buildAutoDeleteQueue(String queueName) throws IOException {
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-expires", 30000);
		mq.channel.queueDeclare(queueName, false, false, true,args);
	}
	
	public void buildAutoDeleteQueue(String queueName,String exchangeName) throws IOException {
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-expires", 30000);
		mq.channel.queueDeclare(queueName, false, false, true,args);
		mq.channel.queueBind(queueName, exchangeName, queueName);
	}

	public Channel getChannel() {
		return mq.channel;
	}

	public void closeChannel() throws IOException {
		mq.channel.close();
		mq.conn.close();
	}

	public void closeChannel(String id) throws IOException {
		mq.channel.queueDelete(id);
		mq.channel.close();
		mq.conn.close();
	}

	public static void main(String[] args) {
		MessageDevice msg;
		try {
			msg = new MessageDevice();
			msg.buildAutoDeleteQueue("test2");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void msgPreparer(String msg) throws IOException {
		// TODO Auto-generated method stub
		msgDroid("preparer", msg);
	}
}
