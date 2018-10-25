package com.AMS.jBEAM.javaParser;

public class TextInsertionInfo
{
    private final IntRange  insertionRange;
    private final int       caretPositionAfterInsertion;
    private final String    textToInsert;

    public TextInsertionInfo(IntRange insertionRange, int caretPositionAfterInsertion, String textToInsert) {
        this.insertionRange = insertionRange;
        this.caretPositionAfterInsertion = caretPositionAfterInsertion;
        this.textToInsert = textToInsert;
    }

    IntRange getInsertionRange() {
        return insertionRange;
    }

    int getCaretPositionAfterInsertion() {
        return caretPositionAfterInsertion;
    }

    String getTextToInsert() {
        return textToInsert;
    }
}
