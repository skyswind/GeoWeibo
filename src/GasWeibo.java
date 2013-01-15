import java.io.IOException;
import java.util.Scanner;

import com.rabbitmq.client.QueueingConsumer;

import weibo4j.Oauth;
import weibo4j.Timeline;
import weibo4j.examples.oauth2.Log;
import weibo4j.http.AccessToken;
import weibo4j.model.Status;
import weibo4j.model.WeiboException;

public class GasWeibo {

	private static int count = 600;

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

		Timeline tm = new Timeline();
		tm.client.setToken(accessToken.getAccessToken());
		consumeandprint(tm, 600);
	}

	private static void consumeandprint(Timeline tm, int count_span) {

		MQUtil mq;
		try {
			mq = new MQUtil();

			// QueueingConsumer qc = mq.buildComsumer("ha.q2");

			QueueingConsumer qc = mq.buildFanoutComsumer("smGasMonitor");
			while (true) {
				QueueingConsumer.Delivery delivery = null;
				try {
					delivery = qc.nextDelivery(1000);
					String msg = new String(delivery.getBody(), "UTF-8");

					if (count == count_span) {
						String[] statuss = msg.split(",");
						String Co = statuss[1].substring(5);
						String H = statuss[2].substring(3);
						String T = statuss[3].substring(3);
						Status status = tm.UpdateStatus("小Q家的实时CO浓度为：" + Co
								+ ";湿度为:" + H + ";温度为:" + T);
						Log.logInfo("小Q家的实时CO浓度为：" + Co + ";湿度为:" + H
								+ ";温度为:" + T);
						count = 0;
					} else {
						count++;
					}

					// updateStatus("超图研究所的实时Co2浓度为：", accessToken);

					mq.channel.basicAck(
							delivery.getEnvelope().getDeliveryTag(), false);

				} catch (WeiboException e1) {
					// TODO Auto-generated catch block

					if(e1.getErrorCode()==10024)
					{
						Thread.sleep(120000);
						consumeandprint(tm, 600);
					}
				}catch (Exception ie) {
					consumeandprint(tm, 600);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			consumeandprint(tm, 600);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
