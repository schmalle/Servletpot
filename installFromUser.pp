
exec { "Kippo-Checkout":
    path => "/bin:/usr/bin",
    command => "svn checkout http://kippo.googlecode.com/svn/trunk/ /home/honeypot/tools/kippo-read-only",
}


exec { "ServletPot-Checkout":
    path => "/bin:/usr/bin",
    command => "git clone https://github.com/schmalle/Servletpot.git /home/honeypot/tools/Servletpot",
}

exec { "Install Servletpot-DB":
    path => "/bin:/usr/bin",
    command => "mysql -u root < /home/honeypot/tools/Servletpot/installmysql.sql",
}

exec { "Install Kippo-DB":
    path => "/bin:/usr/bin",
    command => "mysql kippo -u root < /home/honeypot/tools/kippo-read-only/doc/sql/mysql.sql"
}

exec { "Install Kippo-User":
    path => "/bin:/usr/bin",
    command => "mysql kippo -u root < /home/honeypot/tools/initdb.sql"
}

Exec["Kippo-Checkout"] -> Exec["ServletPot-Checkout"] -> Exec['Install Servletpot-DB'] -> Exec['Install Kippo-DB']  -> Exec['Install Kippo-User']

