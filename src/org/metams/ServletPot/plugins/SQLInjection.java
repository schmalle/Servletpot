package org.metams.ServletPot.plugins;

import org.metams.ServletPot.ConfigHandler;
import org.metams.ServletPot.Database.DBAccess;

/**
 * User: flake
 * Date: Aug 15, 2010
 * Time: 3:58:36 PM
 */
public class SQLInjection
{

    private Logger          m_l = null;
    private ConfigHandler   m_cf = null;
    private DBAccess        m_db = null;
    private String          m_sqlFile = null;

    /*
        constructor for SQLInjection class
        @in: Logger, ConfigHandler, DBAcess, SQLString
     */
    public SQLInjection(Logger l, ConfigHandler cf, DBAccess a, String sqlFile)
    {
        m_l = l;
        m_cf = cf;
        m_db = a;
        m_sqlFile = sqlFile;
    }   // SQLInjection constructor


    /*

     */
    public void init()
    {
        m_l.log("test",0 );
    }



    // TODO add retrieve SQL strings code
    // TODO add check for SQL strings

}   // SQLInjection class
