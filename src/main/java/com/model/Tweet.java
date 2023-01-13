package com.model;
import com.api.SentimentText;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.StringJoiner;
@JacksonXmlRootElement(localName = "tweet")
@Table(name = "tweets")
@IdClass(DateTimeId.class)
@Entity
public class Tweet implements Serializable
{
    @Id
    @Column(name = "Date")
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private LocalDate date;
    @Id
    @Column(name = "Time")
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "HH:mm:ss")
    private LocalTime time;
    @Column(name="Text")
    @JsonProperty
    private String content;
    @Convert(converter = SentimentConverter.class)
    @JsonProperty("sentiment")
    private Sentiment sentiment;
    public Tweet()
    {
    }
    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDate getDate()
    {
        return date;
    }
    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public LocalTime getTime()
    {
        return time;
    }
    @JsonSerialize(using = LocalTimeSerializer.class)
    public void setTime(LocalTime time)
    {
        this.time = time;
    }
    public Sentiment getSentiment()
    {
        return sentiment;
    }
    public void setSentiment(Sentiment sentiment)
    {
        this.sentiment = sentiment;
    }
    @JsonProperty("sentiment")
    public void createSentiment(Map<String,Object> sentiment)
    {
        if(!(sentiment.get("sentimentValue") instanceof Double))
        {
            this.setSentiment(new Sentiment(SentimentText.getValue((String) sentiment.get("sentimentText")), Double.parseDouble((String) sentiment.get("sentimentValue"))));
        }
        else
        {
            this.setSentiment(new Sentiment(SentimentText.getValue((String) sentiment.get("sentimentText")), (double)sentiment.get("sentimentValue")));
        }
    }
    public String toString()
    {
        StringJoiner tweetString = new StringJoiner("\n");
        tweetString.add("Date: "+date);
        tweetString.add("Time: "+time);
        tweetString.add("Content: "+content);
        tweetString.add("Sentiment: "+sentiment.toString());
        return tweetString.toString();
    }
}
