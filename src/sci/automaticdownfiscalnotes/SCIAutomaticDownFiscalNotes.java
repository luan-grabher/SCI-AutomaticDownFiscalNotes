package sci.automaticdownfiscalnotes;

import java.io.File;
import javax.swing.JOptionPane;

public class SCIAutomaticDownFiscalNotes {
    
    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, "Selecione o arquivo de Recebimentos com Retenção do plune com as baixas a serem feitas:");
        File file = Selector.Arquivo.selecionar(System.getProperty("user.home"), "XLSX", "xlsx");
        
        execute(file);
    }
    
    public static void execute(File file){
    
    }
}
