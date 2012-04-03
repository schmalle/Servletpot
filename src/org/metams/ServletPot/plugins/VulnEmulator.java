package org.metams.ServletPot.plugins;

//~--- non-JDK imports --------------------------------------------------------

import org.metams.ServletPot.ConfigHandler;
import org.metams.ServletPot.Database.DBAccess;
import org.metams.ServletPot.Database.Hibernate.MySqlHibernate;
import org.metams.ServletPot.Database.MySql;
import org.metams.ServletPot.Database.Redis;
import org.metams.ServletPot.plugins.http.Downloader;
import org.metams.ServletPot.plugins.http.PHPHandler;
import org.metams.ServletPot.tools.Utils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

/**
 * User: flake
 * Date: May 2, 2010
 * Time: 7:00:28 PM
 */
public class VulnEmulator
{
    private Utils           	m_utils = null;
    private HandleAttackFiles 	m_fileHandler = null;
    private Logger          	m_l = null;
    private DBAccess           	m_database = null;
    private Sender          	m_send = null;
    private ConfigHandler   	m_config = null;
    private PHPHandler      	m_php = null;
    private Downloader      	m_dl = new Downloader();


	/**
	 * returns the database object
	 * @return
	 */
    public DBAccess getDB()
    {
        return m_database;
    }   // getDB

    /*
        returns an intialized database object
        @out: database object
     */

    public DBAccess getDatabase()
    {
        return m_database;
    }   // getDatabase


    /*
        stores the attack data for POST requests
     */

    public void storeAttackForPost(HttpServletRequest request, String attackType, String URI, int reqNr)
    {
        // check for data
        if (attackType == null)
            return;

        String ip = m_utils.getIP(request);
        m_database.writeURI(URI, m_utils.getCRC32(URI, false), URI.length(), 0, reqNr);

        int in = attackType.indexOf("FI): ");
        if (in == -1)
        {
            m_l.log("Info: storeAttackForPost failed as no valid attack type ("+attackType+") was given...", reqNr);
            return;
        }


        String attackTypeMini = attackType.substring(0, in + 4);

        m_l.log("Info: " + attackTypeMini + "from IP: " + ip + " at time " + new Date().toString() + " and user-agent: "+ request.getHeader("User-Agent") +" and request " + URI, reqNr);
        

        sendCentralDB(ip, URI, attackType);

    }   // storeAttackForPost


	/**
	 * initializes the correct DB
 	 * @param cf
	 * @param l
	 * @return
	 */
	private DBAccess initDB(ConfigHandler cf, Logger l)
	{
		String db = cf.getDB();
		if (db == null)
			return null;

		if (db.equalsIgnoreCase("mysql"))
			return new MySql(l);

		if (db.equalsIgnoreCase("hibernate"))
			return new MySqlHibernate(l);

		if (db.equalsIgnoreCase("redis"))
			return new Redis(l);

		return null;
	}


	/**
	 *
	 * @param cf
	 * @param u
	 * @param l
	 * @param reqNr
	 */
    public VulnEmulator(ConfigHandler cf, Utils u, Logger l, int reqNr)
    {

        if (cf != null)
        {
            m_utils = u;
            m_fileHandler = new HandleAttackFiles(u, cf, l);
            m_l = l;

			// init DB and open DB is existing
			m_database = initDB(cf, l);
            if (m_database != null)
				m_database.open(cf.getUserName(), cf.getUserPW(), cf.getDBPath());

			m_send = new Sender(cf.getCentralDBURL(), m_l);
            m_config = cf;
            m_php = new PHPHandler(u, l, m_fileHandler);
        }
        else if (l != null )
        {
            l.log("ServletPot: Error, confing file could not be read...", reqNr);
        }

    }    // VulnEmulator


	/**
	 *
	 * @param request
	 */
	public void handlePost(HttpServletRequest request)
    {

	    m_l.log("ServletPot: Error, dummy handlePost in VulnEmulator has been called ...", 42);

	    String x = request.getRequestURI();
        if (x != null)
        {
            x = "1";
        }


    }    // handlePost


    /*
        downloads a file and stores it in the DB
        @in:    fileName    - file to be downloaded
     */

    private void downloadFile(String fileName, int reqNr)
    {
        if (fileName != null && fileName.toLowerCase().startsWith("http"))
        {
            m_dl.init(m_database, m_utils, m_l, m_fileHandler, fileName, reqNr);
            try
            {
                m_dl.start();
            }
            catch (Exception e)
            {   // if the thread is not starting, start the download "normally"

                m_dl.downloadFile(reqNr);
            }
        }
    }   // downloadFile


    /*
     *   checks, if a parameter starts with http
     *   @in: request - httpservletrequest
     *   @in: URI   - URI to be parsed
     *   @in: reqNr - number of request
     *   @out: null if nothing found
     */
    private String checkForNewAttack(HttpServletRequest request, String URI, int reqNr)
    {
        Enumeration paramNames = request.getParameterNames();
        String attackType = null;
        String ip = m_utils.getIP(request);
        String method = request.getMethod();

        if (paramNames != null)
        {
            while (paramNames.hasMoreElements())
            {
                String paramName = (String) paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);

                if (paramValues != null)
                {
                    for (int runner = 0; runner <= paramValues.length - 1; runner++)
                    {
                        String parameter = paramValues[runner];
                        if ((parameter != null) && (attackType = attackCheck(parameter, URI)) != null)
                        {

							URI = shortenURI(URI, parameter);
							long hash =  m_utils.getCRC32(URI, false);
							

                            // store URI (for learning)
							if (m_database != null )
								m_database.writeURI(URI, hash, URI.length(), 0, reqNr);


                            downloadFile(parameter, reqNr);

                            m_l.log("Info: " + attackType + "from IP: " + ip + " at time " + new Date().toString() + " and " + method + " request " + URI + parameter + " and user-agent: "  + request.getHeader("User-Agent"), reqNr);

	                        m_l.log(null, "Info: " + attackType + "from IP: " + ip.substring(0,4) + ".Y.Z at time " + new Date().toString() + " ", reqNr);

                            sendCentralDB(ip, URI, attackType);

                            return attackType;
                        }   // if
                    }   // paramValues
                }   // if paramValues != null
            }   // while hasmoreleements
        }

        m_l.log("Info: Found no attack in " + method + " request " + URI + " from IP: " + ip + " at time " + new Date().toString() + " and user-agent: "  + request.getHeader("User-Agent"), reqNr );


        // nothing found
        return null;
    }    // checkForNewAttack


	/**
	 * shortens the URI to the valid part
	 * @param URI
	 * @param parameter
	 * @return
	 */
	private String shortenURI(String URI, String parameter)
	{

		URI = URI.toLowerCase();
		int indexHTTP = URI.indexOf("=http");
		int indexDT = URI.indexOf("=../");
		
		if (indexHTTP == -1) indexHTTP = 0;
		if (indexDT == -1) indexDT = 0;

		int index = indexDT + indexHTTP + 1;
		
		if (!(index < URI.length()))
		{
			index = URI.length();
		}

		//TODO normalize hiere even more

		return URI.substring(0, index);

	}   // shorten URI
	
	
	/**
	 * send data to virustotal
	 * @param ip
	 * @param URI
	 * @param attackType
	 */
    private void sendCentralDB(String ip, String URI, String attackType)
    {
        try
        {

	        TimeZone.setDefault(TimeZone.getTimeZone(m_config.getTimeZone()));

            if (m_config.getUseSend())
            {
                SimpleDateFormat x = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String y = x.format(new Date());


				// small workaround for DTAG internal fancy stuff
				String ident = "42";
				if (!m_config.getCentralDBUser().equalsIgnoreCase("gtms"))
				{
					ident = m_config.getCentralDBUser();
				}

                String message = m_send.getMessage(m_config.getCentralDBPassword(), m_config.getCentralDBUser(), ip, URI, y, attackType, ident);
                m_send.sendReport(message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }

    }   // sendCentralDB


	/**
	 * checks for suspicious traces in parameters
 	 * @param parameter
	 * @param URI
	 * @return string or NULL
	 */
	private String attackCheck(String parameter, String URI)
    {

		if (parameter == null)
			return null;

		if (URI == null)
			return  null;


	    String parLow = parameter.toLowerCase();
	    String uriLow = URI.toLowerCase();

        if (parLow.startsWith("http"))
        {
            return "Attack (RFI) ";
        }


        if (parameter.charAt(0) == 0x7c)
        {
            return "Attack (RFI) ";
        }


        if (parameter.charAt(0) == 0x5b)
         {
             return "Attack (RFI) ";
         }


        if (parameter.startsWith("'"))
        {
            return "Attack (SQL) ";
        }

        if (parLow.startsWith("ftp"))
        {
            return "Attack (RFI) ";
        }

        if (parLow.contains("etc/passwd"))
        {
            return "Attack (LFI, etc/passwd) ";
        }

        if (parLow.contains("etc/shadow"))
        {
            return "Attack (LFI, etc/shadow) ";
        }

        if (parLow.contains("../.."))
        {
            return "Attack (LFI, directory traversal) ";
        }

        if (uriLow.contains("etc/passwd"))
        {
            return "Attack (LFI, etc/passwd) ";
        }

        if (uriLow.contains("etc/shadow"))
        {
            return "Attack (LFI, etc/shadow) ";
        }

        if (uriLow.contains("../.."))
        {
            return "Attack (LFI, directory traversal) ";
        }

        if (uriLow.contains("..//.."))
        {
            return "Attack (LFI, directory traversal) ";
        }

        return handleDedicated(uriLow);

    }   // attackCheck


	/**
	 * handles dedicated calls
	 * @param URI
	 * @return
	 */
	private String handleDedicated(String URI)
    {

        String vectors[] = {"/index.php?option=com_rokdownloads&controller=", "Attack (Joomla, XFI) ",
                            "/wp-content/plugins/mygallery/myfunctions/mygallerybrowser.php?mypath=", "Attack (MyGallerie, XFI) ",
                            "/wp-content/plugins/mygallery/myfunctions/mygallerybrowser.php?mypath=", "Attack (MyGallerie, XFI) ",
                            "/program/modules/mods_full/shopping_cart/includes/login.php?_SESSION%5Bdocroot_path", "Attack (XFI)" ,
                            "/index.php?option=com_fabrik&view=table&tableid='", "Attack (SQL Injection)",
                            "/index.php?option=com_gcalendar&controller=", "Attack (RFI) ",
                            "/skin_shop/standard/3_plugin_twindow/twindow_notice.php?shop_this_skin_path=", "Attack (RFI) ",
                            "/index.php?option=com_jresearch&controller=", "Attack (RFI) ", 
                            "/index.php?option=com_gcalendar&controller=", "Attack (RFI) ",
                            "/index.php?option=com_gcalendar&view=event&eventid=peler&start=memek&end=kentu&gcid=", "Attack (SQL Injection, LFI) ", 
                            "/phpmyadmin/config/config.inc.php?p=phpinfo();", "Attack (XFI) ",
                            "/pma/config/config.inc.php?p=phpinfo();", "Attack (XFI) ",
                            "/index.php?option=com_artforms&task=tferforms&viewform=1+union+select+", "Attack (SQL Injection)",
                            "/index.php?option=com_pccookbook&page=viewuserrecipes&user_id=-1", "Attack (SQL Injection)",
                            "/index.php?option=com_datsogallery&func=detail&id=-1", "Attack (SQL Injection)",
                            "/index.php?option=com_pcchess&itemid=-1&page=players&user_id=-1", "Attack (SQL Injection)",
                            " /index.php?option=com_gigcal&itemid=78&id=-999+union+all+select", "Attack (SQL Injection)",
                            "/awstatstotals/awstatstotals.php?sort=%7b", "Attack (RFI) ",
							"/components/com_flexicontent/librairies/phpthumb/phpthumb.php?src=file.jpg&fltr[]=blur", "Attack (RFI)",
							"/admin/tiny_mce/plugins/ibrowser/scripts/phpthumb/phpthumb.php?src=file.jpg&fltr[]=blur", "Attack (RFI)",
							"/cms/plugins/content/jthumbs/includes/phpthumb.php?src=file.jpg&fltr[]=blur|", "Attack(RFI)",
							"/common/scripts/phpthumb/phpthumb.php?src=file.jpg&fltr[]=blur", "Attack (RFI)",
							"/components/com_flexicontent/librairies/phpthumb/phpthumb.php?src=file.jpg&fltr[]=", "Attack (RFI)",
							"/phpmyadmin/config/config.inc.php?c=", "Attack (PhpMyAdmin)",
							"/conlib/prepend.php3?cfg[path][contenido]=", "Attack (RFI)",
							"/wp-content/plugins/com-resize/phpthumb/phpthumb.php?src=", "Attack (RCE)",
							"/wp-content/themes/comfy-plus/scripts/phpthumb/phpthumb.php?src=", "Attack (RCE)",
							"/myadmin/config/config.inc.php?c=echo", "Attack (RCE)",
							"/index.php?content=/proc/self/enviro", "Attack (LFI)",
							"/skin/sirini_simplism_gallery_v4/setup.php?dir", "Attack (RFI)",
							"/content/phpthumb/phpthumb.php?src=file.jpg&fltr[]", "Attack (RFI)",
							"/wp-content/themes/basic//cache/external_1e4de80f0b49ce95d91f9ed089ba3743.php", "Attack",
				            "/wp-content/plugins/highlighter/libs//cache/1e4de80f0b49ce95d91f9ed089ba3743.php", "Attack",
							"/wp-content/plugins/highlighter/libs//temp/2f8380899b6b4ccac4b9ec88eae335e5.php", "Attack",
							"/index.php?option=com_artforms&task=ferforms&viewform=-1%20union%20select%20", "Attack (SQL Injection)",
							"/wp-content/themes/constructor/layouts//cache/external_4ef5475b54a2f9bddf06cdbf8f2f27ca.php", "Attack",
							"/wp-content/themes/constructor/layouts//cache/4ef5475b54a2f9bddf06cdbf8f2f27ca.php", "Attack",
							"/index.php?option=com_user&view=reset", "Attack",
							"//components/com_extcalendar/lib/mail.inc.php?config_ext[lib_dir]=", "Attack",
							"/mysqladmin/config/config.inc.php?c=echo%20", "Attack",
							"/webdb/config/config.inc.php?c=echo%20", "Attack",
							"/websql/config/config.inc.php?c=echo%20", "Attack",
							"/phppgadmin/config/config.inc.php?c=echo%20", "Attack",
				            "/phpmy-admin/config/config.inc.php?c=echo%20", "Attack",
							"/admin/config/config.inc.php?c=echo%20", "Attack (RCE)",
							"/pma/config/config.inc.php?c=echo%20", "Attack (RCE)",
							"/phpmyadmin-2.5.5-pl1/index.php", "Attack (Admin)",
							"/phpmyadmin-2.5.6-rc2/index.php", "Attack (Admin)",
							"/phpmyadmin-2.5.7/index.php", "Attack (Admin)",
							"//wp-content/themes/deep-blue/cache/", "Attack (WP)",
							"/wp-content/plugins/category-grid-view-gallery/includes/timthumb.php", "Attack (WP)",
							"/wp-content/plugins/is-human/engine.php?action=log-reset&type=ih_options();eval(base64_decode(zwnobyanpgjypkpgcnlfjzsncmvjag8gjzxicj5bbmfzs2know));error", "Attack (WP)"

        };

        if (URI == null)
            return null;


        for (int runner = 0; runner <= vectors.length - 1; runner+=2)
        {
            if (URI.contains(vectors[runner].toLowerCase()))
                return vectors[runner+1];  

            if (URI.contains("/".concat(vectors[runner].toLowerCase())))
                return vectors[runner+1];

            if (URI.contains("/%20%20/".concat(vectors[runner].toLowerCase())))
                return vectors[runner+1];


        }
        

        return null;
    }   // handleDedicated


    /*
     *   handles the incoming requests
     */
    public String handleRequest(String URI, HttpServletRequest request, int reqNr)
    {

        URI = URI.toLowerCase();

        // reaching this points means that an RFI attack was ...
        return checkForNewAttack(request, URI, reqNr);

    }    // handleRequest
}


