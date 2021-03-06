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

package com.tang.intellij.lua.debugger.attach.protos;

import org.w3c.dom.Node;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttachSetBreakpointProto extends LuaAttachProto {
    private int line;
    private String name;

    public LuaAttachSetBreakpointProto() {
        super(SetBreakpoint);
    }

    @Override
    protected void eachData(Node item) {
        super.eachData(item);
        String name = item.getNodeName();
        if (name.equals("name")) {
            this.name = item.getTextContent();
        } else if (name.equals("line")) {
            this.line = Integer.parseInt(item.getTextContent());
        }
    }

    public int getLine() {
        return line;
    }

    public String getName() {
        return name;
    }
}
