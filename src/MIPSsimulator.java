import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MIPSsimulator {
    private static final int TEXT_SEGMENT_START = 0x00400000;
    private static final int DATA_SEGMENT_START = 0x10010000;

    private int[] registers;
    private int pc;
    ArrayList<String> textBinary = new ArrayList<String>();
    ArrayList<Integer> textMemory = new ArrayList<Integer>();
    ArrayList<String> dataHeaders = new ArrayList<String>();
    ArrayList<Integer> dataMemory = new ArrayList<Integer>();



    public MIPSsimulator() {
        registers = new int[32];
        pc = TEXT_SEGMENT_START;
    }

    void loadTextSegment(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int address = TEXT_SEGMENT_START;

        while ((line = reader.readLine()) != null) {
            String hexInstruction = line.trim();
            String binaryInstruction = hexToBinary(hexInstruction);
            //storeWord(binaryInstruction, address);
            textBinary.add(binaryInstruction);
            textMemory.add(address);
            address += 4;
        }

        reader.close();
    }

    void loadDataSegment(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int address = DATA_SEGMENT_START;
        int dataValue;

        String line;
        String asciiz = "";

        dataMemory.add(address);
        while (!(line = reader.readLine()).equals("00000000")) {
            int val = Integer.parseInt(line.substring(6, 8), 16);
            char c1 = (char)val;
            if(c1 == 0){
                address += 1;
                dataHeaders.add(asciiz);
                asciiz = "";
                dataMemory.add(address);
            }
            else {
                address += 1;
                asciiz += String.valueOf(c1);
            }
            int val2 = Integer.parseInt(line.substring(4, 6), 16);
            char c2 = (char)val2;
            if(c2 == 0){
                address += 1;
                dataHeaders.add(asciiz);
                asciiz = "";
                dataMemory.add(address);
            }
            else {
                address += 1;
                asciiz += String.valueOf(c2);
            }
            int val3 = Integer.parseInt(line.substring(2, 4), 16);
            char c3 = (char)val3;
            if(c3 == 0){
                address += 1;
                dataHeaders.add(asciiz);
                asciiz = "";
                dataMemory.add(address);
            }
            else {
                address += 1;
                asciiz += String.valueOf(c3);
            }
            int val4 = Integer.parseInt(line.substring(0, 2), 16);
            char c4 = (char)val4;
            if(c4 == 0){
                address += 1;
                dataHeaders.add(asciiz);
                asciiz = "";
                dataMemory.add(address);
            }
            else {
                address += 1;
                asciiz += String.valueOf(c4);
            }
        }
        for(int i = 0; i < dataMemory.size(); i++){
            if(i == dataMemory.size() - 1){
                dataMemory.remove(i);
            }
        }
        reader.close();
    }

    void run() {
        while (pc < TEXT_SEGMENT_START + (textMemory.size() * 4)) {
            String hexString = Integer.toHexString(pc);
            int instruction = fetchInstruction(pc);
            executeInstruction(textBinary.get(instruction));
            pc += 4;
        }
        System.out.println("-- program finished running (dropped off bottom) --");
    }

    private int fetchInstruction(int address) {
        int count = 0;
        for(int i = 0; i < textMemory.size(); i++){
            if(address == textMemory.get(i)){
                count = i;
                break;
            }
        }
        return count;
    }

    private void executeInstruction(String binaryInstruction) {
        String opcode = binaryInstruction.substring(0, 6);

        if (opcode.equals("000000") && !binaryInstruction.substring(26).equals("001100")) { // R-type excluding syscall
            executeRTypeInstruction(binaryInstruction);
        } else if (opcode.equals("000010")) { // J-type
            executeJInstruction(binaryInstruction);
        } else if (opcode.equals("100011")) { // lw
            executeLWInstruction(binaryInstruction);
        } else if (opcode.equals("101011")) { // sw
            executeSWInstruction(binaryInstruction);
        } else if (binaryInstruction.substring(26).equals("001100")) { // syscall
            handleSyscall();
        } else { // I-type
            executeITypeInstruction(binaryInstruction);
        }
    }

    private void executeLWInstruction(String binaryInstruction){
        int opcode = Integer.parseInt(binaryInstruction.substring(0, 6), 2);
        int base = Integer.parseInt(binaryInstruction.substring(6, 11), 2);
        int rt = Integer.parseInt(binaryInstruction.substring(11, 16), 2);
        int offset = Integer.parseInt(binaryInstruction.substring(16), 2);

        int count = 0;
        for(int i = 0; i < textMemory.size(); i++){
            if(base + (offset*4) == textMemory.get(i)){
                count = i;
                break;
            }
        }
        for(int i = 0; i < textBinary.size(); i++){
            registers[rt] = Integer.parseInt(textBinary.get(i));
        }
    }

    private void executeSWInstruction(String binaryInstruction){
        int opcode = Integer.parseInt(binaryInstruction.substring(0, 6), 2);
        int base = Integer.parseInt(binaryInstruction.substring(6, 11), 2);
        int rt = Integer.parseInt(binaryInstruction.substring(11, 16), 2);
        int offset = Integer.parseInt(binaryInstruction.substring(16), 2);
        textBinary.add(String.valueOf(registers[rt]));
        textMemory.add(registers[base] + (offset * 4));
    }

    private void executeITypeInstruction(String binaryInstruction) {
        int opcode = Integer.parseInt(binaryInstruction.substring(0, 6), 2);
        int rs = Integer.parseInt(binaryInstruction.substring(6, 11), 2);
        int rt = Integer.parseInt(binaryInstruction.substring(11, 16), 2);
        int immediate = Integer.parseInt(binaryInstruction.substring(16), 2);

        switch (opcode) {
            case 0xc: //andi
                registers[rt] = registers[rs] & signExtend(immediate);
                break;
            case 0x9: // addiu
                registers[rt] = registers[rs] + signExtend(immediate);
                break;
            case 0x4: // beq
                if(registers[rt] == registers[rs]){
                    pc += signExtend(immediate) * 4;
                }
                break;
            case 0x5: // bne
                if(registers[rt] != registers[rs]){
                    pc += signExtend(immediate) * 4;
                }
            case 0xf: // lui
                registers[rt] = immediate << 16;
                break;
            case 0xd: // ori
                registers[rt] = registers[rs] | immediate;
                break;
            default:
                System.err.println("Unsupported I-type instruction with opcode: " + Integer.toBinaryString(opcode));
                break;
        }
    }

    private int signExtend(int value) {
        if ((value & 0x8000) != 0) { // If the immediate value is negative
            value |= 0xFFFF0000; // Sign-extend to 32 bits
        }
        return value;
    }


    private void executeRTypeInstruction(String binaryInstruction) {
        int rs = Integer.parseInt(binaryInstruction.substring(6, 11), 2);
        int rt = Integer.parseInt(binaryInstruction.substring(11, 16), 2);
        int rd = Integer.parseInt(binaryInstruction.substring(16, 21), 2);
        int shamt = Integer.parseInt(binaryInstruction.substring(21, 26), 2);
        int funct = Integer.parseInt(binaryInstruction.substring(26), 2);


        switch (funct) {
            case 0x20: // add
                registers[rd] = registers[rs] + registers[rt];
                break;
            case 0x22: // sub
                registers[rd] = registers[rs] - registers[rt];
                break;
            case 0x24: // and
                registers[rd] = registers[rs] & registers[rt];
                break;
            case 0x25: // or
                registers[rd] = registers[rs] | registers[rt];
                break;
            case 0x2a: // slt
                registers[rd] = (registers[rs] < registers[rt]) ? 1 : 0;
                break;
            default:
                System.err.println("Unsupported R-type instruction with funct: " + Integer.toBinaryString(funct));
                break;
        }
    }


    private void executeJInstruction(String binaryInstruction) {
        String address = binaryInstruction.substring(6);
        pc = (pc & 0xF0000000) | (Integer.parseInt(address, 2) << 2);
        pc -= 4;
    }

    private void handleSyscall() {
        int v0 = registers[2];  // Assuming $v0 holds the syscall number
        switch (v0) {
            case 1:  // Print integer
                System.out.println(registers[4]);
                break;
            case 4:  // Print string
                printString(registers[4]);  // Implement this to read string from memory
                break;
            case 5:
                Scanner input = new Scanner(System.in);
                int userValue = input.nextInt();
                registers[2] = userValue;
                input.close();
                break;
            case 10: // Exit
                System.out.println("-- program is finished running --");
                System.exit(0);
                break;
            default:
                System.err.println("Unsupported syscall code: " + v0);
                break;
        }
    }

    private void printString(int register) {
        int count = 0;
        for(int i = 0; i < dataMemory.size(); i++){
            if(dataMemory.get(i) == register){
                count = i;
            }
        }
        System.out.println(dataHeaders.get(count));
    }


    private String hexToBinary(String hex) {
        String newString = "";
        for(int i = 0; i < hex.length(); i++){
            String hexValue = hex.substring(i, i+1);
            if(hexValue.equals("0")){
                newString += ("0000");
            }
            else if(hexValue.equals("1")){
                newString += ("0001");
            }
            else if(hexValue.equals("2")){
                newString += ("0010");
            }
            else if(hexValue.equals("3")){
                newString += ("0011");
            }
            else if(hexValue.equals("4")){
                newString += ("0100");
            }
            else if(hexValue.equals("5")){
                newString += ("0101");
            }
            else if(hexValue.equals("6")){
                newString += ("0110");
            }
            else if(hexValue.equals("7")){
                newString += ("0111");
            }
            else if(hexValue.equals("8")){
                newString += ("1000");
            }
            else if(hexValue.equals("9")){
                newString += ("1001");
            }
            else if(hexValue.equals("a")){
                newString += ("1010");
            }
            else if(hexValue.equals("b")){
                newString += ("1011");
            }
            else if(hexValue.equals("c")){
                newString += ("1100");
            }
            else if(hexValue.equals("d")){
                newString += ("1101");
            }
            else if(hexValue.equals("e")){
                newString += ("1110");
            }
            else if(hexValue.equals("f")){
                newString += ("1111");
            }
        }
        return newString;
    }
}