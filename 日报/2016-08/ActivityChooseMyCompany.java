package com.usung.smarttradetwo.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.reflect.TypeToken;
import com.usung.smarttradetwo.R;
import com.usung.smarttradetwo.activity.review_center.ActivityReviewCenterMain;
import com.usung.smarttradetwo.activity.review_center.ActivityReviewHistory;
import com.usung.smarttradetwo.activity.team.ActivityReportHistory;
import com.usung.smarttradetwo.activity.team.ActivityReportStatistics;
import com.usung.smarttradetwo.adapter.user.AdapterOrgList;
import com.usung.smarttradetwo.base.BaseActivity;
import com.usung.smarttradetwo.base.BaseApplication;
import com.usung.smarttradetwo.bean.register.EpBase;
import com.usung.smarttradetwo.bean.register.EpBaseInfo;
import com.usung.smarttradetwo.bean.user.User;
import com.usung.smarttradetwo.utils.APIConstant;
import com.usung.smarttradetwo.utils.GsonHelper;
import com.usung.smarttradetwo.utils.ImmersionStatus;
import com.usung.smarttradetwo.utils.StringHelper;
import com.usung.smarttradetwo.utils.ToastUtil;
import com.usung.smarttradetwo.utils.okhttp.DealCallBacks;
import com.usung.smarttradetwo.utils.okhttp.ResponseUtil;
import com.usung.smarttradetwo.widgets.AlertDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import java.util.ArrayList;
import okhttp3.Call;

/**
 * 选择我的企业列表
 *
 * @author herui 2016/07/19
 */
public class ActivityChooseMyCompany extends BaseActivity {

    private ArrayList<EpBaseInfo> list_ep; //我的公司列表
    private ListView mListView;
    private String fromWhere = "";
    private Intent intent = null;
    private  View headerView;
    private String orgId = "";
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_my_company);
        ImmersionStatus.getInstance().setStateColor(this, R.color.blue_color_theme);
        fromWhere = getIntent().getStringExtra("fromWhere");

        initViews();
        getMyOrgList();
    }

    @Override
    protected void initViews() {
        super.initViews();
        title.setText(getResources().getString(R.string.choose_company));

        user = BaseApplication.getInstance().getUser();
        if (intent == null) {
            intent = new Intent();
        }
        mListView = (ListView) findViewById(R.id.mListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (StringHelper.isNotEmpty(fromWhere)) {
                    if (fromWhere.equals("ActivityCompanyInfoEdit")) {
                        intent.setClass(ActivityChooseMyCompany.this, ActivityCompanyInfoEdit.class);
                    } else if (fromWhere.equals("ActivityReviewCenterMain_FragmentWaittingMeToReview")) {
                        intent.setClass(ActivityChooseMyCompany.this, ActivityReviewCenterMain.class);
                    } else if (fromWhere.equals("ActivityReviewCenterMain_FragmentMyApplicationList")) {
                        intent.setClass(ActivityChooseMyCompany.this, ActivityReviewCenterMain.class);
                    } else if (fromWhere.equals("ActivityReviewHistory")) {
                        intent.setClass(ActivityChooseMyCompany.this, ActivityReviewHistory.class);
                    } else if (fromWhere.equals("ActivityReportHistory")) {
                        intent.setClass(ActivityChooseMyCompany.this, ActivityReportHistory.class);
                        intent.putExtra("Id", user.getUserId());
                        intent.putExtra("position", position);
                    } else if (fromWhere.equals("ActivityReportStatistics")) {
                        intent.setClass(ActivityChooseMyCompany.this, ActivityReportStatistics.class);
                        intent.putExtra("Id", user.getUserId());
                        intent.putExtra("position", position);
                    }
                }

                if (position == 0){
                    if (StringHelper.isNotEmpty(fromWhere)) {
                        if (fromWhere.equals("ActivityReviewCenterMain_FragmentWaittingMeToReview") || fromWhere.equals("ActivityReviewCenterMain_FragmentMyApplicationList")
                                || fromWhere.equals("ActivityReviewHistory")) {
                            intent.putExtra("orgId", "");
                        }else{
                            EpBaseInfo mEpBaseInfo = (EpBaseInfo) parent.getAdapter().getItem(position);
                            EpBase registerEpBase = new EpBase();
                            registerEpBase.setOrgId(mEpBaseInfo.getOrgId());
                            registerEpBase.setOrgName(mEpBaseInfo.getOrgName());
                            registerEpBase.setAuthState(mEpBaseInfo.getAuthState());
                            intent.putExtra("orgId", mEpBaseInfo.getOrgId());
                        }
                    }
                }else {
                    EpBaseInfo mEpBaseInfo = (EpBaseInfo) parent.getAdapter().getItem(position);
                    EpBase registerEpBase = new EpBase();
                    registerEpBase.setOrgId(mEpBaseInfo.getOrgId());
                    registerEpBase.setOrgName(mEpBaseInfo.getOrgName());
                    registerEpBase.setAuthState(mEpBaseInfo.getAuthState());
//                    if (StringHelper.isNotEmpty(fromWhere)) {
//                        if (fromWhere.equals("ActivityCompanyInfoEdit")) {
//                            intent.putExtra("epBase", registerEpBase);
//                        }
//                    }
                    intent.putExtra("orgId", mEpBaseInfo.getOrgId());
                }
                intent.putExtra("fromWhere", fromWhere);
                startActivity(intent);
            }
        });
    }

    /**
     * 获取我的企业列表
     */
    public void getMyOrgList() {
        ShowLoading(getResources().getString(R.string.loading));
            OkHttpUtils.get().url(APIConstant.USUNG_HOST + APIConstant.GetUserEPList)
                .addParams("confirmState", "true")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                dismissDialog();
                ToastUtil.showToastResId(ActivityChooseMyCompany.this, R.string.network_not_activated, Toast.LENGTH_SHORT);
                finish();
            }

            @Override
            public void onResponse(final String response) {
                dismissDialog();
                ResponseUtil.dealResponse(response, new DealCallBacks() {
                    @Override
                    public void onSuccess(String items, int total) {
                        list_ep = GsonHelper.getGson().fromJson(items, new TypeToken<ArrayList<EpBaseInfo>>() { }.getType());
                        if (list_ep != null && list_ep.size() != 0) {
                            AdapterOrgList adapterOrgList = new AdapterOrgList(ActivityChooseMyCompany.this, list_ep);
                            mListView.setAdapter(adapterOrgList);
                        } else {
                            new AlertDialog(ActivityChooseMyCompany.this).builder()
                                    .setCancelable(false)
                                    .setMsg(getString(R.string.no_org))
                                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }

                        addListViewHeader();
                    }

                    @Override
                    public void onFailure(int error, String msg) {
                        ToastUtil.showToastMessageString(ActivityChooseMyCompany.this, msg, Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }

    /**
     * 设置企业选择列表的headerView
     */
    private void addListViewHeader(){
        if (StringHelper.isNotEmpty(fromWhere)){
            headerView = LayoutInflater.from(ActivityChooseMyCompany.this).inflate(R.layout.item_list_choose_my_company, null);
            ((TextView)headerView.findViewById(R.id.tv_id_my_company)).setText("不限");
            if (fromWhere.equals("ActivityReviewCenterMain_FragmentWaittingMeToReview")){
                mListView.addHeaderView(headerView);
                intent.setClass(ActivityChooseMyCompany.this, ActivityReviewCenterMain.class);
            }else if (fromWhere.equals("ActivityReviewCenterMain_FragmentMyApplicationList")){
                mListView.addHeaderView(headerView);
                intent.setClass(ActivityChooseMyCompany.this, ActivityReviewCenterMain.class);
            }else if (fromWhere.equals("ActivityReviewHistory")){

            }
        }
    }
}
