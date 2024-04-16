# Hobble
### "cause it certainly won't run"<br><br>

CSCI 4020U - Compilers<br>
Professor - Ken Pu<br>

To Ken: you made a dire mistake in giving us this power<br>

---

# Compiler features

### File Reading
The compiler will automatically attempt to read a file named "sample.hobl", but you can specify a different file with command line arguments.

### Error Handling
The Hobble compiler uses a revolutionary ostrich error handling technique. The compiler will warn you of errors in your code, then attempt to compile anyway.
<br>We hope your error wasn't important!

# Language features
Hobble is designed around the core principle of writing code the way _you_ want to write it.

### Comments
There are two ways to comment in Hobble.
```kt
// this is a comment
/*
this is also a comment
*/
```
### Delimiters
Hobble uses semicolons to delimit statements like c and java, but the compiler doesn't mind if you forget
```kt
x = 4; //this is fine
y = 8 //this is also fine
```
### Literals
Hobble supports all kinds of data

- booleans
- integers
- decimals
- strings
```kt
x = true;
x = True;
x = TRUE;
x = tRuE;
y = 4;
y = -4;
z = 5.3;
w = "hello there";
```
Hobble will also automatically cast your data to the best type when performing operations.

### Printing
Use the command `print()` to output data to the console.

```kt
print("hello there");
```

### Math
Hobble has lots of options for math. Remember that data is automatically cast to the best option for your operations!

```kt
print(1+1);
print(1-1);
print(1*1);
print(1/1);
print(2/-4); //prints square root of 4
print(8%3); //prints the remainder of 8/3
print(4**2); //prints 4 squared
```
Hobble can also do math with strings.

```kt
print("test "*2); //prints "test test "
print("test"/2); //prints "te"
print("test"-1); //prints "tes"

print("hello "+"there"); //prints "hello there"
print("hello"/"there"); //prints "llo"*
```
*Dividing a string by another string removes all the letters of the second string from the first.

### Comparison
You can compare booleans, integers, and decimals with the operators <,<=,>,>=,==,!=
<br>You can compare strings with the operators ==,!=

```kt
print(4<=8); //prints "true"
print("hello"=="there"); //prints "false"
```

### Variables
Set a variable in hobble with '='.

```kt
x = 4;
```
You can also increment and decrement variables that aren't strings, or use shorthand math assignment.

```kt
x = 4;
x++;
x+=10;
```

### Conditions
There are two ways to write an if statement in Hobble. We've invented a new `would` keyword to let you write the body of the statement before the condition.

```kt
if (x < 4) print("this works");
if (x < 4) {
  print("this also works");
}
if (x < 4) print("hello");
else print("there");

would print("true") if (x < 4);
would {
  print("true");
} if (x < 4) else print("false");
```
You can optionally include an `else` clause in your condition.
You can also use one statement for the body of your condition, or write multiple and wrap them with braces. This applies to both the if and else.

### Loops
Loops in Hobble are a little weird. There is no loop keyword. Instead, you can write just the condition and body, and the compiler will sort out the details!

```kt
(3 < 4) {
  print("hi"); //this will run indefinitely
}
```
You can include just a condition. Optionally, you can also include a creation statement and/or iteration, like classic `for` loops.

```kt
(x=0; x<10; x++) print("hi"); //this will run 10 times
```
Another type of loop is the ranged loop. This has different syntax.

```kt
(i in 10..20) print(i); //prints numbers from 10-20
```
All loops have an optional `do` keyword that makes them run once before evaluating the condition.

```kt
do (3>4) print("what?"); //this will print once
```
Lastly, use the `break` and `continue` keywords as expected.

```kt
x=0;
(3<4) {
  print(".");
  if (x == 10) break;
  x++;
}
```

### Functions
Define a function with the `function` keyword.

```kt
function hello() {
  print("hello");
}

hello();
```
Actually, you can use as much of the word `function` as you want, as long as the letters are in order.

```kt
fun hello() print("hello");
f hello() print("hello");
functi hello() print("hello");
```
You can also define arguments to pass to your function.

```kt
function printx(x) print(x);

printx("hi");
```
You can return from a function with the `return` keyword.

```kt
function example(x) {
  if (x == 5) return;
  print(x);
}
```
You can also use `return` to return data.

```kt
function example(x) {
  if (x > 0) return 100/x;
}
```
Returns aren't explicitly stated, so one function could return different datatypes or only sometimes return data, as shown above.

```kt
function example(x) {
  if (x>0) return 10;
  else if (x<0) return false;
  else return "hi";
```

### Random
If you really don't want to write a condition, leave it blank and the compiler will decide if it wants to run your code.

```kt
if () print("hi!");
```
With random conditions, there is approximately a 50/50 chance of running. This also applies to loops, by the way.

```kt
() print("hi"); //this will print a random number of times; as long as the compiler wants to!
```

### Constants
Most languages have some way to make constant variables. In Hobble, you can make variables constant whenever you want!

```kt
x = 4;
const x;
x = 8;
print(x); //prints "4"
```
You can also make a constant variable mutable again with the `deconst` keyword.

```kt
x = 4;
const x;
deconst x;
x = 8;
print(x); //prints "8"
```
const and deconst also apply to function calls.

```kt
function hello(x) print(x);

const hello("hi");
hello("what"); //prints "hi"
```

# Future features
### Compound Data
Arrays, lists, and a special new datatype called a `bus`.

### Classes
As expected.

### Operator Overloading
Manual setting of operators.

### Please
`please` keyword to run your code without checking safety. You know what you're doing.

### Nevermind
Undoes all operations of the current scope so far.

# Progress
Hobble is still in active development, watch this repo to keep up with it!
