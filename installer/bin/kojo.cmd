@echo off
SET kojolibdir="..\lib"
SET kojojavacp=
SETLOCAL EnableDelayedExpansion
echo %kojolibdir%
FOR %%A IN (%kojolibdir%\*) DO (
  SET kojojavacp=!kojojavacp!;%%~A
)
REM launcher boots on the stock Scala toolchain; DesktopMain swaps in
REM lib\scala-tr for the real Kojo JVM when the user language is Turkish
FOR %%A IN (%kojolibdir%\scala-en\*) DO (
  SET kojojavacp=!kojojavacp!;%%~A
)
echo %kojojavacp%
call java -cp "%kojojavacp%" net.kogics.kojo.lite.DesktopMain
