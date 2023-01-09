package com.api;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
public class DataTransfer
{
    public static void main(String[] args)
    {
        DataTransfer dataTransfer = new DataTransfer();
        String json = "[\n{\n\"date\": \"2023-01-01\",\n\"time\": \"01:38:32\",\n\"content\": \"@micsolana In days to come, weâ\u0080\u0099ll add granularity to verified badge, such as organizational affiliation &amp; ID verification\",\n\"sentiment\": {\n\"sentimentText\": \"neutral\",\n\"sentimentValue\": 0.7305445\n}\n},\n{\n\"date\": \"2023-01-01\",\n\"time\": \"00:45:30\",\n\"content\": \"@micsolana We are changing the text to say â\u0080\u009CLegacy Verified. Could be notable, but could also be bogus.â\u0080\u009D\",\n\"sentiment\": {\n\"sentimentText\": \"neutral\",\n\"sentimentValue\": 0.58761513\n}\n}]";
        dataTransfer.postData("tweets",json);
    }
    public DataTransfer()
    {

    }
    public void postData(String table,String data)//THIS WORKS NO TOUCHIE!!!!
    {
        URL url = null;
        try
        {
            url = new URL("http://localhost:8080/api/"+table+"/insert");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream())
            {
                os.write(out);
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
