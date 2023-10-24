package Interface;

public class DataInformation {
    private String title;
    private String date;
    private String sender;

    public DataInformation(String title, String date, String sender) {
        this.title = title;
        this.date = date;
        this.sender = sender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
