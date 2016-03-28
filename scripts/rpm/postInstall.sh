if [ -x /sbin/chkconfig ]; then
	mv /opt/logtrawler/init.sh /etc/init.d/logtrawler
	/sbin/chkconfig --add logtrawler
elif [ -x /usr/lib/lsb/install_initd ]; then
	mv /opt/logtrawler/init.sh /etc/init.d/logtrawler
	/usr/lib/lsb/install_initd /etc/init.d/logtrawler
else
	mv /opt/logtrawler/init.sh /etc/init.d/logtrawler
	for i in 2 3 4 5; do
		ln -sf /etc/init.d/logtrawler /etc/rc.d/rc${i}.d/S11logtrawler
	done
	for i in 0 1 6; do
    	ln -sf /etc/init.d/logtrawler /etc/rc.d/rc${i}.d/K88logtrawler
	done
fi

if [ -x /bin/systemctl ]; then
	systemctl daemon-reload
fi

