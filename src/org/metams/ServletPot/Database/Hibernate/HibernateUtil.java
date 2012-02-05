package org.metams.ServletPot.Database.Hibernate;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;


/* code taken from http://www.bennyn.de/programmierung/java/hibernate-tutorial-in-kurzform.html tutorial */

public class HibernateUtil
{
    private static SessionFactory sessionFactory;

    static
    {
        try
        {
			sessionFactory = new Configuration().configure().buildSessionFactory();
        }
        catch (RuntimeException ex)
        {
            ex.printStackTrace();
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
}