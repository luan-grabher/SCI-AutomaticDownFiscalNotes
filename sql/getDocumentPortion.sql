SELECT FIRST 1 
    a.BDCODEMP,         -- 0  empresa
    a.BDCHAVE,          -- 1  chave lcto
    a.BDORDEMPAR,       -- 2  ordem
    a.BDCODIGOPLA,      -- 3  ??
    a.BDPARCELAPAR,     -- 4  ??
    a.BDCHEQUEPAR,      -- 5  ??
    a.BDDUPLICATAPAR,   -- 6  numero doc
    a.BDVALORPAR,       -- 7  valor - impostos
    a.BDDATAVENCTOPAR,  -- 8  vencimento
    a.BDOUTROSPAR,      -- 9  ??
    a.BDRECIBOPAR,      -- 10 ??
    a.BDPROMISSPAR,     -- 11 ??
    a.BDVALORRETPIS,    -- 12 pis
    a.BDVALORRETCOFINS, -- 13 cofins
    a.BDVALORRETCSLL,   -- 14 csll
    a.BDVALORRETIRRF,   -- 15 irrf
    a.BDVALORRETISSQN,  -- 16 issqn
    a.BDVALORRETINSS,   -- 17 inss
    a.BDVALORBRUTO      -- 18 valor bruto
FROM VEF_EMP_TMOVSERPAR a 
WHERE
    a.BDCODEMP = ':enterpriseCode' AND 
    a.BDDUPLICATAPAR = ':document'
