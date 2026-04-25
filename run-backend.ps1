$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

Write-Host "Compiling backend..."
javac -cp ".;mysql-connector-j-9.7.0.jar" LostAndFound.java LostFoundWebServer.java TestConnection.java

if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

Write-Host "Starting backend server at http://localhost:8080/"
java -cp ".;mysql-connector-j-9.7.0.jar" LostFoundWebServer
