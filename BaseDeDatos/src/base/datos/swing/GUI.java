/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base.datos.swing;
import java.sql.*;
import java.io.*;
import java.net.*;
import ThingMagic.Readers.*;

/**
 *
 * @author rodla
 */
public class GUI extends javax.swing.JFrame {
    Connection conn = null;
    PreparedStatement pre;
    String epc = "";
    String nombre = "";
    String matricula = "";
    String tipopersonal = "";
    String archivo ="";
    private MercuryReader m4reader;

    /**
     * Creates new form GUI
     */
    public GUI() {
        initComponents();
        initRFIDComponents();
        initDBComponents();
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
    private void initDBComponents(){
     
        try
        { 
            String driverName = "com.mysql.jdbc.Driver";
            Class.forName(driverName);


	   //  Código usado para un servidor local
           /*
            String serverName = "localhost";
            String mydatabase = "credencial";
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = "root";
            String password = "";
           */
           
	   // Código ejemplo usado para un servidor remoto
          
            String serverName = "104.198.195.139";
            String mydatabase = "credencial";
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = "root";
            String password = "Sonic1995";
           
            conn = DriverManager.getConnection(url, username, password);
            
	    // Usar el siguiente código en caso de querer eliminar la tabla PersonasTEC 
            pre = conn.prepareStatement("DROP TABLE IF EXISTS PersonasTEC");
            pre.executeUpdate();

            pre = conn.prepareStatement(
               "CREATE TABLE PersonasTEC ("
               + "id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
               + "tagEPC VARCHAR(26) NOT NULL,"
               + "nombre VARCHAR(50) NOT NULL,"
               + "matricula CHAR(9) NOT NULL,"
               + "tipopersonal VARCHAR(20),"
               + "PRIMARY KEY(id))");
            pre.executeUpdate();

            show("Conexión a la base de datos exitosa.");
            
        }
        catch (Exception e)
        {
            show("No se pudo establecer conexión con la base de datos.");
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldEPC2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldNombre2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldMatricula2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldPersonal2 = new javax.swing.JTextField();
        jButtonEnviarSQL = new javax.swing.JButton();
        jButtonLeerEPC2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldEPC1 = new javax.swing.JTextField();
        jButtonLeeSQL = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldMatricula1 = new javax.swing.JTextField();
        jTextFieldNombre1 = new javax.swing.JTextField();
        jTextFieldPersonal1 = new javax.swing.JTextField();
        jButtonLeerEPC = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaShow = new javax.swing.JTextArea();
        jButtonExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel1.setText("ACTUALIZACIÓN DE INFORMACIÓN A BASE DE DATOS MYSQL");

        jLabel2.setText("Ingresar datos en los siguientes campos");

        jLabel3.setText("EPC (24 valores en HEX):");

        jLabel4.setText("Nombre:");

        jLabel5.setText("Matrícula:");

        jLabel6.setText("Tipo de personal:");

        jButtonEnviarSQL.setText("Enviar información a base de datos");
        jButtonEnviarSQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEnviarSQLActionPerformed(evt);
            }
        });

        jButtonLeerEPC2.setText("Leer EPC del tag");
        jButtonLeerEPC2.setActionCommand("Leer EPC del tag");
        jButtonLeerEPC2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeerEPC2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonEnviarSQL, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 97, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(44, 44, 44)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldPersonal2, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                            .addComponent(jTextFieldMatricula2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                            .addComponent(jTextFieldNombre2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextFieldEPC2, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonLeerEPC2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldEPC2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jButtonLeerEPC2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldNombre2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldMatricula2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldPersonal2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(jButtonEnviarSQL)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Actualización de Base de Datos", jPanel2);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel7.setText("LECTURA DE BASE DE DATOS");

        jLabel8.setText("Ingrese código EPC (24 caracteres HEX):");

        jLabel9.setText("EPC:");

        jTextFieldEPC1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEPC1ActionPerformed(evt);
            }
        });

        jButtonLeeSQL.setText("Leer información de base de datos");
        jButtonLeeSQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeeSQLActionPerformed(evt);
            }
        });

        jLabel10.setText("RESULTADOS");

        jLabel11.setText("Nombre:");

        jLabel12.setText("Matrícula:");

        jLabel13.setText("Tipo de Personal:");

        jTextFieldMatricula1.setEditable(false);

        jTextFieldNombre1.setEditable(false);

        jTextFieldPersonal1.setEditable(false);

        jButtonLeerEPC.setText("Leer EPC del tag");
        jButtonLeerEPC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeerEPCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldNombre1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldEPC1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addComponent(jButtonLeeSQL))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel10))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldMatricula1, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPersonal1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLeerEPC, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextFieldEPC1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLeerEPC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLeeSQL)
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextFieldNombre1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextFieldMatricula1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextFieldPersonal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Lectura de Base de Datos", jPanel1);

        jTextAreaShow.setColumns(20);
        jTextAreaShow.setRows(5);
        jScrollPane3.setViewportView(jTextAreaShow);

        jButtonExit.setText("Salir de la aplicacion");
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonExit)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldEPC1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEPC1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldEPC1ActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (Exception e) { /* ignore close errors */}
        }
        System.exit(0);

    }//GEN-LAST:event_jButtonExitActionPerformed

    //METODO PARA LEER DE DB (METER AL LOOP PRINCIPAL)
    private void jButtonLeeSQLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLeeSQLActionPerformed
        try
        {
            String sql = "SELECT nombre, matricula, tipopersonal FROM PersonasTEC "
                            + "WHERE tagEPC = '" + jTextFieldEPC1.getText() + "'";
            PreparedStatement ps = conn.prepareStatement (sql);
            ResultSet rs = ps.executeQuery();
            
             if (rs.next()) {
                do {
                    String nameVal = rs.getString("nombre");
                    String matriculaVal = rs.getString("matricula");
                    String tipoperVal = rs.getString("tipopersonal");

                    jTextFieldNombre1.setText(nameVal);
                    jTextFieldMatricula1.setText(matriculaVal);
                    jTextFieldPersonal1.setText(tipoperVal);

                    show("Lectura de base datos exitosa.");
                }
                while (rs.next());
            }
            else {
                jTextFieldNombre1.setText("Información no generada");
                jTextFieldMatricula1.setText("Información no generada");
                jTextFieldPersonal1.setText("Información no generada");
                
                show("Este EPC aún no ha sido registrado en la Base de Datos remota");
            }
            rs.close ();
            ps.close ();
            
           
        }
        catch (Exception e) 
        { 
            show("Error. No se pudo ejecutar la función SELECT.");
        }

    }//GEN-LAST:event_jButtonLeeSQLActionPerformed

    //AGREGAR VALIDACION SI YA EXISTE EL REGISTRO, DIRECCIONAR A CREAR O ACTUALIZAR
    private void jButtonEnviarSQLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnviarSQLActionPerformed
        try
        {
                        
            epc = jTextFieldEPC2.getText();
            nombre = jTextFieldNombre2.getText();
            matricula = jTextFieldMatricula2.getText();
            tipopersonal = jTextFieldPersonal2.getText();
            
            pre = conn.prepareStatement("INSERT INTO PersonasTEC (tagEPC, nombre, matricula, tipopersonal) VALUES(?,?,?,?)");            
            pre.setString(1, epc);
            pre.setString(2, nombre);
            pre.setString(3, matricula);
            pre.setString(4, tipopersonal);
            pre.executeUpdate();
            
            pre.close();
            
            show("Actualización de base datos exitosa.");
        }
        catch (Exception e) 
        { 
            show("Error al enviar la información a la base de datos.");
        }
    }//GEN-LAST:event_jButtonEnviarSQLActionPerformed

    private void jButtonLeerEPCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLeerEPCActionPerformed
        
        String cmd = "SELECT data FROM tag_data WHERE protocol_id='GEN2' AND antenna_id=1 AND mem_bank=1 AND block_number=2 AND block_count=6;";
        m4reader.doCommand(cmd);
        
    }//GEN-LAST:event_jButtonLeerEPCActionPerformed

    //METODO PARA LEER EPC (METER AL LOOP)
    private void jButtonLeerEPC2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLeerEPC2ActionPerformed
        // TODO add your handling code here:
        String cmd = "SELECT data FROM tag_data WHERE protocol_id='GEN2' AND antenna_id=1 AND mem_bank=1 AND block_number=2 AND block_count=6;";
        m4reader.doCommand(cmd);
    }//GEN-LAST:event_jButtonLeerEPC2ActionPerformed

    public void show(String response) { 
        jTextAreaShow.append(response + "\n");
    }
    
    //METODO PARA POBLAR TEXTFIELD EPC
    public void fillEPC(String response){
        if(response.contains("0x")){
            jTextFieldEPC1.setText(response);
            jTextFieldEPC2.setText(response);
        }
    }

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

        //LOOP PARA LEER EPC Y POBLAR CAMPO DE TEXTO y LEER DE BASE DE DATOS CON EL EPC OBTENIDO
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonEnviarSQL;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonLeeSQL;
    private javax.swing.JButton jButtonLeerEPC;
    private javax.swing.JButton jButtonLeerEPC2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaShow;
    private javax.swing.JTextField jTextFieldEPC1;
    private javax.swing.JTextField jTextFieldEPC2;
    private javax.swing.JTextField jTextFieldMatricula1;
    private javax.swing.JTextField jTextFieldMatricula2;
    private javax.swing.JTextField jTextFieldNombre1;
    private javax.swing.JTextField jTextFieldNombre2;
    private javax.swing.JTextField jTextFieldPersonal1;
    private javax.swing.JTextField jTextFieldPersonal2;
    // End of variables declaration//GEN-END:variables
}
