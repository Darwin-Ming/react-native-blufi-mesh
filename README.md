
# react-native-blufi-mesh

## 本地集成

将插件文件夹复制到与react native项目同级，然后在react native 项目的package.json文件中增加：

"react-native-blufi-mesh": "file:../react-native-blufi-mesh"

`$ yarn install`

### android
react native项目android目录下的build.gradle文件repositories增加flatDir配置：
```
allprojects { 
	repositories {
		flatDir: dirs project(':react-native-blufi-mesh').file('libs')
	}
}

	编译运行： yarn android
```

### iOS

1. 一定要先yarn install一次
2. pod install
3. 用xcode打开react native项目下ios文件的.xcworkspace文件


## Usage
```javascript

import {
  NativeModules,
  NativeEventEmitter,
} from 'react-native';
const BlufiMesh = NativeModules.BlufiMesh 或者 import RNBlufiMesh from 'react-native-blufi-mesh';
const BlufiMeshEmitter = new NativeEventEmitter(BlufiMesh);
// TODO: What to do with the module?
BlufiMesh;
```

### 插件方法

1. BlufiMesh.startBleScan  开启扫描周边蓝牙设备
2. BlufiMesh.stopScanBle 停止扫描
3. BlufiMesh.connect 连接设备

	android需要传参设备的mac, ios 传参设备的uuid， 示例BlufiMesh.connect(mac 或 uuid)
4. BlufiMesh.postCustomData 自定义接口api

	参数需根据实际技术文档
5. BlufiMesh.configure 配置网络

	参数ssid: wifi名称, wifiPwd：wifi密码，示例BlufiMesh.configure(ssid, wifiPwd)
6. BlufiMesh.disconnect() 断开设备连接
7. BlufiMesh.clear() 销毁控制器

### 事件监听

1. 扫描到设备事件监听 BlufiScanBle 
```
使用示例: 
state 中： this.handleDiscoverBle = this.handleDiscoverBle.bind(this); 
componentDidMount 中：this.handlerDiscoverEvent = BlufiMeshEmitter.addListener( 
		'BlufiScanBle', 
		this.handleDiscoverBle 
	);
	//扫描到设备处理方法
	handleDiscoverBle(bleDevice) {}
```
2. 连接成功事件监听 BlufiConnect
3. 自定义接口调用事件监听 BlufiReceiveCustomData
4. 下发配网指令事件监听 BlufiConfigure
5. 配网结果事件监听 BlufiConfigureRes
6. 断开连接事件监听 BlufiDisconnect
7. 销毁页面时销毁监听事件 

	```
	componentWillUnmount() {
		this.handleDiscoverBle.remove();
	}
	```
  