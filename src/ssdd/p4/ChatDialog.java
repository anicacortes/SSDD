/**
 * Fichero ChatDialog.java
 * Sencilla implementación de una interfaz gráfica de usuario para aplicaciones de chat.
 * Autor: Javier Celaya, 2013
 * jcelaya@unizar.es
 * Este código puede ser reproducido y modificado con libertad, mientras se mantenga su autoría.
 */
package ssdd.p4;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatDialog extends JFrame {
    private JButton    sendButton = new JButton("Enviar");
    private JTextField messageField = new JTextField();
    private JTextArea  messageList = new JTextArea();

    public ChatDialog(final ActionListener l) {
        // Todo el código que modifica componentes de Swing debe ejecutarse
        // en el hilo de eventos de Swing.
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            messageList.setEditable(false);
            messageList.setLineWrap(true);
            messageList.setWrapStyleWord(true);
            sendButton.addActionListener(l);
            messageField.addActionListener(l);

            JScrollPane scroller = new JScrollPane(messageList, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroller.setBorder(BorderFactory.createEmptyBorder(6, 6, 5, 5));
            messageList.setBorder(BorderFactory.createLoweredBevelBorder());

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(scroller, BorderLayout.CENTER);

            JPanel messagePanel = new JPanel();
            messagePanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 5, 5));
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
            messagePanel.add(messageField);
            messagePanel.add(sendButton);
            getContentPane().add(messagePanel, BorderLayout.SOUTH);

            pack();
            setSize(400, 600);
            setVisible(true);
        }});
    }

    public void addMessage(final String text) {
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            messageList.append(text + '\n'); 
        }});
    }

    public String text() {
        String m = messageField.getText();
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            messageField.setText("");
        }});
        return m;
    }
}

