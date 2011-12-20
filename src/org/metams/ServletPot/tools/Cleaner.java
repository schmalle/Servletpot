package org.metams.ServletPot.tools;

import org.metams.ServletPot.Database.DBAccess;
import org.metams.ServletPot.Database.MySql;


/**
 * User: flake
 * Date: Aug 12, 2010
 * Time: 7:02:00 PM
 *
 * Cleaner class makes sure, that no duplicate data is stored in the
 * URIs database and that IP adresses are anonymized after a given
 * amount of time
 *
 */
public class Cleaner
{

    private DBAccess m_db = null;
    private String[] m_attackStrings = null;


    public static void main(String[] args)
    {
        String userName = "servletpot";
        String password = "pw100pw200";
        String db       = "jdbc:mysql://127.0.0.1/ServletPot";

        System.out.println("Starting Servlet DB ImporterMysql");
/*
        // silly command line check
        if (args.length != 4)
        {
            System.out.println("Command line parameters are username password db ");
            return;
        }
        else
        {
            userName = args[0];
            password = args[1];
            db = args[2];
        }
  */
        //Utils util = new Utils(null);
        MySql x = new MySql(null);
        x.open(userName, password, db);

        System.out.println("Info: Importing file ");
        
    }


    /*
        constructor for the Cleaner class

     */
    public Cleaner(DBAccess db, int dayCounter, String uname, String pw, String url)
    {

        m_db = new MySql(null);
        m_db.open(uname, pw, url);

    }

    // TODO add cleaner for DB

    // Code: hole zeile 1, checke if nach dem eigentlichen request ein gleicher code kommt


    private void cleanDBURI(int reqNr)
    {

        int counter = 0;
        //Hashtable hash = new Hashtable();

        // ensure that the attack strings will be only
        //
        if (m_attackStrings == null)
        {
                m_attackStrings = m_db.getURI();
        }


        // scan all lines
        for (int runner = 0; runner <= m_attackStrings.length -1; runner++)
        {
            String origLine = m_attackStrings[runner];
            String line = m_attackStrings[runner];
            while (line.startsWith("//"))
            {
                line = line.substring(1);
            }

            int index = line.indexOf("=htt");
            if (index != -1)
                line = line.substring(0, index + 1);

            index = line.indexOf("=%7c");
            if (index != -1)
                line = line.substring(0, index + 1);


            if (!line.equals(origLine))
            {
                m_db.deleteURI(origLine);

                // Todo FIX IT
                //    m_db.writeURI(line, hash, line.length(), 0, reqNr);
            }

        }                     



    }


}
