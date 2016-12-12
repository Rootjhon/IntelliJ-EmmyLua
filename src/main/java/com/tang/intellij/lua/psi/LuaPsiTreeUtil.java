package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaPsiTreeUtil {

    public interface ElementProcessor<T extends PsiElement> {
        boolean accept(T t);
    }

    /**
     * 向上寻找 local function 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalFuncDef(PsiElement current, ElementProcessor<LuaNameDef> processor) {
        if (current == null || processor == null)
            return;
        boolean continueSearch = true;
        int treeDeep = 0;
        int funcDeep = 0;
        PsiElement curr = current;
        do {
            if (curr instanceof LuaLocalFuncDef) {
                //第一级local function不能使用
                if (funcDeep > 0 || treeDeep == 0) {
                    LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                    LuaNameDef funcName = localFuncDef.getNameDef();
                    //名字部分
                    if (funcName != null)
                        continueSearch = processor.accept(funcName);
                }
                funcDeep++;
            }

            PsiElement prevSibling = curr.getPrevSibling();
            if (prevSibling == null) {
                treeDeep++;
                prevSibling = curr.getParent();
            }
            curr = prevSibling;
        } while (continueSearch && !(curr instanceof PsiFile));
    }

    /**
     * 向上寻找 local 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalNameDef(PsiElement current, ElementProcessor<LuaNameDef> processor) {
        if (current == null || processor == null)
            return;
        boolean continueSearch = true;
        PsiElement curr = current;
        do {
            boolean searchParList = false;
            PsiElement next = curr.getPrevSibling();
            if (next == null) {
                searchParList = true;
                next = curr.getParent();
            }
            curr = next;

            if (curr instanceof LuaLocalDef) {
                LuaNameList nameList = ((LuaLocalDef) curr).getNameList();
                continueSearch = resolveInNameList(nameList, processor);
            }
            else if (curr instanceof LuaFuncBody) {
                //参数部分
                if (searchParList) continueSearch = resolveInFuncBody((LuaFuncBody) curr, processor);
            }
            // for name = x, y do end
            else if (curr instanceof LuaForAStat) {
                LuaForAStat forAStat = (LuaForAStat) curr;
                if (searchParList) continueSearch = processor.accept(forAStat.getNameDef());
            }
            // for name in xxx do end
            else if (curr instanceof LuaForBStat) {
                LuaForBStat forBStat = (LuaForBStat) curr;
                if (searchParList) continueSearch = resolveInNameList(forBStat.getNameList(), processor);
            }
        } while (continueSearch && !(curr instanceof PsiFile));
    }

    private static boolean resolveInFuncBody(LuaFuncBody funcBody, ElementProcessor<LuaNameDef> processor) {
        if (funcBody != null) {
            for (LuaParDef parDef : funcBody.getParDefList()) {
                if (!processor.accept(parDef)) return false;
            }
        }
        return true;
    }

    private static boolean resolveInNameList(LuaNameList nameList, ElementProcessor<LuaNameDef> processor) {
        if (nameList != null) {
            for (LuaNameDef nameDef : nameList.getNameDefList()) {
                if (!processor.accept(nameDef)) return false;
            }
        }
        return true;
    }
}