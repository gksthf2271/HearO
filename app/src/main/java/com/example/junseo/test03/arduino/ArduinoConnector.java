package com.example.junseo.test03.arduino;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.junseo.test03.arduino.PacketParser.Decoder;
import com.example.junseo.test03.arduino.PacketParser.Encoder;
/**
 * Communicate with Arduino Device using bluetooth.
 *
 * Responsible for managing packets and implementing the protocol.
 */
public class ArduinoConnector {
    private final String LOG = ArduinoConnector.class.getName();
    private BluetoothSerial bluetooth_;
    private Listener listener_;

    /**
     * Notifies the arduino connection status
     */
    public interface Listener {
        void onConnect(BluetoothDevice device);
        void onReaction(PacketParser.Type type, String data);
        void onDisconnect(BluetoothDevice device);
    }

    public ArduinoConnector(Listener listener) {
        listener_ = listener;
    }

    public void connect(BluetoothDevice device) {
        if (bluetooth_ != null) {
            disconnect();
        }
        bluetooth_ = new BluetoothSerial();
        bluetooth_.askConnect(device, bluetooth_listener_);
    }

    public void disconnect() {
        bluetooth_.cancel();
        bluetooth_ = null;
    }

    public void send(String command) {
        Packet<String> initial_packet = new Packet<>(PacketParser.Type.fromString(command));
        String packet = Encoder.encodeAsString(initial_packet);
        Log.d(LOG, "Packet Send -> " + packet);
        bluetooth_.Write(packet.getBytes());
    }

    public void destroy() {
        if (bluetooth_ != null) {
            bluetooth_.cancel();
        }
    }

    protected  BluetoothSerial.Listener bluetooth_listener_ = new BluetoothSerial.Listener() {
        private PacketParser parser_ = new PacketParser();

        @Override
        public void onConnect(BluetoothDevice device) {
            Packet<String> initial_packet = new Packet<>(PacketParser.Type.Message, "hi");
            String packet = Encoder.encodeAsString(initial_packet);
            bluetooth_.Write(packet.getBytes());
            listener_.onConnect(device);
        }

        @Override
        public void onRead(BluetoothDevice device, byte[] data, int len) {
            // Store the fragment of packet until it gets a complete packet
            // then, the return value will be true.
            if (!parser_.pushPacketFragment(data, len)) {
                Log.w(LOG, "packet parse error");
            }

            while (parser_.isPacketAvailable()) {
                String raw_packet = parser_.popPacket();
                Packet<String> packet = Decoder.decodeString(raw_packet);
                if (packet.type == PacketParser.Type.Error) {
                    Log.w(LOG, packet.toString());
                    return;
                }

                listener_.onReaction(packet.type, packet.data);
            }
        }

        @Override
        public void onDisconnect(BluetoothDevice device) {
            listener_.onDisconnect(device);
        }
    };
}
