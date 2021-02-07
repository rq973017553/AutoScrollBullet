package com.rq.autoscrollbullet.ui;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rq.autoscrollbullet.R;
import com.rq.autoscrollbullet.data.Bullet;
import com.rq.autoscrollbullet.utils.ImageLoader;
import com.rq.autoscrollbullet.widgets.SampleScrollBulletView;
import com.rq.floatingbullet.widgets.IBulletHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SampleScrollBulletView autoScrollBulletView;

    private SimpleDraweeView background;

    private static final Handler TEST_HANDLER = new Handler();

    private static final int HEAD_PORTRAITS_ARRAY[] = new int[]{
            R.drawable.head_portrait000,
            R.drawable.head_portrait001,
            R.drawable.head_portrait002,
            R.drawable.head_portrait003,
            R.drawable.head_portrait004,
            R.drawable.head_portrait005,
            R.drawable.head_portrait006,
            R.drawable.head_portrait007,
            R.drawable.head_portrait008,
            R.drawable.head_portrait009,
            R.drawable.head_portrait010
    };

    private static final String NICK_NAME_ARRAY[] = new String[]{
            "rockyou666",
            "蛾眉轻敛つ袖舞流年",
            "一场唯美的骗局",
            "你瞅啥瞅",
            "绾青丝",
            "水流云在",
            "叶落琴声萧ゝ",
            "寿终正寝≡",
            "只我一人",
            "对不起、该好友已删除i",
            "许是故人来"
    };

    private static final String COMMENT_ARRAY[] = new String[]{
            "故事套故事，真真假假是虚是实，各种可能。很奇特的构思。",
            "戏中戏讲得略平淡，为啥觉得白白复制了一遍limitless里的表演？都是潦倒男一夜之间大开金手指的故事，limitless好像还好玩点儿",
            "除了结构还算有点意思之外，整个故事毫无新意，内容陈旧、感情空洞，充满了泛泛而谈的敷衍、说教和不知所云。当然也可能是在没什么版权意识的地方生活太久，所以不能深刻体会侵权的形而上学意味。",
            "悬念弱了点，但两个故事的男主都不可方物哇",
            "铺垫了好多，到最后才发现他其实什么都没讲，就想占用你点时间。不是每段失败的人生都能总结点意义出来的，你失败了，他也失败了，该失败的还是会失败，别挣扎了。",
            "想当文艺青年是要付出代价的",
            "果然是编剧转导演，以作者与故事的形式，让三层故事嵌套。三个故事本是各自独立，又有意模糊界限，让你觉得他们都来自于真实。除了比较明显的第二层与第三层，连第一层与第二层也被模糊了。 当然，故事并不复杂，每一层都有叙事人与听众。做出选择并承受选择",
            "中规中矩 这时间段拉的很长 还有三段时间。。。",
            "三段故事层层相套，算是有点哲理味道。不过可惜的是三个故事都流于平庸，少有惊艳之感。巴瑞斯和库珀的表演在艾恩斯面前显得十分青涩呆板。不过值得称道的是中间回忆战时巴黎的一段采用胶片拍摄，质感极好，光影加分。",
            "好交错 作家就是一份摧残人意志的工作",
            "三个故事，故事中的故事，故事本身很老套，但老套的故事再套起来就有点意思。Zoe气质很好。Cooper本片演技有佳，化妆师也很给力。大叔化成正太相当到位。",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        autoScrollBulletView = findViewById(R.id.auto_scroll_bullet_view);
        background = findViewById(R.id.background);
        ImageLoader.showUrlBlur(background, R.drawable.head_portrait000, 5, 5);
        // 加载弹幕
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadBulletData();
            }
        }).start();
    }

    private void loadBulletData(){
        final List<Bullet> bullets = new ArrayList<>();
        for (int i =0; i < 11; i ++){
            Bullet bullet = new Bullet();
            bullet.setHeadPortrait(HEAD_PORTRAITS_ARRAY[i]);
            bullet.setCommentNickName(NICK_NAME_ARRAY[i]);
            bullet.setComment(COMMENT_ARRAY[i]);
            bullets.add(bullet);
        }
        TEST_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                autoScrollBulletView.addAllData(bullets, new IBulletHelper<Bullet>() {
                    @Override
                    public View getView(Bullet bullet) {
                        View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.comment, null);
                        SimpleDraweeView commentPortrait = rootView.findViewById(R.id.comment_head_portrait);
                        TextView commentName = rootView.findViewById(R.id.comment_nick_name);
                        TextView commentContent = rootView.findViewById(R.id.comment_content);

                        ImageLoader.loadFromID(commentPortrait, bullet.getHeadPortrait());
                        commentName.setText(bullet.getCommentNickName());
                        commentContent.setText(bullet.getComment());
                        return rootView;
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (autoScrollBulletView != null){
//            autoScrollBulletView.resumeScroll();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (autoScrollBulletView != null){
//            autoScrollBulletView.pauseScroll();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoScrollBulletView != null){
            autoScrollBulletView.stopScroll();
        }
    }
}