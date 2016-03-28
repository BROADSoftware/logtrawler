#!/bin/sh


/etc/init.d/logtrawler stop  > /dev/null 2>&1
if [ -x /sbin/chkconfig ]; then
   /sbin/chkconfig --del logtrawler
elif [ -x /usr/lib/lsb/remove_initd ]; then
    /usr/lib/lsb/install_initd /etc/init.d/logtrawler
else
    rm -f /etc/rc.d/rc?.d/???logtrawler
fi

rm -f /etc/init.d/logtrawler

rm -rf /usr/lib/logtrawler

