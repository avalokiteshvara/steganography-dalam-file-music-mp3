package steganography;

import java.io.*;
import java.util.BitSet;

public final class MP3 {

    // Attributs
    //Nom du MP3
    private String MP3FileName;
    //Tabel Bytes mewakili content dari MP3
    private byte[] contentBytes;
    private BitSet contentBits;
    private int ContentSize;
    private int ContentSize_bits;   
    private int positionDebut_bits = 0;
   

    // Constructor. The buffer size is not a priori necessary
    public MP3(String fileName) throws IOException {
        
        File theFileName = new File(fileName);
        System.out.println(fileName);

        if (theFileName.exists()) {

            //nama file mp3
            MP3FileName = fileName;
            // Contenu en octets du fileName
            contentBytes = FileToByteArray(fileName);
            ContentSize = contentBytes.length;
            ContentSize_bits = ContentSize * 8;
            contentBits = this.parser();

            if (contentBytes[0] == 'I' && contentBytes[1] == 'D' && contentBytes[2] == '3') {
                
                byte[] theSize = new byte[4];
                
                theSize[0] = contentBytes[7];
                theSize[1] = contentBytes[8];
                theSize[2] = contentBytes[9];
                theSize[3] = contentBytes[10];
                BitSet theSize_bits_tmp = parser(theSize);
                BitSet theSize_bits = new BitSet(32);
                for (int k = 0; k < 4; k++) {
                    for (int l = 0; l < 7; l++) {
                        theSize_bits.set((l + k * 7), theSize_bits_tmp.get(l + k * 8));
                    }
                }
                for (int k = 28; k < 32; k++) {
                    theSize_bits.clear(k);
                }
                System.out.println("ID3TAG version 2 Found");

                int theSize_int = 0;

                for (int k = 0; k < 32; k++) {
                    int pow = 1;
                    for (int l = 1; l <= k; l++) {
                        pow *= 2;
                    }
                    if (theSize_bits.get(k)) {
                        theSize_int += pow;
                    }
                }
                
                theSize_int += 10 * 8;
                positionDebut_bits = theSize_int - 1;
                positionDebut_bits = Math.round((positionDebut_bits + 4) / 8) * 8;
                
            } else {
                System.out.println("ID3TAG versi 2 TIDAK DITEMUKAN!");
            }

        } else {
            // Gestion des erreurs
            System.out.println("File Input Tidak Ada! .\n");
            System.exit(-1);
        }
    }

    // Accessors various
    public String getFileName() {
        return MP3FileName;
    }

    public byte[] getContent() {
        return contentBytes;
    }

    public int getTaille() {
        return contentBytes.length;
    }

  

    //Method to add content by substitution
    //This is to say, by replacing the bit, trying to replace the least significant bits (LSB)
    public void encode(byte[] arrMsgByte, int lsb) throws Exception {
        int messageSize = arrMsgByte.length * 8;
        String strMsgLength = Integer.toString(messageSize);

        if (arrMsgByte.length * 8 * lsb > ContentSize_bits) {
            throw new Exception("Ukuran Pesan Terlalu Besar : " + arrMsgByte.length * 8 * lsb + " bits untuk " + ContentSize_bits + " bits tersedia");
        }
        
        int position_bits = positionDebut_bits;

        strMsgLength += '\n';       
        BitSet arrMsg_bits = this.parser(arrMsgByte);

        
        byte[] header;
        try {
            header = (new StringBuffer(strMsgLength)).reverse().toString().getBytes("UTF8"/*Charset.forName("UTF-8")*/);
        } catch (UnsupportedEncodingException e) {
            header = (new StringBuffer(strMsgLength)).reverse().toString().getBytes();
        }
        
        //byte[] a = (new StringBuffer("a")).reverse().toString().getBytes("UTF8");
        //BitSet test = this.parser(a);
        
        BitSet header_bits = this.parser(header);

        for (int i = 0; i < header.length * 8; i++) {
            if (header_bits.get(i)) {
                contentBits.set(i * lsb + lsb - 1 + position_bits);                
            } else {
                contentBits.clear(i * lsb + lsb - 1 + position_bits);                
            }
        }

        for (int i = 0; i < arrMsgByte.length * 8; i++) {
            int x = (i * lsb + lsb - 1 + position_bits) + header.length * 8 * lsb;
            
            if (arrMsg_bits.get(i) != contentBits.get(x)) {
                contentBits.flip(x);
            }
        }

        contentBytes = toByteArray(contentBits);
    }

    //Methods to convert an MP3 file to a byte array
    private byte[] StreamtoByteArray(FileInputStream stream) throws IOException {
        int offset = 0;
        int remaining = (int) stream.available();
        byte[] data = new byte[remaining];
        while (remaining > 0) {
            int read = stream.read(data, offset, remaining);
            if (read <= 0) {
                throw new IOException();
            }
            remaining -= read;
            offset += read;
        }
        return data;
    }

    // Same as the above method
    private byte[] FileToByteArray(String path) throws IOException {
        byte[] binary;
        try (FileInputStream fs = new FileInputStream(path)) {
            binary = StreamtoByteArray(fs);
        }
        return binary;
    }

    // Same Methode that transforming a string to an array of bits, but with an MP3
    // It thus transforms the content (array of bytes) in bits.
    public BitSet parser() throws IOException {

        BitSet theMP3Bits = new BitSet(contentBytes.length * 8);

        for (int i = 0; i < contentBytes.length * 8; i++) {
            if ((contentBytes[contentBytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                theMP3Bits.set(i);
            }
        }
        return theMP3Bits;
    }

    public BitSet parser(byte[] lesBytes) throws IOException {

        BitSet theByteinBits = new BitSet(lesBytes.length * 8);

        for (int i = 0; i < lesBytes.length * 8; i++) {
            if ((lesBytes[lesBytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                theByteinBits.set(i);
            }
        }
        return theByteinBits;
    }

    // Method to convert a bitmap to a byte array
    public byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }


    public void stega(Message m, int lsb) throws Exception {
        byte[] messageByte = m.toBytes();
        this.encode(messageByte,lsb);
    }

    // Method to convert a byte array into an MP3 file
    public void toMP3(String name) throws Exception {
        byte[] theBytes = toByteArray(this.parser());
        try (FileOutputStream fos = new FileOutputStream(name)) {
            fos.write(theBytes);
            fos.flush();
        }
    }

  

    // Method used to decode a message containing MP3 Cache
    // For the method by substitution
    public String decoder(int lsb) throws Exception {
        BitSet theContent_bits = this.parser(contentBytes);
        BitSet theChar_bits = new BitSet(8);
        BitSet theMessage;
        String taille = "";
        byte[] theChar;
        //int position = positionDebut_bits / 8;
        int currentPosisition = positionDebut_bits + lsb - 1;
        do {
            theChar_bits.clear();
            for (int i = 0; i < 8; i++) {
                if (theContent_bits.get(currentPosisition)) {
                    theChar_bits.set(i);
                } else {
                    theChar_bits.clear(i);
                }
                currentPosisition += lsb;
            }
            theChar = toByteArray(theChar_bits);
            if ((char) theChar[0] != '\n') {
                taille += (char) theChar[0];
            }

        } while ((char) theChar[0] != '\n');
        
        int size_int = Integer.parseInt(taille);
        theMessage = new BitSet(size_int);

        for (int j = 0; j < Integer.parseInt(taille); j++) {
            if (theContent_bits.get(currentPosisition)) {
                theMessage.set(j);
            } else {
                theMessage.clear(j);
            }
            currentPosisition += lsb;
        }

        byte[] theMessage_byte = toByteArray(theMessage);
        String test = new String(theMessage_byte);

        return test;
    }
}
