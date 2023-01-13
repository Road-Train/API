package com.api.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
@JacksonXmlRootElement(localName = "stock")
@Table(name = "stocks")
@Entity
public class Stock implements Serializable
{
    @JacksonXmlProperty(isAttribute = true)
    @Id
    @Column(name = "Date")
    private LocalDate date;
    @JsonProperty
    @Column(name = "Open")
    private double open;
    @JsonProperty
    @Column(name = "Close")
    private double close;
    public Stock()
    {
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
    public double getOpen()
    {
        return open;
    }
    public void setOpen(double open)
    {
        this.open = open;
    }
    public double getClose()
    {
        return close;
    }
    public void setClose(double close)
    {
        this.close = close;
    }
}
