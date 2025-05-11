package org.compiler.model;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class ObjectCode {
    private String[] intermediateCode;
    private int line;
    private HashMap<String, Integer> names;
    private HashMap<String, Integer> tags;
    private HashMap<ArrayList<Integer>, String> calls;
    private StringBuilder objectCodeData;
    private StringBuilder objectCode;
    public ObjectCode (String intermediateCode){
        this.intermediateCode = intermediateCode.split("\n");
        this.names = new HashMap<>();
        this.tags = new HashMap<>();
        this.calls = new HashMap<>();
        this.objectCode = new StringBuilder();
        this.objectCodeData = new StringBuilder();
        while(this.line < this.intermediateCode.length){
            String[] parts = this.intermediateCode[line].replace(","," ").split(" ");
            this.line++;
            label:
            for(String part: parts){
                switch (part) {
                    case ".data":
                        decodeData();
                        objectCodeData.append("\n");
                        break label;
                    case ".bss":
                        decodeData();
                        break label;
                    case ".text":
                        decode();
                        break;
                }
            }
        }
        colocarLlamadas();
    }
    private void colocarLlamadas() {
        // Recorre cad entrada en calls
        for (Map.Entry<ArrayList<Integer>, String> entry : calls.entrySet()) {
            ArrayList<Integer> key = entry.getKey();
            String label = entry.getValue();
            int callStart = key.get(0);
            int auxLength = key.get(1);
            int insertionPos = key.get(2);

            // Obtiene la dirección de la etiqueta desde tags
            if (!tags.containsKey(label)) {
                System.out.println("Etiqueta no encontrada: " + label);
                continue;
            }
            int tagAddress = tags.get(label);

            // Calcula el desplazamiento: (inicio + longitud) - dirección de etiqueta
            int displacement = tagAddress - (callStart + auxLength);

            // Si auxLength es 2 se generan 8 bits, sino 32 bits
            int bits = (auxLength == 2) ? 8 : 32;
            String binDisplacement = processDWDD(displacement, bits, false).toString();

            // Reemplaza en objectCode el desplazamiento generado en little endian
            // Se asume que se debe reemplazar exactamente 'bits' caracteres (cada 4 bits por dígito binario)
            // Aquí se utiliza la longitud real del string generado para reemplazar
            objectCode.insert(insertionPos, binDisplacement);
        }
    }
    private void decode(){
        int bits = 0;
        ArrayList<Integer> list;
        int aux;
        while (line < this.intermediateCode.length) {
            String[] instructions = this.intermediateCode[line].replaceAll("[\t,]+"," ").replaceFirst(" ","").split("\\s+");
            if(Pattern.compile("[._:]").matcher(instructions[0]).find()){
                tags.put(instructions[0], bits);
                line++;
                continue;
            }
            switch(instructions[0]){
                case "MOV":
                    bits += movInstruction(instructions, bits);
                    objectCode.append((char)10);
                    break;
                case "ADD":
                    bits += addInstruction(instructions, bits);
                    objectCode.append((char)10);
                    break;
                case "SUB":
                    bits += subInstruction(instructions, bits);
                    objectCode.append((char)10);
                    break;
                case "MUL":
                    bits += multDivInstruction(instructions, bits, false);
                    objectCode.append((char)10);
                    break;
                case "DIV":
                    bits += multDivInstruction(instructions, bits, true);
                    objectCode.append((char)10);
                    break;
                case "SYSCALL":
                    bits += syscallInstruction(bits);
                    objectCode.append((char)10);
                    break;
                case "CALL":
                    list = new ArrayList<>();
                    list.add(bits);
                    aux = callInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "JGE":
                    list = new ArrayList<>();
                    list.add(bits);
                    aux = jgeInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "JLE":
                    list = new ArrayList<>();
                    list.add(bits);
                    aux = jleInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "JMP":
                    list = new ArrayList();
                    list.add(bits);
                    aux = jmpInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "JNE":
                    list = new ArrayList();
                    list.add(bits);
                    aux = jneInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "JL":
                    list = new ArrayList();
                    list.add(bits);
                    aux = jlInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "JG":
                    list = new ArrayList();
                    list.add(bits);
                    aux = jgInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                case "POP":
                    bits += popInstruction(instructions[1],bits);
                    objectCode.append((char)10);
                    break;
                case "PUSH":
                    bits += pushInstruction(instructions[1],bits);
                    objectCode.append((char)10);
                    break;
                case "INC":
                    bits += incInstruction(instructions[1],bits);
                    objectCode.append((char)10);
                    break;
                case "DEC":
                    bits += decInstruction(instructions[1],bits);
                    objectCode.append((char)10);
                    break;
                case "RET":
                    bits += retInstruction(bits);
                    objectCode.append((char)10);
                    break;
                case "CMP":
                    bits+= cmpInstruction(instructions,bits);
                    objectCode.append((char)10);
                    break;
                case "XOR":
                    String add = getAdd(bits);
                    objectCode.append(add).append(" ");
                    objectCode.append("010010000011000111111111");
                    objectCode.append((char)10);
                    bits+=3;
                    break;
                case "JE":
                    list = new ArrayList();
                    list.add(bits);
                    aux = jeInstruction(bits);
                    list.add(aux);
                    list.add(objectCode.length());
                    calls.put(list, instructions[1]);
                    bits+= aux;
                    objectCode.append((char)10);
                    break;
                default:
                    System.out.println("Unknown instruction: " + instructions[0]);
            }

            line++;
        }
    }
    private int cmpInstruction(String[] instructions, int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String arg1 = instructions[1];
        String arg2 = instructions[2];
        String reg1 = getRegCode(arg1);
        int tamano = getTamano(arg1);
        String REX = getRex(arg1,arg2);
        StringBuilder cmp = new StringBuilder();
        char w = getW(arg1);
        if(arg1.contains("A")){
            String reg = processDWDD(Integer.parseInt(arg2), tamano, false).toString();
            cmp.append(REX).append("001110").append(w).append(reg);
            objectCode.append(cmp);
            return cmp.length();
        }
        int valor;
        try{
            valor = Integer.parseInt(arg2);
        }catch (Exception e){
            valor = names.get(arg2);
            w='1';
        }
        String reg = processDWDD(valor, tamano, false).toString();
        cmp.append(REX).append("1000000").append(w).append("11111").append(reg1).append(reg);
        objectCode.append(cmp);
        return cmp.length();
    }
    private int retInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("11000011");
        return 1;
    }
    private int decInstruction(String reg,int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String regr = getRegCode(reg);
        objectCode.append("01001000111111111001").append(regr);
        return 3;
    }
    private int incInstruction(String reg,int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String regr = getRegCode(reg);
        objectCode.append("01001000111111111000").append(regr);
        return 3;
    }
    private int pushInstruction(String reg,int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String regr = getRegCode(reg);
        objectCode.append("01010").append(regr);
        return 1;
    }
    private int popInstruction(String reg,int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String regr = getRegCode(reg);
        objectCode.append("01011").append(regr);
        return 1;
    }
    private int jeInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("11100100");
        return 2;
    }
    private int jmpInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("11101011");
        return 2;
    }
    private int jlInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("01111100");
        return 2;
    }
    private int jneInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("01111010");
        return 2;
    }
    private int jgInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("01111111");
        return 2;
    }
    private int jleInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("01111110");
        return 2;
    }
    private int jgeInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("01111101");
        return 2;
    }
    private int callInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        objectCode.append("11101000");
        return 5;
    }
    private int syscallInstruction(int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String opcode = "000011110101";
        objectCode.append(opcode);
        return opcode.length()/2;
    }
    private int multDivInstruction(String[] instructions, int index, boolean div){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String arg1 = instructions[1];
        String reg1 = getRegCode(arg1);
        String REX = getRex(arg1,"");
        char w = getW(reg1);
        StringBuilder multi = new StringBuilder();
        if(div){
            multi.append(REX).append("1111011").append(w).append("11110").append(reg1);
            objectCode.append(multi);
            return multi.length()/8;
        }
        multi.append(REX).append("1111011").append(w).append("11100").append(reg1);
        objectCode.append(multi);
        return multi.length()/8;
    }
    private int subInstruction(String[] instructions, int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String arg1 = instructions[1];
        String arg2 = instructions[2];
        String reg1 = getRegCode(arg1);
        boolean number = Pattern.compile("[0-9]+").matcher(arg2).find();
        String reg2 = (number)? processDWDD(Integer.parseInt(arg2), obtenerBase2proximo(Integer.parseInt(arg2)), false).toString(): getRegCode(arg2);
        String REX = getRex(arg1, arg2);
        String mod = getMod(reg1);
        StringBuilder addIns = new StringBuilder();
        char w = getW(arg1);
        addIns.append(REX);
        if(number){
            if(reg1.contains("A")){
                addIns.append("0010110").append(w).append(reg2);
                objectCode.append(addIns);
                return addIns.length()/8;
            }
            addIns.append("1000001").append(w);
        }else {
            addIns.append("1000000").append(w);
        }
        addIns.append(mod).append(reg1).append(reg2);
        objectCode.append(addIns);
        return addIns.length()/8;
    }
    private int addInstruction(String[] instructions, int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String arg1 = instructions[1];
        String arg2 = instructions[2];
        String reg1 = getRegCode(arg1);
        boolean number = Pattern.compile("[0-9]+").matcher(arg2).find();
        String reg2 = (number)? processDWDD(Integer.parseInt(arg2), obtenerBase2proximo(Integer.parseInt(arg2)), false).toString(): getRegCode(arg2);
        String REX = getRex(arg1, arg2);
        String mod = getMod(reg1);
        StringBuilder addIns = new StringBuilder();
        char w = getW(arg1);
        addIns.append(REX);
        if(number){
            addIns.append("1000001").append(w);
        }else {
            addIns.append("1000000").append(w);
        }
        addIns.append(mod).append(reg1).append(reg2);
        objectCode.append(addIns);
        return addIns.length()/8;
    }
    private int movInstruction(String[] instruction, int index){
        String add = getAdd(index);
        objectCode.append(add).append(" ");
        String arg1 = instruction[1];
        String arg2 = instruction[2];
        int tamano = (arg2.contains("["))? 32:getTamano(arg1);
        String memory = getMemoryLocation(arg2, tamano);
        String REX = getRex(arg1, arg2);
        String REX2 = (arg2.contains("["))? "00100101": "";
        String mod = getMod(arg2);
        String reg1 = getRegCode(arg1);
        String reg2 = getRegCode(arg2);
        char w=getW(arg1);
        StringBuilder movInst = new StringBuilder();
        movInst.insert(0,REX);
        if(arg2.equals("byte")){
            arg2 = instruction[3];
            w='0';
            REX2 ="";
            mod ="00";
            reg2="000";
            reg1="000";
            memory = processDWDD(Integer.parseInt(arg2), 8, false).toString();
        }
        if(names.containsKey(arg2)){
            movInst.append("10111").append(reg1).append(memory);
            objectCode.append(movInst);
            return movInst.length()/8;
        }
        movInst.append(getMov(mod));
        switch(mod){
            case "":
                movInst.append(w).append(reg1).append(processDWDD(Integer.parseInt(arg2), tamano, false));
                break;
            case "11":
                movInst.append(w).append(mod).append(reg1).append(reg2);
            case "00":
                movInst.append(w).append(mod).append(reg1).append(reg2).append(REX2).append(memory);
        }
        objectCode.append(movInst);
        return movInst.length()/8;
    }
    private String getMemoryLocation(String arg2, int tamano){
        if (arg2.contains("[")) {
            arg2 = arg2.replace("[", "").replace("]", "");
        }
        if(names.containsKey(arg2)){
            int dir = names.get(arg2);
            StringBuilder addLittleEndian = processDWDD(dir, tamano, false);
            return addLittleEndian.toString();
        }
        return "";
    }
    private int getTamano(String reg){
        if(Pattern.compile(".L").matcher(reg).find()){
            return 8;
        }
        if(Pattern.compile("E.").matcher(reg).find()){
            return 32;
        }if (Pattern.compile("R.").matcher(reg).find()){
            return 64;
        }
        if(Pattern.compile(".X").matcher(reg).find()){
            return 16;
        }
        return 0;
    }
    private String getRegCode(String reg){
        if(reg.contains("B")){
            return "011";
        }else if(reg.contains("A")){
            return "000";
        }else if(reg.contains("C")){
            return "001";
        }else if(reg.contains("D")){
            return "010";
        }else if(reg.contains("SP")){
            return "100";
        }else if(reg.contains("BP")){
            return "101";
        }else if(reg.contains("SI")){
            return "110";
        }else if(reg.contains("DI")){
            return "111";
        }
        return "100";
    }
    private String getMov(String mod){
        if(mod.isEmpty()){
            return "1011";
        }
        return "1000101";
    }
    private String getMod(String arg2){
        String mod = "";
        if(Pattern.compile("]").matcher(arg2).find()){
            mod = "00";
        }else if(isRegister(arg2)){
            mod = "11";
        }
        return mod;
    }
    private char getW(String arg1){
        if(isExtended(arg1)){
            return '1';
        }
        return '0';
    }
    private String getRex(String arg1, String arg2){
        if(Pattern.compile("R.").matcher(arg1).find()){
            return "01001000";
        }else if(Pattern.compile(".X").matcher(arg1).find()){
            return "01100110";
        }else if(names.containsKey(arg2)){
            return "01001000";
        }
        return "";
    }
    private int obtenerBase2proximo(int value){
        int n= 8;
        while(value>n){
            n *=2;
        }
        return n;
    }
    private boolean isExtended(String arg1){
        return Pattern.compile(".X|R..").matcher(arg1).find();
    }
    private boolean isRegister(String arg2){
        return Pattern.compile(".L|.X|E.X|R..").matcher(arg2).find();
    }
    private void decodeData(){
        int bits =0;
        String [] actualLine = this.intermediateCode[this.line].replace(",","").replace("\t","").split(" ");
        while(!actualLine[0].equals("section")){
            String add = getAdd(bits);
            names.put(actualLine[0],bits);
            objectCodeData.append(add).append(" ");
            switch (actualLine[1]){
                case "DB", "RESB":
                    bits += processDB(actualLine);
                    break;
                case "DW":
                    bits += processDWDD(actualLine[2], 16, true);
                    break;
                case "DD":
                    bits += processDWDD(actualLine[2], 32, true);
                    break;
            }
            line++;
            objectCodeData.append("\n");
            actualLine = this.intermediateCode[this.line].replace(",","").replace("\t","").split(" ");
        }

    }
    private int processDWDD(String value, int bitsLenght, boolean space){
        if(value.equals("?")){
            return bitsLenght;
        }
        StringBuilder add = new StringBuilder(Integer.toString(Integer.parseInt(value), 2));
        while(add.length()<bitsLenght){
            add.insert(0, "0");
        }
        objectCodeData.append(littleEndian(add, space));
        return bitsLenght;
    }
    private StringBuilder processDWDD(int value, int bitsLenght, boolean space){
        StringBuilder add = new StringBuilder(Integer.toString(value, 2));
        while(add.length()<bitsLenght){
            add.insert(0, "0");
        }
        return littleEndian(add, space);
    }
    private StringBuilder littleEndian(StringBuilder value, boolean space){
        StringBuilder littleEndian = new StringBuilder();
        for(int i=value.length(); i>=8; i-=8){
            littleEndian.append(value, i-8, i);
            if(space){
                littleEndian.append(" ");
            }
        }
        return littleEndian;
    }
    private int processDB(String[] value){
        int bitsLenght;
        if(value[2].contains("\"")){
            bitsLenght = value[2].length()-2 + Integer.parseInt(value[3]);
            for(int i=1; i<value[2].length()-2; i++){
                StringBuilder add = new StringBuilder(Integer.toString(value[2].charAt(i), 2));
                while(add.length()<8){
                    add.insert(0, "0");
                }
                this.objectCodeData.append(add).append(" ");
            }
        }else if(value.length>3 && value[3].contains("dup")){
            bitsLenght =  Integer.parseInt(value[2]);
        }else if(value[1].equals("RESB")){
            bitsLenght =  Integer.parseInt(value[2]);
        } else{
            bitsLenght = 1;
            StringBuilder add = new StringBuilder(Integer.toString(Integer.parseInt(value[2]), 2));
            while(add.length()<8){
                add.insert(0, "0");
            }
            this.objectCodeData.append(add).append(" ");
        }
        return bitsLenght;
    }
    public String getObjectCode(){
        return this.objectCodeData.toString() + this.objectCode.toString();
    }
    private String getAdd(int bits){
        StringBuilder add = new StringBuilder(Integer.toString(bits, 2));
        while(add.length()<32){
            add.insert(0, "0");
        }
        return add.toString();
    }
}
