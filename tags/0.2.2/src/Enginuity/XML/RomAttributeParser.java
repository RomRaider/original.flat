// Parses attributes from ROM XML

package Enginuity.XML;

import Enginuity.Maps.Table;
import Enginuity.Maps.Scale;

public abstract class RomAttributeParser {
    
    public RomAttributeParser() {
    }
    
    public static int parseEndian(String input) {
        if (input.equalsIgnoreCase("big") || input.equalsIgnoreCase(Table.ENDIAN_BIG+"")) {
            return Table.ENDIAN_BIG;
        } else if (input.equalsIgnoreCase("little") || input.equalsIgnoreCase(Table.ENDIAN_LITTLE+"")) {
            return Table.ENDIAN_LITTLE;
        } else {
            return Table.ENDIAN_LITTLE;
        }
    }   
    
    public static int parseHexString(String input) {
        if (input.length() > 2 && input.substring(0,2).equalsIgnoreCase("0x")) {
            return Integer.parseInt(input.substring(2), 16);
        } else {
            return Integer.parseInt(input, 16);
        }
    }
    
    public static int parseStorageType(String input) {
        try {
            return Integer.parseInt(input.substring(4)) / 8;
        } catch (Exception ex) {
            return Integer.parseInt(input);
        }        
    }
    
    public static int parseScaleType(String input) {
        if (input.equalsIgnoreCase("inverse")) {
            return Scale.INVERSE;
        } else {
            return Scale.LINEAR;
        }
    }
    
    public static int parseTableType(String input) {
        if (input.equalsIgnoreCase("3D") || input.equalsIgnoreCase(Table.TABLE_3D+"")) {
            return Table.TABLE_3D;
        } else if (input.equalsIgnoreCase("2D") || input.equalsIgnoreCase(Table.TABLE_2D+"")) {
            return Table.TABLE_2D;
        } else if (input.equalsIgnoreCase("X Axis") || input.equalsIgnoreCase("Static X Axis") || input.equalsIgnoreCase(Table.TABLE_X_AXIS+"")) {
            return Table.TABLE_X_AXIS;
        } else if (input.equalsIgnoreCase("Y Axis") || input.equalsIgnoreCase("Static Y Axis") || input.equalsIgnoreCase(Table.TABLE_Y_AXIS+"")) {
            return Table.TABLE_Y_AXIS;
        } else {
            return Table.TABLE_1D;
        }
    }
    
    public static int parseByteValue(byte[] input, int endian, int address, int length) throws ArrayIndexOutOfBoundsException {
        try {
            int output = 0;
            if (endian == Table.ENDIAN_BIG) {
                for (int i = 0; i < length; i++) {
                    output += (input[address + i] & 0xff) << 8 * (length - i - 1);
                }
            } else { // little endian
                for (int i = 0; i < length; i++) {
                    output += (input[address + length - i - 1] & 0xff) << 8 * (length - i - 1);
                }
            }
            return output;
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    public static byte[] parseIntegerValue(int input, int endian, int length) {
        byte[] byteArray = new byte[4];
        
        byteArray[0] = (byte)((input >> 24) & 0x000000FF);
        byteArray[1] = (byte)((input >> 16) & 0x0000FF);
        byteArray[2] = (byte)((input >> 8)  & 0x00FF);
        byteArray[3] = (byte)(input &  0xFF  );
        
        byte[] output = new byte[length];
        
        for (int i = 0; i < length; i++) {
            if (endian == Table.ENDIAN_BIG) {
                //output[i] = byteArray[i + length];
                output[i] = byteArray[4 - length + i]; 
            } else { // little endian
                output[length - 1 - i] = byteArray[4 - length + i];
            }
        }
        return output;
    }
}