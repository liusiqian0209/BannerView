package cn.liusiqian.banner;

import android.app.Activity;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import cn.liusiqian.bannerlib.BannerView;

public class MainActivity extends Activity
{
    private BannerView banner_l,banner_n;
    private List<Integer> drawableRes = Arrays.asList(R.drawable.pic1
            ,R.drawable.pic2,R.drawable.pic3,R.drawable.pic4,R.drawable.pic5,R.drawable.pic6,R.drawable.pic7);

    private List<String> urls = Arrays.asList(
            "http://5.26923.com/download/pic/000/282/37a5d7166255a9d5e5b635573edaf266.jpg",
            "http://img2.ph.126.net/JX11w9kZMl3HcRx4QuXmgg==/1755559429745871437.jpg",
            "http://img1.imgtn.bdimg.com/it/u=3223328830,3066003355&fm=15&gp=0.jpg",
            "http://img.poco.cn/mypoco/myphoto/20070718/104941620070718183731024_640.jpg",
            "http://a4.att.hudong.com/11/44/01300000247011123547440686870.jpg");
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        banner_l = (BannerView) findViewById(R.id.banner_local);
        banner_l.resize(640,480);
        banner_l.setImageDrawables(drawableRes);

        banner_n = (BannerView) findViewById(R.id.banner_net);
        banner_n.resize(640,480);
        banner_n.setImageUrls(urls);
    }
}
