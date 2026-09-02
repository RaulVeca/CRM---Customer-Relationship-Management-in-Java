@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0zip_adapter.ps1" %*
