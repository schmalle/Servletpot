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
	@Table(name = "Files")
	public class HibernateFile implements Serializable
	{


		private static final long serialVersionUID = 2675108134819989148L;


		String found;
		int id;
		long hash;
		long counter;
		long length;

		// Default-Konstruktor:
		public HibernateFile()
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

		@Column(name = "hash", nullable = false)
		public long gethash()
		{
			return hash;
		}

		@Column(name = "found", nullable = false)
		public String getfound()
		{
			return found;
		}


		@Column(name = "length", nullable = false)
		public long getlength()
		{
			return length;
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

		public void sethash(long strFirstName)
		{
			this.hash = strFirstName;
		}

		public void setcounter(long strFirstName)
		{
			this.counter = strFirstName;
		}

		public void setlength(long strFirstName)
		{
			this.length = strFirstName;
		}

		public void setfound(String strFirstName)
		{
			this.found = strFirstName;
		}

	}