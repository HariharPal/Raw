package com.drunkncode.raw;

import static com.drunkncode.raw.TokenType.AND;
import static com.drunkncode.raw.TokenType.BANG;
import static com.drunkncode.raw.TokenType.BANG_EQUAL;
import static com.drunkncode.raw.TokenType.CLASS;
import static com.drunkncode.raw.TokenType.COMMA;
import static com.drunkncode.raw.TokenType.DOT;
import static com.drunkncode.raw.TokenType.ELSE;
import static com.drunkncode.raw.TokenType.EOF;
import static com.drunkncode.raw.TokenType.EQUAL;
import static com.drunkncode.raw.TokenType.EQUAL_EQUAL;
import static com.drunkncode.raw.TokenType.FALSE;
import static com.drunkncode.raw.TokenType.FOR;
import static com.drunkncode.raw.TokenType.FUN;
import static com.drunkncode.raw.TokenType.GREATER;
import static com.drunkncode.raw.TokenType.GREATER_EQUAL;
import static com.drunkncode.raw.TokenType.IDENTIFIER;
import static com.drunkncode.raw.TokenType.IF;
import static com.drunkncode.raw.TokenType.LEFT_BRACE;
import static com.drunkncode.raw.TokenType.LEFT_PAREN;
import static com.drunkncode.raw.TokenType.LESS;
import static com.drunkncode.raw.TokenType.LESS_EQUAL;
import static com.drunkncode.raw.TokenType.MINUS;
import static com.drunkncode.raw.TokenType.NIL;
import static com.drunkncode.raw.TokenType.NUMBER;
import static com.drunkncode.raw.TokenType.OR;
import static com.drunkncode.raw.TokenType.PLUS;
import static com.drunkncode.raw.TokenType.PRINT;
import static com.drunkncode.raw.TokenType.RETURN;
import static com.drunkncode.raw.TokenType.RIGHT_BRACE;
import static com.drunkncode.raw.TokenType.RIGHT_PAREN;
import static com.drunkncode.raw.TokenType.SEMICOLON;
import static com.drunkncode.raw.TokenType.SLASH;
import static com.drunkncode.raw.TokenType.STAR;
import static com.drunkncode.raw.TokenType.STRING;
import static com.drunkncode.raw.TokenType.SUPER;
import static com.drunkncode.raw.TokenType.THIS;
import static com.drunkncode.raw.TokenType.TRUE;
import static com.drunkncode.raw.TokenType.VAR;
import static com.drunkncode.raw.TokenType.WHILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;

    static{
        keywords = new HashMap<>();
        keywords.put("var", VAR);
        keywords.put("class", CLASS);
        keywords.put("fun", FUN);
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("this", THIS);
        keywords.put("super", SUPER);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("nil", NIL);
    }
    
    private int start = 0;
    private int current = 0;
    private int line = 1; 

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    // tells if our source is consumed fully or not
    private boolean isAtEnd(){
        return current >= source.length();
    }

    // for each turn of loop we scan a single token. It provides the lexers the token type in which single charactor imagined as lexeme. Just we need to consume the next charactor and pick a token type for it.
    // if there's any unexpected character in the code then we try to throw error becuase after an error the hadError goes true but we still throw all error in one pass becuase we don't want to wait to error shows up after fixing one to surprise us.
    private void scanToken(){
        char c = advance();
        switch(c){
            case '{' : addToken(LEFT_BRACE);break;
            case '}' : addToken(RIGHT_BRACE);break;
            case '(' : addToken(LEFT_PAREN);break;
            case ')' : addToken(RIGHT_PAREN);break;
            case '-' : addToken(MINUS);break;
            case '+' : addToken(PLUS);break;
            case ',' : addToken(COMMA);break;
            case '.' : addToken(DOT);break;
            case '*' : addToken(STAR);break;
            case ';' : addToken(SEMICOLON);break;

            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;

            case '/':
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                }else if(match('*')){
                    int depth = 1;
                    while(depth > 0 && !isAtEnd()){
                        if(peek() == '\n'){
                            line++;
                            advance();
                        }else if(peek() == '/' && peekNext() == '*'){
                            advance();
                            advance();
                            depth++;
                        }else if(peek() == '*' && peekNext() == '/'){
                            advance();
                            advance();
                            depth--;
                        }else{
                            advance();
                        }
                    }
                    if(depth > 0){
                        Raw.error(line, "Unterminated block comment.");
                    }
                }else{
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t': break;
            case '\n': line++; break;

            case '"': string(); break;

            // if that's an digit we check if it's boolean or double means if it's floating point number we use to say token as NUMBER(...) or if that's DOUBLE(...) 
            // if that's an alphabat then we let it identifier 
            default:
                if(isDigit(c)){
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }else{
                    Raw.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier(){
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null){
            type = IDENTIFIER;
        }
        addToken(type);
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private void number(){
        // if that number is number we advance in it and consume as much as we want
        while(isDigit(peek())) advance();

        // if after that number there's a . then we check for after charactor of it;
        if(peek() == '.' && isDigit(peekNext())){
            advance();
            while(isDigit(peek())) advance();
        }

        // we add that number to the token and we pass literal as substring of start till current ;
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void string(){
        // Either two possiblity eiter we encounter the end of the source file or " end of string token
        // we proceed and traverse till we get end or " terminal quotes and if we encounter \n we increaese line;
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n'){
                line++;
            }
            advance();
        }
        if(isAtEnd()){
            Raw.error(line, "Unterminated String. ");
            return;
        }
        // Closing "
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // it's called lookahead it does'nt like advance that consumes the charactor it peeks the character looks for current consumed charactor;
    private char peek(){
        if(isAtEnd()){
            return '\0';
        }
        return source.charAt(current);
    }

    // used to recognize the next charactor we want to expect if thats true then we return true and increment our currrent pointer to next charactor but if it's not equal then we return false; 
    // match is used in scanToken so that advance plays as it is but match tries to match next charactor for some special operators ;
    private boolean match(char expected){
        if(isAtEnd()){
            return false;
        }
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // consumes the next charactor of the source file whenever the scan token is called and because scan token is being called till source file is at end the entire source file must converted into the List of tokens ;
    private char advance(){
        return source.charAt(current++);
    }
    
    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
