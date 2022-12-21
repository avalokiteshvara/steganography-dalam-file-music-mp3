package steganography;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

public class Message {

    String content; // content of message

    // the constructor
    public Message(String leMessage) {
        content = leMessage;
    }

    // Methode de conversion du content en tableau de bytes (octets)
    public byte[] toBytes() {
        byte[] byteTable;
        try {
            byteTable = content.getBytes("UTF8"); // La methode magique qui fait tout
        } catch (UnsupportedEncodingException e) {
            byteTable = content.getBytes(); // La methode magique qui fait tout
        }
        return byteTable;
    }

    // Methode qui transforme le content du message en tableau de bits
    /**
     *
     * @return
     */
    public BitSet parser() {
        byte[] MessageBytes = this.toBytes();
        BitSet MessageBits = new BitSet(MessageBytes.length * 8);

        for (int i = 0; i < MessageBytes.length * 8; i++) {
            if ((MessageBytes[MessageBytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                MessageBits.set(i);
            }
        }
        return MessageBits;
    }
}
