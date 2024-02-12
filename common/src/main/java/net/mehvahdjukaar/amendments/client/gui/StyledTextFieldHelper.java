package net.mehvahdjukaar.amendments.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.font.TextFieldHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StyledTextFieldHelper extends TextFieldHelper {
    private static final char TOKEN =  ChatFormatting.PREFIX_CODE;
    private final Supplier<String> getMessageFn;

    public StyledTextFieldHelper(Supplier<String> supplier, Consumer<String> consumer, Supplier<String> supplier2, Consumer<String> consumer2, Predicate<String> predicate) {
        super(supplier, consumer, supplier2, consumer2, predicate);
        this.getMessageFn = supplier;
    }

    @Override
    public void removeFromCursor(int i, CursorStep cursorStep) {
        String msg = this.getMessageFn.get();
        int cursorPos = getCursorPos();
        boolean hasTokenAtCursor = cursorPos < msg.length() && msg.charAt(cursorPos) == TOKEN;
        if (i < 0) {
            int k = getIndexBeforeToken(i, msg, cursorPos);
            if (cursorPos == msg.length() || hasTokenAtCursor) {
                i = k;
            }
            super.removeFromCursor(i, cursorStep);
            if (k != i) {
                moveBy(k + 1, false, cursorStep);
            }
        } else {
            if (hasTokenAtCursor) {
                moveBy(i, false, CursorStep.CHARACTER);
                this.removeFromCursor(-1, cursorStep);
            }
            else super.removeFromCursor(i,cursorStep);
        }
    }

    private static int getIndexBeforeToken(int i, String msg, int cursorPos) {
        int p = cursorPos - 3;
        if (p >= 0 && msg.charAt(p) == TOKEN) {
            i = -3;
            int p1 = cursorPos - 5;
            if (p1 >= 0 && msg.charAt(p1) == TOKEN) {
                i = -5;
            }
        }
        return i;
    }

    @Override
    public void moveBy(int i, boolean keepSelection, CursorStep cursorStep) {
        String msg = this.getMessageFn.get();
        int cursorPos = getCursorPos();
        if (i < 0) {
            i = getIndexBeforeToken(i, msg, cursorPos);
            super.moveBy(i, keepSelection, cursorStep);
        } else {
            if (cursorPos < msg.length() && msg.charAt(cursorPos) == TOKEN) {
                i = 3;
                int p = cursorPos + 2;
                if (p < msg.length() && msg.charAt(p) == TOKEN) {
                    i = 5;
                }
            }
            super.moveBy(i, keepSelection, CursorStep.CHARACTER);
        }
    }

    public void insertStyledText(String text, ChatFormatting color, ChatFormatting style) {
        String currentMod = getModifier(color, style);
        String lastMod = getPreviousModifier();
        if (!Objects.equals(currentMod, lastMod) ) {
            String s = currentMod + text;
            this.insertText(s);
            int j = this.getCursorPos();
            if (this.getCursorPos() != this.getMessageFn.get().length() && lastMod != null) {
                this.insertText(lastMod);
                super.setCursorPos(j, false);
            }
        } else this.insertText(text);
    }

    @Override
    public void setCursorPos(int textIndex, boolean keepSelection) {
        //validate
        String text = getMessageFn.get();
       // if ( (textIndex>0 && text.charAt(textIndex-1) == TOKEN) || (textIndex>1 && text.charAt(textIndex -2) == TOKEN)) {
         //   setCursorPos(textIndex - 1, keepSelection);
        //}
        //else
        super.setCursorPos(textIndex, keepSelection);
    }

    private String getModifier(ChatFormatting color, ChatFormatting style) {
        String s = color.toString();
        if (style != ChatFormatting.RESET) {
            s += style.toString();
        }
        return s.replace(ChatFormatting.PREFIX_CODE, TOKEN);
    }

    @Nullable
    private String getPreviousModifier() {
        String text = this.getMessageFn.get();
        int cursorPos = this.getCursorPos() - 1;
        for (int i = cursorPos; i >= 0 && i < text.length(); i--) {
            if (text.charAt(i) == TOKEN) {
                int start = i;
                int end = i + 2;
                if (i >= 2 && text.charAt(i - 2) == TOKEN) {
                    start -= 2;
                }
                if (end <= text.length()) return text.substring(start, end);
            }
        }
        return null; // Special character not found in the specified range
    }


    public void formatSelected(@Nullable ChatFormatting ink,@Nullable ChatFormatting quill) {
        // get selected text
        // remove all formatting
        // place new formatting at the beginning
    }
}
