package com.blufimesh;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blufi.etouch.EtouchCallback;
import blufi.etouch.EtouchClient;
import blufi.etouch.params.BlufiConfigureParams;
import blufi.etouch.params.BlufiParameter;
import blufi.etouch.response.BlufiScanResult;
import blufi.etouch.response.BlufiStatusResponse;
import blufi.etouch.response.BlufiVersionResponse;


public class RNBlufiMeshModule extends ReactContextBaseJavaModule {

  private static ReactApplicationContext reactContext;
  private static final int ENABLE_REQUEST = 539;
  private String filterName;
  private ScanCallback mScanCallback;
  private Callback enableBluetoothCallback;
  private EtouchClient mEtouchClient;
  private Map<String, ScanResult> mDeviceMap = new HashMap<>();
  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == ENABLE_REQUEST && enableBluetoothCallback != null) {
        if (resultCode == RESULT_OK) {
          enableBluetoothCallback.invoke();
        } else {
          enableBluetoothCallback.invoke("User refused to enable");
        }
        enableBluetoothCallback = null;
      }
    }

  };
  public RNBlufiMeshModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    reactContext.addActivityEventListener(mActivityEventListener);
    mScanCallback = new ScanCallback();
  }

  @NonNull
  @Override
  public String getName() {
    return "BlufiMesh";
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }
  @ReactMethod
  public void addListener(String eventName) {
    // Set up any upstream listeners or background tasks as necessary
  }
  @ReactMethod
  public void removeListeners(Integer count) {
    // Remove upstream listeners, stop unnecessary background tasks
  }


  @ReactMethod
  public void startBleScan(String params) throws JSONException, SecurityException {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
    JSONObject jsonObj= null;
    if (params != null) {
      jsonObj = new JSONObject(params);
      filterName = jsonObj.optString("filterName");
      if (mDeviceMap != null && jsonObj.optBoolean("isClear")) {
        mDeviceMap.clear();
      }
    }
    scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
  }

  @ReactMethod
  public void stopScanBle() throws SecurityException{
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
    if (scanner != null && mScanCallback != null) {
      scanner.stopScan(mScanCallback);
    }
  }

  @ReactMethod
  public void connect(String mac) {
    if (mEtouchClient != null) {
      mEtouchClient.close();
      mEtouchClient = null;
    }
    if (mDeviceMap.get(mac) != null) {
      mEtouchClient = new EtouchClient(reactContext, mDeviceMap.get(mac).getDevice());
      mEtouchClient.setGattCallback(new GattCallback());
      mEtouchClient.setBlufiCallback(new BlufiCallbackMain());
      mEtouchClient.connect();
    } else {
      WritableMap listenMap = Arguments.createMap();
      listenMap.putBoolean("isConnect", false);
      sendEvent("BlufiDisconnect", listenMap);
    }
  }

  @ReactMethod
  public void postCustomData(String customData ) {
    if (!TextUtils.isEmpty(customData)) {
      mEtouchClient.postCustomData(customData.getBytes());
    }
  }
  //蓝牙配网
  @ReactMethod
  public void configure(String ssid, String pwd ) {
    BlufiConfigureParams blufiConfigureParams = new BlufiConfigureParams();
    if (!TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(pwd)) {
      blufiConfigureParams.setStaSSIDBytes(ssid.getBytes());
      blufiConfigureParams.setOpMode(BlufiParameter.OP_MODE_STA);
      blufiConfigureParams.setStaPassword(pwd);
      mEtouchClient.configure(blufiConfigureParams);
    }
  }

  @ReactMethod
  public void disconnect() {
    if (mEtouchClient != null) {
      mEtouchClient.requestCloseConnection();
    }
  }

  @ReactMethod
  public void clear() {
    if (mEtouchClient != null){
      mEtouchClient.close();
    }
  }

  //扫描周围蓝牙设备回调
  private class ScanCallback extends android.bluetooth.le.ScanCallback {

    @Override
    public void onScanFailed(int errorCode) {
      super.onScanFailed(errorCode);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
      for (ScanResult result : results) {
        onLeScan(result);
      }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      onLeScan(result);
    }

    private void onLeScan(ScanResult scanResult) throws SecurityException{
      String name=null;
      name = scanResult.getDevice().getName();
      if (!TextUtils.isEmpty(filterName)) {
        if (name == null || !name.startsWith(filterName)) {
          return;
        }
      }
      mDeviceMap.put(scanResult.getDevice().getAddress(), scanResult);
      //发送事件
      WritableMap listenMap = Arguments.createMap();
      listenMap.putString("name", name);
      listenMap.putString("mac", scanResult.getDevice().getAddress()); // mac address
      listenMap.putInt("rssi", scanResult.getRssi());
      sendEvent("BlufiScanBle", listenMap);
    }
  }
  /**
   * mEtouchClient call onCharacteristicWrite and onCharacteristicChanged is required
   */
  private class GattCallback extends BluetoothGattCallback {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) throws SecurityException{
      String devAddr = gatt.getDevice().getAddress();
      if (status == BluetoothGatt.GATT_SUCCESS) {
        switch (newState) {
          case BluetoothProfile.STATE_CONNECTED:
//                        onGattConnected();
            break;
          case BluetoothProfile.STATE_DISCONNECTED:
//            连接断开
            gatt.close();
            WritableMap listenMap = Arguments.createMap();
            listenMap.putBoolean("isConnect", false);
            sendEvent("BlufiDisconnect", listenMap);
//                        onGattDisconnected();
            break;
        }
      } else {
        gatt.close();
//                onGattDisconnected();连接失败
        WritableMap listenMap = Arguments.createMap();
        listenMap.putBoolean("isConnect", false);
        sendEvent("BlufiDisconnect", listenMap);
      }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
      //设置数据包最大传输长度
      if (status != BluetoothGatt.GATT_SUCCESS) {
        mEtouchClient.setPostPackageLengthLimit(20);
      }
//            onGattServiceCharacteristicDiscovered();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) throws SecurityException{
      if (status != BluetoothGatt.GATT_SUCCESS) {
          gatt.disconnect();
      }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) throws SecurityException{
      if (status != BluetoothGatt.GATT_SUCCESS) {
          gatt.disconnect();
      }
    }
  }
  private class BlufiCallbackMain extends EtouchCallback {

    @Override
    public void onGattPrepared(EtouchClient client, BluetoothGatt gatt, BluetoothGattService service,
                               BluetoothGattCharacteristic writeChar, BluetoothGattCharacteristic notifyChar) throws SecurityException{
      if (service == null) {
          gatt.disconnect();
        return;
      }
      if (writeChar == null) {
          gatt.disconnect();
        return;
      }
      if (notifyChar == null) {
          gatt.disconnect();
        return;
      }
      int mtu = 128;
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
              && Build.MANUFACTURER.toLowerCase().startsWith("samsung")) {
        mtu = 23;
      }
      boolean requestMtu = false;
      requestMtu = gatt.requestMtu(mtu);
      if (!requestMtu) {
        client.setPostPackageLengthLimit(20);
//                onGattServiceCharacteristicDiscovered();
      }
      //发送事件
      WritableMap listenMap = Arguments.createMap();
      listenMap.putBoolean("isConnect", true);
      listenMap.putString("mac",gatt.getDevice().getAddress());
      sendEvent("BlufiConnect", listenMap);
    }

    @Override
    public void onNegotiateSecurityResult(EtouchClient client, int status) {
      if (status == STATUS_SUCCESS) {
//                updateMessage("Negotiate security complete", false);
      } else {
//                updateMessage("Negotiate security failed， code=" + status, false);
      }
//            mBlufiSecurityBtn.setEnabled(mConnected);
    }

    @Override
    public void onConfigureResult(EtouchClient client, int status) {
      //发送事件
      WritableMap configMap = Arguments.createMap();
      configMap.putInt("status", status);
      sendEvent("BlufiConfigure", configMap);
//            if (status == STATUS_SUCCESS) {
//                updateMessage("Post configure params complete", false);
//            } else {
//                updateMessage("Post configure params failed, code=" + status, false);
//            }
    }

    @Override
    public void onDeviceStatusResponse(EtouchClient client, int status, BlufiStatusResponse response) {
      //发送事件
      WritableMap configMap = Arguments.createMap();
      configMap.putInt("status", status);
      sendEvent("BlufiConfigureRes", configMap);
      if (status == STATUS_SUCCESS) {
//                updateMessage(String.format("Receive device status response:\n%s", response.generateValidInfo()),
//                        true);
      } else {
//                updateMessage("Device status response error, code=" + status, false);
      }

//            mBlufiDeviceStatusBtn.setEnabled(mConnected);
    }

    @Override
    public void onDeviceScanResult(EtouchClient client, int status, List<BlufiScanResult> results) {
      if (status == STATUS_SUCCESS) {
        StringBuilder msg = new StringBuilder();
        msg.append("Receive device scan result:\n");
        for (BlufiScanResult scanResult : results) {
          msg.append(scanResult.toString()).append("\n");
        }
//                updateMessage(msg.toString(), true);
      } else {
//                updateMessage("Device scan result error, code=" + status, false);
      }
//            mBlufiDeviceScanBtn.setEnabled(mConnected);
    }

    @Override
    public void onDeviceVersionResponse(EtouchClient client, int status, BlufiVersionResponse response) {
      if (status == STATUS_SUCCESS) {
//                updateMessage(String.format("Receive device version: %s", response.getVersionString()),
//                        true);
      } else {
//                updateMessage("Device version error, code=" + status, false);
      }
//            mBlufiVersionBtn.setEnabled(mConnected);
    }

    @Override
    public void onPostCustomDataResult(EtouchClient client, int status, byte[] data) {
      String dataStr = new String(data);
      String format = "Post data %s %s";
      if (status == STATUS_SUCCESS) {
//                updateMessage(String.format(format, dataStr, "complete"), false);
      } else {
        //发送事件
        WritableMap customMap = Arguments.createMap();
        customMap.putInt("status",status);
        sendEvent("BlufiReceiveCustomData", customMap);
//                updateMessage(String.format(format, dataStr, "failed"), false);
      }
    }

    @Override
    public void onReceiveCustomData(EtouchClient client, int status, byte[] data) {
      if (status == STATUS_SUCCESS) {
        String customStr = new String(data);
        //发送事件
        WritableMap customMap = Arguments.createMap();
        customMap.putString("customData",customStr);
        sendEvent("BlufiReceiveCustomData", customMap);
      } else {
        //发送事件
        WritableMap customMap = Arguments.createMap();
        customMap.putInt("status",status);
        sendEvent("BlufiReceiveCustomData", customMap);
      }
    }

    @Override
    public void onError(EtouchClient client, int errCode) {
      //发送事件
      WritableMap customMap = Arguments.createMap();
      customMap.putInt("code",errCode);
      sendEvent("BlufiReceiveCustomData", customMap);
//            updateMessage(String.format(Locale.ENGLISH, "Receive error code %d", errCode), false);
    }
  }
}