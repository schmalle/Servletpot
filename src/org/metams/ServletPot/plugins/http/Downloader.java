package org.metams.ServletPot.plugins.http;

import org.metams.ServletPot.Database.DBAccess;
import org.metams.ServletPot.plugins.HandleAttackFiles;
import org.metams.ServletPot.plugins.Logger;
import org.metams.ServletPot.tools.Utils;

import java.util.Hashtable;
import java.util.Random;

/**
 * User: flake
 * Date: Jul 12, 2010
 * Time: 9:17:50 PM
 */
public class Downloader extends Thread
{

    private DBAccess            m_database = null;
    private Utils               m_utils    = null;
    private Logger              m_l        = null;
    private HandleAttackFiles   m_fileHandler = null;
    private String              m_fileName = null;
    private Hashtable           m_downloadedFiles = new Hashtable();
    private int                 m_reqNr     = 0;


    /*
        init all relevant variables
     */
    public void init(DBAccess a, Utils u, Logger l, HandleAttackFiles f, String n, int reqNr)
    {
        m_database = a;
        m_utils = u;
        m_l = l;
        m_fileHandler = f;
        m_fileName = n;
        m_reqNr = reqNr;
    }   // init

    public void run()
    {
            try
            {

                java.util.Random r = new Random();


                int waitTime = r.nextInt(10000); //(int)(Math.random()*10000);
                waitTime+=1000;

                sleep(waitTime);
            }
            catch (InterruptedException e)
            {
            }
            downloadFile(m_reqNr);
    }   // run


    /*
        downloads a file and stores it in the DB
        @in:    fileName    - file to be downloaded
        @in:    reqNr       - number of request
     */
    public void downloadFile(String fn, int reqNr)
    {
        m_fileName = fn;
        downloadFile(reqNr);

    }   // downloadFile(String fn)
    

    /*
        downloads a file and stores it in the DB
        @in:    -
     */
    public void downloadFile(int reqNr)
    {

        // download file only once
        if (m_downloadedFiles.containsKey(m_fileName))
        {

            m_l.log("ServletPot.downloadFile: File " + m_fileName + " already downloaded....", reqNr);
            return;
        }

        m_downloadedFiles.put(m_fileName, 0);

        m_fileHandler.download(m_fileName, reqNr);

        long len = m_utils.getLen();
        //long crc = m_utils.getCRC32();

        if (len != 0)
        {
            m_l.log("Length is: " + m_utils.getLen() + " CRC32 is: "
                    + m_utils.getCRC32(), reqNr);
			if (m_database != null )
				m_database.writeFile(m_utils.getLen(), m_utils.getCRC32(), 0, reqNr);
        }
        else
        {
            m_l.log("ServletPot.downloadFile: File " + m_fileName + " not downloaded....", reqNr);

        }

    }   // downloadFile

}
