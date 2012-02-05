package org.metams.ServletPot.test;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.metams.ServletPot.Database.Hibernate.HibernateURI;
import org.metams.ServletPot.Database.Hibernate.HibernateUtil;

public class HibernateTest
{
    public static void main(String[] args)
    {
        HibernateURI newURI = new HibernateURI();
        newURI.setlength(100);
        newURI.sethash(0x41414141);
		newURI.seturi("testme");
		newURI.setcounter(1);
 
        SessionFactory sf       = new HibernateUtil().getSessionFactory();
        Session session         = null;
        Transaction transaction = null;
		
		if (sf == null)
		{
			System.out.println("Error: SessionFactor not created...");
		}
		
 
        // Neuen Benutzer in Datenbank speichern:
        try
        {
            session             = sf.openSession(); // sf.getCurrentSession();
            transaction         = session.beginTransaction();
            session.save(newURI);
            transaction.commit();
        }
        catch (Exception e)
        {
            // rollback(transaction);
            e.printStackTrace();
        }
    }
}