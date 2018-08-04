

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DatabaseConnection
{
  public DatabaseConnection() {}
  
  public Connection getDatabaseConnection()
    throws SQLException, ClassNotFoundException
  {
    Class.forName("org.apache.derby.jdbc.ClientDriver");
    Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/FingerPrint", "APP", "123456");
    return con;
  }
}
