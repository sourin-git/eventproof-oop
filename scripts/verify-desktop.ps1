# Run after mvn test, from the project root. Saves one clearly marked demo incident.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectRoot
$taskRepository = Join-Path $projectRoot '.maven-repository'
if (-not (Test-Path -LiteralPath "$taskRepository/org/openjfx/javafx-controls/25.0.1/javafx-controls-25.0.1-win.jar")) {
    $taskRepository = Join-Path $env:USERPROFILE '.m2/repository'
}
$javafxJars = @('base', 'graphics', 'controls') | ForEach-Object {
    Join-Path $taskRepository "org/openjfx/javafx-$_/25.0.1/javafx-$_-25.0.1-win.jar"
}
$jdbcJar = Join-Path $taskRepository 'com/mysql/mysql-connector-j/9.5.0/mysql-connector-j-9.5.0.jar'
foreach ($requiredJar in ($javafxJars + @($jdbcJar))) {
    if (-not (Test-Path -LiteralPath $requiredJar)) { throw 'Dependencies are missing. Run Maven test first.' }
}
$modulePath = $javafxJars -join ';'
$classPath = "target/classes;target/test-classes;$jdbcJar"
& java --module-path $modulePath --add-modules javafx.controls --enable-native-access=javafx.graphics -cp $classPath com.eventproof.DesktopDemoCheck
if ($LASTEXITCODE -ne 0) { throw 'Initial desktop check failed.' }
& java --module-path $modulePath --add-modules javafx.controls --enable-native-access=javafx.graphics -cp $classPath com.eventproof.DesktopDemoCheck reopen
if ($LASTEXITCODE -ne 0) { throw 'Reopened desktop check failed.' }
