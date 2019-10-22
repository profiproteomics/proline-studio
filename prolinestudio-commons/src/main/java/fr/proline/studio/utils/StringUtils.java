/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * utils for String
 *
 * @author MB243701
 */
public class StringUtils {

    /**
     * truncates the given text with nbCharac, starting from end
     *
     * @param text
     * @param nbCharac
     * @return
     */
    public final static String truncate(String text, int nbCharac) {
        if (text == null) {
            return null;
        }
        int textSize = text.length();
        if (nbCharac > textSize) {
            return text;
        }
        return text.substring(textSize - nbCharac);
    }

    /* returns the pixel-lenght of a string */
    public static int lenghtOfString(String s, FontMetrics fm) {
        if (fm == null || s == null) {
            return 0;
        } else {
            return fm.stringWidth(s);
        }
    }

    public static List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<>();
        Pattern p = Pattern.compile("\\b.{1," + (lineSize - 1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);
        while (m.find()) {
            System.out.println(m.group().trim());   // Debug
            res.add(m.group());
        }
        return res;
    }

    public static String formatString(String msg, int lineSize) {
        Pattern p = Pattern.compile(".{1," + (lineSize - 1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
            builder.append(m.group());
        }
        return builder.toString();
    }

    /**
     * compile a Pattern with regex
     *
     * @param text
     * @return
     */
    public static Pattern compileRegex(String text) {
        String escapedText = "^" + escapeRegex(text) + "$";
        String wildcardsFilter = escapedText.replaceAll("\\*", ".*").replaceAll("\\?", ".");
        return Pattern.compile(wildcardsFilter, Pattern.CASE_INSENSITIVE);
    }

    private static String escapeRegex(String s) {
        if (s == null) {
            return "";
        }
        int len = s.length();
        if (len == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ("[](){}.+$^|#\\".indexOf(c) != -1) {
                sb.append("\\");
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * parse a String which has html tag, return a plain text String. <br>
     * if IOException, return the input string
     *
     * @param html
     * @return
     */
    public static String extractTextFromHtml(String html) {
        try {
            //first, treat plain Text to html, in order to reserve the end of line
            String htmlTag = "<html>";
            String htmlEndTag = "</html>";
            String[] lines = html.split("\n");
            StringBuffer sb = new StringBuffer();
            for (String s : lines) {
                sb.append(htmlTag).append(s).append(htmlEndTag);
            }
            //then parse
            Reader reader = new StringReader(sb.toString());
            StringBuilder resultText = new StringBuilder();

            ParserDelegator parserDelegator = new ParserDelegator();
            HTMLEditorKit.ParserCallback parserCallback;
            parserCallback = new HTMLEditorKit.ParserCallback() {
                @Override
                public void handleText(final char[] data, final int pos) {
                    resultText.append(data);
                }

                @Override
                public void handleEndTag(HTML.Tag t, int pos) {
                    if (t.equals(HTML.Tag.HTML)) {
                        resultText.append("\n");
                    }
                }
            };
            parserDelegator.parse(reader, parserCallback, true);
            return resultText.toString().trim();
        } catch (IOException ex) {
            return html;
        }
    }

    public static String getTimeInMinutes(float seconds, int nbDigit) {
        double min = seconds / 60;

        return DataFormat.format(min, nbDigit);
    }

    public static String getTimeAsMinSecText(long seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;

        return String.format("%d min %d sec", min, sec);
    }

    private static int STEP_LIMIT = 1000;

    public static DefaultMutableTreeNode createTreeFromJson(String parameter,String rootName) {
        Gson gson = new Gson();
        LinkedTreeMap paramMap = new LinkedTreeMap();
        DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(rootName);
        try {
            paramMap = gson.fromJson(parameter, paramMap.getClass());
            createParameterTree(paramMap, parentNode, 0);
        } catch (JsonSyntaxException jex) {
            //nothing todo
        } catch (Exception ex) {
            //don't treat
        }
        return parentNode;
    }

    private static int createParameterTree(LinkedTreeMap params, DefaultMutableTreeNode parent, int childIndex) {

        if (childIndex > STEP_LIMIT) {
            return childIndex;
        }
        if (params == null) {
            return childIndex;
        }
        int index = childIndex;
        DefaultMutableTreeNode root = parent;
        DefaultMutableTreeNode child;
        for (Object key : params.keySet()) {
            child = new DefaultMutableTreeNode(key);
            root.add(child);
            Object value = params.get(key);
            if (value instanceof LinkedTreeMap) {
                index = createParameterTree((LinkedTreeMap) value, child, index++);
            } else {
                if (value instanceof ArrayList) {
                    ArrayList valueList = (ArrayList) value;
                    if (valueList.get(0) instanceof LinkedTreeMap) {
                        for (Object item : valueList) {
                            index = createParameterTree((LinkedTreeMap) item, child, index++);
                        }
                    } else {
                        child.add(new DefaultMutableTreeNode(value));
                    }
                } else {
                    child.add(new DefaultMutableTreeNode(value));
                }
            }

        }
        return index;
    }
}
