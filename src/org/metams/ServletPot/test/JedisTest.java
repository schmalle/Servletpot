package org.metams.ServletPot.test;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.metams.ServletPot.Database.Redis;
import redis.clients.jedis.Jedis;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: flake
 * Date: 12/10/11
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class JedisTest 
{

    public void run()
    {


        Redis x = new Redis(null);
        x.open(null, null, null);
        x.writeFile(100, 0x42424242, 1,2);
        x.writeURI("index.php", 0x42424242, 100, 1, 2);
        x.writeURI("index.html", 0x42424243, 100, 1, 2);

        List x2 = x.getURI();

    }


    public static void main(String[] args)
	{

        JedisTest test = new JedisTest();
        test.run();

    }


    //public main
}
