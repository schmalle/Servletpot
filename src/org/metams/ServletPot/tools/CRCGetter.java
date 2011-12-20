package org.metams.ServletPot.tools;

import org.metams.ServletPot.plugins.http.PHPHandler;

/**
 * Created by IntelliJ IDEA.
 * User: flake
 * Date: 8/15/11
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CRCGetter
{

	public static void main(String[] args)
	{
		org.metams.ServletPot.plugins.http.PHPHandler x = new PHPHandler(null, null, null);
		x.getCRC32("[php]echo('casper'.php_uname().'kae');die();[/php]");
	}


}
