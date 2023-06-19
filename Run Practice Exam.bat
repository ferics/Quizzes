@echo off
setlocal
set "batch_dir=%~dp0"
set "jdk_bin=%batch_dir%files\jdk\bin"
set "path=%jdk_bin%;%path%"

cd %batch_dir%src

start /min cmd /c "javac Quizzes.java & java Quizzes & del Quizzes$1.class Quizzes$2.class Question.class Quizzes.class & exit"