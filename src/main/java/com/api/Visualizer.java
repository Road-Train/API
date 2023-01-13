package com.api;
import com.raven.chart.Chart;
import com.raven.chart.ModelChart;
import com.model.Bitcoin;
import com.model.Stock;
import com.model.Tweet;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Vector;

import static com.api.Parser.JSON_MAPPER;
public class Visualizer extends JFrame
{
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private com.raven.chart.Chart stockChart;
    private com.raven.chart.Chart bitcoinChart;
    private JTabbedPane selection;
    private JTabbedPane operation;
    private JScrollPane dateScroller;
    private JPanel main;
    private JPanel dateButtons;
    private JPanel queryPanel;
    private JPanel datePanel;
    private JPanel dataPanel;
    private JComboBox getTableSelect;
    private JComboBox dateComboBox;
    private JSplitPane charts;
    private JScrollPane tweetScroller;
    private JPanel tweetPanel;
    public Visualizer(String name)
    {
        super(name);
        setContentPane(main);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        getContentPane().add(datePanel, BorderLayout.WEST);
        getContentPane().add(selection, BorderLayout.CENTER);
        charts.setTopComponent(stockChart);
        charts.setBottomComponent(bitcoinChart);
        pack();
        setVisible(true);

    }
    public static void main(String[] args)
    {
        Visualizer v = new Visualizer("Elon's influence on the stock market");
    }
    /**
     * A method that will automatically fetch all stock dates which have associated tweet and bitcoin data present.
     * @return A list of all the stock values, to be used for generating the dates.
     */
    static ArrayList<Stock> getDates()
    {
        try
        {
            ArrayList<Stock> output = new ArrayList<>();
            Stock[] stocks = getAllStocks();
            Bitcoin[] bitcoins = getAllBitcoins();
            Tweet[] tweets = getAllTweets();
            boolean dateValid;
            for(Stock stock : stocks)
            {
                dateValid = false;
                LocalDate compareDate = stock.getDate();
                for(Bitcoin bitcoin : bitcoins)
                {
                    if(bitcoin.getDate().equals(compareDate))
                    {
                        for(Tweet tweet : tweets)
                        {
                            if(tweet.getDate().equals(compareDate))
                            {
                                dateValid = true;
                                break;
                            }
                        }
                    }

                }

                if(dateValid)
                {
                    output.add(stock);
                }
            }
            return output;
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static Tweet[] getAllTweets() throws IOException
    {
        URL tweetAllDates = new URL("http://localhost:8080/api/tweets/GET/allData");
        return getTweets(tweetAllDates);
    }
    private static Bitcoin[] getAllBitcoins() throws IOException
    {
        URL bitcoinAllDates = new URL("http://localhost:8080/api/bitcoins/GET/allData");
        URLConnection connection = bitcoinAllDates.openConnection();
        HttpURLConnection http = (HttpURLConnection) connection;
        http.setRequestMethod("GET");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        InputStream input = connection.getInputStream();
        return JSON_MAPPER.readValue(input, Bitcoin[].class);
    }
    private static Stock[] getAllStocks() throws IOException
    {
        URL stockAllDates = new URL("http://localhost:8080/api/stocks/GET/allData");
        URLConnection connection = stockAllDates.openConnection();
        HttpURLConnection http = (HttpURLConnection) connection;
        http.setRequestMethod("GET");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        InputStream input = connection.getInputStream();
        return JSON_MAPPER.readValue(input, Stock[].class);
    }
    static Stock stockByDate(String date)
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/stocks/GET/" + date);
            InputStream input = getConnect(url, "application/json");
            Stock[] output = JSON_MAPPER.readValue(input, Stock[].class);
            return output[0];
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static InputStream getConnect(URL target, String returnType) throws IOException
    {
        URLConnection connection = target.openConnection();
        HttpURLConnection http = (HttpURLConnection) connection;
        http.setRequestMethod("GET");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", returnType);
        return connection.getInputStream();
    }
    static Bitcoin bitcoinByDate(String date) throws ArrayIndexOutOfBoundsException
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/bitcoins/GET/" + date);
            URLConnection connection = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Accept", "application/json");
            InputStream input = connection.getInputStream();
            Bitcoin[] output = JSON_MAPPER.readValue(input, Bitcoin[].class);
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
            URL url = new URL("http://localhost:8080/api/tweets/GET/" + date);
            return getTweets(url);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static Tweet[] getTweets(URL url) throws IOException
    {
        URLConnection connection = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) connection;
        http.setRequestMethod("GET");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        InputStream input = connection.getInputStream();
        return JSON_MAPPER.readValue(input, Tweet[].class);
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
    private void setGUI(String date)
    {

        Stock stock = stockByDate(date);
        Bitcoin bitcoin = null;
        Tweet[] tweets = null;
        try
        {
            bitcoin = bitcoinByDate(date);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.err.println("No bitcoin data available for this date.");//TODO: work this into the GUI
        }
        try
        {
            tweets = tweetsByDate(date);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.err.println("No tweets available for this date.");//TODO: work this into the GUI
        }
        bitcoinChart.clear();
        stockChart.clear();
        stockChart.addData(new ModelChart("Stocks", new double[]{stock.getOpen(), stock.getClose()}));
        bitcoinChart.addData(new ModelChart("Bitcoins", new double[]{bitcoin.getOpen(), bitcoin.getClose()}));
        tweetPanel.removeAll();
        for(Tweet tweet : tweets)
        {
            JTextArea tweetField = new JTextArea(tweet.toString()+"\n");
            tweetField.setWrapStyleWord(true);
            tweetField.setLineWrap(true);
            tweetField.setEditable(false);
            tweetPanel.add(tweetField);
        }
        tweetPanel.revalidate();
        bitcoinChart.start();
        stockChart.start();
    }
    private String userGet(String url, String returnType) throws IOException//TODO: implement
    {
        String table = getTableSelect.getSelectedItem().toString();
        URL target = new URL("http://localhost:8080/api/" + table + "/GET/");
        getConnect(target, returnType);
        return "fix";
    }
    private void createUIComponents()
    {
        tweetScroller = new JScrollPane();
        tweetPanel = new JPanel();
        tweetPanel.setLayout(new BoxLayout(tweetPanel, BoxLayout.Y_AXIS));
        stockChart = new Chart();
        stockChart.addLegend("Open", new Color(52, 127, 208));
        stockChart.addLegend("Close", new Color(241, 80, 80));
        bitcoinChart = new Chart();
        bitcoinChart.addLegend("Open", new Color(52, 127, 208));
        bitcoinChart.addLegend("Close", new Color(241, 80, 80));
        String[] tables = {
                "tweets", "stocks", "bitcoin"
        };
        getTableSelect = new JComboBox<>(tables);
        getTableSelect.setEditable(true);
        dateButtons = new JPanel();
        dateButtons.setLayout(new BoxLayout(dateButtons, BoxLayout.Y_AXIS));
        dateScroller = new JScrollPane();
        dataPanel = new JPanel();
        ArrayList<Stock> dates = getDates();
        Vector<String> dateSelect = new Vector<>();
        for(Stock date : dates)
        {
            JButton button = new JButton(date.getDate().toString());
            button.addActionListener(e -> setGUI(button.getText()));
            dateSelect.add(button.getText());
            dateButtons.add(button);
        }
        dateComboBox = new JComboBox<>(dateSelect);
        setGUI(dates.get(0).getDate().toString());
        dateComboBox.setEditable(true);
        dateComboBox.addActionListener(e -> setGUI(dateComboBox.getSelectedItem().toString()));
    }
}
