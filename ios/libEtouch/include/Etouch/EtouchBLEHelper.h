//
//  EtouchBLEHelper.h
//  EueApp
//
//  Created by liyi on 2023/5/11.
//  Copyright © 2023 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EtouchPeripheral.h"

NS_ASSUME_NONNULL_BEGIN

@interface EtouchBLEHelper : NSObject
typedef void(^FBYBleDeviceBackBlock)(EtouchPeripheral *device);

@property (nonatomic, copy) FBYBleDeviceBackBlock bleScanSuccessBlock;
/**
 * 单例构造方法
 * @return ESPFBYLocalAPI共享实例
 */
+ (instancetype)share;

//停止扫描
- (void)stopScan;
//开始扫描
- (void)startScan:(FBYBleDeviceBackBlock)device;

@end

NS_ASSUME_NONNULL_END
