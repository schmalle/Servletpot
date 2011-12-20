package org.metams.ServletPot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * User: flake
 * Date: Jun 30, 2010
 * Time: 9:19:30 PM
 */
public class ConfigHandler
{

    private File m_file = null;
    private BufferedReader m_reader = null;
    private String  m_dbuser = null;
    private String  m_dbpw = null;
    private String  m_filePath = null;
    private String  m_dbPath = null;
    private String  m_logPath = null;
	private String  m_timeZone = null;
    private String  m_proxyUrl = null;
    private String  m_popServer = null;
	private String  m_accessToken = null;
	private String  m_accessTokenSecret = null;
	private String  m_useTwitter = null;
	private String  m_noanswer = null;
    private String  m_db = null;

    public String getDB() {return m_db; }
	public String getNoAnswer() {return m_noanswer; }
	public String getAccessToken() {return m_accessToken; }
	public String getAccessTokenSecret() {return m_accessTokenSecret; }


	/**
	 * returns the preconfigured time line
	 * @return
	 */
	public String getTimeZone()
	{
		if (m_timeZone == null)
			return "Europe/Berlin";
		else
			return m_timeZone;
	}




	private String  m_consumerkey = null;
	private String  m_consumerkeysecret = null;

	public String getConsumerKey() {return m_consumerkey; }
	public String getConsumerKeySecret() {return m_consumerkeysecret; }


	/**
	 *
	 * @return
	 */
	public boolean getUseTwitter()
	{
		if (m_useTwitter == null)
			return false;

		if (m_useTwitter.toLowerCase().equals("yes"))
			return true;


		return false;
	}


    private boolean m_useConsoleOutput = false;
    private boolean m_userevproxy = false;
    public boolean getUseRevProxy() {return m_userevproxy; }


    public String getPopServer() {return m_popServer;}
    public String getSmtpServer() {return m_smtpServer;}
    public String getUserName() {return m_dbuser; }
    public String getUserPW() {return m_dbpw; }
    public String getFilePath() {return m_filePath; }
    public String getDBPath() {return m_dbPath; }
    public String getLogPath() {return m_logPath; }
    public String m_proxyPort = null;
    public String m_proxyUser = null;
    public String m_proxyPass = null;
    public String m_useVirus  = null;
    public String m_smtpUser = null;
    public String m_smtppass = null;
    public String m_smtpServer = null;
    public String m_useCentralDB = null;
    public String m_centraldbuser = null;
    public String m_centraldburl = null;
    public String m_centraldbpassword = null;

    public String getProxyURL() {return m_proxyUrl; };
    public String getCentralDBURL() {return m_centraldburl;}
    public String getSmtpUser() {return m_smtpUser;}
    public String getSmtpPass() {return m_smtppass;}



    public String getCentralDBUser()
    {
        return m_centraldbuser;
    }


    public String getCentralDBPassword()
    {
        return m_centraldbpassword;
    }

    public boolean getUseConsoleOutput()
     {
         return m_useConsoleOutput;
     }

	/**
	 *
	 * @return
	 */
    public boolean getUseVirusTotal()
    {
        if (m_useVirus != null && m_useVirus.toLowerCase().contains("yes"))
        {
            return true;
        }

        return false;
    }

    public boolean getUseSend()
     {
         if (m_useCentralDB != null && m_useCentralDB.toLowerCase().contains("yes"))
         {
             return true;
         }

         return false;
     }


    /*
        constructor for the ConfigHandler class
     */
    public ConfigHandler(String fileName)
    {
        try
        {

            if (fileName != null)
            {
                m_file = new File(fileName);
                m_reader = new BufferedReader(new FileReader(m_file));
                read();

            }
        }
        catch (Exception e)
        {
            m_reader = null;
        }

    }   // ConfigHandler


    private String catchData(String line, String in, String inData)
    {

        if (inData != null)
            return inData;

        if (line.startsWith(in))
        {
            return line.substring(in.length() + 1);
        }

        return inData;
    }   // catchData


    private boolean catchDataBoolean(String line, String in, boolean inData)
    {

        if (inData)
            return inData;

        if (line.startsWith(in))
        {
            return line.substring(in.length()).toLowerCase().contains("yes");
        }

        return false;
    }

	/**
	 *
	 */
    public void read()
    {

        try
        {

            String line = null;

            while ((line = m_reader.readLine()) != null)
            {

                if (line.startsWith("mysqlusername="))
                {
                    m_dbuser = line.substring("mysqlusername=".length());
                }
                else if (line.startsWith("mysqluserpass="))
                {
                    m_dbpw = line.substring("mysqluserpass=".length());
                }
                else if (line.startsWith("noanswer="))
                 {
                     m_noanswer = line.substring("noanswer=".length());
                 }
                 else if (line.startsWith("filepath="))
                {
                    m_filePath = line.substring("filepath=".length());
                }
                else if (line.startsWith("dbpath="))
                {
                    m_dbPath = line.substring("dbpath=".length());
                }
                else if (line.startsWith("logfile="))
                {
                     m_logPath = line.substring("logfile=".length());
                }

                else if (line.startsWith("proxyurl="))
                {
                     m_proxyUrl = line.substring("proxyurl=".length());
                }

	            m_accessToken = catchData(line, "accesstoken", m_accessToken);
	            m_accessTokenSecret = catchData(line, "accesstokensecret", m_accessTokenSecret);

	            m_consumerkey = catchData(line, "consumerkey", m_consumerkey);
	            m_consumerkeysecret = catchData(line, "consumerkeysecret", m_consumerkeysecret);
	            m_useTwitter = catchData(line, "usetwitter", m_useTwitter);


                m_db = catchData(line, "dbtype", m_db);
	            m_timeZone = catchData(line, "timezone", m_timeZone);
                m_proxyPort = catchData(line, "proxyport", m_proxyPort);
                m_proxyUser = catchData(line, "proxyuser", m_proxyUser);
                m_proxyPass = catchData(line, "proxypass", m_proxyPass);
                m_useVirus  = catchData(line, "usevirustotal", m_useVirus);
                m_smtpUser = catchData(line, "smptuser", m_smtpUser);
                m_smtppass = catchData(line, "smtppass", m_smtppass);
                m_smtpServer = catchData(line, "smptserver", m_smtpServer);
                m_popServer = catchData(line, "popserver", m_popServer);
                m_useCentralDB = catchData(line, "usecentraldb", m_useCentralDB);
                m_centraldbuser = catchData(line, "dbuser", m_centraldbuser);
                m_centraldbpassword = catchData(line, "dbpassword", m_centraldbpassword);
                m_centraldburl = catchData(line, "dburl", m_centraldburl);
                m_useConsoleOutput = catchDataBoolean(line, "useconsoleoutput", m_useConsoleOutput);
                m_userevproxy = catchDataBoolean(line, "userevproxy", m_userevproxy);

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }    // read


}
