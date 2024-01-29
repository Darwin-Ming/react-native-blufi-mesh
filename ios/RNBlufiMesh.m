//
//  BlufiMesh.m
//  EueApp
//
//  Created by liyi on 2023/5/9.
//  Copyright © 2023 Facebook. All rights reserved.
//

#import "RNBlufiMesh.h"
#import <React/RCTLog.h>
#import "Etouch.h"

@interface BlufiMesh()<CBCentralManagerDelegate, CBPeripheralDelegate, BlufiDelegate>

@property(nonatomic, strong) NSString *filterContent;
@property(strong, nonatomic)EtouchClient *etouchClient;

@end

@implementation BlufiMesh


RCT_EXPORT_MODULE();

//开启蓝牙扫描
RCT_EXPORT_METHOD(startBleScan:(NSString *) params) {
  self.etouchBLEHelper = [EtouchBLEHelper share];
  if(params != nil) {
    NSData *jsonData = [params dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&err];
    if(!err) {
      _filterContent = [dic objectForKey:@"filterName"];
    }
  }
  [self.dataSource removeAllObjects];
  [self.etouchBLEHelper startScan:^(EtouchPeripheral * _Nonnull device) {
      if ([self shouldAddToSource:device]) {
//        NSLog(@"uuid %@",device.uuid);
        [self.dataSource addObject:device];
        [self sendEventWithName: @"BlufiScanBle" body: [device asDictionary]];
      }
  }];
}

//停止蓝牙扫描
RCT_EXPORT_METHOD(stopScanBle) {
  [self.etouchBLEHelper stopScan];
}

//连接设备
RCT_EXPORT_METHOD(connect:(NSString *) uuid) {
  if (_etouchClient) {
      [_etouchClient close];
      _etouchClient = nil;
  }
  _etouchClient = [[EtouchClient alloc] init];
  _etouchClient.centralManagerDelete = self;
  _etouchClient.peripheralDelegate = self;
  _etouchClient.blufiDelegate = self;
  if (uuid) {
    [_etouchClient connect: uuid];
  }
}
//断开设备
RCT_EXPORT_METHOD(disconnect) {
  if (_etouchClient) {
      [_etouchClient requestCloseConnection];
  }
}
//清理
RCT_EXPORT_METHOD(clear) {
  [self onDisconnected];
}

//自定义指令
RCT_EXPORT_METHOD(postCustomData:(NSString *) customData) {
  NSData *data = [customData dataUsingEncoding:NSUTF8StringEncoding];
  [_etouchClient postCustomData:data];
}

//蓝牙配网
RCT_EXPORT_METHOD(configure:(NSString *) ssid pwd:(NSString *) pwd) {
  BlufiConfigureParams *params = [[BlufiConfigureParams alloc] init];
  params.opMode = OpModeSta;
  params.staSsid = ssid;
  params.staPassword = pwd;
  [_etouchClient configure:params];
}

//开启wifi扫描
RCT_EXPORT_METHOD(startWifiScan) {
  if (_etouchClient) {
    [_etouchClient requestDeviceScan];
    
  }
}
- (NSMutableArray *)dataSource {
    if (!_peripheralArray) {
        _peripheralArray = [[NSMutableArray alloc] init];
    }
    return _peripheralArray;
}

- (BOOL)shouldAddToSource:(EtouchPeripheral *)device {
    NSArray *source = [self dataSource];
    // Check filter
    if (_filterContent && _filterContent.length > 0) {
        if (!device.name || ![device.name hasPrefix:_filterContent]) {
            // The device name has no filter prefix
            return NO;
        }
    }
    
    // Check exist
    for (int i = 0; i < source.count; i++) {
        EtouchPeripheral *existDevice = source[i];
        if ([device.uuid isEqual:existDevice.uuid]) {
            // The device exists in source already
//            [self.dataSource replaceObjectAtIndex: i withObject:device];
            return NO;
        }
    }
    
    return YES;
}

- (NSArray<NSString *> *) supportedEvents {
  return @[@"BlufiScanBle",@"BlufiScanWifi",@"BlufiConnect",@"BlufiReceiveCustomData",
           @"BlufiDisconnect",@"BlufiConfigureRes",@"BlufiConfigure"];
}

- (void)centralManagerDidUpdateState:( CBCentralManager *)central {
  
}
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
//    [self updateMessage:@"Connected device"];
//  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
//  [dictionary setObject: @"true" forKey: @"isConnect"];
//  [self sendEventWithName: @"BlufiConnect" body: dictionary];
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
//    [self updateMessage:@"Connet device failed"];
//    self.connected = NO;
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
  [dictionary setObject: @"false" forKey: @"isConnect"];
  [self sendEventWithName: @"BlufiDisconnect" body: dictionary];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
  [self onDisconnected];
//    [self updateMessage:@"Disconnected device"];
//    self.connected = NO;
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
  [dictionary setObject: @"false" forKey: @"isConnect"];
  [self sendEventWithName: @"BlufiDisconnect" body: dictionary];
}

- (void)blufi:(EtouchClient *)client gattPrepared:(BlufiStatusCode)status service:(CBService *)service writeChar:(CBCharacteristic *)writeChar notifyChar:(CBCharacteristic *)notifyChar {
    NSLog(@"Blufi gattPrepared status:%d", status);
    if (status == StatusSuccess) {
//        self.connected = YES;
//        [self updateMessage:@"BluFi connection has prepared"];
//        [self onBlufiPrepared];
      NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
      [dictionary setObject: @"true" forKey: @"isConnect"];
      [self sendEventWithName: @"BlufiConnect" body: dictionary];
    } else {
      [self onDisconnected];
      NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
      [dictionary setObject: @"false" forKey: @"isConnect"];
      [self sendEventWithName: @"BlufiDisconnect" body: dictionary];
//        if (!service) {
//            [self updateMessage:@"Discover service failed"];
//        } else if (!writeChar) {
//            [self updateMessage:@"Discover write char failed"];
//        } else if (!notifyChar) {
//            [self updateMessage:@"Discover notify char failed"];
//        }
    }
}

- (void)blufi:(EtouchClient *)client didPostConfigureParams:(BlufiStatusCode)status {
    NSLog(@"Post configure params status:%d", status);
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject: [NSNumber numberWithInt:status] forKey: @"status"];
    [self sendEventWithName: @"BlufiConfigure" body: dictionary];
}
- (void)blufi:(EtouchClient *)client didReceiveDeviceStatusResponse:(BlufiStatusResponse *)response status:(BlufiStatusCode)status {
    NSLog(@"Post configure params status:%d", status);
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject: [NSNumber numberWithInt:status] forKey: @"status"];
    [self sendEventWithName: @"BlufiConfigureRes" body: dictionary];
}

- (void)blufi:(EtouchClient *)client didReceiveDeviceScanResponse:(NSArray<BlufiScanResponse *> *)scanResults status:(BlufiStatusCode)status {
    NSMutableArray *array = [NSMutableArray array ];
    for (BlufiScanResponse *response in scanResults) {
      NSMutableDictionary *wifi = [NSMutableDictionary dictionary];
      [wifi setObject:response.ssid forKey:@"ssid"];
      [wifi setObject:[NSNumber numberWithInt:response.rssi] forKey:@"rssi"];
      [array addObject:wifi];
    }
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject: [NSNumber numberWithInt:status] forKey: @"status"];
    [dictionary setObject: array forKey: @"scanResults"];
    [self sendEventWithName: @"BlufiScanWifi" body: dictionary];
}
- (void)blufi:(EtouchClient *)client didPostCustomData:(nonnull NSData *)data status:(BlufiStatusCode)status {
    if (status == StatusSuccess) {
//        [self updateMessage:@"Post custom data complete"];
    } else {
      NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
      [dictionary setObject: [NSNumber numberWithInt:status] forKey: @"status"];
      [self sendEventWithName: @"BlufiReceiveCustomData" body: dictionary];
//        [self updateMessage:[NSString stringWithFormat:@"Post custom data failed: %d", status]];
    }
}

- (void)blufi:(EtouchClient *)client didReceiveCustomData:(NSData *)data status:(BlufiStatusCode)status {
    NSString *customString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"Receive device custom data: %@", customString);
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    if (status  == StatusSuccess){
      [dictionary setObject: customString forKey: @"customData"];
    } else {
      [dictionary setObject: [NSNumber numberWithInt:status] forKey: @"status"];
    };
    [self sendEventWithName: @"BlufiReceiveCustomData" body: dictionary];
//    [self updateMessage:[NSString stringWithFormat:@"Receive device custom data: %@", customString]];
}
- (void)blufi:(EtouchClient *)client didReceiveError:(NSInteger)errCode {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject: [NSNumber numberWithInteger:errCode] forKey: @"code"];
    [self sendEventWithName: @"BlufiReceiveCustomData" body: dictionary];
}

- (void)onDisconnected {
    if (_etouchClient) {
        [_etouchClient close];
    }
}
@end
