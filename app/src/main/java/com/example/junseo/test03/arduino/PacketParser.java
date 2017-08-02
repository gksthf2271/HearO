package com.example.junseo.test03.arduino;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class PacketParser {
    /**
     * PacketParser strings from Arduino
     */
    public enum Type {
        // Human activity detected
        ActivityDetected(0),
        // Whether light is on.
        StatusLight(1),
        // Status of power
        StatusPower(2),

        /*******************************/
        // Simple string message.
        Message(3),
        // Command ' light on'
        Lighton(4),
        // Command 'light off'
        Lightoff(5),

        // Pccket type error
        Error(6);

        /**
         * PacketParser type as string.
         */
        public static final String[] strings = new String[]{
                "activity detected",
                "status light",
                "status power",
                "msg",
                "전등 켜",
                "전등 꺼",
                "error",
        };

        private final int value;

        Type(int value) {
            this.value = value;
        }

        /**
         * Create Enum value from String that is an entry of 'strings'
         * @param string that it creates the type from.
         * @return Type value
         */
        public static Type fromString(String string) {
            int type_idx = Arrays.asList(Type.strings).indexOf(string);
            if (type_idx == -1) {
                return Error;
            }
            else {
                return Type.values()[type_idx];
            }
        }

        /**
         * Get enum value as int.
         */
        public int asInt() {
            return value;
        }
    }

    private static Packet<String> error() {
        return new Packet<>(Type.Error, "parser error");
    }

    private static final String kPacketDelimiter = "#";
    private static final String kParamDelimiter = ":";

    private String buffer_ = "";

    // Push a packet fragment into the packet buffer,
    public boolean pushPacketFragment(byte[] fragment, int len) {
        try {
            buffer_ += new String(fragment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // If there is kPacketDelimiter in the buffer, it means there's at least one packet available.
    public boolean isPacketAvailable() {
        return buffer_.lastIndexOf(kPacketDelimiter) != -1;
    }

    // Pop a packet from packet buffer.
    public String popPacket() {
        int idx = buffer_.indexOf(kPacketDelimiter);
        if (idx == -1) {
            return "";
        }

        String parsed = buffer_.substring(0, idx);
        if (buffer_.length() > idx +1) {
            buffer_ = buffer_.substring(idx + 1);
        } else {
            buffer_ = "";
        }
        return parsed;
    }

    /**
     * Encode Packet
     */
    public static class Encoder {
        private Encoder() {}

        // Encode packet into string.
        public static String encodeAsString(Packet<String> packet) {
            String encoded = Type.strings[packet.type.asInt()];
            if (packet.data != null) {
                encoded += kParamDelimiter + packet.data;
            }
            encoded += kPacketDelimiter;
            return encoded;
        }
    }

    /**
     * Decode packet.
     */
    public static class Decoder {
        private Decoder() {}

        // Create Packet from encoded string.
        public static Packet<String> decodeString(String packet) {
            String type;
            String param;
            int split = packet.indexOf(kParamDelimiter);
            if (split != -1) {
                type = packet.substring(0, split);
                param = packet.substring(split + 1);
            } else {
                type = packet;
                param = "";
            }

           Type t = Type.fromString(type);
            if (t == Type.Error) {
                return error();
            }
            return new Packet<>(t, param);
        }
    }
}
