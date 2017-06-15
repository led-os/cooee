/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\CODE\\GIT\\BeautyCenter\\src\\com\\iLoong\\launcher\\theme\\IThemeService.aidl
 */
package com.iLoong.launcher.theme;
public interface IThemeService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.iLoong.launcher.theme.IThemeService
{
private static final java.lang.String DESCRIPTOR = "com.iLoong.launcher.theme.IThemeService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.iLoong.launcher.theme.IThemeService interface,
 * generating a proxy if needed.
 */
public static com.iLoong.launcher.theme.IThemeService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.iLoong.launcher.theme.IThemeService))) {
return ((com.iLoong.launcher.theme.IThemeService)iin);
}
return new com.iLoong.launcher.theme.IThemeService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_applyTheme:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.applyTheme(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.iLoong.launcher.theme.IThemeService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void applyTheme(java.lang.String themeConfig) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(themeConfig);
mRemote.transact(Stub.TRANSACTION_applyTheme, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_applyTheme = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void applyTheme(java.lang.String themeConfig) throws android.os.RemoteException;
}
