package org.metams.ServletPot.Database;


import org.metams.ServletPot.plugins.Logger;

import java.sql.*;
import java.util.Hashtable;



/**
 * User: flake
 * Date: May 30, 2010
 * Time: 8:06:52 PM
 */
public class MySql implements DBAccess
{
    private java.sql.Connection     m_con       = null;
    private Logger                  m_l = null;
    private java.sql.Statement      m_statement = null;

    // variable
    private String                  m_databasePreBlock = "ServletPot.";
    private String                  m_configLocation = "ServletPot.Config";



	/*
		destroy code
	 */
	public void destroy()
	{
		try
		{
			m_con.close();
		}
		catch (Exception e)
		{
		    m_l.log("Error: Unable to destroy connection...", 0);
		}

	}   // destroy


    /**
     * Contructor for the MySQL class
     * @param l
     */
    public MySql(Logger l)
    {
        m_l = l;
    }   // constructor for the MySql class


    /**
      * Contructor for the MySQL class
      */
     public MySql()
     {

     }   // constructor for the MySql class


    /**
     * open the database connection
     * @param userName
     * @param password
     * @param url
     * @return
     */
    public boolean open(String userName, String password, String url)
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            m_con = DriverManager.getConnection(url, userName, password);
            m_statement = m_con.createStatement();

            return true;
        }
        catch (ClassNotFoundException e)
        {
            if (m_l != null) 
				m_l.log("Error: Unable to load database driver...", 0);
			else 
			{
				System.out.println("Mysql.open(): ClassNotFoundException caught");
			}
            return false;
        }
        catch (SQLException e)
        {
			if (m_l != null) m_l.log("Error: Unable to open database connection...", 0);
			else
			{
				System.out.println("Mysql.open(): SQLException caught");
			}
            return false;
        }
        catch (InstantiationException e)
        {
			if (m_l != null)  m_l.log("Error: Unable to instantiate database driver...", 0);
			else
			{
				System.out.println("Mysql.open(): InstantiationException caught");
			}
            return false;
        }
        catch (IllegalAccessException e)
        {
			if (m_l != null) m_l.log("Error: Illegal access to database driver...", 0);
			else
			{
				System.out.println("Mysql.open(): InstantiationException caught");
			}
            return false;
        }
    }    // open


    /**
     * retuns the counter value
     * @param hash
     * @param len
     * @param DB
     * @return
     * @throws SQLException
     */
    private long getCounter(long hash, long len, String DB) throws SQLException
    {
        long        x = 0;
        ResultSet   rSet = null;
        Statement   statement = null;


        try
        {

            // Statements allow to issue SQL queries to the database
            statement = m_con.createStatement();
            String             sqlQuery  = "select counter from " + m_databasePreBlock + DB +" WHERE hash=" + hash + " AND length=" + len+ ";";
            rSet                         = statement.executeQuery(sqlQuery);

            if (rSet.first())
            {
                x = rSet.getLong(1);
            }

            statement.close();
            rSet.close();
            //m_con.close();

        }
        catch (Exception e)
        {
            statement.close();
            rSet.close();
            //m_con.close();
        }

        return x;
    }




    public int getNumberOfGetsPosts(String in) throws SQLException
    {
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

    }


    public String[] getNumberOfGetsPostsHistory(String in, int history)     throws SQLException
    {
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

    public String[] getLastPosts(int history)  throws SQLException
    {
        return getNumberOfGetsPostsHistory("Posts", history);
    }



    public boolean writeIP(String ip)
    {
        PreparedStatement preparedStatement;

         try
         {
 
             // PreparedStatements can use variables and are more efficient
             preparedStatement = m_con.prepareStatement("delete from " +  m_databasePreBlock + "IPs where ip=\"" + ip+ "\";");
             preparedStatement.executeUpdate();

                         
             // PreparedStatements can use variables and are more efficient
             preparedStatement = m_con.prepareStatement("insert into " + m_databasePreBlock +"IPs values (default, ?, ?, 0)");

             java.util.Date x = new java.util.Date();
             String dd = x.toString();

             preparedStatement.setString(1, ip);
             preparedStatement.setString(2, dd);
             preparedStatement.executeUpdate();
         }
         catch (Exception e)
         {
             System.out.println(e.toString());

             return false;
         }

         return true;

    }   // writeIP


    public boolean writeURI(String URI, long hash, long len, long counter, int reqNr)
    {

        PreparedStatement preparedStatement;

        if (URI == null)
                return false;

        // fix bogus URI
        if (URI.startsWith("//"))
            URI = URI.substring(1);

        if (URI.startsWith("/%20%20/"))
            URI = URI.substring(7);

        try
        {

            // if the URI exists, just increase the counter
            if (existsURI(hash, len, reqNr))
            {
                counter = 1 + getCounter(hash, len, "URIs");
                String sqlQuery = "delete from " + m_databasePreBlock + "URIs WHERE hash=" + hash + " AND length=" + len + ";";

                // PreparedStatements can use variables and are more efficient
                preparedStatement = m_con.prepareStatement(sqlQuery);

                        preparedStatement.executeUpdate();

            }


            // PreparedStatements can use variables and are more efficient
            preparedStatement = m_con.prepareStatement("insert into " + m_databasePreBlock + "URIs values (default, ?, ?, ?, ?)");

            // init fields
            preparedStatement.setString(1, URI);
            preparedStatement.setLong(2, hash);
            preparedStatement.setLong(3, len);
            preparedStatement.setLong(4, counter);

            // save data
            preparedStatement.executeUpdate();
            //m_con.close();
        }
        catch (SQLException e)
        {
            System.out.println(e.toString());
            return false;

        }

        return true;

    }   // writeURI




    public int getCounter()
    {
        int x = 0;

         try
         {

             // Statements allow to issue SQL queries to the database
             java.sql.Statement statement = m_con.createStatement();
             String             sqlQuery  = "select counter from " + m_configLocation + ";";
             ResultSet          rSet      = statement.executeQuery(sqlQuery);

             rSet.last();
             x = rSet.getRow();

             rSet.close();

         }
         catch (Exception e)
         {
             e.printStackTrace();
         }

         return x;

    }   // getCounter

    public boolean increaseCounter()
    {

        PreparedStatement preparedStatement;

        int counter = getCounter() + 1;

        try
        {
                String sqlQuery = "delete from " + m_configLocation + " WHERE counter="  + counter + ";";


                // PreparedStatements can use variables and are more efficient
                preparedStatement = m_con.prepareStatement(sqlQuery);

                preparedStatement.executeUpdate();


            // PreparedStatements can use variables and are more efficient
            preparedStatement = m_con.prepareStatement("insert into " + m_configLocation + " values (0, ?)");


             preparedStatement.setLong(1, counter);
             preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());

            return false;
        }

        return true;
    }


    /*
        write code for HTTP Post
     */
    public boolean writePost(String URI, String data, long hash, long len, long counter, String ip, String found)  throws SQLException
    {

        PreparedStatement preparedStatement = null;

        try
        {

            // PreparedStatements can use variables and are more efficient
            preparedStatement = m_con.prepareStatement("insert into " + m_databasePreBlock + "Posts values (default, ?, ?, ?, ?, ?, ?, ?)");

            // init fields
            preparedStatement.setString(1, URI);
            preparedStatement.setString(2, data);
            preparedStatement.setLong(3, hash);
            preparedStatement.setLong(4, len);
            preparedStatement.setLong(5, counter);

            preparedStatement.setString(7, ip);
            preparedStatement.setString(6, found);

            // save data
            preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());

            return false;
        }
        finally
        {
            if (preparedStatement != null) preparedStatement.close();
        }

        return true;

    }   // writePost


        public boolean writeGet(String URI, long hash, long len, String ip, String found)
    {

        PreparedStatement preparedStatement;

        try
        {

            // PreparedStatements can use variables and are more efficient
            preparedStatement = m_con.prepareStatement("insert into " + m_databasePreBlock + "Gets values (default, ?, ?, ?, ?, ?)");

            // init fields
            preparedStatement.setString(1, URI);
            preparedStatement.setLong(2, hash);
            preparedStatement.setLong(3, len);
            preparedStatement.setString(4, found);
            preparedStatement.setString(5, ip);

            // save data
            preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());

            return false;
        }

        return true;

    }   // writeGet


    public void deleteURI(String line)
    {
        try
        {
            String sqlQuery = "delete from " + m_databasePreBlock + "URIs WHERE URI=?;";


            // PreparedStatements can use variables and are more efficient
            PreparedStatement preparedStatement = m_con.prepareStatement(sqlQuery);
            preparedStatement.setString(1, line);

            preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {

        }

        // return true;
    }


    public boolean writeFile(long len, long crc32, long counter, int reqNr)
    {
        PreparedStatement preparedStatement;

        if (len == 0)
            return true;

        try
        {
            // check if the file is originally existing
            if (existsFile(len, crc32, reqNr))
            {

                counter = 1 + getCounter(crc32, len, "Files");
                String sqlQuery = "delete from " + m_databasePreBlock + "Files WHERE hash=" + crc32 + " AND length=" + len + ";";


                // PreparedStatements can use variables and are more efficient
                preparedStatement = m_con.prepareStatement(sqlQuery);

                preparedStatement.executeUpdate();


               // return true;
            }


            // PreparedStatements can use variables and are more efficient
            preparedStatement = m_con.prepareStatement("insert into " + m_databasePreBlock + "Files values (default, ?, ?, ?, ?)");

            java.util.Date x = new java.util.Date();

            String d = x.toString();


            preparedStatement.setLong(1, len);
            preparedStatement.setLong(2, crc32);
            preparedStatement.setString(3, d);
            preparedStatement.setLong(4, counter);
            preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());

            return false;
        }

        return true;
    }


    /*
     *   returns true, if URI exists
     */
    public boolean existsURI(long hash, long len, int reqNr)
    {

        boolean returnValue = false;

        try
        {

            // Statements allow to issue SQL queries to the database
            java.sql.Statement statement = m_con.createStatement();
            String             sqlQuery  = "select id  from " + m_databasePreBlock + "URIs WHERE hash=" + hash + " AND length=" + len+ ";";
            ResultSet          rSet      = statement.executeQuery(sqlQuery);

            if (rSet.first())
                returnValue =  true;

            rSet.close();

        }
        catch (Exception e)
        {
            if (m_l != null) m_l.log("ServletPot.existsURI: Error at database handling....".concat(e.toString()), reqNr);

        }


        if (m_l != null && returnValue) m_l.log("ServletPot.existsURI: URI (" + hash + ", "+ len +") here", reqNr);

        return returnValue;
    }




    /*
     *   returns true, if file exists
     */
    public boolean existsFile(long len, long crc32, int reqNr)
    {

        if (len == 0)
            return true;

        boolean returnValue = false;


        try
        {

            // Statements allow to issue SQL queries to the database
            java.sql.Statement statement = m_con.createStatement();
            String             sqlQuery  = "select id from Files WHERE length=\"" + len + "\" AND hash=\"" + crc32 + "\";";
            ResultSet          rSet      = statement.executeQuery(sqlQuery);

            if (rSet.first())
            {
                if (m_l != null) m_l.log("ServletPot.existsFile: File (" + crc32 + ", "+ len +") here", reqNr);
                //System.out.println(rSet.getString(1));

                returnValue = true;
            }
        }
        catch (Exception e)
        {
            if (m_l != null) m_l.log("ServletPot.existsFile: Error at database handling....".concat(e.toString()), reqNr);
        }

        return returnValue;
    }   // existsFile


    /*
        returns the given URI
     */

    public String[] getURI()
    {

        Hashtable strings = new Hashtable();
        int counter = 0;
        String[] output = null;

        try
        {

            // Statements allow to issue SQL queries to the database
            //java.sql.Statement statement = m_con.createStatement();
            String sqlQuery = "select uri from " + m_databasePreBlock + "URIs;";
            ResultSet rSet = m_statement.executeQuery(sqlQuery);


            while (rSet.next())
            {

                String x = rSet.getString(1);
                if (x!= null && x.startsWith("//"))
                    x = x.substring(1);
                

                strings.put(counter++, x);
                // System.out.println(rSet.getString(1));
            }

            rSet.close();
        }
        catch (Exception e)
        {
            System.out.println("existsFile: Exception");
        }

        if (counter != 0)
        {
            output = new String[counter];
            for (int runner = 0; runner <= counter - 1; runner++)
            {
                output[runner] = (String) strings.get(runner);
            }

         }
        else
        {
            output = new String[1];
            output[0]="BANGLE";
        }

        return output;
    }   // getURI

}




