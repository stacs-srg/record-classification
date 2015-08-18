@REM
@REM Copyright 2015 Digitising Scotland project:
@REM <http://digitisingscotland.cs.st-andrews.ac.uk/>
@REM
@REM This file is part of the module record_classification.
@REM
@REM record_classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
@REM License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
@REM version.
@REM
@REM record_classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
@REM warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
@REM
@REM You should have received a copy of the GNU General Public License along with record_classification. If not, see
@REM <http://www.gnu.org/licenses/>.
@REM

@ECHO OFF

set CLASSPATH=.
set JAVA_HOME=JREs\jre1.8.0_51_x64

::set PROCESS_DIR=hisco_ensemble_similarity
set PROCESS_DIR=hisco_ensemble_with_olr
set UNSEEN_DATA=test_evaluation_ascii_windows.csv
set OUTPUT=output.csv

%JAVA_HOME%\bin\java -Xms128m -Xmx512m -jar record_classification-1.0-SNAPSHOT-jar-with-dependencies.jar load_clean_classify -p %PROCESS_DIR% -d %UNSEEN_DATA% -o %OUTPUT% -f JSON_COMPRESSED -cl COMBINED -dl "|"