/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.compiler.container.provider;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.compiler.compressor.BZip2PackCompressor;
import com.izforge.izpack.compiler.compressor.DefaultPackCompressor;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.compressor.RawPackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.merge.MergeManager;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class PackCompressorProvider implements Provider
{

    public PackCompressor provide(CompilerData compilerData, MergeManager mergeManager)
    {
        String format = compilerData.getComprFormat();
        if (format.equals("bzip2"))
        {
            return new BZip2PackCompressor(mergeManager);
        }
        else if (format.equals("raw"))
        {
            return new RawPackCompressor();
        }
        return new DefaultPackCompressor();
    }
}
