INSERT INTO VEF_EMP_TMOVSERPARPGTO (BDCODEMP, BDCHAVE, BDVALORPGTOPAR,
    BDDATAPGTOPAR, BDCHAVECONFONLINE, BDCODPLANOONLINE, BDVALORRETPIS,
    BDVALORRETCOFINS, BDVALORRETCSLL, BDVALORRETIRRF, BDVALORRETISSQN,
    BDVALORRETINSS, BDCODTIPOBAIXA, BDCODNAT,BDCODPLANOCONFSN)
VALUES (
    ':enterpriseCode', 
    ':key', 
    ':value', 
    ':date', 
    ':onlineConferenceKey', 
    ':onlinePlan', 
    ':pis', 
    ':cofins', 
    ':csll', 
    ':irrf', 
    ':issqn', 
    ':inss', 
    ':downType', 
    ':cfop',
    null
);
