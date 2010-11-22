package cider.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class BasicGUI extends JPanel implements ActionListener
{

    public BasicGUI()
    {
        JButton quickPush = new JButton("DEV: quickPush");
        quickPush.addActionListener(this);
        quickPush.setBounds(300, 300, 50, 50);
        add(quickPush);

        JTextArea text = new JTextArea(
                "class Hello{\n\tpublic static void main(String[] args) \n\t{\n\t\tSystem.out.println(\"hello\");\n\t}\n}");
        text.setRows(20);
        add(text);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand() == "DEV: quickPush")
        {
            System.out.println("oh good.");
        }

    }

}