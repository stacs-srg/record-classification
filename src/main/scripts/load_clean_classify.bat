@REM
@REM Copyright 2012-2017 Systems Research Group, University of St Andrews:
@REM <https://github.com/stacs-srg>
@REM
@REM This file is part of the module record-classification.
@REM
@REM record-classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
@REM License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
@REM version.
@REM
@REM record-classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
@REM warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
@REM
@REM You should have received a copy of the GNU General Public License along with record-classification. If not, see
@REM <http://www.gnu.org/licenses/>.
@REM

@ECHO OFF

set CLASSPATH=.
set JVM_OPTIONS="-Xms128m -Xmx512m"
set JAR_FILE="record_classification_cli-1.0-SNAPSHOT.jar"

set PROCESS_DIR=hisco_ensemble_with_olr
set UNSEEN_DATA=test_evaluation_ascii_windows.csv
set OUTPUT=output.csv

java %JVM_OPTIONS% -cp %CLASSPATH% -jar %JAR_FILE% load_clean_classify -p %PROCESS_DIR% -d %UNSEEN_DATA% -o %OUTPUT% -f JSON_COMPRESSED -cl COMBINED -dl "|"
