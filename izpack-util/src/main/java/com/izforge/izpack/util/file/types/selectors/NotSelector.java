/*
 * Copyright  2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file.types.selectors;

/**
 * This selector has one other selectors whose meaning it inverts. It actually
 * relies on NoneSelector for its implementation of the isSelected() method, but
 * it adds a check to ensure there is only one other selector contained within.
 */
public class NotSelector extends NoneSelector
{

    /**
     * Default constructor.
     */
    public NotSelector()
    {
    }

    /**
     * @return a string representation of the selector
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        if (hasSelectors())
        {
            buf.append("{notselect: ");
            buf.append(super.toString());
            buf.append("}");
        }
        return buf.toString();
    }

    /**
     * Makes sure that there is only one entry, sets an error message if not.
     */
    public void verifySettings()
    {
        if (selectorCount() != 1)
        {
            setError("One and only one selector is allowed within the "
                    + "<not> tag");
        }
    }

}
