package fingerregistrationv2;

public class Fingerprintmodel
{
  private String first_name;
  


  private String last_name;
  

  private byte[] image_blob;
  


  public String getFirst_name()
  {
    return first_name;
  }
  
  public void setFirst_name(String first_name) {
    this.first_name = first_name;
  }
  
  public byte[] getImage_blob() {
    return image_blob;
  }
  
  public void setImage_blob(byte[] image_blob) {
    this.image_blob = image_blob;
  }
  
  public String getLast_name() {
    return last_name;
  }
  
  public void setLast_name(String last_name) {
    this.last_name = last_name;
  }
  
  public Fingerprintmodel(String first_name, String last_name, byte[] image_blob) {
    this.first_name = first_name;
    this.last_name = last_name;
    this.image_blob = image_blob;
  }
}
