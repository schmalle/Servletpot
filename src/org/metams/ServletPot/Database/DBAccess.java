package org.metams.ServletPot.Database;

import java.sql.SQLException;
import java.util.List;

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

    public int getCounter();

    public boolean increaseCounter();

    public List getURI();

    public void deleteURI(String line);

	public void destroy();

  //  public boolean increaseCounter(String URI);


}


