package automaticdownfiscalnotes.Model;

import Dates.Dates;
import JExcel.JExcel;
import automaticdownfiscalnotes.Model.Entities.Down;

import static automaticdownfiscalnotes.SCIAutomaticDownFiscalNotes.ini;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class DownFileModel {

    private File file;
    private Workbook wk;
    private Sheet sheet;

    private List<Down> downs = new ArrayList<>();

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Pega as Baixas do arquivo de baixas
     */
    public void setDowns() {
        try {
            //if file name ends with ".xls", use HSSFWorkbook, else use XSSFWorkbook
            if (file.getName().endsWith(".xls")) {
                wk = new HSSFWorkbook(new FileInputStream(file));
            } else {
                wk = new XSSFWorkbook(new FileInputStream(file));
            }

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

    /**
     * Pega as informações da planilha
     */
    public void passSheet() {
        String filter = ini.get("Config", "fileRowFilter").toLowerCase();
        String colFilter = ini.get("Colunas", "filial");
        String colDate = ini.get("Colunas", "data");
        String colDocument = ini.get("Colunas", "nota");
        String colValue = ini.get("Colunas", "valor");

        //get in Ini Config.documentMinSize, if not exist, set default "10"
        int documentMinSize = ini.get("Config", "documentMinSize") == null ? 10 : Integer.parseInt(ini.get("Config", "documentMinSize"));

        if (colFilter != null && colDate != null && colDocument != null && colValue != null) {
            //Percorre todas as linhas
            for (Row row : sheet) {
                try {
                    //Verifica o filtro na primeira coluna
                    Cell filterCell = row.getCell(JExcel.Cell(colFilter));
                    if (filterCell != null && filterCell.toString().toLowerCase().contains(filter)) {
                        //Pega data e verifica
                        Cell dateCell = row.getCell(JExcel.Cell(colDate));
                        if (dateCell != null && (JExcel.isDateCell(dateCell) || 
                            (dateCell.getCellType() == CellType.STRING
                            && (Dates.isDateInThisFormat(dateCell.getStringCellValue(), "dd/MM")))
                            )
                        ) {
                            Calendar date = Calendar.getInstance();
                            if(dateCell.getCellType() == CellType.STRING){
                                String dateStr = dateCell.getStringCellValue();
                                //se nao tiver ano, seta o ano atual
                                if(Dates.isDateInThisFormat(dateCell.getStringCellValue(), "dd/MM")){
                                    dateStr += "/" + Calendar.getInstance().get(Calendar.YEAR);
                                }
                                //SET DATE
                                date = Dates.getCalendarFromFormat(dateStr, "dd/MM/yyyy");
                            }else{
                                date.setTime(dateCell.getDateCellValue());
                            }

                            //Se a celula de documento for numerica
                            Cell documentCell = row.getCell(JExcel.Cell(colDocument));
                            if (documentCell != null && (documentCell.getCellType() == CellType.NUMERIC || documentCell.getCellType() == CellType.STRING)) {
                                String document = "";

                                //Se
                                if (documentCell.getCellType() == CellType.NUMERIC) {
                                    document = new BigDecimal(documentCell.getNumericCellValue()).toPlainString();
                                }//else if is string
                                else if (documentCell.getCellType() == CellType.STRING) {
                                    document = documentCell.getStringCellValue();

                                    //remove caracteres nao numericos e não "/"
                                    document = document.replaceAll("[^0-9/]", "");
                                    
                                    //se tiver mais de um "/"
                                    while (document.length() - document.replace("/", "").length() > 1) {
                                        //remove a primeira ocorrencia de "/"
                                        document = document.replaceFirst("/", "");
                                    }

                                    //enquanto tiver menos do tamanho minimo
                                    while (document.length()-1 < documentMinSize) {
                                        //substitui "/" por "/0"
                                        document = document.replace("/", "/0");
                                    }

                                    //substitui "/" por "0"
                                    document = document.replace("/", "");
                                }
                                

                                //Somente documentos com numeros grandes
                                if (document.length() >= documentMinSize) {

                                    //Se a celula de valor for numerica
                                    Cell valueCell = row.getCell(JExcel.Cell(colValue));
                                    if (valueCell != null){
                                        BigDecimal value = new BigDecimal(0);

                                        if(valueCell.getCellType() == CellType.NUMERIC) {
                                            value = new BigDecimal(valueCell.getNumericCellValue());
                                        }else if(valueCell.getCellType() == CellType.STRING){
                                            String valueStr = valueCell.getStringCellValue();
                                            //remove caracteres nao numericos . e ,
                                            valueStr = valueStr.replaceAll("[^0-9.,]", "");

                                            //se tiver "." e ",", e a posição do "." for menor que a posição do ",", remove "." e  faz replace de "," por "."
                                            if(valueStr.contains(",") && valueStr.contains(".")){
                                                if(valueStr.indexOf(".") < valueStr.indexOf(",")){
                                                    valueStr = valueStr.replace(".", "");
                                                    valueStr = valueStr.replace(",", ".");
                                                }
                                            }

                                            value = new BigDecimal(valueStr);
                                        }

                                        //Se o valor for maior que zero
                                        if (value.compareTo(BigDecimal.ZERO) > 0) {
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Erro interno na linha " + row.getRowNum());
                }
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
