import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.awt.Image.SCALE_SMOOTH;

public class Quizzes extends JFrame {
    private List<Question> selectedQuestions;
    private List<Integer> incorrectIndices;
    private int currentIndex;

    private int score = 0;

    private final JLabel questionLabel;
    private JCheckBox[] answerCheckBoxes;
    private final JButton startButton;
    private final JButton nextButton;
    private final JButton previousButton;

    public Quizzes() {
        setTitle("Practice Exam");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(850, 300);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon("..\\files\\Images\\questions-logo.png");
        setIconImage(icon.getImage());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        questionLabel = new JLabel("Press 'Start' to begin the practice exam questions.");
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(questionLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        nextButton = new JButton("Next");
        previousButton = new JButton("Previous");

        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startQuiz());
        nextButton.addActionListener(e -> submitQuiz());
        previousButton.addActionListener(e -> goBackToPreviousQuestion());

        add(mainPanel);

        setVisible(true);
    }

    private void startQuiz() {
        File[] topicFolders = new File("..\\files\\Topics").listFiles(File::isDirectory);
        assert topicFolders != null;
        String[] topics = new String[topicFolders.length];
        for (int i = 0; i < topicFolders.length; i++) {
            topics[i] = topicFolders[i].getName();
        }

        String selectedTopic = (String) JOptionPane.showInputDialog(this, "Choose a topic:", "Topic Selection", JOptionPane.PLAIN_MESSAGE, null, topics, topics[0]);
        if (selectedTopic == null) {
            return; // User canceled the topic selection
        }

        String[] options = {"Subset", "All"};
        int choice = JOptionPane.showOptionDialog(this, "Choose the questions mode:", "Question Mode", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        String questionsFileFull = "all-questions";
        String questionsFilePartial = "subset-question";
        String questionsFile = (choice == 1) ? questionsFileFull : questionsFilePartial;

        List<Question> questions = readQuestionsFromFile(selectedTopic, questionsFile);

        incorrectIndices = new ArrayList<>();
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions found in the file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedQuestions = selectRandomQuestions(questions);
        if (selectedQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Insufficient questions to start the practice exam.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentIndex = 0;

        updateQuestionUI(selectedQuestions.get(currentIndex));
        enableAnswerOptions(true);
        startButton.setEnabled(false);
        nextButton.setEnabled(true);
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
            if (!selectedQuestions.get(currentIndex).getAnswered()){
                score++;
                selectedQuestions.get(currentIndex).setAnsweredCorrect();
            }
        } else {
            if (selectedQuestions.get(currentIndex).getAnswered()){
                score--;
                selectedQuestions.get(currentIndex).setAnsweredIncorrect();
            }
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

        StringBuilder resultMessage = new StringBuilder("Practice Exam finished!\n\n" +
                "Correct answers: " + score + "\n" +
                "Incorrect answers: " + (selectedQuestions.size() - score) + "\n" +
                "Percentage: " + String.format("%.2f", percentageScore) + "%\n\n");

        //cutoff score
        if (percentageScore >= 50) {
            resultMessage.append("Congratulations, you passed!");
            JOptionPane.showMessageDialog(this, resultMessage.toString(), "Exam Results", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon("..\\files\\Images\\green-tick.png").getImage().getScaledInstance(50,50, SCALE_SMOOTH)));
        } else {
            resultMessage.append("Sorry, you did not pass.");
            JOptionPane.showMessageDialog(this, resultMessage.toString(), "Exam Results", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon("..\\files\\Images\\red-cross.png").getImage().getScaledInstance(50,50, SCALE_SMOOTH)));
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
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
    }

    private void updateQuestionUI(Question question) {
        questionLabel.setText("<html><body style='width: 650px; padding: 10px '>" + (currentIndex+1) +"/"+this.selectedQuestions.size()+":"+ question.getQuestion() + "</body></html>");
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
        if (currentIndex+1 == this.selectedQuestions.size()) { this.nextButton.setText("Submit All Answers"); } else { this.nextButton.setText("Next"); }
        buttonPanel.add(nextButton);

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

    private List<Question> readQuestionsFromFile(String selectedTopic, String questionsFile) {
        List<Question> questions = new ArrayList<>();

        String fileName = "..\\files\\Topics\\"+selectedTopic+"\\"+questionsFile+".txt";
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
        List<Question> randomisedQuestions = new ArrayList<>(questions);

        // if the amount of loaded questions is less than the requested amount => just shuffle then return all of them
        if (questions.size() <= 500) {
            Collections.shuffle(randomisedQuestions);
            return randomisedQuestions;
        }

        return randomisedQuestions;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Quizzes::new);
    }
}

class Question {
    private final String question;
    private List<String> options;
    private boolean answered;

    public Question(String question) {
        this.question = question;
        this.options = new ArrayList<>();
        this.answered = false;
    }

    public void setAnsweredCorrect(){ this.answered = true; }

    public void setAnsweredIncorrect(){ this.answered = false; }

    public boolean getAnswered(){ return this.answered; }

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

    public boolean isCorrectOption(int index) { return options.get(index).matches("C.*"); }
}
