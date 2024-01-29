//
//  EtouchPeripheral.h
//  EueApp
//
//  Created by liyi on 2023/5/11.
//  Copyright © 2023 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

NS_ASSUME_NONNULL_BEGIN

@interface EtouchPeripheral : NSObject

@property(strong, nonatomic)CBPeripheral *peripheral;
@property(strong, nonatomic)NSString *name;
@property(strong, nonatomic)NSUUID *uuid;
@property(assign, nonatomic)NSNumber *rssi;

- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral;

- (NSDictionary *)asDictionary;

@end

NS_ASSUME_NONNULL_END
