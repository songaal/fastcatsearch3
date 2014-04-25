#!/bin/bash

source env.sh

if [ -z "$1" ] ; then
	echo "Usage $0 [source] [target]"
	echo "	if target is ommited, target is set by source."
	exit 0;
else
	if [[ "$1" == \/* ]] || [[ "$1" == "~*" ]] ; then
		src="$1"
	else
		src="$this_home/$1"
	fi
fi


if [ -z "$2" ] ; then
	dest=$src
else
	if [[ "$2" == \/* ]] || [[ "$1" == "~*" ]] ; then
		dest="$2"
	else
		dest="$target_home/$2"
	fi
fi

echo "copy $src >> $dest"


source _confirm.sh

let i=1
for ip in ${server_ip_list[@]}
do
    echo "########################################"
    echo "# [$(( i++ ))] Sync file $1 at $ip"
    echo "########################################"
    echo "rsync -avz -e \"ssh -p $ssh_port\" --existing --delete $src \"$ssh_user@$ip:$dest\""
    rsync -avz -e "ssh -p $ssh_port" $src "$ssh_user@$ip:$dest"
    echo ""
done
