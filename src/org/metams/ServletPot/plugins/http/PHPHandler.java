package org.metams.ServletPot.plugins.http;

import org.metams.ServletPot.plugins.HandleAttackFiles;
import org.metams.ServletPot.plugins.Logger;
import org.metams.ServletPot.tools.Utils;

import java.util.Stack;
import java.util.zip.CRC32;


/**
 * User: flake
 * Date: Aug 8, 2010
 * Time: 9:29:55 AM
 */
public class PHPHandler
{

	private CRC32               m_crc = new CRC32();
    private Utils               m_u = null;
    private Logger              m_l = null;
    private HandleAttackFiles   m_fileHandler = null;

    private String[]    m_commands =  {"echo(", "die(", "base64_decode(", "include(", "php_uname("};
    private int[]       m_arguments = {1,       0,     1,               1,         0};
    private Stack       m_stack     = new Stack();


	private String[] m_requests =
			{
									"[php]echo('Origins'.php_uname().'scanner');die();[/php]",
                                    "[php]echo('casper'.php_uname().'kae');die();[/php]",

			};

	private int[]   m_crcs     =
			{
									0x93ccae63,
                                    0x9EA890B6
			};

	private String[] m_answers =
			{
									"Originsjoedoescanner",
                                    "Originsjoedoescanner"
			};

    /*
        constructor for the PHPHandler class
        @in u   Utils class
        @in l   Logger class
        @in a   FIlehandler class
     */
    public PHPHandler(Utils u, Logger l, HandleAttackFiles a)
    {
        m_u = u;
        m_l = l;
        m_fileHandler = a;

    }   // PHP handler constructor


	/**
	 *
	 * @param data
	 * @return
	 */
	public String getCRC32(String data)
	{

		m_crc.reset();
		m_crc.update(data.toLowerCase().getBytes());
		long dataCRCLong = m_crc.getValue();
        int dataCRC = (int) dataCRCLong;

		int len = m_crcs.length - 1;
		for (int runner = 0; runner <= len; runner++)
		{

			long crcFromTable = (m_crcs[runner]);

			if (crcFromTable == dataCRC)
			{
				return m_answers[runner];
			}
		}

		return null;

	}


	/**
	 * parse the php string
	 *
	 * @param data
	 * @param reqNr
	 * @return
	 */
	public String parse(String data, int reqNr)
	{

		String answer = null;

		try
		{

			int runner = 0;
			if (data == null)
			{
				return null;
			}

            // calculate CRC32 value before sanitization
            answer = getCRC32(data);

            if (data.startsWith("[php]"))
                data = data.substring(5);

            if (data.endsWith("[/php]"))
                data = data.substring(0, data.length() - 6);


			// check possible answer based on checksum
			if (answer != null)
				return answer;

			// calculate final length
			int endLen = data.length();

			// iterate through the available commands
			while (runner <= endLen)
			{
				int numberOfArguments = getPHPCommand(data, runner);

				// if no command can be extracted, clean up
				if (numberOfArguments == -1)
				{
					m_stack.clear();
					return null;
				}

				// push command on stack
				m_stack.push(Integer.valueOf(numberOfArguments));

				// add number of parameters
				m_stack.push(m_arguments[numberOfArguments]);

				runner = handleArguments(data, runner, endLen, numberOfArguments);

			}


			//       [php]echo('casper'.php_uname().'kae');die();[/php]


		}   // try block
		catch (Exception e)
		{
			return null;
		}


		// Todo Add here data after code execugion!
		return null;

	}   // parse


	/**
	 * handles all incoming arguments
	 *
	 * @param data
	 * @param runner
	 * @param endLen
	 * @param d
	 * @return
	 */
	private int handleArguments(String data, int runner, int endLen, int d)
	{
		// jump to the function
		runner += m_commands[d].length() + 1;
		if (m_arguments[d] == 0)
		{
			if (data.charAt(runner) == ')')
			{
				runner++;
			}


			return runner;

		}


		// echo('casper'.php_uname().'kae');die();


		// now more than 0 parameters are found here
		for (int argRunner = 0; argRunner <= m_arguments[d] - 1; argRunner++)
		{

			String currentString = data.substring(runner);
			String finalArg = "";
			String currentChar = data.substring(runner, runner + 1);




			while (!currentChar.equals(")"))
			{

				currentString = data.substring(runner);
				currentChar = data.substring(runner, runner + 1);

				if (currentChar.equals("'") || currentChar.equals("\""))
				{

					String arg = fetchString(data, runner + 1);
					runner += arg.length() + 1;

					finalArg = finalArg.concat(arg);
				}   //                 if (data.charAt(runner) == '\'' || data.charAt(runner) == '\"')

				//
				runner++;
			}   // while (data.charAt(runner) != ')')

		}


		return runner;

	}   // handleArguments


    /*
        fetches a string given from a given offset
        @in:    data    - string to b parsed
        @out:   String

     */
    private String fetchString(String data, int runner)
    {

        String out = "";

        while (!data.substring(runner, runner+1).equals("\'") && !data.substring(runner, runner+1).equals("\""))
        {


            out = out.concat(data.substring(runner, runner + 1));
            runner++;
        }

        return out;
    }   // fetString


    /**
     * retrieve the php command
     * @param data
     * @param offset
     * @return
     */
    private int getPHPCommand(String data, int offset)
    {

        for (int runner = 0; runner <= m_commands.length - 1; runner++)
        {
	        int commandLen = m_commands[runner].length() + offset;

            // if data is too long, switch this run
            if (commandLen>=  data.length())
                continue;


	        String firstString = data.substring(offset, commandLen).toLowerCase();

            if (firstString.equals(m_commands[runner]))
            {
                return runner;
            }


        }


        return -1;
    }


    /**
     * retrievs the download data
     * @param in
     * @param command
     * @return
     */
    private String getData(String in, String command)
    {
        if (in == null)
            return null;

        if (command == null)
            return null;

        int index = in.indexOf(command.concat("http://"));
        if (index == -1)
            return null;

        // get start value
        index += command.length();

        // correct start value
        in = in.substring(index);

        int sem = in.indexOf(";");
        int blank = in.indexOf(" ");

        // if not found, quit...
        if (sem == -1 && blank == -1)
            return null;

        if (sem == -1)
            sem = 0;

        if (blank == -1)
            blank = 0;

        if (sem <= blank)
            index = sem;
        else
            index = blank;

        return in.substring(0, index);


    }   // getData


    /*
        check for download code and eventually download the code
        @in: data   -   string to be parsed
        @in: reqNr  -   number of Request
        @out: -
     */
    public void checkForDownload(String data, int reqNr)
    {

        data = data.toLowerCase();
        String wget = getData(data, "wget ");
        String fetch = getData(data, "fetch");
        String lwp = getData(data, "lwp-download ");
        String curl = getData(data, "curl -o ");

        if (m_fileHandler != null)
        {
            m_fileHandler.download(wget, reqNr);
            m_fileHandler.download(fetch, reqNr);
            m_fileHandler.download(lwp, reqNr);
            m_fileHandler.download(curl, reqNr);
        }
    }



}   // PHPHandler





