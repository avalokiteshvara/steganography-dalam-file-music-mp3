/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipher;

/**
 *
 * @author triasfahrudin
 */
public class Vigenere {

    static final int tableRowSize = 95;
    static final int tableColumnSize = 95;
    static final int vigenereTable[][] = new int[tableRowSize][tableColumnSize];

    //constructor
    public Vigenere() {
        for (int rows = 0; rows < tableRowSize; rows++) {
            for (int columns = 0; columns < tableColumnSize; columns++) {
                vigenereTable[rows][columns] = (rows + columns) % 95;
            }
        }
    }

    public String Enc(String plainText, String key) {

        String cipherText = "";
        int keyIndex = 0;

        //serbuberlin
        //PIZZAPIZZA
        for (int ptextIndex = 0; ptextIndex < plainText.length(); ptextIndex++) {
            char pChar = plainText.charAt(ptextIndex);
            int asciiVal = (int) pChar;


            if (asciiVal < 32 || asciiVal > 126) {
                cipherText += pChar;
                continue;
            }
            int basicPlainTextValue = ((int) pChar - 32);

            char kChar = key.charAt(keyIndex);
            int basicKeyValue = ((int) kChar) - 32;
            int tableEntry = vigenereTable[basicPlainTextValue][basicKeyValue];
            char cChar = (char) (tableEntry + 32);
            cipherText += cChar;
            keyIndex++;

            if (keyIndex == key.length()) {
                keyIndex = 0;
            }
        }
        return cipherText;
    }

    public String Dec(String cipherText, String key) {

        String plainText = "";
        int keyIndex = 0;

        for (int ctextIndex = 0; ctextIndex < cipherText.length(); ctextIndex++) {
            char cChar = cipherText.charAt(ctextIndex);
            int asciiVal = (int) cChar;


            if (asciiVal < 32 || asciiVal > 126) {
                plainText += cChar;
                continue;
            }

            int basiccipherTextValue = ((int) cChar) - 32;

            char kChar = key.charAt(keyIndex);
            int basicKeyValue = ((int) kChar) - 32;

            for (int pIndex = 0; pIndex < tableColumnSize; pIndex++) {
                if (vigenereTable[basicKeyValue][pIndex] == basiccipherTextValue) {
                    char potpChar = (char) (pIndex + 32);
                    plainText += potpChar;
                }
            }

            keyIndex++;

            if (keyIndex == key.length()) {
                keyIndex = 0;
            }
        }
        return plainText;
    }

//    public static void main(String[] args) {
//        String a = "!12345defend {the} east @wall of the castle";
//        String key = "fortification";
//        System.out.println("String Asli: " + a);
//        //Vigenere vig = new Vigenere();
//        String cipher = Vigenere.Enc(a, key);
//        System.out.println("String Cipher: " + cipher);
//        String plaintext = Vigenere.Dec(cipher, key);
//        System.out.println("String Plaintext: " + plaintext);
//    }
}
