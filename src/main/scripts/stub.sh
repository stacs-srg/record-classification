#!/bin/sh
#
# Copyright 2012-2017 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module record-classification.
#
# record-classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# record-classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with record-classification. If not, see
# <http://www.gnu.org/licenses/>.
#

# This script is concatenated with the project jar file to give the top-level Unix executable.

# Max heap size can be overridden by setting $CLASSLI_JVM_ARGS e.g. "-Xmx80G" for 80GB.
DEFAULT_MAX_HEAP=1G

#echo dollar0: $0

#MYSELF=`which "$0" 2>/dev/null`
#echo myself1: $MYSELF

#[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"

# http://mywiki.wooledge.org/BashFAQ/031

#echo myself2: $MYSELF
#echo


if test -n "$JAVA_HOME"; then
    JAVA_COMMAND="$JAVA_HOME/bin/java"
else
    JAVA_COMMAND=java
fi

: ${CLASSLI_JVM_ARGS:= "-Xmx$DEFAULT_MAX_HEAP"}

#exec "$java" $CLASSLI_JVM_ARGS -jar $MYSELF "$@"
exec "$JAVA_COMMAND" $CLASSLI_JVM_ARGS -jar $0 "$@"
exit 1




# working directory
# directory containing classli executable
#     relative path specified in invocation
#     full path specified in invocation
#     in search path


# working dir = /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target
# classli dir = /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target
# ./classli -h
#
# dollar0: ./classli
# myself1: ./classli
# myself2: ./classli

# working dir = /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target
# classli dir = /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target
# /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli -h
#
# dollar0: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
# myself1: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
# myself2: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli

# working dir = /Users/graham
# classli dir = /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target
# PATH includes classli dir
# classli -h
#
# dollar0: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
# myself1: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
# myself2: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli

# working dir = /Users/graham
# classli dir = /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target
# /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli -h
#
# dollar0: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
# myself1: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
# myself2: /Users/graham/Documents/Code/IntelliJ/digitising_scotland/record_classification/target/classli
