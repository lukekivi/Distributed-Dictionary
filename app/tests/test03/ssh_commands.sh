ssh -f $DHT_USER_NAME@csel-kh4250-19.cselabs.umn.edu "cd $DHT_APP_PATH; ant superNode"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-20.cselabs.umn.edu "cd $DHT_APP_PATH; ant node"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-21.cselabs.umn.edu "cd $DHT_APP_PATH; ant node"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-22.cselabs.umn.edu "cd $DHT_APP_PATH; ant node"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-23.cselabs.umn.edu "cd $DHT_APP_PATH; ant node"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-24.cselabs.umn.edu "cd $DHT_APP_PATH; ant node"
sleep 2
ssh -f $DHT_USER_NAME@csel-kh4250-26.cselabs.umn.edu "cd $DHT_APP_PATH; ant client"