@echo off
javac *.java

echo.
echo Bem-vindo ao Simulador de Round Robin!
echo.
echo *****************************
echo *                           *
echo *           R O U N D       *
echo *        R O B I N         *
echo *                           *
echo *                           *
echo *                           *
echo *    Pressione Enter para   *
echo *    iniciar a simulação... *
echo *                           *
echo *****************************


pause > nul
echo Insira o Quantum.
java RoundRobin > saida.txt
start saida.txt
start gantt.txt
echo Compilação concluída.

