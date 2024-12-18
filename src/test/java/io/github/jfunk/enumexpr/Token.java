package io.github.jfunk.enumexpr;

public interface Token {
    enum Type {
        SYMBOL, NUMBER, VARIABLE
    }

    Token PLUS = Symbol.PLUS;
    Token MINUS = Symbol.MINUS;
    Token MULT = Symbol.MULT;
    Token DIV = Symbol.DIV;
    Token OPEN = Symbol.OPEN;
    Token CLOSE = Symbol.CLOSE;

    static Token number(double value) {
        return new Number(value);
    }

    static Token variable(char name) {
        return new Variable(name);
    }

    static boolean isSymbol(Token tok) {
        return tok.type().equals(Type.SYMBOL);
    }

    static boolean isNumber(Token tok) {
        return tok.type().equals(Type.NUMBER);
    }

    static boolean isVariable(Token tok) {
        return tok.type().equals(Type.VARIABLE);
    }

    enum Symbol implements Token {
        PLUS('+'),
        MINUS('-'),
        MULT('*'),
        DIV('/'),
        OPEN('('),
        CLOSE(')');

        Symbol(char c) {
        }

        @Override
        public Type type() {
            return Type.SYMBOL;
        }
    }

    record Number(double value) implements Token {

        @Override
            public Type type() {
                return Type.NUMBER;
            }
        }

    final class Variable implements Token {
        private final char name;

        public Variable(char name) {
            this.name = name;
        }

        @Override
        public Type type() {
            return Type.VARIABLE;
        }

        public char name() {
            return name;
        }
    }

    Type type();
}
