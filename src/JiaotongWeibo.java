import java.io.IOException;
import java.util.Scanner;

import com.rabbitmq.client.QueueingConsumer;

import weibo4j.Oauth;
import weibo4j.Timeline;
import weibo4j.examples.oauth2.Log;
import weibo4j.http.AccessToken;
import weibo4j.model.Status;
import weibo4j.model.WeiboException;

public class JiaotongWeibo {

	private static int count = 0;
	private static String old_msg = "";

	public static void main(String[] args) {

		System.out.println("请打开你的浏览器，访问以下页面，登录你的微博账号并授权：");
		System.out
				.println("https://api.weibo.com/oauth2/authorize?client_id=4271070672&response_type=code&redirect_uri=https://api.weibo.com/oauth2/default.html&forcelogin=true");
		// 第二步：获取AccessToken
		System.out.println("请将授权成功后的页面地址栏中的参数code：");
		String code = new Scanner(System.in).next();

		AccessToken accessToken = null;
		try {
			accessToken = new Oauth().getAccessTokenByCode(code);
		} catch (WeiboException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// /String statuses = "test";
		Timeline tm = new Timeline();
		tm.client.setToken(accessToken.getAccessToken());
		consumeandprint(tm, 0);
	}

	private static void consumeandprint(Timeline tm, int count_span) {

		MQUtil mq;
		try {
			mq = new MQUtil();

			QueueingConsumer qc = mq.buildFanoutComsumer("bjssjt");
			while (true) {

				QueueingConsumer.Delivery delivery = null;
				try {
					Thread.sleep(10000);

					delivery = qc.nextDelivery(1000);
					String msg = new String(delivery.getBody(), "UTF-8");
					
					String[] jtxx = msg.split("\n");
					
					if(old_msg.equalsIgnoreCase(jtxx[1]))
					{
						continue;
					}
					
					old_msg = jtxx[1];
					Status status = tm.UpdateStatus("小Q悄悄的告訴你：" + jtxx[1]);
					Log.logInfo("交通状况为：" + msg);
			
					mq.channel.basicAck(
							delivery.getEnvelope().getDeliveryTag(), false);

				} catch (WeiboException e1) {
					// TODO Auto-generated catch block

					if (e1.getErrorCode() == 10024) {
						try {
							Thread.sleep(120000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						consumeandprint(tm, 120);
					}
				} catch (Exception ie) {
					consumeandprint(tm, 0);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			consumeandprint(tm, 0);
		}
	}
}
