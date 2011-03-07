package cider.client.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DebugWindow extends JPanel
{
    JTextArea textArea = new JTextArea();
    JScrollPane scrollTextArea = new JScrollPane(textArea);

    public DebugWindow()
    {
        this.setLayout(new BorderLayout());
        //this.scrolltxt.setBounds(3, 3, 300, 200);
        this.add(this.scrollTextArea);
    }

    public String getText()
    {
        return this.textArea.getText();
    }

    public void setText(String text)
    {
        this.textArea.setText(text);
    }

    public void println(String text)
    {
        this.textArea.setText(this.textArea.getText() + text/* + "\n"*/);
    }
}
