package org.metams.ServletPot.plugins.http;

import org.metams.ServletPot.plugins.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * User: flake
 * Date: Jul 20, 2010
 * Time: 8:50:21 PM
 */
public class PostAnalyzer
{


    private PHPHandler  m_php = null;                                               // php handling class
    private Logger      m_l = null;                                                 // logging module
    private Hashtable   m_emulatedCode = new Hashtable();                           // storage for analyzed PHP code


    /*
        returns emulated code, if existing
        @in:    reqnr
        @out:   emulated PHP code
     */
    public String getEmulatedCode(int reqNr)
    {
        // getEmulatedCode() stub within PostHandler

        return (String) m_emulatedCode.get(reqNr);

    }   // getEmulatedCode


    /*
        constructor for the PostHandler class
        @in: PHPHandler p
     */
    public PostAnalyzer(PHPHandler p, Logger l)
    {
        m_php = p;
        m_l = l;
    }   // PostAnalyzer


    private String[] postData =
            {
                // POST        Type              Parameter 1       Dont Copy Value  Parameter 2    Copy Value as Attack Ende  
                    "/contact.php", "Attack (LFI): ",           "2", "send-contactus", "#",             "author_name", "#COPY#",
                    "//contact.php", "Attack (LFI): ",          "2", "send-contactus", "#",             "author_name", "#COPY#",
                    "/%20%20/contact.php", "Attack (LFI): ",    "2", "send-contactus", "#",             "author_name", "#COPY#",
            };


    /**
     * anazyle the POST request
     * @param request
     * @param URI
     * @param reqNr
     * @return
     */
    public String analyze(HttpServletRequest request, String URI, int reqNr)
    {
        int runner = 0;
        Enumeration paramNames = request.getParameterNames();
        Hashtable params = new Hashtable();

        if (paramNames != null)
        {

           // String paramName (String)paramNames.
            while (paramNames.hasMoreElements())
            {

                String paramName = (String) paramNames.nextElement();
                params.put(paramName, paramName);
            }
        }


        while (runner <= postData.length - 1)
        {
            String x = postData[runner++];
            String attackType = postData[runner++];
            int numberOfParametersinRule = Integer.parseInt(postData[runner++]);
            int nextRule = runner + (2 * numberOfParametersinRule);

            if (x.equalsIgnoreCase(URI))
            {

                int foundCounter = 0;

                for (int inRunner = 0; inRunner <= numberOfParametersinRule - 1; inRunner++)
                {
                    String param = postData[runner++];
                    String cmd = postData[runner++];

                    if (params.contains(param))
                    {
                        foundCounter++;
                        String[] values = request.getParameterValues(param);
                        if (values != null && values.length != 0 && cmd.equals("#COPY#"))
                        {
                            attackType = attackType.concat(values[0]);

                            m_l.log("PHP Post Data: " + cmd + " data: " + values[0], reqNr);

                            m_php.checkForDownload(values[0], reqNr);

                            String out = m_php.parse(values[0], reqNr);
                            if (out != null)
                            {
                                m_emulatedCode.put(reqNr, out);
	                            m_l.log("ServletPot.PostAnalzer.analyze(): Found emulatable code ...", reqNr);

                            }

                        }

                    }   // check for Param


                } // parse dedicated rule

                if (numberOfParametersinRule == foundCounter)
                    return attackType;

            }   // if x.equalsIgnoreCase

            else
            {   // no comparable string found

                runner = nextRule;
                //runner++;
            }

        }   // runner <= postData.length
        return null;

    }   // analyze

}
