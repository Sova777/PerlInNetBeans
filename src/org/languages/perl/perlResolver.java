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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author sova
 */
public class perlResolver extends MIMEResolver {
    
    public perlResolver() {}

    @Override
    public String findMIMEType(FileObject fo) {
        if (fo.hasExt("pl") || fo.hasExt("perl") ||fo.hasExt("pm") || fo.hasExt("cgi")) {
            return "text/x-perl";
        }
        if (fo.isData() && fo.hasExt("")) {
            try {
                InputStream is = fo.getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is));
                    String line = r.readLine();
                    if (line == null) return null;
                    line = line.trim();
                    if (line.indexOf("#!/") == 0 && line.endsWith("/perl")) {
                        return "text/x-perl";
                    }
                } finally {
                    is.close();
                }
            } catch (IOException x) {
            }
        }
        return null;
    }
    
}
