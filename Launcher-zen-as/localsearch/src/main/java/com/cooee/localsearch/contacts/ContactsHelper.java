package com.cooee.localsearch.contacts;


import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.cooee.t9search.model.PinyinUnit;
import com.cooee.t9search.util.PinyinUtil;
import com.cooee.t9search.util.T9MatchPinyinUnits;

import java.util.ArrayList;
import java.util.List;


public class ContactsHelper {

    private static final String TAG = "ContactsHelper";
    private Context mContext;
    private static ContactsHelper mInstance = null;
    private List<Contacts> mBaseContacts = null; //The basic data used for the search
    private List<Contacts> mSearchContacts = null; //The search results from the basic data
    /*save the first input string which search no result.
            mFirstNoSearchResultInput.size<=0, means that the first input string which search no result not appear.
            mFirstNoSearchResultInput.size>0, means that the first input string which search no result has appeared,
            it's  mFirstNoSearchResultInput.toString().
            We can reduce the number of search basic data by the first input string which search no result.
    */
    private StringBuffer mFirstNoSearchResultInput = null;
    private AsyncTask<Object, Object, List<Contacts>> mLoadTask = null;
    private OnContactsLoad mOnContactsLoad = null;
    private OnContactsChanged mOnContactsChanged = null;
    private boolean mContactsChanged = true;
    private ContentObserver mContentObserver;
    private Handler mContactsHandler = new Handler();

    public interface OnContactsLoad {

        void onContactsLoadSuccess();

        void onContactsLoadFailed();
    }

    public interface OnContactsChanged {

        void onContactsChanged();
    }

    private ContactsHelper(
        Context context) {
        this.mContext = context;
        initContactsHelper(context);
        registerContentObserver();
    }

    public static synchronized ContactsHelper getInstance(
        Context context) {
        if (null == mInstance) {
            mInstance = new ContactsHelper(context);
        }
        return mInstance;
    }

    public void destroy() {
        if (null != mInstance) {
            unregisterContentObserver();
            mInstance = null;//the system will free other memory.
        }
    }

    public List<Contacts> getBaseContacts() {
        return mBaseContacts;
    }

    public void setBaseContacts(
        List<Contacts> baseContacts) {
        mBaseContacts = baseContacts;
    }

    public List<Contacts> getSearchContacts() {
        return mSearchContacts;
    }

    public void setSearchContacts(
        List<Contacts> searchContacts) {
        mSearchContacts = searchContacts;
    }

    public OnContactsLoad getOnContactsLoad() {
        return mOnContactsLoad;
    }

    public void setOnContactsLoad(
        OnContactsLoad onContactsLoad) {
        mOnContactsLoad = onContactsLoad;
    }

    private boolean isContactsChanged() {
        return mContactsChanged;
    }

    public void setContactsChanged(
        boolean contactsChanged) {
        mContactsChanged = contactsChanged;
    }

    /**
     * Provides an function to start load contacts
     *
     * @return start load success return true, otherwise return false
     */
    public boolean startLoadContacts() {
        Log.e("",
              "whj res isSearching " + isSearching() + " isContactsChanged " + isContactsChanged());
        if (true == isSearching()) {
            return false;
        }
        if (false == isContactsChanged()) {
            return false;
        }
        initContactsHelper(mContext);
        mLoadTask = new AsyncTask<Object, Object, List<Contacts>>() {

            @Override
            protected List<Contacts> doInBackground(
                Object... params) {
                return loadContacts(mContext);
            }

            @Override
            protected void onPostExecute(
                List<Contacts> result) {
                Log.w("", "whj result " + result.size());
                for (int i = 0; i < result.size(); i++) {
                    Log.v("", "whj result name " + result.get(i).getName());
                }
                parseContacts(result);
                super.onPostExecute(result);
                setContactsChanged(false);
                mLoadTask = null;
            }
        }.execute();
        return true;
    }

    /**
     * @param search (valid characters include:'0'~'9','*','#')
     * @return void
     * @description search base data according to string parameter
     */
    public void parseT9InputSearchContacts(
        String search) {
        if (null == search) {//add all base data to search
            if (null != mSearchContacts) {
                mSearchContacts.clear();
            } else {
                mSearchContacts = new ArrayList<Contacts>();
            }
            for (Contacts contacts : mBaseContacts) {
                contacts.setSearchByType(Contacts.SearchByType.SearchByNull);
                contacts.clearMatchKeywords();
            }
            mSearchContacts.addAll(mBaseContacts);
            mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
            Log.i(TAG,
                  "null==search,mFirstNoSearchResultInput.length()=" + mFirstNoSearchResultInput
                      .length());
            return;
        }
        if (mFirstNoSearchResultInput.length() > 0) {
            if (search.contains(mFirstNoSearchResultInput.toString())) {
                Log.i(
                    TAG,
                    "no need  to search,null!=search,mFirstNoSearchResultInput.length()="
                    + mFirstNoSearchResultInput.length() + "[" + mFirstNoSearchResultInput
                        .toString() + "]" + ";searchlen=" + search
                        .length() + "[" + search + "]");
                return;
            } else {
                Log.i(
                    TAG,
                    "delete  mFirstNoSearchResultInput, null!=search,mFirstNoSearchResultInput.length()="
                    + mFirstNoSearchResultInput.length() + "[" + mFirstNoSearchResultInput
                        .toString() + "]" + ";searchlen=" + search
                        .length() + "[" + search + "]");
                mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
            }
        }
        if (null != mSearchContacts) {
            mSearchContacts.clear();
        } else {
            mSearchContacts = new ArrayList<Contacts>();
        }
        int contactsCount = mBaseContacts.size();
        /**
         * search process:
         * 1:Search by name
         *  (1)Search by name pinyin characters(org name->name pinyin characters)	('0'~'9','*','#')
         *  (2)Search by org name		('0'~'9','*','#')
         * 2:Search by phone number		('0'~'9','*','#')
         */
        for (int i = 0; i < contactsCount; i++) {
            List<PinyinUnit> pinyinUnits = mBaseContacts.get(i).getNamePinyinUnits();
            StringBuffer
                chineseKeyWord =
                new StringBuffer();//In order to get Chinese KeyWords.Of course it's maybe not Chinese characters.
            String name = mBaseContacts.get(i).getName();
            if (true == T9MatchPinyinUnits
                .matchPinyinUnits(pinyinUnits, name, search,
                                  chineseKeyWord)) {//search by NamePinyinUnits;
                mBaseContacts.get(i).setSearchByType(Contacts.SearchByType.SearchByName);
                mBaseContacts.get(i).setMatchKeywords(chineseKeyWord.toString());
                chineseKeyWord.delete(0, chineseKeyWord.length());
                mSearchContacts.add(mBaseContacts.get(i));
                continue;
            }
        }
        if (mSearchContacts.size() <= 0) {
            if (mFirstNoSearchResultInput.length() <= 0) {
                mFirstNoSearchResultInput.append(search);
                Log.i(
                    TAG,
                    "no search result,null!=search,mFirstNoSearchResultInput.length()="
                    + mFirstNoSearchResultInput.length() + "[" + mFirstNoSearchResultInput
                        .toString() + "]" + ";searchlen=" + search
                        .length() + "[" + search + "]");
            } else {
            }
        }
    }

    private void initContactsHelper(
        Context context) {
        mContext = context;
        setContactsChanged(true);
        if (null == mBaseContacts) {
            mBaseContacts = new ArrayList<Contacts>();
        } else {
            mBaseContacts.clear();
        }
        if (null == mSearchContacts) {
            mSearchContacts = new ArrayList<Contacts>();
        } else {
            mSearchContacts.clear();
        }
        if (null == mFirstNoSearchResultInput) {
            mFirstNoSearchResultInput = new StringBuffer();
        } else {
            mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
        }
    }

    /*
     * 监听系统联系人变化
     */
    private void registerContentObserver() {
        if (null == mContentObserver) {
            mContentObserver = new ContentObserver(mContactsHandler) {

                @Override
                public void onChange(
                    boolean selfChange) {
                    // TODO Auto-generated method stub
                    setContactsChanged(true);
                    if (null != mOnContactsChanged) {
                        Log.i("ActivityTest",
                              "mOnContactsChanged mContactsChanged=" + mContactsChanged);
                        mOnContactsChanged.onContactsChanged();
                    }
                    super.onChange(selfChange);
                }
            };
        }
        if (null != mContext) {
            mContext.getContentResolver()
                .registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true,
                                         mContentObserver);
        }
    }

    private void unregisterContentObserver() {
        if (null != mContentObserver) {
            if (null != mContext) {
                mContext.getContentResolver().unregisterContentObserver(mContentObserver);
            }
        }
    }

    private boolean isSearching() {
        return (mLoadTask != null && mLoadTask.getStatus() == Status.RUNNING);
    }

    private List<Contacts> loadContacts(
        Context context) {
        List<Contacts> contacts = new ArrayList<Contacts>();
        Contacts cs = null;
        Cursor cursor = null;
        try {
            Log.v("", "whj res loadContacts");
            cursor =
                context.getContentResolver()
                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                           "sort_key");
            while (cursor.moveToNext()) {
                String
                    displayName =
                    cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String
                    phoneNumber =
                    cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int
                    contactsID =
                    cursor.getInt(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                cs = new Contacts(displayName, phoneNumber, contactsID);
                PinyinUtil.chineseStringToPinyinUnit(cs.getName(),
                                                     cs.getNamePinyinUnits());
                contacts.add(cs);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }
        return contacts;
    }

    private void parseContacts(
        List<Contacts> contacts) {
        if (null == contacts || contacts.size() < 1) {
            if (null != mOnContactsLoad) {
                mOnContactsLoad.onContactsLoadFailed();
            }
            return;
        }
        for (Contacts contact : contacts) {
            if (!mBaseContacts.contains(contact)) {
                mBaseContacts.add(contact);
            }
        }
        if (null != mOnContactsLoad) {
            parseT9InputSearchContacts(null);
            mOnContactsLoad.onContactsLoadSuccess();
        }
        return;
    }
}
