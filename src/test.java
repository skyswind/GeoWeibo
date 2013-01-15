import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import javax.net.ssl.X509TrustManager;

import com.rabbitmq.client.QueueingConsumer;

/**
 * @author 刘显安 不使用任何SDK实现新浪微博Oauth授权并实现发微薄小Demo 日期：2012年11月11日
 */
public class test {
	static String clientId = "4271070672";// 你的应用ID
	static String clientSecret = "7eb68e149db0077b3a0c17eb208633fe";// 你的应用密码
	static String redirectUri = "https://api.weibo.com/oauth2/default.html";// 你在应用管理中心设置的回调页面
	static String authcode;
	
	public static void main(String[] args) throws Exception {
		testHttps();// 测试
		// 第一步：访问授权页面获取授权
		System.out.println("请打开你的浏览器，访问以下页面，登录你的微博账号并授权：");
		System.out.println("https://api.weibo.com/oauth2/authorize?client_id="
				+ clientId + "&response_type=code&redirect_uri=" + redirectUri
				+ "&forcelogin=true");
		// 第二步：获取AccessToken
		System.out.println("请将授权成功后的页面地址栏中的参数code：");
		authcode = new Scanner(System.in).next();
		//getAccessToken(authcode);
		// 第三步：发布一条微博
		System.out.println("请输入上面返回的值中accessToken的值：");
		String accessToken = new Scanner(System.in).next();
		consumeandprint( accessToken);
	}

	/**
	 * 测试能否正常访问HTTPS打头的网站，
	 */
	public static void testHttps() {
		try {
			trustAllHttpsCertificates();// 设置信任所有的http证书
			URL url = new URL("https://api.weibo.com/oauth2/default.html");
			URLConnection con = url.openConnection();
			con.getInputStream();
			System.out.println("恭喜，访问HTTPS打头的网站正常！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 以Post方式访问一个URL
	 * 
	 * @param url
	 *            要访问的URL
	 * @param parameters
	 *            URL后面“？”后面跟着的参数
	 */
	public static void postUrl(String url, String parameters) {
		try {
			trustAllHttpsCertificates();// 设置信任所有的http证书
			URLConnection conn = new URL(url).openConnection();
			conn.setDoOutput(true);// 这里是关键，表示我们要向链接里注入的参数
			OutputStreamWriter out = new OutputStreamWriter(
					conn.getOutputStream());// 获得连接输出流
			out.write(parameters);
			out.flush();
			out.close();
			// 到这里已经完成了，开始打印返回的HTML代码
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取AccessToken
	 * 
	 * @param code
	 *            在授权页面返回的Code
	 */
	public static void getAccessToken(String code) {
		String url = "https://api.weibo.com/oauth2/access_token";
		String parameters = "client_id=" + clientId + "&client_secret="
				+ clientSecret + "&grant_type=authorization_code"
				+ "&redirect_uri=" + redirectUri + "&code=" + code;
		postUrl(url, parameters);
	}

	/**
	 * 利用刚获取的AccessToken发布一条微博
	 * 
	 * @param text
	 *            要发布的微博内容
	 * @param accessToken
	 *            刚获取的AccessToken
	 */
	public static void updateStatus(String text, String accessToken) {
		String url = "https://api.weibo.com/2/statuses/update.json";
		String parameters = "status=" + text + "&access_token=" + accessToken;
		postUrl(url, parameters);
		System.out.println("发布微博成功！");
	}

	/**
	 * 设置信任所有的http证书（正常情况下访问https打头的网站会出现证书不信任相关错误，所以必须在访问前调用此方法）
	 * 
	 * @throws Exception
	 */
	private static void trustAllHttpsCertificates() throws Exception {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		trustAllCerts[0] = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}
		};
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
				.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}
	
	private static void consumeandprint(String accessToken)
	{
		MQUtil mq;
		try {
			mq = new MQUtil();
		
		
		//QueueingConsumer qc = mq.buildComsumer("ha.q2");
		
		QueueingConsumer qc = mq.buildFanoutComsumer("test");
		while (true) {
			QueueingConsumer.Delivery delivery = null;
			try {
				delivery = qc.nextDelivery(1000);
				String msg = new String(delivery.getBody(),"UTF-8");
				
				updateStatus("超图研究所的实时Co2浓度为：", accessToken);
			
				mq.channel.basicAck(
						delivery.getEnvelope().getDeliveryTag(), false);

			} 
			catch (Exception ie) {
				consumeandprint(accessToken);
			}
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			consumeandprint(accessToken);
		}
	}
	
}
