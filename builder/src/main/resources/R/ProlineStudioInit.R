library(DAPAR)
library(limma)

diffAnaWelch <- function (qData, labels, condition1, condition2) 
{
    if (sum(is.na(qData == TRUE)) > 0) {
        warning("There are some missing values. Please impute before.")
        return(NULL)
    }
    indices <- getIndicesConditions(labels, condition1, condition2)
    t <- NULL
    logRatio <- NULL
    for (i in 1:nrow(qData)) {
        res <- t.test(x = qData[i, indices$iCond1], y = qData[i, 
            indices$iCond2])
        t <- c(t, res$p.value)
        logRatio <- c(logRatio, (res$estimate[2] - res$estimate[1]))
    }
    p <- data.frame(P_Value = t, logFC = logRatio)
    return(p)
}

varianceDistD <- function (qData, labels) 
{
	CVDistD(qData, labels)
}