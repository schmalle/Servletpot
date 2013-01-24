


exec { "Kippo-Checkout":
    path => "/bin:/usr/bin",
    command => "svn checkout http://kippo.googlecode.com/svn/trunk/ /home/honeypot/tools/kippo-read-only",
}



exec { "ServletPot-Checkout":
    path => "/bin:/usr/bin",
    command => "git clone https://github.com/schmalle/Servletpot.git /home/honeypot/tools/Servletpot",
}


Exec["Kippo-Checkout"] -> Exec["ServletPot-Checkout"]
