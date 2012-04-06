package org.metams.ServletPot.test;

import org.metams.ServletPot.plugins.Sender;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: flake
 * Date: 4/2/12
 * Time: 8:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class SenderTest
{
	/*


	Analyzer ident : glastopf-uni-bonn
	Token: 32b3eaa5f921d5f6a9ef59cd9988b4a8

	 */


	static public void main(String[] args)
	{
		SenderTest x= new SenderTest();
		x.send();
	}


	public void send()
	{

		SimpleDateFormat x1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		                String y = x1.format(new Date());

		try
		{
			Sender x = new Sender("https://www.t-sec-radar.de/ews-0.1/alert/postSimpleMessage", null);
			String message = x.getMessage("pw",
					"kippo-uni-bonn",
					"127.0.0.1",
					"--TEST--",
					y, "--NO ATTACK--", "kippo-uni-bonn");

			System.out.println(message);
			x.sendReport(message);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


}
