@ECHO OFF
:: ---------------------------------------------------------------------------
:: Apache Maven Wrapper startup script, version 3.3.2 (Windows)
:: See mvnw for details.
:: ---------------------------------------------------------------------------

SET BASE_DIR=%~dp0
SET WRAPPER_JAR=%BASE_DIR%.mvn\wrapper\maven-wrapper.jar
SET WRAPPER_PROPERTIES=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties

IF "%JAVA_HOME%"=="" (
  SET JAVA_CMD=java
) ELSE (
  SET JAVA_CMD=%JAVA_HOME%\bin\java
)

IF NOT EXIST "%WRAPPER_JAR%" (
  FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
  )
  ECHO Downloading Maven Wrapper from: %WRAPPER_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

"%JAVA_CMD%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
