package sci.automaticdownfiscalnotes.Model;

import Dates.Dates;
import JExcel.JExcel;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
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
import static sci.automaticdownfiscalnotes.SCIAutomaticDownFiscalNotes.ini;

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

            try {
                passSheet();
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error("Erro inexperado ao extrair informações do arquivo");
            }

            wk.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Erro ao tentar abrir o arquivo: " + file.getAbsolutePath());
        }
    }

    public void passSheet() {
        String filter = ini.get("Config", "fileRowFilter").toLowerCase();
        String colFilter = ini.get("Colunas", "filial");
        String colDate = ini.get("Colunas", "data");
        String colDocument = ini.get("Colunas", "nota");
        String colValue = ini.get("Colunas", "valor");

        if (colFilter != null && colDate != null && colDocument != null && colValue != null) {
            if ( //Se as colunas estiverem corretas
                    strCell(0, colFilter).toLowerCase().contains("filial")
                    && strCell(0, colDate).toLowerCase().contains("data")
                    && strCell(0, colDocument).toLowerCase().contains("nota")
                    && strCell(0, colValue).toLowerCase().contains("valor")) {
                //Percorre todas as linhas
                for (Row row : sheet) {
                    try {
                        //Verifica o filtro na primeira coluna
                        Cell filterCell = row.getCell(JExcel.Cell(colFilter));
                        if (filterCell != null && filterCell.toString().toLowerCase().contains(filter)) {
                            //Pega data e verifica
                            Cell dateCell = row.getCell(JExcel.Cell(colDate));
                            if (dateCell != null && JExcel.isDateCell(dateCell)) {
                                Calendar date = Calendar.getInstance();
                                if(dateCell.getCellType() == CellType.STRING){
                                    date = Dates.getCalendarFromFormat(dateCell.getStringCellValue(), "dd/MM/yyyy");
                                }else{
                                    date.setTime(dateCell.getDateCellValue());
                                }

                                //Se a celula de documento for numerica
                                Cell documentCell = row.getCell(JExcel.Cell(colDocument));
                                if (documentCell != null && documentCell.getCellType() == CellType.NUMERIC) {
                                    String document = new BigDecimal(documentCell.getNumericCellValue()).toPlainString();

                                    //Somente documentos com numeros grandes
                                    if (document.length() > 10) {

                                        Cell valueCell = row.getCell(JExcel.Cell(colValue));
                                        if (valueCell != null && valueCell.getCellType() == CellType.NUMERIC) {
                                            BigDecimal value = new BigDecimal(valueCell.getNumericCellValue());

                                            Down down = new Down();
                                            down.setDate(date);
                                            down.setDocument(document);
                                            down.setValue(value);

                                            downs.add(down);
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
            } else {
                throw new Error("Este não é o arquivo correto!\n"
                        + "A primeira linha da planilha deve possuir as colunas que tenham 'filial','data','nota' e 'valor' escritos nas suas respectivas colunas.\n"
                        + "As colunas corretas são:\n"
                        + "Filial: " + colFilter + "\n"
                        + "Data: " + colDate + "\n"
                        + "Nota: " + colDocument + "\n"
                        + "Valor: " + colValue);
            }
        } else {
            throw new Error("As colunas não foram configuradas corretamente no arquivo de configuração INI.");
        }
    }

    /**
     * Retorna o valor em string da celula
     * @param row Linha
     * @param col Coluna em Letra
     * @return valor em string da celula
     */
    public String strCell(int row, String col) {
        return JExcel.getCellString(sheet.getRow(row).getCell(JExcel.Cell(col)));
    }

    public List<Down> getDowns() {
        return downs;
    }

}
