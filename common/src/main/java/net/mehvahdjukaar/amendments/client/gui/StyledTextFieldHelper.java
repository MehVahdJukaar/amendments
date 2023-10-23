package net.mehvahdjukaar.amendments.client.gui;

import net.minecraft.client.gui.font.TextFieldHelper;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StyledTextFieldHelper extends TextFieldHelper {
    private final Supplier<String> getMessageFn;

    public StyledTextFieldHelper(Supplier<String> supplier, Consumer<String> consumer, Supplier<String> supplier2, Consumer<String> consumer2, Predicate<String> predicate) {
        super(supplier, consumer, supplier2, consumer2, predicate);
        this.getMessageFn = supplier;
    }

    @Override
    public void removeFromCursor(int i, CursorStep cursorStep) {
        super.removeFromCursor(i, cursorStep);
        String msg = this.getMessageFn.get();
        if (i < 0) {
            int inc = -2;
            int p = getCursorPos() + inc;
            if (p > 0 && msg.charAt(p) == '§') {
                removeFromCursor(inc, CursorStep.CHARACTER);
            }
        } else {
            int inc = 2;
            int p = getCursorPos()-1 + inc - 1;
            if (p < msg.length() - 1 && msg.charAt(p) == '§') {
                removeFromCursor(inc, CursorStep.CHARACTER);
            }
        }
    }

    @Override
    public void moveBy(int direction, boolean keepSelection, CursorStep cursorStep) {
        super.moveBy(direction, keepSelection, cursorStep);
        String msg = this.getMessageFn.get();
        if (direction < 0) {
            int inc = -2;
            int p = getCursorPos() + inc;
            if (p > 0 && msg.charAt(p) == '§') {
                moveBy(inc, keepSelection, CursorStep.CHARACTER);
            }
        } else {
            int inc = 2;
            int p = getCursorPos()-1 + inc - 1;
            if (p < msg.length() - 1 && msg.charAt(p) == '§') {
                moveBy(inc, keepSelection, CursorStep.CHARACTER);
            }
        }
    }


}
