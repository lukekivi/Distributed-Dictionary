ssh -f kivix019@csel-kh4250-19.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant superNode"
sleep 2
ssh -f kivix019@csel-kh4250-20.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant node"
sleep 2
ssh -f kivix019@csel-kh4250-21.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant node"
sleep 2
ssh -f kivix019@csel-kh4250-22.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant node"
sleep 2
ssh -f kivix019@csel-kh4250-23.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant node"
sleep 2
ssh -f kivix019@csel-kh4250-24.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant node"
sleep 2
ssh -f kivix019@csel-kh4250-25.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant node"
sleep 2
ssh -f kivix019@csel-kh4250-26.cselabs.umn.edu "cd ../../project/kivix019/Distributed-Dictionary/app; ant client"
