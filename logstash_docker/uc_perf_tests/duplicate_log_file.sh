#!/bin/bash
tar -xzvf test_logs/1.tar.gz -C test_logs/
for i in {2..50}; do cp test_logs/1.log test_logs/$i.log; done


