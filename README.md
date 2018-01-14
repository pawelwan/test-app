# Parallel Calculator

To run application:

```
sbt run
```

To run tests:

```
sbt test
```

## Algorithm

Parsing of given string to AST is done by using _scala.util.parsing_ module.

Each subexpression is converted into _BinOp_ or _Num_ object (which extends _Expr_ trait).
_Num_ contains single _Double_ value, whereas _BinOp_ consists of: type of operation (_Add_, _Sub_, _Mul_, _Div_),
left and right operands (of type _Expr_).

The AST that has been created in a way described above is then balanced. 
For example `1+2+3+4` is parsed into:
```
BinOp(Add,
    BinOp(Add,
        Num(1.0), 
        Num(2.0)
    ),
    BinOp(Add,
        Num(3.0), 
        Num(4.0)
    )
)
```
instead of:
```
BinOp(Add,
    Num(1.0),
    BinOp(Add,
        Num(2.0),
        BinOp(Add, 
            Num(3.0), 
            Num(4.0)
        )
    )
)
```

Tree of similar shape is created for `1*2*3*4`.

So as to achieve this goal, each subtraction is converted into addition (and each division into multiplication). 
For example:
```
BinOp(Sub, Num(1.0), Num(1.0))
```
would be converted into:
```
BinOp(Add, Num(1.0), BinOp(Sub, Num(0.0), Num(1.0)))
```

Balancing AST enables more actors to execute computation in parallel without waiting for each other.

After that, AST is sent to _EvaluationActor_, which creates two actors of the same type.
The first one should evaluate left operand, the second one - right operand. Subsequently, actor waits for 
two results of this evaluations, then computes given operation and responds to parent with the result.
For optimization reasons, actors for simple expressions such as `Num(3.0)` are not created.

