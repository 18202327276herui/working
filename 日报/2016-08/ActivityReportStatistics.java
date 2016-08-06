package com.usung.smarttradetwo.activity.team;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.usung.smarttradetwo.R;
import com.usung.smarttradetwo.activity.user.ActivityMain;
import com.usung.smarttradetwo.adapter.team.RightChooseAdapter;
import com.usung.smarttradetwo.adapter.team.TeamDepListAdapter;
import com.usung.smarttradetwo.adapter.team.TeamOrgListAdapter;
import com.usung.smarttradetwo.base.BaseActivity;
import com.usung.smarttradetwo.bean.team.Department;
import com.usung.smarttradetwo.bean.team.EPBase;
import com.usung.smarttradetwo.utils.APIConstant;
import com.usung.smarttradetwo.utils.APPConstants;
import com.usung.smarttradetwo.utils.GsonHelper;
import com.usung.smarttradetwo.utils.NetWorkUtil;
import com.usung.smarttradetwo.utils.ToastUtil;
import com.usung.smarttradetwo.utils.WebViewUtil;
import com.usung.smarttradetwo.utils.okhttp.DealCallBacks;
import com.usung.smarttradetwo.utils.okhttp.ResponseUtil;
import com.usung.smarttradetwo.widgets.AlertDialog;
import com.usung.smarttradetwo.widgets.BbxxPopWindow;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;

/**
 * @author herui 2016/7/13
 * 企业获取员工位置报告统计
 *
 */
public class ActivityReportStatistics extends BaseActivity {

    private String depId;       //部门ID
    private String memberId;    //员工ID
    private String orgId;       //企业ID
    private String DateString;
    private static final int SERVICE_PAGE = 1;

    private RelativeLayout  rl_next;
    private TextView tv_departmentTitle;
    private CheckBox cbx_time;
    private FrameLayout frameLayout;
    private WebView mWebView;
    private ImageView img_list_background;
    private BbxxPopWindow bbxxPopWindow;
    //部门列表
    private ArrayList<Department> list_department;
    //当前所选部门下标
    private int depIndex = 0;
    //选择部门的popupWindow
    private PopupWindow mDepPopupWindow;
    private TeamDepListAdapter mTeamDepListAdapter;
    //我的公司列表
    private ArrayList<EPBase> list_ep;
    //当前所选公司下标
    private int orgIndex = 0;
    //选择公司的popupWindow
    private PopupWindow mOrgPopupWindow;
    private TeamOrgListAdapter mTeamOrgListAdapter;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_report_statistics);

        memberId = getIntent().getExtras().getString("Id");
        orgId = getIntent().getExtras().getString("orgId");
        position = getIntent().getExtras().getInt("position", 0);

        initViews();
        addEventListener();
        getMyOrgList();
        initWebView();
    }

    @Override
    protected void initViews() {
        super.initViews();

        ShowLoading(getString(R.string.please_waitting));
        title.setText("位置报告统计");

        Drawable drawable = getResources().getDrawable(R.mipmap.arrow_down_white);
        drawable.setBounds(0, 0 ,24, 12);
        title.setCompoundDrawables(null, null, drawable, null);

        rl_next = (RelativeLayout) findViewById(R.id.rl_next);
        tv_departmentTitle = (TextView) findViewById(R.id.tv_departmentTitle);
        cbx_time = (CheckBox) findViewById(R.id.btn_time);
        rightButton = (Button) findViewById(R.id.rightButton);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mWebView = (WebView) findViewById(R.id.mWebView);
        img_list_background = (ImageView) findViewById(R.id.img_list_background);

        rightButton.setBackgroundResource(R.mipmap.phone_white);

        //获取当前日期
        Date date = new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        DateString = format.format(date);
        cbx_time.setText(DateString);
        cbx_time.setOnClickListener(this);

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

                if (mDepPopupWindow != null && mDepPopupWindow.isShowing()) {
                    mDepPopupWindow.dismiss();
                }
                return true;
            }
        });

        bbxxPopWindow=new BbxxPopWindow(getApplicationContext(), new BbxxPopWindow.MyCallback() {
            @Override
            public void myShow() {
                img_list_background.setVisibility(View.VISIBLE);
            }

            @Override
            public void myDismiss() {
                img_list_background.setVisibility(View.GONE);
               // ReCheckBox();
            }
        });

//        //有网加载webview，没网隐藏
//        if (NetWorkUtil.getNetState(this)) {
//            initWebView();
//        } else {
//            WebViewUtil.hideWebView(mWebView, frameLayout, new WebViewUtil.OnReLoadWebViewCallBack() {
//                @Override
//                public void reLoadWebView() {
//                    initWebView();
//                }
//            });
//        }
    }

    public void addEventListener() {
        rightButton.setOnClickListener(this);
        tv_departmentTitle.setOnClickListener(this);
        title.setOnClickListener(this);
        rl_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rightButton:
                //跳转到客服界面
                Intent intent = new Intent(ActivityReportStatistics.this, ActivityMain.class);
                intent.putExtra("ServicePage", SERVICE_PAGE);
                startActivity(intent);
                break;

            case R.id.btn_time:
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int year        = c.get(Calendar.YEAR);
                int monthOfYear = c.get(Calendar.MONTH);
                int dayOfMonth  = c.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(ActivityReportStatistics.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        DateString=""+year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
                        cbx_time.setText(DateString);
                    }
                },year,monthOfYear,dayOfMonth).show();
                initWebView();
                break;

            case R.id.tv_departmentTitle:
                img_list_background.setVisibility(View.VISIBLE);
                mDepPopupWindow.showAsDropDown(tv_departmentTitle, 0, 1);
                break;

            case R.id.header_title:
                img_list_background.setVisibility(View.VISIBLE);
                mOrgPopupWindow.showAsDropDown(title, 0, 1);
                break;

            case R.id.rl_next:
                Intent intentHistory = new Intent(ActivityReportStatistics.this, ActivityReportHistory.class);
                intentHistory.putExtra("Id", memberId);
                intentHistory.putExtra("OrgId", orgId);
                intentHistory.putExtra("onlyDateSelect", true);
                startActivity(intentHistory);
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
        postDate = "Date=" + DateString + "&MemberId=" + memberId + "&OrgId=" + orgId + "&DeptId" + depId;
        String url = APIConstant.GetUpdatePositionReport;

        //不使用缓存，只从网络获取数据
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置页面支持Javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                if (NetState.getNetState(ActivityReportStatistics.this)) {
//                    WebViewUtil.showWebView(mWebView, frameLayout);
//                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                if (NetState.getNetState(ActivityReportStatistics.this)) {
//                    WebViewUtil.hideWebView(mWebView, frameLayout, new WebViewUtil.OnReLoadWebViewCallBack() {
//                        @Override
//                        public void reLoadWebView() {
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

        mWebView.postUrl(url, Base64.encode(EncodingUtils.getBytes(postDate, "base64"), Base64.DEFAULT));
//        mWebView.postUrl(url, EncodingUtils.getBytes(postDate, "base64"));
//        mWebView.loadUrl("http://baidu.com");
        //调用JavaScript
        mWebView.addJavascriptInterface(new JSBridge(), "Android");
    }

    /**
     * JavaScript
     */
    public class JSBridge {
        @JavascriptInterface
        public void onItemClicked(String userId, String lat, String lng) {

            Intent intent = new Intent(ActivityReportStatistics.this, ActivityReportHistory.class);
            intent.putExtra("Id", userId);
            intent.putExtra("OrgId", orgId);
            intent.putExtra("onlyDateSelect", true);
            startActivity(intent);
        }
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
                ToastUtil.showToastResId(ActivityReportStatistics.this, R.string.network_not_activated, Toast.LENGTH_SHORT);
                finish();
            }

            @Override
            public void onResponse(final String response) {
                ResponseUtil.dealResponse(response, new DealCallBacks() {
                    @Override
                    public void onSuccess(String items, int total) {
                        list_ep = GsonHelper.getGson().fromJson(items, new TypeToken<ArrayList<EPBase>>() {
                        }.getType());
                        if (list_ep != null && list_ep.size() != 0) {
//                            for (FragmentTeamManage f : fragments) {
//                                f.setOrgInfo(list_ep.get(orgIndex));
//                            }
                            title.setText(list_ep.get(orgIndex).getOrgName());
                            initOrgListPopupWindow();
                            getDepartmentList(list_ep.get(orgIndex).getOrgId());
                            //getPurview();
                        } else {
                            new AlertDialog(ActivityReportStatistics.this).builder()
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
                        dismissDialog();
                        ToastUtil.showToastMessageString(ActivityReportStatistics.this, msg, Toast.LENGTH_SHORT);
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
    }

    /**
     * 当企业被选中
     */
    private void onOrgSelected(int position) {
        orgIndex = position;
        mTeamOrgListAdapter.setDataChanged(list_ep, orgIndex);
        title.setText(list_ep.get(orgIndex).getOrgName());
        ShowLoading(getString(R.string.please_waitting));
        getDepartmentList(list_ep.get(orgIndex).getOrgId());
        depIndex = 0;
        orgId = list_ep.get(orgIndex).getOrgId();
        initWebView();
        //getPurview();
        mOrgPopupWindow.dismiss();
    }

    /**
     * 获取部门列表
     */
    private void getDepartmentList(String orgId) {
        OkHttpUtils.get().url(APIConstant.USUNG_HOST + APIConstant.GetChildDepart)
                .addParams("orgId", orgId)
                .addParams("pId", "")
                .addParams("queryType", APPConstants.DEPTTYPE_UNASSIGNED + APPConstants.DEPTTYPE_ASSIGNED + APPConstants.DEPTTYPE_RELATION + "")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ToastUtil.showToastResId(ActivityReportStatistics.this, R.string.http_failure, Toast.LENGTH_SHORT);
                dismissDialog();
            }

            @Override
            public void onResponse(final String response) {
                dismissDialog();
                ResponseUtil.dealResponse(response, new DealCallBacks() {
                    @Override
                    public void onSuccess(String items, int total) {
                        list_department = GsonHelper.getGson().fromJson(items, new TypeToken<ArrayList<Department>>() {
                        }.getType());
                        tv_departmentTitle.setText(list_department.get(depIndex).getName());
                        initDepListPopupWindow();
                    }

                    @Override
                    public void onFailure(int error, String msg) {
                        ToastUtil.showToastMessageString(ActivityReportStatistics.this, msg, Toast.LENGTH_SHORT);
                        dismissDialog();
                    }
                });
            }
        });
    }

    /**
     * 初始化部门选择列表popupWindow
     */
    private void initDepListPopupWindow() {
        ListView popupLv = new ListView(this);
        popupLv.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        popupLv.setDividerHeight(0);
        popupLv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        popupLv.setSelector(android.R.color.transparent);
        popupLv.setVerticalScrollBarEnabled(false);
        mDepPopupWindow = new PopupWindow(popupLv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(0x00000000); // 实例化一个ColorDrawable颜色为半透明
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景；使用该方法点击窗体之外，才可关闭窗体
        mDepPopupWindow.setBackgroundDrawable(dw); // Background不能设置为null，dismiss会失效
        mDepPopupWindow.setOutsideTouchable(true);
        mDepPopupWindow.setFocusable(true);
        mDepPopupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        mTeamDepListAdapter = new TeamDepListAdapter(this, list_department, depIndex);
        popupLv.setAdapter(mTeamDepListAdapter);
        popupLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//当选择某个部门后
                onDepSelected(position);
            }
        });
        mDepPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                 img_list_background.setVisibility(View.GONE);
            }
        });
        onOrgSelected(position);
    }

    /**
     * 当部门被选中
     */
    private void onDepSelected(int position) {
        depIndex = position;
        mTeamDepListAdapter.setDataChanged(list_department, depIndex);
        tv_departmentTitle.setText(list_department.get(depIndex).getName());

        depId = list_department.get(depIndex).getDepartId();
        initWebView();

        //getPurview();
        mDepPopupWindow.dismiss();
    }

}
