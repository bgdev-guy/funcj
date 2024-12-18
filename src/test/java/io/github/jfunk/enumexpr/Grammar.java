package io.github.jfunk.enumexpr;

import io.github.jfunk.Combinators;
import io.github.jfunk.Parser;
import io.github.jfunk.Ref;
import io.github.jfunk.Result;
import io.github.jfunk.expr.Model;
import java.util.*;
import java.util.function.BinaryOperator;

public abstract class Grammar {

    static {
        // To get around circular references.
        final Ref<Token, Model.Expr> expr = Parser.ref();

        final Parser<Token, Token> open = Combinators.value(Token.Symbol.OPEN);
        final Parser<Token, Token> close = Combinators.value(Token.Symbol.CLOSE);

        final Parser<Token, Model.UnaryOp> plus = Combinators.value(Token.Symbol.PLUS, Model.UnaryOp.POS);
        final Parser<Token, Model.UnaryOp> minus = Combinators.value(Token.Symbol.MINUS, Model.UnaryOp.NEG);

        final Parser<Token, Model.BinOp> add = Combinators.value(Token.Symbol.PLUS, Model.BinOp.ADD);
        final Parser<Token, Model.BinOp> sub = Combinators.value(Token.Symbol.MINUS, Model.BinOp.SUBTRACT);
        final Parser<Token, Model.BinOp> mult = Combinators.value(Token.Symbol.MULT, Model.BinOp.MULTIPLY);
        final Parser<Token, Model.BinOp> div = Combinators.value(Token.Symbol.DIV, Model.BinOp.DIVIDE);

        // addSub = add | sub
        final Parser<Token, BinaryOperator<Model.Expr>> addSub =
            add.or(sub).map(Model.BinOp::ctor);

        // multDiv = mult | div
        final Parser<Token, BinaryOperator<Model.Expr>> multDiv =
            mult.or(div).map(Model.BinOp::ctor);

        // num = <dble>
        final Parser<Token, Model.Expr> num =
                Combinators.satisfy("isNumber", Token::isNumber)
                        .map(Token.Number.class::cast)
                        .map(Token.Number::value)
                        .map(Model::numExpr);

        // brackExpr = open expr close
        final Parser<Token, Model.Expr> brackExpr =
            open.andR(expr).andL(close);

        // var = <alpha>
        final Parser<Token, Model.Expr> var =
                Combinators.satisfy("isVariable", Token::isVariable)
                        .map(Token.Variable.class::cast)
                        .map(Token.Variable::name)
                        .map(Model::varExpr);

        // sign = + | -
        final Parser<Token, Model.UnaryOp> sign = plus.or(minus);

        // signedExpr = sign expr
        final Parser<Token, Model.Expr> signedExpr =
            sign.and(expr).map(Model::unaryOpExpr);

        // term = num | brackExpr | funcN | signedExpr
        final Parser<Token, Model.Expr> term =
            num.or(brackExpr).or(var).or(signedExpr);

        // prod = term chainl1 multDiv
        final Parser<Token, Model.Expr> prod = term.chainl1(multDiv);

        // expr = prod chainl1 addSub
        parser = expr.set(prod.chainl1(addSub));
    }

    public static final Parser<Token, Model.Expr> parser;

    public static Result<Token, Model.Expr> parse(List<Token> tokens) {
        return parser.parse(new ListInput<>(tokens));
    }

    public static Result<Token, Model.Expr> parse(Token[] tokens) {
        return parse(Arrays.asList(tokens));
    }
}
