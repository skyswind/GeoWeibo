

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

public class Producer {
	
	
	public static void main(String[] args) {
		buildandpublish();
	}
	
	
	  public static String getPlainText(String str) {
	   try {
	    Parser parser = new Parser();
	    parser.setInputHTML(str);
	    StringBean sb = new StringBean();
	    // 设置不需要得到页面所包含的链接信息
	    sb.setLinks(false);
	    // 设置将不间断空格由正规空格所替代
	    sb.setReplaceNonBreakingSpaces(true);
	    // 设置将一序列空格由一个单一空格所代替
	    sb.setCollapse(true);
	    parser.visitAllNodesWith(sb);
	    str = sb.getStrings();
	   } catch (ParserException e) {
		   System.out.println(e.toString());
	   }
	   return str;
	  }

	
	private static void buildandpublish(){
        HttpClient httpClient = new HttpClient();
  
             
            MQUtil mq;
			
	        try {
	        	mq = new MQUtil();
			while(true) {
				
			      GetMethod getMethod = new GetMethod("" +
			        		"http://sslk.bjjtgl.gov.cn/roadpublish/Map/vmsimg/vmsimage/secondrealinfo.htm");

			            int statusCode = httpClient.executeMethod(getMethod);
			            if (statusCode != HttpStatus.SC_OK) {
			                System.err.println("Method failed: "
			                        + getMethod.getStatusLine());
			            }
			            
				if(!mq.conn.isOpen())
				{
					System.out.println("reconn");
					mq = new MQUtil();
				}

	            byte[] responseBody = getMethod.getResponseBody();
	            String html = getPlainText(new String(responseBody));
	            System.out.println(html);
	            
				mq.fanout("bjssjt", html);

				Thread.sleep(60000);
			}
		}catch(Exception ex)
		{
			buildandpublish();
		}
	}
}