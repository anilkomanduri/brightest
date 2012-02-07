/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd. 
 * Hyderabad, India
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following condition
 * is met:
 *
 *     + Neither the name of Imaginea, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.pramati.brightest.neo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author apurba
 */
public abstract class AbstractWriter implements Writer {
    protected static class ResultInfo {
        protected final String testFileName;
        protected String failureMessage = null;
        protected int failureStep = -1;
        protected String executionTime = null;
        protected HashMap<Integer, String> executionTimeMap = new HashMap<Integer, String>();

        // Sample result :resutStr = C:\Documents and Settings\leon\Desktop\test1.xls~2~Element link=Pramati
        // Technologies not found**$$** 0~3**$$** 1~3
        protected ResultInfo(String rawResult) {
            StringTokenizer st = new StringTokenizer(rawResult, "**$$**");
            // String[] results = rawResult.split("**$$**");
            String firstElem = "";
            if (st.countTokens() >= 1) {
                firstElem = st.nextToken();
            }
            String[] splits = firstElem.split("~");
            testFileName = splits[0];
            if (splits.length > 1) {
                if (splits[1].trim().length() > 0) {
                    try {
                        failureStep = Integer.parseInt(splits[1]);
                    } catch (NumberFormatException nfexc) {
                        // do nothing as this is very possible
                    }
                }
                failureMessage = splits[2];
            }
            while (st.hasMoreElements()) {
                String object = (String) st.nextElement();
                String[] successStep = object.split("~");
                if (successStep.length >= 2) {
                    try {
                        executionTimeMap.put(Integer.parseInt(successStep[0].trim()), successStep[1]);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }
    }

    protected String formatRawCommand(String rawCommand) {
        String[] splits = rawCommand.split("~");
        String formattedCommand = "";
        // TODO hack alert, correct this, we should not be using such string tokens
        switch (splits.length) {
            case 1:
                formattedCommand = String.format("%s()", splits[0]);
                break;
            case 2:
                formattedCommand = String.format("%s(%s)", splits[0], splits[1]);
                break;
            case 3:
                if (splits[2] == null || splits[2].trim().length() == 0) {
                    formattedCommand = String.format("%s(%s)", splits[0], splits[1]);
                } else {
                    splits[2] = splits[2].trim();
                    formattedCommand = String.format("%s(%s, %s)", splits[0], splits[1], splits[2]);
                }
                break;
            default:
                throw new IllegalArgumentException("We can not understand " + rawCommand + " with our current logic");
        }
        return formattedCommand;
    }

    /*
     * (non-Javadoc)
     * @see com.pramati.brightest.neo.ResultWriter#writeResults(java.lang.String[])
     */
    public void writeResults(String[] rawResults) {
        try {
            for (String rawResult : rawResults) {
                ResultInfo resultInfo = new ResultInfo(rawResult);
                HSSFWorkbook workBook = new HSSFWorkbook(new BufferedInputStream(new FileInputStream(resultInfo.testFileName)));
                addResults(workBook, resultInfo);
                write(workBook);
            }
        } catch (Exception exc) {
            // YES, we are eating the exception as I do not want my plugin to be compromised
            exc.printStackTrace();
        }
    }

    protected void addResults(HSSFWorkbook workBook, ResultInfo resultInfo) {
        boolean isFailed = (resultInfo.failureStep > -1);
        String statusMessage = (isFailed) ? "Failed" : "Passed";
        HSSFSheet firstSheet = workBook.getSheetAt(0);
        addCellWithContent(firstSheet.getRow(0), 6, "Result", true);
        addCellWithContent(firstSheet.getRow(1), 6, statusMessage);
        HSSFSheet secondSheet = workBook.getSheetAt(1);
        addCellWithContent(secondSheet.getRow(0), 2, "Result", true);
        addCellWithContent(secondSheet.getRow(0), 3, "Reason", true);
        addCellWithContent(secondSheet.getRow(0), 4, "Execution Time(ms)", true);
        if (isFailed) {
            // HSSFSheet secondSheet = workBook.getSheetAt(1);
            for (int i = 0; i <= resultInfo.failureStep; i++) {
                addCellWithContent(secondSheet.getRow(i + 1), 2, "Passed");
            }
            addCellWithContent(secondSheet.getRow(resultInfo.failureStep + 1), 2, "Failed");
            addCellWithContent(secondSheet.getRow(resultInfo.failureStep + 1), 3, resultInfo.failureMessage);
        }
        // record the the execution time
        for (Map.Entry<Integer, String> entry : resultInfo.executionTimeMap.entrySet()) {
            addCellWithContent(secondSheet.getRow((entry.getKey()).intValue() + 1), 4, entry.getValue());
        }
    }

    protected void addCellWithContent(HSSFRow row, int cellPosition, String cellValue) {
        addCellWithContent(row, cellPosition, cellValue, false);
    }

    protected void addCellWithContent(HSSFRow row, int cellPosition, String cellValue, boolean clonePreviousStyle) {
        HSSFCell cell = row.createCell(cellPosition);
        if (clonePreviousStyle) {
            HSSFCell previousCell = row.getCell(cellPosition - 1);
            cell.setCellStyle(previousCell.getCellStyle());
        }
        try {
            int val = Integer.parseInt(cellValue);
            cell.setCellValue(val);
        } catch (NumberFormatException e) {
            cell.setCellValue(new HSSFRichTextString(cellValue));
        }
    }

    @SuppressWarnings("deprecation")
    protected String getCellValue(HSSFCell cell) {
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case HSSFCell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            default:
                try {
                    return cell.getStringCellValue();
                } catch (Exception exc) {
                    return "";
                }
        }
    }

    protected abstract void write(HSSFWorkbook workBook);

    protected static interface WritingTemplate {
        public void doWithStream(OutputStream outputStream) throws IOException;
    }

    protected void writeToFile(WritingTemplate template, String outputFileName) {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFileName));
            template.doWithStream(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException exc) {
                // ignoring the exception with just logging as we were unable to close the stream
                exc.printStackTrace();
            }
        }
    }
}
