package com.api;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
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
import static com.api.Parser.XML_MAPPER;
@Path("/{table}")
public class API
{
    private Connection connection;
    @PathParam("table")//The table we're selecting.
    private String table;
    private Connection makeConnection() throws ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:C:\\Users\\trol1\\IdeaProjects\\API\\database.sqlite");
    }
    @Path("/GET/allData")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAllData(@Context HttpHeaders headers)
    {
        Response.ResponseBuilder responseBuilder;
        Response response;
        String output = null;
        try
        {
            connection = makeConnection();
            Statement statement = connection.createStatement();
            Format formatToUse = checkFormat(headers);
            ResultSet resultSet;
            switch(table)
            {
                case "tweets":
                    resultSet = statement.executeQuery("select Date ,Time, Text, sentiment from tweets order by Date, Time");
                    output = formatToUse.equals(Format.JSON) ? buildJSONTweetResponse(resultSet) : buildXMLTweetResponse(resultSet);
                    break;
                case "stocks":
                    resultSet = statement.executeQuery("select Date, Open, Close from stocks order by Date");
                    output = formatToUse.equals(Format.JSON) ? buildJSONStockResponse(resultSet) : buildXMLStockResponse(resultSet);
                    break;
                case "bitcoins":
                    resultSet = statement.executeQuery("select Date, Open, Close from bitcoin order by Date");
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
                    responseBuilder = Response.ok(output);
                    response = responseBuilder.build();
                }
                else
                {
                    List<Variant> variantList = new ArrayList<>();
                    responseBuilder = Response.notAcceptable(variantList);
                    response = responseBuilder.status(406).header("message", validationMessage).build();
                }
            }
            else
            {
                validationMessage = validateXML(String.valueOf(output));
                if(validationMessage.equals("Xml validated."))
                {
                    responseBuilder = Response.ok(output);
                    response = responseBuilder.build();
                }
                else
                {
                    List<Variant> variantList = new ArrayList<>();
                    responseBuilder = Response.notAcceptable(variantList);
                    response = responseBuilder.status(406).header("message", validationMessage).build();
                }
            }
            return response;
        }
        catch(IllegalArgumentException | ClassNotFoundException | SQLException e)
        {
            List<Variant> variantList = new ArrayList<>();
            responseBuilder = Response.notAcceptable(variantList);
            response = responseBuilder.status(406).header("message", e.getMessage()).build();
            return response;
        }
    }
    @Path("/GET/{date}")
    @GET
    @Produces({"application/json", "application/xml"})
    public String getDataByDate(@PathParam("date") String date, @Context HttpHeaders headers)
    {
        String formattedDate = "'" + date + "'";
        String output = null;
        try
        {
            connection = makeConnection();
            Statement statement = connection.createStatement();
            Format formatToUse = checkFormat(headers);
            ResultSet resultSet;
            switch(table)
            {
                case "tweets":
                    resultSet = statement.executeQuery("select Date ,Time, Text, sentiment from tweets where Date = " + formattedDate + " order by Time");
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
            connection.close();
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
    public Response insertJson(String data)
    {

        try
        {
            String validationString = validateJSON(data);
            if(! validationString.equals("Json validated."))
            {
                throw new IllegalWebFormatException(validationString);
            }
            switch(table)
            {
                case "tweets":
                    Tweet[] tweets = JSON_MAPPER.readValue(data, Tweet[].class);
                    insertTweetsIntoDatabase(List.of(tweets));
                    break;
                case "stocks":
                    Stock[] stocks = JSON_MAPPER.readValue(data, Stock[].class);
                    insertStocksIntoDatabase(List.of(stocks));
                    break;
                case "bitcoins":
                    Bitcoin[] bitcoins = JSON_MAPPER.readValue(data, Bitcoin[].class);
                    insertBitcoinIntoDatabase(List.of(bitcoins));
                    break;
            }
        }
        catch(SQLException | JsonProcessingException e)
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", e.getMessage()).build();
        }
        catch(ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return Response.ok().build();
    }
    @Path("/insert")
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    public Response insertXML(String data)
    {
        try
        {
            String validationString = validateXML(data);
            if(! validationString.equals("Xml validated."))
            {
                throw new IllegalWebFormatException(validationString);
            }
            switch(table)
            {
                case "tweets":
                    List<Tweet> tweets = XML_MAPPER.readValue(data, new TypeReference<List<Tweet>>() {});
                    insertTweetsIntoDatabase(tweets);
                    break;
                case "stocks":
                    List<Stock> stocks = XML_MAPPER.readValue(data, new TypeReference<List<Stock>>() {});
                    insertStocksIntoDatabase(stocks);
                    break;
                case "bitcoins":
                    List<Bitcoin> bitcoins = XML_MAPPER.readValue(data, new TypeReference<List<Bitcoin>>() {
                    });
                    insertBitcoinIntoDatabase(bitcoins);
                    break;
            }
        }
        catch(SQLException | JsonProcessingException | ClassNotFoundException e)
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", e.getMessage()).build();
        }
        return Response.ok().build();
    }
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response jsonEditData(String data)
    {
        try
        {
            String validationString = validateJSON(data);
            if(! validationString.equals("Json validated."))
            {
                throw new IllegalWebFormatException(validationString);

            }
            switch(table)
            {
                case "tweets":
                    Tweet[] tweets = JSON_MAPPER.readValue(data, Tweet[].class);
                    updateTweetData(List.of(tweets));
                    break;
                case "stocks":
                    Stock[] stocks = JSON_MAPPER.readValue(data, Stock[].class);
                    updateStockData(List.of(stocks));
                    break;
                case "bitcoins":
                    Bitcoin[] bitcoins = JSON_MAPPER.readValue(data, Bitcoin[].class);
                    updateBitcoinData(List.of(bitcoins));
                    break;
            }

        }
        catch(SQLException | JsonProcessingException | ClassNotFoundException e)
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", e.getMessage()).build();
        }
        return Response.ok().build();
    }
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_XML)
    @PUT
    public Response XMLEditData(String data)
    {
        try
        {
            String validationString = validateXML(data);
            if(! validationString.equals("Xml validated."))
            {
                throw new IllegalWebFormatException(validationString);

            }
            switch(table)
            {
                case "tweets":
                    List<Tweet> tweets = XML_MAPPER.readValue(data, new TypeReference<List<Tweet>>() {});
                    updateTweetData(tweets);
                    break;
                case "stocks":
                    List<Stock> stocks = XML_MAPPER.readValue(data, new TypeReference<List<Stock>>() {});
                    updateStockData(stocks);
                    break;
                case "bitcoins":
                    List<Bitcoin> bitcoins = XML_MAPPER.readValue(data, new TypeReference<List<Bitcoin>>() {});
                    updateBitcoinData(bitcoins);
                    break;
            }

        }
        catch(SQLException | JsonProcessingException | ClassNotFoundException e)
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", e.getMessage()).build();
        }
        return Response.ok().build();
    }
    @Path("/DELETE/{date}/{time}")
    @DELETE
    public Response deleteByDateTime(@PathParam("date") String date, @PathParam("time") String time)
    {
        if(!table.equals("tweets"))
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", "invalid request, time is specified but not valid.").build();
        }
        try
        {
            connection = makeConnection();
            String formatDate = "'"+date+"'";
            String formatTime = "'"+time+"'";
            String sql = "delete from tweets where Date = "+formatDate+" and Time = "+formatTime;
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            connection.close();
        }
        catch(SQLException | ClassNotFoundException e)
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", e.getMessage()).build();
        }
        return Response.ok().build();
    }
    @Path("/DELETE/{date}")
    @DELETE
    public Response deleteByDate(@PathParam("date") String date)
    {
        try
        {
            String sql = null;
            PreparedStatement preparedStatement;
            connection = makeConnection();
            switch(table)
            {
                case "stocks":
                    sql = "delete from stocks where Date = ?";
                    break;
                case "bitcoins":
                    sql = "delete from bitcoin where Date = ?";
                    break;
                case "tweets":
                    List<Variant> variantList = new ArrayList<>();
                    Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
                    return responseBuilder.status(406).header("message", "invalid request, Time required").build();
            }
            String formatDate = "'"+date+"'";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, formatDate);
            preparedStatement.executeUpdate();
            connection.close();
        }
        catch(SQLException | ClassNotFoundException e)
        {
            List<Variant> variantList = new ArrayList<>();
            Response.ResponseBuilder responseBuilder = Response.notAcceptable(variantList);
            return responseBuilder.status(406).header("message", e.getMessage()).build();
        }
        return Response.ok().build();
    }
    private void insertTweetsIntoDatabase(List<Tweet> tweets) throws SQLException, ClassNotFoundException
    {
        connection = makeConnection();
        String sql = "insert into tweets(Date,Time,Text,sentiment) values(?,?,?,?)";
        PreparedStatement preparedStatement;
        for(Tweet tweet : tweets)
        {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tweet.getDate().toString());
            preparedStatement.setString(2, tweet.getTime().toString());
            preparedStatement.setString(3, tweet.getContent());
            preparedStatement.setString(4, tweet.getSentiment().toString());
            preparedStatement.execute();
        }
        connection.close();
    }
    private void insertStocksIntoDatabase(List<Stock> stocks) throws SQLException, ClassNotFoundException
    {
        connection = makeConnection();
        String sql = "insert into stocks(Date,Open,Close) values(?,?,?)";
        PreparedStatement preparedStatement;
        for(Stock stock : stocks)
        {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, stock.getDate().toString());
            preparedStatement.setDouble(2, stock.getOpen());
            preparedStatement.setDouble(3, stock.getClose());
            preparedStatement.execute();
        }
        connection.close();
    }
    private void insertBitcoinIntoDatabase(List<Bitcoin> bitcoins) throws SQLException, ClassNotFoundException
    {
        connection = makeConnection();
        String sql = "insert into bitcoin(Date,Open,Close) values(?,?,?)";
        PreparedStatement preparedStatement;
        for(Bitcoin bitcoin : bitcoins)
        {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, bitcoin.getDate().toString());
            preparedStatement.setDouble(2, bitcoin.getOpen());
            preparedStatement.setDouble(3, bitcoin.getClose());
            preparedStatement.execute();
        }
        connection.close();
    }
    private void updateBitcoinData(List<Bitcoin> bitcoins) throws SQLException, ClassNotFoundException
    {
        connection = makeConnection();
        String sql = "update bitcoin set Open = ?, Close = ? where Date = ?";
        PreparedStatement preparedStatement;
        for(Bitcoin bitcoin : bitcoins)
        {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, bitcoin.getOpen());
            preparedStatement.setDouble(2, bitcoin.getClose());
            preparedStatement.setString(3, bitcoin.getDate().toString());
            preparedStatement.executeUpdate();
        }
        connection.close();
    }
    private void updateStockData(List<Stock> stocks) throws SQLException, ClassNotFoundException
    {
        connection = makeConnection();
        String sql = "update stocks set Open = ?, Close = ? where Date = ?";
        PreparedStatement preparedStatement;
        for(Stock stock : stocks)
        {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, stock.getOpen());
            preparedStatement.setDouble(2, stock.getClose());
            preparedStatement.setString(3, stock.getDate().toString());
            preparedStatement.executeUpdate();
        }
        connection.close();
    }
    private void updateTweetData(List<Tweet> tweets) throws SQLException, ClassNotFoundException
    {
        connection = makeConnection();
        String sql = "update tweets set Text = ?,sentiment = ? where Date = ? & Time = ?";
        PreparedStatement preparedStatement;
        for(Tweet tweet : tweets)
        {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tweet.getContent());
            preparedStatement.setString(2, tweet.getSentiment().toString());
            preparedStatement.setString(3, tweet.getDate().toString());
            preparedStatement.setString(4, tweet.getTime().toString());
            preparedStatement.executeUpdate();
        }
        connection.close();
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
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\tweetschema.json";
                break;
            case "bitcoin":
            case "stocks":
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\stockschema.json";
                break;
        }
        try
        {
            JsonNode json = JSON_MAPPER.readTree(toValidate);
            assert path != null;
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
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\tweet.xsd";
                break;
            case "bitcoin":
            case "stocks":
                path = "C:\\Users\\trol1\\IdeaProjects\\API\\src\\main\\resources\\schemas\\stock.xsd";
                break;
        }
        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            assert path != null;
            Schema schema = factory.newSchema(new File(path));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(toValidate)));
        }
        catch(SAXException | IOException e)
        {
            return e.getMessage();
        }
        return "Xml validated.";
    }
    private Format checkFormat(HttpHeaders headers)
    {
        String format = headers.getAcceptableMediaTypes().toString();
        format = format.substring(1, format.indexOf("]"));
        switch(format)
        {
            case "application/json":
                return Format.JSON;
            case "application/xml":
                return Format.XML;
            default:
                throw new IllegalWebFormatException("Invalid format requested. Accepted formats are application/json and application/xml.");
        }
    }

}