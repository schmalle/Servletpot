package org.metams.ServletPot.plugins;


import org.metams.ServletPot.ConfigHandler;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Properties;


public class Virustotal
{

    private Logger  m_l = null;
    private String  m_userName = null;
    private String  m_password = null;
    private String  m_popServer = null;
    private String  m_smtpServer = null;


    /*
        constructor for the VirusTotal class
     */
    public Virustotal(Logger l, ConfigHandler cf)
    {
        if (cf.getUseVirusTotal())
        {
            m_l = l;
            m_userName = cf.getSmtpUser();
            m_password = cf.getSmtpPass();
            m_popServer = cf.getPopServer();
            m_smtpServer = cf.getSmtpServer();
        }
    }


    private class SMTPAuthenticator extends javax.mail.Authenticator
    {
        public PasswordAuthentication getPasswordAuthentication()
        {

            return new PasswordAuthentication(m_userName, m_password);
        }
    }


    private class POPAuthenticator extends javax.mail.Authenticator  
    {
        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(m_userName, m_password);
        }
    }


    public void send(String from, String fileName, int reqNr)
    {
        // validity check
        if (m_userName == null)
            return;


        System.out.println("Info: Sending file " + fileName);

        // Recipient's email ID needs to be mentioned.
        String to = "scan@virustotal.com";


        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", m_smtpServer);
        properties.put("mail.smtp.auth", "true");


        Authenticator auth = new SMTPAuthenticator();

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties, auth);

        try

        {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("SCAN");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Fill the message
            messageBodyPart.setText("This is message body");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(fileName);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

            // EMail the complete message parts
            message.setContent(multipart);

            // EMail message
            Transport.send(message);
            m_l.log("ServletPot.Virustotal: Sent file("+fileName+") successfully....", reqNr);
        }

        catch (
                Exception mex
                )

        {
            mex.printStackTrace();
        }
    }


    /**
     * "receive" method to fetch messages and process them.
     */
    public void receive()
    {

        Store store = null;
        Folder folder = null;
        Hashtable scanner = new Hashtable();
        Hashtable malware = new Hashtable();

        // validity check
        if (m_userName == null)
            return;


        try
        {


            Authenticator auth = new POPAuthenticator();

            // -- Get hold of the default session --
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, auth);

            // -- Get hold of a POP3 message store, and connect to it --
            store = session.getStore("pop3");
            store.connect(m_popServer, m_userName, m_password);

            // -- Try to get hold of the default folder --
            folder = store.getDefaultFolder();
            if (folder == null) throw new Exception("No default folder");

            // -- ...and its INBOX --
            folder = folder.getFolder("INBOX");
            if (folder == null) throw new Exception("No POP3 INBOX");

            // -- Open the folder for read only --
            folder.open(Folder.READ_WRITE);

            // -- Get the message wrappers and process them --
            Message[] msgs = folder.getMessages();
            for (int msgNum = 0; msgNum < msgs.length; msgNum++)
            {

                String sampleName = "";
                String date = "";


                if (msgs[msgNum].getSubject().contains("[VirusTotal] Server notification"))
                {

                  // -- Get the message part (i.e. the message itself) --
                  Part messagePart=msgs[msgNum];
                  Object content=messagePart.getContent();

                  // -- or its first body part if it is a multipart message --
                  if (content instanceof Multipart)
                  {
                        messagePart=((Multipart)content).getBodyPart(0);
                      System.out.println("[ Multipart Message ]");
                  }

                  // -- Get the content type --
                  String contentType=messagePart.getContentType();

                  // -- If the content is plain text, we can print it --
                  System.out.println("CONTENT:"+contentType);

                  if (contentType.startsWith("text/plain") || contentType.startsWith("text/html"))
                  {
                        handleLine(messagePart, scanner, malware);
                  }

     //               msgs[msgNum].setFlag(Flags.Flag.DELETED, true);

                }

                //     printMessage(msgs[msgNum]);
            }


            // Informationen liegen jetzt vor:
            // Welcher Scanner wiviel gefunden hat
            // Anzahl Files
            // Welche Malware wie oft gefunden wurde

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            // -- Close down nicely --
            try
            {
                if (folder != null) folder.close(false);
                if (store != null) store.close();
            }
            catch (Exception ex2)
            {
                ex2.printStackTrace();
            }
        }
    }


    /*
        scans the
     */
    private void handleLine(Part messagePart, Hashtable scanner, Hashtable malware) throws IOException, MessagingException {
        // validity check
        if (m_userName == null)
            return;


        InputStream is = messagePart.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String thisLine = reader.readLine();


        try {

            String name = "";
            String sha1 = "";
            String length = "";

            while (thisLine != null) {
                System.out.println(thisLine);

                if (thisLine.startsWith("* name")) {
                    name = thisLine.substring(10);
                } else if (thisLine.startsWith("* sha1")) {
                    sha1 = thisLine.substring(10);
                } else if (thisLine.startsWith("* size")) {
                    length = thisLine.substring(10);
                } else if (thisLine.contains("found nothing")) {
                    //counterErkennung++;
                } else if (thisLine.contains("found [")) {
                    int malIndex = thisLine.indexOf("[") + 1;
                    int endIndex = thisLine.indexOf("]", malIndex);
                    String malName = thisLine.substring(malIndex, endIndex);

                    Integer counterInteger = (Integer) malware.get(malName);
                    int counter = counterInteger.intValue();
                    counter++;
                    malware.put(malName, counter);


                    endIndex = thisLine.indexOf(" ");
                    String scannerString = thisLine.substring(0, endIndex);
                    counterInteger = (Integer) scanner.get(scannerString);
                    counter = counterInteger.intValue();
                    counter++;
                    scanner.put(scannerString, counter);


                    //counterNoErkennung++;
                }


                thisLine = reader.readLine();
            }

        } catch (Exception e) {
            reader.close();
        }

        reader.close();
    }   // handleLine


}