package bit;

import bit.memory.CompilerRegister;

public class Utils {
    public static CompilerRegister registerBySize(WordSize size, int register) {
        switch (size) {
            case BYTE:
                if (register == 0)
                    return CompilerRegister.al;

                if (register == 1)
                    return CompilerRegister.bl;

                if (register == 2)
                    return CompilerRegister.cl;

                if (register == 3)
                    return CompilerRegister.dl;

                if (register == 4)
                    return CompilerRegister.sil;

                if (register == 5)
                    return CompilerRegister.dil;

                throw new IllegalArgumentException(String.format("Illegal register value: %d", register));

            case WORD:
                if (register == 0)
                    return CompilerRegister.ax;

                if (register == 1)
                    return CompilerRegister.bx;

                if (register == 2)
                    return CompilerRegister.cx;

                if (register == 3)
                    return CompilerRegister.dx;

                if (register == 4)
                    return CompilerRegister.si;

                if (register == 5)
                    return CompilerRegister.di;

                throw new IllegalArgumentException("Illegal register value");

            case DOUBLE_WORD:
                if (register == 0)
                    return CompilerRegister.eax;

                if (register == 1)
                    return CompilerRegister.ebx;

                if (register == 2)
                    return CompilerRegister.ecx;

                if (register == 3)
                    return CompilerRegister.edx;

                if (register == 4)
                    return CompilerRegister.esi;

                if (register == 5)
                    return CompilerRegister.edi;

                throw new IllegalArgumentException("Illegal register value");

            case QUAD_WORD:
                if (register == 0)
                    return CompilerRegister.rax;

                if (register == 1)
                    return CompilerRegister.rbx;

                if (register == 2)
                    return CompilerRegister.rcx;

                if (register == 3)
                    return CompilerRegister.rdx;

                if (register == 4)
                    return CompilerRegister.rsi;

                if (register == 5)
                    return CompilerRegister.rdi;

                throw new IllegalArgumentException("Illegal register value");

            default:
                throw new IllegalArgumentException("Illegal size");
        }
    }

    public static String wordSizeToDataSize(WordSize size) {
        switch (size) {
            case BYTE:
                return "db";
            case WORD:
                return "dw";
            case DOUBLE_WORD:
                return "dd";
            case QUAD_WORD:
                return "dq";
        }

        throw new IllegalArgumentException("Unknown size type");
    }

    public static String wordSizeToGeneral(WordSize size) {
        switch (size) {
            case BYTE:
                return "BYTE";
            case WORD:
                return "WORD";
            case DOUBLE_WORD:
                return "DWORD";
            case QUAD_WORD:
                return "QWORD";
        }

        throw new IllegalArgumentException("Unknown size type");
    }

    public static String intent(int spaces, String str) {
        boolean print = true;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (print && c != '\n') {
                print = false;

                for (int i1 = 0; i1 < spaces; i1++) {
                    builder.append(" ");
                }
            }

            if (c == '\n') {
                print = true;
            }

            builder.append(c);
        }

        return builder.toString();
    }

    public static WordSize bitTypeToSize(BitType type) {
        switch (type) {
            case BOOL:
                return WordSize.BYTE;

            case INT:
                return WordSize.DOUBLE_WORD;

            case STR:
                return WordSize.QUAD_WORD;

            default:
                throw new IllegalArgumentException("Illegal type value");
        }
    }

    public static int wordSizeToBytes(WordSize size) {
        switch (size) {
            case BYTE:
                return 1;
            case WORD:
                return 2;
            case DOUBLE_WORD:
                return 4;
            case QUAD_WORD:
                return 8;
        }

        throw new IllegalArgumentException("Unknown size type");
    }
}
