# Grammar Specification

A functional programming language with static typing, algebraic data types, pattern matching, and type inference.

```bnf
(* Program Structure *)

program        = { declaration } functionBody 
declaration    = dataDecl | typeSigDecl | functionBodyDecl

(* Data types *)
dataDecl       = "data", IDENTIFIER, { IDENTIFIER }, "=", constructor, { "|", constructor }, ";"
constructor    = IDENTIFIER, { constructorArgType }
constructorArgType = typeApplication | "(", typeApplication, ")"

(* Type signatures *)
typeSigDecl    = IDENTIFIER, ":", type, ";"
functionBodyDecl = IDENTIFIER, "=", term, ";"

(* Types *)
type           = funcType
funcType       = typeApplication, { "->", typeApplication }
typeApplication = termType, { termType }
termType       = IDENTIFIER | "Int" | "Bool" | "(", type, ")"

(* Expressions *)
term           = application
application    = { expression }+
expression     = "(", term, ")"
               | atom
               | lambda
               | conditional
               | recursion
               | matchExpression

lambda         = "λ", IDENTIFIER, ".", term
conditional    = "if", term, "then", term, "else", term
recursion      = "rec", IDENTIFIER, ".", term
matchExpression = "match", term, "with", matchCase, { "|", matchCase }, "end"

atom           = IDENTIFIER
               | BOOLEAN
               | INTEGER
               | "(", operator, ")"

(* Pattern matching *)
matchCase      = pattern, "->", term
pattern        = IDENTIFIER, { pattern }  (* Constructor pattern *)
               | IDENTIFIER               (* Variable pattern *)
               
(* Lexical Tokens *) 

IDENTIFIER = [a-zA-Z][a-zA-Z0-9_]*
INTEGER    = [0-9]+
BOOLEAN    = "True" | "False"
               
```

##  Example Programs


## Factorial Function
```
fact : Int -> Int;
fact = rec f. λn.
if n <= 1
then 1
else n * (f (n - 1));

main = fact 5;
```

## List Data Type and Functions
```
data List a = Empty | Cons a (List a);

length : List a -> Int;
length = λxs. match xs with
    Empty -> 0
    | Cons x xs -> 1 + (length xs)
end;

main = length (Cons 1 (Cons 2 Empty));
```

## Arithmetic Operations
```
add : Int -> Int -> Int;
add = λx. λy. x + y;

main = add 3 4;

```

## Boolean Operations
```
not : Bool -> Bool;
not = λb. if b then False else True;

main = not True;

```

## Tree Data Type

```
data Tree a = Leaf a | Node (Tree a) a (Tree a);

depth : Tree a -> Int;
depth = λt. match t with
    Leaf x -> 1
    | Node l x r -> 1 + (max (depth l) (depth r))
end;

main = depth (Node (Leaf 1) 2 (Leaf 3));
```

## Language Features


## Semantics

## Type Checking Rules 

```dtd
x : τ ∈ Γ
---------- (Var)
Γ ⊢ x : τ

Γ, x : τ₁ ⊢ e : τ₂
------------------- (Abs)
Γ ⊢ λx.e : τ₁ → τ₂

Γ ⊢ e₁ : τ₁ → τ₂    Γ ⊢ e₂ : τ₁
------------------------------- (App)
Γ ⊢ e₁ e₂ : τ₂

Γ ⊢ e₁ : Bool    Γ ⊢ e₂ : τ    Γ ⊢ e₃ : τ
----------------------------------------- (If)
Γ ⊢ if e₁ then e₂ else e₃ : τ

Γ, x : τ ⊢ e : τ
---------------- (Rec)
Γ ⊢ rec x.e : τ

Γ ⊢ e : T    ∀i. Γ, p_i : T ⊢ case_i : τ
---------------------------------------- (Match)
Γ ⊢ match e with p₁→e₁ | ... | pₙ→eₙ : τ

```

Static Typing: Full type inference with optional type annotations

Algebraic Data Types: Support for polymorphic type parameters and constructors

Pattern Matching: Comprehensive match expressions with multiple cases

Higher-order Functions: Lambda expressions and function composition

Recursion: Explicit recursion via rec keyword

Type Annotations: Optional type signatures with : syntax

Infix Operators: Full operator precedence and associativity rules

Operator Sections: Partial application of operators





