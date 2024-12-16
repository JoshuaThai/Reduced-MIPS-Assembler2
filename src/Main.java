/*
Authors' Names: Joshua Thaisananikone and Abdul Wahab Malik
This is the second milestone for our Reduced MIPS Assembler project, which we developed
for our Computer Architecture course at the University of Wisconsin - Milwaukee.
This is the final milestone. It builds on the first milestone to simulate the
MARS MIPS simulator through generating a .data and .text file associated with the
assembly program being run.
 */
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) throws IOException {
        // INDICES of all arrays will corresponds with the position of its corresponding instruction in
        // in instructions array.
        // We will use this to figure out the instructions(first array) being called and used it to
        // get op code(second array).
        String[][] instructions = {{"add", "addiu", "and", "andi", "beq", "bne", "j", "lui", "lw", "or", "ori",
                "slt", "sub", "sw", "syscall"},
                {"000000", "001001", "000000", "001100", "000100", "000101",
                        "000010", "001111", "100011", "000000", "001101", "000000",
                        "000000", "101011", "000000"}}; // Each array has a length of 15.
        //First array is for shamt. Second array is for function
        String[][] shamt_function = {{"00000", "", "00000", "", "", "", "", "", "", "00000", "", "00000", "00000", "", ""},//shamt
                {"100000", "", "100100", "", "", "", "", "", "", "100101", "", "101010", "100010", "", "001100"}};//functions
        String[] special_rs = {"00000"};//contains rs for lui (respectively)

        //Array full of registers with the index being used as register index for binary conversion!
        String[] registers = {"$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
                "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"};//binary version of register index.

        StringBuilder pre_result = new StringBuilder();
        StringBuilder result = new StringBuilder();
        StringBuilder writeTo = new StringBuilder();

        String[][] labels = new String[100][100];//left index at 0 contains label name
        //left index at 1 contains the base address for the label.
        int[] labelLength = new int[100];
        int labelIndex = 0;
        int lastStr = 0; //use it to increase the address properly.
        int lineNumber = 0;// use to keep track of line in order to properly do branch calculation
        //first array represent name of the labels. Second array represent the address inside of the labels
        String DataBaseAddress = "10010000"; // in hexadecimal form. Base address for .data
        String textBaseAddress = "00400000";
        ArrayList<Character> wholeStr = new ArrayList<Character>(); // will store whole string.

        File file = new File(args[0]);
        int dot =  args[0].indexOf('.');
        String fileName = args[0].substring(0, dot);
        FileWriter data = new FileWriter(fileName + ".data");
        FileWriter textTest = new FileWriter(fileName + ".text");
        // Creating an object of BufferedReader class
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String st;

        int num = 0;
        boolean viewData = false, viewText = false;
        // Condition holds true till there is no character in a string
        while ((st = br.readLine()) != null) {
            lineNumber++;
            //IGNORE COMMENTS
            int comment = st.trim().indexOf('#');
            if (comment == 0) continue; //skip line of comment

            if(st.isBlank()){//ignores empty lines.
                continue;
            }
            if(st.equals(".data")){
                viewData = true;
                continue;//skip .data line
            }
            if (st.equals(".text")){
                viewData = false; //set to false since we are viewing .text now.
                //COMPUTE THE HEXADECIMAL FOR .DATA FILE.
                while(wholeStr.size() % 4 != 0){
                    // add extra null terminator to ensure left over char are included in data file.
                    wholeStr.add('\0');
                }
                for(int i = 3; i < wholeStr.size(); i += 4){ //+1 for null terminator
                    for(int j = i; j >= i - 3; j--) {//go through 4 letters/bytes
                        int ascii = (int)wholeStr.get(j);
                        String asciiStr = Integer.toBinaryString(ascii);
                        int zeroNeeded = 8 - asciiStr.length();
                        while (zeroNeeded != 0){//makes sure that ascii binary is at full length.
                            pre_result.append("0");
                            zeroNeeded--;
                        }
                        pre_result.append(asciiStr);

                        long temp1;
                        for (int y = 0; y < 8; y += 4) {
                            String binary;
                            binary = pre_result.substring(y, y + 4);
                            temp1 = Long.parseLong(binary, 2);
                            result.append(Long.toHexString(temp1));
                        }
                        data.append(result.toString());
                        data.flush();
                        pre_result.setLength(0);
                        result.setLength(0);
                    }

                    data.append("\n"); //every 8 bits.
                    data.flush();
                }

                data.close(); //no longer writing to data file.
                viewText = true;
                continue;//skip .text line
            }
            if(viewData){
                //Stores the labels
                //Store in small endian format. (Store the string in reverse for hexadecimal)
                //LOOKING FOR QUOTATION MARKS
                int firstQuote = st.indexOf("\"");
                int secondQuote = st.indexOf("\"",firstQuote + 1);
                String label = st.substring(0,st.indexOf(":")).trim();
                String text = st.substring(firstQuote + 1, secondQuote);
                text += '\0';
                for(int i = 0; i < text.length(); i++){
                    wholeStr.add(text.charAt(i));
                }

                //convert each line into a hexadecimal value. (Going by every four letter).
                labels[0][labelIndex] = label;
                labels[1][labelIndex] = DataBaseAddress; //store address
                //We will have to change the address to accomodate the string's length.
                //FIRST STEP: Get the string length
                int length = text.length() + 1 + lastStr;// +1 for null terminator
                //SECOND STEP: Convert Data length to hexadecimal form
                String addBinary = Integer.toBinaryString(length);
                //THIRD STEP: MERGE hexadecimal with base address.
                //STEP A: Convert the string length to hexadecimal.
                long temp1 = 0;
                for(int i = 0; i < 4 - (addBinary.length() - 4); i++){
                    pre_result.append("0");
                }
                pre_result.append(addBinary);
                for (int i = 0; i < pre_result.length(); i += 4) {
                    String binary;
                    binary = pre_result.substring(i, i + 4);
                    temp1 = Long.parseLong(binary, 2);
                    result.append(decimalToHex(temp1));
                }
                pre_result.setLength(0);
                //drop hexadecimals values on end for new hexadecimal values.
                for(int i = 0; i < DataBaseAddress.length() - result.length(); i++){
                    pre_result.append(DataBaseAddress.charAt(i));
                }
                //NOW we got to calculate new DataBaseAddress.
                pre_result.append(result);
                DataBaseAddress = pre_result.toString();
                pre_result.setLength(0);
                result.setLength(0);
                labelIndex++;
                lastStr += text.length() + 1;


                continue;
            }

            if(viewText){
                String command = st;
                String mnemonic = "";
                int Space1 = 0; // helps us find the end index for reading mnemonics.
                for (int i = 0; i < command.length(); i++) { //make sure that mnemonic is read regardless of number of spaces and tabs.
                    char c = command.charAt(i);
                    if (!Character.isWhitespace(c)) {
                        Space1 = command.indexOf(" ", i);
                        if (Space1 == -1) {
                            mnemonic = command.substring(i, command.length());//should get the Mnemonics.
                        } else {
                            mnemonic = command.substring(i, Space1);//should get the Mnemonics.
                        }
                        break;
                    }
                }
                if(mnemonic.equals("#")){//skips lines of comments.
                    continue;
                }
                int comment_index = command.indexOf("#");//used to check for comments.

                //Data field to convert instruction to binary.
                String opcode = "";
                String rs = "";
                String rt = "";
                String rd = "";
                String shamt = "";
                String function = "";


                int rs_index = 0;
                int rt_index = 0;
                int rd_index = 0;

                long hex;

                //will retrieve opcode, shamt, and function
                opcode = getFields(mnemonic, instructions, shamt_function, "opcode");
                shamt = getFields(mnemonic, instructions, shamt_function, "shamt");
                function = getFields(mnemonic, instructions, shamt_function, "function");
                //NOW WE NEED TO RETRIEVE THE ARGUMENTS FOR THE INSTRUCTIONS (Registers or immeadiate values)
                int first_comma_posit = command.indexOf(",", 0);//get first comma
                int second_comma_posit = 0;
                if (first_comma_posit != -1) { // -1 means no comma.
                    second_comma_posit = command.indexOf(",", first_comma_posit + 1);//get second comma.
                }

                //retrieve the three registers parameters for appropriate mnemonics.
                //Prepare for conversion to binary.
                int space2 = 0; //enable us to read the register arguments properly.
                int register1 = 0;
                int register2 = 0;
                int register3 = 0;
                String immeadiate = "";
                String first_input = "";//Stores the first input if not a register
                String second_input = "";//Stores the second input if not a register
                int third_input = 0;//Store the second input if not a register.

                //HANDLING R-TYPES
                if (mnemonic.equals("add") || mnemonic.equals("and") || mnemonic.equals("or") || mnemonic.equals("slt")
                        || mnemonic.equals("sub")) { // all of their arguments are registers or "$".
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");

                    register1 = command.indexOf("$", 0);
                    rd = command.substring(register1, first_comma_posit).trim();//retrieve rd

                    register2 = command.indexOf("$", register1 + 1);
                    rs = command.substring(register2, second_comma_posit).trim();//retrieve rs

                    register3 = command.indexOf("$", register2 + 1);
                    rt = command.substring(register3, command.length()).trim(); //It will ignore comments

                    //get the register index
                    for (int i = 0; i < registers.length; i++) {
                        if (rd.equals(registers[i])) {
                            rd_index = i;
                        }
                        if (rs.equals(registers[i])) {
                            rs_index = i;
                        }
                        if (rt.equals(registers[i])) {
                            rt_index = i;
                        }
                    }

                    rd = Integer.toBinaryString(rd_index);//giveBinary(rd_index, 5);
                    rs = Integer.toBinaryString(rs_index);//giveBinary(rs_index, 5);
                    rt = Integer.toBinaryString(rt_index);//giveBinary(rt_index, 5);
                    pre_result.append(opcode);
                    fixRegister(rs, pre_result); // add missing zeros to rs

                    pre_result.append(rs);
                    fixRegister(rt, pre_result);
                    pre_result.append(rt);
                    fixRegister(rd, pre_result);

                    pre_result.append(rd);
                    pre_result.append(shamt);
                    pre_result.append(function);
                    long temp = 0;
                    for (int i = 0; i < 32; i += 4) {
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();
                    pre_result.setLength(0);
                    result.setLength(0);

                    //HANDLING I-TYPE
                } else if (mnemonic.equals("addiu") || mnemonic.equals("beq") || mnemonic.equals("bne") || mnemonic.equals("andi")
                        || mnemonic.equals("ori")) { //addiu,beq,bne working.

                    register1 = command.indexOf("$", 0);
                    rs = command.substring(register1, first_comma_posit);//retrieve rs

                    register2 = command.indexOf("$", register1 + 1);
                    rt = command.substring(register2, second_comma_posit);//retrieve rt

                    third_input = command.indexOf("0x");

                    if (third_input == -1) { // if third input isn't a hexadecimal.
                        if (comment_index == -1) {//not comment at all
                            immeadiate = command.substring(second_comma_posit + 2, command.length()).trim();
                        } else {//exist a comment.
                            immeadiate = command.substring(second_comma_posit + 2, comment_index).trim();
                        }
                    } else { //if third input is a hexdecimal
                        if (comment_index == -1) {//no comments
                            immeadiate = command.substring(third_input + 2, command.length()).trim();
                        } else {//exist a comment
                            immeadiate = command.substring(third_input + 2, comment_index).trim();
                        }

                    }

                    rs_index = getRegister(rs, registers);
                    rt_index = getRegister(rt, registers);
                    rs = Integer.toBinaryString(rs_index);
                    rt = Integer.toBinaryString(rt_index);
                    pre_result.append(opcode);
                    if (mnemonic.equals("beq") || mnemonic.equals("bne")) {
                        textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                        String[] arr = new String[2]; // will store our address and the immeadiate offset.
                        changeBranch(textBaseAddress, file, lineNumber, arr, immeadiate);
                        immeadiate = arr[0];

                        fixRegister(rs, pre_result);
                        pre_result.append(rs);
                        fixRegister(rt, pre_result);
                        pre_result.append(rt);
                    } else {
                        fixRegister(rt, pre_result);
                        pre_result.append(rt);
                        fixRegister(rs, pre_result);
                        pre_result.append(rs);
                        //change address here for non-branch instructions
                        textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                    }
                    if (third_input == -1) { //if not hexadecimal
                        immeadiate = Integer.toBinaryString(Integer.parseInt(immeadiate)); //giveBinary(Integer.parseInt(immeadiate), 16);
                        if (immeadiate.length() > 16) { //(mnemonic.equals("beq") || mnemonic.equals("bne") || mnemonic.equals("andi") || mnemonic.equals("ori")) &&
                            //chop off leading 1s to ensure binary is 32 bits.
                            immeadiate = immeadiate.substring(16, 32);
                        }
                        for (int i = 0; i < 16 - immeadiate.length(); i++) {
                            pre_result.append("0");
                        }
                        pre_result.append(immeadiate);
                    } else { //if hexadecimal
                        for (int i = 0; i < 4 - immeadiate.length(); i++) {
                            pre_result.append("0000");//add extra 0s.
                        }
                    }

                    long temp = 0;
                    for (int i = 0; i < pre_result.length(); i += 4) {
                        String binary;
                        if (i + 4 > pre_result.length()) {
                            binary = pre_result.substring(i, pre_result.length());
                        } else {
                            binary = pre_result.substring(i, i + 4);
                        }
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    if (third_input != -1) {
                        result.append(immeadiate);
                    }

                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();

                    result.setLength(0);
                    pre_result.setLength(0);
                } else if (mnemonic.equals("j")) {
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                    int first_space = st.indexOf(" ");
                    if (comment_index == -1) {//not comment at all
                        immeadiate = command.substring(first_space + 1, command.length()).trim();
                    } else {//exist a comment.
                        immeadiate = command.substring(first_space + 1, comment_index).trim();
                    }
                    immeadiate = changeJump(textBaseAddress,file, lineNumber, immeadiate);
                    pre_result.append(opcode);
                    pre_result.append(immeadiate);

                    //convert binary to hexadecimal
                    long temp = 0;
                    for (int i = 0; i < pre_result.length(); i += 4) {
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }

                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();

                    result.setLength(0);
                    pre_result.setLength(0);

                } else if (mnemonic.equals("lui")) {
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                    rs = special_rs[0];
                    register1 = command.indexOf("$");
                    rt = command.substring(register1, first_comma_posit).trim();
                    rt_index = getRegister(rt, registers);
                    rt = Integer.toBinaryString(rt_index);//giveBinary(rt_index, 5);

                    pre_result.append(opcode);
                    int immead_index = command.indexOf("0x", 0);
                    if (comment_index == -1) {//not comment at all
                        immeadiate = command.substring(immead_index + 2, command.length()).trim();
                    } else {//exist a comment.
                        immeadiate = command.substring(immead_index + 2, comment_index).trim();
                    }
                    pre_result.append(rs);
                    fixRegister(rt, pre_result);
                    pre_result.append(rt);
                    for (int i = 0; i < 4 - immeadiate.length(); i++) { // shift lefts by 16 bits.
                        pre_result.append("0000");//add extra 0s to ensure 32 bits.
                    }

                    //convert binary to hexadecimal
                    long temp = 0;
                    for (int i = 0; i < pre_result.length(); i += 4) {
                        String binary;
                        if (i + 4 > pre_result.length()) {
                            binary = pre_result.substring(i, pre_result.length());
                        } else {
                            binary = pre_result.substring(i, i + 4);
                        }
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    result.append(immeadiate);
                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();
                    result.setLength(0);
                    pre_result.setLength(0);
                } else if (mnemonic.equals("lw") || mnemonic.equals("sw")) { // opcode + base + rt + offset
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                    int parent1 = command.indexOf("(");
                    int parent2 = command.indexOf(")");
                    register1 = command.indexOf("$"); // gets rt
                    register2 = command.indexOf("$", register1 + 1); // gets base (register inside of parentheses.

                    rt = command.substring(register1, first_comma_posit).trim();

                    rt_index = getRegister(rt, registers);//get register number for rt
                    rs = command.substring(parent1 + 1, parent2);
                    rs_index = getRegister(rs, registers); // get register number for base.
                    rt = Integer.toBinaryString(rt_index);//giveBinary(rt_index, 5);
                    rs = Integer.toBinaryString(rs_index);//giveBinary(rs_index, 5);
                    pre_result.append(opcode);
                    fixRegister(rs, pre_result);//ensure rs field is the right number of bits.
                    pre_result.append(rs);
                    fixRegister(rt, pre_result);
                    pre_result.append(rt);

                    String offset = command.substring(first_comma_posit + 1, parent1).trim();

                    if (!offset.isBlank()) {
                        offset = Integer.toBinaryString(Integer.parseInt(offset));//giveBinary(Integer.parseInt(offset), 16);
                        if (offset.length() < 16) { //shift offset if neccessary
                            for (int i = 0; i < 16 - offset.length(); i++) {
                                pre_result.append("0");
                            }
                        } else {
                            //chop off leading 1s to ensure binary is 32 bits.(Works with negative number)
                            offset = offset.substring(16, 32);
                        }
                    } else {
                        offset = "0000000000000000";//16 bits of offset if it doesn't exist.
                    }

                    pre_result.append(offset);

                    //convert binary to hexadecimal
                    long temp = 0;
                    for (int i = 0; i < pre_result.length(); i += 4) {
                        String binary;
                        if (i + 4 > pre_result.length()) {
                            binary = pre_result.substring(i, pre_result.length());
                        } else {
                            binary = pre_result.substring(i, i + 4);
                        }
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();
                    result.setLength(0);
                    pre_result.setLength(0);

                } else if (mnemonic.equals("syscall")) { // will perform syscall for us.
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");

                    textTest.append("0000000c\n");
                    textTest.flush();
                } else if (mnemonic.equals("move")) { //MOVE working.
                    //HANDLE similar to add.
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                    opcode = getFields("add", instructions, shamt_function, "opcode");
                    shamt = getFields("add", instructions, shamt_function, "shamt");
                    function = getFields("add", instructions, shamt_function, "function");

                    register1 = command.indexOf("$", 0);
                    rd = command.substring(register1, first_comma_posit);//retrieve rd

                    //int first_space = command.indexOf(" ", first_comma_posit);
                    register2 = command.indexOf("$", register1 + 1);

                    if (comment_index == -1) {
                        rs = command.substring(register2, command.length()).trim();
                    } else {
                        rs = command.substring(register2, comment_index);
                    }

                    rt = "$zero";

                    rd_index = getRegister(rd, registers);
                    rs_index = getRegister(rs, registers);
                    rt_index = getRegister(rt, registers);

                    rd = Integer.toBinaryString(rd_index);//giveBinary(rd_index, 5);
                    rs = Integer.toBinaryString(rs_index);//giveBinary(rs_index, 5);
                    rt = Integer.toBinaryString(rt_index);//giveBinary(rt_index, 5);
                    pre_result.append(opcode);
                    fixRegister(rs, pre_result); // add missing zeros to rs

                    pre_result.append(rs);
                    fixRegister(rt, pre_result);
                    pre_result.append(rt);
                    fixRegister(rd, pre_result);

                    pre_result.append(rd);
                    pre_result.append(shamt);
                    pre_result.append(function);
                    long temp = 0;
                    for (int i = 0; i < 32; i += 4) {
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();
                    result.setLength(0);
                    pre_result.setLength(0);
                } else if (mnemonic.equals("li")) { // implement LI
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");

                    register1 = command.indexOf("$", 0);
                    rt = command.substring(register1, first_comma_posit).trim();
                    rt_index = getRegister(rt, registers);//get register number for rt.
                    //check for comments.
                    if (comment_index == -1) {//not comment at all
                        immeadiate = command.substring(first_comma_posit + 1, command.length()).trim();
                    } else {//exist a comment.
                        immeadiate = command.substring(first_comma_posit + 1, comment_index).trim();
                    }
                    long imm = Long.parseLong(immeadiate,10);
                    rt = Integer.toBinaryString(rt_index);//giveBinary(rt_index, 5);

                    if (imm >= -32768 && imm <= 65535) {
                        // Use ori for small immediates
                        pre_result.append("001101"); // opcode for ori
                        pre_result.append("00000"); // $zero as rs
                        fixRegister(rt, pre_result);
                        pre_result.append(rt);
                        pre_result.append(String.format("%16s", Long.toBinaryString(imm & 0xFFFF)).replace(' ', '0'));
                    } else {
                        // Use lui and ori for large immediates
                        int upper = (int) ((imm >> 16) & 0xFFFF);
                        int lower = (int) (imm & 0xFFFF);

                        // lui instruction
                        pre_result.append("001111"); // opcode for lui
                        pre_result.append("00000"); // $zero as rs
                        pre_result.append(Integer.toBinaryString(rt_index));//giveBinary(rt_index, 5));
                        pre_result.append(String.format("%16s", Integer.toBinaryString(upper)).replace(' ', '0'));

                        // Convert binary to hex and print
                        long temp = 0;
                        for (int i = 0; i < 32; i += 4) {
                            String binary;
                            binary = pre_result.substring(i, i + 4);
                            temp = Long.parseLong(binary, 2);
                            result.append(decimalToHex(temp));
                        }
                        textTest.append(result.toString());
                        textTest.flush();
                        // Clear the StringBuilder for the next instruction
                        result.setLength(0);

                        // ori instruction
                        result.append("001101"); // opcode for ori
                        result.append(Integer.toBinaryString(rt_index));
                        result.append(Integer.toBinaryString(rt_index));
                        result.append(String.format("%16s", Integer.toBinaryString(lower)).replace(' ', '0'));
                        textTest.append(result.toString());
                        textTest.flush();
                        result.setLength(0);
                        pre_result.setLength(0);
                        continue;
                    }
                    long temp = 0;
                    for (int i = 0; i < 32; i += 4) {
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();//We gotta use flush after doing append.

                    textTest.append("\n");
                    textTest.flush();

                    result.setLength(0);
                    pre_result.setLength(0);
                } else if (mnemonic.equals("la")){
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");

                    String address01 = "";
                    register1 = command.indexOf("$", 0);
                    rt = command.substring(register1, first_comma_posit).trim();
                    rt_index = getRegister(rt, registers);
                    rt = Integer.toBinaryString(rt_index);

                    //WE WILL USE THE LABEL TO FIND ADDRESS OR WHATEVER WE NEED!
                    String addressLabel = command.substring(first_comma_posit + 1, command.length()).trim();
                    //retrieve label address from memory.
                    for(int i = 0; i < labels[0].length; i++){
                        if(addressLabel.equals(labels[0][i])){//find address label in memory
                            address01 = labels[1][i];
                            break;
                        }
                    }
                    address01 = hexToBinary(address01);
                    for(int i = 0; i < 32 - address01.length(); i++){
                        pre_result.append("0");
                    }
                    pre_result.append(address01);
                    String upperStr = pre_result.substring(0,16);
                    String lowerStr = pre_result.substring(16,32);

                    // lui instruction
                    pre_result.append("001111"); // opcode for lui
                    pre_result.append("00000"); // $zero as rs
                    fixRegister(rt, pre_result);
                    pre_result.append(rt);
                    pre_result.append(upperStr);
                    // Convert binary to hex and print
                    // Convert binary to hex and print
                    long temp = 0;
                    for (int i = 0; i < 32; i += 4) {
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();

                    textTest.append("\n");
                    textTest.flush();

                    // Clear the StringBuilder for the next instruction
                    result.setLength(0);
                    pre_result.setLength(0);

                    // ori instruction
                    pre_result.append("001101"); // opcode for ori
                    fixRegister(rt,pre_result);
                    pre_result.append(rt); // rs is the same as rt
                    fixRegister(rt,pre_result);
                    pre_result.append(rt);

                    pre_result.append(lowerStr);
                    temp = 0;
                    for (int i = 0; i < 32; i += 4) { // convert ori to hex
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }

                    textTest.append(result.toString());
                    textTest.flush();

                    textTest.append("\n");
                    textTest.flush();

                    result.setLength(0);
                    pre_result.setLength(0);
                } else if(mnemonic.equals("blt")) {//blt
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                    register1 = command.indexOf("$", 0);
                    register2 = command.indexOf("$", register1 + 1);
                    rs = command.substring(register1, first_comma_posit).trim();//retrieve rt
                    rt = command.substring(register2, second_comma_posit).trim();//retrieve rd

                    rs_index = getRegister(rs, registers);
                    rt_index = getRegister(rt, registers);

                    int find0x = command.indexOf("0x");
                    String addressLabel = command.substring(find0x + 2, command.length()).trim();

                    long targetAddress = Long.parseLong(addressLabel);

                    rs = Integer.toBinaryString(rs_index);
                    rt = Integer.toBinaryString(rt_index);
                    String at = Integer.toBinaryString(getRegister("$at", registers));

                    // slt instruction
                    pre_result.append("000000"); // opcode for R-type
                    fixRegister(rs,pre_result);
                    pre_result.append(rs);

                    fixRegister(rt,pre_result);
                    pre_result.append(rt);
                    fixRegister(at, pre_result);
                    pre_result.append(at); // $at (assembler temporary)
                    pre_result.append("00000"); // shamt
                    pre_result.append("101010"); // function code for slt

                    // Convert binary to hex and print
                    long temp = 0;
                    for (int i = 0; i < 32; i += 4) { // convert ori to hex
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();

                    // Clear the StringBuilder for the next instruction
                    result.setLength(0);
                    pre_result.setLength(0);

                    // bne instruction
                    pre_result.append("000101"); // opcode for bne
                    fixRegister(at, pre_result);
                    pre_result.append(at); // $at
                    pre_result.append("00000"); // $zero
                    pre_result.append(String.format("%16s", Integer.toBinaryString(4 & 0xFFFF)).replace(' ', '0')); // Assuming 4 instructions ahead

                    temp = 0;
                    for (int i = 0; i < 32; i += 4) { // convert ori to hex
                        String binary;
                        binary = pre_result.substring(i, i + 4);
                        temp = Long.parseLong(binary, 2);
                        result.append(decimalToHex(temp));
                    }
                    textTest.append(result.toString());
                    textTest.flush();
                    textTest.append("\n");
                    textTest.flush();
                    result.setLength(0);
                    pre_result.setLength(0);
                } else{ //handle proceedure labels.
                    textBaseAddress = changeAddress(textBaseAddress, file, "PC");
                }
            }
        }
        textTest.close();
    }

    /*
     * We will use this method to change the address as needed for PC + 4.
     * First Scenario: PC + 4
     */
    public static String changeAddress(String currentAddress, File file, String scenario){
        //long i = 0;
        String preResult = "";
        StringBuilder result = new StringBuilder();
        if(scenario.equals("PC")){//perform PC + 4
            preResult = addHexadecimal(currentAddress,"00000004");
            for(int i = 0; i < 8 - preResult.length(); i++){
                result.append("0");
            }
            result.append(preResult);
        }
        return result.toString();
    }

    // Handle shifting addresses of any Branch instruction
    public static void changeBranch(String currentAddress, File file, int currentLine, String[] arr,
                                    String label) throws IOException {
        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int index = 0;
        int offset = 0;
        while(index != currentLine){//traverse to our current line
            line = br.readLine();
            index++;
        }
        while((line = br.readLine()) != null){
            if(index + 1 == currentLine + 1){
                currentAddress = changeAddress(currentAddress,file, "PC");
                continue;
            }
            offset +=4;
            currentAddress = changeAddress(currentAddress,file, "PC");
            if(line.contains(label)){ //break once we reach the label
                break;
            }
        }
        offset = offset/4;
        arr[0] = Integer.toString(offset);
        //final step: put each result into the respective array position.
    }
    public static String changeJump(String currentAddress, File file, int currentLine,
                                    String label) throws IOException {
        StringBuilder result = new StringBuilder();
        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int index = 0;
        int offset = 0;
        while(index != currentLine){//traverse to our current line
            line = br.readLine();
            index++;
        }
        int skip = 1;
        while((line = br.readLine()) != null){
            int comment = line.trim().indexOf('#');
            if (comment == 0) continue; //skip line of comment

            if(line.isBlank()){//ignores empty lines.
                continue;
            }
            if(skip == 1){//skip next instruction.
                currentAddress = changeAddress(currentAddress,file, "PC");
                skip--;
                continue;
            }
            currentAddress = changeAddress(currentAddress,file, "PC");
            if(line.contains(label)){ //break once we reach the label
                break;
            }
        }

        int decimal1 = Integer.parseInt(currentAddress, 16);
        int decimal2 = 4;

        // Calculate sum and convert it back to hexadecimal
        int quotient = decimal1/decimal2;
        String binResult = Integer.toBinaryString(quotient);
        //turn quotient to 26 bits.
        for(int i = 0; i < 26 - binResult.length(); i++){
            result.append("0");
        }
        for(int i = 0; i < binResult.length(); i++){
            result.append(binResult.charAt(i));
        }

        return result.toString();
        //final step: put each result into the respective array position.
    }

    public static String addHexadecimal(String a, String b) {
        // Convert hexadecimal strings to integers
        long intA = Long.parseLong(a, 16);
        long intB = Long.parseLong(b, 16);

        // Calculate sum and convert it back to hexadecimal
        long sum = intA + intB;
        return Long.toHexString(sum);
    }

    public static String getFields (String mnemonic, String[][]instructions, String[][]shamt_function, String type){
        //substring are second index exclusive.
        String field = "";
        for (int i = 0; i < 15; i++) { //retrieve opcode, shamt, and function
            if (instructions[0][i].equals(mnemonic)) {
                if (type.equals("opcode")) {
                    field = instructions[1][i];
                    break;
                }
                if (type.equals("shamt")) {
                    field = shamt_function[0][i];
                    break;
                }
                if (type.equals("function")) {
                    field = shamt_function[1][i];
                    break;
                }
                break;
            }
        }
        return field;
    }
    /*
     * Ensure register fields have right number of bits (0's and 1's)
     */
    public static void fixRegister (String reg, StringBuilder pre_result){
        //fix register fields.
        if (reg.length() < 5) { //add any missing zeros.
            for (int i = 0; i < 5 - reg.length(); i++) {
                pre_result.append("0");
            }
        }
    }
    public static int getRegister (String reg, String[]registers){
        int result = 0;
        for (int i = 0; i < registers.length; i++) {
            if (reg.equals(registers[i])) {
                result = i;
                break;
            }
        }
        return result;
    }

    // method to convert binary to decimal
    public static int binaryToDecimal ( long binary){
        // variable to store the converted
        // binary number
        int decimalNumber = 0, i = 1;

        while (binary > 0) {
            long lastDigit = binary % 10;  // Extract the last digit (0 or 1)
            decimalNumber += lastDigit * i;   // Add the value of the digit
            binary /= 10;  // Remove the last digit
            i *= 2;     // Move to the next base (2^1, 2^2, etc.)
        }

        // returning the decimal number
        return decimalNumber;
    }

    // method to convert decimal to hexadecimal
    public static String decimalToHex ( long binary)
    {
        // converting the integer to the desired
        // hex string using toHexString() method
        String hexNumber
                = Long.toHexString(binary);
        // returning the final hex string
        return hexNumber;
    }

    // method to convert Hexadecimal to Binary
    // method to convert Decimal to Binary
    public static String decimalToBinary(int decimal)
    {
        // variable to store the converted
        // binary string
        String binaryString = "";

        // loop to generate the binary
        while (decimal != 0) {
            // concatenating the remainder
            // on dividing by 2 to the
            // binary string
            binaryString = (decimal % 2) + binaryString;

            // updating the decimal integer
            // by dividing by 2 in each iteration
            decimal /= 2;
        }

        // loop to ensure that each
        // Hexadecimal character is
        // represented by 4 bits
        while (binaryString.length() % 4 != 0) {
            // adding leading 0's if the
            // character is represented by less
            // than 4 bits
            binaryString = "0" + binaryString;
        }

        // returning the converted binary string
        return binaryString;
    }

    // method to convert Hexadecimal to Binary
    public static String hexToBinary(String hexadecimal)
    {

        // declaring the variables
        int i;
        char ch;
        String binary = "";
        int returnedBinary;

        // converting the accepted Hexadecimal
        // String to upper case
        hexadecimal = hexadecimal.toUpperCase();

        // loop to iterate through the length
        // of the Hexadecimal String
        for (i = 0; i < hexadecimal.length(); i++) {

            // extracting the characters
            ch = hexadecimal.charAt(i);

            // condition to check if
            // the character is not a valid Hexadecimal
            // character
            if (Character.isDigit(ch) == false
                    && ((int)ch >= 65 && (int)ch <= 70)
                    == false) {
                // returning Invalid Hexadecimal
                // String for the invalid Hexadecimal
                // character
                binary = "Invalid Hexadecimal String";
                return binary;
            }

            // checking if the character is a valid
            // Hexadecimal alphabet
            else if ((int)ch >= 65 && (int)ch <= 70)
                // converting alphabet to
                // corresponding value such as 10
                // for A and so on using ASCII code
                returnedBinary = (int)ch - 55;
            else
                returnedBinary
                        = Integer.parseInt(String.valueOf(ch));

            // converting the decimal to binary
            // by calling the decimalToBinary() method
            binary += decimalToBinary(returnedBinary);
        }
        // returning the converted binary sequence
        return binary;
    }
}

