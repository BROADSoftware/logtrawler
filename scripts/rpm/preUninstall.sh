if [ $1 = 0 ]; then
	/etc/init.d/logtrawler stop  > /dev/null 2>&1
	if [ -x /sbin/chkconfig ]; then
		/sbin/chkconfig --del logtrawler
	elif [ -x /usr/lib/lsb/remove_initd ]; then
		/usr/lib/lsb/install_initd /etc/init.d/logtrawler
	else
    	rm -f /etc/rc.d/rc?.d/???logtrawler
    fi
    rm -f /etc/init.d/logtrawler
fi
if [ -f /etc/logtrawler/config.json ]; then
	cp /etc/logtrawler/config.json /etc/logtrawler/config.json.bck
fi
if [ -f /etc/logtrawler/setenv.sh ]; then
 	cp /etc/logtrawler/setenv.sh /etc/logtrawler/setenv.sh.bck
fi

if [ -x /bin/systemctl ]; then
	systemctl daemon-reload
fi

