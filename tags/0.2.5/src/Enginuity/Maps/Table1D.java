package Enginuity.Maps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Serializable;
import javax.swing.JLabel;

public class Table1D extends Table implements Serializable {
    
    private boolean isAxis = false;    
    
    public Table1D() {
        super();
    }
    
    public void populateTable(byte[] input) throws ArrayIndexOutOfBoundsException {
        centerLayout.setRows(1);
        centerLayout.setColumns(this.getDataSize());
        try {
            super.populateTable(input);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }            
        
        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }
        this.add(new JLabel(name + " (" + scale.getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        this.colorize();
    }
    
    public String toString() {
        return super.toString() + " (1D)";
    }

    public boolean isIsAxis() {
        return isAxis;
    }

    public void setIsAxis(boolean isAxis) {
        this.isAxis = isAxis;
    }
    
    public void clearSelection() {
        super.clearSelection();
        if (isAxis) axisParent.clearSelection();                
    }
    
    public void clearSelection(boolean calledByParent) {
        if (calledByParent) {
            super.clearSelection();           
        } else {
            this.clearSelection();
        }        
    }    
    
    public void colorize() {
        if (!isAxis) {
            super.colorize();
        } else {
            for (int i = 0; i < this.getDataSize(); i++) {
                data[i].setColor(Color.WHITE);
            }
        }
    }      
}