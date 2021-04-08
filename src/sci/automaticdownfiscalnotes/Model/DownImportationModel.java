package sci.automaticdownfiscalnotes.Model;

import Dates.Dates;
import SimpleView.Loading;
import fileManager.FileManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sci.automaticdownfiscalnotes.Model.Entities.Down;
import static sci.automaticdownfiscalnotes.SCIAutomaticDownFiscalNotes.ini;
import sql.Database;

public class DownImportationModel {

    private List<Down> downs;
    private StringBuilder log = new StringBuilder();

    public List<Down> getDowns() {
        return downs;
    }

    public void setDowns(List<Down> downs) {
        this.downs = downs;
    }

    public void importDowns() {
        String sqlGetDocumentPortion = FileManager.getText(FileManager.getFile(".\\sql\\getDocumentPortion.sql"));
        String sqlGetFiscalEntryCFOP = FileManager.getText(FileManager.getFile(".\\sql\\getFiscalEntryCFOP.sql"));
        String sqlGetPayValue = FileManager.getText(FileManager.getFile(".\\sql\\getPayValue.sql"));
        String sqlInsertPayValue = FileManager.getText(FileManager.getFile(".\\sql\\insertPayValue.sql"));
        Integer enterpriseCode = Integer.valueOf(ini.get("Config", "enterpriseCode"));

        Map<String, String> variableChanges = new HashMap<>();
        variableChanges.put("enterpriseCode", enterpriseCode.toString());

        //Inicializa barra
        Loading loading = new Loading("Realizando baixas", 0, downs.size());
        int i = -1;

        //Percorre todas linhas
        for (Down down : downs) {
            //atualiza barra
            i++;
            loading.updateBar(i);

            //Troca nro documento
            variableChanges.put("document", down.getDocument());

            //Se o valor para baixar não for zero
            if (down.getValue().compareTo(BigDecimal.ZERO) != 0) {
                //Pega parcelas do documento
                ArrayList<String[]> portionResults = Database.getDatabase().select(sqlGetDocumentPortion, variableChanges);
                if (portionResults.size() > 0) {
                    String[] portion = portionResults.get(0);
                    Integer key = Integer.valueOf(portion[1]);
                    BigDecimal liquidValue = new BigDecimal(portion[7]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal pis = new BigDecimal(portion[12]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal cofins = new BigDecimal(portion[13]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal csll = new BigDecimal(portion[14]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal irrf = new BigDecimal(portion[15]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal issqn = new BigDecimal(portion[16]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal inss = new BigDecimal(portion[17]).setScale(2, RoundingMode.HALF_UP);

                    //BigDecimal grossValue = new BigDecimal(portion[18]);
                    //define troca sql a chave
                    variableChanges.put("key", key.toString());

                    //Busca CFOP
                    ArrayList<String[]> cfopResults = Database.getDatabase().select(sqlGetFiscalEntryCFOP, variableChanges);
                    if (cfopResults.size() > 0) {
                        String cfop = cfopResults.get(0)[0];

                        //Buscar valores já pagos da parcela
                        ArrayList<String[]> payValueResults = Database.getDatabase().select(sqlGetPayValue, variableChanges);
                        BigDecimal payValue = new BigDecimal(payValueResults.get(0)[0] == null ? "0.00" : payValueResults.get(0)[0]).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal missingValue = liquidValue.add(payValue.negate()).setScale(2, RoundingMode.HALF_UP);

                        //Se valor que vai ser pago for maior que o valor que falta que falta pagar, mostra aviso e nao paga
                        if (down.getValue().compareTo(missingValue) < 1) {
                            if (liquidValue.compareTo(BigDecimal.ZERO) != 0) {

                                //Pega % do valor total
                                BigDecimal percentOfTotal = down.getValue().divide(liquidValue, 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);

                                //REdefine os impostos
                                pis = pis.multiply(percentOfTotal).setScale(2, RoundingMode.HALF_UP);
                                cofins = cofins.multiply(percentOfTotal).setScale(2, RoundingMode.HALF_UP);
                                csll = csll.multiply(percentOfTotal).setScale(2, RoundingMode.HALF_UP);
                                irrf = irrf.multiply(percentOfTotal).setScale(2, RoundingMode.HALF_UP);
                                issqn = issqn.multiply(percentOfTotal).setScale(2, RoundingMode.HALF_UP);
                                inss = inss.multiply(percentOfTotal).setScale(2, RoundingMode.HALF_UP);

                                //Prepara trocas
                                variableChanges.put("value", down.getValue().toPlainString());
                                variableChanges.put("date", Dates.getCalendarInThisStringFormat(down.getDate(), "yyyy-MM-dd"));
                                variableChanges.put("onlineConferenceKey", ini.get("Config", "onlineConferenceKey"));
                                variableChanges.put("onlinePlan", ini.get("Config", "onlinePlan"));
                                variableChanges.put("pis", pis.toPlainString());
                                variableChanges.put("cofins", cofins.toPlainString());
                                variableChanges.put("csll", csll.toPlainString());
                                variableChanges.put("irrf", irrf.toPlainString());
                                variableChanges.put("issqn", issqn.toPlainString());
                                variableChanges.put("inss", inss.toPlainString());
                                variableChanges.put("downType", ini.get("Config", "downType"));
                                variableChanges.put("cfop", cfop);

                                try {
                                    Database.getDatabase().query(sqlInsertPayValue, variableChanges);
                                } catch (SQLException ex) {
                                    throw new Error(ex);
                                }
                            } else {
                                log
                                        .append("\r\nO documento ")
                                        .append(down.getDocument())
                                        .append(" Possui valor líquido '0,00' no banco!");
                            }
                        } else {
                            log
                                    .append("\r\nO documento ")
                                    .append(down.getDocument())
                                    .append(" Quer baixar ")
                                    .append(down.getValue())
                                    .append(" e falta apenas ")
                                    .append(missingValue);
                        }
                    } else {
                        log
                                .append("\r\nO documento ")
                                .append(down.getDocument())
                                .append(" não possui CFOP no banco de dados da empresa ")
                                .append(enterpriseCode);
                    }
                } else {
                    log
                            .append("\r\nO documento ")
                            .append(down.getDocument())
                            .append(" não possui parcelas no banco de dados da empresa ")
                            .append(enterpriseCode);
                }
            } else {
                log
                        .append("\r\nO documento ")
                        .append(down.getDocument())
                        .append(" tem valor zerado para baixar. ")
                        .append(enterpriseCode);
            }
        }

        //finaliza barra
        loading.dispose();

        System.out.println("Log:\n" + log.toString());
    }

    public StringBuilder getLog() {
        return log;
    }

    public void setLog(StringBuilder log) {
        this.log = log;
    }
}
