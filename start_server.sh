#!/bin/bash
rm -rf /usr/local/server/nginx/html/*
mv /usr/local/server/nginx/rsfiles.zip /usr/local/server/nginx/html
cd /usr/local/server/nginx/html && unzip -o rsfiles.zip
sed -i 's/localhost:8080/11.11.11.11:18181/g' /usr/local/server/nginx/html/index.html
rm -rf /usr/local/servernginx/html/rsfiles.zip

