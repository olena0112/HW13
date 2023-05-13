import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class JsonPlaceholderApiClient {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public static void main(String[] args) throws IOException {
        JsonPlaceholderApiClient apiClient = new JsonPlaceholderApiClient();

        String newUserJson = "{\"name\": \"John Doe\", \"username\": \"johndoe\", \"email\": \"johndoe@example.com\"}";
        String createdUserJson = apiClient.post("/users", newUserJson);
        System.out.println("Created user: " + createdUserJson);

        String updatedUserJson = apiClient.put("/users/1", "{\"name\": \"Jane Doe\"}");
        System.out.println("Updated user: " + updatedUserJson);

        int responseCode = apiClient.delete("/users/1");
        System.out.println("Delete user response code: " + responseCode);

        String allUsersJson = apiClient.get("/users");
        System.out.println("All users: " + allUsersJson);

        String userByIdJson = apiClient.get("/users/2");
        System.out.println("User by id: " + userByIdJson);

        String userByUsernameJson = apiClient.get("/users?username=johndoe");
        System.out.println("User by username: " + userByUsernameJson);
        apiClient.getAndSaveUserPostComments(1);
    }
    private void getAndSaveUserPostComments(int userId) throws IOException {
        // Отримання останнього поста користувача
        String userPostsJson = get("/users/" + userId + "/posts");
        JsonArray userPosts = JsonParser.parseString(userPostsJson).getAsJsonArray();
        JsonObject lastPost = userPosts.get(userPosts.size() - 1).getAsJsonObject();
        int postId = lastPost.get("id").getAsInt();

        // Отримання коментарів до останнього поста
        String postCommentsJson = get("/posts/" + postId + "/comments");

        // Збереження коментарів у файл
        String fileName = "user-" + userId + "-post-" + postId + "-comments.json";
        saveCommentsToFile(postCommentsJson, fileName);

        System.out.println("Comments saved to file: " + fileName);
    }
    private void saveCommentsToFile(String commentsJson, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        fileWriter.write(commentsJson);
        fileWriter.close();
    }
    private void getOpenTasksForUser(int userId) throws IOException {
        String userTodosJson = get("/users/" + userId + "/todos");
        JsonArray userTodos = JsonParser.parseString(userTodosJson).getAsJsonArray();

        System.out.println("Open tasks for User " + userId + ":");
        for (int i = 0; i < userTodos.size(); i++) {
            JsonObject todo = userTodos.get(i).getAsJsonObject();
            boolean completed = todo.get("completed").getAsBoolean();
            if (!completed) {
                int taskId = todo.get("id").getAsInt();
                String title = todo.get("title").getAsString();
                System.out.println("Task ID: " + taskId + ", Title: " + title);
            }
        }
    }
    private String get(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return getResponse(con);
    }

    private String post(String path, String jsonBody) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        return getResponse(con);
    }

    private String put(String path, String jsonBody) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        return getResponse(con);
    }

    private int delete(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        return con.getResponseCode();
    }

    private String getResponse(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("Response code: " + responseCode);
        }
    }
}
