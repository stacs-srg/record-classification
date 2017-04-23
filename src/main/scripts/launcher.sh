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


if [ -n "$1" ];
then
    export MAVEN_OPTS="-Xmx"$1"G"
    echo Setting heap size: $1GB
fi

mvn exec:java -q -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher" -e -Dexec.args="$2 $3 $4 $5 $6 $7 $8 $9 $10 $11 $12"
