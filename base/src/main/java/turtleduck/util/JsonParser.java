// This class has been generated automatically
// from an SABNF grammar by Java APG, Verision 1.1.0.
// Copyright (c) 2021 Lowell D. Thomas, all rights reserved.
// Licensed under the 2-Clause BSD License.

package turtleduck.util;

import apg.Grammar;
import apg.Parser;
import apg.Parser.RuleCallback;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

public class JsonParser extends Grammar {

    static abstract class Callback extends RuleCallback {

        public Callback(Parser arg0) {
            super(arg0);
        }

        @Override
        public int postBranch(int offset, int length) throws Exception {
            if (length != -1) {
                System.out.println(new String(this.callbackData.inputString).substring(offset, offset + length) + " <- "
                        + this.callbackData.myData);
                accept(offset, length);
            }
            return -1;
        }

        protected abstract void accept(int offset, int length);

    }

    public static void main(String[] args) {
        var parser = new Parser(getInstance());
        String input = "{\"foo\":[1,2,\"foo\",{}],\"bar\":1.2,\"false\":false}";
        parser.setInputString(input);
        Deque<Object> stack = new ArrayDeque<>();

        try {
            parser.enableAst(true);
            parser.enableTrace(false);
            parser.setMyData(stack);
            parser.setRuleCallback(RuleNames.BEGIN_OBJECT.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    stack.push(Dict.create());
                    System.out.println("begin object – " + stack);

                }
            });
            parser.setRuleCallback(RuleNames.OBJECT.id, new Callback(parser) {

                @Override
                public void accept(int offset, int length) {
                    System.out.println("=> OBJECT");
                }

            });
            parser.setRuleCallback(RuleNames.BEGIN_ARRAY.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    stack.push(Array.create());
                    System.out.println("begin array – " + stack);
                }

            });
            parser.setRuleCallback(RuleNames.ARRAY.id, new Callback(parser) {

                @Override
                public void accept(int offset, int length) {
                    System.out.println("=> ARRAY");
                }

            });
            parser.setRuleCallback(RuleNames.STRING_CONTENT.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    System.out.println("STRING: " + input.substring(offset, offset + length));
                    stack.push(input.substring(offset, offset + length));
                }
            });
            parser.setRuleCallback(RuleNames.FALSE.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    System.out.println("FALSE: " + input.substring(offset, offset + length));
                    stack.push(false);
                }
            });
            parser.setRuleCallback(RuleNames.TRUE.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    System.out.println("TRUE: " + input.substring(offset, offset + length));
                    stack.push(true);
                }
            });
            parser.setRuleCallback(RuleNames.NULL.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    System.out.println("NULL: " + input.substring(offset, offset + length));
                    stack.push(null);
                }
            });
            parser.setRuleCallback(RuleNames.NUMBER.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    String s = input.substring(offset, offset + length);
                    System.out.println("NUMBER: " + s);
                    if (s.matches("^[+-]?[0-9]+$"))
                        stack.push(Integer.parseInt(s));
                    else
                        stack.push(Double.parseDouble(s));
                }
            });
            parser.setRuleCallback(RuleNames.MEMBER.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    var value = stack.pop();
                    var key = (String) stack.pop();
                    var dict = (Dict) stack.peek();
                    dict.put(key, value);
                    System.out.println("MEMBER: " + key + ":" + value + " => " + stack);
                }
            });
            parser.setRuleCallback(RuleNames.VALUE.id, new Callback(parser) {
                @Override
                public void accept(int offset, int length) {
                    var value = stack.pop();
                    if (stack.peek() instanceof Array) {
                        ((Array) stack.peek()).add(value);
                    } else {
                        stack.push(value);
                    }
                    System.out.println("VALUE => " + stack);
                }
            });

            var result = parser.parse();
            System.out.println(stack.pop());
            result.displayResult(System.out);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * static class ObjectCallback extends Parser.RuleCallback {
     * 
     * public ObjectCallback(Parser parser) {
     * super(parser);
     * }
     * 
     * @Override
     * public int postBranch(int offset, int length) throws Exception {
     * System.out.println("}");
     * return -1;
     * }
     * 
     * @Override
     * public int preBranch(int offset) throws Exception {
     * System.out.println("{");
     * return -1;
     * }
     * 
     * }
     * 
     */

    // public API
    public static Grammar getInstance() {
        if (factoryInstance == null) {
            factoryInstance = new JsonParser(getRules(), getUdts(), getOpcodes());
        }
        return factoryInstance;
    }

    // rule name enum
    public static int ruleCount = 58;

    public enum RuleNames {
        ARRAY("array", 18, 74, 12),
        ASCII("ascii", 46, 157, 4),
        BACKSPACE("backspace", 41, 152, 1),
        BEGIN_ARRAY("begin-array", 1, 4, 4),
        BEGIN_OBJECT("begin-object", 2, 8, 4),
        CHAR("char", 37, 137, 12),
        CR("cr", 44, 155, 1),
        DECIMAL_POINT("decimal-point", 21, 100, 1),
        DIGIT("DIGIT", 56, 220, 1),
        DIGIT1_9("digit1-9", 22, 101, 1),
        E("e", 23, 102, 3),
        EMINUS("eminus", 29, 125, 1),
        END_ARRAY("end-array", 3, 12, 4),
        END_MEMBER_SEPARATOR("end-member-separator", 7, 28, 4),
        END_OBJECT("end-object", 4, 16, 4),
        END_VALUE_SEPARATOR("end-value-separator", 8, 32, 4),
        EPLUS("eplus", 31, 127, 1),
        EXP("exp", 24, 105, 8),
        FALSE("false", 11, 50, 1),
        FORM_FEED("form-feed", 42, 153, 1),
        FRAC("frac", 25, 113, 3),
        FRAC_DIGITS("frac-digits", 26, 116, 2),
        FRAC_ONLY("frac-only", 20, 99, 1),
        HEXDIG("HEXDIG", 57, 221, 4),
        INT("int", 27, 118, 6),
        JSON_TEXT("JSON-text", 0, 0, 4),
        KEY("key", 16, 69, 4),
        KEY_BEGIN("key-begin", 17, 73, 1),
        LINE_FEED("line-feed", 43, 154, 1),
        MEMBER("member", 15, 65, 4),
        MINUS("minus", 28, 124, 1),
        NAME_SEPARATOR("name-separator", 5, 20, 4),
        NULL("null", 12, 51, 1),
        NUMBER("number", 19, 86, 13),
        OBJECT("object", 14, 53, 12),
        PLUS("plus", 30, 126, 1),
        QUOTE("quote", 38, 149, 1),
        R_SOLIDUS("r-solidus", 39, 150, 1),
        SOLIDUS("solidus", 40, 151, 1),
        STRING("string", 33, 129, 4),
        STRING_BEGIN("string-begin", 34, 133, 1),
        STRING_CONTENT("string-content", 35, 134, 2),
        STRING_END("string-end", 36, 136, 1),
        TAB("tab", 45, 156, 1),
        TRUE("true", 13, 52, 1),
        UTF16("utf16", 47, 161, 3),
        UTF16_1("utf16-1", 49, 171, 3),
        UTF16_2("utf16-2", 48, 164, 7),
        UTF16_TAIL("utf16-tail", 50, 174, 2),
        UTF8("utf8", 51, 176, 4),
        UTF8_2("utf8-2", 52, 180, 3),
        UTF8_3("utf8-3", 53, 183, 21),
        UTF8_4("utf8-4", 54, 204, 15),
        UTF8_TAIL("UTF8-tail", 55, 219, 1),
        VALUE("value", 10, 42, 8),
        VALUE_SEPARATOR("value-separator", 6, 24, 4),
        WS("ws", 9, 36, 6),
        ZERO("zero", 32, 128, 1);

        private String name;
        private int id;
        private int offset;
        private int count;

        RuleNames(String string, int id, int offset, int count) {
            this.name = string;
            this.id = id;
            this.offset = offset;
            this.count = count;
        }

        public String ruleName() {
            return name;
        }

        public int ruleID() {
            return id;
        }

        private int opcodeOffset() {
            return offset;
        }

        private int opcodeCount() {
            return count;
        }
    }

    // UDT name enum
    public static int udtCount = 0;

    public enum UdtNames {
    }

    // private
    private static JsonParser factoryInstance = null;

    private JsonParser(Rule[] rules, Udt[] udts, Opcode[] opcodes) {
        super(rules, udts, opcodes);
    }

    private static Rule[] getRules() {
        Rule[] rules = new Rule[58];
        for (RuleNames r : RuleNames.values()) {
            rules[r.ruleID()] = getRule(r.ruleID(), r.ruleName(), r.opcodeOffset(), r.opcodeCount());
        }
        return rules;
    }

    private static Udt[] getUdts() {
        Udt[] udts = new Udt[0];
        return udts;
    }

    // opcodes
    private static Opcode[] getOpcodes() {
        Opcode[] op = new Opcode[225];
        addOpcodes00(op);
        return op;
    }

    private static void addOpcodes00(Opcode[] op) {
        {
            int[] a = { 1, 2, 3 };
            op[0] = getOpcodeCat(a);
        }
        op[1] = getOpcodeRnm(9, 36); // ws
        op[2] = getOpcodeRnm(10, 42); // value
        op[3] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 5, 6, 7 };
            op[4] = getOpcodeCat(a);
        }
        op[5] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 91 };
            op[6] = getOpcodeTbs(a);
        }
        op[7] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 9, 10, 11 };
            op[8] = getOpcodeCat(a);
        }
        op[9] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 123 };
            op[10] = getOpcodeTbs(a);
        }
        op[11] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 13, 14, 15 };
            op[12] = getOpcodeCat(a);
        }
        op[13] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 93 };
            op[14] = getOpcodeTbs(a);
        }
        op[15] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 17, 18, 19 };
            op[16] = getOpcodeCat(a);
        }
        op[17] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 125 };
            op[18] = getOpcodeTbs(a);
        }
        op[19] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 21, 22, 23 };
            op[20] = getOpcodeCat(a);
        }
        op[21] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 58 };
            op[22] = getOpcodeTbs(a);
        }
        op[23] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 25, 26, 27 };
            op[24] = getOpcodeCat(a);
        }
        op[25] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 44 };
            op[26] = getOpcodeTbs(a);
        }
        op[27] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 29, 30, 31 };
            op[28] = getOpcodeCat(a);
        }
        op[29] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 44 };
            op[30] = getOpcodeTbs(a);
        }
        op[31] = getOpcodeRnm(9, 36); // ws
        {
            int[] a = { 33, 34, 35 };
            op[32] = getOpcodeCat(a);
        }
        op[33] = getOpcodeRnm(9, 36); // ws
        {
            char[] a = { 44 };
            op[34] = getOpcodeTbs(a);
        }
        op[35] = getOpcodeRnm(9, 36); // ws
        op[36] = getOpcodeRep((char) 0, Character.MAX_VALUE, 37);
        {
            int[] a = { 38, 39, 40, 41 };
            op[37] = getOpcodeAlt(a);
        }
        {
            char[] a = { 32 };
            op[38] = getOpcodeTbs(a);
        }
        {
            char[] a = { 9 };
            op[39] = getOpcodeTbs(a);
        }
        {
            char[] a = { 10 };
            op[40] = getOpcodeTbs(a);
        }
        {
            char[] a = { 13 };
            op[41] = getOpcodeTbs(a);
        }
        {
            int[] a = { 43, 44, 45, 46, 47, 48, 49 };
            op[42] = getOpcodeAlt(a);
        }
        op[43] = getOpcodeRnm(13, 52); // true
        op[44] = getOpcodeRnm(11, 50); // false
        op[45] = getOpcodeRnm(12, 51); // null
        op[46] = getOpcodeRnm(14, 53); // object
        op[47] = getOpcodeRnm(18, 74); // array
        op[48] = getOpcodeRnm(19, 86); // number
        op[49] = getOpcodeRnm(33, 129); // string
        {
            char[] a = { 102, 97, 108, 115, 101 };
            op[50] = getOpcodeTbs(a);
        }
        {
            char[] a = { 110, 117, 108, 108 };
            op[51] = getOpcodeTbs(a);
        }
        {
            char[] a = { 116, 114, 117, 101 };
            op[52] = getOpcodeTbs(a);
        }
        {
            int[] a = { 54, 55, 64 };
            op[53] = getOpcodeCat(a);
        }
        op[54] = getOpcodeRnm(2, 8); // begin-object
        op[55] = getOpcodeRep((char) 0, (char) 1, 56);
        {
            int[] a = { 57, 58, 62 };
            op[56] = getOpcodeCat(a);
        }
        op[57] = getOpcodeRnm(15, 65); // member
        op[58] = getOpcodeRep((char) 0, Character.MAX_VALUE, 59);
        {
            int[] a = { 60, 61 };
            op[59] = getOpcodeCat(a);
        }
        op[60] = getOpcodeRnm(6, 24); // value-separator
        op[61] = getOpcodeRnm(15, 65); // member
        op[62] = getOpcodeRep((char) 0, (char) 1, 63);
        op[63] = getOpcodeRnm(7, 28); // end-member-separator
        op[64] = getOpcodeRnm(4, 16); // end-object
        {
            int[] a = { 66, 67, 68 };
            op[65] = getOpcodeCat(a);
        }
        op[66] = getOpcodeRnm(16, 69); // key
        op[67] = getOpcodeRnm(5, 20); // name-separator
        op[68] = getOpcodeRnm(10, 42); // value
        {
            int[] a = { 70, 71, 72 };
            op[69] = getOpcodeCat(a);
        }
        op[70] = getOpcodeRnm(17, 73); // key-begin
        op[71] = getOpcodeRnm(35, 134); // string-content
        op[72] = getOpcodeRnm(36, 136); // string-end
        {
            char[] a = { 34 };
            op[73] = getOpcodeTbs(a);
        }
        {
            int[] a = { 75, 76, 85 };
            op[74] = getOpcodeCat(a);
        }
        op[75] = getOpcodeRnm(1, 4); // begin-array
        op[76] = getOpcodeRep((char) 0, (char) 1, 77);
        {
            int[] a = { 78, 79, 83 };
            op[77] = getOpcodeCat(a);
        }
        op[78] = getOpcodeRnm(10, 42); // value
        op[79] = getOpcodeRep((char) 0, Character.MAX_VALUE, 80);
        {
            int[] a = { 81, 82 };
            op[80] = getOpcodeCat(a);
        }
        op[81] = getOpcodeRnm(6, 24); // value-separator
        op[82] = getOpcodeRnm(10, 42); // value
        op[83] = getOpcodeRep((char) 0, (char) 1, 84);
        op[84] = getOpcodeRnm(8, 32); // end-value-separator
        op[85] = getOpcodeRnm(3, 12); // end-array
        {
            int[] a = { 87, 91, 97 };
            op[86] = getOpcodeCat(a);
        }
        op[87] = getOpcodeRep((char) 0, (char) 1, 88);
        {
            int[] a = { 89, 90 };
            op[88] = getOpcodeAlt(a);
        }
        op[89] = getOpcodeRnm(28, 124); // minus
        op[90] = getOpcodeRnm(30, 126); // plus
        {
            int[] a = { 92, 96 };
            op[91] = getOpcodeAlt(a);
        }
        {
            int[] a = { 93, 94 };
            op[92] = getOpcodeCat(a);
        }
        op[93] = getOpcodeRnm(27, 118); // int
        op[94] = getOpcodeRep((char) 0, (char) 1, 95);
        op[95] = getOpcodeRnm(25, 113); // frac
        op[96] = getOpcodeRnm(20, 99); // frac-only
        op[97] = getOpcodeRep((char) 0, (char) 1, 98);
        op[98] = getOpcodeRnm(24, 105); // exp
        op[99] = getOpcodeRnm(25, 113); // frac
        {
            char[] a = { 46 };
            op[100] = getOpcodeTbs(a);
        }
        op[101] = getOpcodeTrg((char) 49, (char) 57);
        {
            int[] a = { 103, 104 };
            op[102] = getOpcodeAlt(a);
        }
        {
            char[] a = { 101 };
            op[103] = getOpcodeTbs(a);
        }
        {
            char[] a = { 69 };
            op[104] = getOpcodeTbs(a);
        }
        {
            int[] a = { 106, 107, 111 };
            op[105] = getOpcodeCat(a);
        }
        op[106] = getOpcodeRnm(23, 102); // e
        op[107] = getOpcodeRep((char) 0, (char) 1, 108);
        {
            int[] a = { 109, 110 };
            op[108] = getOpcodeAlt(a);
        }
        op[109] = getOpcodeRnm(29, 125); // eminus
        op[110] = getOpcodeRnm(31, 127); // eplus
        op[111] = getOpcodeRep((char) 1, Character.MAX_VALUE, 112);
        op[112] = getOpcodeRnm(56, 220); // DIGIT
        {
            int[] a = { 114, 115 };
            op[113] = getOpcodeCat(a);
        }
        op[114] = getOpcodeRnm(21, 100); // decimal-point
        op[115] = getOpcodeRnm(26, 116); // frac-digits
        op[116] = getOpcodeRep((char) 1, Character.MAX_VALUE, 117);
        op[117] = getOpcodeRnm(56, 220); // DIGIT
        {
            int[] a = { 119, 120 };
            op[118] = getOpcodeAlt(a);
        }
        op[119] = getOpcodeRnm(32, 128); // zero
        {
            int[] a = { 121, 122 };
            op[120] = getOpcodeCat(a);
        }
        op[121] = getOpcodeRnm(22, 101); // digit1-9
        op[122] = getOpcodeRep((char) 0, Character.MAX_VALUE, 123);
        op[123] = getOpcodeRnm(56, 220); // DIGIT
        {
            char[] a = { 45 };
            op[124] = getOpcodeTbs(a);
        }
        {
            char[] a = { 45 };
            op[125] = getOpcodeTbs(a);
        }
        {
            char[] a = { 43 };
            op[126] = getOpcodeTbs(a);
        }
        {
            char[] a = { 43 };
            op[127] = getOpcodeTbs(a);
        }
        {
            char[] a = { 48 };
            op[128] = getOpcodeTbs(a);
        }
        {
            int[] a = { 130, 131, 132 };
            op[129] = getOpcodeCat(a);
        }
        op[130] = getOpcodeRnm(34, 133); // string-begin
        op[131] = getOpcodeRnm(35, 134); // string-content
        op[132] = getOpcodeRnm(36, 136); // string-end
        {
            char[] a = { 34 };
            op[133] = getOpcodeTbs(a);
        }
        op[134] = getOpcodeRep((char) 0, Character.MAX_VALUE, 135);
        op[135] = getOpcodeRnm(37, 137); // char
        {
            char[] a = { 34 };
            op[136] = getOpcodeTbs(a);
        }
        {
            int[] a = { 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148 };
            op[137] = getOpcodeAlt(a);
        }
        op[138] = getOpcodeRnm(46, 157); // ascii
        op[139] = getOpcodeRnm(38, 149); // quote
        op[140] = getOpcodeRnm(39, 150); // r-solidus
        op[141] = getOpcodeRnm(40, 151); // solidus
        op[142] = getOpcodeRnm(41, 152); // backspace
        op[143] = getOpcodeRnm(42, 153); // form-feed
        op[144] = getOpcodeRnm(43, 154); // line-feed
        op[145] = getOpcodeRnm(44, 155); // cr
        op[146] = getOpcodeRnm(45, 156); // tab
        op[147] = getOpcodeRnm(47, 161); // utf16
        op[148] = getOpcodeRnm(51, 176); // utf8
        {
            char[] a = { 92, 34 };
            op[149] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 92 };
            op[150] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 47 };
            op[151] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 98 };
            op[152] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 102 };
            op[153] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 110 };
            op[154] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 114 };
            op[155] = getOpcodeTbs(a);
        }
        {
            char[] a = { 92, 116 };
            op[156] = getOpcodeTbs(a);
        }
        {
            int[] a = { 158, 159, 160 };
            op[157] = getOpcodeAlt(a);
        }
        op[158] = getOpcodeTrg((char) 32, (char) 33);
        op[159] = getOpcodeTrg((char) 35, (char) 91);
        op[160] = getOpcodeTrg((char) 93, (char) 127);
        {
            int[] a = { 162, 163 };
            op[161] = getOpcodeAlt(a);
        }
        op[162] = getOpcodeRnm(48, 164); // utf16-2
        op[163] = getOpcodeRnm(49, 171); // utf16-1
        {
            int[] a = { 165, 166, 168, 169 };
            op[164] = getOpcodeCat(a);
        }
        {
            char[] a = { 92, 117 };
            op[165] = getOpcodeTbs(a);
        }
        op[166] = getOpcodeRep((char) 4, (char) 4, 167);
        op[167] = getOpcodeRnm(57, 221); // HEXDIG
        {
            char[] a = { 92, 117 };
            op[168] = getOpcodeTbs(a);
        }
        op[169] = getOpcodeRep((char) 4, (char) 4, 170);
        op[170] = getOpcodeRnm(57, 221); // HEXDIG
        {
            int[] a = { 172, 173 };
            op[171] = getOpcodeCat(a);
        }
        {
            char[] a = { 92, 117 };
            op[172] = getOpcodeTbs(a);
        }
        op[173] = getOpcodeRnm(50, 174); // utf16-tail
        op[174] = getOpcodeRep((char) 4, (char) 4, 175);
        op[175] = getOpcodeRnm(57, 221); // HEXDIG
        {
            int[] a = { 177, 178, 179 };
            op[176] = getOpcodeAlt(a);
        }
        op[177] = getOpcodeRnm(52, 180); // utf8-2
        op[178] = getOpcodeRnm(53, 183); // utf8-3
        op[179] = getOpcodeRnm(54, 204); // utf8-4
        {
            int[] a = { 181, 182 };
            op[180] = getOpcodeCat(a);
        }
        op[181] = getOpcodeTrg((char) 194, (char) 223);
        op[182] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 184, 188, 192, 196, 200 };
            op[183] = getOpcodeAlt(a);
        }
        {
            int[] a = { 185, 186, 187 };
            op[184] = getOpcodeCat(a);
        }
        {
            char[] a = { 224 };
            op[185] = getOpcodeTbs(a);
        }
        op[186] = getOpcodeTrg((char) 160, (char) 191);
        op[187] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 189, 190 };
            op[188] = getOpcodeCat(a);
        }
        op[189] = getOpcodeTrg((char) 225, (char) 236);
        op[190] = getOpcodeRep((char) 2, (char) 2, 191);
        op[191] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 193, 194, 195 };
            op[192] = getOpcodeCat(a);
        }
        {
            char[] a = { 237 };
            op[193] = getOpcodeTbs(a);
        }
        op[194] = getOpcodeTrg((char) 128, (char) 159);
        op[195] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 197, 198 };
            op[196] = getOpcodeCat(a);
        }
        {
            char[] a = { 238 };
            op[197] = getOpcodeTbs(a);
        }
        op[198] = getOpcodeRep((char) 2, (char) 2, 199);
        op[199] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 201, 202, 203 };
            op[200] = getOpcodeCat(a);
        }
        {
            char[] a = { 239 };
            op[201] = getOpcodeTbs(a);
        }
        op[202] = getOpcodeRnm(55, 219); // UTF8-tail
        op[203] = getOpcodeTrg((char) 128, (char) 189);
        {
            int[] a = { 205, 210, 214 };
            op[204] = getOpcodeAlt(a);
        }
        {
            int[] a = { 206, 207, 208 };
            op[205] = getOpcodeCat(a);
        }
        {
            char[] a = { 240 };
            op[206] = getOpcodeTbs(a);
        }
        op[207] = getOpcodeTrg((char) 144, (char) 191);
        op[208] = getOpcodeRep((char) 2, (char) 2, 209);
        op[209] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 211, 212 };
            op[210] = getOpcodeCat(a);
        }
        op[211] = getOpcodeTrg((char) 241, (char) 243);
        op[212] = getOpcodeRep((char) 3, (char) 3, 213);
        op[213] = getOpcodeRnm(55, 219); // UTF8-tail
        {
            int[] a = { 215, 216, 217 };
            op[214] = getOpcodeCat(a);
        }
        {
            char[] a = { 244 };
            op[215] = getOpcodeTbs(a);
        }
        op[216] = getOpcodeTrg((char) 128, (char) 143);
        op[217] = getOpcodeRep((char) 2, (char) 2, 218);
        op[218] = getOpcodeRnm(55, 219); // UTF8-tail
        op[219] = getOpcodeTrg((char) 128, (char) 191);
        op[220] = getOpcodeTrg((char) 48, (char) 57);
        {
            int[] a = { 222, 223, 224 };
            op[221] = getOpcodeAlt(a);
        }
        op[222] = getOpcodeTrg((char) 48, (char) 57);
        op[223] = getOpcodeTrg((char) 65, (char) 70);
        op[224] = getOpcodeTrg((char) 97, (char) 102);
    }

    public static void display(PrintStream out) {
        out.println(";");
        out.println("; turtleduck.util.JsonParser");
        out.println(";");
        out.println(";");
        out.println("; This grammar is intended to be fully compliant with RFC 8259");
        out.println("; https://tools.ietf.org/html/rfc8259");
        out.println("; The initial version was extracted from RFC 8259 with the");
        out.println("; IETF ABNF extraction tool https://tools.ietf.org/abnf/");
        out.println(";");
        out.println("; Modifications have been made to more easily capture the JSON-text values ");
        out.println("; in a single pass without need of generating and translating an AST.");
        out.println(
                "; Some additional rules are \"error productions\", added to capture and report JSON-text errors. ");
        out.println(";");
        out.println("; Characters greater than 0x7F must be UTF-8 encoded or UTF-16 escaped (\\uXXXX).");
        out.println("; UTF-8 encoding must conform with well-formed byte sequencing.");
        out.println("; (See section D92, table 3.7, https://www.unicode.org/versions/Unicode9.0.0/ch03.pdf#G7404.)");
        out.println(
                "; UTF-16 surrogate pairs are interpreted as such and any occurrence of a lone pair value is reported as an error.");
        out.println(";");
        out.println("JSON-text               = ws value ws");
        out.println("begin-array             = ws %x5B ws  ; [ left square bracket");
        out.println("begin-object            = ws %x7B ws  ; { left curly bracket");
        out.println("end-array               = ws %x5D ws  ; ] right square bracket");
        out.println("end-object              = ws %x7D ws  ; } right curly bracket");
        out.println("name-separator          = ws %x3A ws  ; : colon");
        out.println("value-separator         = ws %x2C ws  ; , comma");
        out.println(
                "end-member-separator    = ws %x2C ws  ; , comma - error production to catch illegal trailing commas");
        out.println(
                "end-value-separator     = ws %x2C ws  ; , comma - error production to catch illegal trailing commas");
        out.println("ws                      = *(");
        out.println("                        %x20 /              ; Space");
        out.println("                        %x09 /              ; Horizontal tab");
        out.println("                        %x0A /              ; Line feed or New line");
        out.println("                        %x0D )              ; Carriage return");
        out.println("value                   = true / false / null / object / array / number / string");
        out.println("false                   = %x66.61.6c.73.65   ; false");
        out.println("null                    = %x6e.75.6c.6c      ; null");
        out.println("true                    = %x74.72.75.65      ; true");
        out.println(
                "object                  = begin-object [ member *( value-separator member ) [end-member-separator] ] end-object");
        out.println("member                  = key name-separator value");
        out.println("key                     = key-begin string-content string-end");
        out.println("key-begin               = %x22");
        out.println(
                "array                   = begin-array [ value *( value-separator value ) [end-value-separator] ] end-array");
        out.println("number                  = [ minus / plus ] ((int [ frac ])/ frac-only) [ exp ]");
        out.println(
                "frac-only               = frac ; error production - fraction without preceding int is not allowd by RFC 8259  ");
        out.println("decimal-point           = %x2E       ; . period");
        out.println("digit1-9                = %x31-39         ; 1-9");
        out.println("e                       = %x65 / %x45            ; e E");
        out.println("exp                     = e [ eminus / eplus ] 1*DIGIT");
        out.println("frac                    = decimal-point frac-digits");
        out.println("frac-digits             = 1*DIGIT");
        out.println("int                     = zero / ( digit1-9 *DIGIT )");
        out.println("minus                   = %x2D ; - minus - recognized only as the integer/decimal sign");
        out.println("eminus                  = %x2D ; - minus - recognized as the exponent sign only");
        out.println(
                "plus                    = %x2B ; + plus - error production - plus sign is invalid for decimal (RFC8259)");
        out.println("eplus                   = %x2B ; + plus - recognized as exponent sign only");
        out.println("zero                    = %x30 ; 0 zero");
        out.println("string                  = string-begin string-content string-end");
        out.println("string-begin            = %x22");
        out.println("string-content          = *char");
        out.println("string-end              = %x22");
        out.println(
                "char                    = ascii / quote / r-solidus / solidus / backspace / form-feed / line-feed / cr / tab / utf16 / utf8");
        out.println("quote                   = %x5C.22 ; \"    quotation mark  U+0022");
        out.println("r-solidus               = %x5C.5C ; \\    reverse solidus U+005C");
        out.println("solidus                 = %x5C.2F ; /    solidus         U+002F");
        out.println("backspace               = %x5C.62 ; b    backspace       U+0008");
        out.println("form-feed               = %x5C.66 ; f    form feed       U+000C");
        out.println("line-feed               = %x5C.6E ; n    line feed       U+000A");
        out.println("cr                      = %x5C.72 ; r    carriage return U+000D");
        out.println("tab                     = %x5C.74 ; t    tab             U+0009");
        out.println("ascii                   = %x20-21 / %x23-5B / %x5D-7F ; all but \\ and \"");
        out.println("utf16                   = utf16-2 / utf16-1");
        out.println(
                "utf16-2                 = %x5C.75 4HEXDIG %x5C.75 4HEXDIG ; surrogate pairs are evaluated semantically");
        out.println("utf16-1                 = %x5C.75 utf16-tail");
        out.println(
                "utf16-tail              = 4HEXDIG ; error production for semantically detecting \\uXXXX formatting errors ");
        out.println("utf8                    = utf8-2 / utf8-3 / utf8-4 ; decoding utf8 is done semantically");
        out.println("utf8-2                  = %xC2-DF UTF8-tail");
        out.println("utf8-3                  = %xE0 %xA0-BF UTF8-tail");
        out.println("                        / %xE1-EC 2( UTF8-tail )");
        out.println("                        / %xED %x80-9F UTF8-tail");
        out.println("                        / %xEE 2( UTF8-tail )");
        out.println("                        / %xEF UTF8-tail %x80-BD");
        out.println("utf8-4                  = %xF0 %x90-BF 2( UTF8-tail )");
        out.println("                        / %xF1-F3 3( UTF8-tail )");
        out.println("                        / %xF4 %x80-8F 2( UTF8-tail )                  ");
        out.println("UTF8-tail               = %x80-BF");
        out.println("DIGIT                   = %d48-57");
        out.println("HEXDIG                  = %d48-57 / %d65-70 / %d97-102");
    }
}
