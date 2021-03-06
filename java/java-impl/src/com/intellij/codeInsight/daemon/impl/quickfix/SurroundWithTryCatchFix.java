// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.generation.surroundWith.JavaWithTryCatchSurrounder;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.psiutils.ControlFlowUtils;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;

public class SurroundWithTryCatchFix implements IntentionAction {
  private static final Logger LOG = Logger.getInstance(SurroundWithTryCatchFix.class);

  private PsiElement myElement;

  public SurroundWithTryCatchFix(@NotNull PsiElement element) {
    if (element instanceof PsiStatement ||
        element instanceof PsiResourceVariable ||
        (element instanceof PsiExpression &&
         !(element instanceof PsiMethodReferenceExpression) &&
         ControlFlowUtils.canExtractStatement(ExpressionUtils.getTopLevelExpression((PsiExpression)element)))) {
      myElement = element;
    }
  }

  @Override
  @NotNull
  public String getText() {
    return QuickFixBundle.message("surround.with.try.catch.fix");
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return QuickFixBundle.message("surround.with.try.catch.fix");
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (myElement != null && myElement.isValid()) {
      PsiElement parentStatement = RefactoringUtil.getParentStatement(myElement, false);
      return !(parentStatement instanceof PsiDeclarationStatement &&
               ((PsiDeclarationStatement)parentStatement).getDeclaredElements()[0] instanceof PsiClass);
    }

    return false;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
    int col = editor.getCaretModel().getLogicalPosition().column;
    int line = editor.getCaretModel().getLogicalPosition().line;
    editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0, 0));

    if (myElement instanceof PsiExpression) {
      myElement = RefactoringUtil.ensureCodeBlock(ExpressionUtils.getTopLevelExpression((PsiExpression)myElement));
    }
    myElement = RefactoringUtil.getParentStatement(myElement, false);
    if (myElement == null) return;

    TextRange range = null;

    try{
      JavaWithTryCatchSurrounder handler = new JavaWithTryCatchSurrounder();
      range = handler.surroundElements(project, editor, new PsiElement[]{myElement});
    }
    catch(IncorrectOperationException e){
      LOG.error(e);
    }
    LogicalPosition pos = new LogicalPosition(line, col);
    editor.getCaretModel().moveToLogicalPosition(pos);
    if (range != null) {
      int offset = range.getStartOffset();
      editor.getCaretModel().moveToOffset(offset);
      editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
      editor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
    }
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
