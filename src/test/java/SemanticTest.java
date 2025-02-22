import org.compiler.model.Lexer;
import org.compiler.model.Parser;
import org.compiler.model.Semantic;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class SemanticTest {
    @Test
    void testValidAssignation(){
        String programa = """
                int x;
                x = 5;
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertFalse(semantic.isError());
        assertEquals("todo bien", semantic.getMessage());
    }
    @Test
    void testValidExpression(){
        String programa = """
                int x;
                x = 3;
                x = 5 + x;
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertFalse(semantic.isError());
        assertEquals("todo bien", semantic.getMessage());
    }
    @Test
    void testNotDeclaredID(){
        String programa = """
                int x;
                x = 3;
                x = 5 + x / b;
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, la variable b no ha sido declarada", semantic.getMessage());
    }
    @Test
    void testNotInitializedID(){
        String programa = """
                int x;
                x = 3;
                int b;
                x = 5 + x / b;
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, la variable b no ha sido inicializada", semantic.getMessage());
    }
    @Test
    void testInvalidAutoUseExpression() {
        String programa = """
                int x;
                x = 5 + x;
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, la variable x no ha sido inicializada", semantic.getMessage());
    }
    @Test
    void testInvalidStringExpression() {
        String programa = """
                string a;
                a = "Hola";
                a = a / 5;
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, solo se permite la concatenacion de cadenas", semantic.getMessage());
    }
    @Test
    void testRelationalOperatorInNonBooleanExpression() {
        String programa = """
            int x;
            x = 5 > 3;
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un tipo BOOLEAN para la expresion", semantic.getMessage());
    }

    @Test
    void testMultipleRelationalOperators() {
        String programa = """
            boolean b;
            b = 5 > 3 < 2;
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, solo se permite un operador relacional por expresion", semantic.getMessage());
    }

    @Test
    void testInvalidTypeAssignment() {
        String programa = """
            int x;
            x = 3.5;
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un tipo de variable FLOAT", semantic.getMessage());
    }
    @Test
    void testInvalidStringAssignment() {
        String programa = """
            int x;
            x = "cadena";
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un tipo de variable STRING", semantic.getMessage());
    }
    @Test
    void testInvalidBooleanAssignment() {
        String programa = """
            boolean x;
            x = 2 + 3;
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un operador relacional en la expresion", semantic.getMessage());
    }
    @Test
    void testInvalidIfExpression() {
        String programa = """
            if(2 + 3){
                print("hola");
            }
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un operador relacional en la expresion", semantic.getMessage());
    }
    @Test
    void testInvalidWhileExpression() {
        String programa = """
            while(2 + 3){
                print("hola");
            }
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un operador relacional en la expresion", semantic.getMessage());
    }
    @Test
    void testValidProgram() {
        String programa = """
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
                FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertFalse(semantic.isError());
        assertEquals("todo bien", semantic.getMessage());
    }
    @Test
    void testValidRead() {
        String programa = """
            int x;
            read(x);
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertFalse(semantic.isError());
        assertEquals("todo bien", semantic.getMessage());
    }

    @Test
    void testInvalidRead() {
        String programa = """
            read(5);
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un ID para la funcion READ", semantic.getMessage());
    }

    @Test
    void testValidPrint() {
        String programa = """
            int x;
            x = 5;
            print(x);
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertFalse(semantic.isError());
        assertEquals("todo bien", semantic.getMessage());
    }

    @Test
    void testInvalidPrint() {
        String programa = """
            print(5 < "cadena");
            FIN""";
        Lexer lexer = new Lexer(programa);
        Parser parser = new Parser(lexer.getTokens());
        Semantic semantic = new Semantic(parser.isError(), parser.getMessage(), parser.getIdentificadores(), parser.getExpressionTrees());
        assertTrue(semantic.isError());
        assertEquals("Error, se esperaba un tipo de variable INT, FLOAT, STRING o BOOLEAN", semantic.getMessage());
    }
}
