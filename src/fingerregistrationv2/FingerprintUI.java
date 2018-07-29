package fingerregistrationv2;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.json.JSONArray;
import org.json.JSONObject;

public class FingerprintUI extends javax.swing.JFrame implements fpLibrary
{
  private static File f;
  java.awt.Image img;
  String username;
  String lastnameString;
  String employeeCodeString;
  private final Timer timer = new Timer();
  private static Preferences prefs;
  private byte[] refbuf = new byte['Ȁ'];
  private int[] refsize = new int[1];
  private byte[] matbuf = new byte['Ȁ'];
  private int[] matsize = new int[1];
  String usernameStored;
  String lastnameStored; String empCodeStored; String adminPasswordStored; byte[] fingerPrintValueByte = new byte['Ȁ'];
  
  int fingerPrintValueByteSizeEmpty;
  byte[] fingerPrintValueByteEmpty = new byte['Ȁ'];
  private String fileName = "ConfigFingerPrint.txt";
  
  private BufferedReader bufferedReader;
  private ArrayList<String> returnedIps = new ArrayList();
  private ArrayList<String> ipNames = new ArrayList();
  private ArrayList<String> ipAddresses = new ArrayList();
  


  static FingerprintUI fingerprintUI = new FingerprintUI();
  Configs configs = new Configs();
  DatabaseConnection databaseConnection = new DatabaseConnection();
  java.sql.Connection connection;
  java.sql.Statement stmt;
  String selectedBranch;
  ReadAndWriteToFile fileWrite = new ReadAndWriteToFile();
  ArrayList<String> ipsFromFile = new ArrayList();
  private JComboBox branchesList;
  
  public FingerprintUI() {
    setUndecorated(true);
    setExtendedState(6);
    initComponents();
    setAlwaysOnTop(true);
    

    enableReader.setVisible(false);
    prefs = Preferences.userRoot().node("FingerprintUI");
    String myLibraryPath = System.getProperty("user.dir");
    System.setProperty("java.library.path", myLibraryPath);
    fpLibrary.INSTANCE.OpenDevice(0, 0, 0);
    
    usernameStored = prefs.get(Configs.username, "");
    lastnameStored = prefs.get(Configs.lastname, "");
    System.out.println(usernameStored + "====" + lastnameStored);
    empCodeStored = prefs.get("empCode", "");
    adminPasswordStored = prefs.get("adminPassword", "");
    


    String baseurl = prefs.get(Configs.BASE_URL, "");
    
    try
    {
      ipsFromFile = readFromFile();
      
      ipLabel.setVisible(false);
      serverIPEditText.setVisible(false);
      updateServerIP.setVisible(false);
      
      if (ipsFromFile.size() > 0)
      {

        for (int x = 0; x < ipsFromFile.size(); x++) {
          if (!((String)ipsFromFile.get(x)).isEmpty()) {
            System.out.println("file ips " + (String)ipsFromFile.get(x));
            String[] ipAddSplit = ((String)ipsFromFile.get(x)).split(":");
            ipNames.add(ipAddSplit[0]);
            ipAddresses.add(ipAddSplit[1].replaceAll(" ", ""));
          }
        }
        
        addToComboBox(ipNames);
      } else {
        JOptionPane.showMessageDialog(this, "Error fetching ips");
        ipLabel.setVisible(true);
        serverIPEditText.setVisible(true);
        updateServerIP.setVisible(true);
      }
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
      WriteToFile("", "");
      JOptionPane.showMessageDialog(this, "Please set connection ips");
      ipLabel.setVisible(true);
      serverIPEditText.setVisible(true);
      updateServerIP.setVisible(true);
    }
    
    int r = fpLibrary.INSTANCE.OpenDevice(0, 0, 0);
    
    if (r == 1) {
      if (fpLibrary.INSTANCE.LinkDevice() == 1) {
        status.setText("Fingerprint Ready!");
      } else {
        status.setText("Fingerprint scanner not present");
      }
    } else {
      status.setText("Fingerprint scanner not present");
      enableReader.setEnabled(true);
    }
    
    ipsList.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        FingerprintUI.prefs.get(Configs.BASE_URL, "");
        int ipIndex = ipsList.getSelectedIndex();
        System.out.println("ip address " + ipIndex + " " + (String)ipAddresses.get(ipIndex));
        FingerprintUI.prefs.put(Configs.BASE_URL, "http://" + ((String)ipAddresses.get(ipIndex)).replaceAll(" ", "") + ":3000");
        FingerprintUI.this.getAllBranches();
      }
    });
  }
  
  private void addToComboBox(ArrayList<String> ipNames) {
    for (int x = 0; x < ipNames.size(); x++) {
      System.out.println("file ips " + (String)ipNames.get(x));
      ipsList.addItem(ipNames.get(x));
    }
  }
  
  private void WriteToFile(String ipName, String ipAddress) {
    try {
      FileWriter writer = new FileWriter(fileName, true);
      BufferedWriter bufferedWriter = new BufferedWriter(writer);
      
      bufferedWriter.newLine();
      bufferedWriter.write(ipName + " : " + ipAddress);
      

      bufferedWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private ArrayList<String> readFromFile() {
    try {
      File yourFile = new File(fileName);
      
      if (!yourFile.exists()) {
        yourFile.createNewFile();
      } else if ((yourFile.exists()) && (!yourFile.isDirectory()))
      {
        FileReader reader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          System.out.println("Reads from files " + line);
          returnedIps.add(line);
        }
        reader.close();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return returnedIps;
  }
  
  private void postUserDetailsToServer(String empCode, String username, String secondName, String image_blob, String size, String image_hex, String emp_branch)
  {
    try {
      System.out.println(image_hex);
      
      HttpResponse<JsonNode> jsonResponse = Unirest.post(prefs.get(Configs.BASE_URL, "") + "/fingerprintCore/fingerprint").header("accept", "application/json").field("first_name", username).field("last_name", secondName).field("employeeCode", empCode).field("image_blob", image_blob).field("image_size", size).field("image_hex", image_hex).field("emp_branch", emp_branch).field("isSuperVisor", Boolean.valueOf(false)).asJson();
      
      System.out.print(jsonResponse.getBody() + " body====" + " " + prefs.get(Configs.BASE_URL, ""));
      if (((JsonNode)jsonResponse.getBody()).getObject().getBoolean("success")) {
        status.setText("Please wait...!");
        loginUser(empCode);
      } else {
        showRegErrDialog(((JsonNode)jsonResponse.getBody()).getObject().getString("message"));
      }
    } catch (UnirestException ex) {
      timer.cancel();
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
      JOptionPane.showMessageDialog(this, "Please check internet connection");
    }
  }
  
  private void loginUser(String empCode)
  {
    try {
      HttpResponse<JsonNode> postData = Unirest.post(prefs.get(Configs.BASE_URL, "") + "/fingerprintCore/userLoginDetails").field("empCode", empCode).field("isLoggedIn", Boolean.valueOf(false)).asJson();
      
      if (((JsonNode)postData.getBody()).getObject().getBoolean("success")) {
        status.setText("Successfully registered...!");
        

        showRegSucDialog();
        

        firstname.setText("");
        lastname.setText("");

      }
      else
      {

        JOptionPane.showMessageDialog(this, "Failed to login.Contact Admin");
      }
    }
    catch (UnirestException ex) {
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private void getAllBranches() {
    try {
      HttpResponse<JsonNode> postData = Unirest.get(prefs.get(Configs.BASE_URL, "") + "/fingerprintCore/getBranches/getAllBranches").asJson();
      
      if (postData.getBody() != null)
      {
        JSONArray content = ((JsonNode)postData.getBody()).getObject().getJSONArray("content");
        if (content.length() > 0) {
          branchesList.removeAllItems();
          
          for (int x = 0; x < content.length(); x++) {
            branchesList.addItem(content.getJSONObject(x).getString("branchName"));
          }
        } else {
          branchesList.addItem("Main Branch");
        }
        JOptionPane.showMessageDialog(this, "Connection Set");
      } else {
        JOptionPane.showMessageDialog(this, "Failed to fetch.Contact Admin");
      }
    }
    catch (UnirestException ex) {
      JOptionPane.showMessageDialog(this, "Failed to connect, check connection");
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void start() {
    timer.schedule(new java.util.TimerTask()
    {
      public void run() {
        fpsmessage();
      }
      

      private void fpsmessage()
      {
        int fpsmsg = fpLibrary.INSTANCE.GetWorkMsg();
        int retmsg = fpLibrary.INSTANCE.GetRetMsg();
        switch (fpsmsg) {
        case 1: 
          status.setText("Please Open Device");
          break;
        case 2: 
          System.out.println(fpsmsg + " ===== " + retmsg);
          status.setText("Please Place Finger");
          break;
        case 3: 
          System.out.println(fpsmsg + " ===== " + retmsg);
          status.setText("Please Lift Finger");
          break;
        case 5: 
          System.out.println(fpsmsg + " ===== " + retmsg);
          if (retmsg == 1) {
            System.out.println("switch case 5====");
            fpLibrary.INSTANCE.GetFpCharByGen(matbuf, matsize);
            try
            {
              HttpResponse<JsonNode> requestForAllFingerprints = Unirest.get(FingerprintUI.prefs.get(Configs.BASE_URL, "") + "/fingerprintCore/fingerprint/").header("Content-Type", "application/json").asJson();
              
              if (((JsonNode)requestForAllFingerprints.getBody()).getObject().getBoolean("success")) {
                JSONArray contentFingerprints = ((JsonNode)requestForAllFingerprints.getBody()).getObject().getJSONArray("content");
                
                System.out.println(contentFingerprints.toString());
                

                if (contentFingerprints.length() > 0)
                {
                  for (int count = 0; count < contentFingerprints.length(); count++) {
                    try {
                      String image_hex = contentFingerprints.getJSONObject(count).getString("image_hex");
                      System.out.println(" ==new message hex content " + fingerPrintValueByte);
                      fingerPrintValueByte = Configs.hexStringToByteArray(image_hex);
                      System.out.println(" ==new message hex content again " + fingerPrintValueByte);
                      
                      int ret = fpLibrary.INSTANCE.MatchTemplateOne(matbuf, fingerPrintValueByte, 512);
                      System.out.println("Match Val : " + String.valueOf(ret));
                      
                      if (ret > 50)
                      {
                        JOptionPane.showMessageDialog(FingerprintUI.this, "Fingerprint already exists!");
                        System.out.println("MAtch kinder found");
                        break;
                      }
                      System.out.println("No match found my friend!");
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                    System.out.println("counter is now at: " + count + " " + contentFingerprints.length());
                    if (count == contentFingerprints.length() - 1) {
                      status.setText("Registering...");
                      System.out.println("Now starting enrollment process");
                      
                      FingerprintUI.this.beginFingerRegistration();
                    }
                    
                  }
                } else {
                  FingerprintUI.this.beginFingerRegistration();
                }
              }
              else {
                System.out.println("failed amn");
              }
            } catch (UnirestException ex) {
              Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
            }
          } else {
            status.setText("Capture Fail");
          }
          break;
        case 6: 
          System.out.println(fpsmsg + " ===== " + retmsg);
          if (retmsg == 1)
          {
            fpLibrary.INSTANCE.GetFpCharByEnl(refbuf, refsize);
            
            System.out.println(refbuf + " byte array " + refsize);
            

            username = firstname.getText().toString();
            lastnameString = lastname.getText().toString();
            employeeCodeString = employeeCode.getText().toString().toUpperCase();
            FingerprintUI.prefs.put("empCode", employeeCodeString);
            
            System.out.println(username + " " + employeeCodeString);
            System.out.println("content here string " + FingerprintUI.prefs.get(Configs.username, Configs.username));
            System.out.println("content here string " + FingerprintUI.prefs.get(Configs.lastname, Configs.lastname));
            System.out.println("content here byte " + FingerprintUI.prefs.getByteArray(employeeCodeString, fingerPrintValueByteEmpty));
            System.out.println("content here int " + FingerprintUI.prefs.getInt("imageSize", fingerPrintValueByteSizeEmpty));
            FingerprintUI.prefs.put(Configs.username, username);
            FingerprintUI.prefs.put(Configs.lastname, lastnameString);
            FingerprintUI.prefs.putByteArray(employeeCodeString, refbuf);
            FingerprintUI.prefs.putInt("imageSize", refsize[0]);
            System.out.println("content here string " + FingerprintUI.prefs.get(Configs.username, ""));
            System.out.println("content here byte " + FingerprintUI.prefs.getByteArray(employeeCodeString, fingerPrintValueByteEmpty) + "  " + employeeCodeString);
            System.out.println("content here int " + FingerprintUI.prefs.getInt("imageSize", fingerPrintValueByteSizeEmpty));
            


            FingerprintUI.this.postUserDetailsToServer(employeeCodeString, username, lastnameString, matbuf.toString(), String.valueOf(matbuf[0]), org.apache.commons.codec.binary.Hex.encodeHexString(matbuf), branchesList.getSelectedItem().toString());
            




            System.out.println(branchesList.getSelectedItem().toString());

          }
          else
          {
            status.setText("Failed to register");
          }
          break;
        case 7: 
          configs.DrawImage(fingerprintimage, img);
          System.out.println("returned image for drawing " + String.valueOf(img));
          
          break;
        case 8: 
          status.setText("Time Out"); } } }, 0L, 100L);
  }
  

  private JTextField employeeCode;
  
  private JButton enableReader;
  private void beginFingerRegistration()
  {
    fpLibrary.INSTANCE.EnrolFpChar();
  }
  
  private void storeblobIntoDB(byte[] imageByteArray)
  {
    try {
      Blob blob = new javax.sql.rowset.serial.SerialBlob(imageByteArray);
      System.out.println(blob + " image blob byte array " + imageByteArray);
      
      Blob blob2 = blob;
      
      int blobLength = (int)blob2.length();
      byte[] blobAsBytes = blob2.getBytes(1L, blobLength);
      
      System.out.println("byte array after reconverting " + blobAsBytes);
      if (java.util.Arrays.equals(imageByteArray, blobAsBytes)) {
        System.out.println(true);
      } else {
        System.out.println(false);
      }
    }
    catch (javax.sql.rowset.serial.SerialException ex) {
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SQLException ex) {
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private void checkStatus()
  {
    fingerprintUI.setVisible(true);
  }
  
  private void requestForAdminPassword()
  {
    String adminPassword = JOptionPane.showInputDialog(this, "Enter Admin Password", "Administrator password");
    


    if (!adminPassword.isEmpty()) {
      status.setText("Contacting server....!");
      
      crossCheckPasswordWithServer(adminPassword);
    } else {
      JOptionPane.showMessageDialog(this, "Enter password please!");
    }
  }
  
  private void crossCheckPasswordWithServer(String adminPassword) {
    try {
      HttpResponse<JsonNode> request = Unirest.get(prefs.get(Configs.BASE_URL, "") + "/fingerprintCore/fingerprint/getPasswordSystem").header("Content-Type", "application/json").asJson();
      
      if (((JsonNode)request.getBody()).getObject().getBoolean("success"))
      {
        String password = ((JsonNode)request.getBody()).getObject().getJSONObject("content").getString("password");
        
        System.out.println("gotten password " + password);
        

        if (adminPassword.equals(password)) {
          status.setText("Registration ready!");
          prefs.get("adminPassword", "");
          prefs.put("adminPassword", password);
          fingerprintUI.start();
          

          fpLibrary.INSTANCE.GenFpChar();
        }
        else
        {
          status.setText("");
          prefs.put("adminPassword", "");
          JOptionPane.showMessageDialog(this, "Password incorrect");
        }
      }
      else
      {
        JOptionPane.showMessageDialog(this, "Error, please contact admin");
      }
    }
    catch (UnirestException ex)
    {
      JOptionPane.showMessageDialog(this, "Check server connection!");
      
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private void showRegErrDialog(String errorMsg) {
    JOptionPane.showMessageDialog(this, errorMsg);
  }
  
  private void showRegSucDialog()
  {
    JOptionPane.showMessageDialog(this, "Successfully registered");
  }
  

  private JLabel fingerprintimage;
  
  private JButton fingerprintscane;
  private JTextField firstname;
  public static void main(String[] args)
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run() {
        FingerprintUI.fingerprintUI.checkStatus();
      }
    });
  }
  
  private void getEmployeeCode() {
    try {
      HttpResponse<JsonNode> request = Unirest.get(prefs.get(Configs.BASE_URL, "") + "/fingerprintCore/employeeCode").header("Content-Type", "application/json").asJson();
      
      if (((JsonNode)request.getBody()).getObject().getBoolean("success")) {
        try
        {
          String employeeCodeUser = ((JsonNode)request.getBody()).getObject().getJSONObject("content").getString("empCode");
          System.out.println("gotten password " + employeeCodeUser);
          employeeCode.setText(employeeCodeUser);
        }
        catch (Exception e) {
          String employeeCodeUser = ((JsonNode)request.getBody()).getObject().getJSONArray("content").getJSONObject(0).getString("empCode");
          System.out.println("gotten password " + employeeCodeUser);
          employeeCode.setText(employeeCodeUser);
          e.printStackTrace();
        }
        
      } else {
        JOptionPane.showMessageDialog(this, "Error, please contact admin");
      }
    }
    catch (UnirestException ex)
    {
      JOptionPane.showMessageDialog(this, "Check server connection!");
      
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  


  private void initComponents()
  {
    jPanel2 = new JPanel();
    firstname = new JTextField();
    jLabel2 = new JLabel();
    jLabel3 = new JLabel();
    lastname = new JTextField();
    jLabel4 = new JLabel();
    employeeCode = new JTextField();
    jLabel1 = new JLabel();
    branchesList = new JComboBox();
    jButton2 = new JButton();
    ipLabel = new JLabel();
    serverIPEditText = new JTextField();
    updateServerIP = new JButton();
    jPanel1 = new JPanel();
    fingerprintscane = new JButton();
    enableReader = new JButton();
    jPanel3 = new JPanel();
    fingerprintimage = new JLabel();
    jPanel4 = new JPanel();
    status = new JLabel();
    jButton1 = new JButton();
    welcomeMessage = new JLabel();
    jLabel6 = new JLabel();
    ipsList = new JComboBox();
    jSeparator1 = new JSeparator();
    jLabel5 = new JLabel();
    
    setDefaultCloseOperation(3);
    setBackground(new Color(109, 109, 109));
    
    jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Username", 0, 0, null, new Color(51, 102, 255)));
    jPanel2.setForeground(new Color(0, 102, 255));
    jPanel2.setCursor(new java.awt.Cursor(0));
    jPanel2.setMaximumSize(new java.awt.Dimension(300, 500));
    
    firstname.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.firstnameActionPerformed(evt);
      }
      
    });
    jLabel2.setFont(new Font("Tahoma", 0, 14));
    jLabel2.setText("First name");
    
    jLabel3.setFont(new Font("Tahoma", 0, 14));
    jLabel3.setText("Last name");
    
    lastname.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.lastnameActionPerformed(evt);
      }
      
    });
    jLabel4.setFont(new Font("Tahoma", 0, 14));
    jLabel4.setText("EmployeeCode");
    
    employeeCode.setEnabled(false);
    
    jLabel1.setFont(new Font("Tahoma", 0, 14));
    jLabel1.setText("Branch");
    
    branchesList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.branchesListActionPerformed(evt);
      }
      
    });
    jButton2.setText("New employee Code");
    jButton2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.jButton2ActionPerformed(evt);
      }
      
    });
    ipLabel.setFont(new Font("Tahoma", 0, 14));
    ipLabel.setText("Enter Server IP");
    
    updateServerIP.setText("Set Server IP");
    updateServerIP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.updateServerIPActionPerformed(evt);
      }
      
    });
    GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jLabel3, -2, 77, -2).addComponent(jLabel2, -2, 67, -2)).addGap(32, 32, 32).addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lastname).addComponent(firstname))).addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel4, -2, 105, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(employeeCode)).addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false).addGroup(GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup().addComponent(jLabel1, -2, 105, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(branchesList, 0, -1, 32767)).addGroup(GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup().addComponent(ipLabel, -2, 105, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(serverIPEditText, -2, 217, -2))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(updateServerIP, -1, 224, 32767)).addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup().addGap(0, 0, 32767).addComponent(jButton2))).addContainerGap()));
    
































    jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jLabel2, -1, -1, 32767).addComponent(firstname, -1, 31, 32767)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jLabel3, -1, -1, 32767).addComponent(lastname, -1, 29, 32767)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(employeeCode, -1, 30, 32767).addComponent(jLabel4, -1, -1, 32767)).addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton2, -2, 36, -2)).addGroup(jPanel2Layout.createSequentialGroup().addGap(42, 42, 42).addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(branchesList, -1, 29, 32767).addComponent(jLabel1, -1, -1, 32767)))).addGap(35, 35, 35).addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false).addComponent(updateServerIP, -1, -1, 32767).addComponent(serverIPEditText).addComponent(ipLabel, -2, 35, -2))));
    




























    jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Register fingerprint", 0, 0, null, new Color(51, 102, 255)));
    
    fingerprintscane.setText("Register fingerprint");
    fingerprintscane.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.fingerprintscaneActionPerformed(evt);
      }
      
    });
    enableReader.setLabel("LOGIN");
    enableReader.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.enableReaderActionPerformed(evt);
      }
      
    });
    GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(fingerprintscane, -1, -1, 32767).addComponent(enableReader, -1, -1, 32767)).addContainerGap()));
    







    jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(fingerprintscane, -2, 38, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(enableReader, -1, 34, 32767).addContainerGap()));
    








    jPanel3.setBorder(BorderFactory.createTitledBorder(null, "Fingerprint scan", 0, 0, null, new Color(51, 102, 255)));
    
    fingerprintimage.setBackground(new Color(255, 255, 255));
    
    GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(fingerprintimage, -1, -1, 32767));
    


    jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(fingerprintimage, -1, 117, 32767));
    



    jPanel4.setBorder(BorderFactory.createTitledBorder(null, "Status messages", 0, 0, null, new Color(51, 51, 255)));
    
    status.setFont(new Font("Myriad Hebrew", 1, 18));
    status.setForeground(new Color(255, 51, 51));
    status.setHorizontalAlignment(0);
    
    GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(status, -1, 230, 32767).addContainerGap()));
    





    jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup().addContainerGap(-1, 32767).addComponent(status, -2, 31, -2).addGap(105, 105, 105)));
    






    jButton1.setText("Exit");
    jButton1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.jButton1ActionPerformed(evt);
      }
      
    });
    welcomeMessage.setFont(new Font("Tahoma", 0, 14));
    welcomeMessage.setHorizontalAlignment(0);
    welcomeMessage.setText("Welcome, Please register for the first time");
    
    jLabel6.setFont(new Font("Tahoma", 0, 14));
    jLabel6.setText("Connection");
    
    ipsList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FingerprintUI.this.ipsListActionPerformed(evt);
      }
      
    });
    jLabel5.setFont(new Font("Futura2-Normal", 1, 14));
    jLabel5.setHorizontalAlignment(0);
    jLabel5.setText("Select Connection");
    
    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jPanel2, -1, -1, 32767).addComponent(jPanel1, -1, -1, 32767).addComponent(welcomeMessage, -1, -1, 32767).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jLabel6, -2, 105, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(ipsList, 0, -1, 32767)).addComponent(jSeparator1).addComponent(jLabel5, -1, -1, 32767).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jPanel3, -1, -1, 32767).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(10, 10, 10).addComponent(jPanel4, -2, -1, -2)).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton1, -2, 131, -2))))).addContainerGap()));
    























    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(9, 9, 9).addComponent(welcomeMessage).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator1, -2, 10, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel5, -2, 25, -2).addGap(18, 18, 18).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(ipsList).addComponent(jLabel6, -1, -1, 32767)).addGap(18, 18, 18).addComponent(jPanel2, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel1, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jPanel3, -2, -1, -2).addGroup(layout.createSequentialGroup().addComponent(jPanel4, -2, 70, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addComponent(jButton1, -2, 42, -2))).addContainerGap()));
    

























    pack();
  }
  
  private void jButton1ActionPerformed(ActionEvent evt)
  {
    prefs.put("adminPassword", "");
    System.exit(0);
  }
  

  private void lastnameActionPerformed(ActionEvent evt) {}
  

  private void fingerprintscaneActionPerformed(ActionEvent evt)
  {
    String username = firstname.getText();
    if (String.valueOf(username).equalsIgnoreCase("")) {
      JOptionPane.showMessageDialog(this, "Please provide username first");
    } else if (employeeCode.getText().toString().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please generate employee Code");
    } else {
      System.out.println("content....");
      String adminPassword = prefs.get("adminPassword", "");
      if (!prefs.get(Configs.BASE_URL, "").isEmpty()) {
        if (adminPassword.isEmpty()) {
          requestForAdminPassword();
        } else {
          crossCheckPasswordWithServer(adminPassword);
        }
      } else {
        JOptionPane.showMessageDialog(this, "Select connection");
      }
    }
  }
  

  private JLabel ipLabel;
  
  private JComboBox ipsList;
  
  private JButton jButton1;
  
  private JButton jButton2;
  
  private JLabel jLabel1;
  
  private JLabel jLabel2;
  
  private JLabel jLabel3;
  private void firstnameActionPerformed(ActionEvent evt) {}
  
  private void enableReaderActionPerformed(ActionEvent evt)
  {
    showRegSucDialog();
  }
  


  private void branchesListActionPerformed(ActionEvent evt) {}
  

  private void jButton2ActionPerformed(ActionEvent evt)
  {
    getEmployeeCode();
  }
  
  private void updateServerIPActionPerformed(ActionEvent evt)
  {
    if (serverIPEditText.getText().toString().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Enter server ip");
    } else {
      String oneTimeIp = "http://" + serverIPEditText.getText().toString() + ":3000";
      
      getIpsAndSave(oneTimeIp);
    }
  }
  

  private void addIpsToView()
  {
    ipsList.removeAllItems();
    ipNames.clear();
    ipAddresses.clear();
    ArrayList<String> fileRead = readFromFile();
    for (int x = 0; x < fileRead.size(); x++) {
      if (!((String)fileRead.get(x)).isEmpty()) {
        System.out.println("file ips " + (String)fileRead.get(x));
        String[] ipAddSplit = ((String)fileRead.get(x)).split(":");
        ipNames.add(ipAddSplit[0]);
        ipAddresses.add(ipAddSplit[1].replaceAll(" ", ""));
      }
    }
    
    addToComboBox(ipNames);
  }
  
  private void getIpsAndSave(String oneTimeIp)
  {
    try {
      HttpResponse<JsonNode> request = Unirest.get(oneTimeIp + "/fingerprintCore/getIps").header("Content-Type", "application/json").asJson();
      
      if (((JsonNode)request.getBody()).getObject().getBoolean("status"))
      {

        ipLabel.setVisible(false);
        serverIPEditText.setVisible(false);
        updateServerIP.setVisible(false);
        
        for (int x = 0; x < ((JsonNode)request.getBody()).getObject().getJSONArray("content").length(); x++) {
          WriteToFile(((JsonNode)request.getBody()).getObject().getJSONArray("content").getJSONObject(x).getString("serverName"), ((JsonNode)request.getBody()).getObject().getJSONArray("content").getJSONObject(x).getString("serverIp"));
        }
        


        addIpsToView();
      }
      else {
        JOptionPane.showMessageDialog(this, "Error, please contact admin");
      }
    }
    catch (UnirestException ex)
    {
      JOptionPane.showMessageDialog(this, "Check server connection!");
      
      Logger.getLogger(FingerprintUI.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  


  private JLabel jLabel4;
  

  private JLabel jLabel5;
  
  private JLabel jLabel6;
  
  private JPanel jPanel1;
  
  private JPanel jPanel2;
  
  private JPanel jPanel3;
  
  private JPanel jPanel4;
  
  private JSeparator jSeparator1;
  
  private JTextField lastname;
  
  private JTextField serverIPEditText;
  
  private JLabel status;
  
  private JButton updateServerIP;
  
  private JLabel welcomeMessage;
  
  private void ipsListActionPerformed(ActionEvent evt) {}
  

  public int OpenDevice(int comnum, int nbaud, int style)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int LinkDevice()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int CloseDevice()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int DevicePrint(byte[] buffer, int size)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int GetImage(byte[] imagedata, int[] size)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public void GenFpChar()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public void EnrolFpChar()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int GetWorkMsg()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int GetRetMsg()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int GetFpCharByGen(byte[] tpbuf, int[] tpsize)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int GetFpCharByEnl(byte[] fpbuf, int[] fpsize)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int ChangeTemplateType(int type, byte[] input, byte[] output)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int MatchTemplateOne(byte[] pSrcData, byte[] pDstData, int nDstSize)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int MatchTemplate(byte[] pSrcData, byte[] pDstData)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}