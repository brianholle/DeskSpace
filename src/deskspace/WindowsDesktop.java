 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deskspace;

/**
 *
 * @author mrbri
 */

import java.util.HashMap;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.*;
import static deskspace.DeskSpace.jarPath;
public class WindowsDesktop {
    
 public static void main(String[] args) {
      
   }

 public static void start(){
      String path = jarPath + "\\DeskSpace.png";
      System.out.println("Desktop Wallpaper Updated");
      SPI.INSTANCE.SystemParametersInfo(
          new UINT_PTR(SPI.SPI_SETDESKWALLPAPER), 
          new UINT_PTR(0), 
          path, 
          new UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
      
 }
   public interface SPI extends StdCallLibrary {

      //from MSDN article
      long SPI_SETDESKWALLPAPER = 20;
      long SPIF_UPDATEINIFILE = 0x01;
      long SPIF_SENDWININICHANGE = 0x02;

      @SuppressWarnings("serial")
        SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class,
            new HashMap<String, Object>() {
                {
                    put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                    put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
                }
            });

      boolean SystemParametersInfo(
          UINT_PTR uiAction,
          UINT_PTR uiParam,
          String pvParam,
          UINT_PTR fWinIni
        );
    }
}