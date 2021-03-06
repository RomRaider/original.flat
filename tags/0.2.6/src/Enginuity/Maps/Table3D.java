package Enginuity.Maps;

import Enginuity.XML.RomAttributeParser;
import Enginuity.SwingComponents.TableFrame;
import Enginuity.SwingComponents.VTextIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Table3D extends Table implements Serializable {
    
    private Table1D      xAxis = new Table1D();
    private Table1D      yAxis = new Table1D();
    private DataCell[][] data = new DataCell[1][1];
    private boolean      swapXY = false;
    private boolean      flipX = false;
    private boolean      flipY = false;
    
    public Table3D() {
        super();
        verticalOverhead += 39;
        horizontalOverhead += 5;
    }

    public Table1D getXAxis() {
        return xAxis;
    }

    public void setXAxis(Table1D xAxis) {
        this.xAxis = xAxis;
    }

    public Table1D getYAxis() {
        return yAxis;
    }

    public void setYAxis(Table1D yAxis) {
        this.yAxis = yAxis;
    }

    public boolean isSwapXY() {
        return swapXY;
    }

    public void setSwapXY(boolean swapXY) {
        this.swapXY = swapXY;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
    
    public void setSizeX(int size) {
        data = new DataCell[size][data[0].length];
        centerLayout.setColumns(size + 1);
    }
            
    public int getSizeX() {
        return data.length;
    }    
    
    public void setSizeY(int size) {
        data = new DataCell[data.length][size];
        centerLayout.setRows(size + 1);
    }
            
    public int getSizeY() {
        return data[0].length;
    }   
    
    public void populateTable(byte[] input) throws NullPointerException, ArrayIndexOutOfBoundsException {
        // fill first empty cell        
        centerPanel.add(new JLabel());
        if (!beforeRam) ramOffset = container.getRomID().getRamOffset();
        
        // populate axiis
        try {
            xAxis.setContainer(container);
            xAxis.populateTable(input);
            yAxis.setContainer(container);
            yAxis.populateTable(input);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }
            
        for (int i = 0; i < xAxis.getDataSize(); i++) {
            centerPanel.add(xAxis.getDataCell(i));
        }  
        
        int offset = 0; 
        
        for (int x = 0; x < yAxis.getDataSize(); x++) {
            centerPanel.add(yAxis.getDataCell(x));
            for (int y = 0; y < xAxis.getDataSize(); y++) {
                data[y][x] = new DataCell(scale);
                data[y][x].setTable(this);
                try {
                    data[y][x].setBinValue(
                            RomAttributeParser.parseByteValue(input,
                                                              endian, 
                                                              storageAddress + offset * storageType - ramOffset,
                                                              storageType)); 
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                
                centerPanel.add(data[y][x]);
                data[y][x].setXCoord(y);
                data[y][x].setYCoord(x);                
                data[y][x].setOriginalValue(data[y][x].getBinValue());
                offset++;
            }
        }
        this.colorize(); 
        
        GridLayout topLayout = new GridLayout(2,1);
        JPanel topPanel = new JPanel(topLayout);
        this.add(topPanel, BorderLayout.NORTH);
        topPanel.add(new JLabel(name + " (" + scale.getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);            
        topPanel.add(new JLabel(xAxis.getName() + " (" + xAxis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        JLabel yLabel = new JLabel();
        yLabel.setFont(new Font("Arial", Font.BOLD, 12));
        VTextIcon icon = new VTextIcon(yLabel, yAxis.getName() + " (" + yAxis.getScale().getUnit()+ ")", VTextIcon.ROTATE_LEFT);
        yLabel.setIcon(icon);
        this.add(yLabel, BorderLayout.WEST);
    }
    
    public void colorize() {
        if (!isStatic) {
            int high = 0;
            int low  = 999999999;
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[0].length; y++) {
                    if (data[x][y].getBinValue() > high) {
                        high = data[x][y].getBinValue();
                    } 
                    if (data[x][y].getBinValue() < low) {
                        low = data[x][y].getBinValue();
                    }
                }
            }
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[0].length; y++) {
                    double scale = (double)(data[x][y].getBinValue() - low) / (high - low);
                    int g = (int)(255 - (255 - 140) * scale);
                    if (g > 255) g = 255;
                    data[x][y].setColor(new Color(255, g, 125));
                }
            }
        }
    }
    
    public void setFrame(TableFrame frame) {
        this.frame = frame;
        xAxis.setFrame(frame);
        yAxis.setFrame(frame);    
        frame.setSize(getFrameSize());
    }
    
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight * data[0].length;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) height = minHeight;
        if (width < minWidth) width = minWidth;    
        return new Dimension(width, height);
    }
    
    public String toString() {
        return super.toString() + " (3D)";/* + 
                "\n   Flip X: " + flipX +
                "\n   Size X: " + data.length +
                "\n   Flip Y: " + flipY +
                "\n   Size Y: " + data[0].length +
                "\n   Swap X/Y: " + swapXY +
                xAxis + 
                yAxis;*/
    }    
    
    public void increment(int increment) {
        if (!isStatic) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) data[x][y].increment(increment);
                }
            }
        }
        xAxis.increment(increment);
        yAxis.increment(increment);
    }
    
    public void clearSelection() {
        xAxis.clearSelection(true);
        yAxis.clearSelection(true);
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setSelected(false);
            }
        }
    }    
    
    public void highlight(int xCoord, int yCoord) {
        if (highlight) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (((y >= highlightY && y <= yCoord) ||
                        (y <= highlightY && y >= yCoord)) &&
                        ((x >= highlightX && x <= xCoord) ||
                        (x <= highlightX && x >= xCoord))  ) {
                            data[x][y].setHighlighted(true);
                    } else {
                        data[x][y].setHighlighted(false);
                    }
                }
            }
        }
    }
    
    public void stopHighlight() {
        highlight = false;
        // loop through, selected and un-highlight
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isHighlighted()) {
                    data[x][y].setSelected(true);
                    data[x][y].setHighlighted(false);
                }
            }
        }
    } 
    
    public void setRevertPoint() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setOriginalValue(data[x][y].getBinValue());
            }
        }
        yAxis.setRevertPoint();
        xAxis.setRevertPoint();
        colorize();
    }
    
    public void undoAll() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setBinValue(data[x][y].getOriginalValue());
            }
        }
        yAxis.undoAll();
        xAxis.undoAll();
        colorize();
    }
    
    public void undoSelected() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isSelected()) data[x][y].setBinValue(data[x][y].getOriginalValue());
            }
        }
        yAxis.undoSelected();
        xAxis.undoSelected();
        colorize();
    }      
    
    
    public byte[] saveFile(byte[] binData) {
        binData = xAxis.saveFile(binData);
        binData = yAxis.saveFile(binData);
        int offset = 0; 
        
        for (int x = 0; x < yAxis.getDataSize(); x++) {
            for (int y = 0; y < xAxis.getDataSize(); y++) {
                byte[] output = RomAttributeParser.parseIntegerValue(data[y][x].getBinValue(), endian, storageType);
                for (int z = 0; z < storageType; z++) {
                    binData[offset * storageType + storageAddress - ramOffset] = output[z];
                    offset++;
                }
            }
        }
        return binData;
    }
       
    public void setRealValue(String realValue) {
        if (!isStatic) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) data[x][y].setRealValue(realValue);
                }
            }
        }
        xAxis.setRealValue(realValue);
        yAxis.setRealValue(realValue);
    }
    
    public void addKeyListener(KeyListener listener) {
        xAxis.addKeyListener(listener);
        yAxis.addKeyListener(listener);
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].addKeyListener(listener);
            }
        }
    }    
    
    public void selectCellAt(int y, Table1D axisType) { 
        if (axisType.getType() == TABLE_Y_AXIS) {
            selectCellAt(0, y);
        } else { // y axis
            selectCellAt(y, 0);
        }
    }
    
    public void selectCellAt(int x, int y) { 
        clearSelection();
        data[x][y].setSelected(true);
        highlightX = x;
        highlightY = y;
    }
    
    public void cursorUp() { 
        if (highlightY > 0 && data[highlightX][highlightY].isSelected()) selectCellAt(highlightX, highlightY - 1);  
        else if (!xAxis.isStatic() && data[highlightX][highlightY].isSelected()) xAxis.selectCellAt(highlightX);
        else {
            xAxis.cursorUp();
            yAxis.cursorUp();
        }
    }    
    
    public void cursorDown() {
        if (highlightY < getSizeY() - 1 && data[highlightX][highlightY].isSelected()) selectCellAt(highlightX, highlightY + 1); 
        else {
            xAxis.cursorDown();
            yAxis.cursorDown();
        }
    }    
    
    public void cursorLeft() {     
        if (highlightX > 0 && data[highlightX][highlightY].isSelected()) selectCellAt(highlightX - 1, highlightY); 
        else if (!yAxis.isStatic() && data[highlightX][highlightY].isSelected()) yAxis.selectCellAt(highlightY);
        else {
            xAxis.cursorLeft();
            yAxis.cursorLeft();
        }
    }    
    
    public void cursorRight() { 
        if (highlightX < getSizeX() - 1 && data[highlightX][highlightY].isSelected()) selectCellAt(highlightX + 1, highlightY);   
        else {
            xAxis.cursorRight();
            yAxis.cursorRight();
        }    
    }     
    
    public void startHighlight(int x, int y) {
        xAxis.clearSelection();
        yAxis.clearSelection();        
        super.startHighlight(x, y);
    }
    
    public void copySelection() {
        // find bounds of selection
        // coords[0] = x min, y min, x max, y max
        boolean copy = false;
        int[] coords = new int[4];
        coords[0] = this.getSizeX();
        coords[1] = this.getSizeY();
        
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isSelected()) {
                    if (x < coords[0]) {
                        coords[0] = x;
                        copy = true;
                    }
                    if (x > coords[2]) {
                        coords[2] = x;                        
                        copy = true;
                    }
                    if (y < coords[1]) {
                        coords[1] = y;
                        copy = true;
                    }
                    if (y > coords[3]) {
                        coords[3] = y;
                        copy = true;
                    }
                }
            }
        }     
        // make string of selection
        if (copy) {
            String newline = System.getProperty("line.separator");
            StringBuffer output = new StringBuffer("[Selection3D]" + newline);
            for (int y = coords[1]; y <= coords[3]; y++) {
                for (int x = coords[0]; x <= coords[2]; x++) {
                    if (data[x][y].isSelected()) output.append(data[x][y].getText());
                    else output.append("x"); // x represents non-selected cell
                    if (x < coords[2]) output.append("\t");
                }
                if (y < coords[3]) output.append(newline);                
                //copy to clipboard
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output+""), null);
            }          
        } else {
            xAxis.copySelection();
            yAxis.copySelection();
        }
    }
    
    public void copyTable() {
        // create string
        String newline = System.getProperty("line.separator");
        StringBuffer output = new StringBuffer("[Table3D]" + newline);
        output.append(xAxis.getTableAsString() + newline);
        
        for (int y = 0; y < getSizeY(); y++) {
            output.append(yAxis.getCellAsString(y) + "\t");
            for (int x = 0; x < getSizeX(); x++) {
               output.append(data[x][y].getText());
               if (x < getSizeX() - 1) output.append("\t");
            }
            if (y < getSizeY() - 1) output.append(newline);                
        }        
        //copy to clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output+""), null);  
    }
    
    public void paste() {
        StringTokenizer st = new StringTokenizer("");
        String input = "";
        try {
            input = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */ 
        } catch (IOException ex) { }
        
        String pasteType = st.nextToken();
        
        if (pasteType.equalsIgnoreCase("[Table3D]")) { // Paste table             
            String newline = System.getProperty("line.separator");
            String xAxisValues = "[Table1D]" + newline + st.nextToken(newline);
            
            // build y axis and data values
            StringBuffer yAxisValues = new StringBuffer("[Table1D]" + newline + st.nextToken("\t"));
            StringBuffer dataValues = new StringBuffer("[Table3D]" + newline + st.nextToken("\t") + st.nextToken(newline));
            while (st.hasMoreTokens()) {
                yAxisValues.append("\t" + st.nextToken("\t"));
                dataValues.append(newline + st.nextToken("\t") + st.nextToken(newline));
            }
            
            // put x axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(xAxisValues+""), null);             
            xAxis.paste();  
            // put y axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(yAxisValues+""), null);             
            yAxis.paste();  
            // put datavalues in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(dataValues+""), null);   
            pasteValues();
            // reset clipboard            
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input+""), null);  
            
        } else if (pasteType.equalsIgnoreCase("[Selection3D]")) { // paste selection            
            pasteValues();
        } else if (pasteType.equalsIgnoreCase("[Selection1D]")) { // paste selection            
            xAxis.paste();
            yAxis.paste();
        }   
    }
    
    public void pasteValues() {        
        StringTokenizer st = new StringTokenizer("");
        try {
            String input = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */ 
        } catch (IOException ex) { }
         
        String pasteType = st.nextToken();
        
        // figure paste start cell
        int startX = 0;
        int startY = 0;
        // if pasting a table, startX and Y at 0, else highlight is start
        if (pasteType.equalsIgnoreCase("[Selection3D]")) {
            startX = highlightX;
            startY = highlightY;
        }
        
        // set values 
        String newline = System.getProperty("line.separator");
        for (int y = startY; y < getSizeY(); y++) {
            if (st.hasMoreTokens()) {
                StringTokenizer currentLine = new StringTokenizer(st.nextToken(newline));
                for (int x = startX; x < getSizeX(); x++) {
                    if (currentLine.hasMoreTokens()) {
                        String currentToken = currentLine.nextToken();
                        boolean tableLarger = false;
                        try {
                            if (!data[x][y].getText().equalsIgnoreCase(currentToken)) {
                                data[x][y].setRealValue(currentToken);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) { /* copied table is larger than current table*/ }
                    }
                }             
            }
        }
        
    }
}