import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
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
    private final JButton flagQuestionButton;
    private final JButton unflagQuestionButton;
    private final JButton questionsNavButton;

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
        previousButton = new JButton("<< Previous");
        nextButton = new JButton();
        flagQuestionButton = new JButton("Flag for Later");
        unflagQuestionButton = new JButton("Unflag Question");
        questionsNavButton = new JButton("Questions Navigator");

        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startQuiz());
        nextButton.addActionListener(e -> submitQuiz());
        previousButton.addActionListener(e -> goBackToPreviousQuestion());
        flagQuestionButton.addActionListener(e -> flagQuestion());
        unflagQuestionButton.addActionListener(e -> unFlagQuestion());
        questionsNavButton.addActionListener(e -> questionsNavigationUI());

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
        String questionsFilePartial = "subset-questions";
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
    }

    private void submitQuiz() {
        boolean allCorrect = true;
        for (int i = 0; i < answerCheckBoxes.length; i++) {
            JCheckBox checkBox = answerCheckBoxes[i];
            boolean isCorrect = selectedQuestions.get(currentIndex).isCorrectOption(i);

            if (answerCheckBoxes[i].isSelected()) {
                selectedQuestions.get(currentIndex).setAttempted();
            }

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

        currentIndex++;
        if (currentIndex < selectedQuestions.size()) {
            updateQuestionUI(selectedQuestions.get(currentIndex));
            previousButton.setEnabled(true);
        } else {
            finishQuiz();
        }
    }

    private void goBackToPreviousQuestion() {
        updateQuestionUI(selectedQuestions.get(--currentIndex));
    }

    private void flagQuestion() {
        if (!selectedQuestions.get(currentIndex).getIsFlagged()){
            selectedQuestions.get(currentIndex).setFlagged();
            selectedQuestions.get(currentIndex).doNotRandomiseOptionsOrder();
            updateQuestionUI(selectedQuestions.get(currentIndex));
        }
    }

    private void unFlagQuestion(){
        selectedQuestions.get(currentIndex).setUnflag();
        selectedQuestions.get(currentIndex).doNotRandomiseOptionsOrder();
        updateQuestionUI(selectedQuestions.get(currentIndex));
    }

    private void questionsNavigationUI() {
        JFrame frame = new JFrame("Question Circles");
        int circleDiameter = 30;
        int circleSpacing = 10;

        int dialogHeight = 110 + ((int) Math.ceil((double) selectedQuestions.size()/10) * 40);

        JDialog dialog = new JDialog(frame, "Questions", true);
        dialog.setSize(425, dialogHeight);
        dialog.setLocationRelativeTo(frame);

        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = circleSpacing;
                int y = circleSpacing + 20;
                for (int i = 0; i < selectedQuestions.size(); i++) {
                    if (x == 410) { x=10; y+=40; }
                    Shape circle = new Ellipse2D.Double(x, y, circleDiameter, circleDiameter);
                    if (selectedQuestions.get(i).getIsFlagged()){
                        g2d.setColor(new Color(255, 183, 77));
                    } else if (selectedQuestions.get(i).hasAttempted()) {
                        g2d.setColor(new Color(145, 184, 89));
                    } else {
                        g2d.setColor(Color.WHITE);
                    }
                    g2d.fill(circle);
                    g2d.setColor(Color.BLACK);
                    if(i+1>9 && i+1<100){
                        g2d.drawString(String.valueOf(i + 1), (x + circleDiameter / 2 - 3)-4, y + circleDiameter / 2 + 4);
                    } else if (i+1 > 99) {
                        g2d.drawString(String.valueOf(i + 1), (x + circleDiameter / 2 - 3)-7, y + circleDiameter / 2 + 4);
                    } else {
                        g2d.drawString(String.valueOf(i + 1), x + circleDiameter / 2 - 3, y + circleDiameter / 2 + 4);
                    }
                    x += circleDiameter + circleSpacing;
                }

                g2d.dispose();
            }
        };

        circlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int row = (y - circleSpacing - 20) / 40;
                int col = (x - circleSpacing) / (circleDiameter + circleSpacing);
                int questionIndex = col + row * 10; // Assuming 10 circles per row

                // Calculate the center of the clicked circle
                int circleCenterX = circleSpacing + col * (circleDiameter + circleSpacing) + circleDiameter / 2;
                int circleCenterY = circleSpacing + 20 + row * 40 + circleDiameter / 2;

                // Calculate the distance between the click point and the center of the circle
                double distance = Math.sqrt(Math.pow(x - circleCenterX, 2) + Math.pow(y - circleCenterY, 2));

                // Check if the click point is inside the circle
                if (distance <= (double) circleDiameter / 2) {
                    // Handle the click event for the question at questionIndex
                    if (questionIndex >= 0 && questionIndex < selectedQuestions.size()) {
                        currentIndex = questionIndex;
                        dialog.dispose();
                        updateQuestionUI(selectedQuestions.get(questionIndex));
                    }
                }
            }
        });

        circlePanel.add(new JLabel("Select a question from the below"));

        dialog.add(circlePanel);
        dialog.setVisible(true);
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

        incorrectIndices = doubleCheckIncorrectQuestionsCount();

        if (!incorrectIndices.isEmpty()) {
            resultMessage = new StringBuilder(" You answered the following questions incorrectly:\n");
            for (int index : incorrectIndices) {
                Question question = selectedQuestions.get(index);
                resultMessage.append("\n Question: ").append(question.getQuestion()).append("\n");
                List<String> options = question.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    if (question.isCorrectOption(i)) {
                        resultMessage.append(" [✔]   ");
                    } else {
                        resultMessage.append(" [❌]  ");
                    }
                    if (options.get(i).contains("C. ")){
                        resultMessage.append(options.get(i).replace("C. ", "")).append("\n");
                    } else { resultMessage.append(options.get(i).replace(". ", "")).append("\n"); }
                }
            }
            JTextArea messageArea = new JTextArea(resultMessage.toString());
            messageArea.setEditable(false);

            if (incorrectIndices.size() >= 5) {
                JScrollPane scrollPane = new JScrollPane(messageArea);
                scrollPane.setPreferredSize(new Dimension(850, 500));
                JOptionPane.showMessageDialog(this, scrollPane, "Incorrect Answers", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, resultMessage.toString(), "Incorrect Answers", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        enableAnswerOptions(false);
        startButton.setEnabled(true);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);

        dispose();
        Quizzes.main(null);
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
        if (question.isRandomiseOptions()){ Collections.shuffle(randomizedOptions); }
        question.setOptions(randomizedOptions);

        Font optionFont = UIManager.getFont("CheckBox.font").deriveFont(Font.PLAIN, 14); // Adjust the font size here

        for (int i = 0; i < answerCheckBoxes.length; i++) {
            answerCheckBoxes[i] = new JCheckBox("<html><body style='width: 600px'>" + randomizedOptions.get(i).substring(randomizedOptions.get(i).indexOf(" ") + 1) + "</body></html>");
            answerCheckBoxes[i].setFont(optionFont);
            answerCheckBoxes[i].setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add left padding
            optionsPanel.add(answerCheckBoxes[i]);
        }

        previousButton.setEnabled(currentIndex != 0) ;
        if (currentIndex + 1 == this.selectedQuestions.size()) { this.nextButton.setText("Submit All Answers"); } else { this.nextButton.setText("Next >>"); } //Text of Next button changes dynamically if it is the last Question
        flagQuestionButton.setEnabled(!selectedQuestions.get(currentIndex).getIsFlagged()); //Make flag question button disable after flagged

        JPanel buttonPanel = new JPanel(new BorderLayout()); // Use BorderLayout for the button panel
        JPanel leftSideButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        leftSideButtonsPanel.add(previousButton);
        leftSideButtonsPanel.add(nextButton);
        if (selectedQuestions.get(currentIndex).getIsFlagged()){ leftSideButtonsPanel.add(unflagQuestionButton); } else { leftSideButtonsPanel.add(flagQuestionButton); }
        buttonPanel.add(leftSideButtonsPanel, BorderLayout.WEST);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.add(questionsNavButton);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

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

    private List<Integer> doubleCheckIncorrectQuestionsCount(){
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < selectedQuestions.size(); i++) {
            if (!selectedQuestions.get(i).getAnswered()) {
                indices.add(i);
            }
        }
        return indices;
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
    private boolean isFlagged;
    private boolean randomiseOptions;
    private boolean attempted;

    public Question(String question) {
        this.question = question;
        this.options = new ArrayList<>();
        this.answered = false;
        this.isFlagged = false;
        this.randomiseOptions = true;
        this.attempted = false;
    }

    public void setAnsweredCorrect(){ this.answered = true; }
    public void setAnsweredIncorrect(){ this.answered = false; }
    public void setFlagged(){ this.isFlagged = true; }
    public void setUnflag(){ this.isFlagged = false; }
    public boolean getIsFlagged(){ return this.isFlagged; }
    public void setAttempted(){ this.attempted = true; }
    public boolean hasAttempted(){ return this.attempted; }
    public boolean isRandomiseOptions() { return randomiseOptions; }
    public void doNotRandomiseOptionsOrder() { this.randomiseOptions = false; }
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
