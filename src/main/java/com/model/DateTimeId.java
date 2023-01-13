package com.model;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
public class DateTimeId implements Serializable
{
    private LocalDate date;
    private LocalTime time;
    public DateTimeId(LocalDate date, LocalTime time)
    {
        this.date = date;
        this.time = time;
    }
    public DateTimeId()
    {

    }
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        DateTimeId that = (DateTimeId) o;
        return date.equals(that.date) && time.equals(that.time);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(date, time);
    }
}
