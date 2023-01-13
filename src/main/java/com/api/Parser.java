package com.api;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.model.Bitcoin;
import com.model.Stock;
import com.model.Tweet;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
public class Parser
{
    public static final ObjectMapper XML_MAPPER = new XmlMapper();
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    @JacksonXmlElementWrapper(localName = "tweets")
    public static ArrayList<Tweet> tweets = new ArrayList<>();
    public static ArrayList<Stock> stocks = new ArrayList<>();
    public static ArrayList<Bitcoin> bitcoins = new ArrayList<>();

    static
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        XML_MAPPER.setDateFormat(simpleDateFormat);
        XML_MAPPER.registerModule(new JavaTimeModule());
        JSON_MAPPER.setDateFormat(simpleDateFormat);
        JSON_MAPPER.registerModule(new JavaTimeModule());
    }
    public static void addTweet(Tweet tweet)
    {
        tweets.add(tweet);
    }
    public static ArrayList<Tweet> getTweets()
    {
        return tweets;
    }
    public static void addStock(Stock stock)
    {
        stocks.add(stock);
    }
    public static ArrayList<Stock> getStocks()
    {
        return stocks;
    }
    public static void addCoin(Bitcoin bitcoin)
    {
        bitcoins.add(bitcoin);
    }
    public static ArrayList<Bitcoin> getBitcoins()
    {
        return bitcoins;
    }
    public static String serialize(Serializable serializable, Format format)
    {
        return format.equals(Format.JSON) ? serialize(serializable, JSON_MAPPER) : serialize(serializable, XML_MAPPER);
    }

    private static String serialize(Serializable serializable, ObjectMapper objectMapper)
    {
        try
        {
            return objectMapper.writeValueAsString(serializable);
        }
        catch(IOException e)
        {
            throw new UncheckedIOException("Oh no!", e);
        }
    }

    public static Serializable deserialize(String serializedString, Format format, SerialFormat serialFormat)
    {
        return format.equals(Format.JSON) ? deserialize(serializedString, JSON_MAPPER, serialFormat) : deserialize(serializedString, XML_MAPPER, serialFormat);
    }
    private static Serializable deserialize(String serializedString, ObjectMapper mapper, SerialFormat serialFormat)
    {
        try
        {
            switch(serialFormat)
            {
                case TWEET:
                    return mapper.readValue(serializedString, Tweet.class);
                case STOCK:
                    return mapper.readValue(serializedString, Stock.class);
                case BITCOIN:
                    return mapper.readValue(serializedString, Bitcoin.class);
                default:
                    return null;
            }
        }
        catch(JsonProcessingException e)
        {
            throw new UncheckedIOException("Bad!", e);
        }
    }

}
