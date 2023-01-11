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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.api.Parser.JSON_MAPPER;
public class Visualizer extends JFrame
{
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private JTabbedPane selection;
    private JTabbedPane operation;
    private JScrollPane dateScroller;
    private JPanel main;
    private JPanel dateButtons;
    private JPanel queryPanel;
    private JFormattedTextField dateSearch;
    private JPanel datePanel;
    private JButton dateButton;
    private JPanel dataPanel;
    public Visualizer(String name)
    {
        super(name);
        setContentPane(main);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        getContentPane().add(datePanel,BorderLayout.WEST);
        getContentPane().add(selection,BorderLayout.CENTER);
        pack();
        setVisible(true);

    }
    public static void main(String[] args)
    {
        Visualizer v = new Visualizer("Elon's influence on the stock market");
    }
    static void setGUI(String date)
    {

        Stock stock = stockByDate(date);
        try
        {
            Bitcoin bitcoin = bitcoinByDate(date);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.err.println("No bitcoin data available for this date.");//TODO: work this into the GUI
        }
        try
        {
            Tweet[] tweets = tweetsByDate(date);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.err.println("No tweets available for this date.");//TODO: work this into the GUI
        }
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
            Stock[] output = JSON_MAPPER.readValue(input,Stock[].class);
            return output[0];
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    static Bitcoin bitcoinByDate(String date) throws ArrayIndexOutOfBoundsException
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
            Bitcoin[] output = JSON_MAPPER.readValue(input,Bitcoin[].class);
            return output[0];
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
    private void createUIComponents()
    {
        dateSearch = new JFormattedTextField(dateFormat);
        dateButtons = new JPanel();
        dateButtons.setLayout(new BoxLayout(dateButtons,BoxLayout.Y_AXIS));
        dateScroller = new JScrollPane();
        Stock[] dates = getDates();
        for(Stock date : dates)
        {
            JButton button = new JButton(date.getDate().toString());
            button.addActionListener(e -> setGUI(button.getText()));
            dateButtons.add(button);
        }
    }
}
