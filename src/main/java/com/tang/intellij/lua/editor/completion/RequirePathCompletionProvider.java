/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ProjectFileIndexFacade;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaString;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/25.
 */
public class RequirePathCompletionProvider extends CompletionProvider<CompletionParameters> {

    static final char PATH_SPLITTER = '.';

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiFile file = completionParameters.getOriginalFile();
        PsiElement cur = file.findElementAt(completionParameters.getOffset() - 1);
        if (cur != null) {
            LuaString ls = LuaString.getContent(cur.getText());
            String content = ls.value.replace('/', PATH_SPLITTER); //统一用.来处理，aaa.bbb.ccc

            CompletionResultSet resultSet = completionResultSet.withPrefixMatcher(content);
            addAllFiles(completionParameters, resultSet);
        }

        completionResultSet.stopHere();
    }

    void addAllFiles(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        Project project = completionParameters.getOriginalFile().getProject();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (VirtualFile sourceRoot : sourceRoots) {
                addAllFiles(project, completionResultSet, null, sourceRoot.getChildren());
            }
        }
    }

    void addAllFiles(Project project, @NotNull CompletionResultSet completionResultSet, String pck, VirtualFile[] children) {
        for (VirtualFile child : children) {
            if (!ProjectFileIndexFacade.getInstance(project).isInSource(child))
                continue;

            String fileName = FileUtil.getNameWithoutExtension(child.getName());
            String newPath = pck == null ? fileName : pck + "." + fileName;

            if (child.isDirectory()) {
                //noinspection UnsafeVfsRecursion
                addAllFiles(project, completionResultSet, newPath, child.getChildren());
            } else if (child.getFileType() == LuaFileType.INSTANCE) {
                LookupElement lookupElement = LookupElementBuilder
                        .create(newPath)
                        .withIcon(LuaIcons.FILE)
                        .withInsertHandler(new FullPackageInsertHandler());
                completionResultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 1));
            }
        }
    }

    static class FullPackageInsertHandler implements InsertHandler<LookupElement> {

        @Override
        public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
            int tailOffset = insertionContext.getTailOffset();
            PsiElement cur = insertionContext.getFile().findElementAt(tailOffset);

            if (cur != null) {
                int start = cur.getTextOffset();

                LuaString ls = LuaString.getContent(cur.getText());
                insertionContext.getDocument().deleteString(start + ls.start, start + ls.end);

                String lookupString = lookupElement.getLookupString();
                insertionContext.getDocument().insertString(start + ls.start, lookupString);
                insertionContext.getEditor().getCaretModel().moveToOffset(start + ls.start + lookupString.length());
            }
        }
    }
}
