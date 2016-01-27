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
package org.languages.perl.actions;

import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author sova
 */
public class perlOutputListener implements OutputListener {

    DataObject dObj;
    int pos;
    int realLineNo;

    perlOutputListener(DataObject dObj, int realLineNo, int pos) {
        this.dObj = dObj;
        this.pos = pos;
        this.realLineNo = realLineNo;
    }

    public void outputLineSelected(OutputEvent arg0) {
    }

    public void outputLineAction(OutputEvent arg0) {
        LineCookie lc = dObj.getCookie(org.openide.cookies.LineCookie.class);
        Line line = lc.getLineSet().getOriginal(realLineNo);
        line.show(Line.SHOW_GOTO, pos);
    }

    public void outputLineCleared(OutputEvent arg0) {
    }
}
