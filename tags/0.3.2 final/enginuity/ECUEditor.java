package enginuity;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import enginuity.maps.Rom;
import enginuity.maps.Table;
import enginuity.net.URL;
import enginuity.swing.ECUEditorMenuBar;
import enginuity.swing.ECUEditorToolBar;
import enginuity.swing.JProgressPane;
import enginuity.swing.MDIDesktopPane;
import enginuity.swing.RomTree;
import enginuity.swing.RomTreeNode;
import enginuity.swing.RomTreeRootNode;
import enginuity.swing.TableFrame;
import enginuity.xml.DOMRomUnmarshaller;
import enginuity.xml.DOMSettingsBuilder;
import enginuity.xml.DOMSettingsUnmarshaller;
import enginuity.xml.RomNotFoundException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class ECUEditor extends JFrame implements WindowListener, PropertyChangeListener {
    
    private RomTreeRootNode  imageRoot       = new RomTreeRootNode("Open Images");
    private RomTree          imageList       = new RomTree(imageRoot);
    private Settings         settings        = new Settings();
    private String           version         = "0.3.2 Beta";
    private String           versionDate     = "8/12/2006";
    private String           titleText       = "Enginuity v" + version;
    private MDIDesktopPane   rightPanel      = new MDIDesktopPane();
    private Rom              lastSelectedRom = null;
    private JSplitPane       splitPane       = new JSplitPane();
    private ECUEditorToolBar toolBar;
    private ECUEditorMenuBar menuBar;
    private JProgressPane    statusPanel     = new JProgressPane();
    
    public ECUEditor() {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception ex) { }
        
        // get settings from xml
        try {
            InputSource src = new InputSource(new FileInputStream(new File("./settings.xml")));
            DOMSettingsUnmarshaller domUms = new DOMSettingsUnmarshaller();
            DOMParser parser = new DOMParser();
            parser.parse(src);
            Document doc = parser.getDocument();
            settings = domUms.unmarshallSettings(doc.getDocumentElement());
            
        } catch (RuntimeException re) {
        	// Catching RE specifially will prevent real bugs from being
            // presented as a settings file not found exception
			throw re;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Settings file not found.\n" +
                    "A new file will be created.", "Error Loading Settings", JOptionPane.INFORMATION_MESSAGE);
        }
        finally {
            
            BufferedReader br = null;
            
            if (!settings.getRecentVersion().equalsIgnoreCase(version)) {
                
                try {
                    // new version being used, display release notes
                    JTextArea releaseNotes = new JTextArea();
                    releaseNotes.setEditable(false);
                    releaseNotes.setWrapStyleWord(true);
                    releaseNotes.setLineWrap(true);
                    releaseNotes.setFont(new Font("Tahoma", Font.PLAIN, 12));

                    JScrollPane scroller = new JScrollPane(releaseNotes, 
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    scroller.setPreferredSize(new Dimension(600,500));

                    br = new BufferedReader(new FileReader(settings.getReleaseNotes()));
                    StringBuffer sb = new StringBuffer();
                    while (br.ready()) {
                        sb.append(br.readLine() + "\n");
                    }

                    releaseNotes.setText(sb+"");

                    JOptionPane.showMessageDialog(this, scroller,
                            "Enginuity " + version + " Release Notes", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { }
            
            }            
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					/* Ignore */
				}
			}
		}
        
        setSize(getSettings().getWindowSize());
        setLocation(getSettings().getWindowLocation());
        if (getSettings().isWindowMaximized()) setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JScrollPane rightScrollPane = new JScrollPane(rightPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane leftScrollPane = new JScrollPane(imageList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerSize(2);
        splitPane.setDividerLocation(getSettings().getSplitPaneLocation());
        splitPane.addPropertyChangeListener(this);
        
        getContentPane().add(splitPane);
        rightPanel.setBackground(Color.BLACK);
        imageList.setScrollsOnExpand(true);
        imageList.setContainer(this);
        
        //create menubar and toolbar
        menuBar = new ECUEditorMenuBar(this);
        this.setJMenuBar(menuBar);
        toolBar = new ECUEditorToolBar(this);
        this.add(toolBar, BorderLayout.NORTH);
        this.add(statusPanel, BorderLayout.SOUTH);
        
        //set remaining window properties
        setIconImage(new ImageIcon("./graphics/enginuity-ico.gif").getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(this);
        setTitle(titleText);
        setVisible(true);
        
    }
    
    public void handleExit() { 
       getSettings().setSplitPaneLocation(splitPane.getDividerLocation()); 
       if (getExtendedState() == JFrame.MAXIMIZED_BOTH)           
       getSettings().setWindowMaximized(true); 
       else { 
          getSettings().setWindowMaximized(false); 
          getSettings().setWindowSize(getSize()); 
          getSettings().setWindowLocation(getLocation()); 
       } 
        
       DOMSettingsBuilder builder = new DOMSettingsBuilder(); 
       try { 
          //JProgressPane progress = new JProgressPane(this, "Saving settings...", "Saving settings...");            
          builder.buildSettings(settings, new File("./settings.xml"), statusPanel, version); 
        } catch (IOException ex) { 
        } finally {
            statusPanel.update("Ready...", 0);
            repaint();
        }
    } 
    
    public void windowClosing(WindowEvent e) { 
       handleExit(); 
    } 
   
    public void windowOpened(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    
    public String getVersion() {
        return version;
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public void addRom(Rom input) {
        // add to ecu image list pane
        RomTreeNode romNode = new RomTreeNode(input, settings.getUserLevel(), settings.isDisplayHighTables());
        imageRoot.add(romNode);
        imageList.updateUI();
        
        imageList.expandRow(imageList.getRowCount() - 1);
        imageList.updateUI();
        setLastSelectedRom(input);
        
        if (input.getRomID().isObsolete() && settings.isObsoleteWarning()) {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(3, 1));
            infoPanel.add(new JLabel("A newer version of this ECU revision exists. " +
                    "Please visit the following link to download the latest revision:"));
            infoPanel.add(new URL(getSettings().getRomRevisionURL()));
            
            JCheckBox check = new JCheckBox("Always display this message", true);
            check.setHorizontalAlignment(JCheckBox.RIGHT);
            
            check.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    settings.setObsoleteWarning(((JCheckBox)e.getSource()).isSelected());
                    }
                }
            );
            
            infoPanel.add(check);
            JOptionPane.showMessageDialog(this, infoPanel, "ECU Revision is Obsolete", JOptionPane.INFORMATION_MESSAGE);
        }
        input.setContainer(this);
        imageList.updateUI();
    }
    
    public void displayTable(TableFrame frame) {
        frame.setVisible(true);
        try {
            rightPanel.add(frame);
        } catch (IllegalArgumentException ex) {
            // table is already open, so set focus
            frame.requestFocus();
        }
        frame.setSize(frame.getTable().getFrameSize());
        rightPanel.repaint();
    }
    
    public void removeDisplayTable(TableFrame frame) {
        rightPanel.remove(frame);
        rightPanel.repaint();
    }
    
    public void closeImage() {
        for (int i = 0; i < imageRoot.getChildCount(); i++) { 
                RomTreeNode romTreeNode = (RomTreeNode)imageRoot.getChildAt(i); 
                Rom rom = romTreeNode.getRom(); 
                if (rom == lastSelectedRom) { 
                        Vector<Table> romTables = rom.getTables(); 
                        for (Iterator j = romTables.iterator(); j.hasNext();) { 
                                Table t = (Table)j.next(); 
                                rightPanel.remove(t.getFrame()); 

                        } 

                        Vector<TreePath> path = new Vector<TreePath>();                                        
                        path.add(new TreePath(romTreeNode.getPath()));    
                        imageRoot.remove(i); 
                        imageList.removeDescendantToggledPaths((Enumeration<TreePath>)path.elements());  

                        break; 
                } 
        } 

        imageList.updateUI();
        
        if (imageRoot.getChildCount() > 0) {
            setLastSelectedRom(((RomTreeNode)imageRoot.getChildAt(0)).getRom());
        } else {
            // no other images open
            setLastSelectedRom(null);
        }
        rightPanel.repaint();
    }
    
    public void closeAllImages() {
        while (imageRoot.getChildCount() > 0) {
            closeImage();
        }
    }
    
    public Rom getLastSelectedRom() {
        return lastSelectedRom;
    }
    
    public void setLastSelectedRom(Rom lastSelectedRom) {
        this.lastSelectedRom = lastSelectedRom;
        if (lastSelectedRom == null) {
            setTitle(titleText);
        } else {
            setTitle(titleText + " - " + lastSelectedRom.getFileName());
        }
        
        // update filenames
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            ((RomTreeNode)imageRoot.getChildAt(i)).updateFileName();
        }
        
        toolBar.updateButtons();
        menuBar.updateMenu();
        imageList.updateUI();
    }
    
    public ECUEditorToolBar getToolBar() {
        return toolBar;
    }
    
    public void setToolBar(ECUEditorToolBar toolBar) {
        this.toolBar = toolBar;
    }
    
    public void setSettings(Settings settings) {
        this.settings = settings;
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode rtn = (RomTreeNode)imageRoot.getChildAt(i);
            rtn.getRom().setContainer(this);
        }
    }
    
    public void setUserLevel(int userLevel) {
        settings.setUserLevel(userLevel);
        imageRoot.setUserLevel(userLevel, settings.isDisplayHighTables());
        imageList.updateUI();
    }
    
    public Vector<Rom> getImages() {
        Vector<Rom> images = new Vector<Rom>();
        for (int i = 0; i < imageRoot.getChildCount(); i++) {
            RomTreeNode rtn = (RomTreeNode)imageRoot.getChildAt(i);
            images.add(rtn.getRom());
        }
        return images;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        imageList.updateUI();
        imageList.repaint();
    }
    
    public void openImage(File inputFile) throws XMLParseException, Exception {        
        
        try {     
            update(getGraphics());
            statusPanel.update("Parsing ECU definitions...", 0);
            repaint();
            
            byte[] input = readFile(inputFile);
            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller(settings, this);
            DOMParser parser = new DOMParser();    
            statusPanel.update("Finding ECU definition...", 10);
            repaint();
            Rom rom;
                          
            // parse ecu definition files until result found
            for (int i = 0; i < getSettings().getEcuDefinitionFiles().size(); i++) {
                InputSource src = new InputSource(new FileInputStream(getSettings().getEcuDefinitionFiles().get(i)));

                parser.parse(src);
                Document doc = parser.getDocument();                        
                
                try {
                    rom = domUms.unmarshallXMLDefinition(doc.getDocumentElement(), input, statusPanel);
                    statusPanel.update("Populating tables...", 50);
                    repaint();
                    rom.populateTables(input, statusPanel);
                    rom.setFileName(inputFile.getName());

                    statusPanel.update("Finalizing...", 90); 
                    repaint();
                    addRom(rom);                   
                    rom.setFullFileName(inputFile);   
                    return;
                    
                } catch (RomNotFoundException ex) {
                    // rom was not found in current file, skip to next
                }
            }
            
            // if code executes to this point, no ROM was found, report to user
            JOptionPane.showMessageDialog(this, "ECU Definition Not Found", "Error Loading " + inputFile.getName(), JOptionPane.ERROR_MESSAGE);
            
        } catch (StackOverflowError ex) {
            // handles looped inheritance, which will use up all available memory
            JOptionPane.showMessageDialog(this, "Looped \"base\" attribute in XML definitions.", "Error Loading ROM", JOptionPane.ERROR_MESSAGE);
            
        } finally {
            // remove progress bar
            //progress.dispose();
            statusPanel.update("Ready...", 0);
            
        }
    }    
    
    private byte[] readFile(File inputFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;
        try {
                fis = new FileInputStream(inputFile);
                byte[] buf = new byte[8192];
                int bytesRead = 0;
                while ((bytesRead = fis.read(buf)) != -1) {
                        baos.write(buf, 0, bytesRead);
                }
        } finally {
                if (fis != null) {
                        fis.close();
                }
        }
        return baos.toByteArray();
    }    
    
    public static void main(String args[]) {
        // try create socket listener for shell opening new files
        ServerSocket   sock         = null; // original server socket
        String         serverName   = "localhost";
        Socket         clientSocket = null; // socket created by accept
        PrintWriter    pw           = null; // socket output stream
        BufferedReader br           = null; // socket input stream 
        int            serverPort   = 8753;

        try {
            sock = new java.net.ServerSocket(serverPort);               // create socket and bind to port
            sock.close();
            
        } catch (Exception ex) {
            // pass filename if file present            
            if (args.length > 0) {
                
                try {
                    Socket socket = new java.net.Socket(serverName,serverPort);       // create socket and connect
                    pw   = new java.io.PrintWriter(socket.getOutputStream(), true);  // create reader and writer
                    br   = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                    pw.println(args[0]);                      // send msg to the server
                    String answer = br.readLine();                              // get data from the server

                    pw.close();                                                 // close everything
                    br.close();
                    socket.close();

                } catch (Throwable e) { e.printStackTrace(); }            
                // after sending filename, exit
                System.exit(0);
            }            
        }
        
        // launch editor    
        ECUEditor editor = new ECUEditor();
        
        // open files, if passed
        try {
            if (args.length > 0) {
                editor.openImage(new File(args[0]));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        
        // listen for files        
        try {
            
            while (true) {
                sock = new java.net.ServerSocket(serverPort); // create socket and bind to port
                clientSocket = sock.accept(); // wait for client to connect
                pw   = new java.io.PrintWriter(clientSocket.getOutputStream(),true);
                br   = new java.io.BufferedReader(
                    new java.io.InputStreamReader(clientSocket.getInputStream()));
                String msg = br.readLine(); // read msg from client

                // open file from client
                editor.openImage(new File(msg));
                
                pw.close();  // close everything
                br.close();
                clientSocket.close();
                sock.close();
            }

        } catch (Throwable e) { e.printStackTrace(); }           
    }    
}