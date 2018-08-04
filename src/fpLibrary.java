


import com.sun.jna.Library;
import com.sun.jna.Native;

public abstract interface fpLibrary
  extends Library
{
  public static final fpLibrary INSTANCE = (fpLibrary)Native.loadLibrary("fpengine", fpLibrary.class);
  
  public abstract int OpenDevice(int paramInt1, int paramInt2, int paramInt3);
  
  public abstract int LinkDevice();
  
  public abstract int CloseDevice();
  
  public abstract int DevicePrint(byte[] paramArrayOfByte, int paramInt);
  
  public abstract int GetImage(byte[] paramArrayOfByte, int[] paramArrayOfInt);
  
  public abstract void GenFpChar();
  
  public abstract void EnrolFpChar();
  
  public abstract int GetWorkMsg();
  
  public abstract int GetRetMsg();
  
  public abstract int GetFpCharByGen(byte[] paramArrayOfByte, int[] paramArrayOfInt);
  
  public abstract int GetFpCharByEnl(byte[] paramArrayOfByte, int[] paramArrayOfInt);
  
  public abstract int ChangeTemplateType(int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
  
  public abstract int MatchTemplateOne(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt);
  
  public abstract int MatchTemplate(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
}
