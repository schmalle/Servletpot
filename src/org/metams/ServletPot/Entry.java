package org.metams.ServletPot;


/**

 Core class, which is the base for all upcoming

 */

import org.metams.ServletPot.Database.DBAccess;
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

public class Entry extends HttpServlet
{
    private String          m_configFile  = null;
    private String          m_defaultPage = null;
    private boolean         m_page = false;
    private ConfigHandler   m_configHandler = null;
    private Utils           m_utils         = null;
    private VulnEmulator    m_emu = null;
    private Logger          m_l = null;
    private Robots          m_robot = new Robots();
    private String[]        m_attackStrings = null;
    private PHPHandler      m_php           = null;
    private SQLInjection    m_sql           = null;
    private DBAccess        m_db            = null;
    private PostAnalyzer    m_post          = null;
	private String          m_noanswer      = null;


	/*
		destroy all data fields, when the servlet is ended
	 */
	public void destroy()
	{
		if (m_db != null) m_db.destroy();
	}

    /**
     * init function of the httpservlet
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException
    {
        if (!m_page)
        {
            m_configFile  = config.getServletContext().getRealPath(File.separator) + "WEB-INF/config.txt";
            m_configHandler = new ConfigHandler(m_configFile);

            // read Config lines
            m_configHandler.read();
            m_utils = new Utils(m_configHandler);
            m_l = new Logger(m_configHandler.getLogPath(), true, m_configHandler, m_utils);

            m_defaultPage = config.getServletContext().getRealPath(File.separator) + "WEB-INF/index.html";
            m_page        = false;

            // Todo Fix
            m_emu         = new VulnEmulator(m_configHandler, m_utils, m_l, 0);
            m_php         = new PHPHandler(m_utils, m_l, new HandleAttackFiles(m_utils, m_configHandler, m_l));
            m_sql         = new SQLInjection(m_l, m_configHandler, m_emu.getDB(), config.getServletContext().getRealPath(File.separator) + "WEB-INF/sql.txt");

            if (m_configHandler.getDB().equalsIgnoreCase("mysql"))
                m_db = new MySql(m_l);
            else if (m_configHandler.getDB().equalsIgnoreCase("redis"))
                m_db = new Redis(m_l);
            else
                m_db = null;



            m_db.open(m_configHandler.getUserName(), m_configHandler.getUserPW(), m_configHandler.getDBPath());

            m_post          = new PostAnalyzer(m_php, m_l);

	        m_noanswer      = m_configHandler.getNoAnswer();

        }
    }    // init


    /*
     *   shows debug information for the user
     */
    private void showDebugInfo(PrintWriter out, HttpServletRequest request)
    {
        out.println("Info: Path to config data " + m_configFile + "<br>");
        out.println("Info: Working with URI    " + request.getRequestURI() + "<br>");
    }    // showDebugInfo


    /*
        returns the URI from a given HttpServletRequest
        @in:    request
        @out:   URI
     */
    private String getURI(HttpServletRequest request)
    {
        // construct full string incl parameters
        String URI         = request.getRequestURI();
        String queryString = request.getQueryString();

        if ((URI != null) && (queryString != null))
        {
            URI = URI.concat("?").concat(queryString);
        }

        return URI;
    }   // getURI



    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
        doHandler(request, response, 0);
     }

    public void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
       {
          doHandler(request, response, 0);
       }
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
       {
          doHandler(request, response, 0);
       }


    /*

     */
    public void doHandler(HttpServletRequest request, HttpServletResponse response, int reqNr) throws ServletException, IOException
    {

        // construct full string incl parameters
        String URI         = getURI(request);
        String method = request.getMethod().toLowerCase();
        String ip = m_utils.getIP(request);

        m_l.log("Found non GET/POST call method: " + method + " from IP " + ip + " at time " + new Date().toString() + " and request " + URI, reqNr);


    }   // doHandler

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        String attackType = null;

        int reqNr = m_db.getCounter();
        m_db.increaseCounter();

        response.setContentType("text/html");
        //m_emu.setOutWriter(out);

        // construct full string incl parameters
        String URI         = getURI(request);

        if (checkForDefaultFiles(URI, out))
            return;


        String method = request.getMethod().toLowerCase();

        // special handling of POST request
        if (method != null && method.contains("post"))
        {
            m_emu.handlePost(request);
            returnDefaultPagePost(out);
            return;
        }
        else if (method != null && method.contains("get"))
        {
           // handleRequestParams(request);
            attackType = m_emu.handleRequest(URI, request, reqNr);
        }


        returnDefaultPage(out, attackType, reqNr, request);
    }    // doGet


    /*
        check for the existance of default files such as robots.txt
        @in:    URI -   file to be requested
                out -   PrintWriter

        @out:   boolean
    
     */
    private boolean checkForDefaultFiles(String URI, PrintWriter out)
    {
        if (URI.toLowerCase().contains("robots.txt"))
            return m_robot.makeRobots(out);

        if (m_noanswer != null && URI.contains(m_noanswer))
	        return m_robot.makeRobots(out);

	    return false;

    }   // checkForDefaultFiles




    /*
      * printts the default page
      */
     private void returnDefaultPagePost(PrintWriter out)
     {

         out.println("<html><body>");
         out.println("Error in handling request....<br>");
         out.println("</body></html>");

         // To change body of created methods use File | Settings | File Templates.
     }    // returnDefaultPagePoist




    /*
     * prints the default page
     */
    private void returnDefaultPage(PrintWriter out, String attackType, int reqNr, HttpServletRequest request) throws IOException
    {

        String outData = "";
        boolean inserted = false;
        BufferedReader inReaderBuf = null;
        FileReader     inReader = null;

        try
        {
            File in          = new File(m_defaultPage);
            inReader    = new FileReader(in);
            inReaderBuf = new BufferedReader(inReader);
            String         line;

            while ((line = inReaderBuf.readLine()) != null)
            {
                if (!inserted && line.contains("<!-- FLK! -->"))
                {
                    outData = outData.concat(insertLearnedCode(attackType, request));
                    line = "";
                    inserted = true;
                }

                outData = outData.concat(line);
            }

            inReaderBuf.close();
            inReader.close();

        }
        catch (IOException e)
        {
             m_l.log("Info: Error in reading default page (" + m_defaultPage + ")....", reqNr);
            inReaderBuf.close();
            inReader.close();
        }

        out.println(outData);

        // To change body of created methods use File | Settings | File Templates.
    }    // returnDefaultPage


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

        }
        else if (attackType != null)
        {
            m_emu.storeAttackForPost(request, attackType, URI, reqNr);
            String data = m_post.getEmulatedCode(reqNr);

            out.print(data);
            out.print("<br>");
            out.flush();

	        m_l.log("Returned emulated code: " + data, reqNr);

        }

    }   // doPost


    /*
        inserts the newly learned values
        @out:   String
     */
    private String insertLearnedCode(String attackType, HttpServletRequest request)
    {


        java.util.Hashtable     x2 = null;
        ServletContext          ssocontext = null;

        try
        {
            String x = request.getSession().getServletContext().getInitParameter("flake");
            ssocontext = request.getSession().getServletContext();
            x2 =     (java.util.Hashtable) ssocontext.getAttribute("flake");

        }
        catch (Exception e)
        {
            x2 = null;    
        }

        if (x2 == null)
        {

            DBAccess db = m_emu.getDatabase();
            if (db != null)
            {
                m_attackStrings = db.getURI();
            }

            x2 = new Hashtable();


            if (m_attackStrings != null)
			{
				for (int runner = 0; runner <= m_attackStrings.length -1; runner++)
				{
					if (m_attackStrings[runner] != null)
						x2.put(runner, m_attackStrings[runner]);
				}
			}

            if (ssocontext != null)
                ssocontext.setAttribute("flake", x2);


        }

        String out = "";

        if (x2 != null && x2.size() != 0)
        {

            // Todo Add randomizer
            // Todo Add varibale style sheet

            for (int runner = 0; runner <= x2.size() - 1; runner++)
            {
                out = out.concat((String)x2.get(runner)).concat("<br>");
            }

        }   // attackStrings != null


        return out;
    }   // insertLearnedCode



}


