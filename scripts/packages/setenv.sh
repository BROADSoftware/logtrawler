
# Set JAVA_HOME. Must be at least 1.7.
# If not set, will try to lookup a correct version.
# JAVA_HOME=/some/place/where/to/find/java

# Set folder to monitor
# OPTS="$OPTS -f /var/log" 

# Set hostname, in fqdn form
OPTS="$OPTS -h $(hostname -f)"

# bind to a local address able to be reached from anywhere.
# OPTS="$OPTS -e 0.0.0.0:20147" 

# Add some exclusion rules
# OPTS="$OPTS -x \"/var/log/nagios/spool/.*\"" 

# Overwrite the default mime type for log files.
# OPTS="$OPTS -m text/plain -m text/x-log -m application/octet-stream" 

# Allow access to admin interface from some local network
# OPTS="$OPTS -a 192.168.0.0/24"
# OPTS="$OPTS -a 10.0.0.0/8"
# Allow access to admin interface from everywhere
# OPTS="$OPTS -a '*'"

# AT LEAST, ONE -o OPTION SHOULD BE SET
# Send all collected logs to a remote server (Port number may let think its an Elasticsearch one.
# OPTS="$OPTS -o http://a.server.com:9200/log1/logs"

# Send all collected logs to a local file. Use this to test/debug only, as this file may become huge in a short time.
# WARNING: Do not send to a file in the monitored space, or you will ends up in infinite loop !!
# OPTS="$OPTS -o file:///tmp/collector.out"

# Allow batching of output in JSON arrays (Not compatible with Elastic search).
# OPTS="$OPTS -b 128"
