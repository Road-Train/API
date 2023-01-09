package com.api;
public enum SentimentText
{
    POSITIVE("positive"),
    NEUTRAL("neutral"),
    NEGATIVE("negative");
    public final String text;
    SentimentText(String text)
    {
        this.text = text;
    }
    public static SentimentText getValue(String key)
    {
        key = key.replaceAll("'","");
        for(SentimentText sentimentText : SentimentText.values())
        {
            if(sentimentText.text.equals(key))
            {
                return sentimentText;
            }
        }
        return null;
    }
}
