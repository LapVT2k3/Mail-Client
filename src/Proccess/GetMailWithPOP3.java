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
        Properties properties = new Properties();
        properties.put("mail.pop3.host", "pop.gmail.com");
        properties.put("mail.pop3.port", "995");
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
        Store store = session.getStore();
        store.connect();
        Folder folderInbox = store.getFolder("INBOX");
        folderInbox.open(Folder.READ_ONLY);
        Message[] messages = folderInbox.getMessages();
        for (Message message : messages) {
            String from = "";
            InternetAddress[] addresses = (InternetAddress[]) message.getFrom();
            for (InternetAddress address : addresses) {
                from += address.getAddress();
            }
            Data.add(new DataInformation(message.getSubject(), message.getSentDate().toString(), from));
        }
        String[] headers = {"Tiêu Đề", "Người Gửi", "Thời Gian"};
        dataInfoModel = new DataInformationModel(headers, Data);
        store.close();
        return dataInfoModel;
    }
}
