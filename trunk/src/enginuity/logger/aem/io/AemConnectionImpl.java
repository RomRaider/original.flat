package enginuity.logger.aem.io;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.SerialConnection;
import enginuity.io.connection.SerialConnectionImpl;
import enginuity.logger.ecu.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class AemConnectionImpl implements AemConnection {
    private final long sendTimeout;
    private final SerialConnection serialConnection;

    public AemConnectionImpl(ConnectionProperties connectionProperties, String portName) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        this.sendTimeout = connectionProperties.getSendTimeout();
        serialConnection = new SerialConnectionImpl(connectionProperties, portName);
        System.out.println("AEM connected");
    }

    //TODO: Update this for AEM...same as lc1 atm which won't work
    public byte[] read() {
        try {
            byte[] response = new byte[6];
//            serialConnection.readStaleData();
//            long timeout = sendTimeout;
//            while (serialConnection.available() < response.length) {
//                sleep(1);
//                timeout -= 1;
//                if (timeout <= 0) {
//                    byte[] badBytes = new byte[serialConnection.available()];
//                    serialConnection.read(badBytes);
//                    System.out.println("Bad response (read timeout): " + asHex(badBytes));
//                    break;
//                }
//            }
            serialConnection.read(response);
            return response;
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        serialConnection.close();
        System.out.println("AEM disconnected");
    }
}
