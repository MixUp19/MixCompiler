import org.compiler.model.util.Pair;
import org.compiler.model.util.TiposDeTokens;
import org.compiler.model.Parser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testValidAssignation()  {
        ArrayList<Pair<TiposDeTokens, String, Integer>> tokens = new ArrayList<>();
        tokens.add(new Pair<>(TiposDeTokens.INT, "int", 1));
        tokens.add(new Pair<>(TiposDeTokens.ID, "x", 1));
        tokens.add(new Pair<>(TiposDeTokens.PC, ";", 1));
        tokens.add(new Pair<>(TiposDeTokens.ID, "x", 2));
        tokens.add(new Pair<>(TiposDeTokens.ASIGNACION, "=", 2));
        tokens.add(new Pair<>(TiposDeTokens.NUMERO, "5", 2));
        tokens.add(new Pair<>(TiposDeTokens.PC, ";", 2));
        tokens.add(new Pair<>(TiposDeTokens.FIN, "", 3));

        Parser parser = new Parser(tokens);
        assertFalse(parser.isError());
        assertEquals("todo bien", parser.getMessage());
    }

    @Test
    void testUndefinedVariable()  {
        ArrayList<Pair<TiposDeTokens, String, Integer>> tokens = new ArrayList<>();
        tokens.add(new Pair<>(TiposDeTokens.ID, "x", 1));
        tokens.add(new Pair<>(TiposDeTokens.ASIGNACION, "=", 1));
        tokens.add(new Pair<>(TiposDeTokens.NUMERO, "5", 1));
        tokens.add(new Pair<>(TiposDeTokens.PC, ";", 1));
        tokens.add(new Pair<>(TiposDeTokens.FIN, "", 2));

        Parser parser = new Parser(tokens);
        assertTrue(parser.isSemanticError());
        assertEquals("Error, la variable x en la linea 1 no ha sido declarada", parser.getSemanticErrorMessage());
    }

    @Test
    void testExpressionParsing()  {
        ArrayList<Pair<TiposDeTokens, String, Integer>> tokens = new ArrayList<>();
        tokens.add(new Pair<>(TiposDeTokens.INT, "int", 1));
        tokens.add(new Pair<>(TiposDeTokens.ID, "x", 1));
        tokens.add(new Pair<>(TiposDeTokens.PC, ";", 1));
        tokens.add(new Pair<>(TiposDeTokens.ID, "x", 2));
        tokens.add(new Pair<>(TiposDeTokens.ASIGNACION, "=", 2));
        tokens.add(new Pair<>(TiposDeTokens.NUMERO, "5", 2));
        tokens.add(new Pair<>(TiposDeTokens.SUMA, "+", 2));
        tokens.add(new Pair<>(TiposDeTokens.NUMERO, "3", 2));
        tokens.add(new Pair<>(TiposDeTokens.MULTIPLICACION, "*", 2));
        tokens.add(new Pair<>(TiposDeTokens.NUMERO, "2", 2));
        tokens.add(new Pair<>(TiposDeTokens.PC, ";", 2));
        tokens.add(new Pair<>(TiposDeTokens.FIN, "", 3));

        Parser parser = new Parser(tokens);
        assertFalse(parser.isError());
        assertEquals("todo bien", parser.getMessage());
    }
}
