package org.metams.ServletPot.plugins;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import sun.misc.BASE64Encoder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * Created with IntelliJ IDEA.
 * User: flake
 * Date: 7/28/12
 * Time: 9:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class CSIRTReport
{
	private static String m_url = "http://interface.csirt.cyscon.net/checkurl.gen.php?url=";
	private BASE64Encoder m_encoder = new BASE64Encoder();


	/**
	 * send the report to the core database system
	 * @param report
	 */
	public void sendReport(String report)
    {

        try
        {
			String reportFinal = m_encoder.encodeBuffer(report.getBytes());

            HttpGet method = new HttpGet(m_url + reportFinal);
            DefaultHttpClient base = new DefaultHttpClient();

            DefaultHttpClient client = getFake(base);
            client.getParams().setParameter("http.useragent", "Flake Test Client");

            ResponseHandler<String> response = new BasicResponseHandler();

            String returnCode = client.execute(method, response);

        }
        catch (Exception e)
        {
            System.err.println(e);
        }

    }	// sendReport


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


}
