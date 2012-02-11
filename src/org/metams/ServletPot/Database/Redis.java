package org.metams.ServletPot.Database;

import org.metams.ServletPot.plugins.Logger;
import redis.clients.jedis.Jedis;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.mail.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: flake
 * Date: 12/10/11
 * Time: 2:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Redis implements DBAccess
{

	private Jedis m_con = null;

	/**
	 * constructor for the Redis class
	 *
	 * @param l
	 */
	public Redis(Logger l)
	{
		m_l = l;
	}



	/**
	 * database open
	 *
	 * @param userName
	 * @param password
	 * @param url
	 * @return
	 */
	public boolean open(String userName, String password, String url)
	{

		m_con = new Jedis("localhost");
		m_con.connect();

		return (m_con != null);

	}   // open

	private Logger m_l = null;

	/**
	 * destroy the DB instance if wanted
	 */
	public void destroy()
	{
		m_con.save();
		m_con = null;
	}   // destroy


	/**
	 * retuns the counter value
	 *
	 * @param hash
	 * @param len
	 * @param DB
	 * @return
	 * @throws SQLException
	 */
	private long getCounter(long hash, long len, String DB) throws SQLException
	{


		String crcString = new Long(hash).toString();
		String lenString = new Long(len).toString();

		String out = m_con.get(DB + "_" + crcString + "_" + lenString);
		if (out == null)
			return 0;

		return new Long(out).longValue();


	}


	public int getNumberOfGetsPosts(String in) throws SQLException
	{

		/*
		 int x = 0;
		 ResultSet          rSet = null;
		 java.sql.Statement statement = null;

		  try
		  {

			  // Statements allow to issue SQL queries to the database
		   statement = m_con.createStatement();
			  String             sqlQuery  = "select * from " + m_databasePreBlock + in + ";";
			  rSet      = statement.executeQuery(sqlQuery);

			  rSet.last();
			  x = rSet.getRow();

			  rSet.close();
			  statement.close();

		  }
		  catch (Exception e)
		  {
			  e.printStackTrace();
			  if (rSet != null) rSet.close();
			  if (statement != null) statement.close();

		  }

		  return x;
				*/
		return 0;
	}


	public String[] getNumberOfGetsPostsHistory(String in, int history) throws SQLException
	{
		/*
				int x = 0;

				String[] strings = new String[history];
				 ResultSet          rSet = null;
			   java.sql.Statement statement = null;

				 try
				 {

					 // Statements allow to issue SQL queries to the database
					 statement = m_con.createStatement();
					 String             sqlQuery  = "select * from " +m_databasePreBlock + in + " where id >= " +  Integer.valueOf(history).toString() + ";";
					 rSet      = statement.executeQuery(sqlQuery);

					 while (rSet.next() && x != history -1)
					 {
						strings[x++] = rSet.getString("uri");
					 }

					 rSet.close();

				 }
				 catch (Exception e)
				 {

		 //            if (statement != null) statement.close();
					 e.printStackTrace();
					 rSet.close();
				 }

				 return strings;

				*/

		return null;

	}

	public int getNumberOfGets() throws SQLException
	{
		return getNumberOfGetsPosts("Gets");
	}


	public int getNumberOfPosts() throws SQLException
	{
		return getNumberOfGetsPosts("Posts");
	}

	public String[] getLastGets(int history) throws SQLException
	{
		return getNumberOfGetsPostsHistory("Gets", history);
	}

	public String[] getLastPosts(int history) throws SQLException
	{
		return getNumberOfGetsPostsHistory("Posts", history);
	}


	/**
	 * writes the IP with the current date in the database
	 *
	 * @param ip
	 * @return
	 */
	public boolean writeIP(String ip)
	{

		try
		{

			if (ip != null)
			{
				java.util.Date x = new java.util.Date();

				String dd = x.toString();

				System.out.println("WriteIP(): IP_" + ip + ":" + dd);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStream b64os = MimeUtility.encode(baos, "base64");
				b64os.write(ip.getBytes());
				b64os.close();

				String out = b64os.toString();
				m_con.set("IP_" + out, dd);				// out instead of IP

				m_con.save();
				return true;

			} else
			{
				return false;
			}

		}
		catch (MessagingException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}


	}   // writeIP


	/**
	 * writes a new URI
	 *
	 * @param URI
	 * @param hash
	 * @param len
	 * @param counter
	 * @param reqNr
	 * @return
	 */
	public boolean writeURI(String URI, long hash, long len, long counter, int reqNr)
	{

		int number = 0;
		if (URI == null)
			return false;


		if (existsURI(hash, len, reqNr))
			return true;


		// fix bogus URI
		if (URI.startsWith("//"))
			URI = URI.substring(1);

		if (URI.startsWith("/%20%20/"))
			URI = URI.substring(7);


		String nr;

		try
		{
			nr = m_con.get("URI_COUNTER");
			if (nr == null)
			{
				number = 1;
			} else
			{
				number = Integer.parseInt(nr) + 1;
			}
		} catch (Exception e)
		{
			System.out.println("Error at reading URI_COUNTER field");
			return false;
		}

		try
		{
			m_con.set("URI_COUNTER", new Integer(number).toString());
			m_con.rpush("URI", URI);
		} catch (Exception e)
		{
			System.out.println("Error at saving URI COUNTER or URI fields");
			return false;
		}

		String lenString = new Long(len).toString();
		String crcString = new Long(hash).toString();
		try
		{
			m_con.set("URIHASH_" + crcString + "_" + lenString, new Integer(number).toString());
		}
		catch (Exception e)
		{
			System.out.println("Error at saving URIHASH field");
			return false;
		}

		return true;
	}   // writeURI


	public int getCounter()
	{
		return 0;

	}   // getCounter

	public boolean increaseCounter()
	{

		return true;
	}




	/**
	 *
	 * @param URI
	 * @param hash
	 * @param len
	 * @param ip
	 * @param found
	 * @return
	 */
	public boolean writeGet(String URI, long hash, long len, String ip, String found)
	{

		if (URI == null)
			return false;

		// fix bogus URI
		if (URI.startsWith("//"))
			URI = URI.substring(1);

		if (URI.startsWith("/%20%20/"))
			URI = URI.substring(7);


		String nr = m_con.get("GET_" + URI);
		if (nr == null)
		{
			m_con.set("GET_" + URI, "1");
		} else
		{
			m_con.incr("GET_" + URI);
			m_con.rpush("GET", URI);
		}

		return true;

	}   // writeGet


	public void deleteURI(String line)
	{

		m_con.del("URI_" + line);
		m_con.save();

	}


	public boolean writeFile(long len, long crc32, long counter, int reqNr)
	{

		String lenString = new Long(len).toString();
		String crcString = new Long(crc32).toString();

		String keyString = "FILE_" + crcString + "_" + lenString;
		String nr = m_con.get(keyString);
		if (nr == null)
		{
			m_con.set(keyString, "1");
		}
		else
		{
			int number = Integer.parseInt(nr) + 1;
			//m_con.set(keyString, new Integer(number).toString());
			m_con.incr(keyString);
		}

		return true;
	}


	/*
		 *   returns true, if URI exists
		 */
	public boolean existsURI(long hash, long len, int reqNr)
	{

		String crcString = new Long(hash).toString();
		String lenString = new Long(len).toString();

		String uriString = "URIHASH_" + crcString + "_" + lenString;
		String exists = m_con.get(uriString);

		if (m_l != null && (exists != null))
			m_l.log("ServletPot.existsURI: URI (" + hash + ", " + len + ") here", reqNr);

		return (exists != null);
	}


	/*
		*   returns true, if file exists
		*/
	public boolean existsFile(long len, long hash, int reqNr)
	{


		String crcString = new Long(hash).toString();
		String lenString = new Long(len).toString();

		String exists = m_con.get("FILE_" + crcString + "_" + lenString);

		if (m_l != null && (exists != null))
			m_l.log("ServletPot.existsURI: FILE (" + hash + ", " + len + ") here", reqNr);

		return (exists != null);

	}   // existsFile


	/**
	 *
	 * @return
	 */
	public String[] getURI()
	{

		String nr = m_con.get("URI_COUNTER");
		if (nr == null)
			return null;

		List uri = m_con.lrange("URI", 0, new Integer(nr).intValue());

		String[] buffer = new String[new Integer(nr).intValue() + 1];
		
		System.out.println("Info: Found a total of " + new Integer(nr).intValue() + "URIs (URI_COUNTER)");
		System.out.println("Info: Found a total of " + uri.size() + "URIs (URI)");

		int runner = 0;
		Iterator<String> iterator = uri.iterator();
		while (iterator.hasNext())
		{
			String element = iterator.next();
			buffer[runner++] = element;
		}


		return buffer;

	}   // getURI


}
