/* To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
 
package ThingMagic.Readers;
 
import java.io.*;
import java.net.*;
import base.datos.swing.GUI;
 
public class MercuryReader {
    
    private GUI app;
    private Socket socket = null;
    private PrintWriter readerOut = null;
    private BufferedReader readerIn = null;
    private Mercury4ReaderThread thread;
        
    /** Creates a new instance of MercuryReader */
    public MercuryReader(GUI app) throws UnknownHostException, IOException {
        this.app = app;
        init();
    }
 
    public void init() throws UnknownHostException, IOException {
        // Connect to a networked reader, use the following:     
        try {
            connect();
        } catch (UnknownHostException ex) {
            app.show("Error: " + ex.toString());
        } catch (IOException ex) {
            app.show("Error: " + ex.toString());
        }
        
        thread = new Mercury4ReaderThread(app, this);
        thread.setName("ThingMagic Mercury4 Reader Thread");
        thread.setDaemon(true);
        thread.start();
    }
    
    public synchronized void connect() throws UnknownHostException, IOException {
        socket = new Socket("10.0.0.101", 8080);
        readerOut = new PrintWriter(socket.getOutputStream(), true);
        readerIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public synchronized void doCommand(String query) {
        readerOut.println(query);
    }
    
    public BufferedReader getBuffer() {
        return readerIn;
    }
}
