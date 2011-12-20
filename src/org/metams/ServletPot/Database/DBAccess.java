package org.metams.ServletPot.Database;

import java.sql.SQLException;

/**
 * User: flake
 * Date: May 30, 2010
 * Time: 7:53:03 PM
 */
public interface DBAccess
{
    public boolean open(String userName, String password, String url);

    public boolean writeFile(long len, long crc32, long counter, int reqNr);

    public boolean existsFile(long len, long crc32, int reqNr);

    public boolean writeURI(String URI, long hash, long len, long counter, int reqNr);

    public boolean writePost(String URI, String data, long hash, long len, long counter, String ip, String found) throws SQLException;

    public boolean writeGet(String URI, long hash, long len, String ip, String found);

    public boolean writeIP(String ip);

    public int getCounter();

    public boolean increaseCounter();

    public int getNumberOfGets() throws SQLException;
   
    public int getNumberOfPosts() throws SQLException;

    public String[] getLastGets(int history) throws SQLException;

    public String[] getLastPosts(int history) throws SQLException;

    public String[] getURI();

    public void deleteURI(String line);

	public void destroy();

  //  public boolean increaseCounter(String URI);


}


