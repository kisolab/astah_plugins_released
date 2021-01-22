package io.github.nnkwrik.astah.util;

import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Yaml FileChooser
 *
 * @author Reika Nonokawa
 */
public class YamlFileChooser {

    protected static JFileChooser fileChooser = createFileChooser();

    private static JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new YmlFileFilter());
        fileChooser.setCurrentDirectory(new File(SystemUtils.getUserHome().getAbsolutePath()));
        return fileChooser;
    }

    /**
     * YAMLファイルを選択してFileを得る
     * @param parent
     * @return
     */
    public static File open(Component parent) {
        File file = null;
        int option = fileChooser.showOpenDialog(parent);
        if (JFileChooser.APPROVE_OPTION == option) {
            file = fileChooser.getSelectedFile();
        }
        return file;
    }

    /**
     * textをYAMLファイルとして保存する
     * @param parent
     * @param text
     * @throws IOException
     */
    public static void save(Component parent,String text) throws IOException {
        File file = null;
        int option = fileChooser.showSaveDialog(parent);
        if (JFileChooser.APPROVE_OPTION == option) {
            file = fileChooser.getSelectedFile();
        }

        if (file == null || (file.exists() && !file.getName().toLowerCase().endsWith(".yml")))
            return;


        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());
        if (!file.exists()) {
            file = new File(file.getPath() + ".yml");
            file.createNewFile();
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * YAMLファイルのフィルター
     */
    public static class YmlFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String ext = getExtension(f);
            if (ext != null) {
                if (ext.equals("yml") || ext.equals("yaml")) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        private String getExtension(File f) {
            String ext = null;
            String filename = f.getName();
            int dotIndex = filename.lastIndexOf('.');

            if ((dotIndex > 0) && (dotIndex < filename.length() - 1)) {
                ext = filename.substring(dotIndex + 1).toLowerCase();
            }

            return ext;
        }

        @Override
        public String getDescription() {
            return "YAML,YML file";
        }
    }


}
