#!/bin/bash

echo "preparing to install $1..."
logstash-plugin install $1
echo "$1 is now installed and ready to use"
