package org.metams.ServletPot.tools;

import org.metams.ServletPot.Database.MySql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * User: flake
 * Date: Jul 3, 2010
 * Time: 1:43:13 PM
 */
public class ImporterMysql
{

    // dummy test codehjh

    public static void main(String[] args)
    {

        try
        {


            String userName = "servletpot";
            String password = "pw100pw200";
            String db       = "jdbc:mysql://localhost/ServletPot";
            String fileName = "/Users/flake/IdeaProjects/ServletPot/web/WEB-INF/strings.txt";

            int     reqNr = 0;

            System.out.println("Starting Servlet DB ImporterMysql");

            // silly command line check
            if (args.length != 4)
            {
                System.out.println("Command line parameters are username password db file_with_vulns");
                return;
            }
            else
            {
                userName = args[0];
                password = args[1];
                db = args[2];
                fileName = args[3];
            }

            Utils util = new Utils(null);
            MySql x = new MySql(null);
            x.open(userName, password, db);

            System.out.println("Info: Importing file ");

            // prepare string file
            File f = new File(fileName);
            BufferedReader i = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

            String line = null;
            long counter = 0;

            while ((line = i.readLine()) != null)
            {
                counter++;
                System.out.println("Inserting line " + line + " in database");

                long len = line.length();
                long crc32 = util.getCRC32(line, false);

                x.writeURI(line, crc32, len, 0, reqNr);

                //    long hash.

            }

            System.out.println("Info: Inserted " + counter + " vectors....");

            x.getURI();


        }
        catch (Exception e)
        {
           e.printStackTrace();
        }


    }


}   // imports file in a database
