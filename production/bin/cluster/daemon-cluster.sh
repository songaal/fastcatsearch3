#!/bin/bash

source env.sh

if [ -z "$1" ] ; then
	echo "Usage $0 run | start | stop | restart | kill | debug | profile"
	exit 0;
fi

source _confirm.sh

let i=1

echo "########################################"
echo "# [$(( i++ ))] Restart local daemon"
echo "########################################"
sh $this_home/bin/daemon.sh $1 notail
echo ""
    
for ip in ${server_ip_list[@]}
do
    echo "########################################"
    echo "# [$(( i++ ))] Restart daemon $ip"
    echo "########################################"
    ssh -p $ssh_port $ssh_user@$ip sh $target_home/bin/daemon.sh $1 notail
    echo ""
done
