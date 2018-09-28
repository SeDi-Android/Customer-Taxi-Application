package ru.sedi.customerclient.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ru.sedi.customer.R;
import ru.sedi.customerclient.activitys.order_history.OrderHistoryActivity;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

public class OrderHistorySearchParamsDialog extends AppCompatActivity
{
  Spinner spnrPeriod, spnrType;
  Button btnSearch;
  String[] m_periods, m_types;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    try {
      super.onCreate(savedInstanceState);

      m_periods = new String[]{getString(R.string.OnMonth), getString(R.string.OnWeek), getString(R.string.Yesterday), getString(R.string.Today)};
      m_types = new String[]{getString(R.string.Everything), getString(R.string.Complete), getString(R.string.Cancelled), getString(R.string.WithoutRating)};

      setContentView(R.layout.dialog_order_history_diapason);
      setTitle(getString(R.string.search_params));
      InitUiElements();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void InitUiElements()
  {
    spnrPeriod = (Spinner) this.findViewById(R.id.dohd_spnrPeriod);
    spnrPeriod.setAdapter(CreateAdapter(m_periods));
    spnrPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
    {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
      {
        Prefs.setValue(PrefsName.PERIOD_SEARCH_INDEX, i);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView)
      {

      }
    });
    spnrPeriod.setSelection(Prefs.getInt(PrefsName.PERIOD_SEARCH_INDEX));


    spnrType = (Spinner) this.findViewById(R.id.dohd_spnrType);
    spnrType.setAdapter(CreateAdapter(m_types));
    spnrType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
    {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
      {
        Prefs.setValue(PrefsName.TYPE_SEARCH_INDEX, i);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView)
      {

      }
    });
    spnrType.setSelection(Prefs.getInt(PrefsName.TYPE_SEARCH_INDEX));

    btnSearch = (Button) this.findViewById(R.id.dohd_btnSearch);
    btnSearch.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        Intent resultData = new Intent(OrderHistorySearchParamsDialog.this, OrderHistoryActivity.class);
        resultData.putExtra("period", spnrPeriod.getSelectedItemPosition());
        resultData.putExtra("type", spnrType.getSelectedItemPosition());
        resultData.putExtra("without_rating", spnrType.getSelectedItemPosition() == 3 ? true : false );
        setResult(RESULT_OK, resultData);
        finish();
      }
    });
  }

  private ArrayAdapter<String> CreateAdapter(String[] array)
  {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_list_view_element, R.id.tvListViewElement, array);
    return adapter;
  }


}