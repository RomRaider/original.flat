package enginuity.xml;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import enginuity.Settings;
import enginuity.swing.JProgressPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.metadata.IIOMetadataNode;

public class DOMSettingsBuilder {
    
    public void buildSettings (Settings settings, File output, JProgressPane progress, String versionNumber) throws IOException {
        
        IIOMetadataNode settingsNode = new IIOMetadataNode("settings");
        
        // create settings
        progress.update("Saving window settings...", 20);
        settingsNode.appendChild(buildWindow(settings));
        progress.update("Saving file settings...", 40);
        settingsNode.appendChild(buildFiles(settings));   
        progress.update("Saving options...", 60);
        settingsNode.appendChild(buildOptions(settings, versionNumber));
        progress.update("Saving display settings...", 80);
        settingsNode.appendChild(buildTableDisplay(settings));
        
        OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
        of.setIndent(1);
        of.setIndenting(true);
        
        progress.update("Writing to file...", 90);
        
        FileOutputStream fos = new FileOutputStream(output);        
        XMLSerializer serializer = new XMLSerializer(fos,of);
        serializer.serialize(settingsNode);
        fos.flush();
        fos.close();  
    } 
    
    public IIOMetadataNode buildWindow(Settings settings) {        
        IIOMetadataNode windowSettings = new IIOMetadataNode("window");
        
        // window size
        IIOMetadataNode size = new IIOMetadataNode("size");
        size.setAttribute("x", ((int)settings.getWindowSize().getHeight())+"");
        size.setAttribute("y", ((int)settings.getWindowSize().getWidth())+"");
        windowSettings.appendChild(size);
        
        // window location
        IIOMetadataNode location = new IIOMetadataNode("location");
        location.setAttribute("x", ((int)settings.getWindowLocation().getX())+"");
        location.setAttribute("y", ((int)settings.getWindowLocation().getY())+"");
        windowSettings.appendChild(location);
        
        // splitpane location
        IIOMetadataNode splitpane = new IIOMetadataNode("splitpane");
        splitpane.setAttribute("location", settings.getSplitPaneLocation()+"");
        windowSettings.appendChild(splitpane);       
        
        return windowSettings;
    }
    
    public IIOMetadataNode buildFiles(Settings settings) {
        IIOMetadataNode files = new IIOMetadataNode("files");
        
        // image directory
        IIOMetadataNode imageDir = new IIOMetadataNode("image_dir");
        imageDir.setAttribute("path", settings.getLastImageDir().getAbsolutePath());
        files.appendChild(imageDir);
        
        // ecu definition files
        Vector<File> defFiles = settings.getEcuDefinitionFiles();
        
        for (int i = 0; i < defFiles.size(); i++) {
            IIOMetadataNode ecuDef = new IIOMetadataNode("ecudefinitionfile");
            ecuDef.setAttribute("name", defFiles.get(i).getAbsolutePath());
            files.appendChild(ecuDef);
        }
        
        return files;
    }
    
    public IIOMetadataNode buildOptions(Settings settings, String versionNumber) {
        IIOMetadataNode options = new IIOMetadataNode("options");
        
        // obsolete warning
        IIOMetadataNode obsoleteWarning = new IIOMetadataNode("obsoletewarning");
        obsoleteWarning.setAttribute("value", settings.isObsoleteWarning()+"");
        options.appendChild(obsoleteWarning);
        
        // calcultion conflicting warning
        IIOMetadataNode calcConflictWarning = new IIOMetadataNode("calcconflictwarning");
        calcConflictWarning.setAttribute("value", settings.isCalcConflictWarning()+"");
        options.appendChild(calcConflictWarning);
        
        // debug mode
        IIOMetadataNode debug = new IIOMetadataNode("debug");
        debug.setAttribute("value", settings.isDebug()+"");
        options.appendChild(debug);
        
        // userlevel
        IIOMetadataNode userLevel = new IIOMetadataNode("userlevel");
        userLevel.setAttribute("value", settings.getUserLevel()+"");
        options.appendChild(userLevel);
        
        // table click count
        IIOMetadataNode tableClickCount = new IIOMetadataNode("tableclickcount");
        tableClickCount.setAttribute("value", settings.getTableClickCount()+"");
        options.appendChild(tableClickCount);
        
        // last version used
        IIOMetadataNode version = new IIOMetadataNode("version");
        version.setAttribute("value", versionNumber);
        options.appendChild(version);
        
        // save debug level tables
        IIOMetadataNode saveDebugTables = new IIOMetadataNode("savedebugtables");
        saveDebugTables.setAttribute("value", settings.isSaveDebugTables()+"");
        options.appendChild(saveDebugTables);

        // display tables higher than userlevel
        IIOMetadataNode displayHighTables = new IIOMetadataNode("displayhightables");
        displayHighTables.setAttribute("value", settings.isDisplayHighTables()+"");
        options.appendChild(displayHighTables);   

        // warning when exceeding limits
        IIOMetadataNode valueLimitWarning = new IIOMetadataNode("valuelimitwarning");
        valueLimitWarning.setAttribute("value", settings.isValueLimitWarning()+"");
        options.appendChild(valueLimitWarning);        
        
        return options;
    }
    
    public IIOMetadataNode buildTableDisplay(Settings settings) {
        IIOMetadataNode tableDisplay = new IIOMetadataNode("tabledisplay");
        
        // font
        IIOMetadataNode font = new IIOMetadataNode("font");
        font.setAttribute("face", settings.getTableFont().getName());
        font.setAttribute("size", settings.getTableFont().getSize()+"");
        font.setAttribute("decoration", settings.getTableFont().getStyle()+"");
        tableDisplay.appendChild(font);
        
        // table cell size
        IIOMetadataNode cellSize = new IIOMetadataNode("cellsize");
        cellSize.setAttribute("height", ((int)settings.getCellSize().getHeight())+"");
        cellSize.setAttribute("width", ((int)settings.getCellSize().getWidth())+"");
        tableDisplay.appendChild(cellSize);
        
        // colors
        IIOMetadataNode colors = new IIOMetadataNode("colors");        
        // max
        IIOMetadataNode max = new IIOMetadataNode("max");
        max.setAttribute("r", settings.getMaxColor().getRed()+"");
        max.setAttribute("g", settings.getMaxColor().getGreen()+"");
        max.setAttribute("b", settings.getMaxColor().getBlue()+"");
        colors.appendChild(max);        
        // min
        IIOMetadataNode min = new IIOMetadataNode("min");
        min.setAttribute("r", settings.getMinColor().getRed()+"");
        min.setAttribute("g", settings.getMinColor().getGreen()+"");
        min.setAttribute("b", settings.getMinColor().getBlue()+"");
        colors.appendChild(min);        
        // highlight
        IIOMetadataNode highlight = new IIOMetadataNode("highlight");
        highlight.setAttribute("r", settings.getHighlightColor().getRed()+"");
        highlight.setAttribute("g", settings.getHighlightColor().getGreen()+"");
        highlight.setAttribute("b", settings.getHighlightColor().getBlue()+"");
        colors.appendChild(highlight);
        // increased cell border
        IIOMetadataNode increaseBorder = new IIOMetadataNode("increaseborder");
        increaseBorder.setAttribute("r", settings.getIncreaseBorder().getRed()+"");
        increaseBorder.setAttribute("g", settings.getIncreaseBorder().getGreen()+"");
        increaseBorder.setAttribute("b", settings.getIncreaseBorder().getBlue()+"");
        colors.appendChild(increaseBorder);
        // decreased cell border
        IIOMetadataNode decreaseBorder = new IIOMetadataNode("decreaseborder");
        decreaseBorder.setAttribute("r", settings.getDecreaseBorder().getRed()+"");
        decreaseBorder.setAttribute("g", settings.getDecreaseBorder().getGreen()+"");
        decreaseBorder.setAttribute("b", settings.getDecreaseBorder().getBlue()+"");
        colors.appendChild(decreaseBorder);
        // axis cells
        IIOMetadataNode axis = new IIOMetadataNode("axis");
        axis.setAttribute("r", settings.getAxisColor().getRed()+"");
        axis.setAttribute("g", settings.getAxisColor().getGreen()+"");
        axis.setAttribute("b", settings.getAxisColor().getBlue()+"");
        colors.appendChild(axis);
        // warning cells
        IIOMetadataNode warning = new IIOMetadataNode("warning");
        warning.setAttribute("r", settings.getWarningColor().getRed()+"");
        warning.setAttribute("g", settings.getWarningColor().getGreen()+"");
        warning.setAttribute("b", settings.getWarningColor().getBlue()+"");
        colors.appendChild(warning);        
        
        tableDisplay.appendChild(colors);
        
        return tableDisplay;
    }
}