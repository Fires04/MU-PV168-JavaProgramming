package cz.muni.fi.pv168.clockcard;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

/**
 * Login Manager Dialog.
 *
 * @author Marek Osvald
 * @version 2011.0629
 */
public class LoginManagerDialog extends javax.swing.JDialog {

    /** Creates new form LoginManagerDialog */
    private Frame parent;
    private static LoginManagerDialog thisWindows;
    public LoginManagerDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.parent=parent;
        initComponents();
        setThisWindows();
    }

    public static LoginManagerDialog getThisWindows() {
        return thisWindows;
    }

    private void setThisWindows(){
        thisWindows=this;
    }

    class loginManagerAction extends AbstractAction{
        public void actionPerformed(ActionEvent e) {
            new loginManagerWorker().execute();
        }
    }

    class cancelAction extends AbstractAction{
        public void actionPerformed(ActionEvent e) {
            dispose();
            parent.setVisible(true);
        }
    }

    class loginManagerWorker extends SwingWorker<Integer, Integer>{
        @Override
        protected Integer doInBackground() throws Exception {
            ResourceBundle.clearCache();
            ResourceBundle translationResource = ResourceBundle.getBundle("Translation", Locale.getDefault());
            labelInfo.setText(translationResource.getString("LoginWorkerDialog.logging"));
            String text = String.valueOf(passPassword.getPassword());
            if(Supervisor.getInstance().authenticate(text)){
                LoginManagerDialog.getThisWindows().dispose();
                new ManagerForm().setVisible(true);
            }else{
                labelInfo.setText(translationResource.getString("LoginWorkerDialog.wrongPassword"));
            }
            return 0;
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCancel = new javax.swing.JButton();
        labelPassword = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();
        passPassword = new javax.swing.JPasswordField();
        labelInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("Translation"); // NOI18N
        setTitle(bundle.getString("LoginManagerDialog.title")); // NOI18N
        setResizable(false);

        btnCancel.setAction(new cancelAction());
        btnCancel.setText(bundle.getString("LoginManagerDialog.btnCancel.text")); // NOI18N

        labelPassword.setText(bundle.getString("LoginManagerDialog.labelPassword.text")); // NOI18N

        btnOk.setAction(new loginManagerAction());
        btnOk.setText(bundle.getString("LoginManagerDialog.btnOk.text")); // NOI18N

        labelInfo.setText(bundle.getString("LoginManagerDialog.labelInfo.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelPassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(passPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancel, btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(labelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPassword)
                    .addComponent(passPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel labelInfo;
    private javax.swing.JLabel labelPassword;
    private javax.swing.JPasswordField passPassword;
    // End of variables declaration//GEN-END:variables

}
