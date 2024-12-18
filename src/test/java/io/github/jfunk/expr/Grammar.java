package io.github.jfunk.expr;

import io.github.jfunk.*;

import java.util.function.BinaryOperator;

public abstract class Grammar {

    private static <A> Parser<Character, A> pure(A a) {
        return Parser.pure(a);
    }

    static {
        // To get around circular references.
        final Ref<Character, Model.Expr> expr = Parser.ref();

        final Parser<Character, Character> open = Text.chr('(');
        final Parser<Character, Character> close = Text.chr(')');

        final Parser<Character, Model.UnaryOp> plus = Text.chr('+').andR(pure(Model.UnaryOp.POS));
        final Parser<Character, Model.UnaryOp> minus = Text.chr('-').andR(pure(Model.UnaryOp.NEG));

        final Parser<Character, Model.BinOp> add = Text.chr('+').andR(pure(Model.BinOp.ADD));
        final Parser<Character, Model.BinOp> sub = Text.chr('-').andR(pure(Model.BinOp.SUBTRACT));
        final Parser<Character, Model.BinOp> mult = Text.chr('*').andR(pure(Model.BinOp.MULTIPLY));
        final Parser<Character, Model.BinOp> div = Text.chr('/').andR(pure(Model.BinOp.DIVIDE));

        // addSub = add | sub
        final Parser<Character, BinaryOperator<Model.Expr>> addSub = add.or(sub).map(Model.BinOp::ctor);

        // multDiv = mult | div
        final Parser<Character, BinaryOperator<Model.Expr>> multDiv = mult.or(div).map(Model.BinOp::ctor);

        // num = <dble>
        final Parser<Character, Model.Expr> num = Text.dble.map(Model::numExpr);

        // brackExpr = open expr close
        final Parser<Character, Model.Expr> brackExpr =
            open.andR(expr).andL(close);

        // var = <alpha>
        final Parser<Character, Model.Expr> var =
            Text.alpha.map(Model::varExpr);

        // sign = + | -
        final Parser<Character, Model.UnaryOp> sign = plus.or(minus);

        // signedExpr = sign expr
        final Parser<Character, Model.Expr> signedExpr =
            sign.and(expr).map(Model::unaryOpExpr);

        // term = num | brackExpr | funcN | signedExpr
        final Parser<Character, Model.Expr> term =
            num.or(brackExpr).or(var).or(signedExpr);

        // prod = term chainl1 multDiv
        final Parser<Character, Model.Expr> prod = term.chainl1(multDiv);

        // expr = prod chainl1 addSub
        parser = expr.set(prod.chainl1(addSub));
    }

    public static final Parser<Character, Model.Expr> parser;

    public static Result<Character, Model.Expr> parse(String s) {
        return parser.parse(Input.of(s));
    }
}
