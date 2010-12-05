package cider.client.gui;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ExampleGUILaunch extends JFrame
{
    // just throwing this together because nobody else has
    public static void main(String[] args)
    {
        new ExampleGUILaunch();
    }

    ExampleGUILaunch()
    {
        JFrame f = new JFrame("CIDEr - A Collaborative Coding Experience :)");
        ExampleBasicGUI g = new ExampleBasicGUI();
        g.setOpaque(true);
        f.add(g);
        f.setContentPane(g);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        f.setResizable(false);
        f.pack();
    }
}