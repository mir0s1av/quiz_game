

import java.io.IOException;


public class Main {

    public static void main(String[] args) {
        try {
            QuizService quizService = new QuizService();
            quizService.startQuiz();
//
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
