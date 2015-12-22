# Fastcatsearch [![Build Status](https://travis-ci.org/fastcat-co/fastcatsearch.png)](https://travis-ci.org/fastcat-co/fastcatsearch)

Fastcatsearch is an open-source distributed search engine. 

## Community

- Korean: Use Fastcatsearch Korea group - https://www.facebook.com/groups/fastcatsearch/
- English: Use google groups - https://groups.google.com/d/forum/fastcatsearch

## Feature List

### System 

- Distributed index and search
- Support search broker - merge sub-search results
- Manage config files as XML
- RESTFul search API
- Search result provides JSON, XML format
- Running on Java JVM
- Support QPS maximum 200 queries / seconds
- Index data can be restored to previous time

### Search

- Boolean search, natual languae search
- Support synonym, stopword
- Search result summary, and highlight.
- Multiple filed searching
- Multiple field sorting
- Multiple field filtering
- Multiple field grouping
- Support various dbms data importing
- Support custom file data importing
- Support MS Office, PDF document indexing
- Indexing schduling
- Full indexing, increment indexing


## License

The license is Apache License Version 2.0.

It's free and you can use it however you want it wharever way.

For more information, see <http://www.apache.org/licenses/LICENSE-2.0.txt>



## Building

1. Download source from github repository

2. Maven it


    $ cd fastcatsearch-{version}   
    $ maven install

3. Done

    Copy built directory to some where you want to install.
    Now, it's ready to run.
    


## Settings

- Port Check
    
    Default port is `8090`. If this port is used, you can change it in config file.
    
    In `conf/id.properties` file,  find line `servicePort=8090` and change port number you want.

- Memory size

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

See <https://github.com/fastcatsearch/fastcatsearch-console>

You can make collections on fastcatsearch, edit configs, and index documents using fastcatsearch-console.


## Need Help?

- Resources : You can find manuals and tutorials on the <http://fastcatsearch.org> site.

- Support : Google groups - <https://groups.google.com/d/forum/fastcatsearch>. Join and get free support.
