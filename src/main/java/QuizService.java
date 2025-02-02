import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class QuizService {
    private final String API_KEY = System.getenv("API_KEY");
    private final String API_URL = System.getenv("API_URL");
    private final Map<Integer, String> categoryMap = new HashMap<>();
    private int questionTotal;
    private String category;
    private List<ApiResponse> questions;
    private final Scanner scanner;

    QuizService() {
        scanner = new Scanner(System.in);
        categoryMap.put(1, "General Knowledge");
        categoryMap.put(2, "Books");
        categoryMap.put(3, "Film");
        categoryMap.put(4, "Music");
        categoryMap.put(5, "Musicals & Theatres");
        categoryMap.put(6, "Television");
        categoryMap.put(7, "Video Games");
        categoryMap.put(8, "Board Games");
        categoryMap.put(9, "Science & Nature");
        categoryMap.put(10, "Computers");
    }

    private void generateRandomQuestions() throws RuntimeException {


        String json = String.format("""
                {
                    "model": "gpt-4o-mini",
                    "messages": [
                        {
                            "role": "user",
                            "content": "Generate %s multiple-choice questions in "%s" area with four different answers. Return the response in JSON format: [{question: response, answers: [], correct_answer: ""}]"
                        }
                    ],
                    "temperature": 0.7
                }
                """, questionTotal, category);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "Quiz_Game") // Set your domain or app name
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            ObjectMapper objectMapper = new ObjectMapper();
            questions = objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ApiResponse.class));

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    private void startAgain() {
        System.out.print("Do you want to continue? (Y/N): ");
        String continueGame = scanner.next();
        if (continueGame.equalsIgnoreCase("N")) {
            System.out.println("Thank you for playing the game.");
            scanner.close();
            System.exit(0);
        }
        printQuestions();
    }

    private void printQuestions() {
        int allowedTries = 3;

        for (int i = 0; i < questions.size(); i++) {
            ApiResponse task = questions.get(i);
            String question = task.getQuestion();
            List<String> answers = task.getAnswers();
            String correctAnswer = task.getCorrect_answer();

            System.out.println("Question " + (i + 1) + ": " + question);
            for (String answer : answers) {
                System.out.printf("%s) ", answer);
            }

            System.out.print("Your Answer: ");
            String userAnswer = scanner.next();

            if (!userAnswer.equalsIgnoreCase(correctAnswer)) {
                allowedTries--;
                if (allowedTries == 0) {

                    System.out.println("You have exhausted all your tries. The correct answer is: " + correctAnswer);
                    startAgain();
                }
                System.out.println("Incorrect Answer. You have " + allowedTries + " tries left.");
            }
            System.out.println("*************************");
            System.out.println("Well Done! You got the correct answer.");
            System.out.println("*************************");
        }
        scanner.close();
    }

    public void startQuiz() throws IOException, InterruptedException {
        while (true) {
            System.out.println("*************************");
            System.out.println("Welcome to the Quiz Game");
            System.out.println("*************************");
            System.out.println("Please choose a category from the following options: ");
            categoryMap.forEach((key, value) -> System.out.println(key + " - " + value));
            int answer = scanner.nextInt();
            if (!categoryMap.containsKey(answer)) {
                System.out.println("Invalid category selected");
                continue;
            }
            category = categoryMap.get(answer);
            System.out.println("Enter the number of questions you want to generate: ");
            questionTotal = scanner.nextInt();
            break;
        }
        generateRandomQuestions();
        printQuestions();
    }
}
