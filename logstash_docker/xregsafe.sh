#!/bin/sh
# © Copyright 2002-2006, Guardium, Inc.  All rights reserved.  This material
# may not be copied, modified, altered, published, distributed, or otherwise
# displayed without the express written consent of Guardium, Inc.
# Copyright 2012 IBM Inc. All rights reserved.

source $GUARD_HOME/lib/shell/std.sh

reexec_as_user tomcat

LOG_FILENAME=xregsafe
source $GUARD_HOME/lib/shell/log.sh

SAVE_DIR="$GUARD_ETC_DIR/web/xreg.save"
WEBAPP_CONF_DIR="$GUARD_TOMCAT_DIR/webapps/ROOT/WEB-INF/conf/"

msg "Keeping xreg files safe."
log_info "Using SAVE_DIR=$SAVE_DIR"
log_info "Using WEBAPP_CONF_DIR=$WEBAPP_CONF_DIR"

for xreg_file in $(find $WEBAPP_CONF_DIR -name "*xreg")
do
	# keep files that seem to end with a valid tag
	xreg_filename=$(basename $xreg_file)
	if tail -n 2 $xreg_file | grep -q "^<\/registry>$"; then
		cp ${xreg_file} $SAVE_DIR
	else
		# files that fail are partial or empty copies. Attempt to retrieve an older good copy
		log_warning "Found invalid xreg file $xreg_filename, copying in an older copy."
		run_err cp $SAVE_DIR/${xreg_filename} $WEBAPP_CONF_DIR
	fi
done
exit 0
