#!/bin/sh
# Set debug options if required
java_debug_args=""

if [ "true" == "${JAVA_DEBUG}" ]; then
    java_debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"
fi

java $java_debug_args -jar demo-0.0.1-SNAPSHOT.jar