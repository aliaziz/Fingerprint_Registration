

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App
{
  public App() {}
  
  public static void main(String[] args)
  {
      try {
          Path move = Files.move(Paths.get(""), Paths.get(""));
          System.out.println("Hello World!");
      } catch (IOException ex) {
          Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
}
