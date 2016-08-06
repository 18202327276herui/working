package com.usung.smarttradetwo.activity.team;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.google.gson.reflect.TypeToken;
import com.usung.smarttradetwo.R;
import com.usung.smarttradetwo.activity.user.ActivityMain;
import com.usung.smarttradetwo.adapter.team.TeamOrgListAdapter;
import com.usung.smarttradetwo.adapter.team.UserLocListAdapter;
import com.usung.smarttradetwo.base.BaseActivity;
import com.usung.smarttradetwo.bean.team.EPBase;
import com.usung.smarttradetwo.bean.team.UserPosition;
import com.usung.smarttradetwo.utils.APIConstant;
import com.usung.smarttradetwo.utils.GsonHelper;
import com.usung.smarttradetwo.utils.ToastUtil;
import com.usung.smarttradetwo.utils.okhttp.DealCallBacks;
import com.usung.smarttradetwo.utils.okhttp.ResponseUtil;
import com.usung.smarttradetwo.widgets.AlertDialog;
import com.usung.smarttradetwo.widgets.BbxxPopWindow;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * 位置报告历史列表
 * @author hj 2016/6/22
 *
 */
public class ActivityReportHistory extends BaseActivity implements OnRefreshListener,OnLoadMoreListener {

    private FrameLayout frameLayout;
    private WebView mWebView;
    private CheckBox cbx_time;
    private BbxxPopWindow bbxxPopWindow;
    private ImageView img_list_background;
    private TextView tv_orgTitle;
    private String DateString;
    private JSONObject jsonObject;//post数据
    private SwipeToLoadLayout swipeToLoadLayout;
    private ListView listView;
    private UserLocListAdapter mAdapter;
    private ArrayList<UserPosition> list_user;
    private int position;
    private String memberId;//员工ID
    private int page = 1;//当前页码
    private final int SIZE = 7;//每页记录数量
    private String orgId;//企业ID
    private UserPosition userPosition;
    private ArrayList<EPBase> list_ep;//我的公司列表
    private int orgIndex = 0;//当前所选公司下标
    private PopupWindow mOrgPopupWindow;//选择公司的popupWindow
    private TeamOrgListAdapter mTeamOrgListAdapter;
    private boolean onlyDateSelect;
    //常量定义
    private static final int SERVICE_PAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_history);

        memberId        = getIntent().getExtras().getString("Id");
        orgId           = getIntent().getExtras().getString("orgId");
        onlyDateSelect  = getIntent().getExtras().getBoolean("onlyDateSelect", false);
        position        = getIntent().getExtras().getInt("position", 0);

        initViews();
        addEventListener();
        //初始化下拉刷新
//        swipeToLoadLayout= (SwipeToLoadLayout) findViewById(R.id.swipeToLoadLayout);
//        CustomRefreshHeadView refreshHeadView=new CustomRefreshHeadView(this);
//        CustomLoadFootView loadFootView=new CustomLoadFootView(this);
//        refreshHeadView.setPadding(20,20,20,20);
//        refreshHeadView.setGravity(Gravity.CENTER);
//        ViewGroup.MarginLayoutParams params=new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        refreshHeadView.setLayoutParams(params);
//        swipeToLoadLayout.setRefreshHeaderView(refreshHeadView);
//        swipeToLoadLayout.setOnRefreshListener(this);
//        swipeToLoadLayout.setRefreshCompleteDelayDuration(1000);
        //上拉加载更多

        getMyOrgList();
        initWebView();

       // GetUserLocList();
    }

    @Override
    protected void initViews() {
        super.initViews();
        title.setText("位置报告历史");
        rightButton = (Button) findViewById(R.id.rightButton);
//        listView         = (ListView) findViewById(R.id.UserLocList);
        cbx_time = (CheckBox) findViewById(R.id.btn_time);
        tv_orgTitle = (TextView) findViewById(R.id.tv_orgTitle);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mWebView = (WebView) findViewById(R.id.mWebView);

        rightButton.setBackgroundResource(R.mipmap.phone_white);

        //获取当前日期
        Date       date     = new Date();
        DateFormat format   = new SimpleDateFormat("yyyy-MM-dd");
        DateString          = format.format(date);

        cbx_time.setText(format.format(date));
        cbx_time.setOnClickListener(this);

        img_list_background= (ImageView) findViewById(R.id.img_list_background);
        img_list_background.setVisibility(View.GONE);
        img_list_background.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (bbxxPopWindow != null && bbxxPopWindow.isShowing()) {
                    bbxxPopWindow.dismiss();
                }

                if (mOrgPopupWindow != null && mOrgPopupWindow.isShowing()) {
                    mOrgPopupWindow.dismiss();
                }
                return true;
            }
        });
        bbxxPopWindow = new BbxxPopWindow(getApplicationContext(), new BbxxPopWindow.MyCallback() {
            @Override
            public void myShow() {
                img_list_background.setVisibility(View.VISIBLE);
            }

            @Override
            public void myDismiss() {
                img_list_background.setVisibility(View.GONE);
                ReCheckBox();
            }
        });

        if (onlyDateSelect) {
            tv_orgTitle.setVisibility(View.GONE);
            rightButton.setVisibility(View.GONE);
        }

        //有网加载webview，没网隐藏
//        if (NetState.getNetState(this)) {
//            initWebView(DateString, memberId);
//        } else {
//            WebViewUtil.hideWebView(mWebView, frameLayout, new WebViewUtil.OnReLoadWebViewCallBack() {
//                @Override
//                public void reLoadWebView() {
//                    initWebView(DateString, memberId);
//                }
//            });
//        }
    }

    public void addEventListener () {
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(ActivityReportHistory.this, ActivityUserLoc.class);
//                intent.putExtra("lat", list_user.get(position).getLocation().getLat());
//                intent.putExtra("lng", list_user.get(position).getLocation().getLng());
//                startActivity(intent);
//            }
//        });

        rightButton.setOnClickListener(this);
        tv_orgTitle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_time:
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int year        = c.get(Calendar.YEAR);
                int monthOfYear = c.get(Calendar.MONTH);
                int dayOfMonth  = c.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(ActivityReportHistory.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        DateString = "" + year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                        cbx_time.setText(DateString);
                    }
                },year,monthOfYear,dayOfMonth).show();

                initWebView();
                break;

            case R.id.tv_orgTitle:
                img_list_background.setVisibility(View.VISIBLE);
                mOrgPopupWindow.showAsDropDown(tv_orgTitle, 0, 1);
                break;

            case R.id.rightButton:
                //跳转到客服界面
                Intent intent = new Intent(ActivityReportHistory.this, ActivityMain.class);
                intent.putExtra("ServicePage", SERVICE_PAGE);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    /**
     * 初始化webView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(){
        String postDate = null;
        postDate = "Date=" + DateString + "&MemberId=" + memberId + "&OrgId=" + orgId;
        String url = APIConstant.GetUpdatePositionReport;

        //不使用缓存，只从网络获取数据
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置页面支持Javascript
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                if (NetState.getNetState(ActivityReportHistory.this)) {
//                    WebViewUtil.showWebView(mWebView, frameLayout);
//                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                if (NetState.getNetState(ActivityReportHistory.this)) {
//                    WebViewUtil.hideWebView(mWebView, frameLayout, new WebViewUtil.OnReLoadWebViewCallBack() {
//                        @Override
//                        public void reLoadWebView() {
//                            initWebView(DateString, memberId);
//                        }
//                    });
//                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();  // 接受所有网站的证书
                initWebView();
            }
        });
        mWebView.postUrl(url, EncodingUtils.getBytes(postDate, "base64"));
//        mWebView.loadUrl(url);
        //调用JavaScript
        mWebView.addJavascriptInterface(new JSBridge(), "Android");
    }

    /**
     * JavaScript
     */
    public class JSBridge {
        @JavascriptInterface
        public void onItemClicked(String userId, String lat, String lng) {
            double latD = Double.parseDouble(lat);
            double lngD = Double.parseDouble(lng);

            Intent intent = new Intent(ActivityReportHistory.this, ActivityUserLoc.class);

            intent.putExtra("lat", latD);
            intent.putExtra("lng", lngD);

            startActivity(intent);
        }
    }

    /**
     * 获取位置报告列表
     */
    private void GetUserLocList(){
       // swipeToLoadLayout.setRefreshing(true);
        page=1;
        jsonObject=new JSONObject();

        try {
            jsonObject.put("MemberId",memberId);
            jsonObject.put("OrgId",orgId);
            jsonObject.put("pageIndex",page);
            jsonObject.put("pageSize",SIZE);
            jsonObject.put("Date",DateString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url= APIConstant.USUNG_HOST+APIConstant.GetUserLocList;
        OkHttpUtils.postString().url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(jsonObject.toString())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                dismissDialog();
            }

            @Override
            public void onResponse(String response) {
                dismissDialog();
                ResponseUtil.dealResponse(response, new DealCallBacks() {
                    @Override
                    public void onSuccess(String items, int total) {
                        list_user = GsonHelper.getGson().fromJson(items, new TypeToken<ArrayList<UserPosition>>(){}.getType());
                       if (list_user!=null) {
//                           mAdapter = new UserLocListAdapter(ActivityReportHistory.this, list_user);
                       }
//                        listView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onFailure(int error, String msg) {

                    }
                });
            }
        });
    }


    @Override
    public void onRefresh() {
        GetUserLocList();
    }

    //重置ChenckBox选中状态
    protected void ReCheckBox(){
        cbx_time.setChecked(false);
    }

    /**
     * 上拉刷新位置报告历史列表
     */
    @Override
    public void onLoadMore() {
        try {
            jsonObject.put("MemberId",memberId);
            jsonObject.put("OrgId",orgId);
            jsonObject.put("pageIndex",page);
            jsonObject.put("pageSize",SIZE);
            jsonObject.put("Date",DateString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url= APIConstant.USUNG_HOST+APIConstant.GetUserLocList;
        OkHttpUtils.postString().url(url)
                .mediaType(MediaType.parse(getString(R.string.media_type)))
                .content(jsonObject.toString())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showToastResId(ActivityReportHistory.this, R.string.http_failure, Toast.LENGTH_SHORT);
                swipeToLoadLayout.setLoadingMore(false);
            }

            @Override
            public void onResponse(String response) {
                swipeToLoadLayout.setLoadingMore(false);
                ResponseUtil.dealResponse(response, new DealCallBacks() {
                    @Override
                    public void onSuccess(String items, int total) {
                        list_user = GsonHelper.getGson().fromJson(items, new TypeToken<ArrayList<UserPosition>>() {
                        }.getType());
                        if (list_user!=null){
                            mAdapter.addUserLoc(list_user);
                        }else {
                            ToastUtil.showToastMessageString(ActivityReportHistory.this,"没有更多信息了",Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onFailure(int error, String msg) {

                    }
                });
            }
        });

    }

    /**
     * 获取我的企业列表
     */
    public void getMyOrgList() {
        ShowLoading(getString(R.string.please_waitting));
        OkHttpUtils.get().url(APIConstant.USUNG_HOST + APIConstant.GetUserEPList)
                .addParams("confirmState", "true")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                dismissDialog();
                ToastUtil.showToastResId(ActivityReportHistory.this, R.string.network_not_activated, Toast.LENGTH_SHORT);
                finish();
            }

            @Override
            public void onResponse(final String response) {
//                Log.e("---getMyOrgList", response);
                dismissDialog();
                ResponseUtil.dealResponse(response, new DealCallBacks() {
                    @Override
                    public void onSuccess(String items, int total) {
                        list_ep = GsonHelper.getGson().fromJson(items, new TypeToken<ArrayList<EPBase>>() {
                        }.getType());
                        if (list_ep != null && list_ep.size() != 0) {
//                            for (FragmentTeamManage f : fragments) {
//                                f.setOrgInfo(list_ep.get(orgIndex));
//                            }
                            tv_orgTitle.setText(list_ep.get(orgIndex).getOrgName());
                            initOrgListPopupWindow();
                            //getPurview();
                        } else {
                            new AlertDialog(ActivityReportHistory.this).builder()
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
                    }

                    @Override
                    public void onFailure(int error, String msg) {
                        ToastUtil.showToastMessageString(ActivityReportHistory.this, msg, Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }

    /**
     * 初始化公司选择列表popupWindow
     */
    private void initOrgListPopupWindow() {
        ListView popupLv = new ListView(this);
        popupLv.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        popupLv.setDividerHeight(0);
        popupLv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        popupLv.setSelector(android.R.color.transparent);
        popupLv.setVerticalScrollBarEnabled(false);
        mOrgPopupWindow = new PopupWindow(popupLv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(0x00000000); // 实例化一个ColorDrawable颜色为半透明
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景；使用该方法点击窗体之外，才可关闭窗体
        mOrgPopupWindow.setBackgroundDrawable(dw); // Background不能设置为null，dismiss会失效
        mOrgPopupWindow.setOutsideTouchable(true);
        mOrgPopupWindow.setFocusable(true);
        mOrgPopupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        mTeamOrgListAdapter = new TeamOrgListAdapter(this, list_ep, orgIndex);
        popupLv.setAdapter(mTeamOrgListAdapter);
        popupLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//当选择某个企业后
                onOrgSelected(position);
            }
        });
        mOrgPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_list_background.setVisibility(View.GONE);
            }
        });
        onOrgSelected(position);
    }

    /**
     * 当企业被选中
     */
    private void onOrgSelected(int position) {
        orgIndex = position;
        mTeamOrgListAdapter.setDataChanged(list_ep, orgIndex);
        tv_orgTitle.setText(list_ep.get(orgIndex).getOrgName());
        orgId = list_ep.get(orgIndex).getOrgId();
        initWebView();
        //getPurview();
        mOrgPopupWindow.dismiss();
    }
}
