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

import java.io.FileReader;
import java.util.LinkedList;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import au.com.bytecode.opencsv.CSVReader;

import android.content.Context;

public class CSVImporter implements AbstractConverter {
    private Context mContext;
    
    /* Null is for default separator "," */
    private Character separator = null;

    public CSVImporter(Context context) {
        mContext = context;
    }

    public CSVImporter(Context context, char separator) {
        mContext = context;
        this.separator = separator;
    }

    public void convert(String src, String dest) throws Exception {
        new File(dest).delete();
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dest);
        try {
            final CardDao cardDao = helper.getCardDao();

            CSVReader reader;
            if (separator == null) {
                reader = new CSVReader(new FileReader(src));
            } else {
                reader = new CSVReader(new FileReader(src), separator);
            }

            String[] nextLine;
            int count = 0;
            final List<Card> cardList = new LinkedList<Card>();
            while((nextLine = reader.readNext()) != null) {
                if(nextLine.length < 2){
                    throw new Exception("Malformed CSV file. Please make sure the CSV's first column is question, second one is answer and the optinal third one is category");
                }
                count++;
                String note = "";
                String category = "";
                if(nextLine.length >= 3){
                    category = nextLine[2];
                }
                if(nextLine.length >= 4){
                    note = nextLine[3];
                }
                Card card = new Card();
                Category cat = new Category();
                LearningData ld = new LearningData();
                cat.setName(category);

                card.setOrdinal(count);
                card.setCategory(cat);
                card.setLearningData(ld);
                card.setQuestion(nextLine[0]);
                card.setAnswer(nextLine[1]);
                card.setNote(note);
                cardList.add(card);
            }

            cardDao.createCards(cardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }
}

