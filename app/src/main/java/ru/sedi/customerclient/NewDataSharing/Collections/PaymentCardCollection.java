package ru.sedi.customerclient.NewDataSharing.Collections;


import android.app.ProgressDialog;
import android.content.Context;

import java.util.Arrays;

import ru.sedi.customerclient.NewDataSharing.BankCard;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.adapters.CardAdapter;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.IFunc;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.IWhere;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;

public class PaymentCardCollection {

    private QueryList<BankCard> mBankCards = new QueryList<>();
    private CardAdapter mAdapter;

    public PaymentCardCollection() {
    }

    public void set(BankCard[] systems) {
        mBankCards.clear();
        mBankCards.addAll(Arrays.asList(systems));
        updateAdapter();
    }

    public QueryList<BankCard> getAll() {
        return mBankCards;
    }

    public BankCard[] getAsArray() {
        return mBankCards.toArray(new BankCard[mBankCards.size()]);
    }

    public QueryList<BankCard> getEnabled() {
        return mBankCards.Where(new IWhere<BankCard>() {
            @Override
            public boolean Condition(BankCard item) {
                return item.isEnabled();
            }
        });
    }

    public void update(final Context context, final boolean needProgress) {
        ProgressDialog pd = null;
        if (needProgress)
            pd = ProgressDialogHelper.show(context);
        final ProgressDialog finalPd = pd;
        AsyncAction.run(new IFunc<BankCard[]>() {
            @Override
            public BankCard[] Func() throws Exception {
                return ServerManager.GetInstance().getPaymentCard();
            }
        }, new IActionFeedback<BankCard[]>() {
            @Override
            public void onResponse(BankCard[] result) {
                if (finalPd != null)
                    finalPd.dismiss();
                set(result);
            }

            @Override
            public void onFailure(Exception e) {
                if (finalPd != null)
                    finalPd.dismiss();
                MessageBox.show(context, e.getMessage());
            }
        });
    }

    private void updateAdapter() {
        if (mAdapter != null) {
            AsyncAction.runInMainThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public CardAdapter getAdapter(Context context) {
        mAdapter = new CardAdapter(context, mBankCards);
        return mAdapter;
    }

    public void edit(final Context context, final BankCard bankCard, final boolean enable, final boolean delete) {
        if (bankCard == null || bankCard.getID().isEmpty())
            return;

        final ProgressDialog pd = ProgressDialogHelper.show(context);
        AsyncAction.run(new IFunc<Server>() {
            @Override
            public Server Func() throws Exception {
                return ServerManager.GetInstance().setCard(bankCard, enable, delete);
            }
        }, new IActionFeedback<Server>() {
            @Override
            public void onResponse(Server result) {
                if (pd != null)
                    pd.dismiss();

                if (result.isSuccess()) {
                    if (delete) {
                        QueryList<BankCard> cards = mBankCards.Where(new IWhere<BankCard>() {
                            @Override
                            public boolean Condition(BankCard item) {
                                return item.getID().equalsIgnoreCase(bankCard.getID());
                            }
                        });
                        mBankCards.removeAll(cards);
                    } else
                        bankCard.setIsEnabled(enable);
                    updateAdapter();
                } else {
                    MessageBox.show(context, result.getError().getName());
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();

                MessageBox.show(context, e.getMessage());
            }
        });
    }
}
