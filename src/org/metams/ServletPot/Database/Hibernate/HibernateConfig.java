package org.metams.ServletPot.Database.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
	@SequenceGenerator(name = "id", initialValue = 1, allocationSize = 1)
	@Table(name = "Config")
	public class HibernateConfig implements Serializable
	{


		private static final long serialVersionUID = 2685108132819989148L;


		long counter;
		int id;


		// Default-Konstruktor:
		public HibernateConfig()
		{

		}

		/* Getter */
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Column(name = "id", nullable = false)
		public int getid()
		{
			return id;
		}



		@Column(name = "counter", nullable = false)
		public long getcounter()
		{
			return counter;
		}


		/* Setter */
		public void setid(int intUserID)
		{
			this.id = intUserID;
		}

		public void setcounter(long strFirstName)
		{
			this.counter = strFirstName;
		}


	}