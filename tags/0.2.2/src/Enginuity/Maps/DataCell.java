package Enginuity.Maps;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import org.nfunk.jep.JEP;

public class DataCell extends JLabel implements MouseListener {
    
    private int     binValue       = 0;
    private int     originalValue  = 0;
    private Scale   scale          = new Scale();
    private String  realValue      = "";
    private Color   scaledColor    = new Color(0,0,0);
    private Color   highlightColor = new Color(155,155,255);
    private Boolean selected       = false;
    private Boolean highlighted    = false;
    private Table   table;
    private int     x = 0;
    private int     y = 0;
    
    public DataCell() {
        this(new Scale());
    }    
    
    public DataCell(Scale scale) {
        this.scale = scale;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(new Font("Arial", Font.BOLD, 12));        
        this.setBorder(new LineBorder(Color.BLACK, 1));
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
    }
    
    public void calcRealValue() {
        DecimalFormat formatter = new DecimalFormat(scale.getFormat());
 
        // create parser
        JEP parser = new JEP();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol table
        parser.addVariable("x", binValue);
        parser.parseExpression(scale.getExpression());
        
        realValue = formatter.format(parser.getValue());        
        this.setText(realValue);
    }
    
    public void setColor(Color color) {
        scaledColor = color;
        if (!selected) super.setBackground(color);
    }
    
    public void setRealValue(String realValue) {
        this.realValue = realValue;
        this.setText(realValue);
    }
    
    public void setBinValue(int binValue) {
        this.binValue = binValue;
        // make sure it's in range
        if (binValue < 0) {
            this.setBinValue(0);
        } else if (binValue > Math.pow(256, table.getStorageType()) - 1) {
            this.setBinValue((int)(Math.pow(256, table.getStorageType()) - 1));
        }
        this.calcRealValue();
        if (binValue != getOriginalValue()) {
            this.setBorder(new LineBorder(Color.RED, 3));
        } else {
            this.setBorder(new LineBorder(Color.BLACK, 1));
        }
    }    
    
    public int getBinValue() {
        return binValue;
    }
        
    public String toString() {
        return realValue;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
        if (selected) {
            this.setBackground(highlightColor);
        } else {
            this.setBackground(scaledColor);
        }
    }

    public void setHighlighted(Boolean highlighted) {
        if (!table.isStatic()) {
            this.highlighted = highlighted;
            if (highlighted) {
                this.setBackground(highlightColor);
            } else {
                if (!selected) this.setBackground(scaledColor);
            }
        }
    }
    
    public boolean isHighlighted() {
        return highlighted;
    }
    
    public void mouseEntered(MouseEvent e) {
        table.highlight(x, y);
    }
    public void mousePressed(MouseEvent e) {         
        if (!table.isStatic()) {            
            if (!e.isControlDown()) table.clearSelection();
            table.startHighlight(x, y);
        }
    }
    public void mouseReleased(MouseEvent e) {         
        if (!table.isStatic()) {
            table.stopHighlight();
        }
    }
    public void mouseClicked(MouseEvent e) { 
        //this.setSelected(!selected);
    }
    public void mouseExited(MouseEvent e) { }
    
    public void increment() {
        this.setBinValue(binValue + scale.getIncrement());
    }
    
    public void decrement() {
        this.setBinValue(binValue - scale.getIncrement());
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setXCoord(int x) {
        this.x = x;
    }

    public void setYCoord(int y) {
        this.y = y;
    }

    public int getOriginalValue() {
        return originalValue;
    } 

    public void setOriginalValue(int originalValue) {
        this.originalValue = originalValue;
        if (binValue != getOriginalValue()) {
            this.setBorder(new LineBorder(Color.RED, 3));
        } else {
            this.setBorder(new LineBorder(Color.BLACK, 1));
        }        
    }
    
    public void undo() {
        this.setBinValue(originalValue);
    }
    
    public void setRevertPoint() {
        this.setOriginalValue(binValue);
    }
}