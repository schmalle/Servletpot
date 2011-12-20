package org.metams.ServletPot.test;


import org.metams.ServletPot.ConfigHandler;
import org.metams.ServletPot.tools.Twitter;

import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: flake
 * Date: 4/27/11
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigHandlerTest
{
	public static void main(String[] args)
	{
	    String x = "/Users/flake/IdeaProjects/ServletPot/web/WEB-INF/config.txt";
		ConfigHandler y = new ConfigHandler(x);
		y.read();


		TimeZone.setDefault(TimeZone.getTimeZone(y.getTimeZone()));


	  /*      SimpleDateFormat x = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	        String yy = x.format(new Date());

			try
			{
			Sender m_send = new Sender(y.getCentralDBURL());
	        String message = m_send.getMessage(y.getCentralDBPassword(), y.getCentralDBUser(), "127.0.0.1", "/NICHTS", yy, "TESTATTACK");
	        m_send.sendReport(message);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

        */

		Twitter z = new Twitter(y.getAccessToken(), y.getAccessTokenSecret(), y.getConsumerKey(), y.getConsumerKeySecret(),null, false);

		z.setStatus("test");




	}

}
