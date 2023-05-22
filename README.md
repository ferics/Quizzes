# Quiz Program

This is a simple Java program that allows you to take a quiz by selecting answers to multiple-choice questions. The program presents questions one by one and calculates your score based on the correctness of your answers. It includes a graphical user interface (GUI) built using Swing.

## Prerequisites

To run this program, you need to have the following:

- Java Development Kit (JDK) installed on your system.
- An integrated development environment (IDE) or a text editor to edit and run Java code.

## Installation

1. Clone the repository or download the source code files.
2. Open the Java source file `Quizzes.java` in your preferred Java IDE or text editor.

## How to Use

1. Compile and run the `Quizzes.java` file to launch the quiz program, either using the jdk CLI or an IDE. 
1.1. If using an IDE, ensure the working directory is at src and not at the root of the project folder. For IntelliJ IDEA, right click on the `Quizzes.java`, click on `Modify Run Configuration..`, change the `Working directory` to the directory of the `src`
1.2. If using the jdk CLI, to to `src` folder and compile the program with `javac Quizzes.java`. Then run the program with `java Quizzes.class`. Alternatively create a `.bat` file next to the `Quizzes.java` file with the content: `start /min cmd /c "javac Quizzes.java & java Quizzes & del Question.class Quizzes.class & exit"` and run the program by double-clicking on the `.bat` file
3. The program window will appear with a "Start" button.
4. Click the "Start" button to begin the quiz.
5. The program will load the quiz questions from the `questions.txt` file located in the `files` directory.
   - Make sure the `questions.txt` file contains valid questions and options in the required format.
   - Each question should start with a "Q" followed by a space and the question text.
   - Each option should start with a number (1-6) followed by a space and the option text.
6. The program randomly selects a subset of questions for the quiz.
7. For each question, read the question text and select one or more correct options by ticking the checkboxes.
8. Click the "Submit" button to submit your answer(s).
   - If no options are selected, an error message will be shown.
9. The program will display a notification indicating whether your answer(s) were correct or incorrect.
10. Click the "Next" button to proceed to the next question.
11. You can also use the "Previous" button to go back to the previous question.
12. Repeat steps 6-10 until you have answered all the questions.
13. After answering all the questions, the program will display your quiz results, including the number of correct and incorrect answers and the percentage score.
14. If your percentage score is 59% or higher, a "Congratulations" message will be shown.
   - A green tick icon will be displayed along with the message.
15. If your percentage score is below 59%, a "Sorry" message will be shown.
   - A red cross icon will be displayed along with the message.
16. After finishing the quiz, you can click the "Start" button again to take another quiz.

## File Structure

The repository contains the following files:

- `Quizzes.java`: The main Java source file containing the quiz program code.
- `files/questions.txt`: A text file containing the quiz questions and options.
- `files/green-tick.png`: An image file used to display the green tick icon.
- `files/red-cross.png`: An image file used to display the red cross icon.

## Customisation

You can customise the program behavior and appearance by modifying the code in the `Quizzes.java` file. Some possible customisations include:

- Changing the window size and title.
- Modifying the text displayed on buttons and labels.
- Adjusting the font size and style.
- Changing the image icons used for quiz results.

Feel free to explore the code and make changes according to your requirements.
