/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.converter;

import java.io.File;

import java.util.List;

import org.liberty.android.fantastischmemo.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import android.content.Context;

public class QATxtImporter implements AbstractConverter{
    private Context mContext;

    public QATxtImporter(Context context){
        mContext = context;
    }

    public void convert(String src, String dest) throws Exception{
        new File(dest).delete();
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dest);
        try {
            final CardDao cardDao = helper.getCardDao();
            BufferedReader txtfile = new BufferedReader(new FileReader(src));
            String line;
            int count = 0;
            List<Card> cardList = new LinkedList<Card>();
            boolean isQ = false;
            StringBuffer qBuf = null;
            StringBuffer aBuf = null;
            while((line = txtfile.readLine()) != null){
                /* remove BOM */
                line = line.replace("\uFEFF", "");

                String head = "";
                if(line.length() >= 2){
                    head = line.substring(0, 2);
                }
                if(line.equals("")){
                    continue;
                }
                else if(head.equals("Q:")){
                    if(isQ == true){
                        /* next line */
                        qBuf.append("<br />" + line.replaceAll("Q:\\s*", ""));
                    }
                    else{
                        isQ = true;
                        /* Save item when the Q is after A
                         * because it is a new item */
                        if(count != 0){
                            Card card = new Card();
                            card.setQuestion(qBuf.toString());
                            card.setAnswer(aBuf.toString());
                            card.setOrdinal(count);
                            card.setCategory(new Category());
                            card.setLearningData(new LearningData());
                            cardList.add(card);
                        }
                        count += 1;
                        qBuf = new StringBuffer();
                        qBuf.append(line.replaceAll("Q:\\s*", ""));
                    }
                }
                else if(head.equals("A:")){
                    if(isQ == true){
                        isQ = false;
                        aBuf = new StringBuffer();
                        aBuf.append(line.replaceAll("A:\\s*", ""));
                    }
                    else{
                        aBuf.append("<br />" + line.replaceAll("A:\\s*", ""));
                    }
                }
                else{
                    if(isQ){
                        qBuf.append("<br />" + line);
                    }
                    else{
                        aBuf.append("<br />" + line);
                    }
                }
            }
            /* Last item need to be added manually */
            count += 1;
            Card card = new Card();
            card.setQuestion(qBuf.toString());
            card.setAnswer(aBuf.toString());
            card.setOrdinal(count);
            card.setCategory(new Category());
            card.setLearningData(new LearningData());
            cardList.add(card);

            txtfile.close();
            cardDao.createCards(cardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }
}

