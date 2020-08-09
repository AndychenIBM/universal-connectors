#!/bin/bash

#Use this script only when 1 pipeline with Persistent Queue is configured
pipelines=$UDS_ETC/pipelines.yml
if [[ $1 = "on" ]]; then
    echo "turning persistent queue on..."
    sed -i -r "s/#queue.type: persisted/queue.type: persisted/g" $pipelines
elif [[ $1 = "off" ]]; then
    echo "turning persistent queue off..."
    sed -i -r "s/queue.type: persisted/#queue.type: persisted/g" $pipelines
else
    echo "invalid parameters - first parameter can be only on / off"
fi

