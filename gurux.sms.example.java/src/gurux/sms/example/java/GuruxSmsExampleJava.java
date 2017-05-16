//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.sms.example.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.sms.GXSMSBatteryInfo;
import gurux.sms.GXSMSMessage;
import gurux.sms.GXSMSSignalQualityInfo;
import gurux.sms.enums.NetworkState;

/**
 *
 * Gurux SMS example for Java.
 */
public class GuruxSmsExampleJava extends javax.swing.JFrame
        implements IGXMediaListener, ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Timer MessageTimer;
    Timer StatusTimer;

    @Override
    public void onError(Object sender, Exception ex) {
        try {
            sms.close();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception Ex) {
            JOptionPane.showMessageDialog(this, Ex.getMessage());
        }
    }

    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {
        try {
            DefaultListModel<String> m =
                    (DefaultListModel<String>) ReceivedTB.getModel();
            GXSMSMessage msg = (GXSMSMessage) e.getData();
            m.add(m.size(), msg.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /*
     * Read statistics.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == StatusTimer) {
            GXSMSSignalQualityInfo si = sms.getSignalQuality();
            GXSMSBatteryInfo bi = sms.getBatteryCharge();
            RSSI.setText("RSSI: " + String.valueOf(si.getRssi()));
            BER.setText("BER: " + String.valueOf(si.getBer()));
            Battery.setText(
                    "Battery: " + String.valueOf(bi.getBatteryCapacity()));
            Power.setText("Power: "
                    + String.valueOf(bi.getAveragePowerConsumption()));
            NetworkState state = sms.getNetworkState();
            switch (state) {
            case NOT_REGISTERED:
                Network.setText("Network: Not Registered");
                break;
            case HOME:
                Network.setText("Network: Home");
                break;
            case SEARCHING:
                Network.setText("Network: Searching");
                break;
            case DENIED:
                Network.setText("Network: Denied");
                break;
            case UNKNOWN:
                Network.setText("Network: Unknown");
                break;
            case ROAMING:
                Network.setText("Network: Roaming");
                break;

            }
        } else if (e.getSource() == MessageTimer) {
            this.ReceivedStat
                    .setText(String.valueOf(sms.getMessagesReceived()));
            this.SentStat.setText(String.valueOf(sms.getMessagesSent()));
            sms.resetMessageCounters();
        } else {
            return;
        }
    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {
        try {
            boolean IsOpen = e.getState() == MediaState.OPEN;
            // OpenBtm
            OpenBtn.setEnabled(!IsOpen);
            // Close Btn.
            CloseBtn.setEnabled(IsOpen);
            // Send Btn.
            SendBtn.setEnabled(IsOpen);
            // Read Btn.
            ReadBtn.setEnabled(IsOpen);
            // Delete Btn.
            DeleteBtn.setEnabled(IsOpen);
            // Info Btn.
            InfoBtn.setEnabled(IsOpen);
            SendTB.setEnabled(IsOpen);

            if (IsOpen) {
                StatusTimer = new Timer(10000, this);
                StatusTimer.setInitialDelay(1000);
                StatusTimer.start();
                MessageTimer = new Timer(60000, this);
                MessageTimer.setInitialDelay(1000);
                MessageTimer.start();
            } else {
                if (StatusTimer != null) {
                    StatusTimer.stop();
                    StatusTimer = null;
                }
                if (MessageTimer != null) {
                    MessageTimer.stop();
                    MessageTimer = null;
                }
            }
        } catch (Exception Ex) {
            JOptionPane.showMessageDialog(this, Ex.getMessage());
        }
    }

    // We do not use trace at the moment.
    @Override
    public void onTrace(Object sender, TraceEventArgs e) {

    }

    // We do not need notify if property is changed.
    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {

    }

    gurux.sms.GXSMS sms = new gurux.sms.GXSMS();

    /**
     * Creates new form test
     */
    public GuruxSmsExampleJava() {
        initComponents();
        sms.addListener(this);
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        ReceivedTB.setModel(listModel);
        DefaultListModel<String> m =
                (DefaultListModel<String>) ReceivedTB.getModel();
        m.clear();
        onMediaStateChange(sms, new MediaStateEventArgs(MediaState.CLOSED));
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        OpenBtn = new javax.swing.JButton();
        SendTB = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ReceivedTB = new javax.swing.JList<String>();
        CloseBtn = new javax.swing.JButton();
        PropertiesBtn = new javax.swing.JButton();
        SendBtn = new javax.swing.JButton();
        ReadBtn = new javax.swing.JButton();
        DeleteBtn = new javax.swing.JButton();
        InfoBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        SentStat = new javax.swing.JLabel();
        ReceivedStat = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        RSSI = new javax.swing.JLabel();
        BER = new javax.swing.JLabel();
        Battery = new javax.swing.JLabel();
        Power = new javax.swing.JLabel();
        Network = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Received:");

        OpenBtn.setText("Open");
        OpenBtn.setName("OpenBtn"); // NOI18N
        OpenBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("Send:");

        ReceivedTB.setModel(new javax.swing.AbstractListModel<String>() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            String[] strings = new String[0];

            public int getSize() {
                return strings.length;
            }

            public String getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(ReceivedTB);

        CloseBtn.setText("Close");
        CloseBtn.setName(""); // NOI18N
        CloseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseBtnActionPerformed(evt);
            }
        });

        PropertiesBtn.setText("Properties");
        PropertiesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PropertiesBtnActionPerformed(evt);
            }
        });

        SendBtn.setText("Send");
        SendBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendBtnActionPerformed(evt);
            }
        });

        ReadBtn.setText("Read");
        ReadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ReadBtnActionPerformed(evt);
            }
        });

        DeleteBtn.setText("Delete");
        DeleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteBtnActionPerformed(evt);
            }
        });

        InfoBtn.setText("Info");
        InfoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InfoBtnActionPerformed(evt);
            }
        });

        jLabel3.setText("Message statistics in last minute");

        SentStat.setText("Sent:");

        ReceivedStat.setText("Received:");

        jLabel4.setText("Modem Status:");

        RSSI.setText("RSSI");

        BER.setText("BER");

        Battery.setText("Battery:");

        Power.setText("Power");

        Network.setText("Network");

        javax.swing.GroupLayout layout =
                new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addGroup(layout.createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(SendBtn))
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout
                                                .createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(layout
                                                        .createSequentialGroup()
                                                        .addComponent(SendTB)
                                                        .addGap(24, 24, 24))
                                                .addComponent(jScrollPane1,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        289, Short.MAX_VALUE)
                                                .addGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                        layout.createSequentialGroup()
                                                                .addGroup(layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                layout.createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addGroup(
                                                                                                layout.createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                SentStat)
                                                                                                        .addGap(77,
                                                                                                                77,
                                                                                                                77)
                                                                                                        .addComponent(
                                                                                                                ReceivedStat))
                                                                                        .addComponent(
                                                                                                jLabel3))
                                                                        .addGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                layout.createSequentialGroup()
                                                                                        .addGap(10,
                                                                                                10,
                                                                                                10)
                                                                                        .addComponent(
                                                                                                jLabel2)))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)))
                                        .addGroup(layout
                                                .createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(ReadBtn,
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(DeleteBtn,
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(InfoBtn,
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(PropertiesBtn,
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(
                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                        layout.createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(
                                                                        OpenBtn)
                                                                .addComponent(
                                                                        CloseBtn))))
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout
                                                .createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel4)
                                                .addGroup(layout
                                                        .createSequentialGroup()
                                                        .addGroup(layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                .addComponent(
                                                                        RSSI,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(
                                                                        BER,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE))
                                                        .addGap(72, 72, 72)
                                                        .addGroup(layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(
                                                                        Power)
                                                                .addGroup(
                                                                        layout.createSequentialGroup()
                                                                                .addComponent(
                                                                                        Battery)
                                                                                .addGap(80,
                                                                                        80,
                                                                                        80)
                                                                                .addComponent(
                                                                                        Network)))))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap()));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addGap(14, 14, 14)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(SendTB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                52,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(9, 9, 9))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(OpenBtn).addGap(5, 5, 5)
                                        .addComponent(CloseBtn)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PropertiesBtn)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1,
                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(SendBtn,
                                        javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPane1,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                97,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel3)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout
                                                .createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(SentStat)
                                                .addComponent(ReceivedStat)))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(ReadBtn)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(DeleteBtn)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(InfoBtn)))
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(RSSI).addComponent(Battery)
                                .addComponent(Network))
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(BER).addComponent(Power))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*
     * Open Media
     */
    private void OpenBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_OpenBtnActionPerformed
        try {
            sms.open();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_OpenBtnActionPerformed

    /*
     * Close Media.
     */
    private void CloseBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_CloseBtnActionPerformed
        try {
            sms.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_CloseBtnActionPerformed

    /*
     * Show media properties.
     */
    private void PropertiesBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PropertiesBtnActionPerformed
        try {
            sms.properties(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_PropertiesBtnActionPerformed

    /*
     * Send new SMS.
     */
    private void SendBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SendBtnActionPerformed
        try {
            GXSMSMessage msg = new GXSMSMessage();
            msg.setData(SendTB.getText());
            sms.send(msg);
            JOptionPane.showMessageDialog(this, "SMS sent.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_SendBtnActionPerformed

    /*
     * Read available SMSs.
     */
    private void ReadBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ReadBtnActionPerformed
        try {
            GXSMSMessage[] messages = sms.read();
            for (GXSMSMessage it : messages) {
                onReceived(sms, new ReceiveEventArgs(it, null));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_ReadBtnActionPerformed

    /*
     * Delete SMSs.
     */
    private void DeleteBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_DeleteBtnActionPerformed
        try {
            if (JOptionPane.showConfirmDialog(null, "Delete all SMS Messages?",
                    "Delete",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                sms.deleteAll();
                JOptionPane.showMessageDialog(null, "All items are deleted.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_DeleteBtnActionPerformed

    static public String join(String[] list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /*
     * Show modem info.
     */
    private void InfoBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_InfoBtnActionPerformed
        try {
            JOptionPane.showMessageDialog(this, join(sms.getInfo(), "\r\n"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_InfoBtnActionPerformed

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting
        // code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
         * html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                    .getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSmsExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSmsExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSmsExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSmsExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GuruxSmsExampleJava().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BER;
    private javax.swing.JLabel Battery;
    private javax.swing.JButton CloseBtn;
    private javax.swing.JButton DeleteBtn;
    private javax.swing.JButton InfoBtn;
    private javax.swing.JLabel Network;
    private javax.swing.JButton OpenBtn;
    private javax.swing.JLabel Power;
    private javax.swing.JButton PropertiesBtn;
    private javax.swing.JLabel RSSI;
    private javax.swing.JButton ReadBtn;
    private javax.swing.JLabel ReceivedStat;
    private javax.swing.JList<String> ReceivedTB;
    private javax.swing.JButton SendBtn;
    private javax.swing.JTextField SendTB;
    private javax.swing.JLabel SentStat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
