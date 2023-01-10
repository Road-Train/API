package com.api;
import model.Bitcoin;
import model.Stock;
import model.Tweet;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static com.api.Parser.JSON_MAPPER;
public class Visualizer
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Elon's influence on the stock markets");
        JPanel dateButtons = new JPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        JMenuBar mb = new JMenuBar();
        JMenu queries = new JMenu("Perform Query");
        mb.add(queries);
        JMenuItem tweets = new JMenuItem("Tweets");
        JMenuItem stocks = new JMenuItem("Stocks");
        JMenuItem bitcoins = new JMenuItem("Bitcoins");
        queries.add(tweets);
        queries.add(stocks);
        queries.add(bitcoins);
        Stock[] dates = getDates();
        for(Stock date : dates)
        {
            JButton button = new JButton(date.getDate().toString());
            button.addActionListener(e -> setGUI(button.getText()));
            dateButtons.add(button);
        }
        JScrollPane scrollPane = new JScrollPane(dateButtons,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dateButtons.setLayout(new BoxLayout(dateButtons,BoxLayout.Y_AXIS));
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setVisible(true);
        frame.getContentPane().add(mb, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.WEST);
    }
    static void setGUI(String date)
    {

        Stock stock = stockByDate(date);
        Bitcoin bitcoin = bitcoinByDate(date);
        Tweet[] tweets = tweetsByDate(date);
    }
    /**
     * A static method to fetch the data of the stock market specifically, to be used for dates.
     * @return A list of all the stock values, to be used for generating the dates.
     */
    static Stock[] getDates()
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/stocks/allData");
            URLConnection connection = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Accept","application/json");
            InputStream input = connection.getInputStream();
            return JSON_MAPPER.readValue(input,Stock[].class);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    static Stock stockByDate(String date)
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/stocks/GET/"+date);
            URLConnection connection = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Accept","application/json");
            InputStream input = connection.getInputStream();
            return JSON_MAPPER.readValue(input,Stock.class);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    static Bitcoin bitcoinByDate(String date)
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/bitcoins/GET/"+date);
            URLConnection connection = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Accept","application/json");
            InputStream input = connection.getInputStream();
            return JSON_MAPPER.readValue(input,Bitcoin.class);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    static Tweet[] tweetsByDate(String date)
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/tweets/GET/"+date);
            URLConnection connection = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Accept","application/json");
            InputStream input = connection.getInputStream();
            return JSON_MAPPER.readValue(input,Tweet[].class);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    static void postData(String table, String data)//THIS WORKS NO TOUCHIE!!!!
    {
        URL url;
        try
        {
            url = new URL("http://localhost:8080/api/" + table + "/insert");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
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
