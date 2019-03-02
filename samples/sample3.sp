function f(a) {
    if (a > 0) {
        f(a - 1);
    } else {
        0;
    }
}

f(2);
