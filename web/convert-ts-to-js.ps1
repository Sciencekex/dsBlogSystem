# Step 1: 删除 TypeScript 相关依赖
Write-Host "卸载 TypeScript 相关依赖..."
npm uninstall typescript @types/react @types/react-dom

# 删除 tsconfig.json
Write-Host "删除 tsconfig.json 配置文件..."
Remove-Item -Path "tsconfig.json" -Force -ErrorAction SilentlyContinue

# Step 2: 批量修改文件扩展名 .tsx -> .jsx 和 .ts -> .js
Write-Host "批量修改文件扩展名..."

# 修改 .tsx 为 .jsx
Get-ChildItem -Recurse -Filter *.tsx | ForEach-Object {
    Rename-Item $_.FullName -NewName ($_.Name -replace '\.tsx$', '.jsx')
}

# 修改 .ts 为 .js
Get-ChildItem -Recurse -Filter *.ts | ForEach-Object {
    Rename-Item $_.FullName -NewName ($_.Name -replace '\.ts$', '.js')
}

# Step 3: 删除 TypeScript 类型注释
Write-Host "删除 TypeScript 类型注释..."

# 删除类型声明（例如：: string, : number）
Get-ChildItem -Recurse -Filter *.js | ForEach-Object {
    (Get-Content $_) | ForEach-Object { $_ -replace ":.*", "" } | Set-Content $_
}

# 删除接口、类型定义
Get-ChildItem -Recurse -Filter *.js | ForEach-Object {
    (Get-Content $_) | ForEach-Object { $_ -replace 'interface .*', "" } | Set-Content $_
    (Get-Content $_) | ForEach-Object { $_ -replace 'type .*', "" } | Set-Content $_
}

# Step 4: 修改 package.json（去除 TypeScript 配置）
Write-Host "修改 package.json..."

# 读取 package.json 内容
$packageJsonPath = "package.json"
$packageJsonContent = Get-Content -Path $packageJsonPath | Out-String

# 删除 "type": "module" 以及 TypeScript 配置
$packageJsonContent = $packageJsonContent -replace '"type": "module",?', ""
$packageJsonContent = $packageJsonContent -replace '"@types/.+"', ""

# 写回修改后的内容
Set-Content -Path $packageJsonPath -Value $packageJsonContent

Write-Host "转换完成！"

# Step 5: 提示用户运行 npm start 或 yarn start
Write-Host "现在你可以运行 'npm start' 或 'yarn start' 来启动项目，检查是否有错误并修复。"
