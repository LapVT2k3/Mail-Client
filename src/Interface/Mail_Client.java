package Interface;

import com.sun.mail.util.BASE64DecoderStream;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public class Mail_Client extends javax.swing.JFrame {

    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        userName = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public void ReadMail(int n) throws IOException {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", userName, password);
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            Message[] messages = folderInbox.getMessages();
            Message msg = messages[n];
            String contentType = msg.getContentType();
            String messageContent = "";
            if (contentType.contains("multipart")) {
                Multipart multiPart = (Multipart) msg.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    messageContent = part.getContent().toString();
                }
            } else if (contentType.contains("text/html")) {
                try {
                    BASE64DecoderStream content = (BASE64DecoderStream) msg.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                } catch (IOException | MessagingException ex) {
                    messageContent = "[Không thể tải nội dung]";
                }
            }
            System.out.println("Nội dung: " + messageContent);
            ePnlNoiDung.setContentType("text/html");
            ePnlNoiDung.setText(messageContent);
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("Lỗi 1: " + ex.getMessage());
        } catch (MessagingException ex) {
            System.out.println("Lỗi 2: " + ex.getMessage());
        }
    }

    public static void sendEmail(Properties smtpProperties, String toAddress,
            String subject, String message, File[] attachFiles)
            throws AddressException, MessagingException, IOException {

        final String userName = smtpProperties.getProperty("mail.user");
        final String password = smtpProperties.getProperty("mail.password");

        // creates a new session with an authenticator
        Authenticator auth;
        auth = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(smtpProperties, auth);
        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (File aFile : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                try {
                    attachPart.attachFile(aFile);
                } catch (IOException ex) {
                    throw ex;
                }

                multipart.addBodyPart(attachPart);
            }
        }
        // sets the multi-part as e-mail's content
        msg.setContent(multipart);
        // sends the e-mail
        Transport.send(msg);
    }

    private boolean validateFields() {
        if (txtToi.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập địa chỉ nhận!",
                    "Thông báo", JOptionPane.ERROR_MESSAGE);
            txtToi.requestFocus();
            return false;
        }

        if (txtSub.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập chủ đề email!",
                    "Thông báo", JOptionPane.ERROR_MESSAGE);
            txtSub.requestFocus();
            return false;
        }

        if (txtNoiDung.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập nội dung email!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            txtNoiDung.requestFocus();
            return false;
        }

        return true;
    }
    private File configFile = new File("smtp.properties");
    private Properties configProps;

    public Properties loadProperties() throws IOException {
        Properties defaultProps = new Properties();
        // sets default properties
        defaultProps.setProperty("mail.smtp.host", "smtp.gmail.com");
        defaultProps.setProperty("mail.smtp.port", "587");
        defaultProps.setProperty("mail.user", getUserName());
        defaultProps.setProperty("mail.password", getPassword());
        defaultProps.setProperty("mail.smtp.starttls.enable", "true");
        defaultProps.setProperty("mail.smtp.auth", "true");

        configProps = new Properties(defaultProps);

        // loads properties from file
        if (configFile.exists()) {
            InputStream inputStream = new FileInputStream(configFile);
            configProps.load(inputStream);
            inputStream.close();
        }

        return configProps;
    }

    public Mail_Client(String user, String pass) throws MessagingException, NoSuchProviderException, IOException {
        initComponents();
        System.out.println("user: " + user);
        System.out.println("pass: " + pass);
        lblUser.setText(user);
        Proccess.GetMailWithPOP3 gm = new Proccess.GetMailWithPOP3();
        jTable1.setModel(gm.getMailWithPOP3(user, pass));
        pnlHopThuDen.setVisible(true);
        pnlSoanThu.setVisible(false);
        pnlNoiDung.setVisible(false);
    }

    public Mail_Client() {
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        pnlDieuKhien = new javax.swing.JPanel();
        btnLogout = new javax.swing.JButton();
        btnHopThuDen = new javax.swing.JButton();
        btnSoanThu = new javax.swing.JButton();
        btnThoat = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        btnGopY = new javax.swing.JButton();
        lblUser = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        pnlSoanThu = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtToi = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSub = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnGui = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtNoiDung = new javax.swing.JEditorPane();
        pnlHopThuDen = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        pnlNoiDung = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtTieuDe = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtNguoiGui = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        ePnlNoiDung = new javax.swing.JEditorPane();
        btnTraLoi = new javax.swing.JButton();
        btnChuyenTiep = new javax.swing.JButton();

        jLabel5.setText("jLabel5");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnLogout.setText("Đăng xuất");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnHopThuDen.setText("Hộp thư đến");
        btnHopThuDen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHopThuDenActionPerformed(evt);
            }
        });

        btnSoanThu.setText("Soạn thư");
        btnSoanThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSoanThuActionPerformed(evt);
            }
        });

        btnThoat.setText("Thoát");
        btnThoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThoatActionPerformed(evt);
            }
        });

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/MAIL_TECHTRA.png"))); // NOI18N

        btnGopY.setText("Góp ý");
        btnGopY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGopYActionPerformed(evt);
            }
        });

        lblUser.setFont(new java.awt.Font("Times New Roman", 0, 10)); // NOI18N
        lblUser.setText(".");

        javax.swing.GroupLayout pnlDieuKhienLayout = new javax.swing.GroupLayout(pnlDieuKhien);
        pnlDieuKhien.setLayout(pnlDieuKhienLayout);
        pnlDieuKhienLayout.setHorizontalGroup(
            pnlDieuKhienLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDieuKhienLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDieuKhienLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLogout)
                    .addComponent(btnHopThuDen)
                    .addComponent(btnSoanThu)
                    .addComponent(btnThoat)
                    .addComponent(btnGopY)
                    .addComponent(lblUser))
                .addGap(0, 74, Short.MAX_VALUE))
        );
        pnlDieuKhienLayout.setVerticalGroup(
            pnlDieuKhienLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDieuKhienLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addComponent(btnLogout)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHopThuDen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSoanThu)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGopY)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnThoat)
                .addGap(90, 90, 90))
        );

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 204));
        jLabel1.setText("SOẠN THƯ");

        jLabel2.setText("Tới");

        jLabel3.setText("Chủ đề:");

        jLabel4.setText("Nội dung:");

        btnGui.setText("Gửi");
        btnGui.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuiActionPerformed(evt);
            }
        });

        jScrollPane4.setViewportView(txtNoiDung);

        javax.swing.GroupLayout pnlSoanThuLayout = new javax.swing.GroupLayout(pnlSoanThu);
        pnlSoanThu.setLayout(pnlSoanThuLayout);
        pnlSoanThuLayout.setHorizontalGroup(
            pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSoanThuLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtToi))
                    .addGroup(pnlSoanThuLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSub))
                    .addGroup(pnlSoanThuLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                .addGap(183, 183, 183)
                .addComponent(btnGui)
                .addContainerGap(170, Short.MAX_VALUE))
            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSoanThuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(141, 141, 141))
        );
        pnlSoanThuLayout.setVerticalGroup(
            pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtToi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSub, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnGui)
                .addGap(17, 17, 17))
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 0, 51));
        jLabel10.setText("HỘP THƯ ĐẾN");

        javax.swing.GroupLayout pnlHopThuDenLayout = new javax.swing.GroupLayout(pnlHopThuDen);
        pnlHopThuDen.setLayout(pnlHopThuDenLayout);
        pnlHopThuDenLayout.setHorizontalGroup(
            pnlHopThuDenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
            .addGroup(pnlHopThuDenLayout.createSequentialGroup()
                .addGap(117, 117, 117)
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlHopThuDenLayout.setVerticalGroup(
            pnlHopThuDenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHopThuDenLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSoanThu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlHopThuDen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSoanThu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlHopThuDen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(204, 0, 153));
        jLabel6.setText("NỘI DUNG THƯ");

        jLabel7.setText("Tiêu đề");

        jLabel8.setText("Người gửi");

        jScrollPane3.setViewportView(ePnlNoiDung);

        btnTraLoi.setText("Trả lời");
        btnTraLoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTraLoiActionPerformed(evt);
            }
        });

        btnChuyenTiep.setText("Chuyển tiếp");
        btnChuyenTiep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChuyenTiepActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlNoiDungLayout = new javax.swing.GroupLayout(pnlNoiDung);
        pnlNoiDung.setLayout(pnlNoiDungLayout);
        pnlNoiDungLayout.setHorizontalGroup(
            pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNoiDungLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(pnlNoiDungLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTieuDe))
                    .addGroup(pnlNoiDungLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNguoiGui)))
                .addContainerGap())
            .addGroup(pnlNoiDungLayout.createSequentialGroup()
                .addGap(136, 136, 136)
                .addComponent(jLabel6)
                .addGap(0, 88, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNoiDungLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnTraLoi)
                .addGap(32, 32, 32)
                .addComponent(btnChuyenTiep)
                .addGap(112, 112, 112))
        );
        pnlNoiDungLayout.setVerticalGroup(
            pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNoiDungLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtTieuDe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtNguoiGui, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTraLoi)
                    .addComponent(btnChuyenTiep))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlDieuKhien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlNoiDung, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlDieuKhien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlNoiDung, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        pnlNoiDung.setVisible(true);
        AbstractTableModel model = (AbstractTableModel) jTable1.getModel();
        txtTieuDe.setText(model.getValueAt(jTable1.getSelectedRow(), 0).toString());
        txtNguoiGui.setText(model.getValueAt(jTable1.getSelectedRow(), 1).toString());
        int n = jTable1.getSelectedRow();
        System.out.println("Hang: " + n);
        try {
            ReadMail(n);
        } catch (IOException ex) {
            System.out.println("Lỗi: " + ex.getMessage());
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void btnGuiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuiActionPerformed
        if (!validateFields()) {
            return;
        }
        String toAddress = txtToi.getText();
        String subject = txtSub.getText();
        String message = txtNoiDung.getText();
        File[] attachFiles = null;
        try {
            System.out.println("user: " + getUserName());
            System.out.println("pass: " + getPassword());
            Properties smtpProperties = loadProperties();
            sendEmail(smtpProperties, toAddress, subject, message, attachFiles);
            JOptionPane.showMessageDialog(this,
                    "Gửi email thành công!");
        } catch (HeadlessException | IOException | MessagingException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(),
                    "Thông báo", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_btnGuiActionPerformed

    private void btnHopThuDenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHopThuDenActionPerformed
        // TODO add your handling code here:
        pnlHopThuDen.setVisible(true);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(false);
    }//GEN-LAST:event_btnHopThuDenActionPerformed

    private void btnSoanThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSoanThuActionPerformed
        // TODO add your handling code here:
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
        txtToi.setText("");
        txtSub.setText("");
        txtNoiDung.setText("");
    }//GEN-LAST:event_btnSoanThuActionPerformed

    private void btnThoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThoatActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnThoatActionPerformed

    private void btnTraLoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTraLoiActionPerformed
        // TODO add your handling code here:
        txtToi.setText(txtNguoiGui.getText());
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
    }//GEN-LAST:event_btnTraLoiActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
        setUserName("");
        setPassword("");
        this.dispose();
        Login frmLogin = new Login();
        frmLogin.setVisible(true);

    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnGopYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGopYActionPerformed
        // TODO add your handling code here:
        txtToi.setText("trunglaptest1@gmail.com");
        txtSub.setText("Góp ý về chương trình Mail Client!");
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);

    }//GEN-LAST:event_btnGopYActionPerformed

    private void btnChuyenTiepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChuyenTiepActionPerformed
        // TODO add your handling code here:
        txtNoiDung.setText(ePnlNoiDung.getText());
        txtSub.setText(txtTieuDe.getText());
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
        txtNoiDung.setContentType("text/html");
        txtNoiDung.setText(ePnlNoiDung.getText());
    }//GEN-LAST:event_btnChuyenTiepActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Mail_Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Mail_Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Mail_Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Mail_Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Mail_Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChuyenTiep;
    private javax.swing.JButton btnGopY;
    private javax.swing.JButton btnGui;
    private javax.swing.JButton btnHopThuDen;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSoanThu;
    private javax.swing.JButton btnThoat;
    private javax.swing.JButton btnTraLoi;
    private javax.swing.JEditorPane ePnlNoiDung;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlDieuKhien;
    private javax.swing.JPanel pnlHopThuDen;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlSoanThu;
    private javax.swing.JTextField txtNguoiGui;
    private javax.swing.JEditorPane txtNoiDung;
    private javax.swing.JTextField txtSub;
    private javax.swing.JTextField txtTieuDe;
    private javax.swing.JTextField txtToi;
    // End of variables declaration//GEN-END:variables
}
