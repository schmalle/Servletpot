package org.metams.ServletPot;

import org.metams.ServletPot.Database.DBAccess;
import org.metams.ServletPot.Database.Hibernate.MySqlHibernate;
import org.metams.ServletPot.Database.MySql;
import org.metams.ServletPot.Database.Redis;
import org.metams.ServletPot.plugins.*;
import org.metams.ServletPot.plugins.http.PHPHandler;
import org.metams.ServletPot.plugins.http.PostAnalyzer;
import org.metams.ServletPot.plugins.http.Robots;
import org.metams.ServletPot.tools.Utils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;



/**

 Core class, which is the base for all upcoming

 */

import org.metams.ServletPot.Database.DBAccess;
import org.metams.ServletPot.Database.Hibernate.MySqlHibernate;
import org.metams.ServletPot.Database.MySql;
import org.metams.ServletPot.Database.Redis;
import org.metams.ServletPot.plugins.HandleAttackFiles;
import org.metams.ServletPot.plugins.Logger;
import org.metams.ServletPot.plugins.SQLInjection;
import org.metams.ServletPot.plugins.VulnEmulator;
import org.metams.ServletPot.plugins.http.PHPHandler;
import org.metams.ServletPot.plugins.http.PostAnalyzer;
import org.metams.ServletPot.plugins.http.Robots;
import org.metams.ServletPot.tools.Utils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class EntryNetty
{
	private String m_configFile = null;
	private String m_defaultPage = null;
	private boolean m_page = false;
	private ConfigHandler m_configHandler = null;
	private Utils m_utils = null;
	private VulnEmulatorNetty m_emu = null;
	private Logger m_l = null;
	private Robots m_robot = new Robots();
	private String[] m_attackStrings = null;
	private PHPHandler m_php = null;
	private SQLInjection m_sql = null;
	private DBAccess m_db = null;
	private PostAnalyzer m_post = null;
	private String m_noanswer = null;


	/*
			destroy all data fields, when the servlet is ended
		 */
	public void destroy()
	{
		if (m_db != null) m_db.destroy();
	}

	/**
	 * init function of the httpservlet
	 *
	 * @param config
	 * @throws javax.servlet.ServletException
	 */
	public void init(String fileName, String indexHtmlName)
	{
		if (!m_page)
		{
			m_configFile = fileName;
			m_configHandler = new ConfigHandler(m_configFile);

			// read Config lines
			m_configHandler.read();
			m_utils = new Utils(m_configHandler);
			m_l = new Logger(m_configHandler.getLogPath(), true, m_configHandler, m_utils);

			m_defaultPage = indexHtmlName;
			m_page = false;

			// Todo Fix
			m_emu = new VulnEmulatorNetty(m_configHandler, m_utils, m_l, 0);
			m_php = new PHPHandler(m_utils, m_l, new HandleAttackFiles(m_utils, m_configHandler, m_l));
			//m_sql = new SQLInjection("WEB-INF/sql.txt");

			if (m_configHandler.getDB().equalsIgnoreCase("mysql"))
				m_db = new MySql(m_l);
			else if (m_configHandler.getDB().equalsIgnoreCase("redis"))
				m_db = new Redis(m_l);
			else if (m_configHandler.getDB().equalsIgnoreCase("hibernate"))
				m_db = new MySqlHibernate(m_l);
			else
				m_db = null;


			m_db.open(m_configHandler.getUserName(), m_configHandler.getUserPW(), m_configHandler.getDBPath());

			m_post = new PostAnalyzer(m_php, m_l);

			m_noanswer = m_configHandler.getNoAnswer();

		}
	}	// init


	/*
		 *   shows debug information for the user
		 */
	private void showDebugInfo(PrintWriter out, HttpServletRequest request)
	{
		out.println("Info: Path to config data " + m_configFile + "<br>");
		out.println("Info: Working with URI    " + request.getRequestURI() + "<br>");
	}	// showDebugInfo


	/**
	 * returns the URI to a given request
	 * @param request
	 * @return
	 */
	private String getURI(HttpServletRequest request)
	{
		// construct full string incl parameters
		String URI = request.getRequestURI();
		String queryString = request.getQueryString();

		if ((URI != null) && (queryString != null))
		{
			URI = URI.concat("?").concat(queryString);
		}

		return URI;
	}   // getURI





	public StringBuilder doGet(Hashtable get, Hashtable post, String ip, String URI, StringBuilder content, String method, String host)
	{

		String attackType = null;

		int reqNr = m_db.getCounter();
		m_db.increaseCounter();


		if (checkForDefaultFiles(URI, content))
			return content;



		// special handling of POST request
		if (method != null && method.contains("post"))
		{
			m_emu.handlePost();
			return returnDefaultPagePost(content);
		}
		else if (method != null && method.contains("get"))
		{
			// handleRequestParams(request);
			attackType = m_emu.handleRequest(URI, get, post, ip, reqNr, host, method);
		}


//		URI = URI.toLowerCase();

		// reaching this points means that an RFI attack was ...
//		m_emu.checkForNewAttack(get, post, URI, reqNr, host,  method, ip);




		return returnDefaultPage(content, attackType, reqNr, URI, get, post, ip);
	}	// doGet


	/*
			check for the existance of default files such as robots.txt
			@in:    URI -   file to be requested
					out -   PrintWriter

			@out:   boolean

		 */
	private boolean checkForDefaultFiles(String URI, StringBuilder content)
	{
		if (URI.toLowerCase().contains("robots.txt"))
			return m_robot.makeRobots(content);

		if (m_noanswer != null && URI.contains(m_noanswer))
			return m_robot.makeRobots(content);

		if (URI.toLowerCase().endsWith("favicon.ico"))
			return true;

		if (URI.toLowerCase().endsWith(".png"))
			return true;

		if (URI.toLowerCase().endsWith(".gif"))
			return true;

		if (URI.toLowerCase().endsWith(".jpg"))
			return true;


		return false;

	}   // checkForDefaultFiles


	/**
	 *
	 * @param content
	 * @return
	 */
	private StringBuilder returnDefaultPagePost(StringBuilder content)
	{

		content.append("POST");
		return content;

		// To change body of created methods use File | Settings | File Templates.
	}	// returnDefaultPagePoist


	/*
		 * prints the default page
		 */
	public StringBuilder returnDefaultPage(StringBuilder content, String attackType, int reqNr, String URI, Hashtable get, Hashtable post, String ip)
	{

		String outData = "";
		boolean inserted = false;
		BufferedReader inReaderBuf = null;
		FileReader inReader = null;

		try
		{
			File in = new File(m_defaultPage);
			inReader = new FileReader(in);
			inReaderBuf = new BufferedReader(inReader);
			String line;

			while ((line = inReaderBuf.readLine()) != null)
			{
				if (!inserted && line.contains("<!-- FLK! -->"))
				{

					content.append(outData);

					// NEU
					insertLearnedCode(content);
					outData = "";

					// ALT
					//outData = outData.concat(insertLearnedCode(attackType, request));
					line = "";
					inserted = true;
				}

				outData = outData.concat(line);
			}

			inReaderBuf.close();
			inReader.close();

		} catch (IOException e)
		{
			m_l.log("Info: Error in reading default page (" + m_defaultPage + ")....", reqNr);
		}

		try
		{
			inReaderBuf.close();
			inReader.close();

		}
		catch (Exception e )
		{

		}
		content.append(outData);

		return content;

		// To change body of created methods use File | Settings | File Templates.
	}	// returnDefaultPage


	/**
	 * handles HTTP Post requests
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{

		String URI = getURI(request);

		PrintWriter out = response.getWriter();

		response.setContentType("text/html");

		String host = request.getHeader("Host");

		int reqNr = m_db.getCounter();
		m_db.increaseCounter();

		String attackType = m_post.analyze(request, request.getRequestURI(), reqNr);

		// dump parameters only, if attacktype was not found
		if (attackType == null)
		{
			m_l.log("Content type for POST request is: " + request.getContentType(), reqNr);
			m_l.log("Req for POST request is: " + URI, reqNr);

			Enumeration paramNames = request.getParameterNames();

			if (paramNames != null)
			{
				while (paramNames.hasMoreElements())
				{
					{
						String paramName = (String) paramNames.nextElement();
						m_l.log("POST Parameter: " + paramName, reqNr);

						String[] values = request.getParameterValues(paramName);
						if (values.length >= 1)
						{
							for (int runner = 0; runner <= values.length - 1; runner++)
								m_l.log("   Values: " + values[runner], reqNr);


						}


					}

				}
			}

		} else if (attackType != null)
		{
			m_emu.storeAttackForPost(request, attackType, URI, reqNr, host);
			String data = m_post.getEmulatedCode(reqNr);

			out.print(data);
			out.print("<br>");
			out.flush();

			m_l.log("Returned emulated code: " + data, reqNr);

		}

	}   // doPost


	/**
	 * inserts learned code in a big string
	 *
	 * @param attackType
	 * @param request
	 * @return
	 */
	private String insertLearnedCode(StringBuilder outWriter)
	{

		String out = "";
		List strings = null;

		DBAccess db = m_emu.getDatabase();
		strings = db.getURI();


		for (int runner = 0; strings != null && runner <= strings.size()  - 1; runner++)
		{
			String temp = (String)strings.get(runner);

			// NEU
			outWriter.append(temp + "<br>");

			// ALT
	//		if (temp != null)
	//			out = out.concat(temp.concat("<br>"));
		}

		// deallocate strings
		strings = null;
		return out;
	}   // insertLearnedCode


}


