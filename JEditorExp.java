
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

public class JEditorExp {

    public void displayPage(String url) {
        final JFrame frame = new JFrame("JEditorPane Demo");
        String initialURL = url;
        final JEditorPane ed;

        JLabel lblURL = new JLabel("URL");
        final JTextField txtURL = new JTextField(initialURL, 30);
        JButton btnBrowse = new JButton("Browse");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(lblURL);
        panel.add(txtURL);
        panel.add(btnBrowse);

        try {
            ed = new JEditorPane(initialURL);
            ed.setEditable(false);

            ed.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        JEditorPane pane = (JEditorPane) e.getSource();
                        if (e instanceof HTMLFrameHyperlinkEvent) {
                            HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                            HTMLDocument doc = (HTMLDocument) pane.getDocument();
                            doc.processHTMLFrameHyperlinkEvent(evt);
                        } else {
                            try {
                                pane.setPage(e.getURL());
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }
                }
            });

            btnBrowse.addActionListener(
                    new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            try {
                                ed.setPage(txtURL.getText().trim());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
            JScrollPane sp = new JScrollPane(ed);
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel, BorderLayout.NORTH);
            frame.getContentPane().add(sp, BorderLayout.CENTER);

            frame.setSize(500, 350);
            frame.setVisible(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}