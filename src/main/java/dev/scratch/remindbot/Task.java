package dev.scratch.remindbot;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Task {
    private String object;
    private ArrayList<Result> results;
    private boolean next_cursor;
    private boolean has_more;

    static class Result {
        private String object;
        private String id;
        private String created_time;
        private String last_edited_time;
        private Parent parent;
        private boolean archived;
        private Properties properties;

        public Properties getProperties() {
            return properties;
        }

        public String getId() {
            return id;
        }
    }

    static class Parent {
        private String type;
        private String database_id;
    }

    static class Properties {
        @SerializedName("Tags")
        private Tag tags;
        @SerializedName("Received")
        private CheckBox received;
        @SerializedName("Completed")
        private CheckBox completed;
        @SerializedName("Name")
        private TextField name;
        @SerializedName("Class")
        private SingleSelect className;
        @SerializedName("Remind_Date")
        private Date remindDate;

        public Date getRemindDate() {
            return remindDate;
        }

        public TextField getName() {
            return name;
        }

        public CheckBox getReceived() {
            return received;
        }

        public CheckBox getCompleted() {
            return completed;
        }
    }

    static class SingleSelect {
        private String id;
        private String type;
        private Select select;
    }

    static class Tag {
        private String id;
        private String type;
        @SerializedName("multi_select")
        private ArrayList<Select> multiSelect;

    }

    static class Select {
        private String id;
        private String name;
        private String color;

    }

    static class CheckBox {
        private String id;
        private String type;
        private boolean checkbox;

        public String getId() {
            return id;
        }

        public boolean isCheckbox() {
            return checkbox;
        }
    }

    static class TextField {
        private String id;
        private String type;
        private ArrayList<Title> title;

        public ArrayList<Title> getTitle() {
            return title;
        }
    }

    static class Title {
        private String type;
        private Text text;
        private Annotations annotations;
        @SerializedName("plain_text")
        private String plainText;
        private String href;

        public String getPlainText() {
            return plainText;
        }

        public String getHref() {
            return href;
        }
    }

    static class Text {
        private String content;
        private String link;

    }

    static class Annotations {
        private boolean bold;
        private boolean italic;
        private boolean strikethrough;
        private boolean code;
        private String color;
    }

    static class Date {
        private String id;
        private String type;
        private DateTime date;

        public Date(DateTime date, String type) {
            this.date = date;
            this.type = type;
        }


        public DateTime getDate() {
            return date;
        }
    }

    static class DateTime {
        private String start;
        private String end;

        public DateTime(String start) {
            this.start = start;
        }

        public String getStart() {
            return start;
        }
    }

    static class ChangeDate {

    }

    static class Property {

    }

    public ArrayList<Result> getResults() {
        return results;
    }
}

