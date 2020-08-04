package sci.automaticdownfiscalnotes.Model;

import JExcel.JExcel;
import SimpleDotEnv.Env;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sci.automaticdownfiscalnotes.Model.Entities.Down;

public class DownFileModel {
    private File file;
    private XSSFWorkbook wk;
    private XSSFSheet sheet;
    
    private List<Down> downs =  new ArrayList<>();

    public DownFileModel(File file) {
        this.file = file;
    }

    public void setDowns() {
        try {
            wk = new XSSFWorkbook(file);
            sheet = wk.getSheetAt(0);
            
            passSheet();
            
            wk.close();
        } catch (Exception e) {
            throw new Error("Erro ao tentar abrir o arquivo: " + file.getAbsolutePath());
        }        
    }
    
    public void passSheet(){
        String filter=  Env.get("fileRowFilter");
        
        //Percorre todas as linhas
        for (Iterator<Row> rowIterator = sheet.iterator(); rowIterator.hasNext();) {
            Row row = rowIterator.next();
            
            String colFilter = "A";
            String colDate = "D";
            String colDocument = "E";
            String colValue = "L";
            
            String filterString = JExcel.getStringCell(row.getCell(JExcel.Cell(colFilter)));
            String dateString = JExcel.getStringCell(row.getCell(JExcel.Cell(colDate)));
            String documentString = JExcel.getStringCell(row.getCell(JExcel.Cell(colDocument)));
            String valueString = JExcel.getStringCell(row.getCell(JExcel.Cell(colValue))).replaceAll("[^0-9\\-.]", "");
            
            //Arruma valor
            valueString = valueString.equals("")?"0":valueString;
            
            if(filterString.equals(filter)){
                System.out.println(dateString + " - " + documentString + " - " + valueString);
            }
        }
    }
}
