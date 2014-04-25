#!/bin/bash

source env.sh

source _confirm.sh

let i=1
for ip in ${server_ip_list[@]}
do
    echo "########################################"
    echo "# [$(( i++ ))] Sync library $ip"
    echo "########################################"
    rsync -avz  -e "ssh -p $ssh_port" --existing --delete $this_home/lib/ $ssh_user@$ip:$target_home/lib
    echo ""
done