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

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.*;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Card;

import android.content.Context;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVExporter implements AbstractConverter {
    private Context mContext;

    /* Null is for default separator "," */
    private Character separator = null;

    public CSVExporter(Context context){
        mContext = context;
    }

    public CSVExporter(Context context, char separator) {
        mContext = context;
        this.separator = separator;
    }

    public void convert(String src, String dest) throws Exception{
        new File(dest).delete();

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, src);
        final CardDao cardDao = helper.getCardDao();
        final CategoryDao categoryDao = helper.getCategoryDao();

        CSVWriter writer;
        if (separator == null) {
            writer = new CSVWriter(new FileWriter(dest));
        } else {
            writer = new CSVWriter(new FileWriter(dest), separator);
        }
        final List<Card> cardList = cardDao.queryForAll();

        // Populate all category field in a transaction.
        categoryDao.callBatchTasks(new Callable<Void>() {
            public Void call() throws Exception {
                for (Card c: cardList) {
                    categoryDao.refresh(c.getCategory());
                }
                return null;
            }
        });
        AnyMemoDBOpenHelperManager.releaseHelper(helper);
        if(cardList.size() == 0){
            throw new IOException("Can't retrieve cards for database: " + src);
        }
        String[] entries = new String[4];
        for(Card card: cardList){
            entries[0] = card.getQuestion();
            entries[1] = card.getAnswer();
            entries[2] = card.getCategory().getName();
            entries[3] = card.getNote();
            writer.writeNext(entries);
        }
        writer.close();
    }
}
