package Proccess;

import Interface.*;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

public class GetMailWithPOP3 {

    DataInformationModel dataInfoModel;
    List<DataInformation> Data = new LinkedList<>();

    public DataInformationModel getMailWithPOP3(String user, String password) throws NoSuchProviderException, MessagingException, IOException {
        Properties properties = System.getProperties();
        properties.put("mail.pop3.host", "pop.gmail.com");
        properties.put("mail.pop3.port", "995");
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
        Store store = session.getStore();
        store.connect();
        Folder[] folders = store.getDefaultFolder().list("*");
        for (Folder folder : folders) {
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }

            Message[] messages = folder.getMessages();

            for (Message message : messages) {
                String from = "";
                InternetAddress[] addresses = (InternetAddress[]) message.getFrom();
                for (InternetAddress address : addresses) {
                    from += address.getAddress();
                }
                String contentType = message.getContentType();
                String messageContent = "";
                if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    try {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    } catch (IOException | MessagingException ex) {
                        messageContent = "[Không thể tải nội dung]";
                    }
                }
                Data.add(new DataInformation(message.getSubject(), message.getSentDate().toString(), from));
                System.out.println("" + messageContent + "");
            }
        }
        String[] headers = {"Tiêu Đề", "Người Gửi", "Thời Gian"};
        dataInfoModel = new DataInformationModel(headers, Data);
        return dataInfoModel;
    }
}
