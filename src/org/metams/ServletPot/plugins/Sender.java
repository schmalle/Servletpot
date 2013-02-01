package org.metams.ServletPot.plugins;


import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;



/**
 * User: flake
 * Date: Jun 13, 2010
 * Time: 8:37:15 PM
 */
public class Sender
{

    private String m_url = null;
	private Logger m_logger = null;


	/**
	 * constructor for the Sender class
	 * @param url
	 * @param l
	 */
    public Sender(String url, Logger l)
    {
        m_url = url;
	    m_logger = l;
    }


	/**
	 * returns the message to be send to the server
	 * @param token
	 * @param userName
	 * @param ip
	 * @param req
	 * @param time
	 * @param attackType
	 * @param ident
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getMessage(String token, String userName, String ip, String req, String time, String attackType, String ident, String host) throws UnsupportedEncodingException
    {


        req = URLEncoder.encode(req, "UTF-8");

        String message =

                "<EWS-SimpleMessage version=\"1.0\">\n" +
                        "        <Authentication>\n" +
                        "                <username>" + userName + "</username>\n" +
                        "                <token>" + token + "</token>\n" +
                        "        </Authentication>\n" +
                        "        <Alert messageid=\""+ host +"\">\n" +
                        "            <Analyzer id=\""+ident+"\" name=\""+ host +"\"/>     \n" +
                        "                <CreateTime tz=\"0100\">"+ time +"</CreateTime>                                  \n" +
                        "                <Source>\n" +
                        "                        <Node>\n" +
                        "                                <Address category=\"ipv4-addr\"><address>" + ip + "</address></Address>       \n" +
                        "                        </Node>\n" +
                        "                </Source>                                      \n" +
                        "                <Target ident=\""+ host +"\">                           \n" +
                        "                        <Node>\n" +
                        "                                <Address category=\"ipv4-addr\">"+ ip +"</Address>             \n" +
                        "                        </Node>\n" +
                        "                        <Service ident=\"0\">                                             \n" +
                        "                                <dport>80</dport>\n" +
                        "                        </Service>\n" +
                        "                </Target>\n" +
                        "                <Classification origin=\""+attackType+"\" ident=\""+ attackType +"\" text=\""+ attackType+"\"/> \n" +
                        "                <AdditionalData type=\"string\" meaning=\"user-agent\">Mozilla/5.0</AdditionalData>    \n" +
                        "                <AdditionalData type=\"string\" meaning=\"header\">"+ attackType +"</AdditionalData>  \n" +
                        "                <AdditionalData type=\"string\" meaning=\"request\">"+ req +"</AdditionalData>\n" +
                        "                <AdditionalData type=\"string\" meaning=\"domain\">evolution.hospedando.com</AdditionalData>   \n" +
                        "        </Alert>\n" +
                        "</EWS-SimpleMessage>";

        return message;
    }     // getMessage


	/**
	 *
	 * @param httpClient
	 * @return
	 */
	public DefaultHttpClient getFake(DefaultHttpClient httpClient)
    {
        try
        {
            // First create a trust manager that won't care.
            X509TrustManager trustManager = new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException
                {
                    // Don't do anything.
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException
                {
                    // Don't do anything.
                }

                public X509Certificate[] getAcceptedIssuers()
                {
                    // Don't do anything.
                    return null;
                }
            };


            // Now put the trust manager into an SSLContext.
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

            // Use the above SSLContext to create your socket factory
            // (I found trying to extend the factory a bit difficult due to a
            // call to createSocket with no arguments, a method which doesn'< ></>
            // exist anywhere I can find, but hey-ho).
            SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            // If you want a thread safe client, use the ThreadSafeConManager, but
            // otherwise just grab the one from the current client, and get hold of its
            // schema registry. THIS IS THE KEY THING.
            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry schemeRegistry = ccm.getSchemeRegistry();

            // Register our new socket factory with the typical SSL port and the
            // correct protocol name.
            schemeRegistry.register(new Scheme("https", sf, 443));

            // Finally, apply the ClientConnectionManager to the Http Client
            // or, as in this example, create a new one.
            return new DefaultHttpClient(ccm, httpClient.getParams());
        }
        catch (Throwable t)
        {

			t.printStackTrace();
            // AND NEVER EVER EVER DO THIS, IT IS LAZY AND ALMOST ALWAYS WRONG!
            System.out.println("Servletplot.plugins.sender: Error in ssl handling");
            return null;
        }
    }


	/**
	 * send the report to the core database system
	 * @param report
	 */
	public void sendReport(String report)
    {

        try
        {
            HttpPost method = new HttpPost(m_url);
            DefaultHttpClient base = new DefaultHttpClient();

            DefaultHttpClient client = getFake(base);
            client.getParams().setParameter("http.useragent", "Flake Test Client");

            StringEntity strent = new StringEntity(report);
            strent.setContentType("text/xml; charset=utf-8");
            method.setEntity(strent);
            ResponseHandler<String> response = new BasicResponseHandler();

            String returnCode = client.execute(method, response);
	        if (returnCode != null && !returnCode.contains("<StatusCode>OK</StatusCode>"))
	        {
				System.out.println("Info: Send NOT data to central db");



				if (m_logger!= null)
					m_logger.log("Error in sending data to central database", 42);
	        }
			else
			{
				System.out.println("Info: Send data to central db");
			}

        }
        catch (Exception e)
        {
            System.err.println(e);
        }

    }	// sendReport


}
