package io.github.nnkwrik.astah.util;

/**
 * networkの色を得るためのutil
 *
 * @author Reika Nonokawa
 */
public class ColorUtil {
    private static String[] distinctColors = new String[]{
            "#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231",
            "#911eb4", "#46f0f0", "#f032e6", "#bcf60c", "#fabebe",
            "#008080", "#e6beff", "#9a6324", "#fffac8", "#800000",
            "#aaffc3", "#808000", "#ffd8b1", "#000075", "#808080"};
    private static int index = -1;

    public static String nextColor() {
        index = ++index % distinctColors.length;
        return distinctColors[index];
    }

    public static void resetColor() {
        index = -1;
    }
}
