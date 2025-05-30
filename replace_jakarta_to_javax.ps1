# Script para reemplazar imports de jakarta a javax
$projectPath = "c:\Users\edwin\Desktop\EvaluApp"
$javaFiles = Get-ChildItem -Path $projectPath -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $newContent = $content -replace 'jakarta\.', 'javax.'
    
    if ($newContent -ne $content) {
        Set-Content -Path $file.FullName -Value $newContent -NoNewline
        Write-Host "Actualizado: $($file.FullName)"
    }
}

Write-Host "Proceso completado."
