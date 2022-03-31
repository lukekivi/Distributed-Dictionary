ssh -f $DHT_USER_NAME@csel-kh4250-19.cselabs.umn.edu "cd $DHT_APP_PATH; ant superNode"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-20.cselabs.umn.edu "cd $DHT_APP_PATH; ant node"
