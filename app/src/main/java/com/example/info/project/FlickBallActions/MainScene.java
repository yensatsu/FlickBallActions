package com.example.info.project.FlickBallActions;

import android.util.Log;
import android.view.KeyEvent;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.input.touch.TouchEvent;

//public class MainScene extends Scene {


public class MainScene extends KeyListenScene implements IOnSceneTouchListener{

    private AnimatedSprite coin;

//    ドラッグ開始座標
    private float[] touchStartPoint;
//    画面をタッチしているか
    private boolean isTouchEnabled;
//    ドラッグ中か否か
    private boolean isDragging;
//    コインが飛翔中か
    private boolean isCoinFlying;

//    コインが飛び出す角度
    private double flyAngle;
//    コインのx座標移動速度
    private float flyXVelocity;
//    コインのｙ座標移動速度
    private float initialCoinSpeed;


//    private BaseGameActivity baseActivity;
//    private MultiSceneActivity baseActivity;
//    private ResourceUtil resourceUtil;

    public MainScene(MultiSceneActivity baseActivity){
        super(baseActivity);
//        this.baseActivity = baseActivity;
        init();
    }


    public void init() {
//        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
//        BitmapTextureAtlas bta = new BitmapTextureAtlas(
//                baseActivity.getTextureManager(),512,1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA
//        );
//        baseActivity.getTextureManager().loadTexture(bta);
//        ITextureRegion btr
//                = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
//                        bta,baseActivity,"main_bg.png",0,0);
//        Sprite bg = new Sprite
//                (0,0,btr,baseActivity.getVertexBufferObjectManager());
//        bg.setBlendFunction(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
//        attachChild(bg);

//        resourceUtil = ResourceUtil.getInstance(baseActivity);
//        attachChild(resourceUtil.getSprite("main_bg.png"));
        attachChild(getBaseActivity().getResourceUtil().getSprite("main_bg.png"));

//        フラグ初期位置
        isTouchEnabled = true;
        isDragging = false;
        isCoinFlying = false;

//        コインのｙ座標移動の初速度
        initialCoinSpeed = 30F;

       touchStartPoint = new float[2];
//       Sceneのタッチリスナー登録
        setOnSceneTouchListener(this);
//        アップデートハンドラーを登録
        registerUpdateHandler(updateHandler);

        setNewCoin();

    }

    private void setNewCoin() {
//        古いコインが存在する場合削除
        if (coin != null){
            detachChild(coin);
        }
//        コインのインスタンス化
        coin = getBaseActivity().getResourceUtil().getAnimatedSprite("coin_100.png",1,3);
//        x座標を画面中心 yを600に設定
        placeToCenterX(coin,600);
//        1コマ50msのアニメーションを開始
//        coin.animate(50);

        attachChild(coin);
    }

    @Override
    public void prepareSoundAndMusic() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        return false;
    }

//    タッチイベントが発生で呼ばれる
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent){
//        タッチの座標取得
        float x = pSceneTouchEvent.getX();
        float y = pSceneTouchEvent.getY();
//        指が触れた瞬間のイベント
//        タッチの座標がコインの上？
        if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN
                && isTouchEnabled
                && (x > coin.getX() && x < coin.getX() + coin.getWidth())
                && (y > coin.getY() && y < coin.getY() + coin.getHeight())){

//            フラグ
            isTouchEnabled = false;
            isDragging = true;

//            開始点を登録
            touchStartPoint[0] = x;
            touchStartPoint[1] = y;

        }

//        何らかの原因でタッチイベントが中断した場合の処理
        else if ((pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP
                || pSceneTouchEvent.getAction() == TouchEvent.ACTION_CANCEL)
                && isDragging)
        {

//            指が離れた地点
            float[] touchEndPoint = new float[2];
            touchEndPoint[0] = x;
            touchEndPoint[1] = y;

//            フリックが短い場合は中断
            if ((touchEndPoint[0] - touchStartPoint[0] > 50 && touchEndPoint[0] -touchStartPoint[0] > -50)
                    &&(touchEndPoint[1] - touchStartPoint[1] > 50 && touchEndPoint[1] - touchStartPoint[1] > -50)){

                isTouchEnabled = true;
                isDragging = false;

                return true;
            }

//            フリック角度
            flyAngle = getAngleByTwoPosition(touchStartPoint,touchEndPoint);
//            下から上へのフリックを0°に調整
            flyAngle -= 180;

//            フリックの角度が前向きではないときフリックを無効に
            if (flyAngle < -80 || flyAngle > 80)
            {
                isTouchEnabled = true;
                isDragging = false;
                return true;
            }

//            出力
            Log.d("ae","angle:::" + flyAngle);

//            コインのｘ座標移動速度を調整
            flyXVelocity = (float)(flyAngle / 10.0F);
//            フラグをOnにする
            isCoinFlying = true;
//            アニメーション開始
            coin.animate(50);
//            コインの角度をランダムに設定
            coin.setRotation((int)(Math.random() * 360));

        }
        return true;
    }

//    アップデートハンドラー　1秒間に60回呼び出される
    public TimerHandler updateHandler = new TimerHandler(
        1F / 60F, true, new ITimerCallback() {
    @Override
    public void onTimePassed(TimerHandler pTimerHandler) {
//        コインが飛んでいるなら実行
        if (isCoinFlying)
        {
//            コインのX座標移動速度が３以上の時
            if (initialCoinSpeed > 3.0F)
            {
//                速度を落とす
                initialCoinSpeed *= 0.96F;
            }
//            コインを小さく
            coin.setScale(coin.getScaleX() * 0.97F);
//            x座標の移動
            coin.setX(coin.getX() + flyXVelocity);
//            ｘの移動量を徐々に大きく
            flyXVelocity *= 1.03F;
//            y座標の移動
            coin.setY(coin.getY() - initialCoinSpeed);
        }
    }
});

//    2点間の角度を求める
    private double getAngleByTwoPosition(float[] start,float[] end) {
        double result = 0;

        float xDistance = end[0] - start[0];
        float yDistance = end[1] = start[1];

        result = Math.atan2((double) yDistance,(double)xDistance) * 180 / Math.PI;
        result += 270;
        return result;
    }


}
