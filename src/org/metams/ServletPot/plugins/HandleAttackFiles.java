package org.metams.ServletPot.plugins;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.metams.ServletPot.ConfigHandler;
import org.metams.ServletPot.tools.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: flake
 * Date: May 2, 2010
 * Time: 9:03:42 PM
 */
public class HandleAttackFiles
{
    private String              m_fileDir = null;
    private DefaultHttpClient   m_httpclient = new DefaultHttpClient();
    private Utils               m_util = null;
    private Logger              m_log = null;
    private Virustotal          m_vt = null;
    private boolean             m_useVirusTotal = false;
    private String              m_sendName = null;


    /*
        constructor for the HandleAttackFiles class
        @in: u  - reference to Utils class
             cf - reference to configs
             l  - logger
     */
    public HandleAttackFiles(Utils u, ConfigHandler cf, Logger l)
    {

        if (cf != null)
        {
            m_sendName = cf.getSmtpUser();
            m_fileDir = cf.getFilePath();
            m_log = l;
            m_util = u;
            m_vt = new Virustotal(l, cf);
            m_useVirusTotal = cf.getUseVirusTotal();
        }
    }   // HandleAttackFiles




    /*
        creates a tempory file
        @in httpEntity for download object
        out: filename
     */
    private String handleTempFile(AtomicReference<HttpEntity> httpEntity, int reqNr) throws IOException
    {
        File a = File.createTempFile("flake", "flake");

        FileOutputStream fos = new java.io.FileOutputStream(a);
        httpEntity.get().writeTo(fos);
        fos.flush();
        fos.close();

        // if file is not written, delete the object
        if (a.length() == 0)
        {
            if (!a.delete())
            {
                m_log.log("ServletPot.handleTempFile: Error deleting time file " + a.getName(), reqNr);

            }

        }

        return a.getPath();
    }       // handleTempFile


    /*
     *   download a file from a given URI
     *  @in:    url -   url to be downloaded
     */
    public boolean download(String url, int reqNr)
    {
        // sanity check
        if (url == null)
                return false;

        m_log.log("ServletPot.download: Executing request " + url, reqNr);

        try
        {

            // execute query and retrieve data
            HttpResponse response = m_httpclient.execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() != 200)
            {
                m_log.log("Servletpot.download: Error in downloading file "+ url + " ErrorCode: " + response.getStatusLine(), reqNr);
                return false;
            }

            AtomicReference<HttpEntity> httpEntity = new AtomicReference<HttpEntity>(response.getEntity());

            if (httpEntity.get() != null)
            {

                // create tempFile
                String tempFilePath = handleTempFile(httpEntity, reqNr);

                // file length 0 means somekind of error
                int len = (int) m_util.getLen(tempFilePath);
                if (len == 0)
                {
                    m_log.log("Servletpot.download: URL " + url + " contains no file", reqNr);
                    return true;
                }

                long crc32 = m_util.getCRC32(tempFilePath, true);

                String destName = m_fileDir + Integer.toString(len) + "_" + crc32;

                File dest = new File(destName);
                if (!dest.exists())
                {

                    File tempFile = new File(tempFilePath);
                    if (!tempFile.renameTo(new File(destName)))
                    {
                        m_log.log("Servletpot.download: Could not rename files (" + tempFilePath + ", " + destName + ")", reqNr);
                    }

                    // check if relevant at all
                    if (m_useVirusTotal)
                        m_vt.send(m_sendName, destName, reqNr);

                }
                
                m_util.setTempName(destName);
                return true;
            }

            return false;
        }
        catch (IOException e)
        {
            return false;
        }
    }    // download



 
}   // HandleAttackFiles

