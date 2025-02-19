package org.compiler.model.util;

public enum TiposDeTokens {
    APERTO_PAR("APERTO_PAR",1),
    CERRADO_PAR( "CERRADO_PAR",2),
    APERTO_LLA( "APERTO_LLA",3),
    CERRADO_LLA( "CERRADO_LLA",4),
    IF("IF",5),
    WHILE("WHILE", 6),
    ELSE("ELSE", 7),
    PRINTLN("PRINTLN",8),
    PRINT("PRINT", 9),
    READ("READ",10),
    TRUE("TRUE", 11),
    FALSE("FALSE", 12),
    BOOLEAN("BOOLEAN", 13),
    STRING("STRING", 14),
    INT("INT", 15),
    FLOAT("FLOAT", 16),
    ID("ID",17),
    SUMA( "SUMA",18),
    RESTA( "RESTA",19),
    DIVISION( "DIVISION",20),
    MULTIPLICACION( "MULTIPLICACION",21),
    IGUAL( "IGUAL", 22),
    MAYOR( "MAYOR",23),
    MENOR( "MENOR",24),
    MAYOR_IGUAL( "MAYOR_IGUAL",25),
    MENOR_IGUAL( "MENOR_IGUAL",26),
    ASIGNACION( "ASIGNACION",27),
    NUMERO("NUMERO",28),
    N_FRACCION("N_FRACCION",29),
    CADENA("CADENA",30),
    FIN ("FIN",31),
    ERROR("ERROR",32),
    PC("PC",33);

    private final String tipo;
    private final int tipoInt;
    TiposDeTokens(String tipo, int tipoInt){
        this.tipo = tipo;
        this.tipoInt = tipoInt;
    }
    public int getTipoInt(){return tipoInt;}
    public static String getEnumByInt(int type){
        for (TiposDeTokens tipo : TiposDeTokens.values()){
            if (tipo.getTipoInt() == type){
                return tipo.toString();
            }
        }
        return "No identificado";
    }
    @Override
    public String toString(){return tipo;}
}
