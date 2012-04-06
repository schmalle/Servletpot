package org.metams.ServletPot.test;


import org.metams.ServletPot.Database.Hibernate.MySqlHibernate;

public class HibernateTest
{
    public static void main(String[] args)
    {

		MySqlHibernate mysql = new MySqlHibernate();
		mysql.getURI();
        mysql.writeURI("hallo",100,100,100, 100);
		mysql.writeFile(1024, 2048, 1, 1);
		mysql.writeURI("hallo2",100,100,100, 100);
	    mysql.writeFile(1025, 2048, 1, 1);




		mysql.increaseCounter();

    }
}