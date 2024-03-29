package co.com.foscal;

import co.com.foscal.ppa.FrmMostrarKDE;
import java.awt.Component;
import java.beans.PropertyVetoException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Feisar Moreno
 * @date 24/02/2016
 */
public class FrmPrincipal extends JFrame {

    public FrmPrincipal() {
        initComponents();
    }

    public JPanel getPanPrincipal() {
        return this.panPrincipal;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panPrincipal = new javax.swing.JPanel();
        mbrPrincipal = new javax.swing.JMenuBar();
        jMenuMetodos = new javax.swing.JMenu();
        mitKDE = new javax.swing.JMenuItem();
        menAyuda = new javax.swing.JMenu();
        mitAcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Análisis de Patrones de Puntos");

        panPrincipal.setBackground(new java.awt.Color(153, 153, 153));

        javax.swing.GroupLayout panPrincipalLayout = new javax.swing.GroupLayout(panPrincipal);
        panPrincipal.setLayout(panPrincipalLayout);
        panPrincipalLayout.setHorizontalGroup(
            panPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
        );
        panPrincipalLayout.setVerticalGroup(
            panPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 434, Short.MAX_VALUE)
        );

        mbrPrincipal.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jMenuMetodos.setText("Métodos");

        mitKDE.setText("Kernel Density Estimation (KDE)");
        mitKDE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitKDEActionPerformed(evt);
            }
        });
        jMenuMetodos.add(mitKDE);

        mbrPrincipal.add(jMenuMetodos);

        menAyuda.setText("Ayuda");

        mitAcercaDe.setText("Acerca de");
        mitAcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitAcercaDeActionPerformed(evt);
            }
        });
        menAyuda.add(mitAcercaDe);

        mbrPrincipal.add(menAyuda);

        setJMenuBar(mbrPrincipal);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mitAcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitAcercaDeActionPerformed
        this.abrirItemMenu(ItemsMenuPpal.ACERCA_DE);
    }//GEN-LAST:event_mitAcercaDeActionPerformed

    private void mitKDEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitKDEActionPerformed
        this.abrirItemMenu(ItemsMenuPpal.MOSTRAR_KDE);
    }//GEN-LAST:event_mitKDEActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }

        //Create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmPrincipal frmPrincipal = new FrmPrincipal();
                frmPrincipal.setExtendedState(FrmPrincipal.MAXIMIZED_BOTH);
                frmPrincipal.setVisible(true);
                frmPrincipal.abrirItemMenu(ItemsMenuPpal.MOSTRAR_KDE);
            }
        });
    }

    public void abrirItemMenu(ItemsMenuPpal itemMenuPpal) {
        Component comps[] = this.panPrincipal.getComponents();
        int i = 0;
        while (comps != null && i < comps.length) {
            this.panPrincipal.remove(comps[i]);
            i++;
        }
        this.panPrincipal.revalidate();
        this.panPrincipal.repaint();

        switch (itemMenuPpal) {
            case MOSTRAR_KDE:
                FrmMostrarKDE frmMostrarKDE = new FrmMostrarKDE(this);
                this.panPrincipal.add(frmMostrarKDE);
                frmMostrarKDE.setVisible(true);
                try {
                    frmMostrarKDE.setMaximum(true);
                } catch (PropertyVetoException ex) {
                }
                break;
            case ACERCA_DE:
                FrmAcercaDe frmAcercaDe = new FrmAcercaDe();
                this.panPrincipal.add(frmAcercaDe);
                frmAcercaDe.setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Option value unknown", "Main Menu", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenuMetodos;
    private javax.swing.JMenuBar mbrPrincipal;
    private javax.swing.JMenu menAyuda;
    private javax.swing.JMenuItem mitAcercaDe;
    private javax.swing.JMenuItem mitKDE;
    private javax.swing.JPanel panPrincipal;
    // End of variables declaration//GEN-END:variables
}
