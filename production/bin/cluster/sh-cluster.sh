#!/bin/bash

source env.sh

if [ "$#" -eq 0 ] ; then
	echo "Run command at all nodes"
	echo "Usage $0 \"[command]\""
	exit 0;
fi


let i=1

echo "########################################"
echo "# [$(( i++ ))] local daemon"
echo "########################################"
echo ">$@"
"$@"
echo ""
    
for ip in ${server_ip_list[@]}
do
    echo "########################################"
    echo "# [$(( i++ ))] daemon $ip"
    echo "########################################"
    echo ">$@"
    ssh -p $ssh_port $ssh_user@$ip "$@"
    echo ""
done
