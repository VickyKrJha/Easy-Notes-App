package com.vickysg.securenotesapp.model;

public class Note {
    private String Title;
    private String Content;
    private String ImageUrl;
    private String Date;
    private String Time;
    private String DateTime;

    public Note() {
    }

    public Note(String title, String content, String imageUrl, String date, String time, String dateTime) {
        Title = title;
        Content = content;
        ImageUrl = imageUrl;
        Date = date;
        Time = time;
        DateTime = dateTime;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }
}
