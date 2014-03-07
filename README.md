# Fastcatsearch

Fastcatsearch is a tool for managing fastcatsearch. You can make collections on fastcatsearch, edit configs, and index documents.


## License

The license is GNU GPL v2.

It's free and you can use it however you want it wharever way.

You don't need to open your closed source unless you distribute derived works to others.

For more information, see <http://www.gnu.org/licenses/gpl-2.0.html>



## Building

1. Download source from github repository

2. Maven it


    $ cd fastcatsearch-{version}   
    $ maven install

3. Done

    Copy built directory to some where you want to install.
    Now, it's ready to run.
    


## Settings

* Port Check
    
    Default port is `8090`. If this port is used, you can change it in config file.
    
    In `conf/id.properties` file,  find line `servicePort=8090` and change port number you want.

* Memory size

    Default memory size is 512m. To change this value, open `bin/start.sh` and change `HEAP_MEMORY_SIZE=512m` you want.

## Running 

1. Run shell script

    Linux : run script `bin/start.sh` 
    Windows : run script `bin/start.cmd` 

    See log at `logs/system.log`
    
    If you see `CatServer started!` in last of log file, search engine has been loaded.

2. Check server is alive

    Using web browser, access url http://localhost:8090/service/isAlive
    
    If result is `{"status":"ok"}` server started successfully.
   
   
## What's next?

To manage search engine, you need manager tool - fastcatsearch-console.

See <https://github.com/fastcatgroup/fastcatsearch-console>

You can make collections on fastcatsearch, edit configs, and index documents using fastcatsearch-console.


## Need Help?

You can find manuals and tutorials on the <http://fastcatsearch.org> site.
