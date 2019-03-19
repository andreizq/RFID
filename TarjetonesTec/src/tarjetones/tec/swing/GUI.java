/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tarjetones.tec.swing;

import ThingMagic.Readers.MercuryReader;
import ThingMagic.Readers.Mercury4ReaderThread;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author rodla
 */
public class GUI extends javax.swing.JFrame {
    
    // Database
    Connection conn = null;
    String foto_path_alta = "";
    
    // Mercury Reader
    MercuryReader m4reader;
    
    // Flags
    Boolean showRaw = false;
    Boolean pollingSensoresPrueba = false; 
    Boolean obstaculoEntradaRegistrado = false; 
    Boolean altaEPC = false;
    Boolean autoRun = false;
    
    // Auto Run
    final int LEER_SENSOR_SIEMPRE = 0;
    final int LEER_EPC = 1; 
    final int VERIFICAR_DB = 2; 
    final int ABRIR_PLUMA = 3; 
    final int CERRAR_PLUMA = 4; 
    final int ESPERAR_COCHE_DENEGADO = 5;
    
    int estadoAutoRun= 0;
    String epcPorRevisar = "";
    
    public GUI() {
        initComponents();
        initRFIDComponents();
        initDBComponents();
        //createDBComponents(); 
    }
    
    private void initRFIDComponents(){
        try {
            m4reader = new MercuryReader(this);
        } catch (UnknownHostException ex) {
            show("Error: " + ex.toString());
        } catch (IOException ex) {
            show("Error: " + ex.toString());
        }
    }
    
    private void initDBComponents() {     
        try {
            String driverName = "com.mysql.jdbc.Driver";
            Class.forName(driverName);
            
            //  Código usado para un servidor local
            String serverName = "localhost";
            String mydatabase = "tarjetones";
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = "root";
            String password = "";

            conn = DriverManager.getConnection(url, username, password);
            
        } catch (Exception e) {
           debug_text_area.append("Error al iniciar la base de datos.\n");
        }
    }
    
    private void createDBComponents(){
        initDBComponents();
            
        PreparedStatement pre; 
        try {
            pre = conn.prepareStatement("DROP TABLE IF EXISTS Tarjetones");
            pre.executeUpdate();
            pre = conn.prepareStatement(
           "CREATE TABLE Tarjetones ("
           + "id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
           + "tagEPC VARCHAR(26) NOT NULL,"
           + "nombre VARCHAR(50) NOT NULL,"
           + "matricula CHAR(9) NOT NULL,"
           + "estado VARCHAR(15),"
           + "modelo VARCHAR(15),"
           + "PRIMARY KEY(id))");
            pre.executeUpdate();

            pre = conn.prepareStatement("DROP TABLE IF EXISTS Fotos");
            pre.executeUpdate();
            pre = conn.prepareStatement(
                "CREATE TABLE Fotos ("
                + "id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                + "tagEPC VARCHAR(26) NOT NULL,"
                + "foto mediumblob NOT NULL,"
                + "PRIMARY KEY(id))");
            pre.executeUpdate();

            debug_text_area.append("Conexión a la base de datos exitosa.\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
        } 
        catch (Exception e) {
            debug_text_area.append("Error al iniciar la base de datos.\n");
        }
    }
    
    
    
    public void show(String response) {
        if (pollingSensoresPrueba) {
            if(response.contains("0x00000000") && !obstaculoEntradaRegistrado) {
               debug_text_area.append("Obstáculo en entrada\n");
               debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
               obstaculoEntradaRegistrado = true; 
            }
            else if(response.contains("0x00000002") && obstaculoEntradaRegistrado) {
               debug_text_area.append("No hay obstáculo en entrada\n");
               debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
               obstaculoEntradaRegistrado = false; 
            }
        }
        
        if(altaEPC) {
            if(response.contains("0x")) {
                epc_alta_textfield.setText(response);
            }
            altaEPC = false; 
        }
        
        if(autoRun) {
            switch(estadoAutoRun){
                case LEER_SENSOR_SIEMPRE: 
                    if(response.contains("0x00000000")) {
                        m4reader.doCommand("SET AUTO = OFF;");
                        
                        autorun_textarea.append("Auto detectado \n");
                        autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                        
                        // Apagar led denegado
                        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x10;");
                        
                        // Cursor leer epc
                        m4reader.doCommand("DECLARE leerEPC CURSOR FOR SELECT data FROM tag_data WHERE protocol_id='GEN2' AND antenna_id=1 AND mem_bank=1 AND block_number=2 AND block_count=6;");
                        m4reader.doCommand("SET AUTO leerEPC = ON;");
                        
                        estadoAutoRun = LEER_EPC;
                    }
                    break;
                    
                case LEER_EPC:
                    if (response.contains("0x00000000")) {
                        estadoAutoRun = LEER_EPC; // Rezago del buffer 
                    }
                    else if (response.contains("0x")) {
                        m4reader.doCommand("SET AUTO = OFF;");
                        
                        autorun_textarea.append("EPC leído: " + response + "\n");
                        autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                        
                        epcPorRevisar = response; 
                        estadoAutoRun = VERIFICAR_DB;
                    }
                    break;
                   
                case VERIFICAR_DB:
                    m4reader.doCommand("SET AUTO = OFF;");
                    
                    if (accessGranted(epcPorRevisar)) {
                        autorun_textarea.append("Acceso permitido\n");
                        autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                        
                        estadoAutoRun = ABRIR_PLUMA;
                    }
                    else {
                        autorun_textarea.append("Acceso denegado\n\n");
                        autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                        
                        // Prender led denegado
                        m4reader.doCommand("UPDATE io SET data=0xFF WHERE type=0 AND mask=0x10;");
                        
                        // Cursor sensor (esperar a que se vaya el coche denegado)
                        m4reader.doCommand("DECLARE entradaGpio CURSOR FOR SELECT data FROM io WHERE type=0 AND mask=0x02;");
                        m4reader.doCommand("SET AUTO entradaGpio = ON;");
                        
                        estadoAutoRun = ESPERAR_COCHE_DENEGADO;
                    }
                    break;
                    
                case ABRIR_PLUMA:
                    m4reader.doCommand("SET AUTO = OFF;");
                    
                    autorun_textarea.append("Abriendo pluma\n\n");
                    autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                    
                    // Abrir
                    m4reader.doCommand("UPDATE io SET data=0xFF WHERE type=0 AND mask=0x04;");
                    m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x08;");
                    try {
                        TimeUnit.MILLISECONDS.sleep(107);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x04;");
                    
                    // Cursor sensor (Esperar a que entre el coche aceptado)
                    m4reader.doCommand("DECLARE entradaGpio CURSOR FOR SELECT data FROM io WHERE type=0 AND mask=0x02;");
                    m4reader.doCommand("SET AUTO entradaGpio = ON;");
                    
                    estadoAutoRun = CERRAR_PLUMA;
                    break; 
                    
                    
                case CERRAR_PLUMA: 
                    if(response.contains("0x00000002")) {
                        m4reader.doCommand("SET AUTO = OFF;");
                        
                        autorun_textarea.append("Cerrando pluma\n\n");
                        autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                        
                        // Esperar
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        // Cerrar pluma
                        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x04;");
                        m4reader.doCommand("UPDATE io SET data=0xFF WHERE type=0 AND mask=0x08;");
                        try {
                            TimeUnit.MILLISECONDS.sleep(107);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x08;");
                        
                        // Cursor sensor (sensor por siempre)
                        m4reader.doCommand("DECLARE entradaGpio CURSOR FOR SELECT data FROM io WHERE type=0 AND mask=0x02;");
                        m4reader.doCommand("SET AUTO entradaGpio = ON;");
                       
                        for(int i = 1; i<10; i++) {
                            try {
                                m4reader.getBuffer().readLine();
                            } catch (IOException ex) {
                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        estadoAutoRun = LEER_SENSOR_SIEMPRE;
                    } 
                    break;
                
                    
                case ESPERAR_COCHE_DENEGADO:
                    if(response.contains("0x00000002")) {
                        m4reader.doCommand("SET AUTO = OFF;");
                        
                        // Cursor sensor (sensor por siempre)
                        m4reader.doCommand("DECLARE entradaGpio CURSOR FOR SELECT data FROM io WHERE type=0 AND mask=0x02;");
                        m4reader.doCommand("SET AUTO entradaGpio = ON;");
                        
                        estadoAutoRun = LEER_SENSOR_SIEMPRE;
                    }
                    break; 
                    
                default:
                    break;
            }
        }
        
        if (showRaw) {
            debug_text_area.append(response + "\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
            showRaw = false;
        }
    }
    
    public Boolean accessGranted(String epc) {
        Boolean access = false;
        String nombreVal, matriculaVal, estadoVal, modeloVal; 
        try
        {
            String sql = "SELECT nombre, matricula, estado, modelo FROM Tarjetones "
                            + "WHERE tagEPC = '" + epc + "'";
            PreparedStatement ps = conn.prepareStatement (sql);
            ResultSet rs = ps.executeQuery();
            
            String formattedDate= (new SimpleDateFormat("hh:mm:ss a")).format(new Date());
            if (rs.next()) {
               do {
                   nombreVal = rs.getString("nombre");
                   matriculaVal = rs.getString("matricula");
                   estadoVal = rs.getString("estado");
                   modeloVal = rs.getString("modelo");
               }
               while (rs.next());

               autorun_textarea.append(formattedDate + " - ");
               autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);

               cargarFotoDeBase(epc, foto_caseta_label);
               if (estadoVal.equals("Actualizado")) {
                   autorun_textarea.append(nombreVal + ", " + matriculaVal + ", " + modeloVal +" ");
                   autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
                   access = true;
               }
               else{
                   autorun_textarea.append(nombreVal + ", " + matriculaVal + ", " + modeloVal + " ");
                   autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
               }
         
            }
            else {
                foto_caseta_label.setIcon(null);
                autorun_textarea.append(formattedDate + " - ");
                autorun_textarea.append("Tarjetón no registrado.");
                 
             }
            rs.close ();
            ps.close ();
        }
        catch (Exception e) 
        { 
            autorun_textarea.append("Error de acceso a base de datos");
            autorun_textarea.setCaretPosition(autorun_textarea.getDocument().getLength() - 1);
            access = false;
        }
        return access;
    }
    
    private void cargarFotoDeBase(String epc, JLabel label) {
        try
        {
             String sql = "SELECT foto FROM Fotos "
                     + "WHERE tagEPC = '" + epc + "'";
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 do {
                     Blob b=rs.getBlob(1);
                     byte barr[]=new byte[(int)b.length()];
                     barr=b.getBytes(1,(int)b.length());

                     File file = new File("Foto.jpg");
                     FileOutputStream fout=new FileOutputStream(file);
                     fout.write(barr);
                     fout.close();

                     BufferedImage img;
                     try {
                         img = ImageIO.read(new File("Foto.jpg"));
                         Image dimg = img.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
                         label.setText("");
                         label.setIcon(new javax.swing.ImageIcon(dimg));
                     } catch (IOException ex) {
                         Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                     }

                 }
                 while (rs.next()); 
             }
             else {
                  debug_text_area.append("Información no generada. Registra tu tarjetón.\n");
                  debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
              }
             rs.close ();
             ps.close ();
         }
         catch (Exception e) 
         { 
             debug_text_area.append("Error de acceso a base de fotos\n");
             debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
         }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leer_escribir_epc_button = new javax.swing.ButtonGroup();
        leer_escribir_potencia_button = new javax.swing.ButtonGroup();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        debug_text_area = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        leer_epc_button_from_group = new javax.swing.JRadioButton();
        escribir_epc_button_from_group = new javax.swing.JRadioButton();
        epc_control_lector_pluma_textfield = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        leer_potencia_button_from_group = new javax.swing.JRadioButton();
        escribir_potencia_button_from_group = new javax.swing.JRadioButton();
        potencia_textfield = new javax.swing.JTextField();
        ejecutar_epc = new javax.swing.JButton();
        ejecutar_potencia = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        abrir_pluma_entrada_button = new javax.swing.JButton();
        cerrar_pluma_entrada_button = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        iniciar_cursor_sensores_button = new javax.swing.JButton();
        detener_cursor_sensores_button = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        epc_alta_textfield = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        nombre_alta_textfield = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        matricula_alta_textfield = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        estado_alta_textfield = new javax.swing.JTextField();
        modelo_alta_textfield = new javax.swing.JTextField();
        actualizar_base_button = new javax.swing.JButton();
        epc_alta_button = new javax.swing.JButton();
        foto_button = new javax.swing.JButton();
        foto_alta_label = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        autorun_textarea = new javax.swing.JTextArea();
        auto_run_button = new javax.swing.JToggleButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        forzar_lectura_autorun_button = new javax.swing.JButton();
        forzar_abrir_autorun_button = new javax.swing.JButton();
        limpiar_log_autorun_button = new javax.swing.JButton();
        foto_caseta_label = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        nombre_consulta_textfield = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        matricula_consulta_textfield = new javax.swing.JTextField();
        consultar_usuario_button = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        epc_consulta_textfield = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        modelo_consulta_textfield = new javax.swing.JTextField();
        foto_consulta_label = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        estado_consulta_textfield = new javax.swing.JTextField();
        clear_debug_text_area_button = new javax.swing.JButton();

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        debug_text_area.setEditable(false);
        debug_text_area.setColumns(20);
        debug_text_area.setRows(5);
        jScrollPane1.setViewportView(debug_text_area);

        jTabbedPane1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel1.setText("EPC");

        leer_escribir_epc_button.add(leer_epc_button_from_group);
        leer_epc_button_from_group.setText("Leer");

        leer_escribir_epc_button.add(escribir_epc_button_from_group);
        escribir_epc_button_from_group.setText("Escribir");

        epc_control_lector_pluma_textfield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                epc_control_lector_pluma_textfieldActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel2.setText("Potencia");

        leer_escribir_potencia_button.add(leer_potencia_button_from_group);
        leer_potencia_button_from_group.setText("Leer");

        leer_escribir_potencia_button.add(escribir_potencia_button_from_group);
        escribir_potencia_button_from_group.setText("Escribir");

        ejecutar_epc.setText("Ejecutar");
        ejecutar_epc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejecutar_epcActionPerformed(evt);
            }
        });

        ejecutar_potencia.setText("Ejecutar");
        ejecutar_potencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejecutar_potenciaActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel3.setText("Sensor de posición");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel6.setText("Pluma");

        abrir_pluma_entrada_button.setText("Abrir");
        abrir_pluma_entrada_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrir_pluma_entrada_buttonActionPerformed(evt);
            }
        });

        cerrar_pluma_entrada_button.setText("Cerrar");
        cerrar_pluma_entrada_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cerrar_pluma_entrada_buttonActionPerformed(evt);
            }
        });

        jLabel7.setText("Entrada");

        iniciar_cursor_sensores_button.setText("Iniciar cursor");
        iniciar_cursor_sensores_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iniciar_cursor_sensores_buttonActionPerformed(evt);
            }
        });

        detener_cursor_sensores_button.setText("Detener cursor");
        detener_cursor_sensores_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detener_cursor_sensores_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(iniciar_cursor_sensores_button)
                                .addGap(18, 18, 18)
                                .addComponent(detener_cursor_sensores_button))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(leer_epc_button_from_group)
                                        .addGap(18, 18, 18)
                                        .addComponent(escribir_epc_button_from_group)
                                        .addGap(18, 18, 18)
                                        .addComponent(epc_control_lector_pluma_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel2)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(leer_potencia_button_from_group)
                                        .addGap(18, 18, 18)
                                        .addComponent(escribir_potencia_button_from_group)
                                        .addGap(18, 18, 18)
                                        .addComponent(potencia_textfield)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ejecutar_epc)
                                    .addComponent(ejecutar_potencia)))
                            .addComponent(jLabel3))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(abrir_pluma_entrada_button)
                                .addGap(18, 18, 18)
                                .addComponent(cerrar_pluma_entrada_button)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leer_epc_button_from_group)
                    .addComponent(escribir_epc_button_from_group)
                    .addComponent(epc_control_lector_pluma_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ejecutar_epc))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leer_potencia_button_from_group)
                    .addComponent(escribir_potencia_button_from_group)
                    .addComponent(potencia_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ejecutar_potencia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iniciar_cursor_sensores_button)
                    .addComponent(detener_cursor_sensores_button))
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(abrir_pluma_entrada_button)
                    .addComponent(cerrar_pluma_entrada_button))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Control de lector y pluma", jPanel1);

        jLabel9.setText("EPC del tarjetón");

        jLabel10.setText("Nombre");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel11.setText("Dueño");

        jLabel12.setText("Matrícula");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel13.setText("Tarjetón");

        jLabel14.setText("Estado");

        jLabel15.setText("Modelo de auto");

        actualizar_base_button.setText("Añadir a base de datos");
        actualizar_base_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizar_base_buttonActionPerformed(evt);
            }
        });

        epc_alta_button.setText("Leer tag");
        epc_alta_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                epc_alta_buttonActionPerformed(evt);
            }
        });

        foto_button.setText("Seleccionar foto");
        foto_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foto_buttonActionPerformed(evt);
            }
        });

        foto_alta_label.setText("Foto");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(actualizar_base_button, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel15)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(modelo_alta_textfield))
                                .addComponent(estado_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel10)
                                    .addGap(18, 18, 18)
                                    .addComponent(nombre_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel12)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(matricula_alta_textfield))
                                .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(18, 18, 18)
                                .addComponent(epc_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(epc_alta_button)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(foto_button)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(foto_alta_label, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                .addContainerGap())))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(epc_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(epc_alta_button)
                    .addComponent(jLabel9))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nombre_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(matricula_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(jLabel13))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(foto_alta_label, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(estado_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(foto_button))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(modelo_alta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(actualizar_base_button)
                .addContainerGap(76, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Alta de tarjetón", jPanel2);

        autorun_textarea.setEditable(false);
        autorun_textarea.setColumns(20);
        autorun_textarea.setRows(5);
        jScrollPane2.setViewportView(autorun_textarea);

        auto_run_button.setText("Auto-Run");
        auto_run_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_run_buttonActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel4.setText("Antena");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel5.setText("Pluma");

        forzar_lectura_autorun_button.setText("Forzar lectura");
        forzar_lectura_autorun_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forzar_lectura_autorun_buttonActionPerformed(evt);
            }
        });

        forzar_abrir_autorun_button.setText("Forzar abrir");
        forzar_abrir_autorun_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forzar_abrir_autorun_buttonActionPerformed(evt);
            }
        });

        limpiar_log_autorun_button.setText("Limpiar log");
        limpiar_log_autorun_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpiar_log_autorun_buttonActionPerformed(evt);
            }
        });

        foto_caseta_label.setText("Foto");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(foto_caseta_label, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                            .addComponent(auto_run_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(limpiar_log_autorun_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(forzar_lectura_autorun_button))
                        .addGap(85, 85, 85)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(forzar_abrir_autorun_button)
                            .addComponent(jLabel5))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(auto_run_button)
                        .addGap(18, 18, 18)
                        .addComponent(foto_caseta_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(limpiar_log_autorun_button))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(forzar_lectura_autorun_button)
                    .addComponent(forzar_abrir_autorun_button))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Caseta", jPanel3);

        jLabel17.setText("Nombre");

        jLabel18.setText("Matrícula");

        consultar_usuario_button.setText("Consultar");
        consultar_usuario_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consultar_usuario_buttonActionPerformed(evt);
            }
        });

        jLabel19.setText("Tarjetón (EPC)");

        jLabel20.setText("Modelo del auto");

        foto_consulta_label.setText("Foto");

        jLabel22.setText("Estado");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(matricula_consulta_textfield))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addGap(18, 18, 18)
                        .addComponent(epc_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(modelo_consulta_textfield)
                            .addComponent(estado_consulta_textfield)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nombre_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(consultar_usuario_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                .addComponent(foto_consulta_label, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matricula_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(18, 18, 18)
                .addComponent(consultar_usuario_button)
                .addGap(28, 28, 28)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel17)
                            .addComponent(nombre_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(epc_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(modelo_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(estado_consulta_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(foto_consulta_label, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(124, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Usuario", jPanel4);

        clear_debug_text_area_button.setText("Limpiar");
        clear_debug_text_area_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_debug_text_area_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(clear_debug_text_area_button)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clear_debug_text_area_button)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void epc_control_lector_pluma_textfieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_epc_control_lector_pluma_textfieldActionPerformed

    }//GEN-LAST:event_epc_control_lector_pluma_textfieldActionPerformed

    private void ejecutar_epcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejecutar_epcActionPerformed
        showRaw = true;
        if(leer_epc_button_from_group.isSelected()) {
            m4reader.doCommand("SELECT data FROM tag_data WHERE protocol_id='GEN2' AND antenna_id=1 AND mem_bank=1 AND block_number=2 AND block_count=6;");
        }
        else if(escribir_epc_button_from_group.isSelected()){
            m4reader.doCommand("UPDATE tag_id SET id=" + epc_control_lector_pluma_textfield.getText() + " WHERE protocol_id='GEN2'AND antenna_id=1;");
        }
    }//GEN-LAST:event_ejecutar_epcActionPerformed

    private void ejecutar_potenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejecutar_potenciaActionPerformed
        showRaw = true;
        if(leer_potencia_button_from_group.isSelected()) {
            m4reader.doCommand("SELECT tx_power FROM saved_settings;");
        }
        else if(escribir_potencia_button_from_group.isSelected()){
            m4reader.doCommand("UPDATE saved_settings SET tx_power=​ ​'" + potencia_textfield.getText() + "00';");
        }
    }//GEN-LAST:event_ejecutar_potenciaActionPerformed

    private void clear_debug_text_area_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_debug_text_area_buttonActionPerformed
        debug_text_area.setText("");
    }//GEN-LAST:event_clear_debug_text_area_buttonActionPerformed

    private void iniciar_cursor_sensores_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iniciar_cursor_sensores_buttonActionPerformed
        m4reader.doCommand("DECLARE entradaGpio CURSOR FOR SELECT data FROM io WHERE type=0 AND mask=0x02;");
        m4reader.doCommand("SET AUTO entradaGpio = ON;");
        pollingSensoresPrueba = true;       
        obstaculoEntradaRegistrado = false;
    }//GEN-LAST:event_iniciar_cursor_sensores_buttonActionPerformed

    private void abrir_pluma_entrada_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrir_pluma_entrada_buttonActionPerformed
        m4reader.doCommand("UPDATE io SET data=0xFF WHERE type=0 AND mask=0x04;");
        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x08;");
        try {
            TimeUnit.MILLISECONDS.sleep(107);
        } catch (InterruptedException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x04;");
        
        showRaw = true; 
    }//GEN-LAST:event_abrir_pluma_entrada_buttonActionPerformed

    private void cerrar_pluma_entrada_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrar_pluma_entrada_buttonActionPerformed
        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x04;");
        m4reader.doCommand("UPDATE io SET data=0xFF WHERE type=0 AND mask=0x08;");
        try {
            TimeUnit.MILLISECONDS.sleep(107);
        } catch (InterruptedException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        m4reader.doCommand("UPDATE io SET data=0x00 WHERE type=0 AND mask=0x08;");
        
        showRaw = true;
    }//GEN-LAST:event_cerrar_pluma_entrada_buttonActionPerformed

    private void actualizar_base_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizar_base_buttonActionPerformed
        PreparedStatement pre;
        String epc = "";
        String nombre = "";
        String matricula = "";
        String estado = "";
        String modelo = ""; 
        
        try
        {
            
            epc = epc_alta_textfield.getText();
            nombre = nombre_alta_textfield.getText();
            matricula = matricula_alta_textfield.getText();
            estado = estado_alta_textfield.getText();
            modelo = modelo_alta_textfield.getText();
            
            pre = conn.prepareStatement("INSERT INTO Tarjetones (tagEPC, nombre, matricula, estado, modelo) VALUES(?,?,?,?,?)");            
            pre.setString(1, epc);
            pre.setString(2, nombre);
            pre.setString(3, matricula);
            pre.setString(4, estado);
            pre.setString(5, modelo);
            
            pre.executeUpdate();
            pre.close();
            debug_text_area.append("Actualización de base datos exitosa.\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
        }
        catch (Exception e) 
        { 
            debug_text_area.append("Error al enviar la información a la base de datos.\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
        }
        
        try {
            File f = new File(foto_path_alta);
            FileInputStream fis = new FileInputStream(f);
            
            nombre = nombre_alta_textfield.getText();
            pre = conn.prepareStatement("INSERT INTO Fotos (tagEPC, foto) VALUES (?,?)");
            pre.setString(1, epc);
            pre.setBinaryStream(2, fis, (int)f.length());
            
            pre.executeUpdate();
            pre.close();
            debug_text_area.append("Actualización de base de fotos exitosa.\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
        }
        catch (Exception e) 
        { 
            debug_text_area.append("Error al enviar la foto a la base de datos.\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
        }
    }//GEN-LAST:event_actualizar_base_buttonActionPerformed

    private void foto_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foto_buttonActionPerformed

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("C:/Users/rodla/Pictures/"));
       
        int result = jFileChooser.showOpenDialog(new JFrame());
     
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jFileChooser.getSelectedFile();
            show("Selected file: " + selectedFile.getAbsolutePath());
            foto_path_alta = selectedFile.getAbsolutePath();
            
            BufferedImage img;
            try {
                img = ImageIO.read(new File(foto_path_alta));
                Image dimg = img.getScaledInstance(foto_alta_label.getWidth(), foto_alta_label.getHeight(), Image.SCALE_SMOOTH);
                foto_alta_label.setText("");
                foto_alta_label.setIcon(new javax.swing.ImageIcon(dimg));
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }//GEN-LAST:event_foto_buttonActionPerformed

    private void detener_cursor_sensores_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detener_cursor_sensores_buttonActionPerformed
        m4reader.doCommand("SET auto = OFF");
        pollingSensoresPrueba = false; 
    }//GEN-LAST:event_detener_cursor_sensores_buttonActionPerformed

    private void epc_alta_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_epc_alta_buttonActionPerformed
        m4reader.doCommand("SELECT data FROM tag_data WHERE protocol_id='GEN2' AND antenna_id=1 AND mem_bank=1 AND block_number=2 AND block_count=6;");
        altaEPC = true;
        //label = "EPC leído para alta"; 
        //expectedResponse = "0x";
    }//GEN-LAST:event_epc_alta_buttonActionPerformed

    private void auto_run_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_run_buttonActionPerformed
        if (auto_run_button.isSelected()) {
            m4reader.doCommand("DECLARE entradaGpio CURSOR FOR SELECT data FROM io WHERE type=0 AND mask=0x02;");
            m4reader.doCommand("SET AUTO entradaGpio = ON;");
            debug_text_area.append("Auto run encendido\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
            autoRun = true; 
            estadoAutoRun = 0;
        }
        else if(!auto_run_button.isSelected()) {
            m4reader.doCommand("SET AUTO = OFF;");
            debug_text_area.append("Auto run apagado\n");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
            autoRun = false;
        }
    }//GEN-LAST:event_auto_run_buttonActionPerformed

    private void limpiar_log_autorun_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiar_log_autorun_buttonActionPerformed
        autorun_textarea.setText("");
    }//GEN-LAST:event_limpiar_log_autorun_buttonActionPerformed

    private void forzar_abrir_autorun_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forzar_abrir_autorun_buttonActionPerformed
        estadoAutoRun = ABRIR_PLUMA;
    }//GEN-LAST:event_forzar_abrir_autorun_buttonActionPerformed

    private void consultar_usuario_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consultar_usuario_buttonActionPerformed
        String epc = "";
        String nombre, modelo, estado; 
        try
        {
            String sql = "SELECT tagEPC, nombre, estado, modelo FROM Tarjetones "
                            + "WHERE matricula = '" + matricula_consulta_textfield.getText() + "'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
             if (rs.next()) {
                do {
                    epc = rs.getString("tagEPC");
                    nombre = rs.getString("nombre");
                    estado = rs.getString("estado");
                    modelo = rs.getString("modelo");
                    
                    nombre_consulta_textfield.setText(nombre);
                    epc_consulta_textfield.setText(epc);
                    estado_consulta_textfield.setText(estado);
                    modelo_consulta_textfield.setText(modelo);
                }
                while (rs.next());             
            }
            else {
                 debug_text_area.append("Información no generada. Registra tu tarjetón.");
                 debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
             }
            rs.close ();
            ps.close ();
        }
        catch (Exception e) 
        { 
            debug_text_area.append("Error de acceso a base de datos");
            debug_text_area.setCaretPosition(debug_text_area.getDocument().getLength() - 1);
        }
        
        cargarFotoDeBase(epc, foto_consulta_label);
    }//GEN-LAST:event_consultar_usuario_buttonActionPerformed

    private void forzar_lectura_autorun_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forzar_lectura_autorun_buttonActionPerformed
        m4reader.doCommand("SET AUTO = OFF;");
        m4reader.doCommand("DECLARE leerEPC CURSOR FOR SELECT data FROM tag_data WHERE protocol_id='GEN2' AND antenna_id=1 AND mem_bank=1 AND block_number=2 AND block_count=6;");
        m4reader.doCommand("SET AUTO leerEPC = ON;");
        
        estadoAutoRun = LEER_EPC;
    }//GEN-LAST:event_forzar_lectura_autorun_buttonActionPerformed

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
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abrir_pluma_entrada_button;
    private javax.swing.JButton actualizar_base_button;
    private javax.swing.JToggleButton auto_run_button;
    private javax.swing.JTextArea autorun_textarea;
    private javax.swing.JButton cerrar_pluma_entrada_button;
    private javax.swing.JButton clear_debug_text_area_button;
    private javax.swing.JButton consultar_usuario_button;
    private javax.swing.JTextArea debug_text_area;
    private javax.swing.JButton detener_cursor_sensores_button;
    private javax.swing.JButton ejecutar_epc;
    private javax.swing.JButton ejecutar_potencia;
    private javax.swing.JButton epc_alta_button;
    private javax.swing.JTextField epc_alta_textfield;
    private javax.swing.JTextField epc_consulta_textfield;
    private javax.swing.JTextField epc_control_lector_pluma_textfield;
    private javax.swing.JRadioButton escribir_epc_button_from_group;
    private javax.swing.JRadioButton escribir_potencia_button_from_group;
    private javax.swing.JTextField estado_alta_textfield;
    private javax.swing.JTextField estado_consulta_textfield;
    private javax.swing.JButton forzar_abrir_autorun_button;
    private javax.swing.JButton forzar_lectura_autorun_button;
    private javax.swing.JLabel foto_alta_label;
    private javax.swing.JButton foto_button;
    private javax.swing.JLabel foto_caseta_label;
    private javax.swing.JLabel foto_consulta_label;
    private javax.swing.JButton iniciar_cursor_sensores_button;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton leer_epc_button_from_group;
    private javax.swing.ButtonGroup leer_escribir_epc_button;
    private javax.swing.ButtonGroup leer_escribir_potencia_button;
    private javax.swing.JRadioButton leer_potencia_button_from_group;
    private javax.swing.JButton limpiar_log_autorun_button;
    private javax.swing.JTextField matricula_alta_textfield;
    private javax.swing.JTextField matricula_consulta_textfield;
    private javax.swing.JTextField modelo_alta_textfield;
    private javax.swing.JTextField modelo_consulta_textfield;
    private javax.swing.JTextField nombre_alta_textfield;
    private javax.swing.JTextField nombre_consulta_textfield;
    private javax.swing.JTextField potencia_textfield;
    // End of variables declaration//GEN-END:variables
}
