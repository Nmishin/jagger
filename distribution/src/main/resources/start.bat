echo off
REM save the first argument because it will be not available after `shift`
set FIRST_ARG="%1" 
REM required to pass all positional arguments except first one
shift 
echo "%JAVA_HOME%\bin\java" -Xmx2550m -Xms2550m -classpath ./modules\chassis\*;./modules\diagnostics\*;./lib\*;./configuration\boot\ -Dlog4j.configuration=file:jagger.log4j.properties -Djava.library.path=./lib/native com.griddynamics.jagger.JaggerLauncher "%FIRST_ARG%"
"%JAVA_HOME%\bin\java" -Xmx2550m -Xms2550m -classpath ./modules\chassis\*;./modules\diagnostics\*;./lib\*;./configuration\boot\ -Dlog4j.configuration=file:jagger.log4j.properties -Djava.library.path=./lib/native com.griddynamics.jagger.JaggerLauncher "%FIRST_ARG%"