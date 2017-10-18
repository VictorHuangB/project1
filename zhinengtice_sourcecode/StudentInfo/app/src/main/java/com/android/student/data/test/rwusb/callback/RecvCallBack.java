package com.android.student.data.test.rwusb.callback;

public interface RecvCallBack {
	public void onRecv(byte[] buffer, int byteOffset, int byteCount);
}
