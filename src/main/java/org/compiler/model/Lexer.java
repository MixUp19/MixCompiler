package org.compiler.model;

import org.compiler.model.util.Pair;
import org.compiler.model.util.TiposDeTokens;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final String input;
    private int posicion;
    private int linea;
    private final Map<String, TiposDeTokens> palabrasReservadasMap = new HashMap<>();
    private final ArrayList<Pair<TiposDeTokens,String,Integer>> tokens = new ArrayList<>();

    private final Pattern patternID = Pattern.compile("[a-zA-Z]+");
    private final Pattern patternNUMERO = Pattern.compile("[0-9]+");
    private final Pattern patternN_FRACCION = Pattern.compile("[0-9]+\\.[0-9]+");
    private final Pattern patternAPERTO_PAR = Pattern.compile("\\(");
    private final Pattern patternCERRADO_PAR = Pattern.compile("\\)");
    private final Pattern patternAPERTO_LLA = Pattern.compile("\\{");
    private final Pattern patternCERRADO_LLA = Pattern.compile("}");
    private final Pattern patternSUMA = Pattern.compile("\\+");
    private final Pattern patternRESTA = Pattern.compile("-");
    private final Pattern patternMULTIPLICACION = Pattern.compile("\\*");
    private final Pattern patternDIVISION = Pattern.compile("/");
    private final Pattern patternMAYOR = Pattern.compile(">");
    private final Pattern patternMENOR = Pattern.compile("<");
    private final Pattern patternMAYOR_IGUAL = Pattern.compile(">=");
    private final Pattern patternMENOR_IGUAL = Pattern.compile("<=");
    private final Pattern patternASIGNACION = Pattern.compile("=");
    private final Pattern patternIGUAL = Pattern.compile("==");
    private final Pattern  patternCADENA = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");

    public Lexer(String input){
        palabrasReservadasMap.put("if", TiposDeTokens.IF);
        palabrasReservadasMap.put("else", TiposDeTokens.ELSE);
        palabrasReservadasMap.put("while", TiposDeTokens.WHILE);
        palabrasReservadasMap.put("int", TiposDeTokens.INT);
        palabrasReservadasMap.put("float", TiposDeTokens.FLOAT);
        palabrasReservadasMap.put("boolean", TiposDeTokens.BOOLEAN);
        palabrasReservadasMap.put("string", TiposDeTokens.STRING);
        palabrasReservadasMap.put("print", TiposDeTokens.PRINT);
        palabrasReservadasMap.put("println", TiposDeTokens.PRINTLN);
        palabrasReservadasMap.put("read", TiposDeTokens.READ);
        palabrasReservadasMap.put("true", TiposDeTokens.TRUE);
        palabrasReservadasMap.put("false", TiposDeTokens.FALSE);
        palabrasReservadasMap.put("FIN", TiposDeTokens.FIN);

        this.input = input;
        this.posicion = 0;
        this.linea = 1;
        if (input.isBlank()){
            tokens.add(new Pair<>(TiposDeTokens.ERROR,"No se ingreso ningun texto",linea));
        }
        scan();
    }

    public void scan() {
        while (posicion < input.length()) {
            if(input.charAt(posicion)=='\n'){
                linea++;
                posicion++;
                continue;
            }

            if (Character.isWhitespace(input.charAt(posicion))) {
                posicion++;
                continue;
            }

            if (input.charAt(posicion)==';') {
                tokens.add(new Pair<>(TiposDeTokens.PC,";",linea));
                posicion++;
                continue;
            }
            if (intentarToken(patternN_FRACCION, TiposDeTokens.N_FRACCION)) {
                continue;
            }
            if (intentarToken(patternNUMERO, TiposDeTokens.NUMERO)) {
                continue;
            }
            if (intentarToken(patternCADENA, TiposDeTokens.CADENA)) {
                continue;
            }
            if (intentarToken(patternID, TiposDeTokens.ID)) {
                String lexema = tokens.getLast().getSecond();
                if (palabrasReservadasMap.containsKey(lexema)) {
                    TiposDeTokens tipoReservado = palabrasReservadasMap.get(lexema);
                    tokens.set(tokens.size() - 1, new Pair<>(tipoReservado, lexema, linea));
                }
                continue;
            }

            if (intentarToken(patternAPERTO_PAR, TiposDeTokens.APERTO_PAR)) continue;
            if (intentarToken(patternCERRADO_PAR, TiposDeTokens.CERRADO_PAR)) continue;
            if (intentarToken(patternAPERTO_LLA, TiposDeTokens.APERTO_LLA)) continue;
            if (intentarToken(patternCERRADO_LLA, TiposDeTokens.CERRADO_LLA)) continue;
            if (intentarToken(patternSUMA, TiposDeTokens.SUMA)) continue;
            if (intentarToken(patternRESTA, TiposDeTokens.RESTA)) continue;
            if (intentarToken(patternMULTIPLICACION, TiposDeTokens.MULTIPLICACION)) continue;
            if (intentarToken(patternDIVISION, TiposDeTokens.DIVISION)) continue;
            if (intentarToken(patternMAYOR_IGUAL, TiposDeTokens.MAYOR_IGUAL)) continue;
            if (intentarToken(patternMENOR_IGUAL, TiposDeTokens.MENOR_IGUAL)) continue;
            if (intentarToken(patternIGUAL, TiposDeTokens.IGUAL)) continue;
            if (intentarToken(patternASIGNACION, TiposDeTokens.ASIGNACION)) continue;
            if (intentarToken(patternMAYOR, TiposDeTokens.MAYOR)) continue;
            if (intentarToken(patternMENOR, TiposDeTokens.MENOR)) continue;

            tokens.add(new Pair<>(TiposDeTokens.ERROR, input.charAt(posicion) + "", linea));
            posicion++;
        }
    }


    private boolean intentarToken(Pattern pattern, TiposDeTokens tipoToken) {
        Matcher matcher = pattern.matcher(input);
        matcher.region(posicion, input.length());
        if (matcher.lookingAt()) {
            String token = matcher.group();
            tokens.add(new Pair<>(tipoToken, token, linea));
            posicion += token.length();
            return true;
        }
        return false;
    }

    public Vector<Vector<String>> getTablaTokens(){
        Vector<Vector<String>> tablaTokens = new Vector<>();
        for (Pair<TiposDeTokens,String,Integer> token : tokens){
            Vector<String> aux = new Vector<>();
            aux.add(token.getSecond());
            aux.add(token.getFirst().toString());
            tablaTokens.add(aux);
        }
        return tablaTokens;
    }

    public ArrayList<Pair<TiposDeTokens,String,Integer>> getTokens(){
        return tokens;
    }
    public static void main(String[] args) {
        new Lexer("""
                boolean c;
                c = false;
                int a;
                a = 5;
                string b;
                b = "h0l4?";
                if (c){
                print(b);
                } else {
                if (a==5){
                print(a);
                }
                }
                int z;
                read(z);
                while (z <=5){
                z = z +1;
                }
                FIN""");
    }

}
