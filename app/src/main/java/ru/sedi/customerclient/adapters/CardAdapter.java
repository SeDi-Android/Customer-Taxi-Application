package ru.sedi.customerclient.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.BankCard;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardHolder> {

    private QueryList<BankCard> mBankCards;
    private Context mContext;

    public CardAdapter(Context contexts, QueryList<BankCard> cards) {
        mContext = contexts;
        mBankCards = cards;
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank_card, parent, false);
        return new CardHolder(view);
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        holder.updateItem(mBankCards.get(position));
    }

    @Override
    public int getItemCount() {
        return mBankCards.size();
    }

    public class CardHolder extends RecyclerView.ViewHolder {

        private final Unbinder mBind;
        private BankCard mBankCard;
        @BindView(R.id.tvId) TextView tvId;
        @BindView(R.id.tvService) TextView tvService;
        @BindView(R.id.tvCardNumber) TextView tvCardNumber;
        @BindView(R.id.cbCardState) CheckBox cbCardState;

        public CardHolder(View itemView) {
            super(itemView);
            mBind = ButterKnife.bind(this, itemView);
        }

        public void updateItem(BankCard card) {
            mBankCard = card;
            if (mBankCard == null)
                return;

            tvId.setText(String.format("ID:%s", mBankCard.getID()));
            tvService.setText(mBankCard.getService().getName());
            tvCardNumber.setText(Helpers.maskCardNumber(mBankCard.getName(), "#### #### #### ####"));
            tvCardNumber.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/credit.ttf"));
            cbCardState.setChecked(mBankCard.isEnabled());
        }

        @OnClick(R.id.btnRemove)
        public void removeCard() {
            MessageBox.show(mContext, mContext.getString(R.string.remove_card_query), null, new UserChoiseListener() {
                @Override
                public void OnOkClick() {
                    super.OnOkClick();
                    Collections.me().getPaymentCards().edit(mContext, mBankCard, mBankCard.isEnabled(), true);
                }

                @Override
                public void onCancelClick() {
                    super.onCancelClick();
                }
            }, true, new int[]{R.string.yes, R.string.no});
        }

        @OnClick(R.id.cbCardState)
        public void changeState() {
            cbCardState.setChecked(!cbCardState.isChecked());
            int msg = mBankCard.isEnabled() ? R.string.deactivated_card_query : R.string.activated_card_query;
            MessageBox.show(mContext, mContext.getString(msg), null, new UserChoiseListener() {
                @Override
                public void OnOkClick() {
                    super.OnOkClick();
                    Collections.me().getPaymentCards().edit(mContext, mBankCard, !mBankCard.isEnabled(), false);
                }

                @Override
                public void onCancelClick() {
                    super.onCancelClick();
                }
            }, true, new int[]{R.string.yes, R.string.no});
        }

        @Override
        protected void finalize() throws Throwable {
            if (mBind != null)
                mBind.unbind();
            super.finalize();
        }
    }
}
