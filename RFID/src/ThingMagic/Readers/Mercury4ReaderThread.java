/*
 * Mercury4ReaderThread.java
 *
 * Created on 19 de noviembre de 2008, 01:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
 
package ThingMagic.Readers;
 
import java.io.IOException;
import rfid.swing.GUI;
 
/**
 *
 * @author L00308163   Dr. Raul Crespo Saucedo
 */
public class Mercury4ReaderThread extends Thread {
    
    private GUI app;
    private MercuryReader reader;
    
    /** Creates a new instance of Mercury4ReaderThread */
    public Mercury4ReaderThread(GUI app, MercuryReader reader) {
        this.app = app;
        this.reader = reader;
    }
    
    public void run() {
        while(true) {
            try {
                String response = reader.getBuffer().readLine();
                app.show(response);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }    
    
}
