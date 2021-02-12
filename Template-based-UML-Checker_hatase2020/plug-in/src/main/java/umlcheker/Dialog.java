package umlcheker;

import javax.swing.JOptionPane;
public class Dialog{
    public static void main(String[] args){

    }

    public void warningdialog(String mes){
        JOptionPane.showMessageDialog(null, mes , "Warning", JOptionPane.WARNING_MESSAGE);
    }
}