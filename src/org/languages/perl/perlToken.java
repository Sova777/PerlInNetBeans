/*
* CDDL HEADER START
*
* The contents of this file are subject to the terms of the
* Common Development and Distribution License, Version 1.0 only
* (the "License"). You may not use this file except in compliance
* with the License.
*
* You can obtain a copy of the license at LICENSE.txt
* or http://netbeans.mojgorod.ru/perl_licensing.html.
* See the License for the specific language governing permissions
* and limitations under the License.
*
* When distributing Covered Code, include this CDDL HEADER in each
* file and include the License file at LICENSE.txt.
* If applicable, add the following below this CDDL HEADER, with the
* fields enclosed by brackets "[]" replaced with your own identifying
* information: Portions Copyright [yyyy] [name of copyright owner]
*
* CDDL HEADER END
*/

/*
* Copyright 2008-2012 Valeriy Soldatov. All rights reserved.
* Use is subject to license terms.
*/
package org.languages.perl;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;

/**
 *
 * @author valera
 */
public class perlToken {
    private static final String MIME_TYPE = "text/x-perl";

    public static Object[] getToken (CharInput input) {
        int start = input.getIndex ();

        while (!input.eof () && input.next () != '^') {
            input.read ();
        }

        if (input.next () == '^') {
            Language language;

            try {
                language = LanguagesManager.get().getLanguage(MIME_TYPE);
            } catch (LanguageDefinitionNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }

            return new Object[] {
                ASTToken.create (language, "string", "", 0, 0, null),
                null
            };
        }

        input.setIndex (start);
        return null;
    }

}
