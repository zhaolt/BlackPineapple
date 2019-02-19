package com.jease.pineapple.gles;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.jease.pineapple.BuildConfig;
import com.jease.pineapple.gles.filters.NoFilter;

/**
 * Helper class to draw texture to whole view on private thread
 */
public final class RenderHandler implements Runnable {
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String TAG = "RenderHandler";

	private final Object mSync = new Object();
	private EGLContext mEGLContext;
	private boolean isRecordable;
	private Object mSurface;
	private int mTextureId = -1;

	private boolean mRequestSetEglContext;
	private boolean mRequestRelease;
	private int mRequestDraw;
	private Resources mResource;

	public static RenderHandler createHandler(final String name) {
		if (DEBUG) Log.v(TAG, "createHandler:");
		final RenderHandler handler = new RenderHandler();
		synchronized (handler.mSync) {
			new Thread(handler, !TextUtils.isEmpty(name) ? name : TAG).start();
			try {
				handler.mSync.wait();
			} catch (final InterruptedException e) {
			    // ignore
			}
		}
		return handler;
	}

	public final void setEglContext(final EGLContext context, final int textureId, final Object surface, final boolean isRecordable, Resources resources) {
		if (DEBUG) Log.i(TAG, "setEglContext:");
		mResource = resources;
		if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder))
			throw new RuntimeException("unsupported window type:" + surface);
		synchronized (mSync) {
			if (mRequestRelease) return;
			mEGLContext = context;
			mTextureId = textureId;
			mSurface = surface;
			this.isRecordable = isRecordable;
			mRequestSetEglContext = true;
			mSync.notifyAll();
			try {
				mSync.wait();
			} catch (final InterruptedException e) {
			    // ignore
			}
		}
	}

	public void draw() {
		synchronized (mSync) {
			if (mRequestRelease) return;
			mRequestDraw++;
			mSync.notifyAll();
		}
	}

	public boolean isValid() {
		synchronized (mSync) {
			return !(mSurface instanceof Surface) || ((Surface)mSurface).isValid();
		}
	}

	public void release() {
		if (DEBUG) Log.i(TAG, "release:");
		synchronized (mSync) {
			if (mRequestRelease) return;
			mRequestRelease = true;
			mSync.notifyAll();
			try {
				mSync.wait();
			} catch (final InterruptedException e) {
			    // ignore
			}
		}
	}

	private EGLBase mEgl;
	private EGLBase.EglSurface mInputSurface;
	private NoFilter mShowFilter;

	@Override
	public final void run() {
		if (DEBUG) Log.i(TAG, "RenderHandler thread started:");
		synchronized (mSync) {
			mRequestSetEglContext = mRequestRelease = false;
			mRequestDraw = 0;
			mSync.notifyAll();
		}
		boolean localRequestDraw;
		for (;;) {
			synchronized (mSync) {
				if (mRequestRelease) break;
				if (mRequestSetEglContext) {
					mRequestSetEglContext = false;
					internalPrepare();
				}
				localRequestDraw = mRequestDraw > 0;
				if (localRequestDraw) {
					mRequestDraw--;
				}
			}
			if (localRequestDraw) {
				if ((mEgl != null) && mTextureId >= 0) {
					mInputSurface.makeCurrent();
					GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
					GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
					mShowFilter.setTextureId(mTextureId);
					mShowFilter.draw();
					mInputSurface.swap();
				}
			} else {
				synchronized(mSync) {
					try {
						mSync.wait();
					} catch (final InterruptedException e) {
						break;
					}
				}
			}
		}
		synchronized (mSync) {
			mRequestRelease = true;
			internalRelease();
			mSync.notifyAll();
		}
		if (DEBUG) Log.i(TAG, "RenderHandler thread finished:");
	}

	private void internalPrepare() {
		if (DEBUG) Log.i(TAG, "internalPrepare:");
		internalRelease();
		mEgl = new EGLBase(mEGLContext, false, isRecordable);
		mInputSurface = mEgl.createFromSurface(mSurface);
		mInputSurface.makeCurrent();
		mShowFilter = new NoFilter(mResource);
		mShowFilter.create();
		mSurface = null;
		mSync.notifyAll();
	}

	private void internalRelease() {
		if (DEBUG) Log.i(TAG, "internalRelease:");
		if (mInputSurface != null) {
			mInputSurface.release();
			mInputSurface = null;
		}
		if (mEgl != null) {
			mEgl.release();
			mEgl = null;
		}
	}
}
