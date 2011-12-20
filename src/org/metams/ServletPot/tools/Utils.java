package org.metams.ServletPot.tools;

import org.metams.ServletPot.ConfigHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

/**
 * User: flake
 * Date: Jul 3, 2010
 * Time: 1:57:41 PM
 */
public class Utils
{

    private ConfigHandler m_cf = null;


    /*
        returns the IP adress of the caller
        @in:    HttpServletrequest
        @out:   IP adress as string
     */
    public String getIP(HttpServletRequest request)
    {
        String ip = request.getRemoteAddr();
        if (ip.contains("127.0.0.1") && m_cf.getUseRevProxy())
        {
            ip = request.getHeader("X-Forwarded-For");

            if (ip == null)
            {
                ip = request.getHeader("CLIENT_IP");
                if (ip == null)
                    ip = "127.0.0.1";
            }
                        
        }

        return ip;
    }   // getIP


    public Utils(ConfigHandler cf)
    {
        m_cf = cf;
    }

    /*
        generic print function depending on console
     */
    public final void print(String s)
    {
        if (m_cf.getUseConsoleOutput())
            System.out.println(s);

    }


    private String m_tempName = null;

    public String getTempName() {return m_tempName; }
    public void setTempName(String x ) {m_tempName = x; }


    public long getCRC32()
    {
        return getCRC32(m_tempName, true);
    }
        
    /*
        returns the len of a given filename
        @out:   long value (-1 in error case
     */
     public long getLen()
     {
         return getLen(m_tempName);
     }  // getLen


    public long getCRC32(String fileName, boolean isFile)
    {

        // sanity check
        if (fileName == null)
        {
            return -1;
        }

        try
        {
            byte b[] = fileName.getBytes();

            if (isFile)
            {
                File x = new File(fileName);
                int len = (int) getLen(fileName);
                b = new byte[len];
                FileInputStream x2 = new FileInputStream(x);

                int len2 = x2.read(b);
                x2.close();

                if (len != len2)
                {
                    return -1;
                }


            }

            CRC32 crc = new CRC32();

            crc.reset();
            crc.update(b);

            return crc.getValue();
        }
        catch (IOException e)
        {
            return -1;
        }
    }   // getCRC32


    /*
        returns the len of a given filename
        @in:    fileName
        @out:   long value (-1 in error case
     */
    public long getLen(String fileName)
    {
        // sanity check
        if (fileName == null)
            return -1;


        try
        {
            File x = new File(fileName);
            return x.length();
        }
        catch (Exception e)
        {
            return -1;
        }

    }   // getLen


}
