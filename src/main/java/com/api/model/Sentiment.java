package com.api.model;
import com.api.SentimentText;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;

import java.io.Serializable;
public class Sentiment implements Serializable
{
    @JsonProperty
    private String sentimentText;
    @JsonProperty
    private double sentimentValue;
    public Sentiment(SentimentText sentimentText, double sentimentValue)
    {
        this.sentimentText = sentimentText.text;
        this.sentimentValue = sentimentValue;
    }
    public String getSentimentText()
    {
        return sentimentText;
    }
    public void setSentimentText(String sentimentText)
    {
        this.sentimentText = sentimentText;
    }
    public double getSentimentValue()
    {
        return sentimentValue;
    }
    public void setSentimentValue(double sentimentValue)
    {
        this.sentimentValue = sentimentValue;
    }
    @Override
    @Column(name="sentiment")
    public String toString()
    {
        return "['"+ sentimentText+"', "+sentimentValue+ "]";
    }
}
