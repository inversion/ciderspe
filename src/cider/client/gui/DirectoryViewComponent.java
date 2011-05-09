/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cider.common.network.client.Client;
import cider.common.processes.LiveFolder;

/**
* Panel that holds the directory tree in the main windows
*/
public class DirectoryViewComponent extends JPanel
{
	/**
	 * Whether in debug mode
	 */
    public static boolean DEBUG = true;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param parent
     * @param name
     * @return
     */
    public static List<Element> getChildrenByTagName(Element parent, String name)
    {
        List<Element> nodeList = new ArrayList<Element>();
        for (Node child = parent.getFirstChild(); child != null; child = child
                .getNextSibling())
        {
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && name.equals(child.getNodeName()))
            {
                nodeList.add((Element) child);
            }
        }

        return nodeList;
    }

    private DefaultMutableTreeNode top;
    private Client client;
    private JTree tree;

    private LiveFolder rootFolder = new LiveFolder("root", "Bot");

    public DirectoryViewComponent()
    {
        top = new DefaultMutableTreeNode("root");
        // createNodes(top);

        tree = new JTree(top);
        // tree.setModel(model);

        JScrollPane scrollpane = new JScrollPane(tree);

        // new JLabel("directory tree");
        //
        // new JLabel("code");
        //
        // new JLabel("chat, oh hai");

        this.setLayout(new BorderLayout());

        JPanel filesHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Box box = Box.createHorizontalBox();

        URL u = this.getClass().getResource("iconfiles.png");
        ImageIcon image = new ImageIcon(u);
        JLabel lblImage = new JLabel(image);
        box.add(lblImage);
        box.add(new JLabel(" File Explorer"));
        filesHeader.add(box);

        this.add(filesHeader, BorderLayout.NORTH);
        this.add(scrollpane, BorderLayout.CENTER);
        this
                .setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                        Color.BLACK));
    }

    /**
     * Wipes the current directory tree and makes a new one.
     * 
     * @param xml
     *            The XML representation of the tree to build
     */
    public void constructTree(String xml)
    {
        top.removeAllChildren();
        rootFolder.removeAllChildren();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            try
            {
                StringWriter xmlBuffer = new StringWriter();
                xmlBuffer.write(xml);
                xmlBuffer.close();
                ByteArrayInputStream xmlParseInputStream = new ByteArrayInputStream(
                        xmlBuffer.toString().getBytes());
                xmlParseInputStream.close();
                Document xmlDoc = db.parse(xmlParseInputStream);

                Element docEle = xmlDoc.getDocumentElement();

                this.subFolders(docEle, top, rootFolder);
                this.parseDocs(docEle, top, rootFolder);

                if (DEBUG)
                {
                    System.out.println("Reconstructed Tree: ");
                    System.out.println(rootFolder.xml(""));
                }

            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        }

        tree.addTreeSelectionListener(new DirectoryViewSelectionListener(tree,
                client));
    }

    /**
     * Creates some objects to simulate files and folders etc
     * 
     * @deprecated
     * @param top Root node
     */
    @Deprecated
    public void createNodes(DefaultMutableTreeNode top)
    {

        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        category = new DefaultMutableTreeNode("Folder 1");
        top.add(category);

        for (int i = 0; i < 50; i++)
        {
            book = new DefaultMutableTreeNode("File 1");
            category.add(book);
        }

        category = new DefaultMutableTreeNode("Folder 2");
        top.add(category);

        book = new DefaultMutableTreeNode("File 2");
        category.add(book);
    }

    /**
     * Returns the directory tree XML representation
     * @return Root of the tree
     */
    public LiveFolder getLiveFolder()
    {
        return rootFolder;
    }

    private void parseDocs(Element docEle, DefaultMutableTreeNode folder,
            LiveFolder parentFolder)
    {
        List<Element> docs = getChildrenByTagName(docEle, "Doc");
        if (docs != null && docs.size() > 0)

        {
            for (int i = 0; i < docs.size(); i++)
            {
                Element el = docs.get(i);
                Element el2 = getChildrenByTagName(el, "Name").get(0);
                String docName = el2.getChildNodes().item(0).getNodeValue()
                        .trim();
                folder.add(new DefaultMutableTreeNode(docName));
                el2 = getChildrenByTagName(el, "CreationTime").get(0);
                String creationTime = el2.getChildNodes().item(0)
                        .getNodeValue().trim();
                parentFolder
                        .makeDocument(docName, Long.parseLong(creationTime));
            }
        }
    }

    /**
     * Reloads the directory viewer
     */
    public void refresh()
    {
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    /**
     * Sets the client for the directory viewer
     * @param client
     */
    public void setClient(Client client)
    {
        this.client = client;
    }

    private void subFolders(Element docEle, DefaultMutableTreeNode parent,
            LiveFolder parentFolder)
    {
        List<Element> subFolder = getChildrenByTagName(docEle, "Sub");
        if (subFolder != null && subFolder.size() > 0)
        {
            for (int i = 0; i < subFolder.size(); i++)
            {
                Element el = subFolder.get(i);

                List<Element> folder = getChildrenByTagName(el, "Folder");
                String folderName = folder.get(0).getChildNodes().item(0)
                        .getNodeValue().trim();
                // NodeList
                DefaultMutableTreeNode newFolder = new DefaultMutableTreeNode(
                        folderName);
                parent.add(newFolder);
                LiveFolder liveFolder = parentFolder.makeFolder(folderName);
                this.subFolders(el, newFolder, liveFolder);
                this.parseDocs(el, newFolder, liveFolder);

            }
        }
    }
}
