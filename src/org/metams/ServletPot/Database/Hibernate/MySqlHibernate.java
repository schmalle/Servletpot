package org.metams.ServletPot.Database.Hibernate;


import org.hibernate.Query;
import org.hibernate.mapping.List;
import org.metams.ServletPot.Database.DBAccess;
import org.metams.ServletPot.plugins.Logger;

import java.sql.*;
import java.util.Hashtable;
import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.metams.ServletPot.Database.Hibernate.HibernateURI;
import org.metams.ServletPot.Database.Hibernate.HibernateUtil;

/**
 * User: flake
 * Date: May 30, 2010
 * Time: 8:06:52 PM
 */
public class MySqlHibernate implements DBAccess
{
	private Logger m_l = null;


	SessionFactory sf = new HibernateUtil().getSessionFactory();
	Session session = sf.openSession();
	Transaction transaction = null;


	/*
				destroy code
			 */
	public void destroy()
	{


	}   // destroy


	/**
	 * Contructor for the MySQL class
	 *
	 * @param l
	 */
	public MySqlHibernate(Logger l)
	{
		m_l = l;
	}   // constructor for the MySql class


	/**
	 * Contructor for the MySQL class
	 */
	public MySqlHibernate()
	{

	}   // constructor for the MySql class


	/**
	 * open the database connection
	 *
	 * @param userName
	 * @param password
	 * @param url
	 * @return
	 */
	public boolean open(String userName, String password, String url)
	{
		return true;
	}	// open


	/**
	 * retuns the counter value
	 *
	 * @param hash
	 * @param len
	 * @param DB
	 * @return
	 * @throws SQLException
	 */
	private long getCounter(long hash, long len, String DB)
	{
		session = sf.openSession();

		Query query = session.createQuery("SELECT cust.counter as COUNTER FROM org.metams.ServletPot.Database.Hibernate.Hibernate" + DB + " AS cust WHERE cust.hash=" + hash + " AND cust.length=" + len);
		java.util.List counterList = query.list();

		session.close();

		Long counterInteger = (Long) counterList.get(0);
		long counter = counterInteger.longValue();

		return counterInteger.longValue();
	}


	/**
	 * @param URI
	 * @param hash
	 * @param len
	 * @param counter
	 * @param reqNr
	 * @return
	 */
	public boolean writeURI(String URI, long hash, long len, long counter, int reqNr)
	{

		if (URI == null)
			return false;

		// fix bogus URI
		if (URI.startsWith("//"))
			URI = URI.substring(1);

		if (URI.startsWith("/%20%20/"))
			URI = URI.substring(7);

		session = sf.openSession();

		// if the URI exists, just increase the counter
		if (existsURI(hash, len, reqNr))
		{
			Query query = session.createQuery("SELECT cust.counter as COUNTER FROM org.metams.ServletPot.Database.Hibernate.HibernateURI AS cust WHERE cust.hash=" + hash + " AND cust.length=" + len);
			java.util.List counterList = query.list();

			Long counterInteger = (Long) counterList.get(0);
			counter = 1 + counterInteger.longValue();

			executeHibernateQuery("DELETE FROM org.metams.ServletPot.Database.Hibernate.HibernateURI WHERE hash=" + hash + " AND length=" + len);
		}

		HibernateURI newURI = new HibernateURI();
		newURI.setlength(len);
		newURI.sethash(hash);
		newURI.seturi(URI);
		newURI.setcounter(counter);

		transaction = session.beginTransaction();
		session.save(newURI);
		transaction.commit();
		session.close();

		return true;

	}   // writeURI


	/**
	 * returns the current counter value from the configuration
	 *
	 * @return
	 */
	public int getCounter()
	{

		session = sf.openSession();

		Query query = session.createQuery("SELECT cust.counter as COUNTER FROM org.metams.ServletPot.Database.Hibernate.HibernateConfig AS cust ");
		java.util.List counterList = query.list();

		//session.close();

		session.close();
		if (counterList.size() != 0)
		{
			Long counterInteger = (Long) counterList.get(0);


			return counterInteger.intValue();
		}


		return 0;
	}   // getCounter


	/**
	 * @return
	 */
	public boolean increaseCounter()
	{


		int counter = getCounter();


		executeHibernateQuery("DELETE FROM org.metams.ServletPot.Database.Hibernate.HibernateConfig WHERE counter <" + counter + 1);

		session = sf.openSession();

		HibernateConfig newURI = new HibernateConfig();
		newURI.setcounter(counter + 1);

		transaction = session.beginTransaction();
		session.save(newURI);
		transaction.commit();
		session.close();

		return true;
	}


	/**
	 * executes a single one shot request to a db
	 *
	 * @param sqlQuery
	 */
	public void executeHibernateQuery(String sqlQuery)
	{
		session = sf.openSession();

		Query query = session.createQuery(sqlQuery);
		int row = query.executeUpdate();
		session.close();

	}


	/**
	 * write code for HTTP post
	 *
	 * @param URI
	 * @param data
	 * @param hash
	 * @param len
	 * @param counter
	 * @param ip
	 * @param found
	 * @param reqNr
	 * @return
	 * @throws SQLException
	 */
	public boolean writePost(String URI, String data, long hash, long len, long counter, String ip, String found, int reqNr) throws SQLException
	{

		return writeURI(URI, hash, len, counter, reqNr);

	}   // writePost


	/**
	 * deletes the line with the given URI
	 *
	 * @param line
	 */
	public void deleteURI(String line)
	{

		session = sf.openSession();

		String hql = "delete from org.metams.ServletPot.Database.Hibernate.HibernateURI URIs WHERE URI= :URI";
		Query query = session.createQuery(hql);
		query.setString("URI", line);

		int row = query.executeUpdate();
		session.close();
		// return true;
	}


	/**
	 * write a file database entry, if file is not existing
	 *
	 * @param len
	 * @param crc32
	 * @param counter
	 * @param reqNr
	 * @return
	 */
	public boolean writeFile(long len, long crc32, long counter, int reqNr)
	{
		session = sf.openSession();

		if (len == 0)
			return true;

		// check if the file is originally existing
		if (existsFile(len, crc32, reqNr))
		{

			counter = 1 + getCounter(crc32, len, "File");
			executeHibernateQuery("DELETE FROM org.metams.ServletPot.Database.Hibernate.HibernateFile WHERE hash=" + crc32 + " AND length=" + len);
		}

		java.util.Date x = new java.util.Date();
		String d = x.toString();

		HibernateFile newURI = new HibernateFile();
		newURI.setlength(len);
		newURI.sethash(crc32);
		newURI.setfound(d);
		newURI.setcounter(counter);

		transaction = session.beginTransaction();
		session.save(newURI);
		transaction.commit();
		session.close();

		return true;
	}


	/**
	 * queries a table and asks for the existance of a dedicated item
	 *
	 * @param hash
	 * @param len
	 * @param table
	 * @return
	 */
	public Boolean exists(long hash, long len, String table, String outputName, int reqNr)
	{
		session = sf.openSession();

		Query query = session.createQuery("SELECT cust.id as ID FROM org.metams.ServletPot.Database.Hibernate." + table + " AS cust WHERE cust.hash=" + hash + " AND cust.length=" + len);

		boolean result = (query.uniqueResult() != null);

		if (m_l != null && result)
		{
			m_l.log("Info: " + outputName + " exists with CRC: " + hash + "and len: " + len, reqNr);
		}

		session.close();
		return result;
	}


	/**
	 * queries the existance of a file
	 *
	 * @param hash
	 * @param len
	 * @param reqNr
	 * @return
	 */
	public boolean existsURI(long hash, long len, int reqNr)
	{
		return exists(hash, len, "HibernateURI", "URI", reqNr);
	}


	/**
	 * queries the existance of a file
	 *
	 * @param hash
	 * @param len
	 * @param reqNr
	 * @return
	 */
	public boolean existsFile(long len, long hash, int reqNr)
	{
		return exists(hash, len, "HibernateFile", "File", reqNr);
	}


	/**
	 * returns an array of strings
	 *
	 * @return
	 */
	public java.util.List getURI()
	{

		session = sf.openSession();

		Hashtable strings = new Hashtable();
		int counter = 0;
		String[] output = null;


		java.util.List results = session.createQuery("select e.uri AS URI from org.metams.ServletPot.Database.Hibernate.HibernateURI AS e ").list();

		session.close();
		return results;
	}   // getURI

}




