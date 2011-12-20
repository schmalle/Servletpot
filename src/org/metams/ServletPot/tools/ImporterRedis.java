package org.metams.ServletPot.tools;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.metams.ServletPot.Database.Redis;

/**
 * User: flake
 * Date: Jul 3, 2010
 * Time: 1:43:13 PM
 */
public class ImporterRedis
{

    // dummy test codehjh

    public static void main(String[] args)
    {

        try
        {
			System.out.println("Starting Servlet DB ImporterRedis");

			String fileName = "/Users/flake/IdeaProjects/ServletPot/web/WEB-INF/strings.txt";

			System.out.println("Working with arguments " + args.length);
			if (args.length == 1)
			{
				System.out.println("File containing the strings is " + args[0]);
				fileName = args[0];
			}
			


            int     reqNr = 0;

            Utils util = new Utils(null);
            Redis x = new Redis(null);
            x.open(null,null,null);

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