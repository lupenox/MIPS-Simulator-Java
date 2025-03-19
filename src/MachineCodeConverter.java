import java.math.BigInteger;

public class MachineCodeConverter {
    private final String hexInstruction;
    private final String binaryInstruction;

    public MachineCodeConverter(String hexInstruction) {
        this.hexInstruction = hexInstruction;
        this.binaryInstruction = hexToBinary(hexInstruction);
    }

    private String hexToBinary(String hex) {
        return new BigInteger(hex, 16).toString(2);
    }

    public String disassemble() {
        return decodeInstruction(binaryInstruction);
    }

    private String decodeInstruction(String binary) {
        binary = String.format("%32s", binary).replace(' ', '0');
        String opcode = binary.substring(0, 6);

        if (opcode.equals("000000") && !binary.substring(26).equals("001100")) { // R-type excluding syscall
            return processRTypeInstruction(binary);
        } else if (opcode.equals("000010")) { // J-type
            return processJTypeInstruction(binary);
        } else if (binary.substring(26).equals("001100")) { // syscall
            return processSyscallInstruction();
        } else { // I-type
            return processITypeInstruction(binary);
        }
    }

    private String processRTypeInstruction(String binary) {
        String operation = getRTypeOperation(binary.substring(26)).toLowerCase();
        String rs = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(6, 11), 2)), 2);
        String rt = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(11, 16), 2)), 2);
        String rd = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(16, 21), 2)), 2);
        String shamt = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(21, 26), 2)), 2);
        String funct = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(26), 2)), 2);

        return String.format("%s {opcode: 00, rs: %s, rt: %s, rd: %s, shmt: %s, funct: %s}", operation, rs, rt, rd, shamt, funct);
    }

    private String processITypeInstruction(String binary) {
        String operation = getITypeOperation(binary.substring(0, 6)).toLowerCase();
        String opcode = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(0, 6), 2)), 2);
        String rs = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(6, 11), 2)), 2);
        String rt = formatHex(Integer.toHexString(Integer.parseInt(binary.substring(11, 16), 2)), 2);
        String immediate = formatHex(Integer.toHexString(new BigInteger(binary.substring(16), 2).intValue()), 4);

        return String.format("%s {opcode: %s, rs(base): %s, rt: %s, immediate(offset): %s}", operation, opcode, rs, rt, immediate);
    }

    private String processJTypeInstruction(String binary) {
        String address = formatHex(Long.toHexString(Long.parseLong(binary.substring(6), 2)), 7);
        return String.format("j {opcode: 02, index: %s}", address);
    }

    private String processSyscallInstruction() {
        return "syscall {opcode: 00, code: 000000, funct: 0c}";
    }

    private String formatHex(String hex, int length) {
        return String.format("%" + length + "s", hex).replace(' ', '0').toLowerCase();
    }


    private String getRTypeOperation(String functBinary) {
        switch (functBinary) {
            case "100000":
                return "add";
            case "100010":
                return "sub";
            case "100100":
                return "and";
            case "100101":
                return "or";
            case "101010":
                return "slt";
            // Add other R-type instructions here
            default:
                return "unknown";
        }
    }

    private String getITypeOperation(String opcodeBinary) {
        switch (opcodeBinary) {
            case "001001":
                return "addiu";
            case "001100":
                return "andi";
            case "000100":
                return "beq";
            case "000101":
                return "bne";
            case "001111":
                return "lui";
            case "100011":
                return "lw";
            case "001101":
                return "ori";
            case "101011":
                return "sw";
            // Add other I-type instructions here
            default:
                return "unknown";
        }
    }



}
