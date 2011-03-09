package cider.client.gui;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cider.common.network.client.Client;
import cider.common.processes.LiveFolder;

public class DirectoryViewComponent extends JPanel
{
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private DefaultMutableTreeNode top;
    private Client client;
    private JTree tree;
    private LiveFolder rootFolder = new LiveFolder("Bot", "root");

    /*
     * public static void main(String[] args) { JFrame w = new JFrame();
     * w.add(new DirectoryViewComponent()); w.setLocationByPlatform(true);
     * w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); w.setPreferredSize(new
     * Dimension(400, 800)); w.setVisible(true); }
     */

    public DirectoryViewComponent()
    {
        top = new DefaultMutableTreeNode("root");
        // createNodes(top);

        tree = new JTree(top);
        // tree.setModel(model);

        JScrollPane scrollpane = new JScrollPane(tree);

        new JLabel("directory tree");

        new JLabel("code");

        new JLabel("chat, oh hai");

        this.setLayout(new BorderLayout());
        this.add(new JLabel(" File Explorer"), BorderLayout.NORTH);
        this.add(scrollpane, BorderLayout.CENTER);
    }

    public void setClient(Client client)
    {
        this.client = client;
    }

    public void createNodes(DefaultMutableTreeNode top)
    {
        /* Creates some objects to simulate files and folders etc */
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        category = new DefaultMutableTreeNode("Folder 1");
        top.add(category);

        // book = new DefaultMutableTreeNode(new BookInfo
        // ("The Java Tutorial: A Short Course on the Basics",
        // "tutorial.html"));
        for (int i = 0; i < 50; i++)
        {
            book = new DefaultMutableTreeNode("File 1");
            category.add(book);
        }
        /*
         * DefaultMutableTreeNode sub = null; sub = new
         * DefaultMutableTreeNode("SubFile 1"); book.add(sub);
         */

        category = new DefaultMutableTreeNode("Folder 2");
        top.add(category);

        book = new DefaultMutableTreeNode("File 2");
        category.add(book);
    }

    public void constructTree(String xml)
    {
        this.top.removeAllChildren();
        this.rootFolder.removeAllChildren();

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

                this.subFolders(docEle, this.top, this.rootFolder);
                this.parseDocs(docEle, this.top, this.rootFolder);
                System.out.println("Reconstructed Tree: ");
                System.out.println(this.rootFolder.xml(""));
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

        this.tree.addTreeSelectionListener(new DirectoryViewSelectionListener(
                this.tree, this.client));
    }

    public LiveFolder getLiveFolder()
    {
        return this.rootFolder;
    }

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

    private void subFolders(Element docEle, DefaultMutableTreeNode parent,
            LiveFolder parentFolder)
    {
        List<Element> subFolder = getChildrenByTagName(docEle, "Sub");
        if (subFolder != null && subFolder.size() > 0)
        {
            for (int i = 0; i < subFolder.size(); i++)
            {
                Element el = (Element) subFolder.get(i);
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

    private void parseDocs(Element docEle, DefaultMutableTreeNode folder,
            LiveFolder parentFolder)
    {
        List<Element> docs = getChildrenByTagName(docEle, "Doc");
        if (docs != null && docs.size() > 0)
        {
            for (int i = 0; i < docs.size(); i++)
            {
                Element el = (Element) docs.get(i);
                String docName = el.getChildNodes().item(0).getNodeValue()
                        .trim();
                folder.add(new DefaultMutableTreeNode(docName));
                parentFolder.makeDocument(docName);
            }
        }
    }

    /*
     * // Flip the keys and values of a hash and return a new one private
     * Hashtable flipHash( Hashtable h ) { Object[] keys = h.keySet().toArray();
     * Hashtable newHash = new Hashtable( keys.length );
     * 
     * for( int i = 0; i < keys.length; i++ ) newHash.put( h.get( keys[i] ),
     * keys[i] );
     * 
     * return newHash; }
     */
}
