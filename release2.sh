#!/bin/bash

mvn clean

mvn release:clean release:prepare release:perform

cd target

name=`ls |grep fastcatsearch`

tar czvf "$name".tar.gz "$name"/

cd ..