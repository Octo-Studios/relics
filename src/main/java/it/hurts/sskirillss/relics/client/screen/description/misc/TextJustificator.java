package it.hurts.sskirillss.relics.client.screen.description.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class TextJustificator {
    private static final Pattern SPACE_RE = Pattern.compile("\\s+");
    private static final Pattern PLACEHOLDER_RE = Pattern.compile("([()%]*%(\\d+)\\$s[()%]*)");

    private static final class Token {
        final String raw;
        final Style style;
        final boolean isSpace;

        Token(String raw, Style style, boolean isSpace) {
            this.raw = raw;
            this.style = style;
            this.isSpace = isSpace;
        }

        FormattedText asFT() {
            return FormattedText.of(raw, style);
        }
    }

    public static final class Word {
        public final MutableComponent component;
        public final boolean isDynamic;

        public Word(MutableComponent component, boolean isDynamic) {
            this.component = component;
            this.isDynamic = isDynamic;
        }

        public boolean isSpace() {
            return component.getString().trim().isEmpty();
        }
    }

    public static final class LayoutLine {
        private final List<Word> words;
        private final boolean justify;

        public LayoutLine(List<Word> words, boolean justify) {
            this.words = words;
            this.justify = justify;
        }

        public List<Word> words() {
            return words;
        }

        public boolean justify() {
            return justify;
        }
    }

    public static final class LineEntry {
        private MutableComponent rawLine;
        private final boolean justify;

        public LineEntry(MutableComponent rawLine, boolean justify) {
            this.rawLine = rawLine;
            this.justify = justify;
        }

        public MutableComponent getRawLine() {
            return rawLine;
        }

        public void setRawLine(MutableComponent rawLine) {
            this.rawLine = rawLine;
        }

        public boolean isJustify() {
            return justify;
        }
    }

    public static List<FormattedCharSequence> justifyStyledText(Component text, int maxWidth) {
        var font = Minecraft.getInstance().font;
        var splitter = font.getSplitter();

        var tokens = new ArrayList<Token>();

        text.visit((style, str) -> {
            var m = SPACE_RE.matcher(str);

            var last = 0;

            while (m.find()) {
                if (m.start() > last)
                    tokens.add(new Token(str.substring(last, m.start()), style, false));

                tokens.add(new Token(m.group(), style, true));

                last = m.end();
            }

            if (last < str.length())
                tokens.add(new Token(str.substring(last), style, false));

            return Optional.empty();
        }, Style.EMPTY);

        var result = new ArrayList<FormattedCharSequence>();
        var line = new ArrayList<Token>();

        var lineWidth = 0f;

        var i = 0;

        while (i < tokens.size()) {
            var tok = tokens.get(i);
            var tokW = splitter.stringWidth(tok.asFT());

            if (!tok.isSpace && tokW > maxWidth) {
                var cut = fitPrefixByWidth(splitter, tok.raw, tok.style, maxWidth - (line.isEmpty() ? 0f : lineWidth));

                if (cut == 0)
                    cut = firstCodePointEnd(tok.raw, 0);

                var left = tok.raw.substring(0, cut);
                var right = tok.raw.substring(cut);
                var leftTok = new Token(left, tok.style, false);
                var leftW = splitter.stringWidth(leftTok.asFT());

                if (lineWidth + leftW > maxWidth && !line.isEmpty()) {
                    emitLine(result, line, maxWidth, splitter, font, true);

                    line.clear();
                    lineWidth = 0f;

                    continue;
                }

                line.add(leftTok);
                lineWidth += leftW;

                tokens.set(i, new Token(right, tok.style, false));

                continue;
            }

            if (lineWidth + tokW > maxWidth && !line.isEmpty()) {
                emitLine(result, line, maxWidth, splitter, font, true);

                line.clear();

                lineWidth = 0f;

                continue;
            }

            if (tok.isSpace && line.isEmpty()) {
                tok = new Token(" ", tok.style, true);
                tokW = splitter.stringWidth(tok.asFT());

                tokens.set(i, tok);
            }

            line.add(tok);
            lineWidth += tokW;

            i++;
        }

        if (!line.isEmpty())
            emitLine(result, line, maxWidth, splitter, font, false);

        return result;
    }

    private static void emitLine(List<FormattedCharSequence> out, List<Token> line, int maxWidth, StringSplitter splitter, Font font, boolean justify) {
        if (!justify) {
            out.add(Language.getInstance().getVisualOrder(FormattedText.composite(line.stream().map(Token::asFT).toList())));

            return;
        }

        var spaceTokens = 0;
        var width = 0f;

        for (var t : line) {
            width += splitter.stringWidth(t.asFT());

            if (t.isSpace)
                spaceTokens++;
        }

        if (spaceTokens == 0) {
            out.add(Language.getInstance().getVisualOrder(FormattedText.composite(line.stream().map(Token::asFT).toList())));

            return;
        }

        var missing = maxWidth - width;

        if (missing <= 0f) {
            out.add(Language.getInstance().getVisualOrder(FormattedText.composite(line.stream().map(Token::asFT).toList())));

            return;
        }

        var spaceW = font.width(" ");
        var extraSpacesTotal = (int) Math.floor(missing / spaceW);

        if (extraSpacesTotal <= 0) {
            out.add(Language.getInstance().getVisualOrder(FormattedText.composite(line.stream().map(Token::asFT).toList())));

            return;
        }

        var base = extraSpacesTotal / spaceTokens;
        var rem = extraSpacesTotal % spaceTokens;

        var parts = new ArrayList<FormattedText>(line.size() * 2);

        var seenSpaces = 0;

        for (var t : line) {
            if (!t.isSpace) {
                parts.add(t.asFT());
            } else {
                var add = base + (seenSpaces < rem ? 1 : 0);

                seenSpaces++;

                var s = t.raw + " ".repeat(add);

                parts.add(FormattedText.of(s, t.style));
            }
        }

        out.add(Language.getInstance().getVisualOrder(FormattedText.composite(parts)));
    }

    public static List<LayoutLine> layoutJustifiedLines(Font font, List<LineEntry> rawLineEntries, List<MutableComponent> dynamicComponents, int maximumLineWidth) {
        var splitter = font.getSplitter();
        var spaceWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(" ", Style.EMPTY)));
        var result = new ArrayList<LayoutLine>();

        for (var entry : rawLineEntries) {
            var rawText = entry.getRawLine().getString();

            if (rawText.isEmpty()) {
                result.add(new LayoutLine(List.of(), false));

                continue;
            }

            var words = new ArrayList<Word>();
            var matcher = PLACEHOLDER_RE.matcher(rawText);
            var baseStyle = entry.getRawLine().getStyle();

            var lastEnd = 0;

            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    var before = rawText.substring(lastEnd, matcher.start());

                    tokenizeLiteral(before, baseStyle, words);
                }

                var segment = matcher.group(1);
                var index = Integer.parseInt(matcher.group(2)) - 1;
                var dyn = dynamicComponents.get(index).copy();
                var style = dyn.getStyle();
                var pre = segment.substring(0, segment.indexOf('%'));
                var suf = segment.substring(segment.lastIndexOf('s') + 1);

                if (!pre.isEmpty())
                    dyn = Component.literal(pre).withStyle(style).append(dyn);

                if (!suf.isEmpty())
                    dyn = dyn.append(Component.literal(suf).withStyle(style));

                words.add(new Word(dyn, true));

                lastEnd = matcher.end();
            }

            if (lastEnd < rawText.length()) {
                var after = rawText.substring(lastEnd);

                tokenizeLiteral(after, baseStyle, words);
            }

            var lines = new ArrayList<List<Word>>();
            var current = new ArrayList<Word>();

            var used = 0;

            var i = 0;

            while (i < words.size()) {
                var w = words.get(i);
                var wStr = w.component.getString();
                var wWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(wStr, w.component.getStyle()))) + (w.isDynamic ? 4 : 0);

                if (!w.isSpace() && wWidth > maximumLineWidth) {
                    var cut = fitPrefixByWidth(splitter, wStr, w.component.getStyle(), maximumLineWidth - (current.isEmpty() ? 0 : used));

                    if (cut == 0)
                        cut = firstCodePointEnd(wStr, 0);

                    var left = Component.literal(wStr.substring(0, cut)).withStyle(w.component.getStyle());
                    var right = Component.literal(wStr.substring(cut)).withStyle(w.component.getStyle());

                    var leftWord = new Word(left.copy(), false);
                    var leftWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(leftWord.component.getString(), leftWord.component.getStyle())));

                    if (!current.isEmpty() && used + leftWidth > maximumLineWidth) {
                        dropTrailingSpaces(current);

                        lines.add(current);

                        current = new ArrayList<>();
                        used = 0;

                        continue;
                    }

                    current.add(leftWord);
                    used += leftWidth;
                    words.set(i, new Word(right.copy(), false));

                    continue;
                }

                if (!current.isEmpty() && used + wWidth > maximumLineWidth) {
                    dropTrailingSpaces(current);

                    lines.add(current);

                    current = new ArrayList<>();
                    used = 0;

                    continue;
                }

                if (w.isSpace() && current.isEmpty()) {
                    w = new Word(Component.literal(" ").withStyle(w.component.getStyle()), false);

                    wWidth = spaceWidth;

                    words.set(i, w);
                }

                current.add(w);

                used += wWidth;
                i++;
            }

            if (!current.isEmpty()) {
                dropTrailingSpaces(current);

                lines.add(current);
            }

            for (var li = 0; li < lines.size(); li++) {
                var justify = entry.isJustify() && li < lines.size() - 1;

                result.add(new LayoutLine(lines.get(li), justify));
            }
        }

        return result;
    }

    public static void renderJustifiedDescriptionWithStatBoxes(GuiGraphics graphics, int startX, int startY, int maximumLineWidth, Font font, List<LayoutLine> layoutLines) {
        var splitter = font.getSplitter();
        var spaceWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(" ", Style.EMPTY)));

        var y = startY;
        var lineHeight = font.lineHeight;

        for (var line : layoutLines) {
            var words = line.words();

            if (words.isEmpty()) {
                y += lineHeight + 1;

                continue;
            }

            var justify = line.justify();
            var totalW = 0;
            var spaceSlots = 0;

            for (var w : words) {
                if (w.isSpace()) {
                    totalW += spaceWidth;

                    spaceSlots++;
                } else {
                    var width = (int) Math.ceil(splitter.stringWidth(w.component)) + (w.isDynamic ? 4 : 0);

                    totalW += width;
                }
            }

            var x = startX;

            if (!justify || spaceSlots == 0 || totalW >= maximumLineWidth) {
                for (var w : words) {
                    if (w.isSpace()) {
                        x += spaceWidth;

                        continue;
                    }

                    var textWidth = (int) Math.ceil(splitter.stringWidth(w.component));
                    var style = w.component.getStyle();
                    var color = style.getColor() != null ? style.getColor().getValue() : DescriptionUtils.TEXT_COLOR;

                    if (w.isDynamic) {
                        graphics.fill(x - 1, y - 1, x + textWidth + 4, y, 0xFF000000 | color);
                        graphics.fill(x - 1, y + lineHeight, x + textWidth + 4, y + lineHeight + 1, 0xFF000000 | color);
                        graphics.fill(x - 1, y - 1, x, y + lineHeight + 1, 0xFF000000 | color);
                        graphics.fill(x + textWidth + 3, y - 1, x + textWidth + 4, y + lineHeight + 1, 0xFF000000 | color);

                        graphics.drawString(font, Language.getInstance().getVisualOrder(w.component), x + 2, y + 1, color, false);

                        x += textWidth + 5;
                    } else {
                        graphics.drawString(font, Language.getInstance().getVisualOrder(w.component), x, y, color, false);

                        x += textWidth;
                    }
                }
            } else {
                var missing = maximumLineWidth - totalW;
                var extraSpacesTotal = Math.max(0, missing / spaceWidth);
                var base = extraSpacesTotal / spaceSlots;
                var rem = extraSpacesTotal % spaceSlots;

                var seen = 0;

                for (var i = 0; i < words.size(); i++) {
                    var w = words.get(i);

                    if (w.isSpace()) {
                        var add = base + (seen < rem ? 1 : 0);

                        seen++;

                        x += spaceWidth + add * spaceWidth;

                        continue;
                    }

                    var textWidth = (int) Math.ceil(splitter.stringWidth(w.component));
                    var style = w.component.getStyle();
                    var color = style.getColor() != null ? style.getColor().getValue() : DescriptionUtils.TEXT_COLOR;

                    if (w.isDynamic) {
                        graphics.fill(x - 1, y - 1, x + textWidth + 4, y, 0xFF000000 | color);
                        graphics.fill(x - 1, y + lineHeight, x + textWidth + 4, y + lineHeight + 1, 0xFF000000 | color);
                        graphics.fill(x - 1, y - 1, x, y + lineHeight + 1, 0xFF000000 | color);
                        graphics.fill(x + textWidth + 3, y - 1, x + textWidth + 4, y + lineHeight + 1, 0xFF000000 | color);

                        graphics.drawString(font, Language.getInstance().getVisualOrder(w.component), x + 2, y + 1, color, false);

                        x += textWidth + 5;
                    } else {
                        graphics.drawString(font, Language.getInstance().getVisualOrder(w.component), x, y, color, false);

                        x += textWidth;
                    }
                }
            }

            y += lineHeight + 1;
        }
    }

    private static void tokenizeLiteral(String s, Style style, List<Word> out) {
        var m = SPACE_RE.matcher(s);

        var last = 0;

        while (m.find()) {
            if (m.start() > last)
                out.add(new Word(Component.literal(s.substring(last, m.start())).withStyle(style), false));

            out.add(new Word(Component.literal(m.group()).withStyle(style), false));

            last = m.end();
        }

        if (last < s.length())
            out.add(new Word(Component.literal(s.substring(last)).withStyle(style), false));
    }

    private static void dropTrailingSpaces(List<Word> line) {
        while (!line.isEmpty() && line.getLast().isSpace()) line.removeLast();
    }

    private static int fitPrefixByWidth(StringSplitter splitter, String s, Style style, float width) {
        if (width <= 0f || s.isEmpty())
            return 0;

        var i = 0;
        var lastGood = 0;

        while (i < s.length()) {
            var next = firstCodePointEnd(s, i);
            var w = splitter.stringWidth(FormattedText.of(s.substring(0, next), style));

            if (w > width)
                break;

            lastGood = next;
            i = next;
        }

        return lastGood;
    }

    private static int firstCodePointEnd(String s, int start) {
        return s.offsetByCodePoints(start, 1);
    }
}