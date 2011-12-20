package org.metams.ServletPot.plugins;

//~--- JDK imports ------------------------------------------------------------

import org.metams.ServletPot.ConfigHandler;
import org.metams.ServletPot.tools.Twitter;
import org.metams.ServletPot.tools.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * User: flake
 * Date: May 1, 2010
 * Time: 11:46:58 PM
 */
public class Logger
{
	private boolean m_verbose = false;
	private ConfigHandler m_cf = null;
	private Utils m_util = null;
	private String m_name = null;

	private org.metams.ServletPot.tools.Twitter m_twitter = null;


	/**
	 * constructor for the buffer class
	 * @param name
	 * @param verbose
	 * @param cf
	 * @param u
	 */
	public Logger(String name, boolean verbose, ConfigHandler cf, Utils u)
	{

		m_verbose = verbose;
		m_name = name;

		// rename file
		String destName = name + "_" + new Date().toString();
		File x = new File(name);
		if (x.length() > 1024 * 1024)
		{
			x.renameTo(new File(destName));
		}


		try
		{
			m_util = u;
			m_cf = cf;
			m_twitter = new Twitter(m_cf.getAccessToken(), m_cf.getAccessTokenSecret(), m_cf.getConsumerKey(), m_cf.getConsumerKeySecret(), this, m_cf.getUseTwitter());
		} catch (Exception e)
		{
			m_util.print("ServletPot: Error in creating logile...");
		}
	}


	/*
			method for logging to file
			@in:    string to be logged
		 */
	public void log(String in, int counter)
	{
		log(in, null, counter);
	}


	/**
	 * log method
	 *
	 * @param in
	 * @param shortText
	 * @param counter
	 * @throws IOException
	 */
	public void log(String in, String shortText, int counter)
	{

		if (shortText != null) m_twitter.setStatus(shortText);


		try
		{
			File logFile = new File(m_name);
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(logFile, true));

			if (in != null)
			{
				String out = "[" + Integer.toString(counter) + "] " + in;
				bufWriter.write(out);
				bufWriter.newLine();
				bufWriter.flush();

				// if the user wants a full output, do it
				if (m_verbose)
				{
					m_util.print("[" + Integer.toString(counter) + "] " + in);
				}
			}

			bufWriter.flush();
			bufWriter.close();

		} catch (IOException e)
		{
			m_util.print("Servletpot: Error in writing to logfile...");
		}
	}

}



//~ Formatted by Jindent --- http://www.jindent.com
