#!/bin/sh

MD=`dirname $0`

install -g root -o root -m 644 -D ${MD}/../../build/lib/logtrawler-*.jar /usr/lib/logtrawler/logtrawler.jar

# Note: Even if no config.json present, we remove an oldest one (As it may be uncompatible)
if [ -f /etc/logtrawler/config.json ]; then
	mv /etc/logtrawler/config.json /etc/logtrawler/config.json.bck
fi
if [ -f ${MD}/../packages/config.json ]; then
	install -g root -o root -m 644 -D ${MD}/../packages/config.json /etc/logtrawler/config.json
fi

if [ -f ${MD}/../packages/setenv.sh ]; then
	if [ -f /etc/logtrawler/setenv.sh ]; then
	 	mv /etc/logtrawler/setenv.sh /etc/logtrawler/setenv.sh.bck
	 fi
	install -g root -o root -m 644 -D ${MD}/../packages/setenv.sh /etc/logtrawler/setenv.sh
fi


install -g root -o root -m 755  ${MD}/../packages/init.sh /etc/init.d/logtrawler


if [ -x /sbin/chkconfig ]; then
  /sbin/chkconfig --add logtrawler
elif [ -x /usr/lib/lsb/install_initd ]; then
  /usr/lib/lsb/install_initd /etc/init.d/logtrawler
else
   for i in 2 3 4 5; do
        ln -sf /etc/init.d/logtrawler /etc/rc.d/rc${i}.d/S11logtrawler
   done
   for i in 0 1 6; do
        ln -sf /etc/init.d/logtrawler /etc/rc.d/rc${i}.d/K88logtrawler
   done
fi
