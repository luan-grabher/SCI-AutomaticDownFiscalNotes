package sci.automaticdownfiscalnotes.Model;

import JExcel.JExcel;
import SimpleDotEnv.Env;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import sci.automaticdownfiscalnotes.Model.Entities.Down;

public class DownFileModel {

    private File file;
    private HSSFWorkbook wk;
    private HSSFSheet sheet;

    private List<Down> downs = new ArrayList<>();

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setDowns() {
        try {
            wk = new HSSFWorkbook(new FileInputStream(file));
            sheet = wk.getSheetAt(0);

            passSheet();

            wk.close();
        } catch (Exception e) {
            throw new Error("Erro ao tentar abrir o arquivo: " + file.getAbsolutePath());
        }
    }

    public void passSheet() {
        String filter = Env.get("fileRowFilter");

        //Percorre todas as linhas
        for (Iterator<Row> rowIterator = sheet.iterator(); rowIterator.hasNext();) {
            Row row = rowIterator.next();

            try {
                String colFilter = "A";
                String colDate = "D";
                String colDocument = "E";
                String colValue = "L";

                //Verifica o filtro na primeira coluna
                Cell filterCell = row.getCell(JExcel.Cell(colFilter));
                if (filterCell != null && filterCell.toString().equals(filter)) {

                    //Pega data e verifica
                    Cell dateCell = row.getCell(JExcel.Cell(colDate));
                    if (dateCell != null && JExcel.isDateCell(dateCell)) {
                        Calendar date = Calendar.getInstance();
                        date.setTime(dateCell.getDateCellValue());

                        //Se a celula de documento for numerica
                        Cell documentCell = row.getCell(JExcel.Cell(colDocument));
                        if (documentCell != null && documentCell.getCellType() == CellType.NUMERIC) {
                            String document = new BigDecimal(documentCell.getNumericCellValue()).toPlainString();

                            //Somente documentos com numeros grandes
                            if (document.length() > 10) {

                                Cell valueCell = row.getCell(JExcel.Cell(colValue));
                                if (valueCell != null && valueCell.getCellType() == CellType.NUMERIC) {
                                    BigDecimal value = new BigDecimal(valueCell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);

                                    Down down = new Down();
                                    down.setDate(date);
                                    down.setDocument(document);
                                    down.setValue(value);

                                    downs.add(down);
                                    System.out.println(date.getTime() + " - " + document + " - " + value.toString());
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro interno na linha " + row.getRowNum());
            }
        }
    }

    public List<Down> getDowns() {
        return downs;
    }

}
