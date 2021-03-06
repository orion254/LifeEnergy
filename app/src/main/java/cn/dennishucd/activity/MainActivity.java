package cn.dennishucd.activity;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import cn.dennishucd.FFmpeg4AndroidActivity;
import cn.dennishucd.R;
import cn.dennishucd.cache.BaseActivity;
import cn.dennishucd.menu.Desktop;
import cn.dennishucd.menu.Desktopr;
import cn.dennishucd.menu.Energy;
import cn.dennishucd.menu.Friends;
import cn.dennishucd.menu.Gifts;
import cn.dennishucd.menu.Group;
import cn.dennishucd.menu.Home;
import cn.dennishucd.menu.Interest;
import cn.dennishucd.menu.Message;
import cn.dennishucd.menu.Photo;
import cn.dennishucd.menu.User;
import cn.dennishucd.menu.Desktop.onChangeViewListener;
import cn.dennishucd.uibase.FlipperLayout;
import cn.dennishucd.uibase.FlipperLayout.OnOpenListener;
import cn.dennishucd.uibase.FlipperLayout.onUgcDismissListener;
import cn.dennishucd.uibase.FlipperLayout.onUgcShowListener;
import cn.dennishucd.utils.ActivityForResultUtil;
import cn.dennishucd.utils.Options;
import cn.dennishucd.utils.PhotoUtil;
import cn.dennishucd.utils.Utils;
import cn.dennishucd.utils.ViewUtil;
/**
 * 主资源
 * 
 * @author lqb
 * 
 */
public class MainActivity extends BaseActivity implements OnOpenListener {
	/**
	 * 当前显示内容的容器(继承于ViewGroup)
	 */
	private FlipperLayout mRoot;
	/**
	 * 左菜单界面
	 */
	private Desktop mDesktop;
	/**
	 * 右菜单界面
	 */
	private Desktopr mDesktopr;
	/**
	 * 用户首页界面
	 */
	private User mUserInfo;
	/**
	 * 内容首页界面
	 */
	private Home mHome;
	/**
	 * 好友首页界面
	 */
	private Friends mFriends;
	/**
	 * 消息首页界面
	 */
	private Message mMessages;
	/**
	 * 能量首页界面
	 */
	private Energy mEnergy;
	/**
	 * 照片首页界面
	 */
	private Photo mPhoto;
	/**
	 * 礼物首页界面
	 */
	private Gifts mGifts;
	/**
	 * 兴趣界面
	 */
	private Interest mInterest;
	/**
	 * 小组界面
	 */
	private Group mGroup;
	/**
	 * 当前显示的View的编号
	 */
	private int mViewPosition;
	/**
	 * 退出时间
	 */
	private long mExitTime;
	/**
	 * 退出间隔
	 */
	private static final int INTERVAL = 2000;
	/**
	 * 
	 */
	public LayoutParams params ;
	public static Activity mInstance;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * 创建容器,并设置全屏大小
		 */
		mKXApplication.options = Options.getListOptions();
		mKXApplication.imageLoader = ImageLoader.getInstance();
		mKXApplication.imageLoader.init(ImageLoaderConfiguration.createDefault(MainActivity.this));
		mRoot = new FlipperLayout(this);
		params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mRoot.setLayoutParams(params);
		/**
		 * 创建菜单界面和内容首页界面,并添加到容器中,用于初始显示
		 */
		mDesktopr = new Desktopr(this,this,mKXApplication);
		mHome = new Home(this, this, mKXApplication);
		mDesktop = new Desktop(this, this, mKXApplication);
		mRoot.addView(mDesktop.getView(),params);
		mRoot.addView(mDesktopr.getView(),params);
		mRoot.addView(mHome.getView(), params);
		setContentView(mRoot);
		setListener();
		mInstance=this;
		
	}

	/**
	 * UI事件监听
	 */
	private void setListener() {
		mHome.setOnOpenListener(this);
		/**
		 * 隐藏path菜单
		 */
		mDesktop.setOnChangeViewListener(new MyChangeViewAdapter());
		mDesktopr.setOnChangeViewListener(new MyChangeViewAdapterr());
		mRoot.setOnUgcDismissListener(new onUgcDismissListener() {
			
			public void dismiss() {
				switch (mViewPosition) {
				case ViewUtil.USER:
					mUserInfo.dismissUgc();
					break;

				case ViewUtil.HOME:
					mHome.dismissUgc();
					break;
				}
			}
		});
		/**
		 * 显示path菜单
		 */
		mRoot.setOnUgcShowListener(new onUgcShowListener() {

			public void show() {
				switch (mViewPosition) {
				case ViewUtil.USER:
					mUserInfo.showUgc();
					break;

				case ViewUtil.HOME:
				mHome.showUgc();
					break;
				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		/**
		 * 发表新动态时回调此方法,跳转到首页好友动态
		 */
		case ActivityForResultUtil.REQUESTCODE_UPLOADVIDEO_CAMERA:
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "能量发射", Toast.LENGTH_SHORT).show();
				mRoot.removeAllViews();
				mRoot.addView(mDesktop.getView(),params);
				mRoot.addView(mHome.getView(), params);
				mHome.setCurView();
			}
			break;
		/**
		 * 切换墙纸时回调此方法,通知菜单界面和用户界面修改墙纸
		 */
		case ActivityForResultUtil.REQUESTCODE_CHANGEWALLPAGER:
			if (resultCode == RESULT_OK) {
				mDesktop.setWallpager();
				mUserInfo.setWallpager();
			}
			break;
		/**
		 * 修改签名时回调此方法,通知菜单界面和用户界面修改签名
		 */
		case ActivityForResultUtil.REQUESTCODE_EDITSIGNATURE:
			if (resultCode == RESULT_OK) {
				String arg0 = data.getStringExtra("signature");
				mDesktop.setSignature(arg0);
				mUserInfo.setSignature(arg0);
			}
			break;
		/**
		 * 通过照相修改头像
		 */
		case ActivityForResultUtil.REQUESTCODE_UPLOADAVATAR_CAMERA:
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast.makeText(this, "SD不可用", Toast.LENGTH_SHORT).show();
					return;
				}
				File file = new File(mKXApplication.mUploadPhotoPath);
				startPhotoZoom(Uri.fromFile(file));
			} else {
				Toast.makeText(this, "取消上传", Toast.LENGTH_SHORT).show();
			}
			break;
		/**
		 * 通过本地修改头像
		 */
		case ActivityForResultUtil.REQUESTCODE_UPLOADAVATAR_LOCATION:
			Uri uri = null;
			if (data == null) {
				Toast.makeText(this, "取消上传", Toast.LENGTH_SHORT).show();
				return;
			}
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast.makeText(this, "SD不可用", Toast.LENGTH_SHORT).show();
					return;
				}
				uri = data.getData();
				startPhotoZoom(uri);
			} else {
				Toast.makeText(this, "照片获取失败", Toast.LENGTH_SHORT).show();
			}
			break;
		/**
		 * 裁剪修改的头像
		 */
		case ActivityForResultUtil.REQUESTCODE_UPLOADAVATAR_CROP:
			if (data == null) {
				Toast.makeText(this, "取消上传", Toast.LENGTH_SHORT).show();
				return;
			} else {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap bitmap = extras.getParcelable("data");
					bitmap = PhotoUtil.toRoundCorner(bitmap, 15);
					if (bitmap != null) {
						mUserInfo.setAvatar(bitmap);
						mDesktop.setAvatar(bitmap);
					}
				} else {
					Toast.makeText(this, "获取裁剪照片错误", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		/**
		 * 通过照相上传图片
		 */
		case ActivityForResultUtil.REQUESTCODE_UPLOADPHOTO_CAMERA:
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast.makeText(this, "SD不可用", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intents = new Intent();
				intents.setClass(MainActivity.this, ImageFilterActivity.class);
				String path = PhotoUtil.saveToLocal(PhotoUtil.createBitmap(
						mKXApplication.mUploadPhotoPath, mScreenWidth,
						mScreenHeight));
				intents.putExtra("path", path);
				startActivity(intents);
			} else {
				Toast.makeText(this, "取消上传", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	/**
	 * 系统裁剪照片
	 * 
	 * @param uri
	 */
	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("scale", true);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", true);
		startActivityForResult(intent,
				ActivityForResultUtil.REQUESTCODE_UPLOADAVATAR_CROP);
	}

	/**
	 * 返回键监听
	 */
	public void onBackPressed() {
		/**
		 * 如果界面的path菜单没有关闭时,先将path菜单关闭,否则则判断两次返回时间间隔,小于两秒则退出程序
		 */
		if (mRoot.getScreenState() == FlipperLayout.SCREEN_STATE_OPEN) {
			if (mDesktop.getUgcIsShowing()) {
				mDesktop.closeUgc();
			} else {
				exit();
			}
		} else {
			switch (mViewPosition) {
			case ViewUtil.USER:
				if (mUserInfo.getUgcIsShowing()) {
					mUserInfo.closeUgc();
				} else {
					exit();
				}
				break;
			case ViewUtil.HOME:
				if (mHome.getUgcIsShowing()) {
					mHome.closeUgc();
				} else {
					exit();
				}
				break;
			default:
				exit();
				break;
			}

		}

	}

	/**
	 * 判断两次返回时间间隔,小于两秒则退出程序
	 */
	private void exit() {
		if (System.currentTimeMillis() - mExitTime > INTERVAL) {
			Toast.makeText(this, "再按一次返回键,可直接退出程序", Toast.LENGTH_SHORT).show();
			mExitTime = System.currentTimeMillis();
		} else {
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		}
	}

	public void open() {
		if (mRoot.getScreenState() == FlipperLayout.SCREEN_STATE_CLOSE) {
			mRoot.removeAllViews();
			mRoot.addView(mDesktop.getView(),params);
			mRoot.addView(mHome.getView(), params);
			mRoot.open();
		}	
	}

	@Override
	public void openr() {
		// TODO Auto-generated method stub
		if (mRoot.getScreenState() == FlipperLayout.SCREEN_STATE_CLOSE) {
			mRoot.removeAllViews();
			mRoot.addView(mDesktopr.getView(),params);
			mRoot.addView(mHome.getView(), params);
			mRoot.openr();
		}
	}
	private class MyChangeViewAdapter implements cn.dennishucd.menu.Desktop.onChangeViewListener{
		
		@Override
		public void onChangeView(int arg0) {
			// TODO Auto-generated method stub
			mViewPosition = arg0;
			switch (arg0) {
			case ViewUtil.USER:
				if (mUserInfo == null) {
					mUserInfo = new User(MainActivity.this,
							MainActivity.this, mKXApplication);
					mUserInfo.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mUserInfo.getView());
				break;
			case ViewUtil.HOME:
				mRoot.close(mHome.getView());
				break;
			case ViewUtil.MESSAGES:
				if (mMessages == null) {
					mMessages = new Message(MainActivity.this);
					mMessages.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mMessages.getView());
				break;
			case ViewUtil.FRIENDS:
				if (mFriends == null) {
					mFriends = new Friends(MainActivity.this,
							mKXApplication);
					mFriends.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mFriends.getView());
				break;
			case ViewUtil.ENERGY:
				if (mEnergy == null) {
					mEnergy = new Energy(MainActivity.this,
							mKXApplication);
					mEnergy.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mEnergy.getView());
				break;
			case ViewUtil.PHOTO:
				if (mPhoto == null) {
					mPhoto = new Photo(MainActivity.this, mKXApplication,
							mScreenWidth);
					mPhoto.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mPhoto.getView());
				break;
			case ViewUtil.ViDEO:
				if (mPhoto == null) {
					mPhoto = new Photo(MainActivity.this, mKXApplication,
							mScreenWidth);
					mPhoto.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mPhoto.getView());
				break;
			case ViewUtil.GIFTS:
				if (mGifts == null) {
					mGifts = new Gifts(MainActivity.this, mKXApplication);
					mGifts.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mGifts.getView());
				break;
			default:
				break;
			}}
		}
	
	private class MyChangeViewAdapterr implements cn.dennishucd.menu.Desktopr.onChangeViewListener{

		@Override
		public void onChangeView(int arg0) {
			// TODO Auto-generated method stub
			mViewPosition = arg0;
			String tag = null ;
			switch (arg0) {
			case ViewUtil.Interest:
				if (mInterest == null) {
					mInterest = new Interest(MainActivity.this,
							mKXApplication);
					mInterest.setOnOpenListener(MainActivity.this);
				}
				mRoot.close(mInterest.getView());
				break;
			case ViewUtil.God:
				mGroup = new Group(MainActivity.this,
						mKXApplication,"God");
				mGroup.setOnOpenListener(MainActivity.this);
				mRoot.close(mGroup.getView());
				break;
			case ViewUtil.Star:
				mGroup = new Group(MainActivity.this,
						mKXApplication,"Star");
				mGroup.setOnOpenListener(MainActivity.this);
				mRoot.close(mGroup.getView());
				break;
			case ViewUtil.Joke:
				mGroup = new Group(MainActivity.this,
						mKXApplication,"Joke");
				mGroup.setOnOpenListener(MainActivity.this);
				mRoot.close(mGroup.getView());
				break;
			case ViewUtil.Travel:
				mGroup = new Group(MainActivity.this,
						mKXApplication,"Travel");
				mGroup.setOnOpenListener(MainActivity.this);
				mRoot.close(mGroup.getView());
				break;
			case ViewUtil.Food:
				mGroup = new Group(MainActivity.this,
						 mKXApplication,"Food");
				mGroup.setOnOpenListener(MainActivity.this);
				mRoot.close(mGroup.getView());
				break;
			}
		}
	}
}
