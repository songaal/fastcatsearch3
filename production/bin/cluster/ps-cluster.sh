#!/bin/bash

source env.sh

let i=1

echo "########################################"
echo "# [$(( i++ ))] local daemon"
echo "########################################"
ps -ef|grep fastcatsearch|grep Bootstrap
echo ""
    
for ip in ${server_ip_list[@]}
do
    echo "########################################"
    echo "# [$(( i++ ))] daemon $ip"
    echo "########################################"
    ssh -p $ssh_port $ssh_user@$ip ps -ef|grep fastcatsearch|grep Bootstrap
    echo ""
done
