read -p "Are you sure? " -n 1 -r

if [[ ! $REPLY =~ ^[Yy]$ ]]
then
	exit 1
fi
echo 
