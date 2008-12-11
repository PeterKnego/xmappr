package org.xlite;

import org.xlite.ArrayUtil;

import java.io.*;
import java.util.Random;

/**
 * User: peter
 * Date: Feb 21, 2008
 * Time: 11:10:13 PM
 */
public class BigWriteTest {

    byte[] data;
    byte[][] bigData;
    byte[] smallData;
    int last = 0;
    int blast = 0;
    int slast = 0;
    private int increment = 10000000;
    int binc = 1000000;
    int sinc = 5;

    public static void main(String[] args) throws IOException {
        BigWriteTest dis = new BigWriteTest();
//        dis.save();
        long start = System.currentTimeMillis();
//        dis.load();

        System.out.println("last: " + dis.last);
        System.out.println("duration: " + (System.currentTimeMillis() - start));
    }

    public void save() throws IOException {
        FileWriter writer = new FileWriter("bigwrite.txt");
        for (int i = 0; i < 5000000; i++) {
            writer.write(randomWord());
            writer.write("\n");
        }
        writer.close();
    }

    public void saveObjectStream() throws IOException {
        File file = new File("bigwrite.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        System.out.println("init size: " + file.length());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) file.length());
        ObjectOutputStream os = new ObjectOutputStream(bytes);

        String line;
        while ((line = in.readLine()) != null) {
            os.writeObject(line);
        }
        in.close();
        System.out.println("size: " + bytes.size());
    }

    public void load() throws IOException {
        File file = new File("bigwrite.txt");
        System.out.println("file length: " + file.length());
        int len = (int) (1.1 * file.length());
        data = new byte[len];
        bigData = new byte[1000000][];
        smallData = new byte[5];
        BufferedReader in = new BufferedReader(new FileReader(file));
        int ch;
        while (in.ready()) {
            ch = in.read();
            if (ch != -1) {
                needsResize();
                data[++last] = (byte) ch;
            } else {
                System.out.println("ERROR: EOF encountered!");
            }
        }
//        while (in.ready()) {
//            ch = in.read();
//            if (ch == 10) {
//                bresize();
//                smallData = new char[10];
//                slast = 0;
//                bigData[++blast] = smallData;
//            } else {
//                sresize();
//                smallData[++slast] = (char) ch;
//            }
//        }
//        in.close();
    }

    static public String randomWord() {
        Random rand = new Random();
        int c;
        char[] chars = new char[10];
        for (int i = 0; i < chars.length - 1; i++) {
            c = rand.nextInt(123 - 97) + 97;
            chars[i] = (char) c;
        }
        chars[9] = '\u00fc';
        return new String(chars);
    }

    private void needsResize() {
        if (last == data.length - 1) {
            data = ArrayUtil.arrayCopy(data, (int) (data.length + increment));
            System.out.println("new length: " + data.length);
            System.gc();
        }
    }

    private void sresize() {
        if (slast == smallData.length - 1) {
            smallData = ArrayUtil.arrayCopy(smallData, (int) (smallData.length + sinc));
//             System.out.println("new length: "+smallData.length);
//             System.gc();
        }
    }

    private void bresize() {
        if (blast == bigData.length - 1) {
            bigData = ArrayUtil.arrayCopy(bigData, (int) (bigData.length + binc));
            System.out.println("new length: " + bigData.length);
            System.gc();
        }
    }

}
