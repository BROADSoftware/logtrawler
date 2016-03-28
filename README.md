# logtrawler

Logtrawler is an application aimed to collect logs files content with a radically different approach from currently existing system. 

The main idea behind Logtrawler is 'store-it as-it first, and think about after'. Also called 'Schema on Read', this is the approach driven BigData and DataLake movement.

In practice, Logtrawler is an agent to install on each monitored system. With almost no configuration, it is able to automatically detect what 'look like' a log file, monitor it, and send all new lines to a server.

This server can be of any type, provided it can store JSON message. Typically, ElasticSearch is a good candidate.


## License

    Copyright (C) 2015 BROADSoftware

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	
## Usage
### Requirement

Logtrawler is a java program. As such, it requires Java to be installed. And java version must be at least 1.7
There is no other requirement, as all dependencies are embedded within the provided jar.

### Installation

#### RedHat, CentOS and alike

Logtrawler is provided as an RPM. 

To install it:

    sudo rpm -i logtrawler-X.Y.Z.noarch.rpm
    
Logtrawler is now installed as a service, and will be started automatically on next reboot.

Of course, you can launch it right now, by issuing:

    sudo service logtrawler start
    
  or

    sudo systemctl start logtrawler

But, this will fail until a minimum configuration is performed.

#### Ubuntu, Debian and alike

Sorry, no .deb packaging yet (Contribution welcome)

### Configuration

All configuration parameters are defined in a Shell variable OPTS, set by the file:

    /etc/logtrawler/setenv.sh
    
At least, two options should be set to define:

- At least one folder of logs files to monitor, using '-f' option. For example:

    OPTS="$OPTS -f /var/log"
    
- Where to send all collected logs, using '-f' option. For example:

    OPTS="$OPTS -o http://a.server.com:9200/log1/logs"

You can refer to the comments in the setenv.sh file.

Of course, modification of setenv.sh will only be effective after a service restart:

    sudo service logtrawler restart
    
or

    sudo systemctl restart logtrawler
    
As you can guess, the $OPTS variable will be passed on the command line of the logtrawler program by the service launching script.

You will find more information about configuration in doc/manual.doc
	
## Build

Logtrawler use Gradle as build system, So:

    ./gradlew rpm

Should build the rpm distribution, in build/distribution 

