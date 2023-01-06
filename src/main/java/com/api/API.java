package com.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import model.Bitcoin;
import model.Stock;
import model.Tweet;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.api.Parser.JSON_MAPPER;
@ApplicationPath("/api")
@Path("/{table}")
public class API
{
    private Connection connection;
    private final List<FileContent> inMemoryFileStore = new ArrayList();
    @PathParam("table")//The table we're selecting.
    private String table;
    private Statement makeConnection() throws ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\trol1\\IdeaProjects\\API\\database.sqlite");
        return connection.createStatement();
    }
    @Path("/{format}/allData")
    @GET
    @Produces({"application/xml", "application/json"})
    public String getAllData(@PathParam("format") String format)
    {
        String output = null;
        try
        {
            Statement statement = makeConnection();
            Format formatToUse = checkFormat(format);
            ResultSet resultSet;
            switch(table)
            {
                case "tweets":
                    resultSet = statement.executeQuery("select Date ,Time, Text, sentiment from tweets");
                    output = formatToUse.equals(Format.JSON) ? buildJSONTweetResponse(resultSet) : buildXMLTweetResponse(resultSet);
                    break;
                case "stocks":
                    resultSet = statement.executeQuery("select Date, Open, Close from stocks");
                    output = formatToUse.equals(Format.JSON) ? buildJSONStockResponse(resultSet) : buildXMLStockResponse(resultSet);
                    break;
                case "bitcoins":
                    resultSet = statement.executeQuery("select Date, Open, Close from bitcoin");
                    output = formatToUse.equals(Format.JSON) ? buildJSONBitcoinResponse(resultSet) : buildXMLBitcoinResponse(resultSet);
                    break;
            }
            connection.close();
            String validationMessage;
            if(formatToUse.equals(Format.JSON))
            {
                validationMessage = validateJSON(output);
                if(validationMessage.equals("Json validated."))
                {
                    return output;
                }
                else
                {
                    return validationMessage;
                }
            }
            else
            {
                validationMessage = validateXML(String.valueOf(output));
                if(validationMessage.equals("Xml validated."))
                {
                    return output;
                }
                else
                {
                    return validationMessage;
                }
            }
        }
        catch(ClassNotFoundException | SQLException e)
        {
            output = String.valueOf(e);
        }
        return output;
    }
    @Path("/{format}/{date}")
    @GET
    @Produces({"application/json", "application/xml"})
    public String getDataByDate(@PathParam("format") String format, @PathParam("date") String date)
    {
        String formattedDate = "'" + date + "'";
        String output = null;
        try
        {
            Statement statement = makeConnection();
            Format formatToUse = checkFormat(format);
            ResultSet resultSet;
            switch(table)
            {
                case "tweets":
                    resultSet = statement.executeQuery("select Date ,Time, Text, sentiment from tweets where Date = " + formattedDate);
                    output = formatToUse.equals(Format.JSON) ? buildJSONTweetResponse(resultSet) : buildXMLTweetResponse(resultSet);
                    break;
                case "stocks":
                    resultSet = statement.executeQuery("select Date, Open, Close from stocks where Date = " + formattedDate);
                    output = formatToUse.equals(Format.JSON) ? buildJSONStockResponse(resultSet) : buildXMLStockResponse(resultSet);
                    break;
                case "bitcoins":
                    resultSet = statement.executeQuery("select Date, Open, Close from bitcoin where Date = " + formattedDate);
                    output = formatToUse.equals(Format.JSON) ? buildJSONBitcoinResponse(resultSet) : buildXMLBitcoinResponse(resultSet);
                    break;
            }
        }
        catch(SQLException | ClassNotFoundException e)
        {
            output = String.valueOf(e);
        }
        return output;
    }
    @Path("/insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String insertJson()
    {

        try
        {
            Statement statement = makeConnection();
        }
        catch(SQLException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return "fix this";
    }
    @Path("/insert")
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public String insertXML()
    {
        try
        {
            Statement statement = makeConnection();
        }
        catch(SQLException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return "fix this";
    }
    @Path("edit")
    @POST
    public String editData()
    {
        try
        {
            Statement statement = makeConnection();
        }
        catch(SQLException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return "fix this";
    }
    private String buildJSONTweetResponse(ResultSet resultSet) throws SQLException
    {
        StringBuilder output = new StringBuilder("[");
        StringJoiner joiner = new StringJoiner(",\n");
        while(resultSet.next())
        {
            Tweet tweet = buildTweet(resultSet);
            joiner.add(Parser.serialize(tweet, Format.JSON));
        }
        output.append(joiner).append("]");
        return output.toString();
    }
    private String buildXMLTweetResponse(ResultSet resultSet) throws SQLException
    {
        StringBuilder output = new StringBuilder("<tweets>");
        StringJoiner joiner = new StringJoiner("\n");
        while(resultSet.next())
        {
            Tweet tweet = buildTweet(resultSet);
            joiner.add(Parser.serialize(tweet, Format.XML));
        }
        output.append(joiner).append("</tweets>");
        return output.toString();
    }
    private Tweet buildTweet(ResultSet resultSet) throws SQLException
    {
        Tweet tweet = new Tweet();
        tweet.setDate(LocalDate.parse(resultSet.getString(1)));
        tweet.setTime(LocalTime.parse(resultSet.getString(2)));
        tweet.setContent(resultSet.getString(3));
        String sentiment = resultSet.getString(4);
        String sentimentText = sentiment.substring(1, sentiment.indexOf(","));
        Double sentimentValue = Double.parseDouble(sentiment.substring(sentiment.indexOf(" "), sentiment.indexOf("]")));
        HashMap<String, Object> sentimentMap = new HashMap<>();
        sentimentMap.put("sentimentText", sentimentText);
        sentimentMap.put("sentimentValue", sentimentValue);
        tweet.createSentiment(sentimentMap);
        return tweet;
    }
    private String buildJSONStockResponse(ResultSet resultSet) throws SQLException
    {
        StringBuilder output = new StringBuilder("[");
        StringJoiner joiner = new StringJoiner(",\n");
        while(resultSet.next())
        {
            Stock stock = buildStock(resultSet);
            joiner.add(Parser.serialize(stock, Format.JSON));
        }
        output.append(joiner);
        output.append("]");
        return output.toString();
    }
    private String buildXMLStockResponse(ResultSet resultSet) throws SQLException
    {
        StringBuilder output = new StringBuilder("<stocks>");
        StringJoiner joiner = new StringJoiner("\n");
        while(resultSet.next())
        {
            Stock stock = buildStock(resultSet);
            joiner.add(Parser.serialize(stock, Format.XML));
        }
        output.append(joiner);
        output.append("</stocks>");
        return output.toString();
    }
    private Stock buildStock(ResultSet resultSet) throws SQLException
    {
        Stock stock = new Stock();
        stock.setDate(LocalDate.parse(resultSet.getString(1)));
        stock.setOpen(resultSet.getDouble(2));
        stock.setClose(resultSet.getDouble(3));
        return stock;
    }
    private String buildJSONBitcoinResponse(ResultSet resultSet) throws SQLException
    {
        StringBuilder output = new StringBuilder("[");
        StringJoiner joiner = new StringJoiner(",\n");
        while(resultSet.next())
        {
            Bitcoin coin = buildBitcoin(resultSet);
            joiner.add(Parser.serialize(coin, Format.JSON));
        }
        output.append(joiner);
        output.append("]");
        return output.toString();
    }
    private String buildXMLBitcoinResponse(ResultSet resultSet) throws SQLException
    {
        StringBuilder output = new StringBuilder("<bitcoins>");
        StringJoiner joiner = new StringJoiner("\n");
        while(resultSet.next())
        {
            Bitcoin coin = buildBitcoin(resultSet);
            joiner.add(Parser.serialize(coin, Format.XML));
        }
        output.append(joiner);
        output.append("</bitcoins>");
        return output.toString();
    }
    private Bitcoin buildBitcoin(ResultSet resultSet) throws SQLException
    {
        Bitcoin coin = new Bitcoin();
        coin.setDate(LocalDate.parse(resultSet.getString(1)));
        coin.setOpen(resultSet.getDouble(2));
        coin.setClose(resultSet.getDouble(3));
        return coin;
    }

    private String validateJSON(String toValidate)
    {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        String path = null;
        switch(table)
        {
            case "tweets":
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\tweetschema.xsd";
                break;
            case "bitcoin":
            case "stocks":
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\stockschema.xsd";
                break;
        }
        try
        {
            JsonNode json = JSON_MAPPER.readTree(toValidate);
            String schemaString = new String(Files.readAllBytes(Paths.get(path)));
            JsonSchema schema = factory.getSchema(schemaString);
            Set<ValidationMessage> validationMessages = schema.validate(json);
            if(! (validationMessages.isEmpty()))
            {
                StringBuilder output = new StringBuilder();
                validationMessages.forEach(validationMessage -> output.append(validationMessage.getMessage()).append("\n"));
                return output.toString();
            }
            return "Json validated.";
        }
        catch(IOException e)
        {
            return e.getMessage();
        }
    }
    private String validateXML(String toValidate)
    {
        String path = null;
        switch(table)
        {
            case "tweets":
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\tweets.xsd";
                break;
            case "bitcoin":
            case "stocks":
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\stocks.xsd";
                break;
        }
        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(path));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(toValidate)));
        }
        catch(SAXException | IOException e)
        {
            return e.getMessage();
        }
        return "Xml validated";
    }
    private Format checkFormat(String format)
    {
        return format.equalsIgnoreCase("json") ? Format.JSON : Format.XML;
    }
}