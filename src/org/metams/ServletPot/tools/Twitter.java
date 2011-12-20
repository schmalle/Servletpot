package org.metams.ServletPot.tools;

import org.metams.ServletPot.plugins.Logger;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by IntelliJ IDEA.
 * User: flake
 * Date: 4/26/11
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Twitter
{

	private Logger m_logger = null;
	private twitter4j.Twitter m_twitter = null;
	private boolean m_active = false;
	private boolean m_useTwitter = false;

	/**
	 * constructor for the twitter class
	 * @param accessToken
	 * @param accessTokenSecret
	 * @param consumerKey
	 * @param consumerSecret
	 * @param l
	 * @param useTwitter
	 */
	public Twitter(String accessToken, String accessTokenSecret, String consumerKey, String consumerSecret, Logger l, boolean useTwitter)
	{
		try
		{
			m_logger = l;
			m_useTwitter = useTwitter;
			m_active = useTwitter;

			System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
			System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);

			AccessToken x = new AccessToken(accessToken, accessTokenSecret);
			m_twitter = new TwitterFactory().getInstance();

			m_twitter.setOAuthAccessToken(x);
		}
		catch (Exception e)
		{
			 e.printStackTrace();
		}
	}


	/**
	 * @param text
	 */
	public void setStatus(String text)
	{

		if (!m_useTwitter)
			return;

		if (m_logger != null) m_logger.log("Updating twitters status....", 0);

		try
		{
		     m_twitter.updateStatus(text);
		}
		catch (twitter4j.TwitterException e)
		{
			if (m_logger != null) m_logger.log(e.toString(), 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}





