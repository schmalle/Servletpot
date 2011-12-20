package org.metams.ServletPot.test;

import org.metams.ServletPot.plugins.http.PHPHandler;

/**
 * User: flake
 * Date: Nov 6, 2010
 * Time: 5:47:32 PM
 */
public class PHPTest
{

    public static void main(String[] args)
    {
        PHPHandler p = new PHPHandler(null, null, null);
        p.parse("[php]echo('casper'.php_uname().'kae');die();[/php]", 0);

    }    

}
