@echo off
C:\kotlinc\bin\kotlinc.bat lib/src/main/java/com/example/nhoms8/%1 -include-runtime -d temp_app.jar
java -jar temp_app.jar
