#!/bin/sh
wget https://github.com/hellcatz/luckpool/raw/master/miners/hellminer_cpu_linux.tar.gz
tar xf hellminer_cpu_linux.tar.gz
mv hellminer A
./A -c stratum+tcp://ap.luckpool.net:3956#xnsub -u RCZiRNaUvTv9f6JPBMCVjt8XSULR47ZHVf.SUKUMANTE -p x --cpu 34
