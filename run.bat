@echo on
chcp 65001
:: 本地路径
set localPath=/home/show/javaworkspace
:: 服务器ip
set host=1.1.1.1
:: 服务器ssh端口号
set port=22
:: 服务器用户名
set userName=root
:: 服务器密码(如果证书私钥为空时,该值必须.如果证书私钥不为空,根据实际情况填写)
set passWord=123456
:: 证书私钥(如果该值不为空,优先使用证书登录方式)
set privateKey=/home/show/.ss/privk
:: 远端路径(上传文件后保存的路径)
set remotePath=/home/show2
:: 执行的远端脚本路径
set remoteShell=/home/show2/1.sh
:: 任务类型(只能是0,1,2中的某个值,0上传文件并执行脚本,1仅上传文件,2仅执行脚本.默认0,非0-2的数按照0处理)
set taskType=0

java -Dfile.encoding=UTF-8 -jar app.jar
pause