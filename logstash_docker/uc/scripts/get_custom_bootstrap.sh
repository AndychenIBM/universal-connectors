#!/bin/bash
# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2023
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************

BASE_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# run in oc debug:
# ./get_custom_bootstrap.sh -u hadar.kornyanski@ibm.com -token <token> -v INS-36111-v3.3.0 -id 5 -o
ARTIFACTORY_USERNAME="default_user"
ARTIFACTORY_AUTH_TOKEN="default_token"
VERSION="main"
OVERWRITE_BOOTSTRAP_JAR=false

# Function to display usage information
function display_usage {
    echo "Usage: $0 -u <username> -token <token> [-id <Connection id>] [-v <version>] [-o]"
    echo "Options:"
    echo "  -u     Artifactory username (string)"
    echo "  -token Artifactory auth token (string)"
    echo "  -id    Connection ID (integer, default: $INPUT_PLUGIN_ID)"
    echo "  -v     Version (string, default: $VERSION)"
    echo "  -o     Overwrite current bootstrap jar(boolean, default: $OVERWRITE_BOOTSTRAP_JAR)"
    exit 1
}

# Parse arguments
while [ "$#" -gt 0 ]; do
    case "$1" in
        -u)
            shift
            ARTIFACTORY_USERNAME="$1"
            ;;
        -token)
            shift
            ARTIFACTORY_AUTH_TOKEN="$1"
            ;;
        -id)
            shift
            export INPUT_PLUGIN_ID="$1"
            ;;
        -v)
            shift
            VERSION="$1"
            ;;
        -o)
            OVERWRITE_BOOTSTRAP_JAR=true
            ;;
        *)
            display_usage
            ;;
    esac
    shift
done

# Validate required arguments
if [ "$ARTIFACTORY_USERNAME" == "default_user" ] || [ "$ARTIFACTORY_AUTH_TOKEN" == "default_token" ]; then
    echo "Error: Username and Token are required."
    display_usage
fi

valid_credentials=$(curl -u $ARTIFACTORY_USERNAME:$ARTIFACTORY_AUTH_TOKEN https://na.artifactory.swg-devops.com/artifactory/api/system/ping)

if [ "$valid_credentials" != "OK" ]; then
    echo "Error: artifactory credentials are invalid."
    exit 1
fi

# Display the provided values
echo "UC ID: $INPUT_PLUGIN_ID"
echo "Version: $VERSION"
echo "Overwrite bootstrap jar: $OVERWRITE_BOOTSTRAP_JAR"

BOOTSTRAP_PATH="/usr/share/logstash/setup/bootstrap-$VERSION.jar"
if [ "$OVERWRITE_BOOTSTRAP_JAR" == "true" ]; then
    BOOTSTRAP_PATH="/usr/share/logstash/setup/uc-bootstrap.jar"
fi

echo "downloading uc-bootstrap JAR from artifactory with version: $VERSION"
curl -u $ARTIFACTORY_USERNAME:$ARTIFACTORY_AUTH_TOKEN -o $BOOTSTRAP_PATH https://na.artifactory.swg-devops.com/artifactory/sec-guardium-next-gen-team-gradle-local/com/ibm/guardium/ucbootstrap/$VERSION/ucbootstrap-$VERSION.jar

chmod +x $BOOTSTRAP_PATH
export BOOTSTRAP_PATH

/usr/share/logstash/scripts/start_logstash_gi.sh
