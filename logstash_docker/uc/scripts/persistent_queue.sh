#!/bin/bash

pipelines=$UC_ETC/pipelines.yml
if [[ $1 = "on" ]]; then
    echo "turning persistent queue on..."
    sed -i -r "s/#queue.type: persisted/queue.type: persisted/g" $pipelines
    sed -i -r "s/queue.type:.*/queue.type: persisted/g" $pipelines
    "${UC_SCRIPTS}"/reload_logstash_conf.sh
elif [[ $1 = "off" ]]; then
    echo "turning persistent queue off..."
    sed -i -r "s/queue.type: persisted/#queue.type: persisted/g" $pipelines
    "${UC_SCRIPTS}"/reload_logstash_conf.sh
else
    echo "invalid parameters - first parameter can be only on / off"
fi