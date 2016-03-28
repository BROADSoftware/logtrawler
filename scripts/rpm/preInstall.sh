if [ -f /etc/collector/config.json ]; then
	mv /etc/collector/config.json /etc/collector/config.json.bck
fi
if [ -f /etc/collector/setenv.sh ]; then
 	mv /etc/collector/setenv.sh /etc/collector/setenv.sh.bck
fi
