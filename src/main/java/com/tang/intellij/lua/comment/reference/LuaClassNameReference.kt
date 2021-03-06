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

package com.tang.intellij.lua.comment.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.tang.intellij.lua.comment.psi.LuaDocClassNameRef
import com.tang.intellij.lua.search.LuaPredefinedScope
import com.tang.intellij.lua.stubs.index.LuaClassIndex

/**

 * Created by TangZX on 2016/11/29.
 */
class LuaClassNameReference(element: LuaDocClassNameRef) : PsiReferenceBase<LuaDocClassNameRef>(element) {

    override fun getRangeInElement() = TextRange(0, myElement.textLength)

    override fun isReferenceTo(element: PsiElement): Boolean {
        return myElement.manager.areElementsEquivalent(element, resolve())
    }

    override fun resolve(): PsiElement? {
        val name = myElement.text
        val defs = LuaClassIndex.getInstance().get(name, myElement.project, LuaPredefinedScope(myElement.project))
        if (defs.isNotEmpty()) {
            return defs.firstOrNull()
        }
        return null
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
