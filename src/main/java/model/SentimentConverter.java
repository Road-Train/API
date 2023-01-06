package model;
import com.api.SentimentText;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter
public class SentimentConverter implements AttributeConverter<Sentiment,String>
{

    @Override
    public String convertToDatabaseColumn(Sentiment sentiment)
    {
        return "[" + sentiment.getSentimentText() + ", " + sentiment.getSentimentValue() + "]";
    }
    @Override
    public Sentiment convertToEntityAttribute(String s)
    {
        if (s == null || s.isBlank()) {
            return null;
        }
        s=s.substring(1,s.indexOf("]"));
        String[] parts = s.split(",");
        return new Sentiment(SentimentText.getValue(parts[0]),Double.parseDouble(parts[1].trim()));
    }
}
