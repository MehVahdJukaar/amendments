package net.mehvahdjukaar.amendments.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StyledTextHelper {

    private static final char TOKEN = ChatFormatting.PREFIX_CODE;

    //MultilineTextField#insertText runs the input through StringUtil.filterText which eats the format
    //token, so we have to rebuild the whole value and put the cursor back ourselves
    public static void insertStyledText(MultilineTextField field, String text, ChatFormatting color, ChatFormatting style) {
        String wanted = modifier(color, style);
        String previous = previousModifier(field);
        boolean changesStyle = !Objects.equals(wanted, previous);
        String insert = changesStyle ? wanted + text : text;

        MultilineTextField.StringView selected = field.getSelected();
        String value = field.value();
        StringBuilder sb = new StringBuilder(value).replace(selected.beginIndex(), selected.endIndex(), insert);
        int cursor = selected.beginIndex() + insert.length();
        //typing in the middle of already styled text, put the old style back after us
        if (changesStyle && previous != null && selected.endIndex() < value.length()) {
            sb.insert(cursor, previous);
        }
        field.setValue(sb.toString());
        field.seekCursor(Whence.ABSOLUTE, cursor);
    }

    private static String modifier(ChatFormatting color, ChatFormatting style) {
        String s = color.toString();
        if (style != ChatFormatting.RESET) s += style.toString();
        return s;
    }

    @Nullable
    private static String previousModifier(MultilineTextField field) {
        String text = field.value();
        for (int i = Math.min(field.cursor(), text.length()) - 1; i >= 0; i--) {
            if (text.charAt(i) != TOKEN) continue;
            int start = i;
            int end = i + 2;
            if (i >= 2 && text.charAt(i - 2) == TOKEN) start -= 2;
            if (end <= text.length()) return text.substring(start, end);
        }
        return null;
    }
}
