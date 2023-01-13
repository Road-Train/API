import com.api.Format;
import com.api.Parser;
import com.api.SentimentText;
import com.api.SerialFormat;
import com.model.Bitcoin;
import com.model.Sentiment;
import com.model.Stock;
import com.model.Tweet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
public class ParserTest
{
    Tweet tweet;
    Bitcoin bitcoin;
    Stock stock;
    @BeforeEach
    void setup()
    {
        tweet = new Tweet();
        tweet.setContent("the bird is freed");
        LocalDate date = LocalDate.of(2022, 10, 28);
        LocalTime time = LocalTime.of(3,49,11);
        tweet.setDate(date);
        tweet.setTime(time);
        Sentiment sentiment = new Sentiment(SentimentText.POSITIVE,0.70848316);
        tweet.setSentiment(sentiment);
        bitcoin = new Bitcoin();
        bitcoin.setDate(date);
        bitcoin.setOpen(465.864014);
        bitcoin.setClose(457.334015);
        stock = new Stock();
        stock.setDate(date);
        stock.setOpen(465.864014);
        stock.setClose(457.334015);
        Parser.addTweet(tweet);
        Parser.addStock(stock);
        Parser.addCoin(bitcoin);
    }
    @Test
    void serializeXML()
    {
        for(Tweet tweetlist : Parser.getTweets())
        {
            System.out.println(Parser.serialize(tweetlist, Format.XML));
        }
        System.out.println(Parser.serialize(tweet, Format.XML));
        System.out.println(Parser.serialize(bitcoin, Format.XML));
        System.out.println(Parser.serialize(stock, Format.XML));
    }
    @Test
    void deserializeXML()
    {
        String tweetSerial = Parser.serialize(tweet, Format.XML);
        String bitcoinSerial = Parser.serialize(bitcoin, Format.XML);
        String stockSerial = Parser.serialize(stock, Format.XML);
        Tweet newTweet = (Tweet) Parser.deserialize(tweetSerial,Format.XML, SerialFormat.TWEET);
        Stock newStock = (Stock) Parser.deserialize(bitcoinSerial,Format.XML, SerialFormat.STOCK);
        Bitcoin newCoin = (Bitcoin) Parser.deserialize(stockSerial,Format.XML, SerialFormat.BITCOIN);
        System.out.println(newTweet.toString());
        System.out.println(newStock.toString());
        System.out.println(newCoin.toString());
    }
    @Test
    void serializeJSON()
    {
        for(Tweet tweetlist : Parser.getTweets())
        {
            System.out.println(Parser.serialize(tweetlist, Format.JSON));
        }
        System.out.println(Parser.serialize(tweet, Format.JSON));
        System.out.println(Parser.serialize(bitcoin, Format.JSON));
        System.out.println(Parser.serialize(stock, Format.JSON));
    }
    @Test
    void deserializeJSON()
    {
        String tweetSerial = Parser.serialize(tweet, Format.JSON);
        String bitcoinSerial = Parser.serialize(bitcoin, Format.JSON);
        String stockSerial = Parser.serialize(stock, Format.JSON);
        Tweet newTweet = (Tweet) Parser.deserialize(tweetSerial,Format.JSON, SerialFormat.TWEET);
        Bitcoin newCoin = (Bitcoin) Parser.deserialize(bitcoinSerial,Format.JSON, SerialFormat.BITCOIN);
        Stock newStock = (Stock) Parser.deserialize(stockSerial,Format.JSON, SerialFormat.STOCK);
        System.out.println(newTweet.toString());
        System.out.println(newStock.toString());
        System.out.println(newCoin.toString());
    }
}
