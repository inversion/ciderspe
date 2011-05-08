package cider.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class ImagePreview extends JPanel implements PropertyChangeListener
{
	private static final long serialVersionUID = 2071277738527711801L;
	
	private Image img;
	private ImageIcon icon;
	private int w, h;
	private Color bg;
	private final int size = 150;
	private File f;
	
	public ImagePreview() 
	{
		setPreferredSize(new Dimension(200, 200));
		bg = getBackground();
	}
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (name.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
		{
			f = (File)evt.getNewValue();
			String path;
			if (f == null)
				return;
			else
				path = f.getAbsolutePath();
			System.out.println(path);
			if ((path != null) && (
					path.toLowerCase().endsWith(".jpg") ||
					path.toLowerCase().endsWith(".jpeg")||
					path.toLowerCase().endsWith(".gif") ||
					path.toLowerCase().endsWith(".png") ||
					path.toLowerCase().endsWith(".bmp") ))
			{
				icon = new ImageIcon(path);
				img = icon.getImage();
				scaleImage();
				loadImage();
				repaint();
			}					
		}
	}
	
	public void loadImage() 
	{
		if (f == null) 
		{
			icon = null;
			return;
		}

		ImageIcon tmpIcon = new ImageIcon(f.getPath());

		if (tmpIcon != null) {
			if (tmpIcon.getIconWidth() > 200) {
				icon = new ImageIcon(tmpIcon.getImage().getScaledInstance(
						200, -1, Image.SCALE_FAST));
			} else {
				icon = tmpIcon;
			}
		}
	}
	
	private void scaleImage() 
	{
		w = img.getWidth(this);
		h = img.getHeight(this);
		double ratio = 1.0;
		
		if (h > size)
		{
			ratio = (double)size/h;
			h = size;
			w = (int) (w * ratio);
		}
		else if (w >= h)
		{
			ratio = (double)size/w;
			w = size;
			h = (int) (h * ratio);
		}
		else
		{
			ratio = (double) getHeight() / h;
			h = getHeight();
			w = (int) (w * ratio);
		}
		img = img.getScaledInstance(w, h, Image.SCALE_DEFAULT);
	}
	
	public void paintComponent (Graphics g)
	{
		g.setColor(bg);
		g.fillRect(0, 0, size+200, getHeight());
		g.drawImage(img, getWidth()/ 2-w / 2 + 5, getHeight() / 2 - h / 2, this);
		
	}

}