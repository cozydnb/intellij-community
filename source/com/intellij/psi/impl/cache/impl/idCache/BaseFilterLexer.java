package com.intellij.psi.impl.cache.impl.idCache;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.search.IndexPattern;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharSequenceSubSequence;
import gnu.trove.TIntIntHashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseFilterLexer extends LexerBase implements IdTableBuilding.ScanWordProcessor {
  protected final Lexer myOriginalLexer;
  protected final TIntIntHashMap myTable;
  protected final int[] myTodoCounts;

  private int myTodoScannedBound = 0;
  private int myOccurenceMask;
  protected CharSequence myBuffer;

  protected BaseFilterLexer(Lexer originalLexer, TIntIntHashMap table, int[] todoCounts) {
    myOriginalLexer = originalLexer;
    myTable = table;
    myTodoCounts = todoCounts;
  }

  public void start(char[] buffer) {
    myOriginalLexer.start(buffer);
  }

  public void start(char[] buffer, int startOffset, int endOffset) {
    myOriginalLexer.start(buffer, startOffset, endOffset);
  }

  public void start(char[] buffer, int startOffset, int endOffset, int initialState) {
    myOriginalLexer.start(buffer, startOffset, endOffset, initialState);
  }

  public final CharSequence getBufferSequence() {
    return myBuffer;
  }

  public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
    myBuffer = buffer;
    myOriginalLexer.start(buffer, startOffset, endOffset, initialState);
  }

  public int getState() {
    return myOriginalLexer.getState();
  }

  public final IElementType getTokenType() {
    return myOriginalLexer.getTokenType();
  }

  public final int getTokenStart() {
    return myOriginalLexer.getTokenStart();
  }

  public final int getTokenEnd() {
    return myOriginalLexer.getTokenEnd();
  }

  public final char[] getBuffer() {
    return myOriginalLexer.getBuffer();
  }

  public int getBufferEnd() {
    return myOriginalLexer.getBufferEnd();
  }

  protected final void advanceTodoItemCountsInToken() {
    if (myTodoCounts != null){
      int start = getTokenStart();
      int end = getTokenEnd();
      start = Math.max(start, myTodoScannedBound);
      if (start >= end) return; // this prevents scanning of the same comment twice

      CharSequence input = new CharSequenceSubSequence(myBuffer, start, end);
      advanceTodoItemsCount(input, myTodoCounts);

      myTodoScannedBound = end;
    }
  }

  public static void advanceTodoItemsCount(final CharSequence input, final int[] todoCounts) {
    IndexPattern[] patterns = IdCacheUtil.getIndexPatterns();
    for(int index = 0; index < patterns.length; index++){
      Pattern pattern = patterns[index].getPattern();
      if (pattern != null){
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()){
          if (matcher.start() != matcher.end()){
            todoCounts[index]++;
          }
        }
      }
    }
  }

  public void run(CharSequence chars, int start, int end) {
    IdCacheUtil.addOccurrence(myTable, chars, start, end, myOccurenceMask);
  }

  protected final void scanWordsInToken(final int occurrenceMask) {
    myOccurenceMask = occurrenceMask;
    IdTableBuilding.scanWords(this, getBufferSequence(), getTokenStart(), getTokenEnd());
  }
}
