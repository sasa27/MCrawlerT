/*   This file is part of My Expenses.
 *   My Expenses is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   My Expenses is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with My Expenses.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.totschnig.myexpenses;

import java.util.Currency;
import java.util.Locale;

import android.database.Cursor;
import android.util.Log;

/**
 * Account represents an account stored in the database.
 * Accounts have label, opening balance, description and currency
 * 
 * @author Michael Totschnig
 *
 */
public class Account {
 
  public long id = 0;
   
  public String label;
   
  public float openingBalance;
   
  public String description;
   
  /**
   * java.util.Currency based on ISO 4217 currency symbols
   */
  public Currency currency;
  
  private ExpensesDbAdapter mDbHelper;
  
  /**
   * @see <a href="http://www.currency-iso.org/dl_iso_table_a1.xml">http://www.currency-iso.org/dl_iso_table_a1.xml</a>
   */
  static String [][] CURRENCY_TABLE = {
      {"Afghani", "AFN"},
      {"Albania Lek", "ALL"},
      {"Algerian Dinar", "DZD"},
      {"Angola Kwanza", "AOA"},
      {"Argentine Peso", "ARS"},
      {"Armenian Dram", "AMD"},
      {"Aruban Florin", "AWG"},
      {"Australian Dollar", "AUD"},
      {"Azerbaijanian Manat", "AZN"},
      {"Bahamian Dollar", "BSD"},
      {"Bahraini Dinar", "BHD"},
      {"Bangladesh Taka", "BDT"},
      {"Barbados Dollar", "BBD"},
      {"Belarussian Ruble", "BYR"},
      {"Belize Dollar", "BZD"},
      {"Bermudian Dollar", "BMD"},
      {"Bhutan Ngultrum", "BTN"},
      {"Bolivia Boliviano", "BOB"},
      {"Bosnia and Herzegovina Convertible Mark", "BAM"},
      {"Botswana Pula", "BWP"},
      {"Brazilian Real", "BRL"},
      {"Brunei Dollar", "BND"},
      {"Bulgarian Lev", "BGN"},
      {"Burundi Franc", "BIF"},
      {"Cambodia Riel", "KHR"},
      {"Canadian Dollar", "CAD"},
      {"Cape Verde Escudo", "CVE"},
      {"Cayman Islands Dollar", "KYD"},
      {"CFA Franc BCEAO", "XOF"},
      {"CFA Franc BEAC", "XAF"},
      {"CFP Franc", "XPF"},
      {"Chilean Peso", "CLP"},
      {"China Yuan Renminbi", "CNY"},
      {"Colombian Peso", "COP"},
      {"Comoro Franc", "KMF"},
      {"Congolese Franc", "CDF"},
      {"Costa Rican Colon", "CRC"},
      {"Croatian Kuna", "HRK"},
      {"Cuban Peso", "CUP"},
      {"Cuba Peso Convertible", "CUC"},
      {"Czech Koruna", "CZK"},
      {"Danish Krone", "DKK"},
      {"Djibouti Franc", "DJF"},
      {"Dominican Peso", "DOP"},
      {"East Caribbean Dollar", "XCD"},
      {"Egyptian Pound", "EGP"},
      {"El Salvador Colon", "SVC"},
      {"Eritrea Nakfa", "ERN"},
      {"Ethiopian Birr", "ETB"},
      {"Euro", "EUR"},
      {"Falkland Islands Pound", "FKP"},
      {"Fiji Dollar", "FJD"},
      {"Gambia Dalasi", "GMD"},
      {"Georgia Lari", "GEL"},
      {"Ghana Cedi", "GHS"},
      {"Gibraltar Pound", "GIP"},
      {"Guatemala Quetzal", "GTQ"},
      {"Guinea Franc", "GNF"},
      {"Guyana Dollar", "GYD"},
      {"Haiti Gourde", "HTG"},
      {"Honduras Lempira", "HNL"},
      {"Hong Kong Dollar", "HKD"},
      {"Hungary Forint", "HUF"},
      {"Iceland Krona", "ISK"},
      {"Indian Rupee", "INR"},
      {"Indonesia Rupiah", "IDR"},
      {"Iranian Rial", "IRR"},
      {"Iraqi Dinar", "IQD"},
      {"Israeli Sheqel", "ILS"},
      {"Jamaican Dollar", "JMD"},
      {"Japan Yen", "JPY"},
      {"Jordanian Dinar", "JOD"},
      {"Kazakhstan Tenge", "KZT"},
      {"Kenyan Shilling", "KES"},
      {"Korea Won", "KRW"},
      {"Kuwaiti Dinar", "KWD"},
      {"Kyrgyzstan Som", "KGS"},
      {"Lao Kip", "LAK"},
      {"Latvian Lats", "LVL"},
      {"Lebanese Pound", "LBP"},
      {"Lesotho Loti", "LSL"},
      {"Liberian Dollar", "LRD"},
      {"Libyan Dinar", "LYD"},
      {"Lithuanian Litas", "LTL"},
      {"Macao Pataca", "MOP"},
      {"Macedonia Denar", "MKD"},
      {"Malagasy Ariary", "MGA"},
      {"Malawi Kwacha", "MWK"},
      {"Malaysian Ringgit", "MYR"},
      {"Maldives Rufiyaa", "MVR"},
      {"Mauritania Ouguiya", "MRO"},
      {"Mauritius Rupee", "MUR"},
      {"Mexican Peso", "MXN"},
      {"Moldovan Leu", "MDL"},
      {"Mongolia Tugrik", "MNT"},
      {"Moroccan Dirham", "MAD"},
      {"Mozambique Metical", "MZN"},
      {"Myanmar Kyat", "MMK"},
      {"Namibia Dollar", "NAD"},
      {"Nepalese Rupee", "NPR"},
      {"Netherlands Antillean Guilder", "ANG"},
      {"New Zealand Dollar", "NZD"},
      {"Nicaragua Cordoba Oro", "NIO"},
      {"Nigeria Naira", "NGN"},
      {"North Korean Won", "KPW"},
      {"Norwegian Krone", "NOK"},
      {"Omani Rial", "OMR"},
      {"Pakistan Rupee", "PKR"},
      {"Panama Balboa", "PAB"},
      {"Papua New Guinea Kina", "PGK"},
      {"Paraguay Guarani", "PYG"},
      {"Peru Nuevo Sol", "PEN"},
      {"Philippine Peso", "PHP"},
      {"Poland Zloty", "PLN"},
      {"Qatari Rial", "QAR"},
      {"Romanian Leu", "RON"},
      {"Russian Ruble", "RUB"},
      {"Rwanda Franc", "RWF"},
      {"Saint Helena Pound", "SHP"},
      {"Samoa Tala", "WST"},
      {"Sao Tome and Principe Dobra", "STD"},
      {"Saudi Riyal", "SAR"},
      {"Serbian Dinar", "RSD"},
      {"Seychelles Rupee", "SCR"},
      {"Sierra Leone Leone", "SLL"},
      {"Singapore Dollar", "SGD"},
      {"Solomon Islands Dollar", "SBD"},
      {"Somali Shilling", "SOS"},
      {"South Africa Rand", "ZAR"},
      {"South Sudanese Pound", "SSP"},
      {"Sri Lanka Rupee", "LKR"},
      {"Sudanese Pound", "SDG"},
      {"Surinam Dollar", "SRD"},
      {"Swaziland Lilangeni", "SZL"},
      {"Swedish Krona", "SEK"},
      {"Swiss Franc", "CHF"},
      {"Syrian Pound", "SYP"},
      {"Taiwan Dollar", "TWD"},
      {"Tajikistan Somoni", "TJS"},
      {"Tanzanian Shilling", "TZS"},
      {"Thai Baht", "THB"},
      {"Tonga Pa’anga", "TOP"},
      {"Trinidad and Tobago Dollar", "TTD"},
      {"Tunisian Dinar", "TND"},
      {"Turkish Lira", "TRY"},
      {"Turkmenistan New Manat", "TMT"},
      {"UAE Dirham", "AED"},
      {"Uganda Shilling", "UGX"},
      {"Ukraine Hryvnia", "UAH"},
      {"United Kingdom Pound Sterling", "GBP"},
      {"Uruguayo Peso", "UYU"},
      {"US Dollar", "USD"},
      {"Uzbekistan Sum", "UZS"},
      {"Vanuatu Vatu", "VUV"},
      {"Venezuela Bolivar Fuerte", "VEF"},
      {"Vietnam Dong", "VND"},
      {"Yemeni Rial", "YER"},
      {"Zambian Kwacha", "ZMK"},
      {"Zimbabwe Dollar", "ZWL"},
      {"No currency ", "XXX"},
      {"Gold", "XAU"},
      {"Palladium", "XPD"},
      {"Platinum", "XPT"},
      {"Silver", "XAG"}
  };
  static String [] getCurrencyCodes() {
    int size = CURRENCY_TABLE.length;
    String[] codes = new String[size];
    for(int i=0; i<size; i++) 
      codes[i] = CURRENCY_TABLE[i][1];
    return codes;
  }
  static String [] getCurrencyDescs() {
    int size = CURRENCY_TABLE.length;
    String[] descs = new String[size];
    for(int i=0; i<size; i++) 
      descs[i] = CURRENCY_TABLE[i][0];
    return descs;
  }

  /**
   * returns an empty Account instance
   * @param mDbHelper the database helper used in the activity
   */
  public Account(ExpensesDbAdapter mDbHelper) {
    this.mDbHelper = mDbHelper;
  }
  public Account(ExpensesDbAdapter mDbHelper, String label, float openingBalance, String description, Currency currency) {
    this.mDbHelper = mDbHelper;
    this.label = label;
    this.openingBalance = openingBalance;
    this.description = description;
    this.currency = currency;
  }
  
  /**
   * retrieves an Account instance from the database
   * @param mDbHelper
   * @param id
   */
  public Account(ExpensesDbAdapter mDbHelper, long id) {
    this.mDbHelper = mDbHelper;
    this.id = id;
    Cursor c = mDbHelper.fetchAccount(id);
    this.label = c.getString(c.getColumnIndexOrThrow("label"));
    this.openingBalance = c.getFloat(c.getColumnIndexOrThrow("opening_balance"));
    this.description = c.getString(c.getColumnIndexOrThrow("description"));
    setCurrency(c.getString(c.getColumnIndexOrThrow("currency")));
    c.close();
  }
  /**
   * @param currency if not a legal symbol, silently the currency from the Locale
   * is used instead
   */
  public void setCurrency(String currency) {
    try {
      this.currency = Currency.getInstance(currency);
    } catch (IllegalArgumentException e) {
      Log.e("MyExpenses",currency + " is not defined in ISO 4217");
      this.currency = Currency.getInstance(Locale.getDefault());
    }
  }
  
  /**
   * @return the sum of opening balance and all transactions for the account
   */
  public float getCurrentBalance() { 
    return openingBalance + mDbHelper.getExpenseSum(id);
  }
  
  /**
   * deletes all expenses and set the new opening balance to the current balance
   */
  public void reset() {
    openingBalance = getCurrentBalance();
    mDbHelper.updateAccountOpeningBalance(id,openingBalance);
    mDbHelper.deleteExpenseAll(id);
  }
  
  /**
   * Saves the account, creating it new if necessary
   * @return the id of the account. Upon creation it is returned from the database
   */
  public long save() {
    if (id == 0) {
      id = mDbHelper.createAccount(label, openingBalance, description,currency.getCurrencyCode());
    } else {
      mDbHelper.updateAccount(id, label,openingBalance,description,currency.getCurrencyCode());
    }
    return id;
  }
}

