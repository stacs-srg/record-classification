# Data Format

Gold standard and classification data is read from CSV files. The particular CSV format variant can be controlled by the
[`set` command](command/set.html).

With the default format, commas are used to separate values. If a single value includes commas, it must be wrapped with double quotes.

A value starting with a double quote must end with one, and double quotes within the value itself must be escaped by using
two consecutive double quotes.

If a value does not start with a double quote, double quotes may occur freely within the value itself.

Examples of legal lines using the default CSV format:

    1,the quick brown fox (resulting string: the quick brown fox)
    2,"the quick brown fox" (resulting string: the quick brown fox)
    3,"the quick, brown, fox" (resulting string: the quick, brown, fox)
    4,the quick "brown" fox (resulting string: the quick "brown" fox)
    5,"the quick, ""brown"", fox" (resulting string: the quick, "brown", fox)
    6, "the quick, "brown", fox" (resulting string: "the quick, "brown", fox")
    6, "the quick brown fox (resulting string: "the quick brown fox)

Examples of illegal lines using the default CSV format:

    1,the quick, brown, fox (not strictly illegal, but will be read as multiple sub-strings)
    2,"the quick, "brown", fox" (quotes part of the value must be escaped)
    3,"the quick brown fox (value starting with a quote must end with a closing quote)
