import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WikipediaSearch {
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static final Logger logger = LoggerFactory.getLogger(WikipediaSearch.class);

public static HttpURLConnection urlConnection(String url1) throws IOException {
    URL url = new URL(url1);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    logger.info("Connection request");
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
        logger.info("Connection OK");

        }else {
            logger.error("Error: HTTP response code " + responseCode);
        }
    return connection;
}
    private static StringBuilder getBufferedReaderResponse(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response;
    }
     public static void querySearch(String query) {
    try {
            // Construct the URL for the Wikipedia API search
            String queryUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + query + "&format=json";
            HttpURLConnection connection = urlConnection(queryUrl);

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(getBufferedReaderResponse(connection).toString());
        JSONObject queryObject = jsonResponse.getJSONObject("query");
        JSONArray searchResults = queryObject.getJSONArray("search");

        if (searchResults.length() == 0) {
                    System.out.println("No results found on Wikipedia for the query: " + query);
                } else {
                    logger.info("Search results for: " + query);
                    for (int i = 0; i < searchResults.length(); i++) {
                        JSONObject result = searchResults.getJSONObject(i);
                        String title = result.getString("title");
                        String snippet = result.getString("snippet");

                        System.out.println((i + 1) + ". " + title);
                        System.out.println(snippet.toString() + "\n");

                    }
                }
            } catch (IOException ex) {
        throw new RuntimeException(ex);
    }
    }

    public static void articleSearch(String articleTitle) {

        String articleUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&titles="
                + articleTitle.replace(" ", "_") + "&format=json";

        try {
            HttpURLConnection connection = urlConnection(articleUrl);
            // Parse and print the article content
            String jsonString = getBufferedReaderResponse(connection).toString();
            int startIndex = jsonString.indexOf("\"extract\":\"");
            int endIndex = jsonString.lastIndexOf("\"}");
            if (startIndex >= 0 && endIndex > startIndex) {
                String articleText = jsonString.substring(startIndex + 11, endIndex);
                articleText = articleText.replace("\\n", "\n"); // Unescape newline characters
                System.out.println(articleText);
            } else {
                System.out.println("Article not found or API response format has changed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void  main(String[] args) throws IOException {
        System.out.print("Enter your Wikipedia search query: ");
        String query = reader.readLine();
        querySearch(query);

        System.out.print("Enter an article: ");
        String articleTitle = reader.readLine();
        articleSearch(articleTitle);


    }
}


