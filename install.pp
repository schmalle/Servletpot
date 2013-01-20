#
# install all dependencies for Servletpot
# tested on ubuntu
#

package { 'openjdk-7-jdk' :
	  ensure => present,
}


package { 'mysql-server' :
	  ensure => present,
}


package { 'tomcat7' :
	  ensure => present,
}


package { 'mono-complete' :
	  ensure => present,
}

package { 'git' :
		ensure => present,
}


package { 'python-twisted' :
        ensure => present
}

package { 'nano' :
        ensure => present
}

package { 'joe' :
        ensure => present
}

package { 'authbind' :
        ensure => present
}


package { 'subversion' :
        ensure => present
}



user { 'honeypot':
  	ensure  => 'present',
  	comment => 'honeypot,,,',
  	gid     => '1000',
  	groups  => ['adm', 'cdrom', 'dip', 'plugdev'],
  	home    => '/home/honeypot',
  	shell   => '/bin/bash',
  	password=> '$6$eLenMc6J$zKuRavqGtaRgr3uFOcNWS2WmV.rM9MVGhTqB7iU.lE0YbS1jkh1cBHsQ6xU6uuvFyT8RZ0YF.tBj7FSTtWTC4.',
  	uid     => '1001',
}




$password = "very_hard_pw"

  exec { "Set MySQL server root password":
#    subscribe => [ Package["MySQL-server"], Package["MySQL-client"], Package["MySQL-shared"] ],
    refreshonly => true,
    unless => "mysqladmin -uroot -p$password status",
    path => "/bin:/usr/bin",
    command => "mysqladmin -uroot password $password",
  }


# svn checkout http://kippo.googlecode.com/svn/trunk/ kippo-read-only


Package['openjdk-7-jdk'] -> Package['mysql-server'] -> Exec['Set MySQL server root password'] -> User['honeypot']
Package['tomcat7'] -> Package['mono-complete'] -> Package['git'] -> Package['authbind'] -> Package['subversion']
Package['python-twisted'] -> Package['nano'] -> Package['joe']



