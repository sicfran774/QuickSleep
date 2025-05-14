package io.sicfran.quickSleep.tools;

import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Requests {

    public static final String hangarAppPage = "https://hangar.papermc.io/sicfran/QuickSleep";
    public static final String hangarAPIURL = "https://hangar.papermc.io/api/v1/projects/sicfran/QuickSleep/versions";

    public static List<String> checkIfNewVersion(String version){
        List<String> messages = new ArrayList<>();

        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hangarAPIURL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // Find the first occurrence of "name":"<version>"
            int nameIndex = body.indexOf("\"name\":\"");
            if (nameIndex == -1) return messages;

            int start = nameIndex + 8; // length of "name":" is 8
            int end = body.indexOf("\"", start);
            String latestVersion = body.substring(start, end);

            if(!version.equals(latestVersion)){
                messages.add("New version available! v" + latestVersion);
                messages.add("Download here: " + hangarAppPage);
            }
        } catch (Exception e){
            messages.add("Failed to check if there is a new version. Please check " +
                    hangarAppPage +
                    " for updates.");
        }

        return messages;
    }

    public static void logMessages(JavaPlugin plugin, List<String> messages){
        for(String message : messages){
            plugin.getLogger().info(message);
        }
    }
}
