#!/bin/sh

#
# Copyright © 2015-2021 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ -n "$DEBUG" ]; then
    echo "[$0] Debug mode enabled"
fi

# Locate the directory of this script
APP_BASE_NAME=`basename "$0"`
SAVED="`pwd`"
cd "`dirname \"$0\"`" >/dev/null
APP_HOME=`pwd -P`
cd "$SAVED" >/dev/null

# Attempt to find Java
if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
        # IBM's JDK on AIX uses this
        JAVACMD="$JAVA_HOME/jre/sh/java"
    elif [ -x "$JAVA_HOME/bin/java" ]; then
        JAVACMD="$JAVA_HOME/bin/java"
    fi
fi

if [ -z "$JAVACMD" ]; then
    JAVACMD="java"
    if ! command -v "$JAVACMD" >/dev/null 2>&1; then
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
        exit 1
    fi
fi

# Increase the maximum file descriptors if we can
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ]; then
    MAX_FD_LIMIT=`ulimit -n 2>/dev/null`
    if [ $? -eq 0 ]; then
        if [ "$MAX_FD_LIMIT" = "max" -o "$MAX_FD_LIMIT" = "unlimited" ]; then
            MAX_FD_LIMIT=65536
        fi
        ulimit -n "$MAX_FD_LIMIT" >/dev/null 2>&1
    fi
fi

# Setup JVM arguments
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

exec "$JAVACMD" $DEFAULT_JVM_OPTS -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
