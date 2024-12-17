package src;

import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;


public class GithubUserActivity {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your github username");
        String input = sc.nextLine();
        String urlString = "https://api.github.com/users/" + input + "/events";

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(urlString)).header("Accept",
                    "application/json").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                System.out.println("Error not found");
                return;
            }
            if (response.statusCode() == 200) {
                JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
                displayJSON(jsonArray);
            }

        } catch (URISyntaxException | IOException uriSyntaxException) {
            System.out.println(uriSyntaxException.getMessage());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            System.out.println(interruptedException.getMessage());
        }

    }

    public static void displayJSON(JsonArray json) {
        for (JsonElement element : json) {
            JsonObject event = element.getAsJsonObject();
            String type = event.get("type").getAsString();
            String action;
            switch (type) {
                case "PushEvent":
                    int commitCount = event.get("payload").getAsJsonObject().get("commits").getAsJsonArray().size();
                    action = "Pushed " + commitCount + " commit(s) to " + event.get("repo").getAsJsonObject().get("name");
                    break;
                case "IssuesEvent":
                    action = event.get("payload").getAsJsonObject().get("action").getAsString().toUpperCase().charAt(0)
                            + event.get("payload").getAsJsonObject().get("action").getAsString()
                            + " an issue in ${event.repo.name}";
                    break;
                case "WatchEvent":
                    action = "Starred " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "ForkEvent":
                    action = "Forked " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "CreateEvent":
                    action = "Created " + event.get("payload").getAsJsonObject().get("ref_type").getAsString()
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();

                    break;
                default:
                    action = event.get("type").getAsString().replace("Event", "")
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
            }
            System.out.println(action);
        }
    }
}
