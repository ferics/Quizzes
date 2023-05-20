import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.awt.Image.SCALE_SMOOTH;

public class Quizzes extends JFrame {
    private List<Question> selectedQuestions;
    private List<Integer> incorrectIndices;
    private int currentIndex;

    private int score = 0;
    private final int numberOfQuestions = 10;
    private int answeredCounter = 1;

    private final JLabel questionLabel;
    private JCheckBox[] answerCheckBoxes;
    private final JButton startButton;
    private final JButton submitButton;
    private final JButton previousButton;

    public Quizzes() {
        setTitle("Quiz Program");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(850, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        questionLabel = new JLabel("Press 'Start' to begin the quiz.");
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(questionLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        submitButton = new JButton("Submit");
        previousButton = new JButton("Previous");
        submitButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(submitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startQuiz());

        submitButton.addActionListener(e -> submitQuiz());

        previousButton.addActionListener(e -> goBackToPreviousQuestion());

        add(mainPanel);

        setVisible(true);
    }

    private void startQuiz() {
        List<Question> questions = readQuestionsFromFile();
        incorrectIndices = new ArrayList<>();
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions found in the file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedQuestions = selectRandomQuestions(questions);
        if (selectedQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Insufficient questions to start the quiz.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentIndex = 0;

        updateQuestionUI(selectedQuestions.get(currentIndex));
        enableAnswerOptions(true);
        startButton.setEnabled(false);
        submitButton.setEnabled(true);
        previousButton.setEnabled(false);
    }

    private void submitQuiz() {
        int selectedCount = 0;

        boolean allCorrect = true;
        for (int i = 0; i < answerCheckBoxes.length; i++) {
            JCheckBox checkBox = answerCheckBoxes[i];
            boolean isCorrect = selectedQuestions.get(currentIndex).isCorrectOption(i);

            if (answerCheckBoxes[i].isSelected()) {
                selectedCount++; }

            if (checkBox.isSelected() != isCorrect) {
                allCorrect = false;
                if (!incorrectIndices.contains(currentIndex)) {
                    incorrectIndices.add(currentIndex);
                }
            }
        }

        if (allCorrect) {
            score++;
        }

        if (selectedCount == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one option.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            currentIndex++;
            if (currentIndex < selectedQuestions.size()) {
                updateQuestionUI(selectedQuestions.get(currentIndex));
                previousButton.setEnabled(true);
            } else {
                finishQuiz();
            }
        }
    }

    private void goBackToPreviousQuestion() {
        currentIndex--;
        if (currentIndex >= 0) {
            updateQuestionUI(selectedQuestions.get(currentIndex));
            if (currentIndex == 0) {
                previousButton.setEnabled(false);
            }
        }
    }
    private void finishQuiz() {
        double percentageScore = (double) score / selectedQuestions.size() * 100;

        StringBuilder resultMessage = new StringBuilder("Quiz finished!\n\n" +
                "Correct answers: " + score + "\n" +
                "Incorrect answers: " + (selectedQuestions.size() - score) + "\n" +
                "Percentage: " + String.format("%.2f", percentageScore) + "%\n\n");

        if (percentageScore >= 59) {
            resultMessage.append("Congratulations, you passed!");
            JOptionPane.showMessageDialog(this, resultMessage.toString(), "Quiz Results", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(".\\files\\green-tick.png").getImage().getScaledInstance(50,50, SCALE_SMOOTH)));
        } else {
            resultMessage.append("Sorry, you did not pass.");
            JOptionPane.showMessageDialog(this, resultMessage.toString(), "Quiz Results", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(".\\files\\red-cross.png").getImage().getScaledInstance(50,50, SCALE_SMOOTH)));
        }

        if (!incorrectIndices.isEmpty()) {
            resultMessage = new StringBuilder("You answered the following questions incorrectly:\n");
            for (int index : incorrectIndices) {
                Question question = selectedQuestions.get(index);
                resultMessage.append("\nQuestion: ").append(question.getQuestion()).append("\n");
                List<String> options = question.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    if (question.isCorrectOption(i)) {
                        resultMessage.append("[✔] ");
                    } else {
                        resultMessage.append("[❌] ");
                    }
                    if (options.get(i).contains("C. ")){
                        resultMessage.append(options.get(i).replace("C. ", "")).append("\n");
                    } else { resultMessage.append(options.get(i).replace(". ", "")).append("\n"); }
                }
            }
            JOptionPane.showMessageDialog(this, resultMessage.toString(), "Incorrect Answers", JOptionPane.INFORMATION_MESSAGE);
        }

        enableAnswerOptions(false);
        startButton.setEnabled(true);
        submitButton.setEnabled(false);
        previousButton.setEnabled(false);
    }

    private void updateQuestionUI(Question question) {

        questionLabel.setText("<html><body style='width: 650px; padding: 10px '>" + answeredCounter++ +"/"+numberOfQuestions+":"+ question.getQuestion() + "</body></html>");
        questionLabel.setVerticalAlignment(JLabel.TOP);

        if (answerCheckBoxes != null) {
            for (JCheckBox checkBox : answerCheckBoxes) {
                checkBox.setVisible(false);
            }
        }

        answerCheckBoxes = new JCheckBox[question.getOptions().size()];
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(question.getOptions().size(), 1, 0, 5)); // Adjust the spacing values here

        List<String> randomizedOptions = new ArrayList<>(question.getOptions());
        Collections.shuffle(randomizedOptions);
        question.setOptions(randomizedOptions);

        Font optionFont = UIManager.getFont("CheckBox.font").deriveFont(Font.PLAIN, 14); // Adjust the font size here

        for (int i = 0; i < answerCheckBoxes.length; i++) {
            answerCheckBoxes[i] = new JCheckBox("<html><body style='width: 600px'>" + randomizedOptions.get(i).substring(randomizedOptions.get(i).indexOf(" ") + 1) + "</body></html>");
            answerCheckBoxes[i].setFont(optionFont);
            answerCheckBoxes[i].setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add left padding
            optionsPanel.add(answerCheckBoxes[i]);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout()); // Use FlowLayout for the button panel
        buttonPanel.add(previousButton);
        buttonPanel.add(submitButton);

        getContentPane().removeAll();
        getContentPane().add(questionLabel, BorderLayout.NORTH);
        getContentPane().add(optionsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void enableAnswerOptions(boolean enable) {
        if (answerCheckBoxes != null) {
            for (JCheckBox checkBox : answerCheckBoxes) {
                checkBox.setEnabled(enable);
            }
        }
    }

    private List<Question> readQuestionsFromFile() {
        List<Question> questions = new ArrayList<>();

        String fileName = ".\\files\\questions.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            Question question = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    if (line.startsWith("Q")) {
                        if (question != null) {
                            questions.add(question);
                        }

                        question = new Question(line.substring(2));
                    } else if (question != null && (line.startsWith("1") || line.startsWith("2") || line.startsWith("3") || line.startsWith("4") || line.startsWith("5") || line.startsWith("6"))) {
                        String option = line.substring(1).trim();
                        question.addOption(option);
                    }
                }
            }

            if (question != null) {
                questions.add(question);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return questions;
    }

    private List<Question> selectRandomQuestions(List<Question> questions) {
        List<Question> selectedQuestions = new ArrayList<>();
        Random random = new Random();

        if (questions.size() <= numberOfQuestions) {
            return questions;
        }

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(questions.size());
            selectedQuestions.add(questions.get(index));
            questions.remove(index);
        }

        return selectedQuestions;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Quizzes();
            }
        });
    }
}

class Question {
    private final String question;
    private List<String> options;

    public Question(String question) {
        this.question = question;
        options = new ArrayList<>();
    }

    public void setOptions(List<String> randomisedOptions){
        options = randomisedOptions;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void addOption(String option) {
        options.add(option);
    }

    public boolean isCorrectOption(int index) {
        String option = options.get(index);
        return option.matches("C.*");
    }
}
